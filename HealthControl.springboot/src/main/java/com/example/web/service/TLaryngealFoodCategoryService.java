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
 * 喉部食物库分类功能的Service接口的定义清单
 */
public interface TLaryngealFoodCategoryService extends IService<TLaryngealFoodCategory> {

    /**
     * 喉部食物库分类的分页查询方法接口定义
     */
    public PagedResult<TLaryngealFoodCategoryDto> List(TLaryngealFoodCategoryPagedInput input) ;
    /**
     * 喉部食物库分类的新增或者修改方法接口定义
     */
    public TLaryngealFoodCategoryDto CreateOrEdit(TLaryngealFoodCategoryDto input);

     /**
     * 获取喉部食物库分类信息
     */
    public TLaryngealFoodCategoryDto Get(TLaryngealFoodCategoryPagedInput input);
 	 /**
     * 喉部食物库分类删除
     */
    public void Delete(IdInput input);

    /**
     * 喉部食物库分类批量删除
     */
    public void BatchDelete(IdsInput input);
  
	  /**
     * 喉部食物库分类导出
     */
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException;

}
