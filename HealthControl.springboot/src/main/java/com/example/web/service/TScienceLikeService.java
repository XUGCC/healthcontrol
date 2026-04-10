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
 * 科普点赞功能的Service接口的定义清单
 */
public interface TScienceLikeService extends IService<TScienceLike> {

    /**
     * 科普点赞的分页查询方法接口定义
     */
    public PagedResult<TScienceLikeDto> List(TScienceLikePagedInput input) ;
    /**
     * 科普点赞的新增或者修改方法接口定义
     */
    public TScienceLikeDto CreateOrEdit(TScienceLikeDto input);

     /**
     * 获取科普点赞信息
     */
    public TScienceLikeDto Get(TScienceLikePagedInput input);
 	 /**
     * 科普点赞删除
     */
    public void Delete(IdInput input);

    /**
     * 科普点赞批量删除
     */
    public void BatchDelete(IdsInput input);
  
	  /**
     * 科普点赞导出
     */
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException;

}
