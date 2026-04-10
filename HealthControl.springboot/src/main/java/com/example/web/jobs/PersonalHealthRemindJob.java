package com.example.web.jobs;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.web.entity.MessageNotice;
import com.example.web.entity.TPersonalHealthRemind;
import com.example.web.mapper.TPersonalHealthRemindMapper;
import com.example.web.service.MessageNoticeService;
import com.example.web.tools.Extension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 每日护嗓提醒任务（对应需求文档 8.1）
 * - RemindType = 1（护嗓）
 * - 根据 RemindTime 每天在指定时间生成 VOICE_CARE 类型消息
 */
@Component
public class PersonalHealthRemindJob {

    @Autowired
    private TPersonalHealthRemindMapper personalHealthRemindMapper;

    @Autowired
    private MessageNoticeService messageNoticeService;

    private static final DateTimeFormatter HM_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * 【测试阶段】每 1 分钟检查一次当前时间点需要触发的护嗓提醒
     * Cron 示例：0 0/1 * * * ?（每 1 分钟）
     * 如需恢复正式环境频率，可改回：0 0/5 * * * ?（每 5 分钟）
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void generateReminderMessages() {
        String nowHm = LocalTime.now().format(HM_FORMATTER);
        LocalDate today = LocalDate.now();

        LambdaQueryWrapper<TPersonalHealthRemind> wrapper = Wrappers.<TPersonalHealthRemind>lambdaQuery()
                .eq(TPersonalHealthRemind::getRemindStatus, 1)
                .eq(TPersonalHealthRemind::getRemindType, 1)
                .eq(TPersonalHealthRemind::getRemindTime, nowHm)
                .ne(TPersonalHealthRemind::getIsDelete, 1);

        List<TPersonalHealthRemind> list = personalHealthRemindMapper.selectList(wrapper);
        if (list == null || list.isEmpty()) {
            return;
        }

        for (TPersonalHealthRemind cfg : list) {
            // 判断"今天不再提醒"（LastPushTime的日期部分等于今天则跳过）
            if (cfg.getLastPushTime() != null) {
                LocalDate lastPushDate = cfg.getLastPushTime().toLocalDate();
                if (lastPushDate.equals(today)) {
                    continue; // 今天已推送过，跳过
                }
            }

            String type = "VOICE_CARE";
            String title = "护嗓小贴士";
            String content = Extension.isNotNullOrEmpty(cfg.getRemindContent())
                    ? cfg.getRemindContent()
                    : "到约定的时间啦，记得保护嗓子，多喝温水、避免用声过度。";

            MessageNotice msg = new MessageNotice();
            msg.setUserId(cfg.getUserId());
            msg.setTitle(title);
            msg.setContent(content);
            msg.setType(type);
            msg.setIsSend(false);
            msg.setIsSuccess(true);
            msg.setIsRead(false);
            msg.setIsDelete(false);
            msg.setPlanSendTime(LocalDateTime.now());

            // 通过 Service 层保存，方便后续扩展
            try {
                messageNoticeService.CreateOrEdit(msg.MapToDto());
            } catch (Exception ex) {
                // 联调阶段出现异常直接跳过该条，避免任务中断
                continue;
            }

            // 更新该配置的 LastPushTime
            cfg.setLastPushTime(LocalDateTime.now());
            personalHealthRemindMapper.updateById(cfg);
        }
    }
}

