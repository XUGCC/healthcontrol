package com.example.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.web.dto.TDataDeleteRequestDto;
import com.example.web.dto.query.TDataDeleteRequestPagedInput;
import com.example.web.entity.TDataDeleteRequest;
import com.example.web.tools.dto.PagedResult;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

/**
 * 数据删除申请 Service 接口
 */
public interface TDataDeleteRequestService extends IService<TDataDeleteRequest> {

    /**
     * 分页查询数据删除申请
     */
    PagedResult<TDataDeleteRequestDto> List(TDataDeleteRequestPagedInput input);

    /**
     * 导出数据删除申请列表为 Excel
     */
    void Export(@RequestParam String query, HttpServletResponse response) throws IOException;

    /**
     * 批量删除数据删除申请
     */
    void BatchDelete(com.example.web.tools.dto.IdsInput input);
}

