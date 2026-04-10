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
 * 健康科普功能的Service接口的定义清单
 */
public interface THealthScienceService extends IService<THealthScience> {

    /**
     * 健康科普的分页查询方法接口定义
     */
    public PagedResult<THealthScienceDto> List(THealthSciencePagedInput input) ;
    /**
     * 健康科普的新增或者修改方法接口定义
     */
    public THealthScienceDto CreateOrEdit(THealthScienceDto input);

     /**
     * 获取健康科普信息
     */
    public THealthScienceDto Get(THealthSciencePagedInput input);
 	 /**
     * 健康科普删除
     */
    public void Delete(IdInput input);

    /**
     * 健康科普批量删除
     */
    public void BatchDelete(IdsInput input);
  
	  /**
     * 健康科普导出
     */
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException;

}
