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
 * 症状日志控制器 
 */
@RestController()
@RequestMapping("/TSymptomLog")
public class TSymptomLogController {
    @Autowired
    private  TSymptomLogService TSymptomLogService;
    @Autowired
    private TSymptomLogMapper TSymptomLogMapper;
    /**
     * 症状日志分页查询
     */
    @RequestMapping(value = "/List", method = RequestMethod.POST)
    @SneakyThrows
    public PagedResult<TSymptomLogDto> List(@RequestBody TSymptomLogPagedInput input)  {
        return TSymptomLogService.List(input);
    }
     /**
     * 单个症状日志查询接口
     */
    @RequestMapping(value = "/Get", method = RequestMethod.POST)
    @SneakyThrows
    public TSymptomLogDto Get(@RequestBody TSymptomLogPagedInput input) {

        return TSymptomLogService.Get(input);
    }
  
    /**
     * 症状日志创建或则修改
     */
    @RequestMapping(value = "/CreateOrEdit", method = RequestMethod.POST)
    public TSymptomLogDto CreateOrEdit(@RequestBody TSymptomLogDto input) throws Exception {
        return TSymptomLogService.CreateOrEdit(input);
    }
    /**
     * 症状日志删除
     */
    @RequestMapping(value = "/Delete", method = RequestMethod.POST)
    public void Delete(@RequestBody IdInput input)
    {
        TSymptomLogService.Delete(input);
    }

    /**
     * 症状日志批量删除
     */
    @RequestMapping(value = "/BatchDelete", method = RequestMethod.POST)
    public void BatchDelete(@RequestBody IdsInput input)
    {
        TSymptomLogService.BatchDelete(input);
    }
	  /**
     * 症状日志导出
     */
    @RequestMapping(value = "/Export", method = RequestMethod.GET)
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException {
      TSymptomLogService.Export(query,response);
    }
  

 
}
