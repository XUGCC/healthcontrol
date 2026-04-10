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
 * 个人喉部健康档案功能的Service接口的定义清单
 */
public interface TPersonalLaryngealHealthRecordService extends IService<TPersonalLaryngealHealthRecord> {

    /**
     * 个人喉部健康档案的分页查询方法接口定义
     */
    public PagedResult<TPersonalLaryngealHealthRecordDto> List(TPersonalLaryngealHealthRecordPagedInput input) ;
    /**
     * 个人喉部健康档案的新增或者修改方法接口定义
     */
    public TPersonalLaryngealHealthRecordDto CreateOrEdit(TPersonalLaryngealHealthRecordDto input);

     /**
     * 获取个人喉部健康档案信息
     */
    public TPersonalLaryngealHealthRecordDto Get(TPersonalLaryngealHealthRecordPagedInput input);
 	 /**
     * 个人喉部健康档案删除
     */
    public void Delete(IdInput input);

    /**
     * 个人喉部健康档案批量删除
     */
    public void BatchDelete(IdsInput input);
  
	  /**
     * 个人喉部健康档案导出
     */
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException;

}
