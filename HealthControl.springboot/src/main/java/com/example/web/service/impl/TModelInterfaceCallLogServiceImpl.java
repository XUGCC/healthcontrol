package com.example.web.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.web.dto.*;
import com.example.web.dto.query.*;
import com.example.web.entity.*;
import com.example.web.dto.admin.ModelInterfaceStatsDtos.ModelInterfaceStatsSummaryInput;
import com.example.web.dto.admin.ModelInterfaceStatsDtos.ModelInterfaceStatsSummaryOutput;
import com.example.web.dto.admin.ModelInterfaceStatsDtos.QualityBucket;
import com.example.web.dto.admin.ModelInterfaceStatsDtos.CallLinkBucket;
import com.example.web.mapper.*;
import com.example.web.service.*;
import com.example.web.tools.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.lang.reflect.InvocationTargetException;
import lombok.SneakyThrows;
import java.io.IOException;
import com.example.web.tools.*;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.springframework.web.bind.annotation.RequestParam;
/**
 * 模型接口调用日志功能实现类
 */
@Service
public class TModelInterfaceCallLogServiceImpl extends ServiceImpl<TModelInterfaceCallLogMapper, TModelInterfaceCallLog> implements TModelInterfaceCallLogService {

    /**
     * 操作数据库的TModelInterfaceCallLog表mapper对象
     */
    @Autowired
    private TModelInterfaceCallLogMapper TModelInterfaceCallLogMapper;
    @Autowired
    private TAudioScreenRecordMapper  TAudioScreenRecordMapper;                        

  
   /**
     * 构建表查询sql
     */
    private LambdaQueryWrapper<TModelInterfaceCallLog> BuilderQuery(TModelInterfaceCallLogPagedInput input) {
       //声明一个支持模型接口调用日志查询的(拉姆达)表达式
        LambdaQueryWrapper<TModelInterfaceCallLog> queryWrapper = Wrappers.<TModelInterfaceCallLog>lambdaQuery()
                .eq(input.getId() != null && input.getId() != 0, TModelInterfaceCallLog::getId, input.getId());
   //如果前端搜索传入查询条件则拼接查询条件
        if (Extension.isNotNullOrEmpty(input.getInputParams())) {
             queryWrapper = queryWrapper.like(TModelInterfaceCallLog::getInputParams, input.getInputParams());
       	 }
        if (Extension.isNotNullOrEmpty(input.getResultSummary())) {
             queryWrapper = queryWrapper.like(TModelInterfaceCallLog::getResultSummary, input.getResultSummary());
       	 }
        if (Extension.isNotNullOrEmpty(input.getFailErrorCode())) {
             queryWrapper = queryWrapper.like(TModelInterfaceCallLog::getFailErrorCode, input.getFailErrorCode());
       	 }
        if (Extension.isNotNullOrEmpty(input.getFailDetailInfo())) {
             queryWrapper = queryWrapper.like(TModelInterfaceCallLog::getFailDetailInfo, input.getFailDetailInfo());
       	 }

        if (input.getDetectId() != null) {
            queryWrapper = queryWrapper.eq(TModelInterfaceCallLog::getDetectId, input.getDetectId());
       	 }
        if (input.getUpdateTimeRange() != null && !input.getUpdateTimeRange().isEmpty()) {
            queryWrapper = queryWrapper.le(TModelInterfaceCallLog::getUpdateTime, input.getUpdateTimeRange().get(1));
            queryWrapper = queryWrapper.ge(TModelInterfaceCallLog::getUpdateTime, input.getUpdateTimeRange().get(0));
        }
        if (input.getCallLink() != null) {
            queryWrapper = queryWrapper.eq(TModelInterfaceCallLog::getCallLink, input.getCallLink());
       	 }
        if (input.getModelInterfaceType() != null) {
            queryWrapper = queryWrapper.eq(TModelInterfaceCallLog::getModelInterfaceType, input.getModelInterfaceType());
       	 }
        if (Extension.isNotNullOrEmpty(input.getModelVersion())) {
            queryWrapper = queryWrapper.like(TModelInterfaceCallLog::getModelVersion, input.getModelVersion());
        }
        if (Extension.isNotNullOrEmpty(input.getServiceName())) {
            queryWrapper = queryWrapper.like(TModelInterfaceCallLog::getServiceName, input.getServiceName());
       	 }
        if (input.getCallStatus() != null) {
            queryWrapper = queryWrapper.eq(TModelInterfaceCallLog::getCallStatus, input.getCallStatus());
       	 }
        if (input.getIsDelete() != null) {
            queryWrapper = queryWrapper.eq(TModelInterfaceCallLog::getIsDelete, input.getIsDelete());
       	 }
      

 
 
     if(Extension.isNotNullOrEmpty(input.getKeyWord()))
        {
			queryWrapper=queryWrapper.and(i->i
          	   .like(TModelInterfaceCallLog::getInputParams,input.getKeyWord()).or()   	 
          	   .like(TModelInterfaceCallLog::getResultSummary,input.getKeyWord()).or()   	 
          	   .like(TModelInterfaceCallLog::getFailErrorCode,input.getKeyWord()).or()   	 
          	   .like(TModelInterfaceCallLog::getFailDetailInfo,input.getKeyWord()).or()   	 
        );
                                       
 		   }
    
      return queryWrapper;
    }
  
    /**
     * 处理模型接口调用日志对于的外键数据
     */
   private List<TModelInterfaceCallLogDto> DispatchItem(List<TModelInterfaceCallLogDto> items) {
          
       for (TModelInterfaceCallLogDto item : items) {           
           //查询出关联的TAudioScreenRecord表信息           
           TAudioScreenRecord detectEntity = TAudioScreenRecordMapper.selectById(item.getDetectId());
           if (detectEntity != null) {
               try {
                   item.setDetectDto(detectEntity.MapToDto());
               } catch (IllegalAccessException | InvocationTargetException e) {
                   item.setDetectDto(new TAudioScreenRecordDto());
               }
           } else {
               item.setDetectDto(new TAudioScreenRecordDto());
           }
       }
       
     return items; 
   }
  
    /**
     * 模型接口调用日志分页查询
     */
    @Override
    public PagedResult<TModelInterfaceCallLogDto> List(TModelInterfaceCallLogPagedInput input) {
			//构建where条件+排序
        LambdaQueryWrapper<TModelInterfaceCallLog> queryWrapper = BuilderQuery(input);
        // 动态排序处理
        if (input.getSortItem() != null) {
            // 根据字段名动态排序
            queryWrapper.last("ORDER BY " + input.getSortItem().getFieldName()
                    + (input.getSortItem().getIsAsc() ? " ASC" : " DESC"));
        } else {
            // 默认按创建时间从大到小排序
            queryWrapper = queryWrapper.orderByDesc(TModelInterfaceCallLog::getCreationTime);
        }

        //构建一个分页查询的model
        Page<TModelInterfaceCallLog> page = new Page<>(input.getPage(), input.getLimit());
         //从数据库进行分页查询获取模型接口调用日志数据
        IPage<TModelInterfaceCallLog> pageRecords= TModelInterfaceCallLogMapper.selectPage(page, queryWrapper);
        //获取所有满足条件的数据行数
        Long totalCount= TModelInterfaceCallLogMapper.selectCount(queryWrapper);
        //把TModelInterfaceCallLog实体转换成TModelInterfaceCallLog传输模型
        List<TModelInterfaceCallLogDto> items= Extension.copyBeanList(pageRecords.getRecords(),TModelInterfaceCallLogDto.class);

		   DispatchItem(items);
        //返回一个分页结构给前端
        return PagedResult.GetInstance(items,totalCount);

    }
  
    /**
     * 单个模型接口调用日志查询
     */
    @SneakyThrows
    @Override
    public TModelInterfaceCallLogDto Get(TModelInterfaceCallLogPagedInput input) {
       if(input.getId()==null)
        {
         return new TModelInterfaceCallLogDto();
        }
      
       PagedResult<TModelInterfaceCallLogDto> pagedResult = List(input);
        return pagedResult.getItems().stream().findFirst().orElse(new TModelInterfaceCallLogDto()); 
    }

    /**
     *模型接口调用日志创建或者修改
     */
    @SneakyThrows
    @Override
    public TModelInterfaceCallLogDto CreateOrEdit(TModelInterfaceCallLogDto input) {
        //声明一个模型接口调用日志实体
        TModelInterfaceCallLog TModelInterfaceCallLog=input.MapToEntity();  
        //调用数据库的增加或者修改方法
        saveOrUpdate(TModelInterfaceCallLog);
        //把传输模型返回给前端
        return TModelInterfaceCallLog.MapToDto();
    }
    /**
     * 模型接口调用日志删除
     */
    @Override
    public void Delete(IdInput input) {
        TModelInterfaceCallLog entity = TModelInterfaceCallLogMapper.selectById(input.getId());
        TModelInterfaceCallLogMapper.deleteById(entity);
    }

    /**
     * 模型接口调用日志批量删除
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
     * 模型接口调用日志导出
     */
    @Override
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException {

        // 这里为了简化依赖与异常处理，不再从 query 反序列化复杂条件，
        // 直接按「不过滤条件，导出全部」的方式处理。
        TModelInterfaceCallLogPagedInput input = new TModelInterfaceCallLogPagedInput();
        input.setPage(Long.valueOf(1));
        input.setLimit(Long.valueOf(Integer.MAX_VALUE));
        List<TModelInterfaceCallLogDto> items = List(input).getItems();
        //声明一个工作簿
        HSSFWorkbook workbook = new HSSFWorkbook();
        //生成一个表格，设置表格名称为"模型接口调用日志"
        HSSFSheet sheet = workbook.createSheet("模型接口调用日志表");
        //设置表格列宽度为10个字节
        sheet.setDefaultColumnWidth(30);
        //创建标题的显示样式
        HSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.YELLOW.index);
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        //创建第一行表头
        HSSFRow headrow = sheet.createRow(0);
        //表头数据
        String[] header = {"孪生Densenet模型版本" ,"调用环节" ,"模型接口类型" ,"输入图谱URL" ,"调用参数" ,"调用耗时" ,"返回结果摘要" ,"调用状态：0=失败，1=成功" ,"失败错误码" ,"失败详细信息" ,"软删除标记" ,};   
        //遍历添加表头(下面模拟遍历模型接口调用日志，也是同样的操作过程)
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
            TModelInterfaceCallLogDto dto = items.get(i);
            //创建一行
            HSSFRow row = sheet.createRow(i + 1);    
   
              if (dto.getDetectDto()!=null&&Extension.isNotNullOrEmpty(dto.getDetectDto().getDensenetModelVersion())) { 
                row.createCell(0).setCellValue(new HSSFRichTextString(dto.getDetectDto().getDensenetModelVersion()));
              }
              if (dto.getCallLink() != null) { 
                row.createCell(1).setCellValue(new HSSFRichTextString(dto.getCallLink()?"是":"否"));
              }
              if (dto.getModelInterfaceType() != null) { 
                row.createCell(2).setCellValue(new HSSFRichTextString(dto.getModelInterfaceType()?"是":"否"));
              }
              if (Extension.isNotNullOrEmpty(dto.getInputSpectrumUrl())) { 
                row.createCell(3).setCellValue(new HSSFRichTextString(dto.getInputSpectrumUrl()));
              }
              if (Extension.isNotNullOrEmpty(dto.getInputParams())) { 
                row.createCell(4).setCellValue(new HSSFRichTextString(dto.getInputParams()));
              }
              if (dto.getCallCost() != null) { 
                row.createCell(5).setCellValue(new HSSFRichTextString(dto.getCallCost()+""));
              }
              if (Extension.isNotNullOrEmpty(dto.getResultSummary())) { 
                row.createCell(6).setCellValue(new HSSFRichTextString(dto.getResultSummary()));
              }
              if (dto.getCallStatus() != null) { 
                row.createCell(7).setCellValue(new HSSFRichTextString(dto.getCallStatus()?"是":"否"));
              }
              if (Extension.isNotNullOrEmpty(dto.getFailErrorCode())) { 
                row.createCell(8).setCellValue(new HSSFRichTextString(dto.getFailErrorCode()));
              }
              if (Extension.isNotNullOrEmpty(dto.getFailDetailInfo())) { 
                row.createCell(9).setCellValue(new HSSFRichTextString(dto.getFailDetailInfo()));
              }
              if (dto.getIsDelete() != null) { 
                row.createCell(10).setCellValue(new HSSFRichTextString(dto.getIsDelete()?"是":"否"));
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
     * 模型接口调用日志统计概要
     */
    @Override
    public ModelInterfaceStatsSummaryOutput StatsSummary(ModelInterfaceStatsSummaryInput input) {
        // 默认统计近 7 天
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6);
        LocalDate endDate = today;
        if (input != null) {
            try {
                if (input.getDateStart() != null && !input.getDateStart().trim().isEmpty()) {
                    startDate = LocalDate.parse(input.getDateStart().trim());
                }
                if (input.getDateEnd() != null && !input.getDateEnd().trim().isEmpty()) {
                    endDate = LocalDate.parse(input.getDateEnd().trim());
                }
            } catch (Exception ignored) {
            }
        }
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        LambdaQueryWrapper<TModelInterfaceCallLog> qw = Wrappers.<TModelInterfaceCallLog>lambdaQuery()
                .between(TModelInterfaceCallLog::getCreationTime, start, end);

        List<TModelInterfaceCallLog> logs = TModelInterfaceCallLogMapper.selectList(qw);
        ModelInterfaceStatsSummaryOutput out = new ModelInterfaceStatsSummaryOutput();
        if (logs == null || logs.isEmpty()) {
            out.setTotalCalls(0L);
            out.setSuccessCalls(0L);
            out.setFailCalls(0L);
            out.setFailRate(0.0);
            out.setAvgCost(0);
            out.setByQualityLevel(new ArrayList<>());
            out.setByCallLink(new ArrayList<>());
            return out;
        }

        long total = logs.size();
        long success = 0;
        long fail = 0;
        long costSum = 0;
        long costCount = 0;

        HashMap<Integer, Long> qualityMap = new HashMap<>();
        HashMap<Integer, List<TModelInterfaceCallLog>> callLinkMap = new HashMap<>();

        for (TModelInterfaceCallLog log : logs) {
            if (Boolean.TRUE.equals(log.getCallStatus())) {
                success++;
            } else {
                fail++;
            }
            if (log.getCallCost() != null) {
                costSum += log.getCallCost();
                costCount++;
            }

            Integer q = log.getQualityLevel();
            if (q != null) {
                qualityMap.put(q, qualityMap.getOrDefault(q, 0L) + 1);
            }

            Integer callLink = null;
            if (log.getCallLink() != null) {
                callLink = Boolean.TRUE.equals(log.getCallLink()) ? 1 : 0;
            }
            callLinkMap.computeIfAbsent(callLink, k -> new ArrayList<>()).add(log);
        }

        int avgCost = costCount > 0 ? (int) (costSum / costCount) : 0;
        double failRate = total > 0 ? (double) fail / (double) total : 0.0;

        out.setTotalCalls(total);
        out.setSuccessCalls(success);
        out.setFailCalls(fail);
        out.setAvgCost(avgCost);
        out.setFailRate(failRate);

        List<QualityBucket> qItems = new ArrayList<>();
        for (Integer k : qualityMap.keySet()) {
            QualityBucket qb = new QualityBucket();
            qb.setQualityLevel(k);
            qb.setCount(qualityMap.get(k));
            qItems.add(qb);
        }
        out.setByQualityLevel(qItems);

        List<CallLinkBucket> clItems = new ArrayList<>();
        for (Integer k : callLinkMap.keySet()) {
            List<TModelInterfaceCallLog> group = callLinkMap.get(k);
            long gTotal = group.size();
            long gFail = group.stream().filter(x -> !Boolean.TRUE.equals(x.getCallStatus())).count();
            long gCostSum = group.stream().filter(x -> x.getCallCost() != null)
                    .mapToLong(x -> x.getCallCost()).sum();
            long gCostCount = group.stream().filter(x -> x.getCallCost() != null).count();
            int gAvg = gCostCount > 0 ? (int) (gCostSum / gCostCount) : 0;

            CallLinkBucket cb = new CallLinkBucket();
            cb.setCallLink(k);
            cb.setCallLinkName(buildCallLinkName(k));
            cb.setTotal(gTotal);
            cb.setFail(gFail);
            cb.setAvgCost(gAvg);
            clItems.add(cb);
        }
        out.setByCallLink(clItems);

        return out;
    }

    /**
     * 根据调用环节编码构造一个简单可读名称
     */
    private String buildCallLinkName(Integer callLink) {
        if (callLink == null) {
            return "其他环节";
        }
        if (callLink == 0) {
            return "其它/未标记";
        }
        if (callLink == 1) {
            return "主模型推理";
        }
        if (callLink == 2) {
            return "前处理/特征提取";
        }
        if (callLink == 3) {
            return "后处理/结果解释";
        }
        return "环节#" + callLink;
    }
}
