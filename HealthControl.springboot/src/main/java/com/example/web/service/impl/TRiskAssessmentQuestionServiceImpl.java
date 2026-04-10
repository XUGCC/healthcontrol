package com.example.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.web.dto.TRiskAssessmentQuestionDto;
import com.example.web.dto.query.TRiskAssessmentQuestionPagedInput;
import com.example.web.entity.TRiskAssessmentQuestion;
import com.example.web.entity.TRiskAssessmentQuestionnaire;
import com.example.web.mapper.TRiskAssessmentQuestionMapper;
import com.example.web.mapper.TRiskAssessmentQuestionnaireMapper;
import com.example.web.service.TRiskAssessmentQuestionService;
import com.example.web.tools.Extension;
import com.example.web.tools.dto.IdInput;
import com.example.web.tools.dto.IdsInput;
import com.example.web.tools.dto.PagedResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;

/**
 * 风险评估题目实现类
 */
@Service
public class TRiskAssessmentQuestionServiceImpl extends ServiceImpl<TRiskAssessmentQuestionMapper, TRiskAssessmentQuestion> implements TRiskAssessmentQuestionService {

    @Autowired
    private TRiskAssessmentQuestionMapper questionMapper;

    @Autowired
    private TRiskAssessmentQuestionnaireMapper questionnaireMapper;

    private LambdaQueryWrapper<TRiskAssessmentQuestion> BuilderQuery(TRiskAssessmentQuestionPagedInput input) {
        LambdaQueryWrapper<TRiskAssessmentQuestion> queryWrapper = Wrappers.<TRiskAssessmentQuestion>lambdaQuery()
                .eq(input.getId() != null && input.getId() != 0, TRiskAssessmentQuestion::getId, input.getId());

        if (input.getQuestionnaireId() != null) {
            queryWrapper = queryWrapper.eq(TRiskAssessmentQuestion::getQuestionnaireId, input.getQuestionnaireId());
        }
        if (Extension.isNotNullOrEmpty(input.getQuestionTitle())) {
            queryWrapper = queryWrapper.like(TRiskAssessmentQuestion::getQuestionTitle, input.getQuestionTitle());
        }
        if (Extension.isNotNullOrEmpty(input.getDimensionCode())) {
            queryWrapper = queryWrapper.eq(TRiskAssessmentQuestion::getDimensionCode, input.getDimensionCode());
        }
        // 当前题目表未提供更新时间字段，暂不支持时间范围筛选（保留接口字段以便后续扩展）

        if (Extension.isNotNullOrEmpty(input.getKeyWord())) {
            String kw = input.getKeyWord();
            queryWrapper = queryWrapper.and(i -> i
                    .like(TRiskAssessmentQuestion::getQuestionTitle, kw)
                    .or()
                    .like(TRiskAssessmentQuestion::getQuestionDesc, kw)
            );
        }
        return queryWrapper;
    }

    /**
     * 重新统计并回写对应问卷的 QuestionCount
     */
    private void ReCalcQuestionCount(Integer questionnaireId) {
        if (questionnaireId == null) return;
        Long cnt = questionMapper.selectCount(
                Wrappers.<TRiskAssessmentQuestion>lambdaQuery()
                        .eq(TRiskAssessmentQuestion::getQuestionnaireId, questionnaireId)
                        .and(w -> w.isNull(TRiskAssessmentQuestion::getIsDelete).or().ne(TRiskAssessmentQuestion::getIsDelete, 1))
        );
        TRiskAssessmentQuestionnaire q = questionnaireMapper.selectById(questionnaireId);
        if (q != null) {
            q.setQuestionCount(cnt != null ? cnt.intValue() : 0);
            questionnaireMapper.updateById(q);
        }
    }

    @SneakyThrows
    @Override
    public PagedResult<TRiskAssessmentQuestionDto> List(TRiskAssessmentQuestionPagedInput input) {
        LambdaQueryWrapper<TRiskAssessmentQuestion> queryWrapper = BuilderQuery(input);
        // 默认按 SortNum 升序，其次按 Id
        queryWrapper = queryWrapper.orderByAsc(TRiskAssessmentQuestion::getSortNum)
                .orderByAsc(TRiskAssessmentQuestion::getId);

        Page<TRiskAssessmentQuestion> page = new Page<>(input.getPage(), input.getLimit());
        IPage<TRiskAssessmentQuestion> pageRecords = questionMapper.selectPage(page, queryWrapper);
        Long totalCount = questionMapper.selectCount(queryWrapper);

        List<TRiskAssessmentQuestionDto> items = Extension.copyBeanList(pageRecords.getRecords(), TRiskAssessmentQuestionDto.class);
        return PagedResult.GetInstance(items, totalCount);
    }

    @SneakyThrows
    @Override
    public TRiskAssessmentQuestionDto Get(TRiskAssessmentQuestionPagedInput input) {
        if (input.getId() == null) {
            return new TRiskAssessmentQuestionDto();
        }
        PagedResult<TRiskAssessmentQuestionDto> pagedResult = List(input);
        return pagedResult.getItems().stream().findFirst().orElse(new TRiskAssessmentQuestionDto());
    }

    @SneakyThrows
    @Override
    public TRiskAssessmentQuestionDto CreateOrEdit(TRiskAssessmentQuestionDto input) {
        TRiskAssessmentQuestion entity = input.MapToEntity();
        saveOrUpdate(entity);
        ReCalcQuestionCount(entity.getQuestionnaireId());
        // 直接返回入参（包含前端可见字段），避免额外映射
        if (entity.getId() != null) {
            input.setId(entity.getId());
        }
        return input;
    }

    @Override
    public void Delete(IdInput input) {
        TRiskAssessmentQuestion entity = questionMapper.selectById(input.getId());
        if (entity != null) {
            Integer qid = entity.getQuestionnaireId();
            questionMapper.deleteById(entity);
            ReCalcQuestionCount(qid);
        }
    }

    @Override
    public void BatchDelete(IdsInput input) {
        for (Integer id : input.getIds()) {
            IdInput i = new IdInput();
            i.setId(id);
            Delete(i);
        }
    }

    @Override
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        TRiskAssessmentQuestionPagedInput input = mapper.readValue(query, TRiskAssessmentQuestionPagedInput.class);
        List<TRiskAssessmentQuestionDto> items = List(input).getItems();

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("风险评估题目表");
        sheet.setDefaultColumnWidth(30);
        HSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.YELLOW.index);
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        HSSFRow headrow = sheet.createRow(0);
        String[] header = {"问卷Id", "维度编码", "题干", "题目说明", "题型", "是否必答", "排序值", "软删除标记"};
        for (int i = 0; i < header.length; i++) {
            HSSFCell cell = headrow.createCell(i);
            HSSFRichTextString text = new HSSFRichTextString(header[i]);
            cell.setCellValue(text);
            cell.setCellStyle(headerStyle);
        }
        for (int i = 0; i < items.size(); i++) {
            TRiskAssessmentQuestionDto dto = items.get(i);
            HSSFRow row = sheet.createRow(i + 1);
            if (dto.getQuestionnaireId() != null) {
                row.createCell(0).setCellValue(new HSSFRichTextString(dto.getQuestionnaireId() + ""));
            }
            if (Extension.isNotNullOrEmpty(dto.getDimensionCode())) {
                row.createCell(1).setCellValue(new HSSFRichTextString(dto.getDimensionCode()));
            }
            if (Extension.isNotNullOrEmpty(dto.getQuestionTitle())) {
                row.createCell(2).setCellValue(new HSSFRichTextString(dto.getQuestionTitle()));
            }
            if (Extension.isNotNullOrEmpty(dto.getQuestionDesc())) {
                row.createCell(3).setCellValue(new HSSFRichTextString(dto.getQuestionDesc()));
            }
            if (dto.getQuestionType() != null) {
                row.createCell(4).setCellValue(new HSSFRichTextString(dto.getQuestionType() + ""));
            }
            if (dto.getIsRequired() != null) {
                row.createCell(5).setCellValue(new HSSFRichTextString(dto.getIsRequired() + ""));
            }
            if (dto.getSortNum() != null) {
                row.createCell(6).setCellValue(new HSSFRichTextString(dto.getSortNum() + ""));
            }
            if (dto.getIsDelete() != null) {
                row.createCell(7).setCellValue(new HSSFRichTextString(dto.getIsDelete() + ""));
            }
        }

        response.setContentType("application/octet-stream");
        response.setHeader("Content-disposition", "attachment;filename=" + System.currentTimeMillis() + ".xls");
        response.flushBuffer();
        try {
            workbook.write(response.getOutputStream());
        } finally {
            workbook.close();
        }
    }
}

