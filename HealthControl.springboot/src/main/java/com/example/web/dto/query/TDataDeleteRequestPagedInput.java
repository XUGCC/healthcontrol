package com.example.web.dto.query;

import com.example.web.tools.dto.PagedInput;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据删除申请查询模型
 */
@NoArgsConstructor
@Data
public class TDataDeleteRequestPagedInput extends PagedInput {

    /**
     * Id 主键
     */
    @JsonProperty("Id")
    private Integer Id;

    /**
     * 关联用户表主键
     */
    @JsonProperty("UserId")
    private Integer UserId;

    /**
     * 申请类型：Delete=删除
     */
    @JsonProperty("RequestType")
    private String RequestType;

    /**
     * 状态：Pending/Processing/Completed/Failed
     */
    @JsonProperty("Status")
    private String Status;

    /**
     * 软删除标记
     */
    @JsonProperty("IsDelete")
    private Boolean IsDelete;

    /**
     * 创建时间范围（用于筛选）
     */
    @JsonProperty("CreationTimeRange")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private List<LocalDateTime> CreationTimeRange;
}

