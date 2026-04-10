package com.example.web.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.web.dto.*;
import com.example.web.dto.query.*;
import com.example.web.entity.*;
import com.example.web.tools.dto.*;
import com.example.web.enums.*;
import java.lang.reflect.InvocationTargetException;
import org.springframework.web.bind.annotation.RequestParam;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import jakarta.servlet.http.HttpServletResponse;
/**
 * 耳鼻喉医院医生功能的Service接口的定义清单
 */
public interface TOtolaryngologyHospitalDoctorService extends IService<TOtolaryngologyHospitalDoctor> {

    /**
     * 耳鼻喉医院医生的分页查询方法接口定义
     */
    public PagedResult<TOtolaryngologyHospitalDoctorDto> List(TOtolaryngologyHospitalDoctorPagedInput input) ;
    /**
     * 耳鼻喉医院医生的新增或者修改方法接口定义
     */
    public TOtolaryngologyHospitalDoctorDto CreateOrEdit(TOtolaryngologyHospitalDoctorDto input);

     /**
     * 获取耳鼻喉医院医生信息
     */
    public TOtolaryngologyHospitalDoctorDto Get(TOtolaryngologyHospitalDoctorPagedInput input);
 	 /**
     * 耳鼻喉医院医生删除
     */
    public void Delete(IdInput input);

    /**
     * 耳鼻喉医院医生批量删除
     */
    public void BatchDelete(IdsInput input);
  
	  /**
     * 耳鼻喉医院医生导出
     */
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException;

}
