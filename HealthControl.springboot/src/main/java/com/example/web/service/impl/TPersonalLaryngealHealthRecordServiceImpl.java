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
import java.time.LocalDateTime;
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
 * 个人喉部健康档案功能实现类
 */
@Service
public class TPersonalLaryngealHealthRecordServiceImpl extends ServiceImpl<TPersonalLaryngealHealthRecordMapper, TPersonalLaryngealHealthRecord> implements TPersonalLaryngealHealthRecordService {

	 /**
     * 操作数据库AppUser表mapper对象
     */
    @Autowired
    private AppUserMapper AppUserMapper;
    /**
     * 操作数据库的TPersonalLaryngealHealthRecord表mapper对象
     */
    @Autowired
    private TPersonalLaryngealHealthRecordMapper TPersonalLaryngealHealthRecordMapper;
    @Autowired
    private TAudioScreenRecordMapper  TAudioScreenRecordMapper;                        
    
    @Autowired
    private HealthTrendService healthTrendService;
    
    @Autowired
    private TAudioScreenRecordService audioScreenRecordService;

  
   /**
     * 构建表查询sql
     */
    private LambdaQueryWrapper<TPersonalLaryngealHealthRecord> BuilderQuery(TPersonalLaryngealHealthRecordPagedInput input) {
       //声明一个支持个人喉部健康档案查询的(拉姆达)表达式
        LambdaQueryWrapper<TPersonalLaryngealHealthRecord> queryWrapper = Wrappers.<TPersonalLaryngealHealthRecord>lambdaQuery()
                .eq(input.getId() != null && input.getId() != 0, TPersonalLaryngealHealthRecord::getId, input.getId());
   //如果前端搜索传入查询条件则拼接查询条件
        if (Extension.isNotNullOrEmpty(input.getRecentSymptom())) {
             queryWrapper = queryWrapper.like(TPersonalLaryngealHealthRecord::getRecentSymptom, input.getRecentSymptom());
       	 }
        if (Extension.isNotNullOrEmpty(input.getSymptomDuration())) {
             queryWrapper = queryWrapper.like(TPersonalLaryngealHealthRecord::getSymptomDuration, input.getSymptomDuration());
       	 }
        if (Extension.isNotNullOrEmpty(input.getDailyVoiceCare())) {
             queryWrapper = queryWrapper.like(TPersonalLaryngealHealthRecord::getDailyVoiceCare, input.getDailyVoiceCare());
       	 }
        if (Extension.isNotNullOrEmpty(input.getHealthTrendTag())) {
             queryWrapper = queryWrapper.like(TPersonalLaryngealHealthRecord::getHealthTrendTag, input.getHealthTrendTag());
       	 }

        if (input.getUserId() != null) {
            queryWrapper = queryWrapper.eq(TPersonalLaryngealHealthRecord::getUserId, input.getUserId());
       	 }

        if (input.getLatestDetectId() != null) {
            queryWrapper = queryWrapper.eq(TPersonalLaryngealHealthRecord::getLatestDetectId, input.getLatestDetectId());
       	 }
        if (input.getLastScreenTimeRange() != null && !input.getLastScreenTimeRange().isEmpty()) {
            queryWrapper = queryWrapper.le(TPersonalLaryngealHealthRecord::getLastScreenTime, input.getLastScreenTimeRange().get(1));
            queryWrapper = queryWrapper.ge(TPersonalLaryngealHealthRecord::getLastScreenTime, input.getLastScreenTimeRange().get(0));
        }
        if (input.getRiskAssessmentLevel() != null) {
            queryWrapper = queryWrapper.eq(TPersonalLaryngealHealthRecord::getRiskAssessmentLevel, input.getRiskAssessmentLevel());
       	 }
        if (input.getIsDelete() != null) {
            queryWrapper = queryWrapper.eq(TPersonalLaryngealHealthRecord::getIsDelete, input.getIsDelete());
       	 }
      

 
 
     if(Extension.isNotNullOrEmpty(input.getKeyWord()))
        {
			queryWrapper=queryWrapper.and(i->i
          	   .like(TPersonalLaryngealHealthRecord::getRecentSymptom,input.getKeyWord()).or()   	 
          	   .like(TPersonalLaryngealHealthRecord::getSymptomDuration,input.getKeyWord()).or()   	 
          	   .like(TPersonalLaryngealHealthRecord::getDailyVoiceCare,input.getKeyWord()).or()   	 
          	   .like(TPersonalLaryngealHealthRecord::getHealthTrendTag,input.getKeyWord()).or()   	 
        );
                                       
 		   }
    
      return queryWrapper;
    }
  
    /**
     * 处理个人喉部健康档案对于的外键数据
     */
   private List<TPersonalLaryngealHealthRecordDto> DispatchItem(List<TPersonalLaryngealHealthRecordDto> items) throws InvocationTargetException, IllegalAccessException {
          
       for (TPersonalLaryngealHealthRecordDto item : items) {           
          	            
           //查询出关联的AppUser表信息           
            AppUser  UserEntity= AppUserMapper.selectById(item.getUserId());
            item.setUserDto(UserEntity!=null?UserEntity.MapToDto():new AppUserDto());              
           
          	            
           //查询出关联的TAudioScreenRecord表信息           
            TAudioScreenRecord  LatestDetectEntity= TAudioScreenRecordMapper.selectById(item.getLatestDetectId());
            item.setLatestDetectDto(LatestDetectEntity!=null?LatestDetectEntity.MapToDto():new TAudioScreenRecordDto());              
       }
       
     return items; 
   }
  
    /**
     * 个人喉部健康档案分页查询
     */
    @SneakyThrows
    @Override
    public PagedResult<TPersonalLaryngealHealthRecordDto> List(TPersonalLaryngealHealthRecordPagedInput input) {
			//构建where条件+排序
        LambdaQueryWrapper<TPersonalLaryngealHealthRecord> queryWrapper = BuilderQuery(input);
        // 动态排序处理
        if (input.getSortItem() != null) {
            // 根据字段名动态排序
            queryWrapper.last("ORDER BY " + input.getSortItem().getFieldName()
                    + (input.getSortItem().getIsAsc() ? " ASC" : " DESC"));
        } else {
            // 默认按创建时间从大到小排序
            queryWrapper = queryWrapper.orderByDesc(TPersonalLaryngealHealthRecord::getCreationTime);
        }

        //构建一个分页查询的model
        Page<TPersonalLaryngealHealthRecord> page = new Page<>(input.getPage(), input.getLimit());
         //从数据库进行分页查询获取个人喉部健康档案数据
        IPage<TPersonalLaryngealHealthRecord> pageRecords= TPersonalLaryngealHealthRecordMapper.selectPage(page, queryWrapper);
        //获取所有满足条件的数据行数
        Long totalCount= TPersonalLaryngealHealthRecordMapper.selectCount(queryWrapper);
        //把TPersonalLaryngealHealthRecord实体转换成TPersonalLaryngealHealthRecord传输模型
        List<TPersonalLaryngealHealthRecordDto> items= Extension.copyBeanList(pageRecords.getRecords(),TPersonalLaryngealHealthRecordDto.class);

		   DispatchItem(items);
        //返回一个分页结构给前端
        return PagedResult.GetInstance(items,totalCount);

    }
  
    /**
     * 单个个人喉部健康档案查询
     */
    @SneakyThrows
    @Override
    public TPersonalLaryngealHealthRecordDto Get(TPersonalLaryngealHealthRecordPagedInput input) {
       // 如果提供了UserId，优先使用GetByUserId方法
       if (input.getUserId() != null && input.getUserId() > 0) {
           return GetByUserId(input.getUserId());
       }
       
       if(input.getId()==null)
        {
         return new TPersonalLaryngealHealthRecordDto();
        }
      
       PagedResult<TPersonalLaryngealHealthRecordDto> pagedResult = List(input);
        return pagedResult.getItems().stream().findFirst().orElse(new TPersonalLaryngealHealthRecordDto()); 
    }

    /**
     *个人喉部健康档案创建或者修改
     */
    @SneakyThrows
    @Override
    public TPersonalLaryngealHealthRecordDto CreateOrEdit(TPersonalLaryngealHealthRecordDto input) {
        //声明一个个人喉部健康档案实体
        TPersonalLaryngealHealthRecord TPersonalLaryngealHealthRecord=input.MapToEntity();  
        //调用数据库的增加或者修改方法
        saveOrUpdate(TPersonalLaryngealHealthRecord);
        //把传输模型返回给前端
        return TPersonalLaryngealHealthRecord.MapToDto();
    }
    /**
     * 个人喉部健康档案删除
     */
    @Override
    public void Delete(IdInput input) {
        TPersonalLaryngealHealthRecord entity = TPersonalLaryngealHealthRecordMapper.selectById(input.getId());
        TPersonalLaryngealHealthRecordMapper.deleteById(entity);
    }

    /**
     * 个人喉部健康档案批量删除
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
     * 个人喉部健康档案导出
     */
    @Override
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        TPersonalLaryngealHealthRecordPagedInput input = mapper.readValue(query, TPersonalLaryngealHealthRecordPagedInput.class);
        List<TPersonalLaryngealHealthRecordDto> items =List(input).getItems();
        //声明一个工作簿
        HSSFWorkbook workbook = new HSSFWorkbook();
        //生成一个表格，设置表格名称为"个人喉部健康档案"
        HSSFSheet sheet = workbook.createSheet("个人喉部健康档案表");
        //设置表格列宽度为10个字节
        sheet.setDefaultColumnWidth(30);
        //创建标题的显示样式
        HSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.YELLOW.index);
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        //创建第一行表头
        HSSFRow headrow = sheet.createRow(0);
        //表头数据
        String[] header = {"名称" ,"近期主要不适症状" ,"症状持续时间：如1周/1月" ,"日常护嗓习惯" ,"健康趋势标签：如好转/恶化/稳定" ,"孪生Densenet模型版本" ,"风险评估等级：0=低，1=中，2=高" ,"软删除标记：0=未删除，1=已删除" ,};   
        //遍历添加表头(下面模拟遍历个人喉部健康档案，也是同样的操作过程)
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
            TPersonalLaryngealHealthRecordDto dto = items.get(i);
            //创建一行
            HSSFRow row = sheet.createRow(i + 1);    
   
              if (dto.getUserDto()!=null&&Extension.isNotNullOrEmpty(dto.getUserDto().getName())) { 
                row.createCell(0).setCellValue(new HSSFRichTextString(dto.getUserDto().getName()));
              }
              if (Extension.isNotNullOrEmpty(dto.getRecentSymptom())) { 
                row.createCell(1).setCellValue(new HSSFRichTextString(dto.getRecentSymptom()));
              }
              if (Extension.isNotNullOrEmpty(dto.getSymptomDuration())) { 
                row.createCell(2).setCellValue(new HSSFRichTextString(dto.getSymptomDuration()));
              }
              if (Extension.isNotNullOrEmpty(dto.getDailyVoiceCare())) { 
                row.createCell(3).setCellValue(new HSSFRichTextString(dto.getDailyVoiceCare()));
              }
              if (Extension.isNotNullOrEmpty(dto.getHealthTrendTag())) { 
                row.createCell(4).setCellValue(new HSSFRichTextString(dto.getHealthTrendTag()));
              }
              if (dto.getLatestDetectDto()!=null&&Extension.isNotNullOrEmpty(dto.getLatestDetectDto().getDensenetModelVersion())) { 
                row.createCell(5).setCellValue(new HSSFRichTextString(dto.getLatestDetectDto().getDensenetModelVersion()));
              }
              if (dto.getRiskAssessmentLevel() != null) { 
                String riskLevelText = "";
                if (dto.getRiskAssessmentLevel() == 0) {
                    riskLevelText = "低风险";
                } else if (dto.getRiskAssessmentLevel() == 1) {
                    riskLevelText = "中风险";
                } else if (dto.getRiskAssessmentLevel() == 2) {
                    riskLevelText = "高风险";
                }
                row.createCell(6).setCellValue(new HSSFRichTextString(riskLevelText));
              }
              if (dto.getIsDelete() != null) { 
                row.createCell(7).setCellValue(new HSSFRichTextString(dto.getIsDelete()?"是":"否"));
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
    
    /**
     * 根据UserId获取健康档案（用于前端健康档案页）
     */
    @SneakyThrows
    public TPersonalLaryngealHealthRecordDto GetByUserId(Integer userId) {
        if (userId == null || userId <= 0) {
            return null;
        }
        
        LambdaQueryWrapper<TPersonalLaryngealHealthRecord> queryWrapper = Wrappers
            .<TPersonalLaryngealHealthRecord>lambdaQuery()
            .eq(TPersonalLaryngealHealthRecord::getUserId, userId)
            .eq(TPersonalLaryngealHealthRecord::getIsDelete, false)
            .orderByDesc(TPersonalLaryngealHealthRecord::getCreationTime)
            .last("LIMIT 1");
        
        TPersonalLaryngealHealthRecord entity = getOne(queryWrapper);
        if (entity == null) {
            return null;
        }
        
        TPersonalLaryngealHealthRecordDto dto = entity.MapToDto();
        
        // 关联查询最新检测记录
        if (dto.getLatestDetectId() != null && dto.getLatestDetectId() > 0) {
            TAudioScreenRecordPagedInput detectInput = new TAudioScreenRecordPagedInput();
            detectInput.setId(dto.getLatestDetectId());
            detectInput.setPage(1L);
            detectInput.setLimit(1L);
            TAudioScreenRecordDto detectDto = audioScreenRecordService.Get(detectInput);
            dto.setLatestDetectDto(detectDto);
        }
        
        return dto;
    }
    
    /**
     * 自动更新健康档案（检测完成后调用）
     */
    public void AutoUpdateAfterDetect(Integer userId, Integer detectId) {
        try {
            // 1. 查询或创建健康档案
            TPersonalLaryngealHealthRecordDto healthRecord = GetByUserId(userId);
            boolean isNew = (healthRecord == null);
            if (isNew) {
                healthRecord = new TPersonalLaryngealHealthRecordDto();
                healthRecord.setUserId(userId);
                healthRecord.setCreationTime(LocalDateTime.now());
                healthRecord.setIsDelete(false);
            }
            
            // 2. 更新末次自查时间和最新检测ID
            healthRecord.setLastScreenTime(LocalDateTime.now());
            healthRecord.setLatestDetectId(detectId);
            
            // 3. 计算健康趋势
            String trendTag = healthTrendService.calculateHealthTrend(userId);
            healthRecord.setHealthTrendTag(trendTag);
            
            // 4. 计算风险等级
            Integer riskLevel = healthTrendService.calculateRiskLevel(userId);
            healthRecord.setRiskAssessmentLevel(riskLevel);
            
            // 5. 保存档案
            CreateOrEdit(healthRecord);
            System.out.println("健康档案" + (isNew ? "创建" : "更新") + "成功: userId=" + userId + ", detectId=" + detectId);
        } catch (Exception e) {
            // 档案更新失败不影响检测结果返回，但需要记录详细错误
            System.err.println("自动更新健康档案失败: userId=" + userId + ", detectId=" + detectId);
            e.printStackTrace();
        }
    }
}
