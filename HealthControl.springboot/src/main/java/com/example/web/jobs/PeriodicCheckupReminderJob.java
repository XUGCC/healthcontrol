package com.example.web.jobs;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.web.entity.MessageNotice;
import com.example.web.entity.TPersonalHealthRemind;
import com.example.web.entity.TPersonalLaryngealHealthRecord;
import com.example.web.mapper.TPersonalHealthRemindMapper;
import com.example.web.mapper.TPersonalLaryngealHealthRecordMapper;
import com.example.web.service.MessageNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 周期自查提醒任务（需求文档 8.2）
 * - RemindType = 0（定期自查）
 * - 根据 LastScreenTime 与 RepeatFrequency 判断是否需要生成 CHECKUP 提醒
 */
@Component
public class PeriodicCheckupReminderJob {

    @Autowired
    private TPersonalHealthRemindMapper personalHealthRemindMapper;

    @Autowired
    private TPersonalLaryngealHealthRecordMapper healthRecordMapper;

    @Autowired
    private MessageNoticeService messageNoticeService;

    /**
     * 【测试阶段】每 1 分钟执行一次周期自查提醒扫描
     * Cron 示例：0 0/1 * * * ?（每 1 分钟）
     * 正式环境可改回：0 30 0 * * ?（每天 00:30）
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void generatePeriodicCheckupReminders() {
        LambdaQueryWrapper<TPersonalHealthRemind> wrapper = Wrappers.<TPersonalHealthRemind>lambdaQuery()
                .eq(TPersonalHealthRemind::getRemindType, 0)
                .eq(TPersonalHealthRemind::getRemindStatus, 1)
                .ne(TPersonalHealthRemind::getIsDelete, 1);

        List<TPersonalHealthRemind> configs = personalHealthRemindMapper.selectList(wrapper);
        if (configs == null || configs.isEmpty()) {
            return;
        }

        LocalDate today = LocalDate.now();

        for (TPersonalHealthRemind cfg : configs) {
            if (cfg.getUserId() == null) {
                continue;
            }

            // 查询该用户最新的健康档案
            LambdaQueryWrapper<TPersonalLaryngealHealthRecord> hrWrapper = Wrappers
                    .<TPersonalLaryngealHealthRecord>lambdaQuery()
                    .eq(TPersonalLaryngealHealthRecord::getUserId, cfg.getUserId())
                    .eq(TPersonalLaryngealHealthRecord::getIsDelete, false)
                    .orderByDesc(TPersonalLaryngealHealthRecord::getCreationTime)
                    .last("LIMIT 1");

            TPersonalLaryngealHealthRecord record = healthRecordMapper.selectOne(hrWrapper);

            LocalDate lastScreenDate = null;
            if (record != null && record.getLastScreenTime() != null) {
                lastScreenDate = record.getLastScreenTime().toLocalDate();
            }

            int freqDays = mapRepeatFrequencyToDays(cfg.getRepeatFrequency());

            boolean needRemind;
            if (lastScreenDate == null) {
                // 没有自查记录时，视为需要提醒
                needRemind = true;
            } else {
                long diff = ChronoUnit.DAYS.between(lastScreenDate, today);
                needRemind = diff >= freqDays;
            }

            // 判断"今天不再提醒"（LastPushTime的日期部分等于今天则跳过）
            if (cfg.getLastPushTime() != null) {
                LocalDate lastPushDate = cfg.getLastPushTime().toLocalDate();
                if (lastPushDate.equals(today)) {
                    continue; // 今天已推送过，跳过
                }
            }

            // 使用 LastPushTime 做基础去重控制，避免一天内重复多次
            if (cfg.getLastPushTime() != null
                    && !today.isAfter(cfg.getLastPushTime().toLocalDate())) {
                needRemind = false;
            }

            if (!needRemind) {
                continue;
            }

            // 获取用户风险等级，个性化推送内容
            Integer riskLevel = getRiskLevel(cfg.getUserId(), record);
            String content = buildContent(riskLevel);

            MessageNotice msg = new MessageNotice();
            msg.setUserId(cfg.getUserId());
            msg.setTitle("定期自查提醒");
            msg.setContent(content);
            msg.setType("CHECKUP");
            msg.setIsSend(false);
            msg.setIsSuccess(true);
            msg.setIsRead(false);
            msg.setIsDelete(false);
            msg.setPlanSendTime(LocalDateTime.now());
            msg.setActionType("GOTO_AUDIO_DETECT");

            try {
                messageNoticeService.CreateOrEdit(msg.MapToDto());
            } catch (Exception ex) {
                continue;
            }

            cfg.setLastPushTime(LocalDateTime.now());
            personalHealthRemindMapper.updateById(cfg);
        }
    }

    /**
     * 将 RepeatFrequency 映射为间隔天数
     * 0=每天(1天)、1=工作日(1天，按天检查)、2=每周一次(7天)、3=每30天一次
     */
    private int mapRepeatFrequencyToDays(Integer repeatFrequency) {
        if (repeatFrequency == null) {
            return 7;
        }
        // 简化映射：0=每天(1天)、1=工作日(1天)、2=每周一次(7天)、3=每30天一次
        switch (repeatFrequency) {
            case 0:
            case 1:
                return 1;
            case 2:
                return 7;
            case 3:
                return 30;
            default:
                return 7;
        }
    }

    /**
     * 获取用户风险等级
     * @param userId 用户ID
     * @param record 健康档案记录
     * @return 风险等级：0=未评定，1=低风险，2=中风险，3=高风险
     */
    private Integer getRiskLevel(Integer userId, TPersonalLaryngealHealthRecord record) {
        if (record == null) {
            return 0; // 未评定
        }
        // RiskAssessmentLevel: 0=未评定，1=低风险，2=中风险，3=高风险
        return record.getRiskAssessmentLevel() != null ? record.getRiskAssessmentLevel() : 0;
    }

    /**
     * 根据风险等级构建个性化推送内容
     */
    private String buildContent(Integer riskLevel) {
        if (riskLevel == null || riskLevel == 0) {
            return "建议您根据提醒完成一次喉部自查，了解当前嗓音健康情况。保持良好习惯，定期自查。";
        } else if (riskLevel == 1) {
            return "建议您根据提醒完成一次喉部自查，了解当前嗓音健康情况。当前为低风险，每30天一次自查即可。";
        } else if (riskLevel == 2) {
            return "建议您根据提醒完成一次喉部自查，了解当前嗓音健康情况。当前为中风险，请关注症状变化，建议每14天左右自查一次。";
        } else {
            return "建议您根据提醒完成一次喉部自查，了解当前嗓音健康情况。当前为高风险，建议结合就医建议，定期复查，建议每7天自查一次。";
        }
    }
}

