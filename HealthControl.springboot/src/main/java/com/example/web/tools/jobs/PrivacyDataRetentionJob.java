package com.example.web.tools.jobs;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.web.entity.*;
import com.example.web.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 按用户隐私设置中的“数据留存与自动清理”策略，定期清理超期历史数据
 *
 * 说明：
 * - 仅对开启 DataRetentionEnabled 且设置了 DataRetentionMonths 的用户生效
 * - 以 CreationTime（或等价时间字段）早于阈值的数据做软删除（IsDelete = true）
 * - 出于合规与建模考虑，这里不会自动清理模型优化标注记录（TModelOptimizeLabel）
 */
@Slf4j
@Component
public class PrivacyDataRetentionJob {

    @Autowired
    private TUserPrivacySettingMapper privacySettingMapper;

    @Autowired
    private TAudioScreenRecordMapper audioScreenRecordMapper;

    @Autowired
    private TPersonalLaryngealHealthRecordMapper healthRecordMapper;

    @Autowired
    private TSymptomLogMapper symptomLogMapper;

    @Autowired
    private TUserDietRecordMapper dietRecordMapper;

    @Autowired
    private TPersonalHealthRemindMapper personalHealthRemindMapper;

    @Autowired
    private MessageNoticeMapper messageNoticeMapper;

    @Autowired
    private TUserQuestionnaireAnswerMapper questionnaireAnswerMapper;

    /**
     * 每天凌晨 3:30 扫描一次需要自动清理的用户数据
     */
    @Scheduled(cron = "0 30 3 * * ?")
    public void cleanupByRetentionSetting() {
        List<TUserPrivacySetting> list = privacySettingMapper.selectList(
                Wrappers.<TUserPrivacySetting>lambdaQuery()
                        .eq(TUserPrivacySetting::getDataRetentionEnabled, true)
                        .isNotNull(TUserPrivacySetting::getDataRetentionMonths)
                        .gt(TUserPrivacySetting::getDataRetentionMonths, 0)
                        .and(w -> w.isNull(TUserPrivacySetting::getIsDelete).or().eq(TUserPrivacySetting::getIsDelete, false))
        );
        if (list == null || list.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        for (TUserPrivacySetting setting : list) {
            try {
                if (setting.getUserId() == null || setting.getUserId() <= 0) {
                    continue;
                }
                Integer months = setting.getDataRetentionMonths();
                if (months == null || months <= 0) {
                    // 0 或 null 表示永久保留
                    continue;
                }
                LocalDateTime threshold = now.minusMonths(months);
                Integer userId = setting.getUserId();

                int affected = 0;

                // 自查音频记录
                affected += audioScreenRecordMapper.update(null,
                        Wrappers.<TAudioScreenRecord>lambdaUpdate()
                                .set(TAudioScreenRecord::getIsDelete, true)
                                .eq(TAudioScreenRecord::getUserId, userId)
                                .lt(TAudioScreenRecord::getCreationTime, threshold)
                                .and(w -> w.isNull(TAudioScreenRecord::getIsDelete)
                                        .or().eq(TAudioScreenRecord::getIsDelete, false))
                );

                // 健康档案
                affected += healthRecordMapper.update(null,
                        Wrappers.<TPersonalLaryngealHealthRecord>lambdaUpdate()
                                .set(TPersonalLaryngealHealthRecord::getIsDelete, true)
                                .eq(TPersonalLaryngealHealthRecord::getUserId, userId)
                                .lt(TPersonalLaryngealHealthRecord::getCreationTime, threshold)
                                .and(w -> w.isNull(TPersonalLaryngealHealthRecord::getIsDelete)
                                        .or().eq(TPersonalLaryngealHealthRecord::getIsDelete, false))
                );

                // 症状日志
                affected += symptomLogMapper.update(null,
                        Wrappers.<TSymptomLog>lambdaUpdate()
                                .set(TSymptomLog::getIsDelete, true)
                                .eq(TSymptomLog::getUserId, userId)
                                .lt(TSymptomLog::getCreationTime, threshold)
                                .and(w -> w.isNull(TSymptomLog::getIsDelete)
                                        .or().eq(TSymptomLog::getIsDelete, false))
                );

                // 饮食记录：优先按摄入时间判断，其次创建时间
                affected += dietRecordMapper.update(null,
                        Wrappers.<TUserDietRecord>lambdaUpdate()
                                .set(TUserDietRecord::getIsDelete, true)
                                .eq(TUserDietRecord::getUserId, userId)
                                .and(w -> w
                                        .lt(TUserDietRecord::getIntakeTime, threshold)
                                        .or()
                                        .lt(TUserDietRecord::getCreationTime, threshold))
                                .and(w -> w.isNull(TUserDietRecord::getIsDelete)
                                        .or().eq(TUserDietRecord::getIsDelete, false))
                );

                // 个性化提醒设置
                affected += personalHealthRemindMapper.update(null,
                        Wrappers.<TPersonalHealthRemind>lambdaUpdate()
                                .set(TPersonalHealthRemind::getIsDelete, true)
                                .eq(TPersonalHealthRemind::getUserId, userId)
                                .lt(TPersonalHealthRemind::getCreationTime, threshold)
                                .and(w -> w.isNull(TPersonalHealthRemind::getIsDelete)
                                        .or().eq(TPersonalHealthRemind::getIsDelete, false))
                );

                // 消息通知记录
                affected += messageNoticeMapper.update(null,
                        Wrappers.<MessageNotice>lambdaUpdate()
                                .set(MessageNotice::getIsDelete, true)
                                .eq(MessageNotice::getUserId, userId)
                                .lt(MessageNotice::getCreationTime, threshold)
                                .and(w -> w.isNull(MessageNotice::getIsDelete)
                                        .or().eq(MessageNotice::getIsDelete, false))
                );

                // 问卷答题记录
                affected += questionnaireAnswerMapper.update(null,
                        Wrappers.<TUserQuestionnaireAnswer>lambdaUpdate()
                                .set(TUserQuestionnaireAnswer::getIsDelete, true)
                                .eq(TUserQuestionnaireAnswer::getUserId, userId)
                                .lt(TUserQuestionnaireAnswer::getCreationTime, threshold)
                                .and(w -> w.isNull(TUserQuestionnaireAnswer::getIsDelete)
                                        .or().eq(TUserQuestionnaireAnswer::getIsDelete, false))
                );

                if (affected > 0) {
                    log.info("自动清理用户历史数据：UserId={}, Months={}, Affected={}", userId, months, affected);
                }
            } catch (Exception ex) {
                log.error("执行数据留存自动清理失败，UserId={}", setting.getUserId(), ex);
            }
        }
    }
}

