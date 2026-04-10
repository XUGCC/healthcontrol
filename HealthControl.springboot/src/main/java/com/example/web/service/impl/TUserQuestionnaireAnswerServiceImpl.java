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
import java.text.DecimalFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.springframework.web.bind.annotation.RequestParam;
/**
 * 用户问卷答题功能实现类
 */
@Service
public class TUserQuestionnaireAnswerServiceImpl extends ServiceImpl<TUserQuestionnaireAnswerMapper, TUserQuestionnaireAnswer> implements TUserQuestionnaireAnswerService {

	 /**
     * 操作数据库AppUser表mapper对象
     */
    @Autowired
    private AppUserMapper AppUserMapper;
    /**
     * 操作数据库的TUserQuestionnaireAnswer表mapper对象
     */
    @Autowired
    private TUserQuestionnaireAnswerMapper TUserQuestionnaireAnswerMapper;

  
   /**
     * 构建表查询sql
     */
    private LambdaQueryWrapper<TUserQuestionnaireAnswer> BuilderQuery(TUserQuestionnaireAnswerPagedInput input) {
       //声明一个支持用户问卷答题查询的(拉姆达)表达式
        LambdaQueryWrapper<TUserQuestionnaireAnswer> queryWrapper = Wrappers.<TUserQuestionnaireAnswer>lambdaQuery()
                .eq(input.getId() != null && input.getId() != 0, TUserQuestionnaireAnswer::getId, input.getId());
        // 兼容旧字段：SmokingHistory / DrinkingHistory / OccupationalVoice / AcidReflexSymptom / OtherRiskFactor
        // 这些字段已从实体中移除，仅保留在 DTO 中用于历史数据展示，因此这里不再作为查询条件
        if (input.getUserId() != null) {
            queryWrapper = queryWrapper.eq(TUserQuestionnaireAnswer::getUserId, input.getUserId());
       	 }
        if (input.getAnswerTimeRange() != null && !input.getAnswerTimeRange().isEmpty()) {
            queryWrapper = queryWrapper.le(TUserQuestionnaireAnswer::getAnswerTime, input.getAnswerTimeRange().get(1));
            queryWrapper = queryWrapper.ge(TUserQuestionnaireAnswer::getAnswerTime, input.getAnswerTimeRange().get(0));
        }
        if (input.getIsDelete() != null) {
            queryWrapper = queryWrapper.eq(TUserQuestionnaireAnswer::getIsDelete, input.getIsDelete());
       	 }
      

 
 
     // 关键字搜索也不再基于 OtherRiskFactor，保留接口字段但暂不生效
    
      return queryWrapper;
    }
  
    /**
     * 处理用户问卷答题对于的外键数据
     */
   private List<TUserQuestionnaireAnswerDto> DispatchItem(List<TUserQuestionnaireAnswerDto> items) throws InvocationTargetException, IllegalAccessException {
          
       for (TUserQuestionnaireAnswerDto item : items) {           
          	            
           //查询出关联的AppUser表信息           
            AppUser  UserEntity= AppUserMapper.selectById(item.getUserId());
            item.setUserDto(UserEntity!=null?UserEntity.MapToDto():new AppUserDto());              
       }
       
     return items; 
   }
  
    /**
     * 用户问卷答题分页查询
     */
    @SneakyThrows
    @Override
    public PagedResult<TUserQuestionnaireAnswerDto> List(TUserQuestionnaireAnswerPagedInput input) {
			//构建where条件+排序
        LambdaQueryWrapper<TUserQuestionnaireAnswer> queryWrapper = BuilderQuery(input);
        // 动态排序处理
        if (input.getSortItem() != null) {
            // 根据字段名动态排序
            queryWrapper.last("ORDER BY " + input.getSortItem().getFieldName()
                    + (input.getSortItem().getIsAsc() ? " ASC" : " DESC"));
        } else {
            // 默认按创建时间从大到小排序
            queryWrapper = queryWrapper.orderByDesc(TUserQuestionnaireAnswer::getCreationTime);
        }

        //构建一个分页查询的model
        Page<TUserQuestionnaireAnswer> page = new Page<>(input.getPage(), input.getLimit());
         //从数据库进行分页查询获取用户问卷答题数据
        IPage<TUserQuestionnaireAnswer> pageRecords= TUserQuestionnaireAnswerMapper.selectPage(page, queryWrapper);
        //获取所有满足条件的数据行数
        Long totalCount= TUserQuestionnaireAnswerMapper.selectCount(queryWrapper);
        //把TUserQuestionnaireAnswer实体转换成TUserQuestionnaireAnswer传输模型
        List<TUserQuestionnaireAnswerDto> items= Extension.copyBeanList(pageRecords.getRecords(),TUserQuestionnaireAnswerDto.class);

		   DispatchItem(items);
        //返回一个分页结构给前端
        return PagedResult.GetInstance(items,totalCount);

    }
  
    /**
     * 单个用户问卷答题查询
     */
    @SneakyThrows
    @Override
    public TUserQuestionnaireAnswerDto Get(TUserQuestionnaireAnswerPagedInput input) {
       if(input.getId()==null)
        {
         return new TUserQuestionnaireAnswerDto();
        }
      
       PagedResult<TUserQuestionnaireAnswerDto> pagedResult = List(input);
        return pagedResult.getItems().stream().findFirst().orElse(new TUserQuestionnaireAnswerDto()); 
    }

    /**
     *用户问卷答题创建或者修改
     */
    @SneakyThrows
    @Override
    public TUserQuestionnaireAnswerDto CreateOrEdit(TUserQuestionnaireAnswerDto input) {
        //声明一个用户问卷答题实体
        TUserQuestionnaireAnswer TUserQuestionnaireAnswer=input.MapToEntity();  
        //调用数据库的增加或者修改方法
        saveOrUpdate(TUserQuestionnaireAnswer);
        //把传输模型返回给前端
        return TUserQuestionnaireAnswer.MapToDto();
    }
    /**
     * 用户问卷答题删除
     */
    @Override
    public void Delete(IdInput input) {
        TUserQuestionnaireAnswer entity = TUserQuestionnaireAnswerMapper.selectById(input.getId());
        TUserQuestionnaireAnswerMapper.deleteById(entity);
    }

    /**
     * 用户问卷答题批量删除
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
     * 用户问卷答题导出
     */
    @Override
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        TUserQuestionnaireAnswerPagedInput input = mapper.readValue(query, TUserQuestionnaireAnswerPagedInput.class);
        List<TUserQuestionnaireAnswerDto> items =List(input).getItems();
        //声明一个工作簿
        HSSFWorkbook workbook = new HSSFWorkbook();
        //生成一个表格，设置表格名称为"用户问卷答题"
        HSSFSheet sheet = workbook.createSheet("用户问卷答题表");
        //设置表格列宽度为10个字节
        sheet.setDefaultColumnWidth(30);
        //创建标题的显示样式
        HSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.YELLOW.index);
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        //创建第一行表头
        HSSFRow headrow = sheet.createRow(0);
        //表头数据
        String[] header = {"名称" ,"关联问卷表主键" ,"吸烟史" ,"饮酒史" ,"职业用嗓" ,"反酸症状" ,"其他风险因素" ,"风险评估得分" ,"软删除标记" ,};   
        //遍历添加表头(下面模拟遍历用户问卷答题，也是同样的操作过程)
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
            TUserQuestionnaireAnswerDto dto = items.get(i);
            //创建一行
            HSSFRow row = sheet.createRow(i + 1);    
   
              if (dto.getUserDto()!=null&&Extension.isNotNullOrEmpty(dto.getUserDto().getName())) { 
                row.createCell(0).setCellValue(new HSSFRichTextString(dto.getUserDto().getName()));
              }
              if (dto.getQuestionnaireId() != null) { 
                row.createCell(1).setCellValue(new HSSFRichTextString(dto.getQuestionnaireId()+""));
              }
              if (dto.getSmokingHistory() != null) { 
                row.createCell(2).setCellValue(new HSSFRichTextString(dto.getSmokingHistory()?"是":"否"));
              }
              if (dto.getDrinkingHistory() != null) { 
                row.createCell(3).setCellValue(new HSSFRichTextString(dto.getDrinkingHistory()?"是":"否"));
              }
              if (dto.getOccupationalVoice() != null) { 
                row.createCell(4).setCellValue(new HSSFRichTextString(dto.getOccupationalVoice()?"是":"否"));
              }
              if (dto.getAcidReflexSymptom() != null) { 
                row.createCell(5).setCellValue(new HSSFRichTextString(dto.getAcidReflexSymptom()?"是":"否"));
              }
              if (Extension.isNotNullOrEmpty(dto.getOtherRiskFactor())) { 
                row.createCell(6).setCellValue(new HSSFRichTextString(dto.getOtherRiskFactor()));
              }
              if (dto.getRiskAssessmentScore() != null) { 
                //保留2位小数
                DecimalFormat df = new DecimalFormat("#.00");
                row.createCell(7).setCellValue(new HSSFRichTextString(df.format(dto.getRiskAssessmentScore())));
              }
              if (dto.getIsDelete() != null) { 
                row.createCell(8).setCellValue(new HSSFRichTextString(dto.getIsDelete()?"是":"否"));
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
