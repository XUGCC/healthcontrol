package com.example.web.tools.jobs;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.web.entity.*;
import com.example.web.mapper.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据删除申请异步处理（MVP：仅做软删除）
 *
 * 说明：
 * - 这里以“用户主动发起删除申请 -> 系统后台异步处理”为闭环实现
 * - 若后续需要管理员审核，可在此处增加审核状态字段与处理条件
 */
@Slf4j
@Component
public class PrivacyDeleteRequestJob {

    @Autowired
    private TDataDeleteRequestMapper deleteRequestMapper;

    @Autowired
    private TAudioScreenRecordMapper audioScreenRecordMapper;

    @Autowired
    private TPersonalLaryngealHealthRecordMapper healthRecordMapper;

    @Autowired
    private TSymptomLogMapper symptomLogMapper;

    @Autowired
    private TUserDietRecordMapper dietRecordMapper;

    @Autowired
    private TModelOptimizeLabelMapper modelOptimizeLabelMapper;

    @Autowired
    private TPersonalHealthRemindMapper personalHealthRemindMapper;

    @Autowired
    private MessageNoticeMapper messageNoticeMapper;

    @Autowired
    private TUserQuestionnaireAnswerMapper questionnaireAnswerMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 每 1 分钟扫描一次 Pending 的删除申请，逐条处理（避免一次性删除压力过大）
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void processPendingDeleteRequests() {
        List<TDataDeleteRequest> pending = deleteRequestMapper.selectList(
                Wrappers.<TDataDeleteRequest>lambdaQuery()
                        // 变更：仅处理管理员已审核通过的删除申请
                        .eq(TDataDeleteRequest::getStatus, "Approved")
                        .and(w -> w.isNull(TDataDeleteRequest::getIsDelete).or().eq(TDataDeleteRequest::getIsDelete, false))
                        .orderByAsc(TDataDeleteRequest::getCreationTime)
                        .last("LIMIT 3")
        );
        if (pending == null || pending.isEmpty()) return;

        for (TDataDeleteRequest req : pending) {
            try {
                if (req.getUserId() == null || req.getUserId() <= 0) {
                    markFailed(req, "UserId无效");
                    continue;
                }

                // 标记处理中
                req.setStatus("Processing");
                req.setUpdateTime(LocalDateTime.now());
                deleteRequestMapper.updateById(req);

                List<String> types = parseDataTypes(req.getDataType());
                int total = 0;

                // 注意：这里采用“软删除”为主，仅更新 IsDelete=true
                if (types.contains("All") || types.contains("AudioRecord")) {
                    total += audioScreenRecordMapper.update(null,
                            Wrappers.<TAudioScreenRecord>lambdaUpdate()
                                    .set(TAudioScreenRecord::getIsDelete, true)
                                    .eq(TAudioScreenRecord::getUserId, req.getUserId())
                                    .and(w -> w.isNull(TAudioScreenRecord::getIsDelete).or().eq(TAudioScreenRecord::getIsDelete, false))
                    );
                }
                if (types.contains("All") || types.contains("HealthRecord")) {
                    total += healthRecordMapper.update(null,
                            Wrappers.<TPersonalLaryngealHealthRecord>lambdaUpdate()
                                    .set(TPersonalLaryngealHealthRecord::getIsDelete, true)
                                    .eq(TPersonalLaryngealHealthRecord::getUserId, req.getUserId())
                                    .and(w -> w.isNull(TPersonalLaryngealHealthRecord::getIsDelete).or().eq(TPersonalLaryngealHealthRecord::getIsDelete, false))
                    );
                }
                if (types.contains("All") || types.contains("SymptomLog")) {
                    total += symptomLogMapper.update(null,
                            Wrappers.<TSymptomLog>lambdaUpdate()
                                    .set(TSymptomLog::getIsDelete, true)
                                    .eq(TSymptomLog::getUserId, req.getUserId())
                                    .and(w -> w.isNull(TSymptomLog::getIsDelete).or().eq(TSymptomLog::getIsDelete, false))
                    );
                }
                if (types.contains("All") || types.contains("DietRecord")) {
                    total += dietRecordMapper.update(null,
                            Wrappers.<TUserDietRecord>lambdaUpdate()
                                    .set(TUserDietRecord::getIsDelete, true)
                                    .eq(TUserDietRecord::getUserId, req.getUserId())
                                    .and(w -> w.isNull(TUserDietRecord::getIsDelete).or().eq(TUserDietRecord::getIsDelete, false))
                    );
                }
                if (types.contains("All") || types.contains("ModelLabel")) {
                    total += modelOptimizeLabelMapper.update(null,
                            Wrappers.<TModelOptimizeLabel>lambdaUpdate()
                                    .set(TModelOptimizeLabel::getIsDelete, true)
                                    .eq(TModelOptimizeLabel::getUserId, req.getUserId())
                                    .and(w -> w.isNull(TModelOptimizeLabel::getIsDelete).or().eq(TModelOptimizeLabel::getIsDelete, false))
                    );
                }
                if (types.contains("All") || types.contains("HealthRemind")) {
                    total += personalHealthRemindMapper.update(null,
                            Wrappers.<TPersonalHealthRemind>lambdaUpdate()
                                    .set(TPersonalHealthRemind::getIsDelete, true)
                                    .eq(TPersonalHealthRemind::getUserId, req.getUserId())
                                    .and(w -> w.isNull(TPersonalHealthRemind::getIsDelete).or().eq(TPersonalHealthRemind::getIsDelete, false))
                    );
                }
                if (types.contains("All") || types.contains("MessageNotice")) {
                    total += messageNoticeMapper.update(null,
                            Wrappers.<MessageNotice>lambdaUpdate()
                                    .set(MessageNotice::getIsDelete, true)
                                    .eq(MessageNotice::getUserId, req.getUserId())
                                    .and(w -> w.isNull(MessageNotice::getIsDelete).or().eq(MessageNotice::getIsDelete, false))
                    );
                }
                if (types.contains("All") || types.contains("QuestionnaireAnswer")) {
                    total += questionnaireAnswerMapper.update(null,
                            Wrappers.<TUserQuestionnaireAnswer>lambdaUpdate()
                                    .set(TUserQuestionnaireAnswer::getIsDelete, true)
                                    .eq(TUserQuestionnaireAnswer::getUserId, req.getUserId())
                                    .and(w -> w.isNull(TUserQuestionnaireAnswer::getIsDelete).or().eq(TUserQuestionnaireAnswer::getIsDelete, false))
                    );
                }

                // 完成
                req.setStatus("Completed");
                req.setDeleteCount(total);
                req.setProcessTime(LocalDateTime.now());
                req.setErrorMessage(null);
                req.setUpdateTime(LocalDateTime.now());
                deleteRequestMapper.updateById(req);
            } catch (Exception ex) {
                markFailed(req, ex.getMessage());
                log.error("处理删除申请失败，RequestId={}", req.getId(), ex);
            }
        }
    }

    private void markFailed(TDataDeleteRequest req, String msg) {
        try {
            req.setStatus("Failed");
            req.setErrorMessage(msg);
            req.setProcessTime(LocalDateTime.now());
            req.setUpdateTime(LocalDateTime.now());
            deleteRequestMapper.updateById(req);
        } catch (Exception ignore) {
        }
    }

    private List<String> parseDataTypes(String raw) {
        List<String> res = new ArrayList<>();
        if (raw == null) return res;
        String s = raw.trim();
        if (s.isEmpty()) return res;
        if (s.startsWith("[")) {
            try {
                String[] arr = objectMapper.readValue(s, String[].class);
                for (String a : arr) {
                    if (a != null && !a.trim().isEmpty()) res.add(a.trim());
                }
                return res;
            } catch (Exception ignore) {
            }
        }
        for (String p : s.split(",")) {
            if (p != null && !p.trim().isEmpty()) res.add(p.trim());
        }
        return res;
    }
}

