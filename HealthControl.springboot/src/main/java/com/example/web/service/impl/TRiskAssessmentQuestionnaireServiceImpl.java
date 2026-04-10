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
 * 风险评估问卷功能实现类
 */
@Service
public class TRiskAssessmentQuestionnaireServiceImpl extends ServiceImpl<TRiskAssessmentQuestionnaireMapper, TRiskAssessmentQuestionnaire> implements TRiskAssessmentQuestionnaireService {

	 /**
     * 操作数据库AppUser表mapper对象
     */
    @Autowired
    private AppUserMapper AppUserMapper;
    /**
     * 操作数据库的TRiskAssessmentQuestionnaire表mapper对象
     */
    @Autowired
    private TRiskAssessmentQuestionnaireMapper TRiskAssessmentQuestionnaireMapper;

  
   /**
     * 构建表查询sql
     */
    private LambdaQueryWrapper<TRiskAssessmentQuestionnaire> BuilderQuery(TRiskAssessmentQuestionnairePagedInput input) {
       //声明一个支持风险评估问卷查询的(拉姆达)表达式
        LambdaQueryWrapper<TRiskAssessmentQuestionnaire> queryWrapper = Wrappers.<TRiskAssessmentQuestionnaire>lambdaQuery()
                .eq(input.getId() != null && input.getId() != 0, TRiskAssessmentQuestionnaire::getId, input.getId());
   //如果前端搜索传入查询条件则拼接查询条件
        if (Extension.isNotNullOrEmpty(input.getQuestionnaireTitle())) {
             queryWrapper = queryWrapper.like(TRiskAssessmentQuestionnaire::getQuestionnaireTitle, input.getQuestionnaireTitle());
       	 }
        if (Extension.isNotNullOrEmpty(input.getQuestionnaireDesc())) {
             queryWrapper = queryWrapper.like(TRiskAssessmentQuestionnaire::getQuestionnaireDesc, input.getQuestionnaireDesc());
       	 }

        if (input.getCreatorId() != null) {
            queryWrapper = queryWrapper.eq(TRiskAssessmentQuestionnaire::getCreatorId, input.getCreatorId());
       	 }
        if (input.getUpdateTimeRange() != null && !input.getUpdateTimeRange().isEmpty()) {
            queryWrapper = queryWrapper.le(TRiskAssessmentQuestionnaire::getUpdateTime, input.getUpdateTimeRange().get(1));
            queryWrapper = queryWrapper.ge(TRiskAssessmentQuestionnaire::getUpdateTime, input.getUpdateTimeRange().get(0));
        }
      

 
 
     if(Extension.isNotNullOrEmpty(input.getKeyWord()))
        {
			queryWrapper=queryWrapper.and(i->i
          	   .like(TRiskAssessmentQuestionnaire::getQuestionnaireTitle,input.getKeyWord()).or()   	 
          	   .like(TRiskAssessmentQuestionnaire::getQuestionnaireDesc,input.getKeyWord()).or()   	 
        );
                                       
 		   }
    
      return queryWrapper;
    }
  
    /**
     * 处理风险评估问卷对于的外键数据
     */
   private List<TRiskAssessmentQuestionnaireDto> DispatchItem(List<TRiskAssessmentQuestionnaireDto> items) throws InvocationTargetException, IllegalAccessException {
          
       for (TRiskAssessmentQuestionnaireDto item : items) {           
          	            
           //查询出关联的AppUser表信息           
            AppUser  CreatorEntity= AppUserMapper.selectById(item.getCreatorId());
            item.setCreatorDto(CreatorEntity!=null?CreatorEntity.MapToDto():new AppUserDto());              
       }
       
     return items; 
   }
  
    /**
     * 风险评估问卷分页查询
     */
    @SneakyThrows
    @Override
    public PagedResult<TRiskAssessmentQuestionnaireDto> List(TRiskAssessmentQuestionnairePagedInput input) {
			//构建where条件+排序
        LambdaQueryWrapper<TRiskAssessmentQuestionnaire> queryWrapper = BuilderQuery(input);
        // 动态排序处理
        if (input.getSortItem() != null) {
            // 根据字段名动态排序
            queryWrapper.last("ORDER BY " + input.getSortItem().getFieldName()
                    + (input.getSortItem().getIsAsc() ? " ASC" : " DESC"));
        } else {
            // 默认按创建时间从大到小排序
            queryWrapper = queryWrapper.orderByDesc(TRiskAssessmentQuestionnaire::getCreationTime);
        }

        //构建一个分页查询的model
        Page<TRiskAssessmentQuestionnaire> page = new Page<>(input.getPage(), input.getLimit());
         //从数据库进行分页查询获取风险评估问卷数据
        IPage<TRiskAssessmentQuestionnaire> pageRecords= TRiskAssessmentQuestionnaireMapper.selectPage(page, queryWrapper);
        //获取所有满足条件的数据行数
        Long totalCount= TRiskAssessmentQuestionnaireMapper.selectCount(queryWrapper);
        //把TRiskAssessmentQuestionnaire实体转换成TRiskAssessmentQuestionnaire传输模型
        List<TRiskAssessmentQuestionnaireDto> items= Extension.copyBeanList(pageRecords.getRecords(),TRiskAssessmentQuestionnaireDto.class);

		   DispatchItem(items);
        //返回一个分页结构给前端
        return PagedResult.GetInstance(items,totalCount);

    }
  
    /**
     * 单个风险评估问卷查询
     */
    @SneakyThrows
    @Override
    public TRiskAssessmentQuestionnaireDto Get(TRiskAssessmentQuestionnairePagedInput input) {
       if(input.getId()==null)
        {
         return new TRiskAssessmentQuestionnaireDto();
        }
      
       PagedResult<TRiskAssessmentQuestionnaireDto> pagedResult = List(input);
        return pagedResult.getItems().stream().findFirst().orElse(new TRiskAssessmentQuestionnaireDto()); 
    }

    /**
     *风险评估问卷创建或者修改
     */
    @SneakyThrows
    @Override
    public TRiskAssessmentQuestionnaireDto CreateOrEdit(TRiskAssessmentQuestionnaireDto input) {
        //声明一个风险评估问卷实体
        TRiskAssessmentQuestionnaire TRiskAssessmentQuestionnaire=input.MapToEntity();  
        //调用数据库的增加或者修改方法
        saveOrUpdate(TRiskAssessmentQuestionnaire);
        //把传输模型返回给前端
        return TRiskAssessmentQuestionnaire.MapToDto();
    }
    /**
     * 风险评估问卷删除
     */
    @Override
    public void Delete(IdInput input) {
        TRiskAssessmentQuestionnaire entity = TRiskAssessmentQuestionnaireMapper.selectById(input.getId());
        TRiskAssessmentQuestionnaireMapper.deleteById(entity);
    }

    /**
     * 风险评估问卷批量删除
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
     * 风险评估问卷导出
     */
    @Override
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        TRiskAssessmentQuestionnairePagedInput input = mapper.readValue(query, TRiskAssessmentQuestionnairePagedInput.class);
        List<TRiskAssessmentQuestionnaireDto> items =List(input).getItems();
        //声明一个工作簿
        HSSFWorkbook workbook = new HSSFWorkbook();
        //生成一个表格，设置表格名称为"风险评估问卷"
        HSSFSheet sheet = workbook.createSheet("风险评估问卷表");
        //设置表格列宽度为10个字节
        sheet.setDefaultColumnWidth(30);
        //创建标题的显示样式
        HSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.YELLOW.index);
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        //创建第一行表头
        HSSFRow headrow = sheet.createRow(0);
        //表头数据
        String[] header = {"名称" ,"问卷标题" ,"问卷描述" ,"问题数量" ,"展示状态" ,"软删除标记" ,};   
        //遍历添加表头(下面模拟遍历风险评估问卷，也是同样的操作过程)
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
            TRiskAssessmentQuestionnaireDto dto = items.get(i);
            //创建一行
            HSSFRow row = sheet.createRow(i + 1);    
   
              if (dto.getCreatorDto()!=null&&Extension.isNotNullOrEmpty(dto.getCreatorDto().getName())) { 
                row.createCell(0).setCellValue(new HSSFRichTextString(dto.getCreatorDto().getName()));
              }
              if (Extension.isNotNullOrEmpty(dto.getQuestionnaireTitle())) { 
                row.createCell(1).setCellValue(new HSSFRichTextString(dto.getQuestionnaireTitle()));
              }
              if (Extension.isNotNullOrEmpty(dto.getQuestionnaireDesc())) { 
                row.createCell(2).setCellValue(new HSSFRichTextString(dto.getQuestionnaireDesc()));
              }
              if (dto.getQuestionCount() != null) { 
                row.createCell(3).setCellValue(new HSSFRichTextString(dto.getQuestionCount()+""));
              }
              if (dto.getShowStatus() != null) { 
                row.createCell(4).setCellValue(new HSSFRichTextString(dto.getShowStatus()+""));
              }
              if (dto.getIsDelete() != null) { 
                row.createCell(5).setCellValue(new HSSFRichTextString(dto.getIsDelete()+""));
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
