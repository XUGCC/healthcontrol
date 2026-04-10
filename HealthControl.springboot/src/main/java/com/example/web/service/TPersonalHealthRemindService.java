package com.example.web.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.web.dto.*;
import com.example.web.dto.query.*;
import com.example.web.entity.*;
import com.example.web.tools.dto.*;
import com.example.web.enums.*;
import java.lang.reflect.InvocationTargetException;
import org.springframework.web.bind.annotation.RequestParam;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import jakarta.servlet.http.HttpServletResponse;
/**
 * 个性化健康提醒功能的Service接口的定义清单
 */
public interface TPersonalHealthRemindService extends IService<TPersonalHealthRemind> {

    /**
     * 个性化健康提醒的分页查询方法接口定义
     */
    public PagedResult<TPersonalHealthRemindDto> List(TPersonalHealthRemindPagedInput input) ;
    /**
     * 个性化健康提醒的新增或者修改方法接口定义
     */
    public TPersonalHealthRemindDto CreateOrEdit(TPersonalHealthRemindDto input);

     /**
     * 获取个性化健康提醒信息
     */
    public TPersonalHealthRemindDto Get(TPersonalHealthRemindPagedInput input);
 	 /**
     * 个性化健康提醒删除
     */
    public void Delete(IdInput input);

    /**
     * 个性化健康提醒批量删除
     */
    public void BatchDelete(IdsInput input);
  
	  /**
     * 个性化健康提醒导出
     */
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException;

    /**
     * 今天不再提醒（更新LastPushTime为当前时间）
     */
    public void SnoozeToday(Integer userId, Integer remindType);

}
