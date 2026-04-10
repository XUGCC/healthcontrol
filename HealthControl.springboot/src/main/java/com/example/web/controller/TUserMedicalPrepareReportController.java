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
 * 用户就诊准备报告控制器 
 */
@RestController()
@RequestMapping("/TUserMedicalPrepareReport")
public class TUserMedicalPrepareReportController {
    @Autowired
    private  TUserMedicalPrepareReportService TUserMedicalPrepareReportService;
    @Autowired
    private TUserMedicalPrepareReportMapper TUserMedicalPrepareReportMapper;
    /**
     * 用户就诊准备报告分页查询
     */
    @RequestMapping(value = "/List", method = RequestMethod.POST)
    @SneakyThrows
    public PagedResult<TUserMedicalPrepareReportDto> List(@RequestBody TUserMedicalPrepareReportPagedInput input)  {
        return TUserMedicalPrepareReportService.List(input);
    }
     /**
     * 单个用户就诊准备报告查询接口
     */
    @RequestMapping(value = "/Get", method = RequestMethod.POST)
    @SneakyThrows
    public TUserMedicalPrepareReportDto Get(@RequestBody TUserMedicalPrepareReportPagedInput input) {

        return TUserMedicalPrepareReportService.Get(input);
    }
  
    /**
     * 用户就诊准备报告创建或则修改
     */
    @RequestMapping(value = "/CreateOrEdit", method = RequestMethod.POST)
    public TUserMedicalPrepareReportDto CreateOrEdit(@RequestBody TUserMedicalPrepareReportDto input) throws Exception {
        return TUserMedicalPrepareReportService.CreateOrEdit(input);
    }
    /**
     * 用户就诊准备报告删除
     */
    @RequestMapping(value = "/Delete", method = RequestMethod.POST)
    public void Delete(@RequestBody IdInput input)
    {
        TUserMedicalPrepareReportService.Delete(input);
    }

    /**
     * 用户就诊准备报告批量删除
     */
    @RequestMapping(value = "/BatchDelete", method = RequestMethod.POST)
    public void BatchDelete(@RequestBody IdsInput input)
    {
        TUserMedicalPrepareReportService.BatchDelete(input);
    }
	  /**
     * 用户就诊准备报告导出
     */
    @RequestMapping(value = "/Export", method = RequestMethod.GET)
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException {
      TUserMedicalPrepareReportService.Export(query,response);
    }
  

 
}
