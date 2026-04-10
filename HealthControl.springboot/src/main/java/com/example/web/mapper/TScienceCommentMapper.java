package com.example.web.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.web.entity.*;
import org.apache.ibatis.annotations.Mapper;

/**
 * 科普评论表对应的Mapper
 */
@Mapper
public interface TScienceCommentMapper  extends BaseMapper<TScienceComment> {

}
