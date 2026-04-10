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
 * 用户隐私设置控制器 
 */
@RestController()
@RequestMapping("/TUserPrivacySetting")
public class TUserPrivacySettingController {
    @Autowired
    private  TUserPrivacySettingService TUserPrivacySettingService;
    @Autowired
    private TUserPrivacySettingMapper TUserPrivacySettingMapper;
    /**
     * 用户隐私设置分页查询
     */
    @RequestMapping(value = "/List", method = RequestMethod.POST)
    @SneakyThrows
    public PagedResult<TUserPrivacySettingDto> List(@RequestBody TUserPrivacySettingPagedInput input)  {
        return TUserPrivacySettingService.List(input);
    }
     /**
     * 单个用户隐私设置查询接口
     */
    @RequestMapping(value = "/Get", method = RequestMethod.POST)
    @SneakyThrows
    public TUserPrivacySettingDto Get(@RequestBody TUserPrivacySettingPagedInput input) {

        return TUserPrivacySettingService.Get(input);
    }
  
    /**
     * 用户隐私设置创建或则修改
     */
    @RequestMapping(value = "/CreateOrEdit", method = RequestMethod.POST)
    public TUserPrivacySettingDto CreateOrEdit(@RequestBody TUserPrivacySettingDto input) throws Exception {
        return TUserPrivacySettingService.CreateOrEdit(input);
    }
    /**
     * 用户隐私设置删除
     */
    @RequestMapping(value = "/Delete", method = RequestMethod.POST)
    public void Delete(@RequestBody IdInput input)
    {
        TUserPrivacySettingService.Delete(input);
    }

    /**
     * 用户隐私设置批量删除
     */
    @RequestMapping(value = "/BatchDelete", method = RequestMethod.POST)
    public void BatchDelete(@RequestBody IdsInput input)
    {
        TUserPrivacySettingService.BatchDelete(input);
    }
	  /**
     * 用户隐私设置导出
     */
    @RequestMapping(value = "/Export", method = RequestMethod.GET)
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException {
      TUserPrivacySettingService.Export(query,response);
    }
  

 
}
