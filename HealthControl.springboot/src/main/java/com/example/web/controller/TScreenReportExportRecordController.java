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
 * 自查报告导出记录控制器 
 */
@RestController()
@RequestMapping("/TScreenReportExportRecord")
public class TScreenReportExportRecordController {
    @Autowired
    private  TScreenReportExportRecordService TScreenReportExportRecordService;
    @Autowired
    private TScreenReportExportRecordMapper TScreenReportExportRecordMapper;
    /**
     * 自查报告导出记录分页查询
     */
    @RequestMapping(value = "/List", method = RequestMethod.POST)
    @SneakyThrows
    public PagedResult<TScreenReportExportRecordDto> List(@RequestBody TScreenReportExportRecordPagedInput input)  {
        return TScreenReportExportRecordService.List(input);
    }
     /**
     * 单个自查报告导出记录查询接口
     */
    @RequestMapping(value = "/Get", method = RequestMethod.POST)
    @SneakyThrows
    public TScreenReportExportRecordDto Get(@RequestBody TScreenReportExportRecordPagedInput input) {

        return TScreenReportExportRecordService.Get(input);
    }
  
    /**
     * 自查报告导出记录创建或则修改
     */
    @RequestMapping(value = "/CreateOrEdit", method = RequestMethod.POST)
    public TScreenReportExportRecordDto CreateOrEdit(@RequestBody TScreenReportExportRecordDto input) throws Exception {
        return TScreenReportExportRecordService.CreateOrEdit(input);
    }
    /**
     * 自查报告导出记录删除
     */
    @RequestMapping(value = "/Delete", method = RequestMethod.POST)
    public void Delete(@RequestBody IdInput input)
    {
        TScreenReportExportRecordService.Delete(input);
    }

    /**
     * 自查报告导出记录批量删除
     */
    @RequestMapping(value = "/BatchDelete", method = RequestMethod.POST)
    public void BatchDelete(@RequestBody IdsInput input)
    {
        TScreenReportExportRecordService.BatchDelete(input);
    }
	  /**
     * 自查报告导出记录导出
     */
    @RequestMapping(value = "/Export", method = RequestMethod.GET)
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException {
      TScreenReportExportRecordService.Export(query,response);
    }
  

 
}
