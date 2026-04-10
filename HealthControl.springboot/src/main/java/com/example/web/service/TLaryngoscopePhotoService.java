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
 * 喉镜照片记录功能的Service接口的定义清单
 */
public interface TLaryngoscopePhotoService extends IService<TLaryngoscopePhoto> {

    /**
     * 喉镜照片记录的分页查询方法接口定义
     */
    public PagedResult<TLaryngoscopePhotoDto> List(TLaryngoscopePhotoPagedInput input) ;
    /**
     * 喉镜照片记录的新增或者修改方法接口定义
     */
    public TLaryngoscopePhotoDto CreateOrEdit(TLaryngoscopePhotoDto input);

     /**
     * 获取喉镜照片记录信息
     */
    public TLaryngoscopePhotoDto Get(TLaryngoscopePhotoPagedInput input);
 	 /**
     * 喉镜照片记录删除
     */
    public void Delete(IdInput input);

    /**
     * 喉镜照片记录批量删除
     */
    public void BatchDelete(IdsInput input);
  
	  /**
     * 喉镜照片记录导出
     */
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException;

}
