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
 * 自查报告导出记录功能实现类
 */
@Service
public class TScreenReportExportRecordServiceImpl extends ServiceImpl<TScreenReportExportRecordMapper, TScreenReportExportRecord> implements TScreenReportExportRecordService {

	 /**
     * 操作数据库AppUser表mapper对象
     */
    @Autowired
    private AppUserMapper AppUserMapper;
    /**
     * 操作数据库的TScreenReportExportRecord表mapper对象
     */
    @Autowired
    private TScreenReportExportRecordMapper TScreenReportExportRecordMapper;
    @Autowired
    private TAudioScreenRecordMapper  TAudioScreenRecordMapper;                        

  
   /**
     * 构建表查询sql
     */
    private LambdaQueryWrapper<TScreenReportExportRecord> BuilderQuery(TScreenReportExportRecordPagedInput input) {
       //声明一个支持自查报告导出记录查询的(拉姆达)表达式
        LambdaQueryWrapper<TScreenReportExportRecord> queryWrapper = Wrappers.<TScreenReportExportRecord>lambdaQuery()
                .eq(input.getId() != null && input.getId() != 0, TScreenReportExportRecord::getId, input.getId());
   //如果前端搜索传入查询条件则拼接查询条件

        if (input.getUserId() != null) {
            queryWrapper = queryWrapper.eq(TScreenReportExportRecord::getUserId, input.getUserId());
       	 }

        if (input.getDetectId() != null) {
            queryWrapper = queryWrapper.eq(TScreenReportExportRecord::getDetectId, input.getDetectId());
       	 }
        if (input.getExportTimeRange() != null && !input.getExportTimeRange().isEmpty()) {
            queryWrapper = queryWrapper.le(TScreenReportExportRecord::getExportTime, input.getExportTimeRange().get(1));
            queryWrapper = queryWrapper.ge(TScreenReportExportRecord::getExportTime, input.getExportTimeRange().get(0));
        }
        if (input.getExportFormat() != null) {
            queryWrapper = queryWrapper.eq(TScreenReportExportRecord::getExportFormat, input.getExportFormat());
       	 }
        if (input.getExportPurpose() != null) {
            queryWrapper = queryWrapper.eq(TScreenReportExportRecord::getExportPurpose, input.getExportPurpose());
       	 }
        if (input.getIsDelete() != null) {
            queryWrapper = queryWrapper.eq(TScreenReportExportRecord::getIsDelete, input.getIsDelete());
       	 }
      

 
    
      return queryWrapper;
    }
  
    /**
     * 处理自查报告导出记录对于的外键数据
     */
   private List<TScreenReportExportRecordDto> DispatchItem(List<TScreenReportExportRecordDto> items) throws InvocationTargetException, IllegalAccessException {
          
       for (TScreenReportExportRecordDto item : items) {           
          	            
           //查询出关联的TAudioScreenRecord表信息           
            TAudioScreenRecord  DetectEntity= TAudioScreenRecordMapper.selectById(item.getDetectId());
            item.setDetectDto(DetectEntity!=null?DetectEntity.MapToDto():new TAudioScreenRecordDto());              
           
          	            
           //查询出关联的AppUser表信息           
            AppUser  UserEntity= AppUserMapper.selectById(item.getUserId());
            item.setUserDto(UserEntity!=null?UserEntity.MapToDto():new AppUserDto());              
       }
       
     return items; 
   }
  
    /**
     * 自查报告导出记录分页查询
     */
    @SneakyThrows
    @Override
    public PagedResult<TScreenReportExportRecordDto> List(TScreenReportExportRecordPagedInput input) {
			//构建where条件+排序
        LambdaQueryWrapper<TScreenReportExportRecord> queryWrapper = BuilderQuery(input);
        // 动态排序处理
        if (input.getSortItem() != null) {
            // 根据字段名动态排序
            queryWrapper.last("ORDER BY " + input.getSortItem().getFieldName()
                    + (input.getSortItem().getIsAsc() ? " ASC" : " DESC"));
        } else {
            // 默认按创建时间从大到小排序
            queryWrapper = queryWrapper.orderByDesc(TScreenReportExportRecord::getCreationTime);
        }

        //构建一个分页查询的model
        Page<TScreenReportExportRecord> page = new Page<>(input.getPage(), input.getLimit());
         //从数据库进行分页查询获取自查报告导出记录数据
        IPage<TScreenReportExportRecord> pageRecords= TScreenReportExportRecordMapper.selectPage(page, queryWrapper);
        //获取所有满足条件的数据行数
        Long totalCount= TScreenReportExportRecordMapper.selectCount(queryWrapper);
        //把TScreenReportExportRecord实体转换成TScreenReportExportRecord传输模型
        List<TScreenReportExportRecordDto> items= Extension.copyBeanList(pageRecords.getRecords(),TScreenReportExportRecordDto.class);

		   DispatchItem(items);
        //返回一个分页结构给前端
        return PagedResult.GetInstance(items,totalCount);

    }
  
    /**
     * 单个自查报告导出记录查询
     */
    @SneakyThrows
    @Override
    public TScreenReportExportRecordDto Get(TScreenReportExportRecordPagedInput input) {
       if(input.getId()==null)
        {
         return new TScreenReportExportRecordDto();
        }
      
       PagedResult<TScreenReportExportRecordDto> pagedResult = List(input);
        return pagedResult.getItems().stream().findFirst().orElse(new TScreenReportExportRecordDto()); 
    }

    /**
     *自查报告导出记录创建或者修改
     */
    @SneakyThrows
    @Override
    public TScreenReportExportRecordDto CreateOrEdit(TScreenReportExportRecordDto input) {
        //声明一个自查报告导出记录实体
        TScreenReportExportRecord TScreenReportExportRecord=input.MapToEntity();  
        //调用数据库的增加或者修改方法
        saveOrUpdate(TScreenReportExportRecord);
        //把传输模型返回给前端
        return TScreenReportExportRecord.MapToDto();
    }
    /**
     * 自查报告导出记录删除
     */
    @Override
    public void Delete(IdInput input) {
        TScreenReportExportRecord entity = TScreenReportExportRecordMapper.selectById(input.getId());
        TScreenReportExportRecordMapper.deleteById(entity);
    }

    /**
     * 自查报告导出记录批量删除
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
     * 自查报告导出记录导出
     */
    @Override
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        TScreenReportExportRecordPagedInput input = mapper.readValue(query, TScreenReportExportRecordPagedInput.class);
        List<TScreenReportExportRecordDto> items =List(input).getItems();
        //声明一个工作簿
        HSSFWorkbook workbook = new HSSFWorkbook();
        //生成一个表格，设置表格名称为"自查报告导出记录"
        HSSFSheet sheet = workbook.createSheet("自查报告导出记录表");
        //设置表格列宽度为10个字节
        sheet.setDefaultColumnWidth(30);
        //创建标题的显示样式
        HSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.YELLOW.index);
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        //创建第一行表头
        HSSFRow headrow = sheet.createRow(0);
        //表头数据
        String[] header = {"名称" ,"孪生Densenet模型版本" ,"导出格式" ,"导出文件URL" ,"导出用途" ,"软删除标记" ,};   
        //遍历添加表头(下面模拟遍历自查报告导出记录，也是同样的操作过程)
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
            TScreenReportExportRecordDto dto = items.get(i);
            //创建一行
            HSSFRow row = sheet.createRow(i + 1);    
   
              if (dto.getUserDto()!=null&&Extension.isNotNullOrEmpty(dto.getUserDto().getName())) { 
                row.createCell(0).setCellValue(new HSSFRichTextString(dto.getUserDto().getName()));
              }
              if (dto.getDetectDto()!=null&&Extension.isNotNullOrEmpty(dto.getDetectDto().getDensenetModelVersion())) { 
                row.createCell(1).setCellValue(new HSSFRichTextString(dto.getDetectDto().getDensenetModelVersion()));
              }
              if (dto.getExportFormat() != null) { 
                row.createCell(2).setCellValue(new HSSFRichTextString(dto.getExportFormat()?"是":"否"));
              }
              if (Extension.isNotNullOrEmpty(dto.getExportFileUrl())) { 
                row.createCell(3).setCellValue(new HSSFRichTextString(dto.getExportFileUrl()));
              }
              if (dto.getExportPurpose() != null) { 
                row.createCell(4).setCellValue(new HSSFRichTextString(dto.getExportPurpose()?"是":"否"));
              }
              if (dto.getIsDelete() != null) { 
                row.createCell(5).setCellValue(new HSSFRichTextString(dto.getIsDelete()?"是":"否"));
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
