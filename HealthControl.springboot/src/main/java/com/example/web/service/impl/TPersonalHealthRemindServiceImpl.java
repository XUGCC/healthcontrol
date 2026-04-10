package com.example.web.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.web.SysConst;
import com.example.web.dto.*;
import com.example.web.dto.query.*;
import com.example.web.entity.*;
import com.example.web.mapper.*;
import com.example.web.enums.*;
import com.example.web.service.*;
import com.example.web.tools.dto.*;
import com.example.web.tools.exception.CustomException;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.lang.reflect.InvocationTargetException;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import lombok.SneakyThrows;
import java.io.IOException;
import com.example.web.tools.*;
import java.text.DecimalFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDateTime;
/**
 * 个性化健康提醒功能实现类
 */
@Service
public class TPersonalHealthRemindServiceImpl extends ServiceImpl<TPersonalHealthRemindMapper, TPersonalHealthRemind> implements TPersonalHealthRemindService {

	 /**
     * 操作数据库AppUser表mapper对象
     */
    @Autowired
    private AppUserMapper AppUserMapper;
    /**
     * 操作数据库的TPersonalHealthRemind表mapper对象
     */
    @Autowired
    private TPersonalHealthRemindMapper TPersonalHealthRemindMapper;

  
   /**
     * 构建表查询sql
     */
    private LambdaQueryWrapper<TPersonalHealthRemind> BuilderQuery(TPersonalHealthRemindPagedInput input) {
       //声明一个支持个性化健康提醒查询的(拉姆达)表达式
        LambdaQueryWrapper<TPersonalHealthRemind> queryWrapper = Wrappers.<TPersonalHealthRemind>lambdaQuery()
                .eq(input.getId() != null && input.getId() != 0, TPersonalHealthRemind::getId, input.getId());
   //如果前端搜索传入查询条件则拼接查询条件
        if (Extension.isNotNullOrEmpty(input.getRemindContent())) {
             queryWrapper = queryWrapper.like(TPersonalHealthRemind::getRemindContent, input.getRemindContent());
       	 }

        if (input.getUserId() != null) {
            queryWrapper = queryWrapper.eq(TPersonalHealthRemind::getUserId, input.getUserId());
       	 }
        if (input.getUpdateTimeRange() != null && !input.getUpdateTimeRange().isEmpty()) {
            queryWrapper = queryWrapper.le(TPersonalHealthRemind::getUpdateTime, input.getUpdateTimeRange().get(1));
            queryWrapper = queryWrapper.ge(TPersonalHealthRemind::getUpdateTime, input.getUpdateTimeRange().get(0));
        }
        if (input.getRemindTimeRange() != null && !input.getRemindTimeRange().isEmpty()) {
            queryWrapper = queryWrapper.le(TPersonalHealthRemind::getRemindTime, input.getRemindTimeRange().get(1));
            queryWrapper = queryWrapper.ge(TPersonalHealthRemind::getRemindTime, input.getRemindTimeRange().get(0));
        }
        if (input.getLastPushTimeRange() != null && !input.getLastPushTimeRange().isEmpty()) {
            queryWrapper = queryWrapper.le(TPersonalHealthRemind::getLastPushTime, input.getLastPushTimeRange().get(1));
            queryWrapper = queryWrapper.ge(TPersonalHealthRemind::getLastPushTime, input.getLastPushTimeRange().get(0));
        }
        if (input.getRemindType() != null) {
            queryWrapper = queryWrapper.eq(TPersonalHealthRemind::getRemindType, input.getRemindType());
       	 }
        if (input.getRepeatFrequency() != null) {
            queryWrapper = queryWrapper.eq(TPersonalHealthRemind::getRepeatFrequency, input.getRepeatFrequency());
       	 }
        if (input.getRemindStatus() != null) {
            queryWrapper = queryWrapper.eq(TPersonalHealthRemind::getRemindStatus, input.getRemindStatus());
       	 }
        if (input.getIsDelete() != null) {
            queryWrapper = queryWrapper.eq(TPersonalHealthRemind::getIsDelete, input.getIsDelete());
       	 }
      

 
 
     if(Extension.isNotNullOrEmpty(input.getKeyWord()))
        {
			queryWrapper=queryWrapper.and(i->i
          	   .like(TPersonalHealthRemind::getRemindContent,input.getKeyWord()).or()   	 
        );
                                       
 		   }
    
      return queryWrapper;
    }
  
    /**
     * 处理个性化健康提醒对于的外键数据
     */
   private List<TPersonalHealthRemindDto> DispatchItem(List<TPersonalHealthRemindDto> items) throws InvocationTargetException, IllegalAccessException {
          
       for (TPersonalHealthRemindDto item : items) {           
          	            
           //查询出关联的AppUser表信息           
            AppUser  UserEntity= AppUserMapper.selectById(item.getUserId());
            item.setUserDto(UserEntity!=null?UserEntity.MapToDto():new AppUserDto());              
       }
       
     return items; 
   }
  
    /**
     * 个性化健康提醒分页查询
     */
    @SneakyThrows
    @Override
    public PagedResult<TPersonalHealthRemindDto> List(TPersonalHealthRemindPagedInput input) {
			//构建where条件+排序
        LambdaQueryWrapper<TPersonalHealthRemind> queryWrapper = BuilderQuery(input);
        // 动态排序处理
        if (input.getSortItem() != null) {
            // 根据字段名动态排序
            queryWrapper.last("ORDER BY " + input.getSortItem().getFieldName()
                    + (input.getSortItem().getIsAsc() ? " ASC" : " DESC"));
        } else {
            // 默认按创建时间从大到小排序
            queryWrapper = queryWrapper.orderByDesc(TPersonalHealthRemind::getCreationTime);
        }

        //构建一个分页查询的model
        Page<TPersonalHealthRemind> page = new Page<>(input.getPage(), input.getLimit());
         //从数据库进行分页查询获取个性化健康提醒数据
        IPage<TPersonalHealthRemind> pageRecords= TPersonalHealthRemindMapper.selectPage(page, queryWrapper);
        //获取所有满足条件的数据行数
        Long totalCount= TPersonalHealthRemindMapper.selectCount(queryWrapper);
        //把TPersonalHealthRemind实体转换成TPersonalHealthRemind传输模型
        List<TPersonalHealthRemindDto> items= Extension.copyBeanList(pageRecords.getRecords(),TPersonalHealthRemindDto.class);

		   DispatchItem(items);
        //返回一个分页结构给前端
        return PagedResult.GetInstance(items,totalCount);

    }
  
    /**
     * 单个个性化健康提醒查询
     */
    @SneakyThrows
    @Override
    public TPersonalHealthRemindDto Get(TPersonalHealthRemindPagedInput input) {
       if(input.getId()==null)
        {
         return new TPersonalHealthRemindDto();
        }
      
       PagedResult<TPersonalHealthRemindDto> pagedResult = List(input);
        return pagedResult.getItems().stream().findFirst().orElse(new TPersonalHealthRemindDto()); 
    }

    /**
     *个性化健康提醒创建或者修改
     */
    @SneakyThrows
    @Override
    public TPersonalHealthRemindDto CreateOrEdit(TPersonalHealthRemindDto input) {
        //声明一个个性化健康提醒实体
        TPersonalHealthRemind TPersonalHealthRemind=input.MapToEntity();
        // 默认未删除
        if (TPersonalHealthRemind.getIsDelete() == null) {
            TPersonalHealthRemind.setIsDelete(0);
        }
        //调用数据库的增加或者修改方法
        saveOrUpdate(TPersonalHealthRemind);
        //把传输模型返回给前端
        return TPersonalHealthRemind.MapToDto();
    }
    /**
     * 个性化健康提醒删除
     */
    @Override
    public void Delete(IdInput input) {
        TPersonalHealthRemind entity = TPersonalHealthRemindMapper.selectById(input.getId());
        TPersonalHealthRemindMapper.deleteById(entity);
    }

    /**
     * 个性化健康提醒批量删除
     */
    @Override
    public void BatchDelete(IdsInput input) {
        for (Integer id : input.getIds()) {
            IdInput idInput = new IdInput();
            idInput.setId(id);
            Delete(idInput);
        }
    }
    /**
     * 个性化健康提醒导出
     */
    @Override
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        TPersonalHealthRemindPagedInput input = mapper.readValue(query, TPersonalHealthRemindPagedInput.class);
        List<TPersonalHealthRemindDto> items =List(input).getItems();
        //声明一个工作簿
        HSSFWorkbook workbook = new HSSFWorkbook();
        //生成一个表格，设置表格名称为"个性化健康提醒"
        HSSFSheet sheet = workbook.createSheet("个性化健康提醒表");
        //设置表格列宽度为10个字节
        sheet.setDefaultColumnWidth(30);
        //创建标题的显示样式
        HSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.YELLOW.index);
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        //创建第一行表头
        HSSFRow headrow = sheet.createRow(0);
        //表头数据
        String[] header = {"名称" ,"提醒类型：0=定期自查，1=护喉饮食，2=发音训练，3=饮水" ,"提醒时间：如08:00" ,"重复频率：0=每天，1=每周一至五，2=每周一次" ,"提醒状态：0=关闭，1=开启" ,"提醒内容" ,"软删除标记：0=未删除，1=已删除" ,};   
        //遍历添加表头(下面模拟遍历个性化健康提醒，也是同样的操作过程)
         for (int i = 0; i < header.length; i++) {
            //创建一个单元格
            HSSFCell cell = headrow.createCell(i);
            //创建一个内容对象
            HSSFRichTextString text = new HSSFRichTextString(header[i]);
            //将内容对象的文字内容写入到单元格中
            cell.setCellValue(text);
            cell.setCellStyle(headerStyle);
        }
        for(int i=0;i<items.size();i++){
            TPersonalHealthRemindDto dto = items.get(i);
            //创建一行
            HSSFRow row = sheet.createRow(i + 1);    
   
              if (dto.getUserDto()!=null&&Extension.isNotNullOrEmpty(dto.getUserDto().getName())) { 
                row.createCell(0).setCellValue(new HSSFRichTextString(dto.getUserDto().getName()));
              }
              if (dto.getRemindType() != null) { 
                String v = dto.getRemindType() != null && dto.getRemindType() != 0 ? "是" : "否";
                row.createCell(1).setCellValue(new HSSFRichTextString(v));
              }
              if (Extension.isNotNullOrEmpty(dto.getRemindTime())) { 
                row.createCell(2).setCellValue(new HSSFRichTextString(dto.getRemindTime()));
              }
              if (dto.getRepeatFrequency() != null) { 
                String v = dto.getRepeatFrequency() != null && dto.getRepeatFrequency() != 0 ? "是" : "否";
                row.createCell(3).setCellValue(new HSSFRichTextString(v));
              }
              if (dto.getRemindStatus() != null) { 
                String v = dto.getRemindStatus() != null && dto.getRemindStatus() != 0 ? "是" : "否";
                row.createCell(4).setCellValue(new HSSFRichTextString(v));
              }
              if (Extension.isNotNullOrEmpty(dto.getRemindContent())) { 
                row.createCell(5).setCellValue(new HSSFRichTextString(dto.getRemindContent()));
              }
              if (dto.getIsDelete() != null) { 
                String v = dto.getIsDelete() != null && dto.getIsDelete() != 0 ? "是" : "否";
                row.createCell(6).setCellValue(new HSSFRichTextString(v));
              }
}
        //准备将Excel的输出流通过response输出到页面下载
        //八进制输出流
        response.setContentType("application/octet-stream");
        //这后面可以设置导出Excel的名称
        response.setHeader("Content-disposition", "attachment;filename=" + System.currentTimeMillis() + ".xls");
        //刷新缓冲
        response.flushBuffer();
        //workbook将Excel写入到response的输出流中，供页面下载
        try {
        workbook.write(response.getOutputStream());
        } finally {
            workbook.close();
        }
    }

    /**
     * 今天不再提醒（更新LastPushTime为当前时间）
     */
    @Override
    public void SnoozeToday(Integer userId, Integer remindType) {
        if (userId == null || remindType == null) {
            return;
        }
        LambdaQueryWrapper<TPersonalHealthRemind> queryWrapper = Wrappers
                .<TPersonalHealthRemind>lambdaQuery()
                .eq(TPersonalHealthRemind::getUserId, userId)
                .eq(TPersonalHealthRemind::getRemindType, remindType)
                .eq(TPersonalHealthRemind::getIsDelete, 0);
        
        TPersonalHealthRemind remind = TPersonalHealthRemindMapper.selectOne(queryWrapper);
        if (remind != null) {
            remind.setLastPushTime(LocalDateTime.now());
            TPersonalHealthRemindMapper.updateById(remind);
        }
    }
}
