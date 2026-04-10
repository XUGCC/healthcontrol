package com.example.web.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.web.dto.*;
import com.example.web.dto.query.*;
import com.example.web.entity.*;
import com.example.web.tools.dto.*;
import com.example.web.enums.*;
import java.lang.reflect.InvocationTargetException;
import org.springframework.web.bind.annotation.RequestParam;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import jakarta.servlet.http.HttpServletResponse;
/**
 * 自查报告导出记录功能的Service接口的定义清单
 */
public interface TScreenReportExportRecordService extends IService<TScreenReportExportRecord> {

    /**
     * 自查报告导出记录的分页查询方法接口定义
     */
    public PagedResult<TScreenReportExportRecordDto> List(TScreenReportExportRecordPagedInput input) ;
    /**
     * 自查报告导出记录的新增或者修改方法接口定义
     */
    public TScreenReportExportRecordDto CreateOrEdit(TScreenReportExportRecordDto input);

     /**
     * 获取自查报告导出记录信息
     */
    public TScreenReportExportRecordDto Get(TScreenReportExportRecordPagedInput input);
 	 /**
     * 自查报告导出记录删除
     */
    public void Delete(IdInput input);

    /**
     * 自查报告导出记录批量删除
     */
    public void BatchDelete(IdsInput input);
  
	  /**
     * 自查报告导出记录导出
     */
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException;

}
