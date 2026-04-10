package com.example.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.web.dto.TDataDeleteRequestDto;
import com.example.web.dto.query.TDataDeleteRequestPagedInput;
import com.example.web.entity.AppUser;
import com.example.web.entity.TDataDeleteRequest;
import com.example.web.mapper.AppUserMapper;
import com.example.web.mapper.TDataDeleteRequestMapper;
import com.example.web.service.TDataDeleteRequestService;
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
 * 数据删除申请功能实现类（管理端）
 */
@Service
public class TDataDeleteRequestServiceImpl
        extends ServiceImpl<TDataDeleteRequestMapper, TDataDeleteRequest>
        implements TDataDeleteRequestService {

    @Autowired
    private TDataDeleteRequestMapper deleteRequestMapper;

    @Autowired
    private AppUserMapper appUserMapper;

    private LambdaQueryWrapper<TDataDeleteRequest> buildQuery(TDataDeleteRequestPagedInput input) {
        LambdaQueryWrapper<TDataDeleteRequest> qw = Wrappers.<TDataDeleteRequest>lambdaQuery()
                .eq(input.getId() != null && input.getId() != 0, TDataDeleteRequest::getId, input.getId());

        if (input.getUserId() != null) {
            qw.eq(TDataDeleteRequest::getUserId, input.getUserId());
        }
        if (Extension.isNotNullOrEmpty(input.getRequestType())) {
            qw.eq(TDataDeleteRequest::getRequestType, input.getRequestType());
        }
        if (Extension.isNotNullOrEmpty(input.getStatus())) {
            qw.eq(TDataDeleteRequest::getStatus, input.getStatus());
        }
        if (input.getIsDelete() != null) {
            qw.eq(TDataDeleteRequest::getIsDelete, input.getIsDelete());
        }
        if (input.getCreationTimeRange() != null && !input.getCreationTimeRange().isEmpty()) {
            qw.ge(TDataDeleteRequest::getCreationTime, input.getCreationTimeRange().get(0));
            qw.le(TDataDeleteRequest::getCreationTime, input.getCreationTimeRange().get(1));
        }

        qw.orderByDesc(TDataDeleteRequest::getCreationTime).orderByDesc(TDataDeleteRequest::getId);
        return qw;
    }

    @SneakyThrows
    @Override
    public PagedResult<TDataDeleteRequestDto> List(TDataDeleteRequestPagedInput input) {
        LambdaQueryWrapper<TDataDeleteRequest> qw = buildQuery(input);
        Page<TDataDeleteRequest> pageModel = new Page<>(input.getPage(), input.getLimit());
        IPage<TDataDeleteRequest> pageRes = deleteRequestMapper.selectPage(pageModel, qw);
        Long total = deleteRequestMapper.selectCount(qw);

        List<TDataDeleteRequestDto> items = Extension.copyBeanList(pageRes.getRecords(), TDataDeleteRequestDto.class);
        for (TDataDeleteRequestDto dto : items) {
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
        TDataDeleteRequestPagedInput input = mapper.readValue(query, TDataDeleteRequestPagedInput.class);
        List<TDataDeleteRequestDto> items = List(input).getItems();

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("数据删除申请表");
        sheet.setDefaultColumnWidth(30);

        HSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.YELLOW.index);
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        HSSFRow headrow = sheet.createRow(0);
        String[] header = {"用户名称", "数据类型", "删除原因", "状态", "删除数量", "创建时间", "完成时间"};
        for (int i = 0; i < header.length; i++) {
            HSSFCell cell = headrow.createCell(i);
            HSSFRichTextString text = new HSSFRichTextString(header[i]);
            cell.setCellValue(text);
            cell.setCellStyle(headerStyle);
        }

        for (int i = 0; i < items.size(); i++) {
            TDataDeleteRequestDto dto = items.get(i);
            HSSFRow row = sheet.createRow(i + 1);

            if (dto.getUserDto() != null && Extension.isNotNullOrEmpty(dto.getUserDto().getName())) {
                row.createCell(0).setCellValue(new HSSFRichTextString(dto.getUserDto().getName()));
            }
            if (Extension.isNotNullOrEmpty(dto.getDataType())) {
                row.createCell(1).setCellValue(new HSSFRichTextString(dto.getDataType()));
            }
            if (Extension.isNotNullOrEmpty(dto.getDeleteReason())) {
                row.createCell(2).setCellValue(new HSSFRichTextString(dto.getDeleteReason()));
            }
            if (Extension.isNotNullOrEmpty(dto.getStatus())) {
                row.createCell(3).setCellValue(new HSSFRichTextString(dto.getStatus()));
            }
            if (dto.getDeleteCount() != null) {
                row.createCell(4).setCellValue(new HSSFRichTextString(dto.getDeleteCount().toString()));
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
            TDataDeleteRequest entity = deleteRequestMapper.selectById(id);
            if (entity == null) continue;
            // 仅软删除申请记录，不再次触发数据删除逻辑
            entity.setIsDelete(true);
            deleteRequestMapper.updateById(entity);
        }
    }
}

