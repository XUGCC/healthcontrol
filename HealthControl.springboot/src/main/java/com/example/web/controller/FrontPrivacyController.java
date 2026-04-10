package com.example.web.controller;

import com.example.web.dto.TUserPrivacySettingDto;
import com.example.web.dto.TDataExportRequestDto;
import com.example.web.dto.TDataDeleteRequestDto;
import com.example.web.dto.query.TUserPrivacySettingPagedInput;
import com.example.web.dto.front.FrontPrivacyDtos.*;
import com.example.web.service.TUserPrivacySettingService;
import com.example.web.service.front.FrontPrivacyService;
import com.example.web.tools.BaseContext;
import com.example.web.tools.dto.ResponseData;
import com.example.web.tools.exception.CustomException;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import com.example.web.tools.dto.PagedResult;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 前台隐私设置接口（小程序端）
 */
@RestController
@RequestMapping("/Front/Privacy")
public class FrontPrivacyController {

    @Autowired
    private TUserPrivacySettingService privacySettingService;

    @Autowired
    private FrontPrivacyService frontPrivacyService;

    /**
     * 获取隐私设置
     */
    @RequestMapping(value = "/GetSetting", method = RequestMethod.POST)
    @SneakyThrows
    public TUserPrivacySettingDto GetSetting(@RequestBody(required = false) Map<String, Object> input) {
        Integer userId = getCurrentUserId();
        if (userId == null || userId <= 0) {
            throw new CustomException("用户未登录");
        }

        TUserPrivacySettingPagedInput queryInput = new TUserPrivacySettingPagedInput();
        queryInput.setUserId(userId);
        TUserPrivacySettingDto setting = privacySettingService.Get(queryInput);

        // 如果不存在，创建默认设置
        if (setting == null || setting.getId() == null) {
            setting = new TUserPrivacySettingDto();
            setting.setUserId(userId);
            setting.setLocalStorageStatus(false);
            setting.setDataAnonymousAuth(false);
            setting.setPrivacyAgreeStatus(false);
            // 默认关闭自动清理，保留时长为永久
            setting.setDataRetentionEnabled(false);
            setting.setDataRetentionMonths(null);
            setting.setIsDelete(false);
            setting = privacySettingService.CreateOrEdit(setting);
        }

        return setting;
    }

    /**
     * 更新隐私设置
     */
    @RequestMapping(value = "/UpdateSetting", method = RequestMethod.POST)
    @SneakyThrows
    public TUserPrivacySettingDto UpdateSetting(@RequestBody TUserPrivacySettingDto input) {
        Integer userId = getCurrentUserId();
        if (userId == null || userId <= 0) {
            throw new CustomException("用户未登录");
        }

        // 确保只能更新自己的设置
        input.setUserId(userId);
        return privacySettingService.CreateOrEdit(input);
    }

    /**
     * 同意隐私协议
     */
    @RequestMapping(value = "/AgreeProtocol", method = RequestMethod.POST)
    @SneakyThrows
    public ResponseData<Map<String, Object>> AgreeProtocol(@RequestBody(required = false) Map<String, Object> input) {
        Integer userId = getCurrentUserId();
        if (userId == null || userId <= 0) {
            throw new CustomException("用户未登录");
        }

        // 获取或创建隐私设置
        TUserPrivacySettingPagedInput queryInput = new TUserPrivacySettingPagedInput();
        queryInput.setUserId(userId);
        TUserPrivacySettingDto setting = privacySettingService.Get(queryInput);

        if (setting == null || setting.getId() == null) {
            setting = new TUserPrivacySettingDto();
            setting.setUserId(userId);
            setting.setLocalStorageStatus(false);
            setting.setDataAnonymousAuth(false);
            setting.setPrivacyAgreeStatus(true);
            setting.setDataRetentionEnabled(false);
            setting.setDataRetentionMonths(null);
            setting.setIsDelete(false);
        } else {
            setting.setPrivacyAgreeStatus(true);
        }

        privacySettingService.CreateOrEdit(setting);

        Map<String, Object> result = new HashMap<>();
        result.put("Success", true);
        return ResponseData.GetResponseDataInstance(result, "隐私协议已同意", true);
    }

    /**
     * 提交导出申请
     */
    @RequestMapping(value = "/ExportRequest", method = RequestMethod.POST)
    public Map<String, Object> ExportRequest(@RequestBody FrontPrivacyExportRequestInput input) {
        return frontPrivacyService.exportRequest(input);
    }

    /**
     * 查询导出申请列表
     */
    @RequestMapping(value = "/ExportRequestList", method = RequestMethod.POST)
    public PagedResult<TDataExportRequestDto> ExportRequestList(@RequestBody(required = false) FrontPrivacyExportRequestListInput input) {
        return frontPrivacyService.exportRequestList(input);
    }

    /**
     * 下载导出文件
     */
    @RequestMapping(value = "/DownloadExport", method = RequestMethod.GET)
    public void DownloadExport(@RequestParam("RequestId") Integer requestId, HttpServletResponse response) {
        frontPrivacyService.downloadExport(requestId, response);
    }

    /**
     * 提交删除申请
     */
    @RequestMapping(value = "/DeleteRequest", method = RequestMethod.POST)
    public Map<String, Object> DeleteRequest(@RequestBody FrontPrivacyDeleteRequestInput input) {
        return frontPrivacyService.deleteRequest(input);
    }

    /**
     * 查询删除申请列表
     */
    @RequestMapping(value = "/DeleteRequestList", method = RequestMethod.POST)
    public PagedResult<TDataDeleteRequestDto> DeleteRequestList(@RequestBody(required = false) FrontPrivacyDeleteRequestListInput input) {
        return frontPrivacyService.deleteRequestList(input);
    }

    private Integer getCurrentUserId() {
        if (BaseContext.getCurrentUserDto() == null || BaseContext.getCurrentUserDto().getUserId() == null) {
            return 0;
        }
        return BaseContext.getCurrentUserDto().getUserId();
    }
}
