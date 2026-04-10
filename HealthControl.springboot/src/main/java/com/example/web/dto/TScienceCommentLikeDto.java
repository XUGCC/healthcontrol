package com.example.web.dto;
import com.example.web.enums.*;
import com.example.web.tools.dto.BaseDto;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.sql.Date;
import java.sql.Timestamp;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.example.web.entity.*;
import org.apache.commons.beanutils.BeanUtils;
import java.lang.reflect.InvocationTargetException;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
/**
 * 科普评论点赞类
 */
@Data
public class TScienceCommentLikeDto extends BaseDto
{

    
     
    /**
     * 取消点赞时间
     */ 
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("UpdateTime")
    private LocalDateTime UpdateTime;             
    
     
    /**
     * 关联用户表主键t_user.
     */ 
    @JsonProperty("UserId")
    private Integer UserId;          
    
     
    /**
     * 关联评论表主键
     */ 
    @JsonProperty("CommentId")
    private Integer CommentId;          
    
     
    /**
     * 点赞操作时间
     */ 
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("LikeTime")
    private LocalDateTime LikeTime;             
    
     
    /**
     * 取消点赞操作时间
     */ 
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("CancelLikeTime")
    private LocalDateTime CancelLikeTime;             
    
     
    /**
     * 软删除标记
     */ 
    @JsonProperty("IsDelete")
    private Boolean IsDelete;          

     @JsonProperty("CommentDto") 
    private TScienceCommentDto CommentDto;                        
   
     @JsonProperty("UserDto") 
    private AppUserDto UserDto;                        
   
 	 /**
     * 把科普评论点赞传输模型转换成科普评论点赞实体
     */
    public TScienceCommentLike MapToEntity() throws InvocationTargetException, IllegalAccessException {
        TScienceCommentLike TScienceCommentLike= new TScienceCommentLike();
     
         BeanUtils.copyProperties(TScienceCommentLike,this);
        
        return TScienceCommentLike;
    }

}
