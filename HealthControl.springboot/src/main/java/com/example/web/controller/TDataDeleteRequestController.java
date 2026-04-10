package com.example.web.controller;

import com.example.web.dto.TDataDeleteRequestDto;
import com.example.web.dto.query.TDataDeleteRequestPagedInput;
import com.example.web.entity.TDataDeleteRequest;
import com.example.web.enums.RoleTypeEnum;
import com.example.web.entity.*;
import com.example.web.mapper.*;
import com.example.web.mapper.TDataDeleteRequestMapper;
import com.example.web.service.TDataDeleteRequestService;
import com.example.web.tools.BaseContext;
import com.example.web.tools.dto.PagedResult;
import com.example.web.tools.exception.CustomException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 数据删除申请控制器（管理端）
 */
@RestController
@RequestMapping("/TDataDeleteRequest")
public class TDataDeleteRequestController {

    @Autowired
    private TDataDeleteRequestService dataDeleteRequestService;

    @Autowired
    private TDataDeleteRequestMapper deleteRequestMapper;

    @Autowired
    private ObjectMapper objectMapper;

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

    /**
     * 数据删除申请分页查询
     */
    @RequestMapping(value = "/List", method = RequestMethod.POST)
    @SneakyThrows
    public PagedResult<TDataDeleteRequestDto> List(@RequestBody TDataDeleteRequestPagedInput input) {
        return dataDeleteRequestService.List(input);
    }

    /**
     * 导出数据删除申请列表
     */
    @RequestMapping(value = "/Export", method = RequestMethod.GET)
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException {
        dataDeleteRequestService.Export(query, response);
    }

    /**
     * 管理端审核（通过/拒绝）删除申请
     * 入参：{ Id: number, Action: "Approve"|"Reject", Remark?: string }
     */
    @RequestMapping(value = "/Audit", method = RequestMethod.POST)
    public void Audit(@RequestBody Map<String, Object> input) {
        requireAdmin();
        if (input == null) throw new CustomException("请求体不能为空");
        Integer id = input.get("Id") == null ? null : Integer.valueOf(String.valueOf(input.get("Id")));
        String action = input.get("Action") == null ? null : String.valueOf(input.get("Action"));
        String remark = input.get("Remark") == null ? null : String.valueOf(input.get("Remark"));
        if (id == null || id <= 0) throw new CustomException("Id不能为空");
        if (action == null || action.trim().isEmpty()) throw new CustomException("Action不能为空");

        TDataDeleteRequest req = deleteRequestMapper.selectById(id);
        if (req == null) throw new CustomException("删除申请不存在");
        // 仅允许审核待审核状态
        if (!"PendingAudit".equals(req.getStatus())) {
            throw new CustomException("仅待审核状态可审核，当前状态：" + req.getStatus());
        }

        LocalDateTime now = LocalDateTime.now();
        if ("Approve".equalsIgnoreCase(action)) {
            // 变更：审核通过后立即执行删除并标记完成
            try {
                req.setStatus("Processing");
                req.setErrorMessage(null);
                req.setUpdateTime(now);
                deleteRequestMapper.updateById(req);

                int total = executeDelete(req);

                req.setStatus("Completed");
                req.setDeleteCount(total);
                req.setProcessTime(LocalDateTime.now());
                req.setErrorMessage(null);
                req.setUpdateTime(LocalDateTime.now());
                deleteRequestMapper.updateById(req);
                return;
            } catch (Exception ex) {
                req.setStatus("Failed");
                req.setErrorMessage(ex.getMessage());
                req.setProcessTime(LocalDateTime.now());
                req.setUpdateTime(LocalDateTime.now());
                deleteRequestMapper.updateById(req);
                throw new CustomException("删除执行失败：" + ex.getMessage());
            }
        }
        if ("Reject".equalsIgnoreCase(action)) {
            req.setStatus("Rejected");
            req.setErrorMessage(remark); // 复用 ErrorMessage 存放拒绝原因
            req.setProcessTime(now);
            req.setUpdateTime(now);
            deleteRequestMapper.updateById(req);
            return;
        }
        throw new CustomException("Action仅支持 Approve / Reject");
    }

    /**
     * 批量删除删除申请（软删除 IsDelete=true）
     */
    @RequestMapping(value = "/BatchDelete", method = RequestMethod.POST)
    public void BatchDelete(@RequestBody com.example.web.tools.dto.IdsInput input) {
        requireAdmin();
        dataDeleteRequestService.BatchDelete(input);
    }

    private void requireAdmin() {
        if (BaseContext.getCurrentUserDto() == null
                || BaseContext.getCurrentUserDto().getRoleType() == null
                || BaseContext.getCurrentUserDto().getRoleType() != RoleTypeEnum.管理员) {
            throw new CustomException("仅管理员可操作");
        }
    }

    /**
     * 立即执行软删除，返回本次更新的记录数
     */
    private int executeDelete(TDataDeleteRequest req) {
        if (req.getUserId() == null || req.getUserId() <= 0) {
            throw new CustomException("UserId无效");
        }
        List<String> types = parseDataTypes(req.getDataType());
        int total = 0;

        // 注意：这里采用“软删除”为主，仅更新 IsDelete=true
        if (types.contains("All") || types.contains("AudioRecord")) {
            total += audioScreenRecordMapper.update(null,
                    com.baomidou.mybatisplus.core.toolkit.Wrappers.<TAudioScreenRecord>lambdaUpdate()
                            .set(TAudioScreenRecord::getIsDelete, true)
                            .eq(TAudioScreenRecord::getUserId, req.getUserId())
                            .and(w -> w.isNull(TAudioScreenRecord::getIsDelete).or().eq(TAudioScreenRecord::getIsDelete, false))
            );
        }
        if (types.contains("All") || types.contains("HealthRecord")) {
            total += healthRecordMapper.update(null,
                    com.baomidou.mybatisplus.core.toolkit.Wrappers.<TPersonalLaryngealHealthRecord>lambdaUpdate()
                            .set(TPersonalLaryngealHealthRecord::getIsDelete, true)
                            .eq(TPersonalLaryngealHealthRecord::getUserId, req.getUserId())
                            .and(w -> w.isNull(TPersonalLaryngealHealthRecord::getIsDelete).or().eq(TPersonalLaryngealHealthRecord::getIsDelete, false))
            );
        }
        if (types.contains("All") || types.contains("SymptomLog")) {
            total += symptomLogMapper.update(null,
                    com.baomidou.mybatisplus.core.toolkit.Wrappers.<TSymptomLog>lambdaUpdate()
                            .set(TSymptomLog::getIsDelete, true)
                            .eq(TSymptomLog::getUserId, req.getUserId())
                            .and(w -> w.isNull(TSymptomLog::getIsDelete).or().eq(TSymptomLog::getIsDelete, false))
            );
        }
        if (types.contains("All") || types.contains("DietRecord")) {
            total += dietRecordMapper.update(null,
                    com.baomidou.mybatisplus.core.toolkit.Wrappers.<TUserDietRecord>lambdaUpdate()
                            .set(TUserDietRecord::getIsDelete, true)
                            .eq(TUserDietRecord::getUserId, req.getUserId())
                            .and(w -> w.isNull(TUserDietRecord::getIsDelete).or().eq(TUserDietRecord::getIsDelete, false))
            );
        }
        if (types.contains("All") || types.contains("ModelLabel")) {
            total += modelOptimizeLabelMapper.update(null,
                    com.baomidou.mybatisplus.core.toolkit.Wrappers.<TModelOptimizeLabel>lambdaUpdate()
                            .set(TModelOptimizeLabel::getIsDelete, true)
                            .eq(TModelOptimizeLabel::getUserId, req.getUserId())
                            .and(w -> w.isNull(TModelOptimizeLabel::getIsDelete).or().eq(TModelOptimizeLabel::getIsDelete, false))
            );
        }
        if (types.contains("All") || types.contains("HealthRemind")) {
            total += personalHealthRemindMapper.update(null,
                    com.baomidou.mybatisplus.core.toolkit.Wrappers.<TPersonalHealthRemind>lambdaUpdate()
                            .set(TPersonalHealthRemind::getIsDelete, true)
                            .eq(TPersonalHealthRemind::getUserId, req.getUserId())
                            .and(w -> w.isNull(TPersonalHealthRemind::getIsDelete).or().eq(TPersonalHealthRemind::getIsDelete, false))
            );
        }
        if (types.contains("All") || types.contains("MessageNotice")) {
            total += messageNoticeMapper.update(null,
                    com.baomidou.mybatisplus.core.toolkit.Wrappers.<MessageNotice>lambdaUpdate()
                            .set(MessageNotice::getIsDelete, true)
                            .eq(MessageNotice::getUserId, req.getUserId())
                            .and(w -> w.isNull(MessageNotice::getIsDelete).or().eq(MessageNotice::getIsDelete, false))
            );
        }
        if (types.contains("All") || types.contains("QuestionnaireAnswer")) {
            total += questionnaireAnswerMapper.update(null,
                    com.baomidou.mybatisplus.core.toolkit.Wrappers.<TUserQuestionnaireAnswer>lambdaUpdate()
                            .set(TUserQuestionnaireAnswer::getIsDelete, true)
                            .eq(TUserQuestionnaireAnswer::getUserId, req.getUserId())
                            .and(w -> w.isNull(TUserQuestionnaireAnswer::getIsDelete).or().eq(TUserQuestionnaireAnswer::getIsDelete, false))
            );
        }

        return total;
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
                if (!res.isEmpty()) return res;
            } catch (Exception ignore) {
            }
        }
        for (String p : s.split(",")) {
            if (p != null && !p.trim().isEmpty()) res.add(p.trim());
        }
        return res;
    }
}

