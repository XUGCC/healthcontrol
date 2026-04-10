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
 * 用户就诊准备报告功能的Service接口的定义清单
 */
public interface TUserMedicalPrepareReportService extends IService<TUserMedicalPrepareReport> {

    /**
     * 用户就诊准备报告的分页查询方法接口定义
     */
    public PagedResult<TUserMedicalPrepareReportDto> List(TUserMedicalPrepareReportPagedInput input) ;
    /**
     * 用户就诊准备报告的新增或者修改方法接口定义
     */
    public TUserMedicalPrepareReportDto CreateOrEdit(TUserMedicalPrepareReportDto input);

     /**
     * 获取用户就诊准备报告信息
     */
    public TUserMedicalPrepareReportDto Get(TUserMedicalPrepareReportPagedInput input);
 	 /**
     * 用户就诊准备报告删除
     */
    public void Delete(IdInput input);

    /**
     * 用户就诊准备报告批量删除
     */
    public void BatchDelete(IdsInput input);
  
	  /**
     * 用户就诊准备报告导出
     */
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException;

}
