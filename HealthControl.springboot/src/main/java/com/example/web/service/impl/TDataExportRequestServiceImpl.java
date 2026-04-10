package com.example.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.web.dto.TDataExportRequestDto;
import com.example.web.dto.query.TDataExportRequestPagedInput;
import com.example.web.entity.AppUser;
import com.example.web.entity.TDataExportRequest;
import com.example.web.mapper.AppUserMapper;
import com.example.web.mapper.TDataExportRequestMapper;
import com.example.web.service.TDataExportRequestService;
import com.example.web.tools.Extension;
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
 * 数据导出申请功能实现类（管理端）
 */
@Service
public class TDataExportRequestServiceImpl
        extends ServiceImpl<TDataExportRequestMapper, TDataExportRequest>
        implements TDataExportRequestService {

    @Autowired
    private TDataExportRequestMapper exportRequestMapper;

    @Autowired
    private AppUserMapper appUserMapper;

    private LambdaQueryWrapper<TDataExportRequest> buildQuery(TDataExportRequestPagedInput input) {
        LambdaQueryWrapper<TDataExportRequest> qw = Wrappers.<TDataExportRequest>lambdaQuery()
                .eq(input.getId() != null && input.getId() != 0, TDataExportRequest::getId, input.getId());

        if (input.getUserId() != null) {
            qw.eq(TDataExportRequest::getUserId, input.getUserId());
        }
        if (Extension.isNotNullOrEmpty(input.getRequestType())) {
            qw.eq(TDataExportRequest::getRequestType, input.getRequestType());
        }
        if (Extension.isNotNullOrEmpty(input.getStatus())) {
            qw.eq(TDataExportRequest::getStatus, input.getStatus());
        }
        if (input.getIsDelete() != null) {
            qw.eq(TDataExportRequest::getIsDelete, input.getIsDelete());
        }
        if (input.getCreationTimeRange() != null && !input.getCreationTimeRange().isEmpty()) {
            qw.ge(TDataExportRequest::getCreationTime, input.getCreationTimeRange().get(0));
            qw.le(TDataExportRequest::getCreationTime, input.getCreationTimeRange().get(1));
        }

        // 默认按创建时间倒序
        qw.orderByDesc(TDataExportRequest::getCreationTime).orderByDesc(TDataExportRequest::getId);
        return qw;
    }

    @SneakyThrows
    @Override
    public PagedResult<TDataExportRequestDto> List(TDataExportRequestPagedInput input) {
        LambdaQueryWrapper<TDataExportRequest> qw = buildQuery(input);
        Page<TDataExportRequest> pageModel = new Page<>(input.getPage(), input.getLimit());
        IPage<TDataExportRequest> pageRes = exportRequestMapper.selectPage(pageModel, qw);
        Long total = exportRequestMapper.selectCount(qw);

        List<TDataExportRequestDto> items = Extension.copyBeanList(pageRes.getRecords(), TDataExportRequestDto.class);
        // 处理关联用户
        for (TDataExportRequestDto dto : items) {
            if (dto.getUserId() != null) {
                AppUser user = appUserMapper.selectById(dto.getUserId());
                if (user != null) {
                    dto.setUserDto(user.MapToDto());
                }
            }
        }
        return PagedResult.GetInstance(items, total);
    }

    @Override
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        TDataExportRequestPagedInput input = mapper.readValue(query, TDataExportRequestPagedInput.class);
        List<TDataExportRequestDto> items = List(input).getItems();

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("数据导出申请表");
        sheet.setDefaultColumnWidth(30);

        HSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.YELLOW.index);
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        HSSFRow headrow = sheet.createRow(0);
        String[] header = {"用户名称", "数据类型", "导出格式", "状态", "文件大小", "创建时间", "完成时间"};
        for (int i = 0; i < header.length; i++) {
            HSSFCell cell = headrow.createCell(i);
            HSSFRichTextString text = new HSSFRichTextString(header[i]);
            cell.setCellValue(text);
            cell.setCellStyle(headerStyle);
        }

        for (int i = 0; i < items.size(); i++) {
            TDataExportRequestDto dto = items.get(i);
            HSSFRow row = sheet.createRow(i + 1);

            if (dto.getUserDto() != null && Extension.isNotNullOrEmpty(dto.getUserDto().getName())) {
                row.createCell(0).setCellValue(new HSSFRichTextString(dto.getUserDto().getName()));
            }
            if (Extension.isNotNullOrEmpty(dto.getDataType())) {
                row.createCell(1).setCellValue(new HSSFRichTextString(dto.getDataType()));
            }
            if (Extension.isNotNullOrEmpty(dto.getExportFormat())) {
                row.createCell(2).setCellValue(new HSSFRichTextString(dto.getExportFormat()));
            }
            if (Extension.isNotNullOrEmpty(dto.getStatus())) {
                row.createCell(3).setCellValue(new HSSFRichTextString(dto.getStatus()));
            }
            if (dto.getFileSize() != null) {
                row.createCell(4).setCellValue(new HSSFRichTextString(dto.getFileSize().toString()));
            }
            if (dto.getCreationTime() != null) {
                row.createCell(5).setCellValue(new HSSFRichTextString(dto.getCreationTime().toString()));
            }
            if (dto.getProcessTime() != null) {
                row.createCell(6).setCellValue(new HSSFRichTextString(dto.getProcessTime().toString()));
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

    @Override
    public void BatchDelete(com.example.web.tools.dto.IdsInput input) {
        if (input == null || input.getIds() == null || input.getIds().isEmpty()) {
            return;
        }
        for (Integer id : input.getIds()) {
            if (id == null || id <= 0) continue;
            TDataExportRequest entity = exportRequestMapper.selectById(id);
            if (entity == null) continue;
            // 仅软删除申请记录，本身不会影响已导出的文件
            entity.setIsDelete(true);
            exportRequestMapper.updateById(entity);
        }
    }
}

