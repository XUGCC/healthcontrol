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
 * 科普评论功能的Service接口的定义清单
 */
public interface TScienceCommentService extends IService<TScienceComment> {

    /**
     * 科普评论的分页查询方法接口定义
     */
    public PagedResult<TScienceCommentDto> List(TScienceCommentPagedInput input) ;
    /**
     * 科普评论的新增或者修改方法接口定义
     */
    public TScienceCommentDto CreateOrEdit(TScienceCommentDto input);

     /**
     * 获取科普评论信息
     */
    public TScienceCommentDto Get(TScienceCommentPagedInput input);
 	 /**
     * 科普评论删除
     */
    public void Delete(IdInput input);

    /**
     * 科普评论批量删除
     */
    public void BatchDelete(IdsInput input);
  
	  /**
     * 科普评论导出
     */
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException;

}
