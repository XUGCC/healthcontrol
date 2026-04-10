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
 * 个性化健康提醒控制器 
 */
@RestController()
@RequestMapping("/TPersonalHealthRemind")
public class TPersonalHealthRemindController {
    @Autowired
    private  TPersonalHealthRemindService TPersonalHealthRemindService;
    @Autowired
    private TPersonalHealthRemindMapper TPersonalHealthRemindMapper;
    /**
     * 个性化健康提醒分页查询
     */
    @RequestMapping(value = "/List", method = RequestMethod.POST)
    @SneakyThrows
    public PagedResult<TPersonalHealthRemindDto> List(@RequestBody TPersonalHealthRemindPagedInput input)  {
        return TPersonalHealthRemindService.List(input);
    }
     /**
     * 单个个性化健康提醒查询接口
     */
    @RequestMapping(value = "/Get", method = RequestMethod.POST)
    @SneakyThrows
    public TPersonalHealthRemindDto Get(@RequestBody TPersonalHealthRemindPagedInput input) {

        return TPersonalHealthRemindService.Get(input);
    }
  
    /**
     * 个性化健康提醒创建或则修改
     */
    @RequestMapping(value = "/CreateOrEdit", method = RequestMethod.POST)
    public TPersonalHealthRemindDto CreateOrEdit(@RequestBody TPersonalHealthRemindDto input) throws Exception {
        return TPersonalHealthRemindService.CreateOrEdit(input);
    }
    /**
     * 个性化健康提醒删除
     */
    @RequestMapping(value = "/Delete", method = RequestMethod.POST)
    public void Delete(@RequestBody IdInput input)
    {
        TPersonalHealthRemindService.Delete(input);
    }

    /**
     * 个性化健康提醒批量删除
     */
    @RequestMapping(value = "/BatchDelete", method = RequestMethod.POST)
    public void BatchDelete(@RequestBody IdsInput input)
    {
        TPersonalHealthRemindService.BatchDelete(input);
    }
	  /**
     * 个性化健康提醒导出
     */
    @RequestMapping(value = "/Export", method = RequestMethod.GET)
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException {
      TPersonalHealthRemindService.Export(query,response);
    }
  
    /**
     * 今天不再提醒
     */
    @RequestMapping(value = "/SnoozeToday", method = RequestMethod.POST)
    public void SnoozeToday(@RequestBody TPersonalHealthRemindDto input) {
        TPersonalHealthRemindService.SnoozeToday(input.getUserId(), input.getRemindType());
    }

 
}
