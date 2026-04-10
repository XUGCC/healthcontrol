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
 * 用户饮食记录控制器 
 */
@RestController()
@RequestMapping("/TUserDietRecord")
public class TUserDietRecordController {
    @Autowired
    private  TUserDietRecordService TUserDietRecordService;
    @Autowired
    private TUserDietRecordMapper TUserDietRecordMapper;
    /**
     * 用户饮食记录分页查询
     */
    @RequestMapping(value = "/List", method = RequestMethod.POST)
    @SneakyThrows
    public PagedResult<TUserDietRecordDto> List(@RequestBody TUserDietRecordPagedInput input)  {
        return TUserDietRecordService.List(input);
    }
     /**
     * 单个用户饮食记录查询接口
     */
    @RequestMapping(value = "/Get", method = RequestMethod.POST)
    @SneakyThrows
    public TUserDietRecordDto Get(@RequestBody TUserDietRecordPagedInput input) {

        return TUserDietRecordService.Get(input);
    }
  
    /**
     * 用户饮食记录创建或则修改
     */
    @RequestMapping(value = "/CreateOrEdit", method = RequestMethod.POST)
    public TUserDietRecordDto CreateOrEdit(@RequestBody TUserDietRecordDto input) throws Exception {
        return TUserDietRecordService.CreateOrEdit(input);
    }
    /**
     * 用户饮食记录删除
     */
    @RequestMapping(value = "/Delete", method = RequestMethod.POST)
    public void Delete(@RequestBody IdInput input)
    {
        TUserDietRecordService.Delete(input);
    }

    /**
     * 用户饮食记录批量删除
     */
    @RequestMapping(value = "/BatchDelete", method = RequestMethod.POST)
    public void BatchDelete(@RequestBody IdsInput input)
    {
        TUserDietRecordService.BatchDelete(input);
    }
	  /**
     * 用户饮食记录导出
     */
    @RequestMapping(value = "/Export", method = RequestMethod.GET)
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException {
      TUserDietRecordService.Export(query,response);
    }
  

 
}
