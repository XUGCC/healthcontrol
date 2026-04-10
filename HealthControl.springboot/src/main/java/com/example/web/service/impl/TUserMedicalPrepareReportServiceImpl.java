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
 * 用户就诊准备报告功能实现类
 */
@Service
public class TUserMedicalPrepareReportServiceImpl extends ServiceImpl<TUserMedicalPrepareReportMapper, TUserMedicalPrepareReport> implements TUserMedicalPrepareReportService {

	 /**
     * 操作数据库AppUser表mapper对象
     */
    @Autowired
    private AppUserMapper AppUserMapper;
    /**
     * 操作数据库的TUserMedicalPrepareReport表mapper对象
     */
    @Autowired
    private TUserMedicalPrepareReportMapper TUserMedicalPrepareReportMapper;
    @Autowired
    private TPersonalLaryngealHealthRecordMapper  TPersonalLaryngealHealthRecordMapper;                        

  
   /**
     * 构建表查询sql
     */
    private LambdaQueryWrapper<TUserMedicalPrepareReport> BuilderQuery(TUserMedicalPrepareReportPagedInput input) {
       //声明一个支持用户就诊准备报告查询的(拉姆达)表达式
        LambdaQueryWrapper<TUserMedicalPrepareReport> queryWrapper = Wrappers.<TUserMedicalPrepareReport>lambdaQuery()
                .eq(input.getId() != null && input.getId() != 0, TUserMedicalPrepareReport::getId, input.getId());
   //如果前端搜索传入查询条件则拼接查询条件
        if (Extension.isNotNullOrEmpty(input.getDetectIdList())) {
             queryWrapper = queryWrapper.like(TUserMedicalPrepareReport::getDetectIdList, input.getDetectIdList());
       	 }
        if (Extension.isNotNullOrEmpty(input.getContentScope())) {
             queryWrapper = queryWrapper.like(TUserMedicalPrepareReport::getContentScope, input.getContentScope());
       	 }
        if (Extension.isNotNullOrEmpty(input.getGenerateFailReason())) {
             queryWrapper = queryWrapper.like(TUserMedicalPrepareReport::getGenerateFailReason, input.getGenerateFailReason());
       	 }

        if (input.getUserId() != null) {
            queryWrapper = queryWrapper.eq(TUserMedicalPrepareReport::getUserId, input.getUserId());
       	 }

        if (input.getHealthRecordId() != null) {
            queryWrapper = queryWrapper.eq(TUserMedicalPrepareReport::getHealthRecordId, input.getHealthRecordId());
       	 }
        if (input.getTemplateType() != null) {
            queryWrapper = queryWrapper.eq(TUserMedicalPrepareReport::getTemplateType, input.getTemplateType());
       	 }
        if (input.getGenerateStatus() != null) {
            queryWrapper = queryWrapper.eq(TUserMedicalPrepareReport::getGenerateStatus, input.getGenerateStatus());
       	 }
        if (input.getIsDelete() != null) {
            queryWrapper = queryWrapper.eq(TUserMedicalPrepareReport::getIsDelete, input.getIsDelete());
       	 }
      

 
 
     if(Extension.isNotNullOrEmpty(input.getKeyWord()))
        {
			queryWrapper=queryWrapper.and(i->i
          	   .like(TUserMedicalPrepareReport::getDetectIdList,input.getKeyWord()).or()   	 
          	   .like(TUserMedicalPrepareReport::getContentScope,input.getKeyWord()).or()   	 
          	   .like(TUserMedicalPrepareReport::getGenerateFailReason,input.getKeyWord()).or()   	 
        );
                                       
 		   }
    
      return queryWrapper;
    }
  
    /**
     * 处理用户就诊准备报告对于的外键数据
     */
   private List<TUserMedicalPrepareReportDto> DispatchItem(List<TUserMedicalPrepareReportDto> items) throws InvocationTargetException, IllegalAccessException {
          
       for (TUserMedicalPrepareReportDto item : items) {           
          	            
           //查询出关联的TPersonalLaryngealHealthRecord表信息           
            TPersonalLaryngealHealthRecord  HealthRecordEntity= TPersonalLaryngealHealthRecordMapper.selectById(item.getHealthRecordId());
            item.setHealthRecordDto(HealthRecordEntity!=null?HealthRecordEntity.MapToDto():new TPersonalLaryngealHealthRecordDto());              
           
          	            
           //查询出关联的AppUser表信息           
            AppUser  UserEntity= AppUserMapper.selectById(item.getUserId());
            item.setUserDto(UserEntity!=null?UserEntity.MapToDto():new AppUserDto());              
       }
       
     return items; 
   }
  
    /**
     * 用户就诊准备报告分页查询
     */
    @SneakyThrows
    @Override
    public PagedResult<TUserMedicalPrepareReportDto> List(TUserMedicalPrepareReportPagedInput input) {
			//构建where条件+排序
        LambdaQueryWrapper<TUserMedicalPrepareReport> queryWrapper = BuilderQuery(input);
        // 动态排序处理
        if (input.getSortItem() != null) {
            // 根据字段名动态排序
            queryWrapper.last("ORDER BY " + input.getSortItem().getFieldName()
                    + (input.getSortItem().getIsAsc() ? " ASC" : " DESC"));
        } else {
            // 默认按创建时间从大到小排序
            queryWrapper = queryWrapper.orderByDesc(TUserMedicalPrepareReport::getCreationTime);
        }

        //构建一个分页查询的model
        Page<TUserMedicalPrepareReport> page = new Page<>(input.getPage(), input.getLimit());
         //从数据库进行分页查询获取用户就诊准备报告数据
        IPage<TUserMedicalPrepareReport> pageRecords= TUserMedicalPrepareReportMapper.selectPage(page, queryWrapper);
        //获取所有满足条件的数据行数
        Long totalCount= TUserMedicalPrepareReportMapper.selectCount(queryWrapper);
        //把TUserMedicalPrepareReport实体转换成TUserMedicalPrepareReport传输模型
        List<TUserMedicalPrepareReportDto> items= Extension.copyBeanList(pageRecords.getRecords(),TUserMedicalPrepareReportDto.class);

		   DispatchItem(items);
        //返回一个分页结构给前端
        return PagedResult.GetInstance(items,totalCount);

    }
  
    /**
     * 单个用户就诊准备报告查询
     */
    @SneakyThrows
    @Override
    public TUserMedicalPrepareReportDto Get(TUserMedicalPrepareReportPagedInput input) {
       if(input.getId()==null)
        {
         return new TUserMedicalPrepareReportDto();
        }
      
       PagedResult<TUserMedicalPrepareReportDto> pagedResult = List(input);
        return pagedResult.getItems().stream().findFirst().orElse(new TUserMedicalPrepareReportDto()); 
    }

    /**
     *用户就诊准备报告创建或者修改
     */
    @SneakyThrows
    @Override
    public TUserMedicalPrepareReportDto CreateOrEdit(TUserMedicalPrepareReportDto input) {
        //声明一个用户就诊准备报告实体
        TUserMedicalPrepareReport TUserMedicalPrepareReport=input.MapToEntity();  
        //调用数据库的增加或者修改方法
        saveOrUpdate(TUserMedicalPrepareReport);
        //把传输模型返回给前端
        return TUserMedicalPrepareReport.MapToDto();
    }
    /**
     * 用户就诊准备报告删除
     */
    @Override
    public void Delete(IdInput input) {
        TUserMedicalPrepareReport entity = TUserMedicalPrepareReportMapper.selectById(input.getId());
        TUserMedicalPrepareReportMapper.deleteById(entity);
    }

    /**
     * 用户就诊准备报告批量删除
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
     * 用户就诊准备报告导出
     */
    @Override
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        TUserMedicalPrepareReportPagedInput input = mapper.readValue(query, TUserMedicalPrepareReportPagedInput.class);
        List<TUserMedicalPrepareReportDto> items =List(input).getItems();
        //声明一个工作簿
        HSSFWorkbook workbook = new HSSFWorkbook();
        //生成一个表格，设置表格名称为"用户就诊准备报告"
        HSSFSheet sheet = workbook.createSheet("用户就诊准备报告表");
        //设置表格列宽度为10个字节
        sheet.setDefaultColumnWidth(30);
        //创建标题的显示样式
        HSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.YELLOW.index);
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        //创建第一行表头
        HSSFRow headrow = sheet.createRow(0);
        //表头数据
        String[] header = {"名称" ,"症状持续时间：如1周/1月" ,"关联检测" ,"文档模板类型：0=简版，1=详版" ,"文档内容范围：如筛查记录+症状日志+饮食记录" ,"标准化文档URL" ,"生成状态：0=待生成，1=生成中，2=已完成，3=生成失败" ,"生成失败原因" ,"累计查看次数" ,"累计下载次数" ,"累计分享次数" ,"软删除标记：0=未删除，1=已删除" ,};   
        //遍历添加表头(下面模拟遍历用户就诊准备报告，也是同样的操作过程)
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
            TUserMedicalPrepareReportDto dto = items.get(i);
            //创建一行
            HSSFRow row = sheet.createRow(i + 1);    
   
              if (dto.getUserDto()!=null&&Extension.isNotNullOrEmpty(dto.getUserDto().getName())) { 
                row.createCell(0).setCellValue(new HSSFRichTextString(dto.getUserDto().getName()));
              }
              if (dto.getHealthRecordDto()!=null&&Extension.isNotNullOrEmpty(dto.getHealthRecordDto().getSymptomDuration())) { 
                row.createCell(1).setCellValue(new HSSFRichTextString(dto.getHealthRecordDto().getSymptomDuration()));
              }
              if (Extension.isNotNullOrEmpty(dto.getDetectIdList())) { 
                row.createCell(2).setCellValue(new HSSFRichTextString(dto.getDetectIdList()));
              }
              if (dto.getTemplateType() != null) { 
                row.createCell(3).setCellValue(new HSSFRichTextString(dto.getTemplateType()?"是":"否"));
              }
              if (Extension.isNotNullOrEmpty(dto.getContentScope())) { 
                row.createCell(4).setCellValue(new HSSFRichTextString(dto.getContentScope()));
              }
              if (Extension.isNotNullOrEmpty(dto.getStandardDocUrl())) { 
                row.createCell(5).setCellValue(new HSSFRichTextString(dto.getStandardDocUrl()));
              }
              if (dto.getGenerateStatus() != null) { 
                row.createCell(6).setCellValue(new HSSFRichTextString(dto.getGenerateStatus()?"是":"否"));
              }
              if (Extension.isNotNullOrEmpty(dto.getGenerateFailReason())) { 
                row.createCell(7).setCellValue(new HSSFRichTextString(dto.getGenerateFailReason()));
              }
              if (dto.getViewCount() != null) { 
                row.createCell(8).setCellValue(new HSSFRichTextString(dto.getViewCount()+""));
              }
              if (dto.getDownloadCount() != null) { 
                row.createCell(9).setCellValue(new HSSFRichTextString(dto.getDownloadCount()+""));
              }
              if (dto.getShareCount() != null) { 
                row.createCell(10).setCellValue(new HSSFRichTextString(dto.getShareCount()+""));
              }
              if (dto.getIsDelete() != null) { 
                row.createCell(11).setCellValue(new HSSFRichTextString(dto.getIsDelete()?"是":"否"));
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
