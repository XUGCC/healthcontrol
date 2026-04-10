package com.example.web.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.web.dto.*;
import com.example.web.dto.query.*;
import com.example.web.entity.*;
import com.example.web.mapper.*;
import com.example.web.service.*;
import com.example.web.tools.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import lombok.SneakyThrows;
import java.io.IOException;
import com.example.web.tools.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.springframework.web.bind.annotation.RequestParam;
/**
 * 健康科普功能实现类
 */
@Service
public class THealthScienceServiceImpl extends ServiceImpl<THealthScienceMapper, THealthScience> implements THealthScienceService {

	 /**
     * 操作数据库AppUser表mapper对象
     */
    @Autowired
    private AppUserMapper AppUserMapper;
    /**
     * 操作数据库的THealthScience表mapper对象
     */
    @Autowired
    private THealthScienceMapper THealthScienceMapper;
    @Autowired
    private THealthScienceCategoryMapper  THealthScienceCategoryMapper;                        

  
   /**
     * 构建表查询sql
     */
    private LambdaQueryWrapper<THealthScience> BuilderQuery(THealthSciencePagedInput input) {
       //声明一个支持健康科普查询的(拉姆达)表达式
        LambdaQueryWrapper<THealthScience> queryWrapper = Wrappers.<THealthScience>lambdaQuery()
                .eq(input.getId() != null && input.getId() != 0, THealthScience::getId, input.getId());
   //如果前端搜索传入查询条件则拼接查询条件
        if (Extension.isNotNullOrEmpty(input.getTitle())) {
             queryWrapper = queryWrapper.like(THealthScience::getTitle, input.getTitle());
       	 }

        if (input.getCreatorId() != null) {
            queryWrapper = queryWrapper.eq(THealthScience::getCreatorId, input.getCreatorId());
       	 }

        if (input.getCategoryId() != null) {
            queryWrapper = queryWrapper.eq(THealthScience::getCategoryId, input.getCategoryId());
       	 }
        if (input.getUpdateTimeRange() != null && !input.getUpdateTimeRange().isEmpty()) {
            queryWrapper = queryWrapper.le(THealthScience::getUpdateTime, input.getUpdateTimeRange().get(1));
            queryWrapper = queryWrapper.ge(THealthScience::getUpdateTime, input.getUpdateTimeRange().get(0));
        }
        if (input.getAuditStatus() != null) {
            queryWrapper = queryWrapper.eq(THealthScience::getAuditStatus, input.getAuditStatus());
        }

 
 
     if(Extension.isNotNullOrEmpty(input.getKeyWord()))
        {
			queryWrapper=queryWrapper.and(i->i
          	   .like(THealthScience::getTitle,input.getKeyWord()).or()   	 
        );
                                       
 		   }
    
      return queryWrapper;
    }
  
    /**
     * 处理健康科普对于的外键数据
     */
   private List<THealthScienceDto> DispatchItem(List<THealthScienceDto> items) throws InvocationTargetException, IllegalAccessException {
          
       for (THealthScienceDto item : items) {           
          	            
           //查询出关联的AppUser表信息           
            AppUser  CreatorEntity= AppUserMapper.selectById(item.getCreatorId());
            item.setCreatorDto(CreatorEntity!=null?CreatorEntity.MapToDto():new AppUserDto());              
           
          	            
           //查询出关联的THealthScienceCategory表信息           
            THealthScienceCategory  CategoryEntity= THealthScienceCategoryMapper.selectById(item.getCategoryId());
            item.setCategoryDto(CategoryEntity!=null?CategoryEntity.MapToDto():new THealthScienceCategoryDto());              
       }
       
     return items; 
   }
  
    /**
     * 健康科普分页查询
     */
    @SneakyThrows
    @Override
    public PagedResult<THealthScienceDto> List(THealthSciencePagedInput input) {
			//构建where条件+排序
        LambdaQueryWrapper<THealthScience> queryWrapper = BuilderQuery(input);
        // 动态排序处理
        if (input.getSortItem() != null) {
            // 根据字段名动态排序
            queryWrapper.last("ORDER BY " + input.getSortItem().getFieldName()
                    + (input.getSortItem().getIsAsc() ? " ASC" : " DESC"));
        } else {
            // 默认按创建时间从大到小排序
            queryWrapper = queryWrapper.orderByDesc(THealthScience::getCreationTime);
        }

        //构建一个分页查询的model
        Page<THealthScience> page = new Page<>(input.getPage(), input.getLimit());
         //从数据库进行分页查询获取健康科普数据
        IPage<THealthScience> pageRecords= THealthScienceMapper.selectPage(page, queryWrapper);
        //获取所有满足条件的数据行数
        Long totalCount= THealthScienceMapper.selectCount(queryWrapper);
        //把THealthScience实体转换成THealthScience传输模型
        List<THealthScienceDto> items= Extension.copyBeanList(pageRecords.getRecords(),THealthScienceDto.class);

		   DispatchItem(items);
        //返回一个分页结构给前端
        return PagedResult.GetInstance(items,totalCount);

    }
  
    /**
     * 单个健康科普查询
     */
    @SneakyThrows
    @Override
    public THealthScienceDto Get(THealthSciencePagedInput input) {
       if(input.getId()==null)
        {
         return new THealthScienceDto();
        }
      
       PagedResult<THealthScienceDto> pagedResult = List(input);
        return pagedResult.getItems().stream().findFirst().orElse(new THealthScienceDto()); 
    }

    /**
     *健康科普创建或者修改
     */
    @SneakyThrows
    @Override
    public THealthScienceDto CreateOrEdit(THealthScienceDto input) {
        //声明一个健康科普实体
        THealthScience THealthScience=input.MapToEntity();  
        //调用数据库的增加或者修改方法
        saveOrUpdate(THealthScience);
        //把传输模型返回给前端
        return THealthScience.MapToDto();
    }
    /**
     * 健康科普删除
     */
    @Override
    public void Delete(IdInput input) {
        THealthScience entity = THealthScienceMapper.selectById(input.getId());
        THealthScienceMapper.deleteById(entity);
    }

    /**
     * 健康科普批量删除
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
     * 健康科普导出
     */
    @Override
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        THealthSciencePagedInput input = mapper.readValue(query, THealthSciencePagedInput.class);
        List<THealthScienceDto> items =List(input).getItems();
        //声明一个工作簿
        HSSFWorkbook workbook = new HSSFWorkbook();
        //生成一个表格，设置表格名称为"健康科普"
        HSSFSheet sheet = workbook.createSheet("健康科普表");
        //设置表格列宽度为10个字节
        sheet.setDefaultColumnWidth(30);
        //创建标题的显示样式
        HSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.YELLOW.index);
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        //创建第一行表头
        HSSFRow headrow = sheet.createRow(0);
        //表头数据
        String[] header = {"名称" ,"分类名称：如声带保护/疾病科普" ,"科普标题" ,"封面图URL" ,"阅读量" ,"知识类型" ,"展示状态" ,"软删除标记" ,};   
        //遍历添加表头(下面模拟遍历健康科普，也是同样的操作过程)
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
            THealthScienceDto dto = items.get(i);
            //创建一行
            HSSFRow row = sheet.createRow(i + 1);    
   
              if (dto.getCreatorDto()!=null&&Extension.isNotNullOrEmpty(dto.getCreatorDto().getName())) { 
                row.createCell(0).setCellValue(new HSSFRichTextString(dto.getCreatorDto().getName()));
              }
              if (dto.getCategoryDto()!=null&&Extension.isNotNullOrEmpty(dto.getCategoryDto().getCategoryName())) { 
                row.createCell(1).setCellValue(new HSSFRichTextString(dto.getCategoryDto().getCategoryName()));
              }
              if (Extension.isNotNullOrEmpty(dto.getTitle())) { 
                row.createCell(2).setCellValue(new HSSFRichTextString(dto.getTitle()));
              }
              if (Extension.isNotNullOrEmpty(dto.getCoverUrl())) { 
                row.createCell(3).setCellValue(new HSSFRichTextString(dto.getCoverUrl()));
              }
              if (dto.getReadCount() != null) { 
                row.createCell(4).setCellValue(new HSSFRichTextString(dto.getReadCount()+""));
              }
              if (dto.getKnowledgeType() != null) { 
                row.createCell(5).setCellValue(new HSSFRichTextString(dto.getKnowledgeType()+""));
              }
              if (dto.getShowStatus() != null) { 
                row.createCell(6).setCellValue(new HSSFRichTextString(dto.getShowStatus()+""));
              }
              if (dto.getIsDelete() != null) { 
                row.createCell(7).setCellValue(new HSSFRichTextString(dto.getIsDelete()+""));
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
