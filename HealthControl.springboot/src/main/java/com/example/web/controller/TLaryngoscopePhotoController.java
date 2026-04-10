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
 * 喉镜照片记录控制器 
 */
@RestController()
@RequestMapping("/TLaryngoscopePhoto")
public class TLaryngoscopePhotoController {
    @Autowired
    private  TLaryngoscopePhotoService TLaryngoscopePhotoService;
    @Autowired
    private TLaryngoscopePhotoMapper TLaryngoscopePhotoMapper;
    /**
     * 喉镜照片记录分页查询
     */
    @RequestMapping(value = "/List", method = RequestMethod.POST)
    @SneakyThrows
    public PagedResult<TLaryngoscopePhotoDto> List(@RequestBody TLaryngoscopePhotoPagedInput input)  {
        return TLaryngoscopePhotoService.List(input);
    }
     /**
     * 单个喉镜照片记录查询接口
     */
    @RequestMapping(value = "/Get", method = RequestMethod.POST)
    @SneakyThrows
    public TLaryngoscopePhotoDto Get(@RequestBody TLaryngoscopePhotoPagedInput input) {

        return TLaryngoscopePhotoService.Get(input);
    }
  
    /**
     * 喉镜照片记录创建或则修改
     */
    @RequestMapping(value = "/CreateOrEdit", method = RequestMethod.POST)
    public TLaryngoscopePhotoDto CreateOrEdit(@RequestBody TLaryngoscopePhotoDto input) throws Exception {
        return TLaryngoscopePhotoService.CreateOrEdit(input);
    }
    /**
     * 喉镜照片记录删除
     */
    @RequestMapping(value = "/Delete", method = RequestMethod.POST)
    public void Delete(@RequestBody IdInput input)
    {
        TLaryngoscopePhotoService.Delete(input);
    }

    /**
     * 喉镜照片记录批量删除
     */
    @RequestMapping(value = "/BatchDelete", method = RequestMethod.POST)
    public void BatchDelete(@RequestBody IdsInput input)
    {
        TLaryngoscopePhotoService.BatchDelete(input);
    }
	  /**
     * 喉镜照片记录导出
     */
    @RequestMapping(value = "/Export", method = RequestMethod.GET)
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException {
      TLaryngoscopePhotoService.Export(query,response);
    }
  

 
}
