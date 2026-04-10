package com.example.web.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.web.dto.*;
import com.example.web.dto.admin.ModelInterfaceStatsDtos.ModelInterfaceStatsSummaryInput;
import com.example.web.dto.admin.ModelInterfaceStatsDtos.ModelInterfaceStatsSummaryOutput;
import com.example.web.dto.query.*;
import com.example.web.entity.*;
import com.example.web.tools.dto.*;
import org.springframework.web.bind.annotation.RequestParam;
import java.io.IOException;
import jakarta.servlet.http.HttpServletResponse;
/**
 * 模型接口调用日志功能的Service接口的定义清单
 */
public interface TModelInterfaceCallLogService extends IService<TModelInterfaceCallLog> {

    /**
     * 模型接口调用日志的分页查询方法接口定义
     */
    public PagedResult<TModelInterfaceCallLogDto> List(TModelInterfaceCallLogPagedInput input) ;
    /**
     * 模型接口调用日志的新增或者修改方法接口定义
     */
    public TModelInterfaceCallLogDto CreateOrEdit(TModelInterfaceCallLogDto input);

     /**
     * 获取模型接口调用日志信息
     */
    public TModelInterfaceCallLogDto Get(TModelInterfaceCallLogPagedInput input);
 	 /**
     * 模型接口调用日志删除
     */
    public void Delete(IdInput input);

    /**
     * 模型接口调用日志批量删除
     */
    public void BatchDelete(IdsInput input);
  
	  /**
     * 模型接口调用日志导出
     */
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException;

    /**
     * 模型接口调用日志统计概要（按时间范围）
     */
    ModelInterfaceStatsSummaryOutput StatsSummary(ModelInterfaceStatsSummaryInput input);

}
