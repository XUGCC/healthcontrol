package com.example.web.controller;

import com.example.web.dto.TAudioScreenRecordDto;
import com.example.web.dto.query.TAudioScreenRecordPagedInput;
import com.example.web.service.TAudioScreenRecordService;
import com.example.web.tools.dto.IdInput;
import com.example.web.tools.dto.IdsInput;
import com.example.web.tools.dto.PagedResult;
import com.example.web.tools.dto.ResponseData;
import lombok.SneakyThrows;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
/**
 * 音频自查记录控制器 
 */
@RestController()
@RequestMapping("/TAudioScreenRecord")
public class TAudioScreenRecordController {
    @Autowired
    private  TAudioScreenRecordService TAudioScreenRecordService;
    /**
     * 音频自查记录分页查询
     */
    @RequestMapping(value = "/List", method = RequestMethod.POST)
    @SneakyThrows
    public PagedResult<TAudioScreenRecordDto> List(@RequestBody TAudioScreenRecordPagedInput input)  {
        return TAudioScreenRecordService.List(input);
    }
     /**
     * 单个音频自查记录查询接口
     */
    @RequestMapping(value = "/Get", method = RequestMethod.POST)
    @SneakyThrows
    public TAudioScreenRecordDto Get(@RequestBody TAudioScreenRecordPagedInput input) {

        return TAudioScreenRecordService.Get(input);
    }
  
    /**
     * 音频自查记录创建或则修改
     */
    @RequestMapping(value = "/CreateOrEdit", method = RequestMethod.POST)
    public TAudioScreenRecordDto CreateOrEdit(@RequestBody TAudioScreenRecordDto input) throws Exception {
        return TAudioScreenRecordService.CreateOrEdit(input);
    }

    /**
     * 触发音频检测（异步）
     */
    @RequestMapping(value = "/StartDetect", method = RequestMethod.POST)
    public ResponseData<Object> StartDetect(@RequestBody IdInput input) {
        TAudioScreenRecordService.StartDetect(input);
        // 重要：不要返回 String，否则会触发 GlobalResponseAdvice + StringHttpMessageConverter 不匹配导致 500
        return ResponseData.OfSuccess();
    }
    /**
     * 音频自查记录删除
     */
    @RequestMapping(value = "/Delete", method = RequestMethod.POST)
    public void Delete(@RequestBody IdInput input)
    {
        TAudioScreenRecordService.Delete(input);
    }

    /**
     * 音频自查记录批量删除
     */
    @RequestMapping(value = "/BatchDelete", method = RequestMethod.POST)
    public void BatchDelete(@RequestBody IdsInput input)
    {
        TAudioScreenRecordService.BatchDelete(input);
    }
	  /**
     * 音频自查记录导出
     */
    @RequestMapping(value = "/Export", method = RequestMethod.GET)
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException {
      TAudioScreenRecordService.Export(query,response);
    }
  

 
}
