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
 * 耳鼻喉医院医生控制器 
 */
@RestController()
@RequestMapping("/TOtolaryngologyHospitalDoctor")
public class TOtolaryngologyHospitalDoctorController {
    @Autowired
    private  TOtolaryngologyHospitalDoctorService TOtolaryngologyHospitalDoctorService;
    @Autowired
    private TOtolaryngologyHospitalDoctorMapper TOtolaryngologyHospitalDoctorMapper;
    /**
     * 耳鼻喉医院医生分页查询
     */
    @RequestMapping(value = "/List", method = RequestMethod.POST)
    @SneakyThrows
    public PagedResult<TOtolaryngologyHospitalDoctorDto> List(@RequestBody TOtolaryngologyHospitalDoctorPagedInput input)  {
        return TOtolaryngologyHospitalDoctorService.List(input);
    }
     /**
     * 单个耳鼻喉医院医生查询接口
     */
    @RequestMapping(value = "/Get", method = RequestMethod.POST)
    @SneakyThrows
    public TOtolaryngologyHospitalDoctorDto Get(@RequestBody TOtolaryngologyHospitalDoctorPagedInput input) {

        return TOtolaryngologyHospitalDoctorService.Get(input);
    }
  
    /**
     * 耳鼻喉医院医生创建或则修改
     */
    @RequestMapping(value = "/CreateOrEdit", method = RequestMethod.POST)
    public TOtolaryngologyHospitalDoctorDto CreateOrEdit(@RequestBody TOtolaryngologyHospitalDoctorDto input) throws Exception {
        return TOtolaryngologyHospitalDoctorService.CreateOrEdit(input);
    }
    /**
     * 耳鼻喉医院医生删除
     */
    @RequestMapping(value = "/Delete", method = RequestMethod.POST)
    public void Delete(@RequestBody IdInput input)
    {
        TOtolaryngologyHospitalDoctorService.Delete(input);
    }

    /**
     * 耳鼻喉医院医生批量删除
     */
    @RequestMapping(value = "/BatchDelete", method = RequestMethod.POST)
    public void BatchDelete(@RequestBody IdsInput input)
    {
        TOtolaryngologyHospitalDoctorService.BatchDelete(input);
    }
	  /**
     * 耳鼻喉医院医生导出
     */
    @RequestMapping(value = "/Export", method = RequestMethod.GET)
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException {
      TOtolaryngologyHospitalDoctorService.Export(query,response);
    }
  

 
}
