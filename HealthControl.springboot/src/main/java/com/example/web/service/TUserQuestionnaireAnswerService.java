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
 * 用户问卷答题功能的Service接口的定义清单
 */
public interface TUserQuestionnaireAnswerService extends IService<TUserQuestionnaireAnswer> {

    /**
     * 用户问卷答题的分页查询方法接口定义
     */
    public PagedResult<TUserQuestionnaireAnswerDto> List(TUserQuestionnaireAnswerPagedInput input) ;
    /**
     * 用户问卷答题的新增或者修改方法接口定义
     */
    public TUserQuestionnaireAnswerDto CreateOrEdit(TUserQuestionnaireAnswerDto input);

     /**
     * 获取用户问卷答题信息
     */
    public TUserQuestionnaireAnswerDto Get(TUserQuestionnaireAnswerPagedInput input);
 	 /**
     * 用户问卷答题删除
     */
    public void Delete(IdInput input);

    /**
     * 用户问卷答题批量删除
     */
    public void BatchDelete(IdsInput input);
  
	  /**
     * 用户问卷答题导出
     */
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException;

}
