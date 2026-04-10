package com.example.web.dto;
import com.example.web.tools.dto.BaseDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;
import org.apache.commons.beanutils.BeanUtils;
import java.lang.reflect.InvocationTargetException;
import com.example.web.entity.*;
import lombok.EqualsAndHashCode;

/**
 * 数据删除申请类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TDataDeleteRequestDto extends BaseDto {

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
     * 数据类型（JSON数组）
     */
    @JsonProperty("DataType")
    private String DataType;

    /**
     * 删除原因
     */
    @JsonProperty("DeleteReason")
    private String DeleteReason;

    /**
     * 删除原因说明
     */
    @JsonProperty("DeleteReasonDesc")
    private String DeleteReasonDesc;

    /**
     * 状态：Pending/Processing/Completed/Failed
     */
    @JsonProperty("Status")
    private String Status;

    /**
     * 删除记录数（完成后统计）
     */
    @JsonProperty("DeleteCount")
    private Integer DeleteCount;

    /**
     * 处理完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("ProcessTime")
    private LocalDateTime ProcessTime;

    /**
     * 错误信息（失败时）
     */
    @JsonProperty("ErrorMessage")
    private String ErrorMessage;

    /**
     * 软删除标记
     */
    @JsonProperty("IsDelete")
    private Boolean IsDelete;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("UpdateTime")
    private LocalDateTime UpdateTime;

    /**
     * 关联用户信息
     */
    @JsonProperty("UserDto")
    private AppUserDto UserDto;

    /**
     * 把数据删除申请传输模型转换成数据删除申请实体
     */
    public TDataDeleteRequest MapToEntity() throws InvocationTargetException, IllegalAccessException {
        TDataDeleteRequest entity = new TDataDeleteRequest();
        BeanUtils.copyProperties(entity, this);
        return entity;
    }
}
