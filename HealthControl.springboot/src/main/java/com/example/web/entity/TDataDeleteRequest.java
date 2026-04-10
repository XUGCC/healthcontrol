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
 * 数据删除申请表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("`tdatadeleterequest`")
public class TDataDeleteRequest extends BaseEntity {

    /**
     * 关联用户表主键
     */
    @JsonProperty("UserId")
    @TableField(value="UserId",updateStrategy = FieldStrategy.ALWAYS)
    private Integer UserId;

    /**
     * 申请类型：Delete=删除
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
     * 删除原因
     */
    @JsonProperty("DeleteReason")
    @TableField(value="DeleteReason",updateStrategy = FieldStrategy.ALWAYS)
    private String DeleteReason;

    /**
     * 删除原因说明
     */
    @JsonProperty("DeleteReasonDesc")
    @TableField(value="DeleteReasonDesc",updateStrategy = FieldStrategy.ALWAYS)
    private String DeleteReasonDesc;

    /**
     * 状态：Pending/Processing/Completed/Failed
     */
    @JsonProperty("Status")
    @TableField(value="Status",updateStrategy = FieldStrategy.ALWAYS)
    private String Status;

    /**
     * 删除记录数（完成后统计）
     */
    @JsonProperty("DeleteCount")
    @TableField(value="DeleteCount",updateStrategy = FieldStrategy.ALWAYS)
    private Integer DeleteCount;

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
     * 把数据删除申请实体转换成数据删除申请传输模型
     */
    public TDataDeleteRequestDto MapToDto() throws InvocationTargetException, IllegalAccessException {
        TDataDeleteRequestDto dto = new TDataDeleteRequestDto();
        BeanUtils.copyProperties(dto, this);
        return dto;
    }
}
