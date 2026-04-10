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
 * 用户问卷答题控制器 
 */
@RestController()
@RequestMapping("/TUserQuestionnaireAnswer")
public class TUserQuestionnaireAnswerController {
    @Autowired
    private  TUserQuestionnaireAnswerService TUserQuestionnaireAnswerService;
    @Autowired
    private TUserQuestionnaireAnswerMapper TUserQuestionnaireAnswerMapper;
    /**
     * 用户问卷答题分页查询
     */
    @RequestMapping(value = "/List", method = RequestMethod.POST)
    @SneakyThrows
    public PagedResult<TUserQuestionnaireAnswerDto> List(@RequestBody TUserQuestionnaireAnswerPagedInput input)  {
        return TUserQuestionnaireAnswerService.List(input);
    }
     /**
     * 单个用户问卷答题查询接口
     */
    @RequestMapping(value = "/Get", method = RequestMethod.POST)
    @SneakyThrows
    public TUserQuestionnaireAnswerDto Get(@RequestBody TUserQuestionnaireAnswerPagedInput input) {

        return TUserQuestionnaireAnswerService.Get(input);
    }
  
    /**
     * 用户问卷答题创建或则修改
     */
    @RequestMapping(value = "/CreateOrEdit", method = RequestMethod.POST)
    public TUserQuestionnaireAnswerDto CreateOrEdit(@RequestBody TUserQuestionnaireAnswerDto input) throws Exception {
        return TUserQuestionnaireAnswerService.CreateOrEdit(input);
    }
    /**
     * 用户问卷答题删除
     */
    @RequestMapping(value = "/Delete", method = RequestMethod.POST)
    public void Delete(@RequestBody IdInput input)
    {
        TUserQuestionnaireAnswerService.Delete(input);
    }

    /**
     * 用户问卷答题批量删除
     */
    @RequestMapping(value = "/BatchDelete", method = RequestMethod.POST)
    public void BatchDelete(@RequestBody IdsInput input)
    {
        TUserQuestionnaireAnswerService.BatchDelete(input);
    }
	  /**
     * 用户问卷答题导出
     */
    @RequestMapping(value = "/Export", method = RequestMethod.GET)
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException {
      TUserQuestionnaireAnswerService.Export(query,response);
    }
  

 
}
