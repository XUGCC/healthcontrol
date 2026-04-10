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
 * 耳鼻喉医院医生功能实现类
 */
@Service
public class TOtolaryngologyHospitalDoctorServiceImpl extends ServiceImpl<TOtolaryngologyHospitalDoctorMapper, TOtolaryngologyHospitalDoctor> implements TOtolaryngologyHospitalDoctorService {

	 /**
     * 操作数据库AppUser表mapper对象
     */
    @Autowired
    private AppUserMapper AppUserMapper;
    /**
     * 操作数据库的TOtolaryngologyHospitalDoctor表mapper对象
     */
    @Autowired
    private TOtolaryngologyHospitalDoctorMapper TOtolaryngologyHospitalDoctorMapper;

  
   /**
     * 构建表查询sql
     */
    private LambdaQueryWrapper<TOtolaryngologyHospitalDoctor> BuilderQuery(TOtolaryngologyHospitalDoctorPagedInput input) {
       //声明一个支持耳鼻喉医院医生查询的(拉姆达)表达式
        LambdaQueryWrapper<TOtolaryngologyHospitalDoctor> queryWrapper = Wrappers.<TOtolaryngologyHospitalDoctor>lambdaQuery()
                .eq(input.getId() != null && input.getId() != 0, TOtolaryngologyHospitalDoctor::getId, input.getId());
   //如果前端搜索传入查询条件则拼接查询条件
        if (Extension.isNotNullOrEmpty(input.getHospitalName())) {
             queryWrapper = queryWrapper.like(TOtolaryngologyHospitalDoctor::getHospitalName, input.getHospitalName());
       	 }
        if (Extension.isNotNullOrEmpty(input.getDoctorName())) {
             queryWrapper = queryWrapper.like(TOtolaryngologyHospitalDoctor::getDoctorName, input.getDoctorName());
       	 }
        if (Extension.isNotNullOrEmpty(input.getRegion())) {
             queryWrapper = queryWrapper.like(TOtolaryngologyHospitalDoctor::getRegion, input.getRegion());
       	 }
        if (Extension.isNotNullOrEmpty(input.getAddress())) {
             queryWrapper = queryWrapper.like(TOtolaryngologyHospitalDoctor::getAddress, input.getAddress());
       	 }
        if (Extension.isNotNullOrEmpty(input.getContactPhone())) {
             queryWrapper = queryWrapper.like(TOtolaryngologyHospitalDoctor::getContactPhone, input.getContactPhone());
       	 }

        if (input.getCreatorId() != null) {
            queryWrapper = queryWrapper.eq(TOtolaryngologyHospitalDoctor::getCreatorId, input.getCreatorId());
       	 }
        if (input.getDepartmentType() != null) {
            queryWrapper = queryWrapper.eq(TOtolaryngologyHospitalDoctor::getDepartmentType, input.getDepartmentType());
       	 }
        if (input.getShowStatus() != null) {
            queryWrapper = queryWrapper.eq(TOtolaryngologyHospitalDoctor::getShowStatus, input.getShowStatus());
       	 }
        if (input.getIsDelete() != null) {
            queryWrapper = queryWrapper.eq(TOtolaryngologyHospitalDoctor::getIsDelete, input.getIsDelete());
       	 }
      

 
 
     if(Extension.isNotNullOrEmpty(input.getKeyWord()))
        {
			queryWrapper=queryWrapper.and(i->i
          	   .like(TOtolaryngologyHospitalDoctor::getHospitalName,input.getKeyWord()).or()   	 
          	   .like(TOtolaryngologyHospitalDoctor::getDoctorName,input.getKeyWord()).or()   	 
          	   .like(TOtolaryngologyHospitalDoctor::getRegion,input.getKeyWord()).or()   	 
          	   .like(TOtolaryngologyHospitalDoctor::getAddress,input.getKeyWord()).or()   	 
          	   .like(TOtolaryngologyHospitalDoctor::getContactPhone,input.getKeyWord()).or()   	 
        );
                                       
 		   }
    
      return queryWrapper;
    }
  
    /**
     * 处理耳鼻喉医院医生对于的外键数据
     */
   private List<TOtolaryngologyHospitalDoctorDto> DispatchItem(List<TOtolaryngologyHospitalDoctorDto> items) throws InvocationTargetException, IllegalAccessException {
          
       for (TOtolaryngologyHospitalDoctorDto item : items) {           
          	            
           //查询出关联的AppUser表信息           
            AppUser  CreatorEntity= AppUserMapper.selectById(item.getCreatorId());
            item.setCreatorDto(CreatorEntity!=null?CreatorEntity.MapToDto():new AppUserDto());              
       }
       
     return items; 
   }
  
    /**
     * 耳鼻喉医院医生分页查询
     */
    @SneakyThrows
    @Override
    public PagedResult<TOtolaryngologyHospitalDoctorDto> List(TOtolaryngologyHospitalDoctorPagedInput input) {
			//构建where条件+排序
        LambdaQueryWrapper<TOtolaryngologyHospitalDoctor> queryWrapper = BuilderQuery(input);
        // 动态排序处理
        if (input.getSortItem() != null) {
            // 根据字段名动态排序
            queryWrapper.last("ORDER BY " + input.getSortItem().getFieldName()
                    + (input.getSortItem().getIsAsc() ? " ASC" : " DESC"));
        } else {
            // 默认按创建时间从大到小排序
            queryWrapper = queryWrapper.orderByDesc(TOtolaryngologyHospitalDoctor::getCreationTime);
        }

        //构建一个分页查询的model
        Page<TOtolaryngologyHospitalDoctor> page = new Page<>(input.getPage(), input.getLimit());
         //从数据库进行分页查询获取耳鼻喉医院医生数据
        IPage<TOtolaryngologyHospitalDoctor> pageRecords= TOtolaryngologyHospitalDoctorMapper.selectPage(page, queryWrapper);
        //获取所有满足条件的数据行数
        Long totalCount= TOtolaryngologyHospitalDoctorMapper.selectCount(queryWrapper);
        //把TOtolaryngologyHospitalDoctor实体转换成TOtolaryngologyHospitalDoctor传输模型
        List<TOtolaryngologyHospitalDoctorDto> items= Extension.copyBeanList(pageRecords.getRecords(),TOtolaryngologyHospitalDoctorDto.class);

		   DispatchItem(items);
        //返回一个分页结构给前端
        return PagedResult.GetInstance(items,totalCount);

    }
  
    /**
     * 单个耳鼻喉医院医生查询
     */
    @SneakyThrows
    @Override
    public TOtolaryngologyHospitalDoctorDto Get(TOtolaryngologyHospitalDoctorPagedInput input) {
       if(input.getId()==null)
        {
         return new TOtolaryngologyHospitalDoctorDto();
        }
      
       PagedResult<TOtolaryngologyHospitalDoctorDto> pagedResult = List(input);
        return pagedResult.getItems().stream().findFirst().orElse(new TOtolaryngologyHospitalDoctorDto()); 
    }

    /**
     *耳鼻喉医院医生创建或者修改
     */
    @SneakyThrows
    @Override
    public TOtolaryngologyHospitalDoctorDto CreateOrEdit(TOtolaryngologyHospitalDoctorDto input) {
        //声明一个耳鼻喉医院医生实体
        TOtolaryngologyHospitalDoctor TOtolaryngologyHospitalDoctor=input.MapToEntity();  
        //调用数据库的增加或者修改方法
        saveOrUpdate(TOtolaryngologyHospitalDoctor);
        //把传输模型返回给前端
        return TOtolaryngologyHospitalDoctor.MapToDto();
    }
    /**
     * 耳鼻喉医院医生删除
     */
    @Override
    public void Delete(IdInput input) {
        TOtolaryngologyHospitalDoctor entity = TOtolaryngologyHospitalDoctorMapper.selectById(input.getId());
        TOtolaryngologyHospitalDoctorMapper.deleteById(entity);
    }

    /**
     * 耳鼻喉医院医生批量删除
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
     * 耳鼻喉医院医生导出
     */
    @Override
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        TOtolaryngologyHospitalDoctorPagedInput input = mapper.readValue(query, TOtolaryngologyHospitalDoctorPagedInput.class);
        List<TOtolaryngologyHospitalDoctorDto> items =List(input).getItems();
        //声明一个工作簿
        HSSFWorkbook workbook = new HSSFWorkbook();
        //生成一个表格，设置表格名称为"耳鼻喉医院医生"
        HSSFSheet sheet = workbook.createSheet("耳鼻喉医院医生表");
        //设置表格列宽度为10个字节
        sheet.setDefaultColumnWidth(30);
        //创建标题的显示样式
        HSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.YELLOW.index);
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        //创建第一行表头
        HSSFRow headrow = sheet.createRow(0);
        //表头数据
        String[] header = {"医院名称" ,"医生姓名" ,"科室类型：0=耳鼻喉科，1=咽喉科，2=头颈外科" ,"所在地区：如北京市海淀区" ,"详细地址" ,"联系电话" ,"预约链接" ,"展示状态：0=隐藏，1=展示" ,"软删除标记：0=未删除，1=已删除" ,"名称" ,};   
        //遍历添加表头(下面模拟遍历耳鼻喉医院医生，也是同样的操作过程)
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
            TOtolaryngologyHospitalDoctorDto dto = items.get(i);
            //创建一行
            HSSFRow row = sheet.createRow(i + 1);    
   
              if (Extension.isNotNullOrEmpty(dto.getHospitalName())) { 
                row.createCell(0).setCellValue(new HSSFRichTextString(dto.getHospitalName()));
              }
              if (Extension.isNotNullOrEmpty(dto.getDoctorName())) { 
                row.createCell(1).setCellValue(new HSSFRichTextString(dto.getDoctorName()));
              }
              if (dto.getDepartmentType() != null) { 
                row.createCell(2).setCellValue(new HSSFRichTextString(dto.getDepartmentType()?"是":"否"));
              }
              if (Extension.isNotNullOrEmpty(dto.getRegion())) { 
                row.createCell(3).setCellValue(new HSSFRichTextString(dto.getRegion()));
              }
              if (Extension.isNotNullOrEmpty(dto.getAddress())) { 
                row.createCell(4).setCellValue(new HSSFRichTextString(dto.getAddress()));
              }
              if (Extension.isNotNullOrEmpty(dto.getContactPhone())) { 
                row.createCell(5).setCellValue(new HSSFRichTextString(dto.getContactPhone()));
              }
              if (Extension.isNotNullOrEmpty(dto.getAppointmentUrl())) { 
                row.createCell(6).setCellValue(new HSSFRichTextString(dto.getAppointmentUrl()));
              }
              if (dto.getShowStatus() != null) { 
                row.createCell(7).setCellValue(new HSSFRichTextString(dto.getShowStatus()?"是":"否"));
              }
              if (dto.getIsDelete() != null) { 
                row.createCell(8).setCellValue(new HSSFRichTextString(dto.getIsDelete()?"是":"否"));
              }
              if (dto.getCreatorDto()!=null&&Extension.isNotNullOrEmpty(dto.getCreatorDto().getName())) { 
                row.createCell(9).setCellValue(new HSSFRichTextString(dto.getCreatorDto().getName()));
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
