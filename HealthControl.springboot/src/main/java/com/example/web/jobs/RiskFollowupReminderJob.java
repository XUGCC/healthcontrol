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
 * 风险随访提醒任务（需求文档 8.3）
 * - 针对中高风险用户（RiskAssessmentLevel=1/2）
 * - 结合随访提醒配置生成 FOLLOWUP 类型消息
 */
@Component
public class RiskFollowupReminderJob {

    @Autowired
    private TPersonalLaryngealHealthRecordMapper healthRecordMapper;

    @Autowired
    private TPersonalHealthRemindMapper personalHealthRemindMapper;

    @Autowired
    private MessageNoticeService messageNoticeService;

    /**
     * 【测试阶段】每 2 分钟扫描一次高风险/中风险用户
     * Cron 示例：0 0/2 * * * ?（每 2 分钟）
     * 正式环境可改回：0 0 9 ? * MON（每周一 09:00）
     */
    @Scheduled(cron = "0 */2 * * * ?")
    public void generateRiskFollowupReminders() {
        LambdaQueryWrapper<TPersonalLaryngealHealthRecord> hrWrapper = Wrappers
                .<TPersonalLaryngealHealthRecord>lambdaQuery()
                .in(TPersonalLaryngealHealthRecord::getRiskAssessmentLevel, 1, 2)
                .eq(TPersonalLaryngealHealthRecord::getIsDelete, false);

        List<TPersonalLaryngealHealthRecord> records = healthRecordMapper.selectList(hrWrapper);
        if (records == null || records.isEmpty()) {
            return;
        }

        LocalDate today = LocalDate.now();

        for (TPersonalLaryngealHealthRecord record : records) {
            if (record.getUserId() == null) {
                continue;
            }

            // 查找该用户的随访提醒配置（约定 RemindType=4）
            LambdaQueryWrapper<TPersonalHealthRemind> cfgWrapper = Wrappers
                    .<TPersonalHealthRemind>lambdaQuery()
                    .eq(TPersonalHealthRemind::getUserId, record.getUserId())
                    .eq(TPersonalHealthRemind::getRemindType, 4)
                    .eq(TPersonalHealthRemind::getRemindStatus, 1)
                    .ne(TPersonalHealthRemind::getIsDelete, 1)
                    .last("LIMIT 1");

            TPersonalHealthRemind cfg = personalHealthRemindMapper.selectOne(cfgWrapper);
            if (cfg == null) {
                continue;
            }

            int risk = record.getRiskAssessmentLevel() == null ? 0 : record.getRiskAssessmentLevel();
            int intervalDays = (risk == 2) ? 7 : 14;

            if (cfg.getLastPushTime() != null) {
                long diff = ChronoUnit.DAYS.between(cfg.getLastPushTime().toLocalDate(), today);
                if (diff < intervalDays) {
                    continue;
                }
            }

            String title = risk == 2 ? "高风险随访提醒" : "风险随访提醒";
            StringBuilder content = new StringBuilder();
            if (risk == 2) {
                content.append("根据您的档案评估，当前为高风险状态，建议尽快前往耳鼻喉相关专科就诊，并携带近期自查记录。");
            } else {
                content.append("根据您的档案评估，当前喉部风险偏高，建议关注近期症状变化，必要时前往门诊复查。");
            }
            if (record.getLastScreenTime() != null) {
                content.append(" 最近一次自查时间：").append(record.getLastScreenTime().toLocalDate()).append("。");
            }

            MessageNotice msg = new MessageNotice();
            msg.setUserId(record.getUserId());
            msg.setTitle(title);
            msg.setContent(content.toString());
            msg.setType("FOLLOWUP");
            msg.setIsSend(false);
            msg.setIsSuccess(true);
            msg.setIsRead(false);
            msg.setPlanSendTime(LocalDateTime.now());
            msg.setActionType("GOTO_HEALTH_RECORD");

            try {
                messageNoticeService.CreateOrEdit(msg.MapToDto());
            } catch (Exception ex) {
                continue;
            }

            cfg.setLastPushTime(LocalDateTime.now());
            personalHealthRemindMapper.updateById(cfg);
        }
    }
}

