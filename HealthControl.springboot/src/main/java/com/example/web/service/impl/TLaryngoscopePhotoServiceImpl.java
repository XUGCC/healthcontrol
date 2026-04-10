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
import java.time.LocalDateTime;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.springframework.web.bind.annotation.RequestParam;
/**
 * 喉镜照片记录功能实现类
 */
@Service
public class TLaryngoscopePhotoServiceImpl extends ServiceImpl<TLaryngoscopePhotoMapper, TLaryngoscopePhoto> implements TLaryngoscopePhotoService {

	 /**
     * 操作数据库AppUser表mapper对象
     */
    @Autowired
    private AppUserMapper AppUserMapper;
    /**
     * 操作数据库的TLaryngoscopePhoto表mapper对象
     */
    @Autowired
    private TLaryngoscopePhotoMapper TLaryngoscopePhotoMapper;
    @Autowired
    private TAudioScreenRecordMapper  TAudioScreenRecordMapper;                        

  
   /**
     * 构建表查询sql
     */
    private LambdaQueryWrapper<TLaryngoscopePhoto> BuilderQuery(TLaryngoscopePhotoPagedInput input) {
       //声明一个支持喉镜照片记录查询的(拉姆达)表达式
        LambdaQueryWrapper<TLaryngoscopePhoto> queryWrapper = Wrappers.<TLaryngoscopePhoto>lambdaQuery()
                .eq(input.getId() != null && input.getId() != 0, TLaryngoscopePhoto::getId, input.getId());
   //如果前端搜索传入查询条件则拼接查询条件

        if (input.getUserId() != null) {
            queryWrapper = queryWrapper.eq(TLaryngoscopePhoto::getUserId, input.getUserId());
       	 }

        if (input.getDetectId() != null) {
            queryWrapper = queryWrapper.eq(TLaryngoscopePhoto::getDetectId, input.getDetectId());
       	 }
        if (input.getUpdateTimeRange() != null && !input.getUpdateTimeRange().isEmpty()) {
            queryWrapper = queryWrapper.le(TLaryngoscopePhoto::getUpdateTime, input.getUpdateTimeRange().get(1));
            queryWrapper = queryWrapper.ge(TLaryngoscopePhoto::getUpdateTime, input.getUpdateTimeRange().get(0));
        }
        if (input.getUploadTimeRange() != null && !input.getUploadTimeRange().isEmpty()) {
            queryWrapper = queryWrapper.le(TLaryngoscopePhoto::getUploadTime, input.getUploadTimeRange().get(1));
            queryWrapper = queryWrapper.ge(TLaryngoscopePhoto::getUploadTime, input.getUploadTimeRange().get(0));
        }
        if (input.getAuditStatus() != null) {
            queryWrapper = queryWrapper.eq(TLaryngoscopePhoto::getAuditStatus, input.getAuditStatus());
       	 }
        if (input.getIsDelete() != null) {
            queryWrapper = queryWrapper.eq(TLaryngoscopePhoto::getIsDelete, input.getIsDelete());
       	 }
      

 
    
      return queryWrapper;
    }
  
    /**
     * 处理喉镜照片记录对于的外键数据
     */
   private List<TLaryngoscopePhotoDto> DispatchItem(List<TLaryngoscopePhotoDto> items) throws InvocationTargetException, IllegalAccessException {
          
       for (TLaryngoscopePhotoDto item : items) {           
          	            
           //查询出关联的AppUser表信息           
            AppUser  UserEntity= AppUserMapper.selectById(item.getUserId());
            item.setUserDto(UserEntity!=null?UserEntity.MapToDto():new AppUserDto());              
           
          	            
           //查询出关联的TAudioScreenRecord表信息           
            TAudioScreenRecord  DetectEntity= TAudioScreenRecordMapper.selectById(item.getDetectId());
            item.setDetectDto(DetectEntity!=null?DetectEntity.MapToDto():new TAudioScreenRecordDto());              
       }
       
     return items; 
   }
  
    /**
     * 喉镜照片记录分页查询
     */
    @SneakyThrows
    @Override
    public PagedResult<TLaryngoscopePhotoDto> List(TLaryngoscopePhotoPagedInput input) {
			//构建where条件+排序
        LambdaQueryWrapper<TLaryngoscopePhoto> queryWrapper = BuilderQuery(input);
        // 动态排序处理
        if (input.getSortItem() != null) {
            // 根据字段名动态排序
            queryWrapper.last("ORDER BY " + input.getSortItem().getFieldName()
                    + (input.getSortItem().getIsAsc() ? " ASC" : " DESC"));
        } else {
            // 默认按创建时间从大到小排序
            queryWrapper = queryWrapper.orderByDesc(TLaryngoscopePhoto::getCreationTime);
        }

        //构建一个分页查询的model
        Page<TLaryngoscopePhoto> page = new Page<>(input.getPage(), input.getLimit());
         //从数据库进行分页查询获取喉镜照片记录数据
        IPage<TLaryngoscopePhoto> pageRecords= TLaryngoscopePhotoMapper.selectPage(page, queryWrapper);
        //获取所有满足条件的数据行数
        Long totalCount= TLaryngoscopePhotoMapper.selectCount(queryWrapper);
        //把TLaryngoscopePhoto实体转换成TLaryngoscopePhoto传输模型
        List<TLaryngoscopePhotoDto> items= Extension.copyBeanList(pageRecords.getRecords(),TLaryngoscopePhotoDto.class);

		   DispatchItem(items);
        //返回一个分页结构给前端
        return PagedResult.GetInstance(items,totalCount);

    }
  
    /**
     * 单个喉镜照片记录查询
     */
    @SneakyThrows
    @Override
    public TLaryngoscopePhotoDto Get(TLaryngoscopePhotoPagedInput input) {
       if(input.getId()==null)
        {
         return new TLaryngoscopePhotoDto();
        }
      
       PagedResult<TLaryngoscopePhotoDto> pagedResult = List(input);
        return pagedResult.getItems().stream().findFirst().orElse(new TLaryngoscopePhotoDto()); 
    }

    /**
     *喉镜照片记录创建或者修改
     */
    @SneakyThrows
    @Override
    public TLaryngoscopePhotoDto CreateOrEdit(TLaryngoscopePhotoDto input) {
        //声明一个喉镜照片记录实体
        TLaryngoscopePhoto TLaryngoscopePhoto=input.MapToEntity();  
        // 如果未显式设置上传时间，则在首次创建或编辑时统一填充为当前时间
        if (TLaryngoscopePhoto.getUploadTime() == null) {
            TLaryngoscopePhoto.setUploadTime(LocalDateTime.now());
        }
        //调用数据库的增加或者修改方法
        saveOrUpdate(TLaryngoscopePhoto);
        //把传输模型返回给前端
        return TLaryngoscopePhoto.MapToDto();
    }
    /**
     * 喉镜照片记录删除
     */
    @Override
    public void Delete(IdInput input) {
        TLaryngoscopePhoto entity = TLaryngoscopePhotoMapper.selectById(input.getId());
        TLaryngoscopePhotoMapper.deleteById(entity);
    }

    /**
     * 喉镜照片记录批量删除
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
     * 喉镜照片记录导出
     */
    @Override
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        TLaryngoscopePhotoPagedInput input = mapper.readValue(query, TLaryngoscopePhotoPagedInput.class);
        List<TLaryngoscopePhotoDto> items =List(input).getItems();
        //声明一个工作簿
        HSSFWorkbook workbook = new HSSFWorkbook();
        //生成一个表格，设置表格名称为"喉镜照片记录"
        HSSFSheet sheet = workbook.createSheet("喉镜照片记录表");
        //设置表格列宽度为10个字节
        sheet.setDefaultColumnWidth(30);
        //创建标题的显示样式
        HSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.YELLOW.index);
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        //创建第一行表头
        HSSFRow headrow = sheet.createRow(0);
        //表头数据
        String[] header = {"名称" ,"孪生Densenet模型版本" ,"喉镜照片URL" ,"照片描述" ,"审核状态" ,"软删除标记" ,};   
        //遍历添加表头(下面模拟遍历喉镜照片记录，也是同样的操作过程)
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
            TLaryngoscopePhotoDto dto = items.get(i);
            //创建一行
            HSSFRow row = sheet.createRow(i + 1);    
   
              if (dto.getUserDto()!=null&&Extension.isNotNullOrEmpty(dto.getUserDto().getName())) { 
                row.createCell(0).setCellValue(new HSSFRichTextString(dto.getUserDto().getName()));
              }
              if (dto.getDetectDto()!=null&&Extension.isNotNullOrEmpty(dto.getDetectDto().getDensenetModelVersion())) { 
                row.createCell(1).setCellValue(new HSSFRichTextString(dto.getDetectDto().getDensenetModelVersion()));
              }
              if (Extension.isNotNullOrEmpty(dto.getLaryngoscopePhotoUrl())) { 
                row.createCell(2).setCellValue(new HSSFRichTextString(dto.getLaryngoscopePhotoUrl()));
              }
              if (Extension.isNotNullOrEmpty(dto.getPhotoDesc())) { 
                row.createCell(3).setCellValue(new HSSFRichTextString(dto.getPhotoDesc()));
              }
              if (dto.getAuditStatus() != null) { 
                row.createCell(4).setCellValue(new HSSFRichTextString(dto.getAuditStatus()?"是":"否"));
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
