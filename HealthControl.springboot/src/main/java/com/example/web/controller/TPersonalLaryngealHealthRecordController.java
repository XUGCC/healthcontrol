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
 * 个人喉部健康档案控制器 
 */
@RestController()
@RequestMapping("/TPersonalLaryngealHealthRecord")
public class TPersonalLaryngealHealthRecordController {
    @Autowired
    private  TPersonalLaryngealHealthRecordService TPersonalLaryngealHealthRecordService;
    @Autowired
    private TPersonalLaryngealHealthRecordMapper TPersonalLaryngealHealthRecordMapper;
    /**
     * 个人喉部健康档案分页查询
     */
    @RequestMapping(value = "/List", method = RequestMethod.POST)
    @SneakyThrows
    public PagedResult<TPersonalLaryngealHealthRecordDto> List(@RequestBody TPersonalLaryngealHealthRecordPagedInput input)  {
        return TPersonalLaryngealHealthRecordService.List(input);
    }
     /**
     * 单个个人喉部健康档案查询接口
     */
    @RequestMapping(value = "/Get", method = RequestMethod.POST)
    @SneakyThrows
    public TPersonalLaryngealHealthRecordDto Get(@RequestBody TPersonalLaryngealHealthRecordPagedInput input) {

        return TPersonalLaryngealHealthRecordService.Get(input);
    }
  
    /**
     * 个人喉部健康档案创建或则修改
     */
    @RequestMapping(value = "/CreateOrEdit", method = RequestMethod.POST)
    public TPersonalLaryngealHealthRecordDto CreateOrEdit(@RequestBody TPersonalLaryngealHealthRecordDto input) throws Exception {
        return TPersonalLaryngealHealthRecordService.CreateOrEdit(input);
    }
    /**
     * 个人喉部健康档案删除
     */
    @RequestMapping(value = "/Delete", method = RequestMethod.POST)
    public void Delete(@RequestBody IdInput input)
    {
        TPersonalLaryngealHealthRecordService.Delete(input);
    }

    /**
     * 个人喉部健康档案批量删除
     */
    @RequestMapping(value = "/BatchDelete", method = RequestMethod.POST)
    public void BatchDelete(@RequestBody IdsInput input)
    {
        TPersonalLaryngealHealthRecordService.BatchDelete(input);
    }
	  /**
     * 个人喉部健康档案导出
     */
    @RequestMapping(value = "/Export", method = RequestMethod.GET)
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException {
      TPersonalLaryngealHealthRecordService.Export(query,response);
    }
  

 
}
