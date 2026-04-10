package com.example.web.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.web.dto.*;
import com.example.web.dto.query.*;
import com.example.web.entity.*;
import com.example.web.tools.dto.*;
import org.springframework.web.bind.annotation.RequestParam;
import java.io.IOException;
import jakarta.servlet.http.HttpServletResponse;
/**
 * 模型优化标注功能的Service接口的定义清单
 */
public interface TModelOptimizeLabelService extends IService<TModelOptimizeLabel> {

    /**
     * 模型优化标注的分页查询方法接口定义
     */
    public PagedResult<TModelOptimizeLabelDto> List(TModelOptimizeLabelPagedInput input) ;
    /**
     * 模型优化标注的新增或者修改方法接口定义
     */
    public TModelOptimizeLabelDto CreateOrEdit(TModelOptimizeLabelDto input);

     /**
     * 获取模型优化标注信息
     */
    public TModelOptimizeLabelDto Get(TModelOptimizeLabelPagedInput input);
 	 /**
     * 模型优化标注删除
     */
    public void Delete(IdInput input);

    /**
     * 模型优化标注批量删除
     */
    public void BatchDelete(IdsInput input);
  
	  /**
     * 模型优化标注导出
     */
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException;

    /**
     * 前台创建或更新标注（带权限校验和隐私设置联动）
     */
    public TModelOptimizeLabelDto CreateOrEditForFront(TModelOptimizeLabelDto input);

    /**
     * 前台获取用户标注列表（仅返回当前用户的数据）
     */
    public PagedResult<TModelOptimizeLabelDto> ListForFront(TModelOptimizeLabelPagedInput input);

    /**
     * 前台获取单条标注详情（带权限校验）
     */
    public TModelOptimizeLabelDto GetForFront(TModelOptimizeLabelPagedInput input);

    /**
     * 前台更新授权状态（带权限校验和隐私设置联动）
     */
    public void UpdateAuthForFront(TModelOptimizeLabelDto input);

    /**
     * 前台删除标注（带权限校验，软删除）
     */
    public void DeleteForFront(TModelOptimizeLabelPagedInput input);

    /**
     * 前台获取用户检测记录列表（用于选择关联）
     */
    public PagedResult<TAudioScreenRecordDto> GetUserDetectListForFront(TModelOptimizeLabelPagedInput input);

}
