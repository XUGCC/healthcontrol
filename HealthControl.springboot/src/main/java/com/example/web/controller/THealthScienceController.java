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
 * 健康科普控制器 
 */
@RestController()
@RequestMapping("/THealthScience")
public class THealthScienceController {
    @Autowired
    private  THealthScienceService THealthScienceService;
    @Autowired
    private THealthScienceMapper THealthScienceMapper;
    /**
     * 健康科普分页查询
     */
    @RequestMapping(value = "/List", method = RequestMethod.POST)
    @SneakyThrows
    public PagedResult<THealthScienceDto> List(@RequestBody THealthSciencePagedInput input)  {
        return THealthScienceService.List(input);
    }
     /**
     * 单个健康科普查询接口
     */
    @RequestMapping(value = "/Get", method = RequestMethod.POST)
    @SneakyThrows
    public THealthScienceDto Get(@RequestBody THealthSciencePagedInput input) {

        return THealthScienceService.Get(input);
    }
  
    /**
     * 健康科普创建或则修改
     */
    @RequestMapping(value = "/CreateOrEdit", method = RequestMethod.POST)
    public THealthScienceDto CreateOrEdit(@RequestBody THealthScienceDto input) throws Exception {
        return THealthScienceService.CreateOrEdit(input);
    }
    /**
     * 健康科普删除
     */
    @RequestMapping(value = "/Delete", method = RequestMethod.POST)
    public void Delete(@RequestBody IdInput input)
    {
        THealthScienceService.Delete(input);
    }

    /**
     * 健康科普批量删除
     */
    @RequestMapping(value = "/BatchDelete", method = RequestMethod.POST)
    public void BatchDelete(@RequestBody IdsInput input)
    {
        THealthScienceService.BatchDelete(input);
    }
	  /**
     * 健康科普导出
     */
    @RequestMapping(value = "/Export", method = RequestMethod.GET)
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException {
      THealthScienceService.Export(query,response);
    }
  

 
}
