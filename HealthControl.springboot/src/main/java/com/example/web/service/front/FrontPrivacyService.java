package com.example.web.service.front;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.web.dto.TDataDeleteRequestDto;
import com.example.web.dto.TDataExportRequestDto;
import com.example.web.dto.front.FrontPrivacyDtos.*;
import com.example.web.entity.*;
import com.example.web.mapper.*;
import com.example.web.tools.BaseContext;
import com.example.web.tools.Extension;
import com.example.web.tools.PrivacyMaskingUtils;
import com.example.web.tools.dto.PagedResult;
import com.example.web.tools.exception.CustomException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class FrontPrivacyService {

    @Autowired
    private TDataExportRequestMapper exportRequestMapper;

    @Autowired
    private TDataDeleteRequestMapper deleteRequestMapper;

    @Autowired
    private AppUserMapper appUserMapper;

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
     * 统一使用 Spring 管理的 ObjectMapper，已在全局配置中注册 JavaTimeModule，
     * 避免 LocalDateTime 等类型在本地新建 ObjectMapper 时无法序列化的问题。
     */
    @Autowired
    private ObjectMapper objectMapper;

    public Map<String, Object> exportRequest(FrontPrivacyExportRequestInput input) {
        Integer userId = getCurrentUserIdOrThrow();
        if (input == null) input = new FrontPrivacyExportRequestInput();

        if (input.getDataType() == null || input.getDataType().isEmpty()) {
            throw new CustomException("请选择至少一种导出数据类型");
        }

        String exportFormat = Extension.isNotNullOrEmpty(input.getExportFormat())
                ? input.getExportFormat().trim().toUpperCase()
                : "JSON";
        if (!"JSON".equals(exportFormat)
                && !"EXCEL".equals(exportFormat)
                && !"PDF".equals(exportFormat)) {
            throw new CustomException("当前仅支持 JSON / Excel / PDF 格式导出");
        }

        TDataExportRequest entity = new TDataExportRequest();
        entity.setUserId(userId);
        entity.setRequestType("Export");
        try {
            entity.setDataType(objectMapper.writeValueAsString(input.getDataType()));
        } catch (Exception ex) {
            // 兜底：逗号拼接
            entity.setDataType(String.join(",", input.getDataType()));
        }
        entity.setExportFormat(exportFormat);
        entity.setRequestDesc(input.getRequestDesc());
        // 变更：导出需管理员审核通过后才允许生成/下载
        entity.setStatus("PendingAudit");
        entity.setFileUrl(null);
        entity.setFileSize(null);
        entity.setExpireTime(null);
        entity.setProcessTime(null);
        entity.setErrorMessage(null);
        entity.setIsDelete(false);
        entity.setUpdateTime(LocalDateTime.now());
        exportRequestMapper.insert(entity);

        Map<String, Object> res = new HashMap<>();
        res.put("RequestId", entity.getId());
        return res;
    }

    public PagedResult<TDataExportRequestDto> exportRequestList(FrontPrivacyExportRequestListInput input) {
        Integer userId = getCurrentUserIdOrThrow();
        if (input == null) input = new FrontPrivacyExportRequestListInput();

        long page = input.getPage() == null || input.getPage() <= 0 ? 1 : input.getPage();
        long limit = input.getLimit() == null || input.getLimit() <= 0 ? 20 : input.getLimit();

        LambdaQueryWrapper<TDataExportRequest> qw = Wrappers.<TDataExportRequest>lambdaQuery()
                .eq(TDataExportRequest::getUserId, userId)
                .and(w -> w.isNull(TDataExportRequest::getIsDelete).or().eq(TDataExportRequest::getIsDelete, false));
        if (Extension.isNotNullOrEmpty(input.getStatus())) {
            qw.eq(TDataExportRequest::getStatus, input.getStatus());
        }
        qw.orderByDesc(TDataExportRequest::getCreationTime).orderByDesc(TDataExportRequest::getId);

        Page<TDataExportRequest> pageModel = new Page<>(page, limit);
        IPage<TDataExportRequest> pageRes = exportRequestMapper.selectPage(pageModel, qw);
        Long total = exportRequestMapper.selectCount(qw);

        List<TDataExportRequestDto> items = Extension.copyBeanList(pageRes.getRecords(), TDataExportRequestDto.class);
        return PagedResult.GetInstance(items, total);
    }

    public void downloadExport(Integer requestId, HttpServletResponse response) {
        Integer userId = getCurrentUserIdOrThrow();
        if (requestId == null || requestId <= 0) throw new CustomException("RequestId不能为空");

        TDataExportRequest req = exportRequestMapper.selectById(requestId);
        if (req == null || req.getId() == null) throw new CustomException("导出申请不存在");
        if (req.getUserId() == null || !req.getUserId().equals(userId)) throw new CustomException("无权限下载该导出文件");

        // 审核控制：仅“已通过/已完成”允许下载（下载时实时生成）
        if (!"Approved".equals(req.getStatus()) && !"Completed".equals(req.getStatus())) {
            if ("PendingAudit".equals(req.getStatus())) {
                throw new CustomException("该导出申请正在等待管理员审核通过");
            }
            if ("Rejected".equals(req.getStatus())) {
                throw new CustomException("该导出申请已被管理员拒绝" + (Extension.isNotNullOrEmpty(req.getErrorMessage()) ? ("：" + req.getErrorMessage()) : ""));
            }
            throw new CustomException("当前状态不允许下载：" + req.getStatus());
        }

        // 若为已通过，则标记为处理中（便于管理端观察）
        if ("Approved".equals(req.getStatus())) {
            req.setStatus("Processing");
            req.setUpdateTime(LocalDateTime.now());
            exportRequestMapper.updateById(req);
        }

        // 解析 DataType
        List<String> types = parseDataTypes(req.getDataType());
        if (types.isEmpty()) types = Collections.singletonList("All");

        Map<String, Object> exportData = new LinkedHashMap<>();
        exportData.put("RequestId", req.getId());
        exportData.put("UserId", userId);
        String exportFormat = Extension.isNotNullOrEmpty(req.getExportFormat())
                ? req.getExportFormat().trim().toUpperCase()
                : "JSON";
        if (!"JSON".equals(exportFormat)
                && !"EXCEL".equals(exportFormat)
                && !"PDF".equals(exportFormat)) {
            exportFormat = "JSON";
        }
        exportData.put("ExportFormat", exportFormat);
        exportData.put("CreatedTime", LocalDateTime.now().toString());

        // 用户信息（脱敏）
        AppUser user = appUserMapper.selectById(userId);
        if (user != null) {
            Map<String, Object> userInfo = new LinkedHashMap<>();
            userInfo.put("Id", user.getId());
            userInfo.put("UserName", user.getUserName());
            userInfo.put("Name", PrivacyMaskingUtils.maskName(user.getName()));
            userInfo.put("PhoneNumber", PrivacyMaskingUtils.maskPhone(user.getPhoneNumber()));
            userInfo.put("Email", PrivacyMaskingUtils.maskEmail(user.getEmail()));
            exportData.put("UserInfo", userInfo);
        }

        // 数据集合
        Map<String, Object> datasets = new LinkedHashMap<>();
        exportData.put("Datasets", datasets);

        if (types.contains("All") || types.contains("AudioRecord")) {
            datasets.put("AudioRecord", audioScreenRecordMapper.selectList(
                    Wrappers.<TAudioScreenRecord>lambdaQuery()
                            .eq(TAudioScreenRecord::getUserId, userId)
                            .and(w -> w.isNull(TAudioScreenRecord::getIsDelete).or().eq(TAudioScreenRecord::getIsDelete, false))
                            .orderByDesc(TAudioScreenRecord::getCreationTime)
            ));
        }
        if (types.contains("All") || types.contains("HealthRecord")) {
            datasets.put("HealthRecord", healthRecordMapper.selectList(
                    Wrappers.<TPersonalLaryngealHealthRecord>lambdaQuery()
                            .eq(TPersonalLaryngealHealthRecord::getUserId, userId)
                            .and(w -> w.isNull(TPersonalLaryngealHealthRecord::getIsDelete).or().eq(TPersonalLaryngealHealthRecord::getIsDelete, false))
                            .orderByDesc(TPersonalLaryngealHealthRecord::getCreationTime)
            ));
        }
        if (types.contains("All") || types.contains("SymptomLog")) {
            datasets.put("SymptomLog", symptomLogMapper.selectList(
                    Wrappers.<TSymptomLog>lambdaQuery()
                            .eq(TSymptomLog::getUserId, userId)
                            .and(w -> w.isNull(TSymptomLog::getIsDelete).or().eq(TSymptomLog::getIsDelete, false))
                            .orderByDesc(TSymptomLog::getCreationTime)
            ));
        }
        if (types.contains("All") || types.contains("DietRecord")) {
            datasets.put("DietRecord", dietRecordMapper.selectList(
                    Wrappers.<TUserDietRecord>lambdaQuery()
                            .eq(TUserDietRecord::getUserId, userId)
                            .and(w -> w.isNull(TUserDietRecord::getIsDelete).or().eq(TUserDietRecord::getIsDelete, false))
                            .orderByDesc(TUserDietRecord::getIntakeTime)
                            .orderByDesc(TUserDietRecord::getCreationTime)
            ));
        }
        if (types.contains("All") || types.contains("ModelLabel")) {
            datasets.put("ModelLabel", modelOptimizeLabelMapper.selectList(
                    Wrappers.<TModelOptimizeLabel>lambdaQuery()
                            .eq(TModelOptimizeLabel::getUserId, userId)
                            .and(w -> w.isNull(TModelOptimizeLabel::getIsDelete).or().eq(TModelOptimizeLabel::getIsDelete, false))
                            .orderByDesc(TModelOptimizeLabel::getCreationTime)
            ));
        }
        if (types.contains("All") || types.contains("HealthRemind")) {
            datasets.put("HealthRemind", personalHealthRemindMapper.selectList(
                    Wrappers.<TPersonalHealthRemind>lambdaQuery()
                            .eq(TPersonalHealthRemind::getUserId, userId)
                            .and(w -> w.isNull(TPersonalHealthRemind::getIsDelete).or().eq(TPersonalHealthRemind::getIsDelete, false))
                            .orderByDesc(TPersonalHealthRemind::getCreationTime)
            ));
        }
        if (types.contains("All") || types.contains("MessageNotice")) {
            datasets.put("MessageNotice", messageNoticeMapper.selectList(
                    Wrappers.<MessageNotice>lambdaQuery()
                            .eq(MessageNotice::getUserId, userId)
                            .and(w -> w.isNull(MessageNotice::getIsDelete).or().eq(MessageNotice::getIsDelete, false))
                            .orderByDesc(MessageNotice::getCreationTime)
            ));
        }
        if (types.contains("All") || types.contains("QuestionnaireAnswer")) {
            datasets.put("QuestionnaireAnswer", questionnaireAnswerMapper.selectList(
                    Wrappers.<TUserQuestionnaireAnswer>lambdaQuery()
                            .eq(TUserQuestionnaireAnswer::getUserId, userId)
                            .and(w -> w.isNull(TUserQuestionnaireAnswer::getIsDelete).or().eq(TUserQuestionnaireAnswer::getIsDelete, false))
                            .orderByDesc(TUserQuestionnaireAnswer::getAnswerTime)
                            .orderByDesc(TUserQuestionnaireAnswer::getCreationTime)
            ));
        }

        try {
            // 根据导出格式生成不同类型的文件
            if ("EXCEL".equals(exportFormat)) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                HSSFWorkbook workbook = buildExcel(exportData);
                workbook.write(baos);
                workbook.close();

                String fileName = "export_" + userId + "_" + requestId + ".xls";
                String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
                response.setContentType("application/vnd.ms-excel");
                response.setHeader("Content-Disposition", "attachment;filename=" + encoded);

                try (OutputStream os = response.getOutputStream()) {
                    os.write(baos.toByteArray());
                    os.flush();
                }
            } else if ("PDF".equals(exportFormat)) {
                // 先序列化为 JSON 字符串，再渲染为 PDF
                String jsonString = objectMapper
                        .writerWithDefaultPrettyPrinter()
                        .writeValueAsString(exportData);
                String html = "<html><head><meta charset='utf-8'/></head><body>"
                        + "<h1>数据导出</h1>"
                        + "<pre style='font-family:monospace;font-size:10px;white-space:pre-wrap;'>"
                        + jsonString.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                        + "</pre></body></html>";

                String fileName = "export_" + userId + "_" + requestId + ".pdf";
                String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition", "attachment;filename=" + encoded);

                try (OutputStream os = response.getOutputStream()) {
                    PdfRendererBuilder builder = new PdfRendererBuilder();
                    builder.useFastMode();
                    builder.withHtmlContent(html, null);
                    builder.toStream(os);
                    builder.run();
                }
            } else {
                // 默认 JSON
                byte[] bytes = objectMapper
                        .writerWithDefaultPrettyPrinter()
                        .writeValueAsBytes(exportData);

                String fileName = "export_" + userId + "_" + requestId + ".json";
                String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
                response.setContentType("application/json");
                response.setHeader("Content-Disposition", "attachment;filename=" + encoded);

                try (OutputStream os = response.getOutputStream()) {
                    os.write(bytes);
                    os.flush();
                }
            }

            // 成功后更新状态为已完成（导出为实时生成，不落 FileUrl）
            req.setStatus("Completed");
            req.setFileUrl(null);
            req.setFileSize(null);
            req.setExpireTime(LocalDateTime.now().plusDays(7));
            req.setProcessTime(LocalDateTime.now());
            req.setErrorMessage(null);
            req.setUpdateTime(LocalDateTime.now());
            exportRequestMapper.updateById(req);
        } catch (Exception e) {
            try {
                req.setStatus("Failed");
                req.setErrorMessage(e.getMessage());
                req.setProcessTime(LocalDateTime.now());
                req.setUpdateTime(LocalDateTime.now());
                exportRequestMapper.updateById(req);
            } catch (Exception ignore) {
            }
            throw new CustomException("导出文件生成失败：" + e.getMessage());
        }
    }

    public Map<String, Object> deleteRequest(FrontPrivacyDeleteRequestInput input) {
        Integer userId = getCurrentUserIdOrThrow();
        if (input == null) input = new FrontPrivacyDeleteRequestInput();
        if (input.getDataType() == null || input.getDataType().isEmpty()) {
            throw new CustomException("请选择至少一种删除数据类型");
        }

        TDataDeleteRequest entity = new TDataDeleteRequest();
        entity.setUserId(userId);
        entity.setRequestType("Delete");
        try {
            entity.setDataType(objectMapper.writeValueAsString(input.getDataType()));
        } catch (Exception ex) {
            entity.setDataType(String.join(",", input.getDataType()));
        }
        entity.setDeleteReason(input.getDeleteReason());
        entity.setDeleteReasonDesc(input.getDeleteReasonDesc());
        // 变更：删除需管理员审核通过后才允许执行（定时任务处理 Approved 状态）
        entity.setStatus("PendingAudit");
        entity.setDeleteCount(null);
        entity.setProcessTime(null);
        entity.setErrorMessage(null);
        entity.setIsDelete(false);
        entity.setUpdateTime(LocalDateTime.now());
        deleteRequestMapper.insert(entity);

        Map<String, Object> res = new HashMap<>();
        res.put("RequestId", entity.getId());
        return res;
    }

    public PagedResult<TDataDeleteRequestDto> deleteRequestList(FrontPrivacyDeleteRequestListInput input) {
        Integer userId = getCurrentUserIdOrThrow();
        if (input == null) input = new FrontPrivacyDeleteRequestListInput();

        long page = input.getPage() == null || input.getPage() <= 0 ? 1 : input.getPage();
        long limit = input.getLimit() == null || input.getLimit() <= 0 ? 20 : input.getLimit();

        LambdaQueryWrapper<TDataDeleteRequest> qw = Wrappers.<TDataDeleteRequest>lambdaQuery()
                .eq(TDataDeleteRequest::getUserId, userId)
                .and(w -> w.isNull(TDataDeleteRequest::getIsDelete).or().eq(TDataDeleteRequest::getIsDelete, false));
        if (Extension.isNotNullOrEmpty(input.getStatus())) {
            qw.eq(TDataDeleteRequest::getStatus, input.getStatus());
        }
        qw.orderByDesc(TDataDeleteRequest::getCreationTime).orderByDesc(TDataDeleteRequest::getId);

        Page<TDataDeleteRequest> pageModel = new Page<>(page, limit);
        IPage<TDataDeleteRequest> pageRes = deleteRequestMapper.selectPage(pageModel, qw);
        Long total = deleteRequestMapper.selectCount(qw);

        List<TDataDeleteRequestDto> items = Extension.copyBeanList(pageRes.getRecords(), TDataDeleteRequestDto.class);
        return PagedResult.GetInstance(items, total);
    }

    private Integer getCurrentUserIdOrThrow() {
        if (BaseContext.getCurrentUserDto() == null || BaseContext.getCurrentUserDto().getUserId() == null) return 0;
        Integer userId = BaseContext.getCurrentUserDto().getUserId();
        if (userId == null || userId <= 0) throw new CustomException("请先登录");
        return userId;
    }

    private List<String> parseDataTypes(String raw) {
        if (!Extension.isNotNullOrEmpty(raw)) return new ArrayList<>();
        String s = raw.trim();
        if (s.startsWith("[")) {
            try {
                String[] arr = objectMapper.readValue(s, String[].class);
                List<String> res = new ArrayList<>();
                for (String a : arr) {
                    if (Extension.isNotNullOrEmpty(a)) res.add(a.trim());
                }
                return res;
            } catch (Exception ignore) {
            }
        }
        // 逗号兜底
        String[] parts = s.split(",");
        List<String> res = new ArrayList<>();
        for (String p : parts) {
            if (Extension.isNotNullOrEmpty(p)) res.add(p.trim());
        }
        return res;
    }

    /**
     * 通用地把导出数据写成一个多 Sheet 的 Excel：
     * - Summary：请求信息与用户信息（key-value）
     * - 其他 Sheet：Datasets 下的各个数据集，每个字段一列。
     */
    private HSSFWorkbook buildExcel(Map<String, Object> exportData) {
        HSSFWorkbook workbook = new HSSFWorkbook();

        // Summary sheet
        HSSFSheet summary = workbook.createSheet("Summary");
        int rowIndex = 0;
        for (Map.Entry<String, Object> entry : exportData.entrySet()) {
            if ("Datasets".equals(entry.getKey())) continue;
            HSSFRow row = summary.createRow(rowIndex++);
            HSSFCell k = row.createCell(0);
            HSSFCell v = row.createCell(1);
            k.setCellValue(entry.getKey());
            v.setCellValue(entry.getValue() != null ? String.valueOf(entry.getValue()) : "");
        }

        Object datasetsObj = exportData.get("Datasets");
        if (datasetsObj instanceof Map<?, ?> datasets) {
            for (Map.Entry<?, ?> dsEntry : datasets.entrySet()) {
                String sheetName = String.valueOf(dsEntry.getKey());
                Object value = dsEntry.getValue();
                if (!(value instanceof List<?> list) || list.isEmpty()) {
                    continue;
                }

                HSSFSheet sheet = workbook.createSheet(sheetName);
                // 将第一个元素转为 Map 以获取字段列表
                List<LinkedHashMap<String, Object>> rows = new ArrayList<>();
                for (Object item : list) {
                    LinkedHashMap<String, Object> map = objectMapper.convertValue(
                            item, new com.fasterxml.jackson.core.type.TypeReference<LinkedHashMap<String, Object>>() {});
                    rows.add(map);
                }
                if (rows.isEmpty()) continue;

                // 表头
                HSSFRow headerRow = sheet.createRow(0);
                List<String> keys = new ArrayList<>(rows.get(0).keySet());
                for (int i = 0; i < keys.size(); i++) {
                    headerRow.createCell(i).setCellValue(keys.get(i));
                }

                // 数据行
                for (int i = 0; i < rows.size(); i++) {
                    HSSFRow dataRow = sheet.createRow(i + 1);
                    LinkedHashMap<String, Object> data = rows.get(i);
                    for (int j = 0; j < keys.size(); j++) {
                        Object v = data.get(keys.get(j));
                        dataRow.createCell(j).setCellValue(v != null ? String.valueOf(v) : "");
                    }
                }
            }
        }

        return workbook;
    }

    // 脱敏逻辑已抽取到 PrivacyMaskingUtils
}

