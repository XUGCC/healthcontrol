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
 * 风险评估问卷功能的Service接口的定义清单
 */
public interface TRiskAssessmentQuestionnaireService extends IService<TRiskAssessmentQuestionnaire> {

    /**
     * 风险评估问卷的分页查询方法接口定义
     */
    public PagedResult<TRiskAssessmentQuestionnaireDto> List(TRiskAssessmentQuestionnairePagedInput input) ;
    /**
     * 风险评估问卷的新增或者修改方法接口定义
     */
    public TRiskAssessmentQuestionnaireDto CreateOrEdit(TRiskAssessmentQuestionnaireDto input);

     /**
     * 获取风险评估问卷信息
     */
    public TRiskAssessmentQuestionnaireDto Get(TRiskAssessmentQuestionnairePagedInput input);
 	 /**
     * 风险评估问卷删除
     */
    public void Delete(IdInput input);

    /**
     * 风险评估问卷批量删除
     */
    public void BatchDelete(IdsInput input);
  
	  /**
     * 风险评估问卷导出
     */
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException;

}
