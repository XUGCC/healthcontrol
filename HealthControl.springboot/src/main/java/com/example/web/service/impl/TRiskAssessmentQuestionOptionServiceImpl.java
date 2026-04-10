package com.example.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.web.dto.TRiskAssessmentQuestionOptionDto;
import com.example.web.dto.query.TRiskAssessmentQuestionOptionPagedInput;
import com.example.web.entity.TRiskAssessmentQuestionOption;
import com.example.web.mapper.TRiskAssessmentQuestionOptionMapper;
import com.example.web.service.TRiskAssessmentQuestionOptionService;
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
 * 风险评估题目选项实现类
 */
@Service
public class TRiskAssessmentQuestionOptionServiceImpl extends ServiceImpl<TRiskAssessmentQuestionOptionMapper, TRiskAssessmentQuestionOption> implements TRiskAssessmentQuestionOptionService {

    @Autowired
    private TRiskAssessmentQuestionOptionMapper optionMapper;

    private LambdaQueryWrapper<TRiskAssessmentQuestionOption> BuilderQuery(TRiskAssessmentQuestionOptionPagedInput input) {
        LambdaQueryWrapper<TRiskAssessmentQuestionOption> queryWrapper = Wrappers.<TRiskAssessmentQuestionOption>lambdaQuery()
                .eq(input.getId() != null && input.getId() != 0, TRiskAssessmentQuestionOption::getId, input.getId());

        if (input.getQuestionId() != null) {
            queryWrapper = queryWrapper.eq(TRiskAssessmentQuestionOption::getQuestionId, input.getQuestionId());
        }
        if (Extension.isNotNullOrEmpty(input.getOptionText())) {
            queryWrapper = queryWrapper.like(TRiskAssessmentQuestionOption::getOptionText, input.getOptionText());
        }
        // 当前选项表未提供更新时间字段，暂不支持时间范围筛选（保留接口字段以便后续扩展）

        if (Extension.isNotNullOrEmpty(input.getKeyWord())) {
            String kw = input.getKeyWord();
            queryWrapper = queryWrapper.and(i -> i
                    .like(TRiskAssessmentQuestionOption::getOptionText, kw)
            );
        }
        return queryWrapper;
    }

    @SneakyThrows
    @Override
    public PagedResult<TRiskAssessmentQuestionOptionDto> List(TRiskAssessmentQuestionOptionPagedInput input) {
        LambdaQueryWrapper<TRiskAssessmentQuestionOption> queryWrapper = BuilderQuery(input);
        queryWrapper = queryWrapper.orderByAsc(TRiskAssessmentQuestionOption::getSortNum)
                .orderByAsc(TRiskAssessmentQuestionOption::getId);

        Page<TRiskAssessmentQuestionOption> page = new Page<>(input.getPage(), input.getLimit());
        IPage<TRiskAssessmentQuestionOption> pageRecords = optionMapper.selectPage(page, queryWrapper);
        Long totalCount = optionMapper.selectCount(queryWrapper);

        List<TRiskAssessmentQuestionOptionDto> items = Extension.copyBeanList(pageRecords.getRecords(), TRiskAssessmentQuestionOptionDto.class);
        return PagedResult.GetInstance(items, totalCount);
    }

    @SneakyThrows
    @Override
    public TRiskAssessmentQuestionOptionDto Get(TRiskAssessmentQuestionOptionPagedInput input) {
        if (input.getId() == null) {
            return new TRiskAssessmentQuestionOptionDto();
        }
        PagedResult<TRiskAssessmentQuestionOptionDto> pagedResult = List(input);
        return pagedResult.getItems().stream().findFirst().orElse(new TRiskAssessmentQuestionOptionDto());
    }

    @SneakyThrows
    @Override
    public TRiskAssessmentQuestionOptionDto CreateOrEdit(TRiskAssessmentQuestionOptionDto input) {
        TRiskAssessmentQuestionOption entity = input.MapToEntity();
        saveOrUpdate(entity);
        if (entity.getId() != null) {
            input.setId(entity.getId());
        }
        return input;
    }

    @Override
    public void Delete(IdInput input) {
        TRiskAssessmentQuestionOption entity = optionMapper.selectById(input.getId());
        if (entity != null) {
            optionMapper.deleteById(entity);
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
        TRiskAssessmentQuestionOptionPagedInput input = mapper.readValue(query, TRiskAssessmentQuestionOptionPagedInput.class);
        List<TRiskAssessmentQuestionOptionDto> items = List(input).getItems();

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("风险评估题目选项表");
        sheet.setDefaultColumnWidth(30);
        HSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.YELLOW.index);
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        HSSFRow headrow = sheet.createRow(0);
        String[] header = {"题目Id", "选项文本", "选项值", "得分", "排序值", "软删除标记"};
        for (int i = 0; i < header.length; i++) {
            HSSFCell cell = headrow.createCell(i);
            HSSFRichTextString text = new HSSFRichTextString(header[i]);
            cell.setCellValue(text);
            cell.setCellStyle(headerStyle);
        }
        for (int i = 0; i < items.size(); i++) {
            TRiskAssessmentQuestionOptionDto dto = items.get(i);
            HSSFRow row = sheet.createRow(i + 1);
            if (dto.getQuestionId() != null) {
                row.createCell(0).setCellValue(new HSSFRichTextString(dto.getQuestionId() + ""));
            }
            if (Extension.isNotNullOrEmpty(dto.getOptionText())) {
                row.createCell(1).setCellValue(new HSSFRichTextString(dto.getOptionText()));
            }
            if (Extension.isNotNullOrEmpty(dto.getOptionValue())) {
                row.createCell(2).setCellValue(new HSSFRichTextString(dto.getOptionValue()));
            }
            if (dto.getScoreValue() != null) {
                row.createCell(3).setCellValue(new HSSFRichTextString(dto.getScoreValue() + ""));
            }
            if (dto.getSortNum() != null) {
                row.createCell(4).setCellValue(new HSSFRichTextString(dto.getSortNum() + ""));
            }
            if (dto.getIsDelete() != null) {
                row.createCell(5).setCellValue(new HSSFRichTextString(dto.getIsDelete() + ""));
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

