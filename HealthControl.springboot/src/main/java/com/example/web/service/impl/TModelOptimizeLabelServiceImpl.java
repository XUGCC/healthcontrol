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
import java.util.stream.Collectors;
import java.util.List;
import lombok.SneakyThrows;
import java.io.IOException;
import com.example.web.tools.*;
import com.example.web.tools.BaseContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Objects;
/**
 * 模型优化标注功能实现类
 */
@Service
public class TModelOptimizeLabelServiceImpl extends ServiceImpl<TModelOptimizeLabelMapper, TModelOptimizeLabel> implements TModelOptimizeLabelService {

	 /**
     * 操作数据库AppUser表mapper对象
     */
    @Autowired
    private AppUserMapper AppUserMapper;
    /**
     * 操作数据库的TModelOptimizeLabel表mapper对象
     */
    @Autowired
    private TModelOptimizeLabelMapper TModelOptimizeLabelMapper;
    @Autowired
    private TAudioScreenRecordMapper  TAudioScreenRecordMapper;                        
    @Autowired
    private TUserPrivacySettingMapper TUserPrivacySettingMapper;                        

  
   /**
     * 构建表查询sql
     */
    private LambdaQueryWrapper<TModelOptimizeLabel> BuilderQuery(TModelOptimizeLabelPagedInput input) {
       //声明一个支持模型优化标注查询的(拉姆达)表达式
        LambdaQueryWrapper<TModelOptimizeLabel> queryWrapper = Wrappers.<TModelOptimizeLabel>lambdaQuery()
                .eq(input.getId() != null && input.getId() != 0, TModelOptimizeLabel::getId, input.getId());
   //如果前端搜索传入查询条件则拼接查询条件
        if (Extension.isNotNullOrEmpty(input.getHospitalDiagnoseResult())) {
             queryWrapper = queryWrapper.like(TModelOptimizeLabel::getHospitalDiagnoseResult, input.getHospitalDiagnoseResult());
       	 }
        if (Extension.isNotNullOrEmpty(input.getLabelDesc())) {
             queryWrapper = queryWrapper.like(TModelOptimizeLabel::getLabelDesc, input.getLabelDesc());
       	 }

        if (input.getUserId() != null) {
            queryWrapper = queryWrapper.eq(TModelOptimizeLabel::getUserId, input.getUserId());
       	 }

        if (input.getDetectId() != null) {
            queryWrapper = queryWrapper.eq(TModelOptimizeLabel::getDetectId, input.getDetectId());
       	 }
        if (input.getUpdateTimeRange() != null && !input.getUpdateTimeRange().isEmpty()) {
            queryWrapper = queryWrapper.le(TModelOptimizeLabel::getUpdateTime, input.getUpdateTimeRange().get(1));
            queryWrapper = queryWrapper.ge(TModelOptimizeLabel::getUpdateTime, input.getUpdateTimeRange().get(0));
        }
        if (input.getLabelType() != null) {
            queryWrapper = queryWrapper.eq(TModelOptimizeLabel::getLabelType, input.getLabelType());
       	 }
        if (input.getAuthStatus() != null) {
            queryWrapper = queryWrapper.eq(TModelOptimizeLabel::getAuthStatus, input.getAuthStatus());
       	 }
        if (input.getIsDelete() != null) {
            queryWrapper = queryWrapper.eq(TModelOptimizeLabel::getIsDelete, input.getIsDelete());
       	 }
      

 
 
     if(Extension.isNotNullOrEmpty(input.getKeyWord()))
        {
			queryWrapper=queryWrapper.and(i->i
          	   .like(TModelOptimizeLabel::getHospitalDiagnoseResult,input.getKeyWord()).or()   	 
          	   .like(TModelOptimizeLabel::getLabelDesc,input.getKeyWord()).or()   	 
        );
                                       
 		   }
    
      return queryWrapper;
    }
  
    /**
     * 处理模型优化标注对于的外键数据
     */
   private List<TModelOptimizeLabelDto> DispatchItem(List<TModelOptimizeLabelDto> items) throws InvocationTargetException, IllegalAccessException {
          
       for (TModelOptimizeLabelDto item : items) {           
          	            
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
     * 模型优化标注分页查询
     */
    @SneakyThrows
    @Override
    public PagedResult<TModelOptimizeLabelDto> List(TModelOptimizeLabelPagedInput input) {
			//构建where条件+排序
        LambdaQueryWrapper<TModelOptimizeLabel> queryWrapper = BuilderQuery(input);
        // 动态排序处理
        if (input.getSortItem() != null) {
            // 根据字段名动态排序
            queryWrapper.last("ORDER BY " + input.getSortItem().getFieldName()
                    + (input.getSortItem().getIsAsc() ? " ASC" : " DESC"));
        } else {
            // 默认按创建时间从大到小排序
            queryWrapper = queryWrapper.orderByDesc(TModelOptimizeLabel::getCreationTime);
        }

        //构建一个分页查询的model
        Page<TModelOptimizeLabel> page = new Page<>(input.getPage(), input.getLimit());
         //从数据库进行分页查询获取模型优化标注数据
        IPage<TModelOptimizeLabel> pageRecords= TModelOptimizeLabelMapper.selectPage(page, queryWrapper);
        //获取所有满足条件的数据行数
        Long totalCount= TModelOptimizeLabelMapper.selectCount(queryWrapper);
        //把TModelOptimizeLabel实体转换成TModelOptimizeLabel传输模型
        List<TModelOptimizeLabelDto> items= Extension.copyBeanList(pageRecords.getRecords(),TModelOptimizeLabelDto.class);

		   DispatchItem(items);
        //返回一个分页结构给前端
        return PagedResult.GetInstance(items,totalCount);

    }
  
    /**
     * 单个模型优化标注查询
     */
    @SneakyThrows
    @Override
    public TModelOptimizeLabelDto Get(TModelOptimizeLabelPagedInput input) {
       if(input.getId()==null)
        {
         return new TModelOptimizeLabelDto();
        }
      
       PagedResult<TModelOptimizeLabelDto> pagedResult = List(input);
        return pagedResult.getItems().stream().findFirst().orElse(new TModelOptimizeLabelDto()); 
    }

    /**
     *模型优化标注创建或者修改
     */
    @SneakyThrows
    @Override
    public TModelOptimizeLabelDto CreateOrEdit(TModelOptimizeLabelDto input) {
        //声明一个模型优化标注实体
        TModelOptimizeLabel TModelOptimizeLabel=input.MapToEntity();  
        //调用数据库的增加或者修改方法
        saveOrUpdate(TModelOptimizeLabel);
        //把传输模型返回给前端
        return TModelOptimizeLabel.MapToDto();
    }
    /**
     * 模型优化标注删除
     */
    @Override
    public void Delete(IdInput input) {
        TModelOptimizeLabel entity = TModelOptimizeLabelMapper.selectById(input.getId());
        TModelOptimizeLabelMapper.deleteById(entity);
    }

    /**
     * 模型优化标注批量删除
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
     * 模型优化标注导出
     */
    @Override
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        TModelOptimizeLabelPagedInput input = mapper.readValue(query, TModelOptimizeLabelPagedInput.class);
        List<TModelOptimizeLabelDto> items =List(input).getItems();
        //声明一个工作簿
        HSSFWorkbook workbook = new HSSFWorkbook();
        //生成一个表格，设置表格名称为"模型优化标注"
        HSSFSheet sheet = workbook.createSheet("模型优化标注表");
        //设置表格列宽度为10个字节
        sheet.setDefaultColumnWidth(30);
        //创建标题的显示样式
        HSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.YELLOW.index);
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        //创建第一行表头
        HSSFRow headrow = sheet.createRow(0);
        //表头数据
        String[] header = {"名称" ,"孪生Densenet模型版本" ,"医院确诊结果" ,"标注类型" ,"标注说明" ,"授权状态" ,"软删除标记" ,};   
        //遍历添加表头(下面模拟遍历模型优化标注，也是同样的操作过程)
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
            TModelOptimizeLabelDto dto = items.get(i);
            //创建一行
            HSSFRow row = sheet.createRow(i + 1);    
   
              if (dto.getUserDto()!=null&&Extension.isNotNullOrEmpty(dto.getUserDto().getName())) { 
                row.createCell(0).setCellValue(new HSSFRichTextString(dto.getUserDto().getName()));
              }
              if (dto.getDetectDto()!=null&&Extension.isNotNullOrEmpty(dto.getDetectDto().getDensenetModelVersion())) { 
                row.createCell(1).setCellValue(new HSSFRichTextString(dto.getDetectDto().getDensenetModelVersion()));
              }
              if (Extension.isNotNullOrEmpty(dto.getHospitalDiagnoseResult())) { 
                row.createCell(2).setCellValue(new HSSFRichTextString(dto.getHospitalDiagnoseResult()));
              }
              if (dto.getLabelType() != null) { 
                row.createCell(3).setCellValue(new HSSFRichTextString(dto.getLabelType()?"是":"否"));
              }
              if (Extension.isNotNullOrEmpty(dto.getLabelDesc())) { 
                row.createCell(4).setCellValue(new HSSFRichTextString(dto.getLabelDesc()));
              }
              if (dto.getAuthStatus() != null) { 
                row.createCell(5).setCellValue(new HSSFRichTextString(dto.getAuthStatus()?"是":"否"));
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

    /**
     * 前台创建或更新标注（带权限校验和隐私设置联动）
     */
    @SneakyThrows
    @Override
    public TModelOptimizeLabelDto CreateOrEditForFront(TModelOptimizeLabelDto input) {
        com.example.web.tools.dto.CurrentUserDto currentUser = BaseContext.getCurrentUserDto();
        if (currentUser == null || currentUser.getUserId() == null || currentUser.getUserId() <= 0) {
            throw new CustomException("用户未登录");
        }

        Integer currentUserId = currentUser.getUserId();
        if (input.getUserId() == null) {
            input.setUserId(currentUserId);
        }
        if (!currentUserId.equals(input.getUserId())) {
            throw new CustomException("无权操作");
        }
        if (input.getDetectId() == null || input.getDetectId() <= 0) {
            throw new CustomException("请选择检测记录");
        }

        TAudioScreenRecord detect = TAudioScreenRecordMapper.selectById(input.getDetectId());
        if (detect == null || !currentUserId.equals(detect.getUserId())) {
            throw new CustomException("检测记录不存在或无权访问");
        }

        TModelOptimizeLabel existing = findFrontLabelByDetectId(currentUserId, input.getDetectId());
        if (input.getId() == null) {
            if (existing != null) {
                input.setId(existing.getId());
            }
        } else if (existing != null && !existing.getId().equals(input.getId())) {
            throw new CustomException("该检测记录已存在其他标注，请刷新后重试");
        }

        TModelOptimizeLabel entity = input.MapToEntity();
        entity.setUserId(currentUserId);
        entity.setDetectId(input.getDetectId());
        entity.setIsDelete(false);

        if (input.getAuthStatus() != null && input.getAuthStatus()) {
            checkAndUpdatePrivacySetting(currentUserId, true);
        }

        if (input.getId() == null) {
            entity.setCreationTime(java.time.LocalDateTime.now());
            entity.setUpdateTime(java.time.LocalDateTime.now());
            TModelOptimizeLabelMapper.insert(entity);
        } else {
            TModelOptimizeLabel dbEntity = TModelOptimizeLabelMapper.selectById(input.getId());
            if (dbEntity == null || !currentUserId.equals(dbEntity.getUserId())) {
                throw new CustomException("标注记录不存在或无权访问");
            }
            entity.setCreationTime(dbEntity.getCreationTime());
            entity.setUpdateTime(java.time.LocalDateTime.now());
            TModelOptimizeLabelMapper.updateById(entity);
        }

        TModelOptimizeLabel saved = TModelOptimizeLabelMapper.selectById(entity.getId());
        return saved != null ? saved.MapToDto() : entity.MapToDto();
    }

    /**
     * 前台获取用户标注列表（仅返回当前用户的数据）
     */
    @Override
    public PagedResult<TModelOptimizeLabelDto> ListForFront(TModelOptimizeLabelPagedInput input) {
        // 优先使用BaseContext中的用户ID，如果没有则使用前端传递的UserId
        com.example.web.tools.dto.CurrentUserDto currentUser = BaseContext.getCurrentUserDto();
        Integer userId = null;
        
        if (currentUser != null && currentUser.getUserId() != null && currentUser.getUserId() > 0) {
            userId = currentUser.getUserId();
        } else if (input.getUserId() != null && input.getUserId() > 0) {
            // 如果BaseContext中没有用户信息，使用前端传递的UserId（兼容处理）
            userId = input.getUserId();
        }
        
        if (userId == null || userId <= 0) {
            throw new CustomException("用户未登录");
        }
        
        input.setUserId(userId);
        
        // 自动过滤软删除记录
        input.setIsDelete(false);
        
        // 调用原有List方法
        return List(input);
    }

    /**
     * 前台获取单条标注详情（带权限校验）
     */
    @SneakyThrows
    @Override
    public TModelOptimizeLabelDto GetForFront(TModelOptimizeLabelPagedInput input) {
        com.example.web.tools.dto.CurrentUserDto currentUser = BaseContext.getCurrentUserDto();
        if (currentUser == null || currentUser.getUserId() == null) {
            throw new CustomException("用户未登录");
        }
        Integer currentUserId = currentUser.getUserId();

        TModelOptimizeLabel entity = null;
        if (input.getId() != null && input.getId() > 0) {
            entity = TModelOptimizeLabelMapper.selectById(input.getId());
            if (entity == null || !entity.getUserId().equals(currentUserId) || Boolean.TRUE.equals(entity.getIsDelete())) {
                throw new CustomException("标注记录不存在或无权访问");
            }
        } else if (input.getDetectId() != null && input.getDetectId() > 0) {
            entity = findFrontLabelByDetectId(currentUserId, input.getDetectId());
            if (entity == null) {
                return new TModelOptimizeLabelDto();
            }
        } else {
            throw new CustomException("缺少查询参数");
        }

        TModelOptimizeLabelDto dto = entity.MapToDto();
        TAudioScreenRecord detect = TAudioScreenRecordMapper.selectById(entity.getDetectId());
        if (detect != null) {
            dto.setDetectDto(detect.MapToDto());
        }
        return dto;
    }

    /**
     * 前台更新授权状态（带权限校验和隐私设置联动）
     */
    @Override
    public void UpdateAuthForFront(TModelOptimizeLabelDto input) {
        com.example.web.tools.dto.CurrentUserDto currentUser = BaseContext.getCurrentUserDto();
        if (currentUser == null || currentUser.getUserId() == null) {
            throw new CustomException("用户未登录");
        }
        Integer currentUserId = currentUser.getUserId();
        if (!currentUserId.equals(input.getUserId())) {
            throw new CustomException("无权操作");
        }
        
        TModelOptimizeLabel entity = TModelOptimizeLabelMapper.selectById(input.getId());
        if (entity == null || !entity.getUserId().equals(currentUserId)) {
            throw new CustomException("标注记录不存在或无权访问");
        }
        
        entity.setAuthStatus(input.getAuthStatus());
        entity.setUpdateTime(java.time.LocalDateTime.now());
        TModelOptimizeLabelMapper.updateById(entity);
        
        // 如果开启授权，同步更新隐私设置
        if (input.getAuthStatus() != null && input.getAuthStatus()) {
            checkAndUpdatePrivacySetting(input.getUserId(), true);
        }
    }

    /**
     * 前台删除标注（带权限校验，软删除）
     */
    @Override
    public void DeleteForFront(TModelOptimizeLabelPagedInput input) {
        com.example.web.tools.dto.CurrentUserDto currentUser = BaseContext.getCurrentUserDto();
        if (currentUser == null || currentUser.getUserId() == null) {
            throw new CustomException("用户未登录");
        }
        Integer currentUserId = currentUser.getUserId();
        if (!currentUserId.equals(input.getUserId())) {
            throw new CustomException("无权操作");
        }
        
        TModelOptimizeLabel entity = TModelOptimizeLabelMapper.selectById(input.getId());
        if (entity == null || !entity.getUserId().equals(currentUserId)) {
            throw new CustomException("标注记录不存在或无权访问");
        }
        
        // 软删除
        entity.setIsDelete(true);
        TModelOptimizeLabelMapper.updateById(entity);
    }

    /**
     * 前台获取用户检测记录列表（用于选择关联）
     */
    @SneakyThrows
    @Override
    public PagedResult<TAudioScreenRecordDto> GetUserDetectListForFront(TModelOptimizeLabelPagedInput input) {
        com.example.web.tools.dto.CurrentUserDto currentUser = BaseContext.getCurrentUserDto();
        if (currentUser == null || currentUser.getUserId() == null) {
            throw new CustomException("用户未登录");
        }
        Integer currentUserId = currentUser.getUserId();
        
        // 查询用户最近的检测记录
        LambdaQueryWrapper<TAudioScreenRecord> queryWrapper = Wrappers
            .<TAudioScreenRecord>lambdaQuery()
            .eq(TAudioScreenRecord::getUserId, currentUserId)
            .eq(TAudioScreenRecord::getIsDelete, false)
            .orderByDesc(TAudioScreenRecord::getCreationTime);
        
        Page<TAudioScreenRecord> page = new Page<>(input.getPage(), input.getLimit());
        IPage<TAudioScreenRecord> pageResult = TAudioScreenRecordMapper.selectPage(page, queryWrapper);
        
        List<TAudioScreenRecordDto> items = pageResult.getRecords().stream()
            .map(entity -> {
                try {
                    return entity.MapToDto();
                } catch (Exception e) {
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
        PagedResult<TAudioScreenRecordDto> result = new PagedResult<>();
        result.setItems(items);
        result.setTotalCount(pageResult.getTotal());
        return result;
    }

    /**
     * 校验并更新隐私设置
     */
    private void checkAndUpdatePrivacySetting(Integer userId, Boolean authStatus) {
        try {
            TUserPrivacySetting privacySetting = TUserPrivacySettingMapper.selectOne(
                Wrappers.<TUserPrivacySetting>lambdaQuery()
                    .eq(TUserPrivacySetting::getUserId, userId)
                    .eq(TUserPrivacySetting::getIsDelete, false)
            );
            
            if (privacySetting == null) {
                // 创建隐私设置记录（CreationTime 由 MyMetaObjectHandler 自动填充）
                privacySetting = new TUserPrivacySetting();
                privacySetting.setUserId(userId);
                privacySetting.setDataAnonymousAuth(authStatus);
                privacySetting.setLocalStorageStatus(false);
                privacySetting.setPrivacyAgreeStatus(false);
                privacySetting.setIsDelete(false);
                TUserPrivacySettingMapper.insert(privacySetting);
            } else if (authStatus && 
                       (privacySetting.getDataAnonymousAuth() == null || !privacySetting.getDataAnonymousAuth())) {
                // 更新隐私设置
                privacySetting.setDataAnonymousAuth(true);
                TUserPrivacySettingMapper.updateById(privacySetting);
            }
        } catch (Exception e) {
            // 记录异常但不抛出，避免影响标注保存
            System.err.println("更新隐私设置失败: " + e.getMessage());
            e.printStackTrace();
            // 如果更新隐私设置失败，不影响标注保存，只记录日志
        }
    }

    private TModelOptimizeLabel findFrontLabelByDetectId(Integer userId, Integer detectId) {
        return TModelOptimizeLabelMapper.selectOne(
            Wrappers.<TModelOptimizeLabel>lambdaQuery()
                .eq(TModelOptimizeLabel::getUserId, userId)
                .eq(TModelOptimizeLabel::getDetectId, detectId)
                .eq(TModelOptimizeLabel::getIsDelete, false)
                .last("LIMIT 1")
        );
    }
}
