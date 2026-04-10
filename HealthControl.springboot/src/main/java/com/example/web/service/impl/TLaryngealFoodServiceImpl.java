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
/**
 * 喉部食物库功能实现类
 */
@Service
public class TLaryngealFoodServiceImpl extends ServiceImpl<TLaryngealFoodMapper, TLaryngealFood> implements TLaryngealFoodService {

	 /**
     * 操作数据库AppUser表mapper对象
     */
    @Autowired
    private AppUserMapper AppUserMapper;
    /**
     * 操作数据库的TLaryngealFood表mapper对象
     */
    @Autowired
    private TLaryngealFoodMapper TLaryngealFoodMapper;
    @Autowired
    private TLaryngealFoodCategoryMapper  TLaryngealFoodCategoryMapper;                        

  
   /**
     * 构建表查询sql
     */
    private LambdaQueryWrapper<TLaryngealFood> BuilderQuery(TLaryngealFoodPagedInput input) {
       //声明一个支持喉部食物库查询的(拉姆达)表达式
        LambdaQueryWrapper<TLaryngealFood> queryWrapper = Wrappers.<TLaryngealFood>lambdaQuery()
                .eq(input.getId() != null && input.getId() != 0, TLaryngealFood::getId, input.getId());
   //如果前端搜索传入查询条件则拼接查询条件
        if (Extension.isNotNullOrEmpty(input.getFoodName())) {
             queryWrapper = queryWrapper.like(TLaryngealFood::getFoodName, input.getFoodName());
       	 }
        if (Extension.isNotNullOrEmpty(input.getFoodAlias())) {
             queryWrapper = queryWrapper.like(TLaryngealFood::getFoodAlias, input.getFoodAlias());
       	 }
        if (Extension.isNotNullOrEmpty(input.getEffectHarmDesc())) {
             queryWrapper = queryWrapper.like(TLaryngealFood::getEffectHarmDesc, input.getEffectHarmDesc());
       	 }
        if (Extension.isNotNullOrEmpty(input.getSuggestContent())) {
             queryWrapper = queryWrapper.like(TLaryngealFood::getSuggestContent, input.getSuggestContent());
       	 }

        if (input.getCreatorId() != null) {
            queryWrapper = queryWrapper.eq(TLaryngealFood::getCreatorId, input.getCreatorId());
       	 }

        if (input.getCategoryId() != null) {
            queryWrapper = queryWrapper.eq(TLaryngealFood::getCategoryId, input.getCategoryId());
       	 }
        if (input.getUpdateTimeRange() != null && !input.getUpdateTimeRange().isEmpty()) {
            queryWrapper = queryWrapper.le(TLaryngealFood::getUpdateTime, input.getUpdateTimeRange().get(1));
            queryWrapper = queryWrapper.ge(TLaryngealFood::getUpdateTime, input.getUpdateTimeRange().get(0));
        }
      

 
 
     if(Extension.isNotNullOrEmpty(input.getKeyWord()))
        {
			queryWrapper=queryWrapper.and(i->i
          	   .like(TLaryngealFood::getFoodName,input.getKeyWord()).or()   	 
          	   .like(TLaryngealFood::getFoodAlias,input.getKeyWord()).or()   	 
          	   .like(TLaryngealFood::getEffectHarmDesc,input.getKeyWord()).or()   	 
          	   .like(TLaryngealFood::getSuggestContent,input.getKeyWord()).or()   	 
        );
                                       
 		   }
    
      return queryWrapper;
    }
  
    /**
     * 处理喉部食物库对于的外键数据
     */
   private List<TLaryngealFoodDto> DispatchItem(List<TLaryngealFoodDto> items) throws InvocationTargetException, IllegalAccessException {
          
       for (TLaryngealFoodDto item : items) {           
          	            
           //查询出关联的TLaryngealFoodCategory表信息           
            TLaryngealFoodCategory  CategoryEntity= TLaryngealFoodCategoryMapper.selectById(item.getCategoryId());
            item.setCategoryDto(CategoryEntity!=null?CategoryEntity.MapToDto():new TLaryngealFoodCategoryDto());              
           
          	            
           //查询出关联的AppUser表信息           
            AppUser  CreatorEntity= AppUserMapper.selectById(item.getCreatorId());
            item.setCreatorDto(CreatorEntity!=null?CreatorEntity.MapToDto():new AppUserDto());              
       }
       
     return items; 
   }
  
    /**
     * 喉部食物库分页查询
     */
    @SneakyThrows
    @Override
    public PagedResult<TLaryngealFoodDto> List(TLaryngealFoodPagedInput input) {
			//构建where条件+排序
        LambdaQueryWrapper<TLaryngealFood> queryWrapper = BuilderQuery(input);
        // 动态排序处理
        if (input.getSortItem() != null) {
            // 根据字段名动态排序
            queryWrapper.last("ORDER BY " + input.getSortItem().getFieldName()
                    + (input.getSortItem().getIsAsc() ? " ASC" : " DESC"));
        } else {
            // 默认按创建时间从大到小排序
            queryWrapper = queryWrapper.orderByDesc(TLaryngealFood::getCreationTime);
        }

        //构建一个分页查询的model
        Page<TLaryngealFood> page = new Page<>(input.getPage(), input.getLimit());
         //从数据库进行分页查询获取喉部食物库数据
        IPage<TLaryngealFood> pageRecords= TLaryngealFoodMapper.selectPage(page, queryWrapper);
        //获取所有满足条件的数据行数
        Long totalCount= TLaryngealFoodMapper.selectCount(queryWrapper);
        //把TLaryngealFood实体转换成TLaryngealFood传输模型
        List<TLaryngealFoodDto> items= Extension.copyBeanList(pageRecords.getRecords(),TLaryngealFoodDto.class);

		   DispatchItem(items);
        //返回一个分页结构给前端
        return PagedResult.GetInstance(items,totalCount);

    }
  
    /**
     * 单个喉部食物库查询
     */
    @SneakyThrows
    @Override
    public TLaryngealFoodDto Get(TLaryngealFoodPagedInput input) {
       if(input.getId()==null)
        {
         return new TLaryngealFoodDto();
        }
      
       PagedResult<TLaryngealFoodDto> pagedResult = List(input);
        return pagedResult.getItems().stream().findFirst().orElse(new TLaryngealFoodDto()); 
    }

    /**
     *喉部食物库创建或者修改
     */
    @SneakyThrows
    @Override
    public TLaryngealFoodDto CreateOrEdit(TLaryngealFoodDto input) {
        //声明一个喉部食物库实体
        TLaryngealFood TLaryngealFood=input.MapToEntity();  
        //调用数据库的增加或者修改方法
        saveOrUpdate(TLaryngealFood);
        //把传输模型返回给前端
        return TLaryngealFood.MapToDto();
    }
    /**
     * 喉部食物库删除
     */
    @Override
    public void Delete(IdInput input) {
        TLaryngealFood entity = TLaryngealFoodMapper.selectById(input.getId());
        TLaryngealFoodMapper.deleteById(entity);
    }

    /**
     * 喉部食物库批量删除
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
     * 喉部食物库导出
     */
    @Override
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        TLaryngealFoodPagedInput input = mapper.readValue(query, TLaryngealFoodPagedInput.class);
        List<TLaryngealFoodDto> items =List(input).getItems();
        //声明一个工作簿
        HSSFWorkbook workbook = new HSSFWorkbook();
        //生成一个表格，设置表格名称为"喉部食物库"
        HSSFSheet sheet = workbook.createSheet("喉部食物库表");
        //设置表格列宽度为10个字节
        sheet.setDefaultColumnWidth(30);
        //创建标题的显示样式
        HSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.YELLOW.index);
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        //创建第一行表头
        HSSFRow headrow = sheet.createRow(0);
        //表头数据
        String[] header = {"名称" ,"分类名称：如润喉类/辛辣类" ,"食物名称" ,"食物别名" ,"功效危害说明" ,"忌口建议" ,"食物配图URL" ,"展示状态" ,"软删除标记" ,};   
        //遍历添加表头(下面模拟遍历喉部食物库，也是同样的操作过程)
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
            TLaryngealFoodDto dto = items.get(i);
            //创建一行
            HSSFRow row = sheet.createRow(i + 1);    
   
              if (dto.getCreatorDto()!=null&&Extension.isNotNullOrEmpty(dto.getCreatorDto().getName())) { 
                row.createCell(0).setCellValue(new HSSFRichTextString(dto.getCreatorDto().getName()));
              }
              if (dto.getCategoryDto()!=null&&Extension.isNotNullOrEmpty(dto.getCategoryDto().getCategoryName())) { 
                row.createCell(1).setCellValue(new HSSFRichTextString(dto.getCategoryDto().getCategoryName()));
              }
              if (Extension.isNotNullOrEmpty(dto.getFoodName())) { 
                row.createCell(2).setCellValue(new HSSFRichTextString(dto.getFoodName()));
              }
              if (Extension.isNotNullOrEmpty(dto.getFoodAlias())) { 
                row.createCell(3).setCellValue(new HSSFRichTextString(dto.getFoodAlias()));
              }
              if (Extension.isNotNullOrEmpty(dto.getEffectHarmDesc())) { 
                row.createCell(4).setCellValue(new HSSFRichTextString(dto.getEffectHarmDesc()));
              }
              if (Extension.isNotNullOrEmpty(dto.getSuggestContent())) { 
                row.createCell(5).setCellValue(new HSSFRichTextString(dto.getSuggestContent()));
              }
              if (Extension.isNotNullOrEmpty(dto.getPicUrl())) { 
                row.createCell(6).setCellValue(new HSSFRichTextString(dto.getPicUrl()));
              }
              if (dto.getShowStatus() != null) { 
                row.createCell(7).setCellValue(new HSSFRichTextString(dto.getShowStatus()+""));
              }
              if (dto.getIsDelete() != null) { 
                row.createCell(8).setCellValue(new HSSFRichTextString(dto.getIsDelete()+""));
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
}
