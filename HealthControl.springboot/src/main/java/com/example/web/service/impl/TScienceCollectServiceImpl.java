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
 * 科普收藏功能实现类
 */
@Service
public class TScienceCollectServiceImpl extends ServiceImpl<TScienceCollectMapper, TScienceCollect> implements TScienceCollectService {

	 /**
     * 操作数据库AppUser表mapper对象
     */
    @Autowired
    private AppUserMapper AppUserMapper;
    /**
     * 操作数据库的TScienceCollect表mapper对象
     */
    @Autowired
    private TScienceCollectMapper TScienceCollectMapper;
    @Autowired
    private THealthScienceMapper  THealthScienceMapper;                        

  
   /**
     * 构建表查询sql
     */
    private LambdaQueryWrapper<TScienceCollect> BuilderQuery(TScienceCollectPagedInput input) {
       //声明一个支持科普收藏查询的(拉姆达)表达式
        LambdaQueryWrapper<TScienceCollect> queryWrapper = Wrappers.<TScienceCollect>lambdaQuery()
                .eq(input.getId() != null && input.getId() != 0, TScienceCollect::getId, input.getId());
   //如果前端搜索传入查询条件则拼接查询条件

        if (input.getUserId() != null) {
            queryWrapper = queryWrapper.eq(TScienceCollect::getUserId, input.getUserId());
       	 }

        if (input.getScienceId() != null) {
            queryWrapper = queryWrapper.eq(TScienceCollect::getScienceId, input.getScienceId());
       	 }
        if (input.getUpdateTimeRange() != null && !input.getUpdateTimeRange().isEmpty()) {
            queryWrapper = queryWrapper.le(TScienceCollect::getUpdateTime, input.getUpdateTimeRange().get(1));
            queryWrapper = queryWrapper.ge(TScienceCollect::getUpdateTime, input.getUpdateTimeRange().get(0));
        }
        if (input.getCollectTimeRange() != null && !input.getCollectTimeRange().isEmpty()) {
            queryWrapper = queryWrapper.le(TScienceCollect::getCollectTime, input.getCollectTimeRange().get(1));
            queryWrapper = queryWrapper.ge(TScienceCollect::getCollectTime, input.getCollectTimeRange().get(0));
        }
        if (input.getCancelCollectTimeRange() != null && !input.getCancelCollectTimeRange().isEmpty()) {
            queryWrapper = queryWrapper.le(TScienceCollect::getCancelCollectTime, input.getCancelCollectTimeRange().get(1));
            queryWrapper = queryWrapper.ge(TScienceCollect::getCancelCollectTime, input.getCancelCollectTimeRange().get(0));
        }
        if (input.getIsDelete() != null) {
            queryWrapper = queryWrapper.eq(TScienceCollect::getIsDelete, input.getIsDelete());
       	 }
      

 
    
      return queryWrapper;
    }
  
    /**
     * 处理科普收藏对于的外键数据
     */
   private List<TScienceCollectDto> DispatchItem(List<TScienceCollectDto> items) throws InvocationTargetException, IllegalAccessException {
          
       for (TScienceCollectDto item : items) {           
          	            
           //查询出关联的AppUser表信息           
            AppUser  UserEntity= AppUserMapper.selectById(item.getUserId());
            item.setUserDto(UserEntity!=null?UserEntity.MapToDto():new AppUserDto());              
           
          	            
           //查询出关联的THealthScience表信息           
            THealthScience  ScienceEntity= THealthScienceMapper.selectById(item.getScienceId());
            item.setScienceDto(ScienceEntity!=null?ScienceEntity.MapToDto():new THealthScienceDto());              
       }
       
     return items; 
   }
  
    /**
     * 科普收藏分页查询
     */
    @SneakyThrows
    @Override
    public PagedResult<TScienceCollectDto> List(TScienceCollectPagedInput input) {
			//构建where条件+排序
        LambdaQueryWrapper<TScienceCollect> queryWrapper = BuilderQuery(input);
        // 动态排序处理
        if (input.getSortItem() != null) {
            // 根据字段名动态排序
            queryWrapper.last("ORDER BY " + input.getSortItem().getFieldName()
                    + (input.getSortItem().getIsAsc() ? " ASC" : " DESC"));
        } else {
            // 默认按创建时间从大到小排序
            queryWrapper = queryWrapper.orderByDesc(TScienceCollect::getCreationTime);
        }

        //构建一个分页查询的model
        Page<TScienceCollect> page = new Page<>(input.getPage(), input.getLimit());
         //从数据库进行分页查询获取科普收藏数据
        IPage<TScienceCollect> pageRecords= TScienceCollectMapper.selectPage(page, queryWrapper);
        //获取所有满足条件的数据行数
        Long totalCount= TScienceCollectMapper.selectCount(queryWrapper);
        //把TScienceCollect实体转换成TScienceCollect传输模型
        List<TScienceCollectDto> items= Extension.copyBeanList(pageRecords.getRecords(),TScienceCollectDto.class);

		   DispatchItem(items);
        //返回一个分页结构给前端
        return PagedResult.GetInstance(items,totalCount);

    }
  
    /**
     * 单个科普收藏查询
     */
    @SneakyThrows
    @Override
    public TScienceCollectDto Get(TScienceCollectPagedInput input) {
       if(input.getId()==null)
        {
         return new TScienceCollectDto();
        }
      
       PagedResult<TScienceCollectDto> pagedResult = List(input);
        return pagedResult.getItems().stream().findFirst().orElse(new TScienceCollectDto()); 
    }

    /**
     *科普收藏创建或者修改
     */
    @SneakyThrows
    @Override
    public TScienceCollectDto CreateOrEdit(TScienceCollectDto input) {
        //声明一个科普收藏实体
        TScienceCollect TScienceCollect=input.MapToEntity();  
        //调用数据库的增加或者修改方法
        saveOrUpdate(TScienceCollect);
        //把传输模型返回给前端
        return TScienceCollect.MapToDto();
    }
    /**
     * 科普收藏删除
     */
    @Override
    public void Delete(IdInput input) {
        TScienceCollect entity = TScienceCollectMapper.selectById(input.getId());
        TScienceCollectMapper.deleteById(entity);
    }

    /**
     * 科普收藏批量删除
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
     * 科普收藏导出
     */
    @Override
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        TScienceCollectPagedInput input = mapper.readValue(query, TScienceCollectPagedInput.class);
        List<TScienceCollectDto> items =List(input).getItems();
        //声明一个工作簿
        HSSFWorkbook workbook = new HSSFWorkbook();
        //生成一个表格，设置表格名称为"科普收藏"
        HSSFSheet sheet = workbook.createSheet("科普收藏表");
        //设置表格列宽度为10个字节
        sheet.setDefaultColumnWidth(30);
        //创建标题的显示样式
        HSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.YELLOW.index);
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        //创建第一行表头
        HSSFRow headrow = sheet.createRow(0);
        //表头数据
        String[] header = {"名称" ,"科普标题" ,"软删除标记" ,};   
        //遍历添加表头(下面模拟遍历科普收藏，也是同样的操作过程)
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
            TScienceCollectDto dto = items.get(i);
            //创建一行
            HSSFRow row = sheet.createRow(i + 1);    
   
              if (dto.getUserDto()!=null&&Extension.isNotNullOrEmpty(dto.getUserDto().getName())) { 
                row.createCell(0).setCellValue(new HSSFRichTextString(dto.getUserDto().getName()));
              }
              if (dto.getScienceDto()!=null&&Extension.isNotNullOrEmpty(dto.getScienceDto().getTitle())) { 
                row.createCell(1).setCellValue(new HSSFRichTextString(dto.getScienceDto().getTitle()));
              }
              if (dto.getIsDelete() != null) { 
                row.createCell(2).setCellValue(new HSSFRichTextString(dto.getIsDelete()?"是":"否"));
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
