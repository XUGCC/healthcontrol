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
import com.example.web.tools.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import lombok.SneakyThrows;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import com.example.web.tools.*;
import java.text.DecimalFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.springframework.web.bind.annotation.RequestParam;
/**
 * 音频自查记录功能实现类
 */
@Service
public class TAudioScreenRecordServiceImpl extends ServiceImpl<TAudioScreenRecordMapper, TAudioScreenRecord> implements TAudioScreenRecordService {

	 /**
     * 操作数据库AppUser表mapper对象
     */
    // 预留：后续可用于回写用户统计信息
    // @Autowired
    // private AppUserMapper AppUserMapper;
    /**
     * 操作数据库的TAudioScreenRecord表mapper对象
     */
    @Autowired
    private TAudioScreenRecordMapper TAudioScreenRecordMapper;
    @Autowired
    private TLaryngoscopePhotoMapper  TLaryngoscopePhotoMapper;                        

  
   /**
     * 构建表查询sql
     */
    private LambdaQueryWrapper<TAudioScreenRecord> BuilderQuery(TAudioScreenRecordPagedInput input) {
       //声明一个支持音频自查记录查询的(拉姆达)表达式
        LambdaQueryWrapper<TAudioScreenRecord> queryWrapper = Wrappers.<TAudioScreenRecord>lambdaQuery()
                .eq(input.getId() != null && input.getId() != 0, TAudioScreenRecord::getId, input.getId())
                // 按用户过滤：每个用户只能看到自己的检测记录
                .eq(input.getUserId() != null && input.getUserId() != 0, TAudioScreenRecord::getUserId, input.getUserId());
   //如果前端搜索传入查询条件则拼接查询条件
        if (Extension.isNotNullOrEmpty(input.getMfccSpectrumResolution())) {
             queryWrapper = queryWrapper.like(TAudioScreenRecord::getMfccSpectrumResolution, input.getMfccSpectrumResolution());
       	 }
        if (Extension.isNotNullOrEmpty(input.getMelSpectrumResolution())) {
             queryWrapper = queryWrapper.like(TAudioScreenRecord::getMelSpectrumResolution, input.getMelSpectrumResolution());
       	 }
        if (Extension.isNotNullOrEmpty(input.getDensenetModelVersion())) {
             queryWrapper = queryWrapper.like(TAudioScreenRecord::getDensenetModelVersion, input.getDensenetModelVersion());
       	 }
        // Deepseek 字段已移除：不再支持按 DeepseekApiVersion 查询
        if (Extension.isNotNullOrEmpty(input.getSpectrumFailReason())) {
             queryWrapper = queryWrapper.like(TAudioScreenRecord::getSpectrumFailReason, input.getSpectrumFailReason());
       	 }
        if (Extension.isNotNullOrEmpty(input.getModelFailReason())) {
             queryWrapper = queryWrapper.like(TAudioScreenRecord::getModelFailReason, input.getModelFailReason());
       	 }
        if (Extension.isNotNullOrEmpty(input.getTotalFailReason())) {
             queryWrapper = queryWrapper.like(TAudioScreenRecord::getTotalFailReason, input.getTotalFailReason());
       	 }
        if (input.getUpdateTimeRange() != null && !input.getUpdateTimeRange().isEmpty()) {
            queryWrapper = queryWrapper.le(TAudioScreenRecord::getUpdateTime, input.getUpdateTimeRange().get(1));
            queryWrapper = queryWrapper.ge(TAudioScreenRecord::getUpdateTime, input.getUpdateTimeRange().get(0));
        }
        if (input.getPronunciationGuideType() != null) {
            queryWrapper = queryWrapper.eq(TAudioScreenRecord::getPronunciationGuideType, input.getPronunciationGuideType());
       	 }
        if (input.getPrimaryScreenResult() != null) {
            queryWrapper = queryWrapper.eq(TAudioScreenRecord::getPrimaryScreenResult, input.getPrimaryScreenResult());
       	 }
        if (input.getDetectTotalStatus() != null) {
            queryWrapper = queryWrapper.eq(TAudioScreenRecord::getDetectTotalStatus, input.getDetectTotalStatus());
       	 }
        if (input.getDetectSubStatus() != null) {
            queryWrapper = queryWrapper.eq(TAudioScreenRecord::getDetectSubStatus, input.getDetectSubStatus());
       	 }
        if (input.getOfflineStatus() != null) {
            queryWrapper = queryWrapper.eq(TAudioScreenRecord::getOfflineStatus, input.getOfflineStatus());
       	 }
        if (input.getIsDelete() != null) {
            queryWrapper = queryWrapper.eq(TAudioScreenRecord::getIsDelete, input.getIsDelete());
       	 }
      

 
 
     if(Extension.isNotNullOrEmpty(input.getKeyWord()))
        {
			queryWrapper=queryWrapper.and(i->i
          	   .like(TAudioScreenRecord::getMfccSpectrumResolution,input.getKeyWord()).or()   	 
          	   .like(TAudioScreenRecord::getMelSpectrumResolution,input.getKeyWord()).or()   	 
          	   .like(TAudioScreenRecord::getDensenetModelVersion,input.getKeyWord()).or()   	 
          	   .like(TAudioScreenRecord::getSpectrumFailReason,input.getKeyWord()).or()   	 
          	   .like(TAudioScreenRecord::getModelFailReason,input.getKeyWord()).or()   	 
          	   .like(TAudioScreenRecord::getTotalFailReason,input.getKeyWord()).or()   	 
        );
                                       
 		   }
    
      return queryWrapper;
    }
  
    /**
     * 处理音频自查记录对于的外键数据
     */
   private List<TAudioScreenRecordDto> DispatchItem(List<TAudioScreenRecordDto> items) throws InvocationTargetException, IllegalAccessException {
          
       for (TAudioScreenRecordDto item : items) {           
          	            
           //查询出关联的TLaryngoscopePhoto表信息           
            TLaryngoscopePhoto  LaryngoscopePhotoEntity= TLaryngoscopePhotoMapper.selectById(item.getLaryngoscopePhotoId());
            item.setLaryngoscopePhotoDto(LaryngoscopePhotoEntity!=null?LaryngoscopePhotoEntity.MapToDto():new TLaryngoscopePhotoDto());              
       }
       
     return items; 
   }
  
    /**
     * 音频自查记录分页查询
     */
    @SneakyThrows
    @Override
    public PagedResult<TAudioScreenRecordDto> List(TAudioScreenRecordPagedInput input) {
			//构建where条件+排序
        LambdaQueryWrapper<TAudioScreenRecord> queryWrapper = BuilderQuery(input);
        // 动态排序处理
        if (input.getSortItem() != null) {
            // 根据字段名动态排序
            queryWrapper.last("ORDER BY " + input.getSortItem().getFieldName()
                    + (input.getSortItem().getIsAsc() ? " ASC" : " DESC"));
        } else {
            // 默认按创建时间从大到小排序
            queryWrapper = queryWrapper.orderByDesc(TAudioScreenRecord::getCreationTime);
        }

        //构建一个分页查询的model
        Page<TAudioScreenRecord> page = new Page<>(input.getPage(), input.getLimit());
         //从数据库进行分页查询获取音频自查记录数据
        IPage<TAudioScreenRecord> pageRecords= TAudioScreenRecordMapper.selectPage(page, queryWrapper);
        //获取所有满足条件的数据行数
        Long totalCount= TAudioScreenRecordMapper.selectCount(queryWrapper);
        //把TAudioScreenRecord实体转换成TAudioScreenRecord传输模型
        List<TAudioScreenRecordDto> items= Extension.copyBeanList(pageRecords.getRecords(),TAudioScreenRecordDto.class);

		   DispatchItem(items);
        //返回一个分页结构给前端
        return PagedResult.GetInstance(items,totalCount);

    }
  
    /**
     * 单个音频自查记录查询
     */
    @SneakyThrows
    @Override
    public TAudioScreenRecordDto Get(TAudioScreenRecordPagedInput input) {
       if(input.getId()==null)
        {
         return new TAudioScreenRecordDto();
        }
      
       PagedResult<TAudioScreenRecordDto> pagedResult = List(input);
        return pagedResult.getItems().stream().findFirst().orElse(new TAudioScreenRecordDto()); 
    }

    /**
     *音频自查记录创建或者修改
     */
    @SneakyThrows
    @Override
    public TAudioScreenRecordDto CreateOrEdit(TAudioScreenRecordDto input) {
        //声明一个音频自查记录实体
        TAudioScreenRecord TAudioScreenRecord=input.MapToEntity();  
        //调用数据库的增加或者修改方法
        saveOrUpdate(TAudioScreenRecord);
        //把传输模型返回给前端
        return TAudioScreenRecord.MapToDto();
    }

    /**
     * 触发音频检测：生成 MFCC 图谱并给出良/恶性初筛结果
     *
     * 说明：
     * - 为了保证前端在合理时间内一定能拿到“已完成/失败”的状态，这里改为【同步】调用 runDetectJob，
     *   而不是异步 CompletableFuture。
     * - 前端依然通过轮询 /TAudioScreenRecord/Get 获取结果，不依赖本接口的返回内容，因此同步执行不会改变接口契约。
     */
    @Override
    public void StartDetect(IdInput input) {
        if (input == null || input.getId() == null) {
            throw new CustomException("缺少检测记录Id");
        }
        Integer recordId = input.getId();
        System.out.println(">>> StartDetect called, recordId=" + recordId);

        // 先将状态置为“检测中”
        TAudioScreenRecord entity = TAudioScreenRecordMapper.selectById(recordId);
        if (entity == null) {
            throw new CustomException("检测记录不存在");
        }
        entity.setDetectTotalStatus(false);
        entity.setDetectSubStatus(false);
        entity.setTotalFailReason(null);
        entity.setModelFailReason(null);
        entity.setSpectrumFailReason(null);
        entity.setUpdateTime(LocalDateTime.now());
        TAudioScreenRecordMapper.updateById(entity);

        // 同步执行检测任务：确保无论成功/失败，都能及时更新 DetectTotalStatus，避免前端长期“检测中”
        runDetectJob(recordId);
    }

    /**
     * 执行检测任务：调用 python 脚本生成 mfcc 图谱并做初筛分类。
     *
     * 说明：
     * - 该方法内部自己处理所有异常，并负责写回 DetectTotalStatus / TotalFailReason，
     *   确保前端不会一直停留在“检测中”。
     */
    private void runDetectJob(Integer recordId) {
        try {
        System.out.println(">>> runDetectJob start, recordId=" + recordId);
        TAudioScreenRecord entity = TAudioScreenRecordMapper.selectById(recordId);
        if (entity == null) {
            throw new CustomException("检测记录不存在");
        }
        if (entity.getAudioUrl() == null || entity.getAudioUrl().isEmpty()) {
            throw new CustomException("检测记录缺少音频地址");
        }

        // 将 AudioUrl 映射为本地文件路径：external-resources/<random>/<filename>
        String audioUrl = entity.getAudioUrl();
        String[] parts = audioUrl.split("/");
        if (parts.length < 2) {
            throw new CustomException("音频地址格式不正确：" + audioUrl);
        }
        String filename = parts[parts.length - 1];
        String folder = parts[parts.length - 2];

        String userDir = System.getProperty("user.dir");
        String localDir = userDir + File.separator + "external-resources" + File.separator + folder;
        String localAudioPath = localDir + File.separator + filename;

        File audioFile = new File(localAudioPath);
        if (!audioFile.exists()) {
            throw new CustomException("找不到音频文件：" + localAudioPath);
        }

        // 调用 python 脚本（兼容后端运行目录在 HealthControl.springboot/ 的情况）
        String pyScript = userDir + File.separator + "voice" + File.separator + "detect_audio.py";
        File pyFile = new File(pyScript);
        if (!pyFile.exists()) {
            // 常见：工作目录为 .../HealthControl.springboot，则 voice 在上一级目录
            String alt = new File(userDir).getParent() + File.separator + "voice" + File.separator + "detect_audio.py";
            File altFile = new File(alt);
            if (altFile.exists()) {
                pyScript = alt;
                pyFile = altFile;
            } else {
                throw new CustomException("缺少检测脚本：" + pyScript);
            }
        }

        ProcessBuilder pb = new ProcessBuilder("python", "-u", pyScript, "--audio", localAudioPath, "--outdir", localDir);
        pb.redirectErrorStream(true);
        // 统一 python 输出编码，避免 Windows 控制台编码导致 JSON 解析失败
        pb.environment().put("PYTHONIOENCODING", "utf-8");
        Process p = pb.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
        }
        // 等待脚本执行完成，最多 120 秒，避免一直卡住导致前端长时间无结果
        boolean finished = p.waitFor(120, TimeUnit.SECONDS);
        if (!finished) {
            p.destroyForcibly();
            throw new CustomException("检测脚本执行超时（>120秒），请检查模型或环境配置，输出=" + output);
        }
        int code = p.exitValue();
        if (code != 0) {
            throw new CustomException("检测脚本运行失败，退出码=" + code + "，输出=" + output);
        }

        // Python 侧可能会有第三方库警告混入 stdout，导致 JSON 解析失败
        // 这里取最后一个 JSON 对象片段进行解析，保证健壮性
        String raw = output.toString().trim();
        int lastL = raw.lastIndexOf('{');
        int lastR = raw.lastIndexOf('}');
        if (lastL < 0 || lastR < 0 || lastR <= lastL) {
            throw new CustomException("检测脚本输出无法解析为JSON，输出=" + raw);
        }
        String json = raw.substring(lastL, lastR + 1);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(json);
        boolean ok = node.path("success").asBoolean(false);
        if (!ok) {
            throw new CustomException("检测失败：" + node.path("msg").asText("unknown"));
        }

        String mfccFile = node.path("mfcc_file").asText("");
        String melFile = node.path("mel_file").asText("");
        int predictedClass = node.path("predicted_class").asInt(-1);
        boolean malignantFromPy = node.path("malignant").asBoolean(false);
        double confidence = node.path("confidence").asDouble(0.0);
        String method = node.path("method").asText("");
        // 调试字段：prob0/prob1（不修改数据库结构，写入 ModelFailReason 便于前端展示）
        // 说明：prob0=良性概率，prob1=恶性概率
        double prob0 = node.path("prob0").asDouble(Double.NaN);
        double prob1 = node.path("prob1").asDouble(Double.NaN);

        // 生成图谱 URL（和音频同目录）
        String baseUrl = audioUrl.substring(0, audioUrl.lastIndexOf("/"));
        String mfccUrl = mfccFile.isEmpty() ? null : (baseUrl + "/" + mfccFile);
        String melUrl = melFile.isEmpty() ? null : (baseUrl + "/" + melFile);

        // 判定规则（按需求）：
        // - predicted_class=0 且 confidence>0.5 => 良性
        // - 其他情况 => 恶性
        // 说明：数据库字段 PrimaryScreenResult 是 boolean（true/false），这里用 true 表示“恶性/异常”，false 表示“良性/正常”
        boolean isBenign = (predictedClass == 0 && confidence > 0.5);
        boolean malignant = !isBenign;
        // 如果 python 没返回 predicted_class（老脚本），则回退用 malignant 字段
        if (predictedClass < 0) {
            malignant = malignantFromPy;
            isBenign = !malignant;
        }

        // 写回数据库：使用明确的更新条件，避免字段策略影响
        String rawShort = raw.length() > 800 ? raw.substring(raw.length() - 800) : raw;
        String debug = "method=" + (method == null ? "" : method)
                + ",predicted_class=" + (predictedClass < 0 ? "" : predictedClass)
                + ",isBenign=" + isBenign
                + ",prob0=" + (Double.isNaN(prob0) ? "" : prob0)
                + ",prob1=" + (Double.isNaN(prob1) ? "" : prob1)
                + ",confidence=" + confidence
                + ",raw_tail=" + rawShort;

        int rows = TAudioScreenRecordMapper.update(null,
                Wrappers.<TAudioScreenRecord>lambdaUpdate()
                        .eq(TAudioScreenRecord::getId, recordId)
                        .set(TAudioScreenRecord::getMfccSpectrumUrl, mfccUrl)
                        .set(TAudioScreenRecord::getMfccSpectrumResolution, "512x512")
                        .set(TAudioScreenRecord::getMelSpectrumUrl, melUrl)
                        .set(TAudioScreenRecord::getMelSpectrumResolution, "512x512")
                        .set(TAudioScreenRecord::getDensenetModelVersion, (method == null || method.isEmpty()) ? "unknown" : method)
                        .set(TAudioScreenRecord::getPrimaryScreenResult, malignant)
                        .set(TAudioScreenRecord::getPrimaryScreenConfidence, confidence)
                        .set(TAudioScreenRecord::getDetectTotalStatus, true)
                        .set(TAudioScreenRecord::getDetectSubStatus, true)
                        .set(TAudioScreenRecord::getTotalFailReason, null)
                        .set(TAudioScreenRecord::getSpectrumFailReason, null)
                        .set(TAudioScreenRecord::getModelFailReason, debug)
                        .set(TAudioScreenRecord::getUpdateTime, LocalDateTime.now())
        );
        System.out.println(">>> runDetectJob success, updated rows=" + rows + ", recordId=" + recordId);
        } catch (Exception ex) {
            ex.printStackTrace();
            // 兜底失败：确保前端能停止“检测中”，并能在 TotalFailReason 中看到错误信息
            try {
                String msg = null;
                if (ex instanceof CustomException) {
                    msg = ((CustomException) ex).getErrorMsg();
                }
                if (msg == null || msg.trim().isEmpty()) {
                    msg = ex.getMessage();
                }
                if (msg == null || msg.trim().isEmpty()) {
                    msg = ex.toString();
                }

                int rowsFail = TAudioScreenRecordMapper.update(null,
                        Wrappers.<TAudioScreenRecord>lambdaUpdate()
                                .eq(TAudioScreenRecord::getId, recordId)
                                .set(TAudioScreenRecord::getDetectTotalStatus, true)
                                .set(TAudioScreenRecord::getDetectSubStatus, false)
                                .set(TAudioScreenRecord::getTotalFailReason, "检测失败：" + msg)
                                .set(TAudioScreenRecord::getUpdateTime, LocalDateTime.now())
                );
                System.out.println(">>> runDetectJob fail, updated rows=" + rowsFail + ", recordId=" + recordId);
            } catch (Exception ignore) {
                // 最后兜底：这里如果再异常，只打印日志，避免影响主线程
                ignore.printStackTrace();
            }
        }
    }
    /**
     * 音频自查记录删除
     */
    @Override
    public void Delete(IdInput input) {
        TAudioScreenRecord entity = TAudioScreenRecordMapper.selectById(input.getId());
        TAudioScreenRecordMapper.deleteById(entity);
    }

    /**
     * 音频自查记录批量删除
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
     * 音频自查记录导出
     */
    @Override
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        TAudioScreenRecordPagedInput input = mapper.readValue(query, TAudioScreenRecordPagedInput.class);
        List<TAudioScreenRecordDto> items =List(input).getItems();
        //声明一个工作簿
        HSSFWorkbook workbook = new HSSFWorkbook();
        //生成一个表格，设置表格名称为"音频自查记录"
        HSSFSheet sheet = workbook.createSheet("音频自查记录表");
        //设置表格列宽度为10个字节
        sheet.setDefaultColumnWidth(30);
        //创建标题的显示样式
        HSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.YELLOW.index);
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        //创建第一行表头
        HSSFRow headrow = sheet.createRow(0);
        //表头数据
        String[] header = {"关联用户表主键t_user." ,"音频文件URL" ,"音频格式：mp3wav等" ,"音频时长" ,"音频采样率" ,"音频文件大小" ,"发音引导类型" ,"MFCC图谱URL" ,"MFCC图谱分辨率：如256*256" ,"mel图谱URL" ,"mel图谱分辨率：如256*256" ,"孪生Densenet模型版本" ,"初筛结果：0=良性，1=恶性" ,"初筛置信度" ,"基频jitter" ,"基频shimmer" ,"谐噪比HNR" ,"最长发声时间MPT" ,"检测总状态" ,"检测子状态" ,"图谱生成失败原因" ,"模型调用失败原因" ,"整体失败原因" ,"离线状态：0=在线，1=离线" ,"喉镜照片URL" ,"软删除标记：0=未删除，1=已删除" ,};   
        //遍历添加表头(下面模拟遍历音频自查记录，也是同样的操作过程)
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
            TAudioScreenRecordDto dto = items.get(i);
            //创建一行
            HSSFRow row = sheet.createRow(i + 1);    
   
              if (dto.getUserId() != null) { 
                row.createCell(0).setCellValue(new HSSFRichTextString(dto.getUserId()+""));
              }
              if (Extension.isNotNullOrEmpty(dto.getAudioUrl())) { 
                row.createCell(1).setCellValue(new HSSFRichTextString(dto.getAudioUrl()));
              }
              if (Extension.isNotNullOrEmpty(dto.getAudioFormat())) { 
                row.createCell(2).setCellValue(new HSSFRichTextString(dto.getAudioFormat()));
              }
              if (dto.getAudioDuration() != null) { 
                row.createCell(3).setCellValue(new HSSFRichTextString(dto.getAudioDuration()+""));
              }
              if (dto.getAudioSamplingRate() != null) { 
                row.createCell(4).setCellValue(new HSSFRichTextString(dto.getAudioSamplingRate()+""));
              }
              if (dto.getAudioFileSize() != null) { 
                row.createCell(5).setCellValue(new HSSFRichTextString(dto.getAudioFileSize()+""));
              }
              if (dto.getPronunciationGuideType() != null) { 
                row.createCell(6).setCellValue(new HSSFRichTextString(dto.getPronunciationGuideType()?"是":"否"));
              }
              if (Extension.isNotNullOrEmpty(dto.getMfccSpectrumUrl())) { 
                row.createCell(7).setCellValue(new HSSFRichTextString(dto.getMfccSpectrumUrl()));
              }
              if (Extension.isNotNullOrEmpty(dto.getMfccSpectrumResolution())) { 
                row.createCell(8).setCellValue(new HSSFRichTextString(dto.getMfccSpectrumResolution()));
              }
              if (Extension.isNotNullOrEmpty(dto.getMelSpectrumUrl())) { 
                row.createCell(9).setCellValue(new HSSFRichTextString(dto.getMelSpectrumUrl()));
              }
              if (Extension.isNotNullOrEmpty(dto.getMelSpectrumResolution())) { 
                row.createCell(10).setCellValue(new HSSFRichTextString(dto.getMelSpectrumResolution()));
              }
              if (Extension.isNotNullOrEmpty(dto.getDensenetModelVersion())) { 
                row.createCell(11).setCellValue(new HSSFRichTextString(dto.getDensenetModelVersion()));
              }
              if (dto.getPrimaryScreenResult() != null) { 
                row.createCell(12).setCellValue(new HSSFRichTextString(dto.getPrimaryScreenResult()?"是":"否"));
              }
              if (dto.getPrimaryScreenConfidence() != null) { 
                //保留2位小数
                DecimalFormat df = new DecimalFormat("#.00");
                row.createCell(13).setCellValue(new HSSFRichTextString(df.format(dto.getPrimaryScreenConfidence())));
              }
              if (dto.getJitter() != null) { 
                //保留2位小数
                DecimalFormat df = new DecimalFormat("#.00");
                row.createCell(14).setCellValue(new HSSFRichTextString(df.format(dto.getJitter())));
              }
              if (dto.getShimmer() != null) { 
                //保留2位小数
                DecimalFormat df = new DecimalFormat("#.00");
                row.createCell(15).setCellValue(new HSSFRichTextString(df.format(dto.getShimmer())));
              }
              if (dto.getHnr() != null) { 
                //保留2位小数
                DecimalFormat df = new DecimalFormat("#.00");
                row.createCell(16).setCellValue(new HSSFRichTextString(df.format(dto.getHnr())));
              }
              if (dto.getMpt() != null) { 
                row.createCell(17).setCellValue(new HSSFRichTextString(dto.getMpt()+""));
              }
              if (dto.getDetectTotalStatus() != null) { 
                row.createCell(18).setCellValue(new HSSFRichTextString(dto.getDetectTotalStatus()?"是":"否"));
              }
              if (dto.getDetectSubStatus() != null) { 
                row.createCell(19).setCellValue(new HSSFRichTextString(dto.getDetectSubStatus()?"是":"否"));
              }
              if (Extension.isNotNullOrEmpty(dto.getSpectrumFailReason())) { 
                row.createCell(20).setCellValue(new HSSFRichTextString(dto.getSpectrumFailReason()));
              }
              if (Extension.isNotNullOrEmpty(dto.getModelFailReason())) { 
                row.createCell(21).setCellValue(new HSSFRichTextString(dto.getModelFailReason()));
              }
              if (Extension.isNotNullOrEmpty(dto.getTotalFailReason())) { 
                row.createCell(22).setCellValue(new HSSFRichTextString(dto.getTotalFailReason()));
              }
              if (dto.getOfflineStatus() != null) { 
                row.createCell(23).setCellValue(new HSSFRichTextString(dto.getOfflineStatus()?"是":"否"));
              }
              if (dto.getLaryngoscopePhotoDto()!=null&&Extension.isNotNullOrEmpty(dto.getLaryngoscopePhotoDto().getLaryngoscopePhotoUrl())) { 
                row.createCell(24).setCellValue(new HSSFRichTextString(dto.getLaryngoscopePhotoDto().getLaryngoscopePhotoUrl()));
              }
              if (dto.getIsDelete() != null) { 
                row.createCell(25).setCellValue(new HSSFRichTextString(dto.getIsDelete()?"是":"否"));
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
