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
 * 喉部食物库功能的Service接口的定义清单
 */
public interface TLaryngealFoodService extends IService<TLaryngealFood> {

    /**
     * 喉部食物库的分页查询方法接口定义
     */
    public PagedResult<TLaryngealFoodDto> List(TLaryngealFoodPagedInput input) ;
    /**
     * 喉部食物库的新增或者修改方法接口定义
     */
    public TLaryngealFoodDto CreateOrEdit(TLaryngealFoodDto input);

     /**
     * 获取喉部食物库信息
     */
    public TLaryngealFoodDto Get(TLaryngealFoodPagedInput input);
 	 /**
     * 喉部食物库删除
     */
    public void Delete(IdInput input);

    /**
     * 喉部食物库批量删除
     */
    public void BatchDelete(IdsInput input);
  
	  /**
     * 喉部食物库导出
     */
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException;

}
