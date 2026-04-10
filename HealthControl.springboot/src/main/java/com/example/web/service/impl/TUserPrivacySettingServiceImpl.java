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
 * 用户隐私设置功能实现类
 */
@Service
public class TUserPrivacySettingServiceImpl extends ServiceImpl<TUserPrivacySettingMapper, TUserPrivacySetting> implements TUserPrivacySettingService {

	 /**
     * 操作数据库AppUser表mapper对象
     */
    @Autowired
    private AppUserMapper AppUserMapper;
    /**
     * 操作数据库的TUserPrivacySetting表mapper对象
     */
    @Autowired
    private TUserPrivacySettingMapper TUserPrivacySettingMapper;

  
   /**
     * 构建表查询sql
     */
    private LambdaQueryWrapper<TUserPrivacySetting> BuilderQuery(TUserPrivacySettingPagedInput input) {
       //声明一个支持用户隐私设置查询的(拉姆达)表达式
        LambdaQueryWrapper<TUserPrivacySetting> queryWrapper = Wrappers.<TUserPrivacySetting>lambdaQuery()
                .eq(input.getId() != null && input.getId() != 0, TUserPrivacySetting::getId, input.getId());
   //如果前端搜索传入查询条件则拼接查询条件

        if (input.getUserId() != null) {
            queryWrapper = queryWrapper.eq(TUserPrivacySetting::getUserId, input.getUserId());
       	 }
        if (input.getLocalStorageStatus() != null) {
            queryWrapper = queryWrapper.eq(TUserPrivacySetting::getLocalStorageStatus, input.getLocalStorageStatus());
       	 }
        if (input.getDataAnonymousAuth() != null) {
            queryWrapper = queryWrapper.eq(TUserPrivacySetting::getDataAnonymousAuth, input.getDataAnonymousAuth());
       	 }
        if (input.getPrivacyAgreeStatus() != null) {
            queryWrapper = queryWrapper.eq(TUserPrivacySetting::getPrivacyAgreeStatus, input.getPrivacyAgreeStatus());
       	 }
        if (input.getDataRetentionEnabled() != null) {
            queryWrapper = queryWrapper.eq(TUserPrivacySetting::getDataRetentionEnabled, input.getDataRetentionEnabled());
        }
        if (input.getDataRetentionMonths() != null) {
            queryWrapper = queryWrapper.eq(TUserPrivacySetting::getDataRetentionMonths, input.getDataRetentionMonths());
        }
        if (input.getIsDelete() != null) {
            queryWrapper = queryWrapper.eq(TUserPrivacySetting::getIsDelete, input.getIsDelete());
       	 }
      

 
    
      return queryWrapper;
    }
  
    /**
     * 处理用户隐私设置对于的外键数据
     */
   private List<TUserPrivacySettingDto> DispatchItem(List<TUserPrivacySettingDto> items) throws InvocationTargetException, IllegalAccessException {
          
       for (TUserPrivacySettingDto item : items) {           
          	            
           //查询出关联的AppUser表信息           
            AppUser  UserEntity= AppUserMapper.selectById(item.getUserId());
            item.setUserDto(UserEntity!=null?UserEntity.MapToDto():new AppUserDto());              
       }
       
     return items; 
   }
  
    /**
     * 用户隐私设置分页查询
     */
    @SneakyThrows
    @Override
    public PagedResult<TUserPrivacySettingDto> List(TUserPrivacySettingPagedInput input) {
			//构建where条件+排序
        LambdaQueryWrapper<TUserPrivacySetting> queryWrapper = BuilderQuery(input);
        // 动态排序处理
        if (input.getSortItem() != null) {
            // 根据字段名动态排序
            queryWrapper.last("ORDER BY " + input.getSortItem().getFieldName()
                    + (input.getSortItem().getIsAsc() ? " ASC" : " DESC"));
        } else {
            // 默认按创建时间从大到小排序
            queryWrapper = queryWrapper.orderByDesc(TUserPrivacySetting::getCreationTime);
        }

        //构建一个分页查询的model
        Page<TUserPrivacySetting> page = new Page<>(input.getPage(), input.getLimit());
         //从数据库进行分页查询获取用户隐私设置数据
        IPage<TUserPrivacySetting> pageRecords= TUserPrivacySettingMapper.selectPage(page, queryWrapper);
        //获取所有满足条件的数据行数
        Long totalCount= TUserPrivacySettingMapper.selectCount(queryWrapper);
        //把TUserPrivacySetting实体转换成TUserPrivacySetting传输模型
        List<TUserPrivacySettingDto> items= Extension.copyBeanList(pageRecords.getRecords(),TUserPrivacySettingDto.class);

		   DispatchItem(items);
        //返回一个分页结构给前端
        return PagedResult.GetInstance(items,totalCount);

    }
  
    /**
     * 单个用户隐私设置查询
     */
    @SneakyThrows
    @Override
    public TUserPrivacySettingDto Get(TUserPrivacySettingPagedInput input) {
       // 如果既没有Id也没有UserId，返回空对象
       if(input.getId() == null && input.getUserId() == null) {
         return new TUserPrivacySettingDto();
        }
      
       // 根据Id或UserId查询
       PagedResult<TUserPrivacySettingDto> pagedResult = List(input);
        return pagedResult.getItems().stream().findFirst().orElse(new TUserPrivacySettingDto()); 
    }

    /**
     *用户隐私设置创建或者修改
     */
    @SneakyThrows
    @Override
    public TUserPrivacySettingDto CreateOrEdit(TUserPrivacySettingDto input) {
        if (input.getUserId() == null) {
            throw new CustomException("用户ID不能为空");
        }
        
        //声明一个用户隐私设置实体
        TUserPrivacySetting entity = input.MapToEntity();
        
        // 如果Id为空，尝试根据UserId查找现有记录
        if (entity.getId() == null) {
            TUserPrivacySetting existing = TUserPrivacySettingMapper.selectOne(
                Wrappers.<TUserPrivacySetting>lambdaQuery()
                    .eq(TUserPrivacySetting::getUserId, input.getUserId())
                    .eq(TUserPrivacySetting::getIsDelete, false)
            );
            if (existing != null) {
                // 如果存在，使用现有记录的Id进行更新
                entity.setId(existing.getId());
                entity.setCreationTime(existing.getCreationTime());
                TUserPrivacySettingMapper.updateById(entity);
            } else {
                // 如果不存在，创建新记录（CreationTime 会由 MyMetaObjectHandler 自动填充）
                TUserPrivacySettingMapper.insert(entity);
            }
        } else {
            // 如果Id不为空，直接更新
            TUserPrivacySetting existing = TUserPrivacySettingMapper.selectById(entity.getId());
            if (existing == null) {
                throw new CustomException("隐私设置记录不存在");
            }
            entity.setCreationTime(existing.getCreationTime());
            TUserPrivacySettingMapper.updateById(entity);
        }
        
        // 重新查询以确保返回最新数据
        TUserPrivacySetting saved = TUserPrivacySettingMapper.selectById(entity.getId());
        return saved != null ? saved.MapToDto() : entity.MapToDto();
    }
    /**
     * 用户隐私设置删除
     */
    @Override
    public void Delete(IdInput input) {
        TUserPrivacySetting entity = TUserPrivacySettingMapper.selectById(input.getId());
        TUserPrivacySettingMapper.deleteById(entity);
    }

    /**
     * 用户隐私设置批量删除
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
     * 用户隐私设置导出
     */
    @Override
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        TUserPrivacySettingPagedInput input = mapper.readValue(query, TUserPrivacySettingPagedInput.class);
        List<TUserPrivacySettingDto> items =List(input).getItems();
        //声明一个工作簿
        HSSFWorkbook workbook = new HSSFWorkbook();
        //生成一个表格，设置表格名称为"用户隐私设置"
        HSSFSheet sheet = workbook.createSheet("用户隐私设置表");
        //设置表格列宽度为10个字节
        sheet.setDefaultColumnWidth(30);
        //创建标题的显示样式
        HSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.YELLOW.index);
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        //创建第一行表头
        HSSFRow headrow = sheet.createRow(0);
        //表头数据
        String[] header = {
                "名称",
                "本地存储开启状态：0=否，1=是",
                "数据匿名授权状态：0=否，1=是",
                "隐私协议同意状态：0=否，1=是",
                "是否开启自动清理：0=否，1=是",
                "数据保留时长（月），NULL/0=永久保留",
                "软删除标记：0=未删除，1=已删除"
        };   
        //遍历添加表头(下面模拟遍历用户隐私设置，也是同样的操作过程)
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
            TUserPrivacySettingDto dto = items.get(i);
            //创建一行
            HSSFRow row = sheet.createRow(i + 1);    
   
              if (dto.getUserDto()!=null&&Extension.isNotNullOrEmpty(dto.getUserDto().getName())) { 
                row.createCell(0).setCellValue(new HSSFRichTextString(dto.getUserDto().getName()));
              }
              if (dto.getLocalStorageStatus() != null) { 
                row.createCell(1).setCellValue(new HSSFRichTextString(dto.getLocalStorageStatus()?"是":"否"));
              }
              if (dto.getDataAnonymousAuth() != null) { 
                row.createCell(2).setCellValue(new HSSFRichTextString(dto.getDataAnonymousAuth()?"是":"否"));
              }
              if (dto.getPrivacyAgreeStatus() != null) { 
                row.createCell(3).setCellValue(new HSSFRichTextString(dto.getPrivacyAgreeStatus()?"是":"否"));
              }
              if (dto.getDataRetentionEnabled() != null) {
                row.createCell(4).setCellValue(new HSSFRichTextString(dto.getDataRetentionEnabled() ? "是" : "否"));
              }
              if (dto.getDataRetentionMonths() != null) {
                row.createCell(5).setCellValue(new HSSFRichTextString(String.valueOf(dto.getDataRetentionMonths())));
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
