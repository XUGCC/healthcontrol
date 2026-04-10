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
 * 音频自查记录功能的Service接口的定义清单
 */
public interface TAudioScreenRecordService extends IService<TAudioScreenRecord> {

    /**
     * 音频自查记录的分页查询方法接口定义
     */
    public PagedResult<TAudioScreenRecordDto> List(TAudioScreenRecordPagedInput input) ;
    /**
     * 音频自查记录的新增或者修改方法接口定义
     */
    public TAudioScreenRecordDto CreateOrEdit(TAudioScreenRecordDto input);

     /**
     * 获取音频自查记录信息
     */
    public TAudioScreenRecordDto Get(TAudioScreenRecordPagedInput input);
 	 /**
     * 音频自查记录删除
     */
    public void Delete(IdInput input);

    /**
     * 音频自查记录批量删除
     */
    public void BatchDelete(IdsInput input);
  
	  /**
     * 音频自查记录导出
     */
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException;

    /**
     * 触发音频检测（异步）：生成MFCC并给出良/恶性初筛结果
     */
    public void StartDetect(IdInput input);

}
