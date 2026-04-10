package com.example.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.web.dto.TDataExportRequestDto;
import com.example.web.dto.query.TDataExportRequestPagedInput;
import com.example.web.entity.TDataExportRequest;
import com.example.web.tools.dto.PagedResult;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

/**
 * 数据导出申请 Service 接口
 */
public interface TDataExportRequestService extends IService<TDataExportRequest> {

    /**
     * 分页查询数据导出申请
     */
    PagedResult<TDataExportRequestDto> List(TDataExportRequestPagedInput input);

    /**
     * 导出数据导出申请列表为 Excel
     */
    void Export(@RequestParam String query, HttpServletResponse response) throws IOException;

    /**
     * 批量删除数据导出申请
     */
    void BatchDelete(com.example.web.tools.dto.IdsInput input);
}

