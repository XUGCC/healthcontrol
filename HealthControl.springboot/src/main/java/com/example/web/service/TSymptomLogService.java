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
 * 症状日志功能的Service接口的定义清单
 */
public interface TSymptomLogService extends IService<TSymptomLog> {

    /**
     * 症状日志的分页查询方法接口定义
     */
    public PagedResult<TSymptomLogDto> List(TSymptomLogPagedInput input) ;
    /**
     * 症状日志的新增或者修改方法接口定义
     */
    public TSymptomLogDto CreateOrEdit(TSymptomLogDto input);

     /**
     * 获取症状日志信息
     */
    public TSymptomLogDto Get(TSymptomLogPagedInput input);
 	 /**
     * 症状日志删除
     */
    public void Delete(IdInput input);

    /**
     * 症状日志批量删除
     */
    public void BatchDelete(IdsInput input);
  
	  /**
     * 症状日志导出
     */
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException;

}
