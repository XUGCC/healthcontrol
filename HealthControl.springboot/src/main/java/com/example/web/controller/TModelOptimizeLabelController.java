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
 * 模型优化标注控制器 
 */
@RestController()
@RequestMapping("/TModelOptimizeLabel")
public class TModelOptimizeLabelController {
    @Autowired
    private  TModelOptimizeLabelService TModelOptimizeLabelService;
    @Autowired
    private TModelOptimizeLabelMapper TModelOptimizeLabelMapper;
    /**
     * 模型优化标注分页查询
     */
    @RequestMapping(value = "/List", method = RequestMethod.POST)
    @SneakyThrows
    public PagedResult<TModelOptimizeLabelDto> List(@RequestBody TModelOptimizeLabelPagedInput input)  {
        return TModelOptimizeLabelService.List(input);
    }
     /**
     * 单个模型优化标注查询接口
     */
    @RequestMapping(value = "/Get", method = RequestMethod.POST)
    @SneakyThrows
    public TModelOptimizeLabelDto Get(@RequestBody TModelOptimizeLabelPagedInput input) {

        return TModelOptimizeLabelService.Get(input);
    }
  
    /**
     * 模型优化标注创建或则修改
     */
    @RequestMapping(value = "/CreateOrEdit", method = RequestMethod.POST)
    public TModelOptimizeLabelDto CreateOrEdit(@RequestBody TModelOptimizeLabelDto input) throws Exception {
        return TModelOptimizeLabelService.CreateOrEdit(input);
    }
    /**
     * 模型优化标注删除
     */
    @RequestMapping(value = "/Delete", method = RequestMethod.POST)
    public void Delete(@RequestBody IdInput input)
    {
        TModelOptimizeLabelService.Delete(input);
    }

    /**
     * 模型优化标注批量删除
     */
    @RequestMapping(value = "/BatchDelete", method = RequestMethod.POST)
    public void BatchDelete(@RequestBody IdsInput input)
    {
        TModelOptimizeLabelService.BatchDelete(input);
    }
	  /**
     * 模型优化标注导出
     */
    @RequestMapping(value = "/Export", method = RequestMethod.GET)
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException {
      TModelOptimizeLabelService.Export(query,response);
    }
  

 
}
