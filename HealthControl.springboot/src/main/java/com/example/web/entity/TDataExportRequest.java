package com.example.web.entity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.beanutils.BeanUtils;
import java.lang.reflect.InvocationTargetException;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import java.time.LocalDateTime;
import com.example.web.dto.*;
import lombok.EqualsAndHashCode;

/**
 * 数据导出申请表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("`tdataexportrequest`")
public class TDataExportRequest extends BaseEntity {

    /**
     * 关联用户表主键
     */
    @JsonProperty("UserId")
    @TableField(value="UserId",updateStrategy = FieldStrategy.ALWAYS)
    private Integer UserId;

    /**
     * 申请类型：Export=导出
     */
    @JsonProperty("RequestType")
    @TableField(value="RequestType",updateStrategy = FieldStrategy.ALWAYS)
    private String RequestType;

    /**
     * 数据类型（JSON数组）
     */
    @JsonProperty("DataType")
    @TableField(value="DataType",updateStrategy = FieldStrategy.ALWAYS)
    private String DataType;

    /**
     * 导出格式：JSON/Excel/PDF
     */
    @JsonProperty("ExportFormat")
    @TableField(value="ExportFormat",updateStrategy = FieldStrategy.ALWAYS)
    private String ExportFormat;

    /**
     * 申请说明
     */
    @JsonProperty("RequestDesc")
    @TableField(value="RequestDesc",updateStrategy = FieldStrategy.ALWAYS)
    private String RequestDesc;

    /**
     * 状态：Pending/Processing/Completed/Failed
     */
    @JsonProperty("Status")
    @TableField(value="Status",updateStrategy = FieldStrategy.ALWAYS)
    private String Status;

    /**
     * 导出文件URL
     */
    @JsonProperty("FileUrl")
    @TableField(value="FileUrl",updateStrategy = FieldStrategy.ALWAYS)
    private String FileUrl;

    /**
     * 文件大小（字节）
     */
    @JsonProperty("FileSize")
    @TableField(value="FileSize",updateStrategy = FieldStrategy.ALWAYS)
    private Long FileSize;

    /**
     * 下载链接过期时间
     */
    @JsonProperty("ExpireTime")
    @TableField(value="ExpireTime",updateStrategy = FieldStrategy.ALWAYS)
    private LocalDateTime ExpireTime;

    /**
     * 处理完成时间
     */
    @JsonProperty("ProcessTime")
    @TableField(value="ProcessTime",updateStrategy = FieldStrategy.ALWAYS)
    private LocalDateTime ProcessTime;

    /**
     * 错误信息（失败时）
     */
    @JsonProperty("ErrorMessage")
    @TableField(value="ErrorMessage",updateStrategy = FieldStrategy.ALWAYS)
    private String ErrorMessage;

    /**
     * 软删除标记
     */
    @JsonProperty("IsDelete")
    @TableField(value="IsDelete",updateStrategy = FieldStrategy.ALWAYS)
    private Boolean IsDelete;

    /**
     * 更新时间
     */
    @JsonProperty("UpdateTime")
    @TableField(value="UpdateTime",updateStrategy = FieldStrategy.ALWAYS)
    private LocalDateTime UpdateTime;

    /**
     * 把数据导出申请实体转换成数据导出申请传输模型
     */
    public TDataExportRequestDto MapToDto() throws InvocationTargetException, IllegalAccessException {
        TDataExportRequestDto dto = new TDataExportRequestDto();
        BeanUtils.copyProperties(dto, this);
        return dto;
    }
}
