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
 * 科普收藏控制器 
 */
@RestController()
@RequestMapping("/TScienceCollect")
public class TScienceCollectController {
    @Autowired
    private  TScienceCollectService TScienceCollectService;
    @Autowired
    private TScienceCollectMapper TScienceCollectMapper;
    /**
     * 科普收藏分页查询
     */
    @RequestMapping(value = "/List", method = RequestMethod.POST)
    @SneakyThrows
    public PagedResult<TScienceCollectDto> List(@RequestBody TScienceCollectPagedInput input)  {
        return TScienceCollectService.List(input);
    }
     /**
     * 单个科普收藏查询接口
     */
    @RequestMapping(value = "/Get", method = RequestMethod.POST)
    @SneakyThrows
    public TScienceCollectDto Get(@RequestBody TScienceCollectPagedInput input) {

        return TScienceCollectService.Get(input);
    }
  
    /**
     * 科普收藏创建或则修改
     */
    @RequestMapping(value = "/CreateOrEdit", method = RequestMethod.POST)
    public TScienceCollectDto CreateOrEdit(@RequestBody TScienceCollectDto input) throws Exception {
        return TScienceCollectService.CreateOrEdit(input);
    }
    /**
     * 科普收藏删除
     */
    @RequestMapping(value = "/Delete", method = RequestMethod.POST)
    public void Delete(@RequestBody IdInput input)
    {
        TScienceCollectService.Delete(input);
    }

    /**
     * 科普收藏批量删除
     */
    @RequestMapping(value = "/BatchDelete", method = RequestMethod.POST)
    public void BatchDelete(@RequestBody IdsInput input)
    {
        TScienceCollectService.BatchDelete(input);
    }
	  /**
     * 科普收藏导出
     */
    @RequestMapping(value = "/Export", method = RequestMethod.GET)
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException {
      TScienceCollectService.Export(query,response);
    }
  

 
}
