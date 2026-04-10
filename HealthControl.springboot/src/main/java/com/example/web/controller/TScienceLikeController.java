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
 * 科普点赞控制器 
 */
@RestController()
@RequestMapping("/TScienceLike")
public class TScienceLikeController {
    @Autowired
    private  TScienceLikeService TScienceLikeService;
    @Autowired
    private TScienceLikeMapper TScienceLikeMapper;
    /**
     * 科普点赞分页查询
     */
    @RequestMapping(value = "/List", method = RequestMethod.POST)
    @SneakyThrows
    public PagedResult<TScienceLikeDto> List(@RequestBody TScienceLikePagedInput input)  {
        return TScienceLikeService.List(input);
    }
     /**
     * 单个科普点赞查询接口
     */
    @RequestMapping(value = "/Get", method = RequestMethod.POST)
    @SneakyThrows
    public TScienceLikeDto Get(@RequestBody TScienceLikePagedInput input) {

        return TScienceLikeService.Get(input);
    }
  
    /**
     * 科普点赞创建或则修改
     */
    @RequestMapping(value = "/CreateOrEdit", method = RequestMethod.POST)
    public TScienceLikeDto CreateOrEdit(@RequestBody TScienceLikeDto input) throws Exception {
        return TScienceLikeService.CreateOrEdit(input);
    }
    /**
     * 科普点赞删除
     */
    @RequestMapping(value = "/Delete", method = RequestMethod.POST)
    public void Delete(@RequestBody IdInput input)
    {
        TScienceLikeService.Delete(input);
    }

    /**
     * 科普点赞批量删除
     */
    @RequestMapping(value = "/BatchDelete", method = RequestMethod.POST)
    public void BatchDelete(@RequestBody IdsInput input)
    {
        TScienceLikeService.BatchDelete(input);
    }
	  /**
     * 科普点赞导出
     */
    @RequestMapping(value = "/Export", method = RequestMethod.GET)
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException {
      TScienceLikeService.Export(query,response);
    }
  

 
}
