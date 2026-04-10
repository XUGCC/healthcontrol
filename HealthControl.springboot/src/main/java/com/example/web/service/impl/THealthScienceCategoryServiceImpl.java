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
 * 健康科普分类功能实现类
 */
@Service
public class THealthScienceCategoryServiceImpl extends ServiceImpl<THealthScienceCategoryMapper, THealthScienceCategory> implements THealthScienceCategoryService {

	 /**
     * 操作数据库AppUser表mapper对象
     */
    @Autowired
    private AppUserMapper AppUserMapper;
    /**
     * 操作数据库的THealthScienceCategory表mapper对象
     */
    @Autowired
    private THealthScienceCategoryMapper THealthScienceCategoryMapper;

  
   /**
     * 构建表查询sql
     */
    private LambdaQueryWrapper<THealthScienceCategory> BuilderQuery(THealthScienceCategoryPagedInput input) {
       //声明一个支持健康科普分类查询的(拉姆达)表达式
        LambdaQueryWrapper<THealthScienceCategory> queryWrapper = Wrappers.<THealthScienceCategory>lambdaQuery()
                .eq(input.getId() != null && input.getId() != 0, THealthScienceCategory::getId, input.getId());
   //如果前端搜索传入查询条件则拼接查询条件
        if (Extension.isNotNullOrEmpty(input.getCategoryName())) {
             queryWrapper = queryWrapper.like(THealthScienceCategory::getCategoryName, input.getCategoryName());
       	 }
        if (Extension.isNotNullOrEmpty(input.getCategoryDesc())) {
             queryWrapper = queryWrapper.like(THealthScienceCategory::getCategoryDesc, input.getCategoryDesc());
       	 }
        if (input.getShowStatus() != null) {
            queryWrapper = queryWrapper.eq(THealthScienceCategory::getShowStatus, input.getShowStatus());
       	 }
        if (input.getIsDelete() != null) {
            queryWrapper = queryWrapper.eq(THealthScienceCategory::getIsDelete, input.getIsDelete());
       	 }
      

 
 
     if(Extension.isNotNullOrEmpty(input.getKeyWord()))
        {
			queryWrapper=queryWrapper.and(i->i
          	   .like(THealthScienceCategory::getCategoryName,input.getKeyWord()).or()   	 
          	   .like(THealthScienceCategory::getCategoryDesc,input.getKeyWord()).or()   	 
        );
                                       
 		   }
    
      return queryWrapper;
    }
  
    /**
     * 处理健康科普分类对于的外键数据
     */
   private List<THealthScienceCategoryDto> DispatchItem(List<THealthScienceCategoryDto> items) throws InvocationTargetException, IllegalAccessException {
          
       for (THealthScienceCategoryDto item : items) {       }
       
     return items; 
   }
  
    /**
     * 健康科普分类分页查询
     */
    @SneakyThrows
    @Override
    public PagedResult<THealthScienceCategoryDto> List(THealthScienceCategoryPagedInput input) {
			//构建where条件+排序
        LambdaQueryWrapper<THealthScienceCategory> queryWrapper = BuilderQuery(input);
        // 动态排序处理
        if (input.getSortItem() != null) {
            // 根据字段名动态排序
            queryWrapper.last("ORDER BY " + input.getSortItem().getFieldName()
                    + (input.getSortItem().getIsAsc() ? " ASC" : " DESC"));
        } else {
            // 默认按创建时间从大到小排序
            queryWrapper = queryWrapper.orderByDesc(THealthScienceCategory::getCreationTime);
        }

        //构建一个分页查询的model
        Page<THealthScienceCategory> page = new Page<>(input.getPage(), input.getLimit());
         //从数据库进行分页查询获取健康科普分类数据
        IPage<THealthScienceCategory> pageRecords= THealthScienceCategoryMapper.selectPage(page, queryWrapper);
        //获取所有满足条件的数据行数
        Long totalCount= THealthScienceCategoryMapper.selectCount(queryWrapper);
        //把THealthScienceCategory实体转换成THealthScienceCategory传输模型
        List<THealthScienceCategoryDto> items= Extension.copyBeanList(pageRecords.getRecords(),THealthScienceCategoryDto.class);

		   DispatchItem(items);
        //返回一个分页结构给前端
        return PagedResult.GetInstance(items,totalCount);

    }
  
    /**
     * 单个健康科普分类查询
     */
    @SneakyThrows
    @Override
    public THealthScienceCategoryDto Get(THealthScienceCategoryPagedInput input) {
       if(input.getId()==null)
        {
         return new THealthScienceCategoryDto();
        }
      
       PagedResult<THealthScienceCategoryDto> pagedResult = List(input);
        return pagedResult.getItems().stream().findFirst().orElse(new THealthScienceCategoryDto()); 
    }

    /**
     *健康科普分类创建或者修改
     */
    @SneakyThrows
    @Override
    public THealthScienceCategoryDto CreateOrEdit(THealthScienceCategoryDto input) {
        //声明一个健康科普分类实体
        THealthScienceCategory THealthScienceCategory=input.MapToEntity();  
        //调用数据库的增加或者修改方法
        saveOrUpdate(THealthScienceCategory);
        //把传输模型返回给前端
        return THealthScienceCategory.MapToDto();
    }
    /**
     * 健康科普分类删除
     */
    @Override
    public void Delete(IdInput input) {
        THealthScienceCategory entity = THealthScienceCategoryMapper.selectById(input.getId());
        THealthScienceCategoryMapper.deleteById(entity);
    }

    /**
     * 健康科普分类批量删除
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
     * 健康科普分类导出
     */
    @Override
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        THealthScienceCategoryPagedInput input = mapper.readValue(query, THealthScienceCategoryPagedInput.class);
        List<THealthScienceCategoryDto> items =List(input).getItems();
        //声明一个工作簿
        HSSFWorkbook workbook = new HSSFWorkbook();
        //生成一个表格，设置表格名称为"健康科普分类"
        HSSFSheet sheet = workbook.createSheet("健康科普分类表");
        //设置表格列宽度为10个字节
        sheet.setDefaultColumnWidth(30);
        //创建标题的显示样式
        HSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.YELLOW.index);
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        //创建第一行表头
        HSSFRow headrow = sheet.createRow(0);
        //表头数据
        String[] header = {"分类名称：如声带保护/疾病科普" ,"分类描述" ,"排序值：升序，值越小越靠前" ,"展示状态：0=隐藏，1=展示" ,"软删除标记：0=未删除，1=已删除" ,};   
        //遍历添加表头(下面模拟遍历健康科普分类，也是同样的操作过程)
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
            THealthScienceCategoryDto dto = items.get(i);
            //创建一行
            HSSFRow row = sheet.createRow(i + 1);    
   
              if (Extension.isNotNullOrEmpty(dto.getCategoryName())) { 
                row.createCell(0).setCellValue(new HSSFRichTextString(dto.getCategoryName()));
              }
              if (Extension.isNotNullOrEmpty(dto.getCategoryDesc())) { 
                row.createCell(1).setCellValue(new HSSFRichTextString(dto.getCategoryDesc()));
              }
              if (dto.getSortNum() != null) { 
                row.createCell(2).setCellValue(new HSSFRichTextString(dto.getSortNum()+""));
              }
              if (dto.getShowStatus() != null) { 
                row.createCell(3).setCellValue(new HSSFRichTextString(dto.getShowStatus() == 1 ? "是" : "否"));
              }
              if (dto.getIsDelete() != null) { 
                row.createCell(4).setCellValue(new HSSFRichTextString(dto.getIsDelete() == 1 ? "是" : "否"));
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
