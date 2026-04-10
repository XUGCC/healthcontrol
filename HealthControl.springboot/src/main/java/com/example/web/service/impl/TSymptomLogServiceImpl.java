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
 * 症状日志功能实现类
 */
@Service
public class TSymptomLogServiceImpl extends ServiceImpl<TSymptomLogMapper, TSymptomLog> implements TSymptomLogService {

	 /**
     * 操作数据库AppUser表mapper对象
     */
    @Autowired
    private AppUserMapper AppUserMapper;
    /**
     * 操作数据库的TSymptomLog表mapper对象
     */
    @Autowired
    private TSymptomLogMapper TSymptomLogMapper;
    @Autowired
    private TAudioScreenRecordMapper  TAudioScreenRecordMapper;
    
    @Autowired
    private HealthTrendService healthTrendService;
    
    @Autowired
    private TPersonalLaryngealHealthRecordService personalLaryngealHealthRecordService;

  
   /**
     * 构建表查询sql
     */
    private LambdaQueryWrapper<TSymptomLog> BuilderQuery(TSymptomLogPagedInput input) {
       //声明一个支持症状日志查询的(拉姆达)表达式
        LambdaQueryWrapper<TSymptomLog> queryWrapper = Wrappers.<TSymptomLog>lambdaQuery()
                .eq(input.getId() != null && input.getId() != 0, TSymptomLog::getId, input.getId());
   //如果前端搜索传入查询条件则拼接查询条件
        if (Extension.isNotNullOrEmpty(input.getSymptomDuration())) {
             queryWrapper = queryWrapper.like(TSymptomLog::getSymptomDuration, input.getSymptomDuration());
       	 }
        if (Extension.isNotNullOrEmpty(input.getRemark())) {
             queryWrapper = queryWrapper.like(TSymptomLog::getRemark, input.getRemark());
       	 }

        if (input.getUserId() != null) {
            queryWrapper = queryWrapper.eq(TSymptomLog::getUserId, input.getUserId());
       	 }

        if (input.getDetectId() != null) {
            queryWrapper = queryWrapper.eq(TSymptomLog::getDetectId, input.getDetectId());
       	 }
        if (input.getSymptomOccurTimeRange() != null && !input.getSymptomOccurTimeRange().isEmpty()) {
            queryWrapper = queryWrapper.le(TSymptomLog::getSymptomOccurTime, input.getSymptomOccurTimeRange().get(1));
            queryWrapper = queryWrapper.ge(TSymptomLog::getSymptomOccurTime, input.getSymptomOccurTimeRange().get(0));
        }
        if (input.getSymptomType() != null) {
            queryWrapper = queryWrapper.eq(TSymptomLog::getSymptomType, input.getSymptomType());
       	 }
        if (input.getSymptomLevel() != null) {
            queryWrapper = queryWrapper.eq(TSymptomLog::getSymptomLevel, input.getSymptomLevel());
       	 }

        // 默认仅查询未删除数据；只有当显式传入 IsDelete 时才按照指定值过滤
        if (input.getIsDelete() != null) {
            queryWrapper = queryWrapper.eq(TSymptomLog::getIsDelete, input.getIsDelete());
        } else {
            queryWrapper = queryWrapper
                    .and(w -> w.isNull(TSymptomLog::getIsDelete).or().eq(TSymptomLog::getIsDelete, false));
       	 }
      

 
 
     if(Extension.isNotNullOrEmpty(input.getKeyWord()))
        {
			queryWrapper=queryWrapper.and(i->i
          	   .like(TSymptomLog::getSymptomDuration,input.getKeyWord()).or()   	 
          	   .like(TSymptomLog::getRemark,input.getKeyWord()).or()   	 
        );
                                       
 		   }
    
      return queryWrapper;
    }
  
    /**
     * 处理症状日志对于的外键数据
     */
   private List<TSymptomLogDto> DispatchItem(List<TSymptomLogDto> items) throws InvocationTargetException, IllegalAccessException {
          
       for (TSymptomLogDto item : items) {           
          	            
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
     * 症状日志分页查询
     */
    @SneakyThrows
    @Override
    public PagedResult<TSymptomLogDto> List(TSymptomLogPagedInput input) {
			//构建where条件+排序
        LambdaQueryWrapper<TSymptomLog> queryWrapper = BuilderQuery(input);
        // 动态排序处理
        if (input.getSortItem() != null) {
            // 根据字段名动态排序
            queryWrapper.last("ORDER BY " + input.getSortItem().getFieldName()
                    + (input.getSortItem().getIsAsc() ? " ASC" : " DESC"));
        } else {
            // 默认按创建时间从大到小排序
            queryWrapper = queryWrapper.orderByDesc(TSymptomLog::getCreationTime);
        }

        //构建一个分页查询的model
        Page<TSymptomLog> page = new Page<>(input.getPage(), input.getLimit());
         //从数据库进行分页查询获取症状日志数据
        IPage<TSymptomLog> pageRecords= TSymptomLogMapper.selectPage(page, queryWrapper);
        //获取所有满足条件的数据行数
        Long totalCount= TSymptomLogMapper.selectCount(queryWrapper);
        //把TSymptomLog实体转换成TSymptomLog传输模型
        List<TSymptomLogDto> items= Extension.copyBeanList(pageRecords.getRecords(),TSymptomLogDto.class);

		   DispatchItem(items);
        //返回一个分页结构给前端
        return PagedResult.GetInstance(items,totalCount);

    }
  
    /**
     * 单个症状日志查询
     */
    @SneakyThrows
    @Override
    public TSymptomLogDto Get(TSymptomLogPagedInput input) {
       if(input.getId()==null)
        {
         return new TSymptomLogDto();
        }
      
       PagedResult<TSymptomLogDto> pagedResult = List(input);
        return pagedResult.getItems().stream().findFirst().orElse(new TSymptomLogDto()); 
    }

    /**
     *症状日志创建或者修改
     */
    @SneakyThrows
    @Override
    public TSymptomLogDto CreateOrEdit(TSymptomLogDto input) {
        try {
            // 打印输入数据用于调试
            System.out.println("创建/编辑症状记录 - 输入数据:");
            System.out.println("  UserId: " + input.getUserId());
            System.out.println("  SymptomType: " + input.getSymptomType() + " (类型: " + (input.getSymptomType() != null ? input.getSymptomType().getClass().getName() : "null") + ")");
            System.out.println("  SymptomLevel: " + input.getSymptomLevel() + " (类型: " + (input.getSymptomLevel() != null ? input.getSymptomLevel().getClass().getName() : "null") + ")");
            System.out.println("  SymptomDuration: " + input.getSymptomDuration());
            System.out.println("  SymptomOccurTime: " + input.getSymptomOccurTime());
            
            //声明一个症状日志实体
            TSymptomLog TSymptomLog=input.MapToEntity();
            
            // 确保必要字段已设置
            if (TSymptomLog.getIsDelete() == null) {
                TSymptomLog.setIsDelete(false);
            }
            if (TSymptomLog.getId() == null && TSymptomLog.getCreationTime() == null) {
                TSymptomLog.setCreationTime(java.time.LocalDateTime.now());
            }
            
            System.out.println("保存前实体数据:");
            System.out.println("  SymptomType: " + TSymptomLog.getSymptomType() + " (类型: " + (TSymptomLog.getSymptomType() != null ? TSymptomLog.getSymptomType().getClass().getName() : "null") + ")");
            System.out.println("  SymptomLevel: " + TSymptomLog.getSymptomLevel() + " (类型: " + (TSymptomLog.getSymptomLevel() != null ? TSymptomLog.getSymptomLevel().getClass().getName() : "null") + ")");
            
            //调用数据库的增加或者修改方法
            saveOrUpdate(TSymptomLog);
            
            System.out.println("保存成功，ID: " + TSymptomLog.getId());
            
            //把传输模型返回给前端
            TSymptomLogDto result = TSymptomLog.MapToDto();
            
            // 新增：触发健康档案风险等级重新评定
            if (input.getUserId() != null && input.getUserId() > 0) {
                try {
                    TPersonalLaryngealHealthRecordPagedInput healthInput = new TPersonalLaryngealHealthRecordPagedInput();
                    healthInput.setUserId(input.getUserId());
                    healthInput.setPage(1L);
                    healthInput.setLimit(1L);
                    TPersonalLaryngealHealthRecordDto healthRecord = personalLaryngealHealthRecordService.Get(healthInput);
                    
                    if (healthRecord != null) {
                        // 重新计算风险等级
                        Integer riskLevel = healthTrendService.calculateRiskLevel(input.getUserId());
                        healthRecord.setRiskAssessmentLevel(riskLevel);
                        personalLaryngealHealthRecordService.CreateOrEdit(healthRecord);
                    }
                } catch (Exception e) {
                    // 风险等级更新失败不影响症状记录保存
                    System.err.println("健康档案风险等级更新失败: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            return result;
        } catch (Exception e) {
            System.err.println("创建/编辑症状记录失败: " + e.getMessage());
            e.printStackTrace();
            throw e; // 重新抛出异常，让全局异常处理器处理
        }
    }
    /**
     * 症状日志删除
     */
    @Override
    public void Delete(IdInput input) {
        // 先获取要删除的记录，以便后续重新评定风险等级
        TSymptomLog entity = TSymptomLogMapper.selectById(input.getId());
        Integer userId = entity != null ? entity.getUserId() : null;
        
        TSymptomLogMapper.deleteById(entity);
        
        // 删除后，触发健康档案风险等级重新评定
        if (userId != null && userId > 0) {
            try {
                TPersonalLaryngealHealthRecordPagedInput healthInput = new TPersonalLaryngealHealthRecordPagedInput();
                healthInput.setUserId(userId);
                healthInput.setPage(1L);
                healthInput.setLimit(1L);
                TPersonalLaryngealHealthRecordDto healthRecord = personalLaryngealHealthRecordService.Get(healthInput);
                
                if (healthRecord != null) {
                    // 重新计算风险等级
                    Integer riskLevel = healthTrendService.calculateRiskLevel(userId);
                    healthRecord.setRiskAssessmentLevel(riskLevel);
                    personalLaryngealHealthRecordService.CreateOrEdit(healthRecord);
                }
            } catch (Exception e) {
                // 风险等级更新失败不影响删除操作
                e.printStackTrace();
            }
        }
    }

    /**
     * 症状日志批量删除
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
     * 症状日志导出
     */
    @Override
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        TSymptomLogPagedInput input = mapper.readValue(query, TSymptomLogPagedInput.class);
        List<TSymptomLogDto> items =List(input).getItems();
        //声明一个工作簿
        HSSFWorkbook workbook = new HSSFWorkbook();
        //生成一个表格，设置表格名称为"症状日志"
        HSSFSheet sheet = workbook.createSheet("症状日志表");
        //设置表格列宽度为10个字节
        sheet.setDefaultColumnWidth(30);
        //创建标题的显示样式
        HSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.YELLOW.index);
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        //创建第一行表头
        HSSFRow headrow = sheet.createRow(0);
        //表头数据
        String[] header = {"名称" ,"孪生Densenet模型版本" ,"症状类型" ,"症状轻重" ,"症状持续时长" ,"症状备注" ,"软删除标记" ,};   
        //遍历添加表头(下面模拟遍历症状日志，也是同样的操作过程)
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
            TSymptomLogDto dto = items.get(i);
            //创建一行
            HSSFRow row = sheet.createRow(i + 1);    
   
              if (dto.getUserDto()!=null&&Extension.isNotNullOrEmpty(dto.getUserDto().getName())) { 
                row.createCell(0).setCellValue(new HSSFRichTextString(dto.getUserDto().getName()));
              }
              if (dto.getDetectDto()!=null&&Extension.isNotNullOrEmpty(dto.getDetectDto().getDensenetModelVersion())) { 
                row.createCell(1).setCellValue(new HSSFRichTextString(dto.getDetectDto().getDensenetModelVersion()));
              }
              if (dto.getSymptomType() != null && !dto.getSymptomType().isEmpty()) { 
                row.createCell(2).setCellValue(new HSSFRichTextString(dto.getSymptomType()));
              }
              if (dto.getSymptomLevel() != null) { 
                String levelText = "";
                if (dto.getSymptomLevel() == 1) {
                    levelText = "轻度";
                } else if (dto.getSymptomLevel() == 2) {
                    levelText = "中度";
                } else if (dto.getSymptomLevel() == 3) {
                    levelText = "重度";
                }
                row.createCell(3).setCellValue(new HSSFRichTextString(levelText));
              }
              if (Extension.isNotNullOrEmpty(dto.getSymptomDuration())) { 
                row.createCell(4).setCellValue(new HSSFRichTextString(dto.getSymptomDuration()));
              }
              if (Extension.isNotNullOrEmpty(dto.getRemark())) { 
                row.createCell(5).setCellValue(new HSSFRichTextString(dto.getRemark()));
              }
              if (dto.getIsDelete() != null) { 
                row.createCell(6).setCellValue(new HSSFRichTextString(dto.getIsDelete()?"是":"否"));
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
