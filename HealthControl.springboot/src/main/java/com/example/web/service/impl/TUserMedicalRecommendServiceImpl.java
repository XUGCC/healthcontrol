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
 * 用户就医推荐记录功能实现类
 */
@Service
public class TUserMedicalRecommendServiceImpl extends ServiceImpl<TUserMedicalRecommendMapper, TUserMedicalRecommend> implements TUserMedicalRecommendService {

	 /**
     * 操作数据库AppUser表mapper对象
     */
    @Autowired
    private AppUserMapper AppUserMapper;
    /**
     * 操作数据库的TUserMedicalRecommend表mapper对象
     */
    @Autowired
    private TUserMedicalRecommendMapper TUserMedicalRecommendMapper;
    @Autowired
    private TAudioScreenRecordMapper  TAudioScreenRecordMapper;                        
    @Autowired
    private TOtolaryngologyHospitalDoctorMapper  TOtolaryngologyHospitalDoctorMapper;                        

  
   /**
     * 构建表查询sql
     */
    private LambdaQueryWrapper<TUserMedicalRecommend> BuilderQuery(TUserMedicalRecommendPagedInput input) {
       //声明一个支持用户就医推荐记录查询的(拉姆达)表达式
        LambdaQueryWrapper<TUserMedicalRecommend> queryWrapper = Wrappers.<TUserMedicalRecommend>lambdaQuery()
                .eq(input.getId() != null && input.getId() != 0, TUserMedicalRecommend::getId, input.getId());
   //如果前端搜索传入查询条件则拼接查询条件

        if (input.getUserId() != null) {
            queryWrapper = queryWrapper.eq(TUserMedicalRecommend::getUserId, input.getUserId());
       	 }

        if (input.getDetectId() != null) {
            queryWrapper = queryWrapper.eq(TUserMedicalRecommend::getDetectId, input.getDetectId());
       	 }

        if (input.getRecommendHospitalId() != null) {
            queryWrapper = queryWrapper.eq(TUserMedicalRecommend::getRecommendHospitalId, input.getRecommendHospitalId());
       	 }
        if (input.getUpdateTimeRange() != null && !input.getUpdateTimeRange().isEmpty()) {
            queryWrapper = queryWrapper.le(TUserMedicalRecommend::getUpdateTime, input.getUpdateTimeRange().get(1));
            queryWrapper = queryWrapper.ge(TUserMedicalRecommend::getUpdateTime, input.getUpdateTimeRange().get(0));
        }
        if (input.getRecommendTimeRange() != null && !input.getRecommendTimeRange().isEmpty()) {
            queryWrapper = queryWrapper.le(TUserMedicalRecommend::getRecommendTime, input.getRecommendTimeRange().get(1));
            queryWrapper = queryWrapper.ge(TUserMedicalRecommend::getRecommendTime, input.getRecommendTimeRange().get(0));
        }
        if (input.getRiskLevel() != null) {
            queryWrapper = queryWrapper.eq(TUserMedicalRecommend::getRiskLevel, input.getRiskLevel());
       	 }
        if (input.getViewStatus() != null) {
            queryWrapper = queryWrapper.eq(TUserMedicalRecommend::getViewStatus, input.getViewStatus());
       	 }
        if (input.getIsDelete() != null) {
            queryWrapper = queryWrapper.eq(TUserMedicalRecommend::getIsDelete, input.getIsDelete());
       	 }
      

 
    
      return queryWrapper;
    }
  
    /**
     * 处理用户就医推荐记录对于的外键数据
     */
   private List<TUserMedicalRecommendDto> DispatchItem(List<TUserMedicalRecommendDto> items) throws InvocationTargetException, IllegalAccessException {
          
       for (TUserMedicalRecommendDto item : items) {           
          	            
           //查询出关联的TAudioScreenRecord表信息           
            TAudioScreenRecord  DetectEntity= TAudioScreenRecordMapper.selectById(item.getDetectId());
            item.setDetectDto(DetectEntity!=null?DetectEntity.MapToDto():new TAudioScreenRecordDto());              
           
          	            
           //查询出关联的TOtolaryngologyHospitalDoctor表信息           
            TOtolaryngologyHospitalDoctor  RecommendHospitalEntity= TOtolaryngologyHospitalDoctorMapper.selectById(item.getRecommendHospitalId());
            item.setRecommendHospitalDto(RecommendHospitalEntity!=null?RecommendHospitalEntity.MapToDto():new TOtolaryngologyHospitalDoctorDto());              
           
          	            
           //查询出关联的AppUser表信息           
            AppUser  UserEntity= AppUserMapper.selectById(item.getUserId());
            item.setUserDto(UserEntity!=null?UserEntity.MapToDto():new AppUserDto());              
       }
       
     return items; 
   }
  
    /**
     * 用户就医推荐记录分页查询
     */
    @SneakyThrows
    @Override
    public PagedResult<TUserMedicalRecommendDto> List(TUserMedicalRecommendPagedInput input) {
			//构建where条件+排序
        LambdaQueryWrapper<TUserMedicalRecommend> queryWrapper = BuilderQuery(input);
        // 动态排序处理
        if (input.getSortItem() != null) {
            // 根据字段名动态排序
            queryWrapper.last("ORDER BY " + input.getSortItem().getFieldName()
                    + (input.getSortItem().getIsAsc() ? " ASC" : " DESC"));
        } else {
            // 默认按创建时间从大到小排序
            queryWrapper = queryWrapper.orderByDesc(TUserMedicalRecommend::getCreationTime);
        }

        //构建一个分页查询的model
        Page<TUserMedicalRecommend> page = new Page<>(input.getPage(), input.getLimit());
         //从数据库进行分页查询获取用户就医推荐记录数据
        IPage<TUserMedicalRecommend> pageRecords= TUserMedicalRecommendMapper.selectPage(page, queryWrapper);
        //获取所有满足条件的数据行数
        Long totalCount= TUserMedicalRecommendMapper.selectCount(queryWrapper);
        //把TUserMedicalRecommend实体转换成TUserMedicalRecommend传输模型
        List<TUserMedicalRecommendDto> items= Extension.copyBeanList(pageRecords.getRecords(),TUserMedicalRecommendDto.class);

		   DispatchItem(items);
        //返回一个分页结构给前端
        return PagedResult.GetInstance(items,totalCount);

    }
  
    /**
     * 单个用户就医推荐记录查询
     */
    @SneakyThrows
    @Override
    public TUserMedicalRecommendDto Get(TUserMedicalRecommendPagedInput input) {
       if(input.getId()==null)
        {
         return new TUserMedicalRecommendDto();
        }
      
       PagedResult<TUserMedicalRecommendDto> pagedResult = List(input);
        return pagedResult.getItems().stream().findFirst().orElse(new TUserMedicalRecommendDto()); 
    }

    /**
     *用户就医推荐记录创建或者修改
     */
    @SneakyThrows
    @Override
    public TUserMedicalRecommendDto CreateOrEdit(TUserMedicalRecommendDto input) {
        //声明一个用户就医推荐记录实体
        TUserMedicalRecommend TUserMedicalRecommend=input.MapToEntity();  
        //调用数据库的增加或者修改方法
        saveOrUpdate(TUserMedicalRecommend);
        //把传输模型返回给前端
        return TUserMedicalRecommend.MapToDto();
    }
    /**
     * 用户就医推荐记录删除
     */
    @Override
    public void Delete(IdInput input) {
        TUserMedicalRecommend entity = TUserMedicalRecommendMapper.selectById(input.getId());
        TUserMedicalRecommendMapper.deleteById(entity);
    }

    /**
     * 用户就医推荐记录批量删除
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
     * 用户就医推荐记录导出
     */
    @Override
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        TUserMedicalRecommendPagedInput input = mapper.readValue(query, TUserMedicalRecommendPagedInput.class);
        List<TUserMedicalRecommendDto> items =List(input).getItems();
        //声明一个工作簿
        HSSFWorkbook workbook = new HSSFWorkbook();
        //生成一个表格，设置表格名称为"用户就医推荐记录"
        HSSFSheet sheet = workbook.createSheet("用户就医推荐记录表");
        //设置表格列宽度为10个字节
        sheet.setDefaultColumnWidth(30);
        //创建标题的显示样式
        HSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.YELLOW.index);
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        //创建第一行表头
        HSSFRow headrow = sheet.createRow(0);
        //表头数据
        String[] header = {"名称" ,"孪生Densenet模型版本" ,"推荐依据风险等级" ,"医院名称" ,"查看状态" ,"软删除标记" ,};   
        //遍历添加表头(下面模拟遍历用户就医推荐记录，也是同样的操作过程)
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
            TUserMedicalRecommendDto dto = items.get(i);
            //创建一行
            HSSFRow row = sheet.createRow(i + 1);    
   
              if (dto.getUserDto()!=null&&Extension.isNotNullOrEmpty(dto.getUserDto().getName())) { 
                row.createCell(0).setCellValue(new HSSFRichTextString(dto.getUserDto().getName()));
              }
              if (dto.getDetectDto()!=null&&Extension.isNotNullOrEmpty(dto.getDetectDto().getDensenetModelVersion())) { 
                row.createCell(1).setCellValue(new HSSFRichTextString(dto.getDetectDto().getDensenetModelVersion()));
              }
              if (dto.getRiskLevel() != null) { 
                row.createCell(2).setCellValue(new HSSFRichTextString(dto.getRiskLevel()?"是":"否"));
              }
              if (dto.getRecommendHospitalDto()!=null&&Extension.isNotNullOrEmpty(dto.getRecommendHospitalDto().getHospitalName())) { 
                row.createCell(3).setCellValue(new HSSFRichTextString(dto.getRecommendHospitalDto().getHospitalName()));
              }
              if (dto.getViewStatus() != null) { 
                row.createCell(4).setCellValue(new HSSFRichTextString(dto.getViewStatus()?"是":"否"));
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
