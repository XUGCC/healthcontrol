package com.example.web.controller;
import com.example.web.SysConst;
import com.example.web.dto.*;
import com.example.web.dto.admin.ModelInterfaceStatsDtos.ModelInterfaceStatsSummaryInput;
import com.example.web.dto.admin.ModelInterfaceStatsDtos.ModelInterfaceStatsSummaryOutput;
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
 * 模型接口调用日志控制器 
 */
@RestController()
@RequestMapping("/TModelInterfaceCallLog")
public class TModelInterfaceCallLogController {
    @Autowired
    private  TModelInterfaceCallLogService TModelInterfaceCallLogService;
    @Autowired
    private TModelInterfaceCallLogMapper TModelInterfaceCallLogMapper;
    /**
     * 模型接口调用日志分页查询
     */
    @RequestMapping(value = "/List", method = RequestMethod.POST)
    @SneakyThrows
    public PagedResult<TModelInterfaceCallLogDto> List(@RequestBody TModelInterfaceCallLogPagedInput input)  {
        return TModelInterfaceCallLogService.List(input);
    }
     /**
     * 单个模型接口调用日志查询接口
     */
    @RequestMapping(value = "/Get", method = RequestMethod.POST)
    @SneakyThrows
    public TModelInterfaceCallLogDto Get(@RequestBody TModelInterfaceCallLogPagedInput input) {

        return TModelInterfaceCallLogService.Get(input);
    }
  
    /**
     * 模型接口调用日志创建或则修改
     */
    @RequestMapping(value = "/CreateOrEdit", method = RequestMethod.POST)
    public TModelInterfaceCallLogDto CreateOrEdit(@RequestBody TModelInterfaceCallLogDto input) throws Exception {
        return TModelInterfaceCallLogService.CreateOrEdit(input);
    }
    /**
     * 模型接口调用日志删除
     */
    @RequestMapping(value = "/Delete", method = RequestMethod.POST)
    public void Delete(@RequestBody IdInput input)
    {
        TModelInterfaceCallLogService.Delete(input);
    }

    /**
     * 模型接口调用日志批量删除
     */
    @RequestMapping(value = "/BatchDelete", method = RequestMethod.POST)
    public void BatchDelete(@RequestBody IdsInput input)
    {
        TModelInterfaceCallLogService.BatchDelete(input);
    }
	  /**
     * 模型接口调用日志导出
     */
    @RequestMapping(value = "/Export", method = RequestMethod.GET)
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException {
      TModelInterfaceCallLogService.Export(query,response);
    }
  
    /**
     * 模型接口调用日志统计概要（管理端看板）
     */
    @RequestMapping(value = "/StatsSummary", method = RequestMethod.POST)
    public ModelInterfaceStatsSummaryOutput StatsSummary(@RequestBody(required = false) ModelInterfaceStatsSummaryInput input) {
        return TModelInterfaceCallLogService.StatsSummary(input == null ? new ModelInterfaceStatsSummaryInput() : input);
    }
 
}
