package com.example.web.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.web.entity.*;
import org.apache.ibatis.annotations.Mapper;

/**
 * 科普评论点赞表对应的Mapper
 */
@Mapper
public interface TScienceCommentLikeMapper  extends BaseMapper<TScienceCommentLike> {

}
