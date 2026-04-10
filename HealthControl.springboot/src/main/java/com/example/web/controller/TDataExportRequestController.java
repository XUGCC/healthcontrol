package com.example.web.controller;

import com.example.web.dto.TDataExportRequestDto;
import com.example.web.dto.query.TDataExportRequestPagedInput;
import com.example.web.entity.TDataExportRequest;
import com.example.web.enums.RoleTypeEnum;
import com.example.web.mapper.TDataExportRequestMapper;
import com.example.web.service.TDataExportRequestService;
import com.example.web.tools.BaseContext;
import com.example.web.tools.dto.PagedResult;
import com.example.web.tools.exception.CustomException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 数据导出申请控制器（管理端）
 */
@RestController
@RequestMapping("/TDataExportRequest")
public class TDataExportRequestController {

    @Autowired
    private TDataExportRequestService dataExportRequestService;

    @Autowired
    private TDataExportRequestMapper exportRequestMapper;

    /**
     * 数据导出申请分页查询
     */
    @RequestMapping(value = "/List", method = RequestMethod.POST)
    @SneakyThrows
    public PagedResult<TDataExportRequestDto> List(@RequestBody TDataExportRequestPagedInput input) {
        return dataExportRequestService.List(input);
    }

    /**
     * 导出数据导出申请列表
     */
    @RequestMapping(value = "/Export", method = RequestMethod.GET)
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException {
        dataExportRequestService.Export(query, response);
    }

    /**
     * 批量删除导出申请（软删除 IsDelete=true）
     */
    @RequestMapping(value = "/BatchDelete", method = RequestMethod.POST)
    public void BatchDelete(@RequestBody com.example.web.tools.dto.IdsInput input) {
        requireAdmin();
        dataExportRequestService.BatchDelete(input);
    }

    /**
     * 管理端审核（通过/拒绝）导出申请
     * 入参：{ Id: number, Action: "Approve"|"Reject", Remark?: string }
     */
    @RequestMapping(value = "/Audit", method = RequestMethod.POST)
    public void Audit(@RequestBody Map<String, Object> input) {
        requireAdmin();
        if (input == null) throw new CustomException("请求体不能为空");
        Integer id = input.get("Id") == null ? null : Integer.valueOf(String.valueOf(input.get("Id")));
        String action = input.get("Action") == null ? null : String.valueOf(input.get("Action"));
        String remark = input.get("Remark") == null ? null : String.valueOf(input.get("Remark"));
        if (id == null || id <= 0) throw new CustomException("Id不能为空");
        if (action == null || action.trim().isEmpty()) throw new CustomException("Action不能为空");

        TDataExportRequest req = exportRequestMapper.selectById(id);
        if (req == null) throw new CustomException("导出申请不存在");
        // 仅允许审核待审核状态
        if (!"PendingAudit".equals(req.getStatus())) {
            throw new CustomException("仅待审核状态可审核，当前状态：" + req.getStatus());
        }

        LocalDateTime now = LocalDateTime.now();
        if ("Approve".equalsIgnoreCase(action)) {
            req.setStatus("Approved");
            req.setErrorMessage(null);
            req.setProcessTime(null);
            req.setUpdateTime(now);
            exportRequestMapper.updateById(req);
            return;
        }
        if ("Reject".equalsIgnoreCase(action)) {
            req.setStatus("Rejected");
            req.setErrorMessage(remark); // 复用 ErrorMessage 存放拒绝原因
            req.setProcessTime(now);
            req.setUpdateTime(now);
            exportRequestMapper.updateById(req);
            return;
        }
        throw new CustomException("Action仅支持 Approve / Reject");
    }

    private void requireAdmin() {
        if (BaseContext.getCurrentUserDto() == null
                || BaseContext.getCurrentUserDto().getRoleType() == null
                || BaseContext.getCurrentUserDto().getRoleType() != RoleTypeEnum.管理员) {
            throw new CustomException("仅管理员可操作");
        }
    }
}

