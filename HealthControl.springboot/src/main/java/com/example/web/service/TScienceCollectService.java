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
 * 科普收藏功能的Service接口的定义清单
 */
public interface TScienceCollectService extends IService<TScienceCollect> {

    /**
     * 科普收藏的分页查询方法接口定义
     */
    public PagedResult<TScienceCollectDto> List(TScienceCollectPagedInput input) ;
    /**
     * 科普收藏的新增或者修改方法接口定义
     */
    public TScienceCollectDto CreateOrEdit(TScienceCollectDto input);

     /**
     * 获取科普收藏信息
     */
    public TScienceCollectDto Get(TScienceCollectPagedInput input);
 	 /**
     * 科普收藏删除
     */
    public void Delete(IdInput input);

    /**
     * 科普收藏批量删除
     */
    public void BatchDelete(IdsInput input);
  
	  /**
     * 科普收藏导出
     */
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException;

}
