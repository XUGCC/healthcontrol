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
 * 风险评估问卷控制器 
 */
@RestController()
@RequestMapping("/TRiskAssessmentQuestionnaire")
public class TRiskAssessmentQuestionnaireController {
    @Autowired
    private  TRiskAssessmentQuestionnaireService TRiskAssessmentQuestionnaireService;
    @Autowired
    private TRiskAssessmentQuestionnaireMapper TRiskAssessmentQuestionnaireMapper;
    /**
     * 风险评估问卷分页查询
     */
    @RequestMapping(value = "/List", method = RequestMethod.POST)
    @SneakyThrows
    public PagedResult<TRiskAssessmentQuestionnaireDto> List(@RequestBody TRiskAssessmentQuestionnairePagedInput input)  {
        return TRiskAssessmentQuestionnaireService.List(input);
    }
     /**
     * 单个风险评估问卷查询接口
     */
    @RequestMapping(value = "/Get", method = RequestMethod.POST)
    @SneakyThrows
    public TRiskAssessmentQuestionnaireDto Get(@RequestBody TRiskAssessmentQuestionnairePagedInput input) {

        return TRiskAssessmentQuestionnaireService.Get(input);
    }
  
    /**
     * 风险评估问卷创建或则修改
     */
    @RequestMapping(value = "/CreateOrEdit", method = RequestMethod.POST)
    public TRiskAssessmentQuestionnaireDto CreateOrEdit(@RequestBody TRiskAssessmentQuestionnaireDto input) throws Exception {
        return TRiskAssessmentQuestionnaireService.CreateOrEdit(input);
    }
    /**
     * 风险评估问卷删除
     */
    @RequestMapping(value = "/Delete", method = RequestMethod.POST)
    public void Delete(@RequestBody IdInput input)
    {
        TRiskAssessmentQuestionnaireService.Delete(input);
    }

    /**
     * 风险评估问卷批量删除
     */
    @RequestMapping(value = "/BatchDelete", method = RequestMethod.POST)
    public void BatchDelete(@RequestBody IdsInput input)
    {
        TRiskAssessmentQuestionnaireService.BatchDelete(input);
    }
	  /**
     * 风险评估问卷导出
     */
    @RequestMapping(value = "/Export", method = RequestMethod.GET)
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException {
      TRiskAssessmentQuestionnaireService.Export(query,response);
    }
  

 
}
