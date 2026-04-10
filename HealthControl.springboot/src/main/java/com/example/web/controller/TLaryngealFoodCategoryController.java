package com.example.web.controller;
import com.example.web.SysConst;
import com.example.web.dto.*;
import com.example.web.dto.query.*;
import com.example.web.entity.*;
import com.example.web.mapper.*;
import com.example.web.service.*;
import com.example.web.tools.dto.*;
import com.example.web.tools.exception.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import lombok.SneakyThrows;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletResponse;
/**
 * 喉部食物库分类控制器 
 */
@RestController()
@RequestMapping("/TLaryngealFoodCategory")
public class TLaryngealFoodCategoryController {
    @Autowired
    private  TLaryngealFoodCategoryService TLaryngealFoodCategoryService;
    @Autowired
    private TLaryngealFoodCategoryMapper TLaryngealFoodCategoryMapper;
    /**
     * 喉部食物库分类分页查询
     */
    @RequestMapping(value = "/List", method = RequestMethod.POST)
    @SneakyThrows
    public PagedResult<TLaryngealFoodCategoryDto> List(@RequestBody TLaryngealFoodCategoryPagedInput input)  {
        return TLaryngealFoodCategoryService.List(input);
    }
     /**
     * 单个喉部食物库分类查询接口
     */
    @RequestMapping(value = "/Get", method = RequestMethod.POST)
    @SneakyThrows
    public TLaryngealFoodCategoryDto Get(@RequestBody TLaryngealFoodCategoryPagedInput input) {

        return TLaryngealFoodCategoryService.Get(input);
    }
  
    /**
     * 喉部食物库分类创建或则修改
     */
    @RequestMapping(value = "/CreateOrEdit", method = RequestMethod.POST)
    public TLaryngealFoodCategoryDto CreateOrEdit(@RequestBody TLaryngealFoodCategoryDto input) throws Exception {
        return TLaryngealFoodCategoryService.CreateOrEdit(input);
    }
    /**
     * 喉部食物库分类删除
     */
    @RequestMapping(value = "/Delete", method = RequestMethod.POST)
    public void Delete(@RequestBody IdInput input)
    {
        TLaryngealFoodCategoryService.Delete(input);
    }

    /**
     * 喉部食物库分类批量删除
     */
    @RequestMapping(value = "/BatchDelete", method = RequestMethod.POST)
    public void BatchDelete(@RequestBody IdsInput input)
    {
        TLaryngealFoodCategoryService.BatchDelete(input);
    }
	  /**
     * 喉部食物库分类导出
     */
    @RequestMapping(value = "/Export", method = RequestMethod.GET)
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException {
      TLaryngealFoodCategoryService.Export(query,response);
    }
  

 
}
