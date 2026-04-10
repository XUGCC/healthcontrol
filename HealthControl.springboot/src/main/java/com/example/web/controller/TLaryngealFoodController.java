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
 * 喉部食物库控制器 
 */
@RestController()
@RequestMapping("/TLaryngealFood")
public class TLaryngealFoodController {
    @Autowired
    private  TLaryngealFoodService TLaryngealFoodService;
    @Autowired
    private TLaryngealFoodMapper TLaryngealFoodMapper;
    /**
     * 喉部食物库分页查询
     */
    @RequestMapping(value = "/List", method = RequestMethod.POST)
    @SneakyThrows
    public PagedResult<TLaryngealFoodDto> List(@RequestBody TLaryngealFoodPagedInput input)  {
        return TLaryngealFoodService.List(input);
    }
     /**
     * 单个喉部食物库查询接口
     */
    @RequestMapping(value = "/Get", method = RequestMethod.POST)
    @SneakyThrows
    public TLaryngealFoodDto Get(@RequestBody TLaryngealFoodPagedInput input) {

        return TLaryngealFoodService.Get(input);
    }
  
    /**
     * 喉部食物库创建或则修改
     */
    @RequestMapping(value = "/CreateOrEdit", method = RequestMethod.POST)
    public TLaryngealFoodDto CreateOrEdit(@RequestBody TLaryngealFoodDto input) throws Exception {
        return TLaryngealFoodService.CreateOrEdit(input);
    }
    /**
     * 喉部食物库删除
     */
    @RequestMapping(value = "/Delete", method = RequestMethod.POST)
    public void Delete(@RequestBody IdInput input)
    {
        TLaryngealFoodService.Delete(input);
    }

    /**
     * 喉部食物库批量删除
     */
    @RequestMapping(value = "/BatchDelete", method = RequestMethod.POST)
    public void BatchDelete(@RequestBody IdsInput input)
    {
        TLaryngealFoodService.BatchDelete(input);
    }
	  /**
     * 喉部食物库导出
     */
    @RequestMapping(value = "/Export", method = RequestMethod.GET)
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException {
      TLaryngealFoodService.Export(query,response);
    }
  

 
}
