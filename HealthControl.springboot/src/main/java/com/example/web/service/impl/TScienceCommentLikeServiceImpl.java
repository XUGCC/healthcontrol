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
 * 科普评论点赞功能实现类
 */
@Service
public class TScienceCommentLikeServiceImpl extends ServiceImpl<TScienceCommentLikeMapper, TScienceCommentLike> implements TScienceCommentLikeService {

	 /**
     * 操作数据库AppUser表mapper对象
     */
    @Autowired
    private AppUserMapper AppUserMapper;
    /**
     * 操作数据库的TScienceCommentLike表mapper对象
     */
    @Autowired
    private TScienceCommentLikeMapper TScienceCommentLikeMapper;
    @Autowired
    private TScienceCommentMapper  TScienceCommentMapper;                        

  
   /**
     * 构建表查询sql
     */
    private LambdaQueryWrapper<TScienceCommentLike> BuilderQuery(TScienceCommentLikePagedInput input) {
       //声明一个支持科普评论点赞查询的(拉姆达)表达式
        LambdaQueryWrapper<TScienceCommentLike> queryWrapper = Wrappers.<TScienceCommentLike>lambdaQuery()
                .eq(input.getId() != null && input.getId() != 0, TScienceCommentLike::getId, input.getId());
   //如果前端搜索传入查询条件则拼接查询条件

        if (input.getUserId() != null) {
            queryWrapper = queryWrapper.eq(TScienceCommentLike::getUserId, input.getUserId());
       	 }

        if (input.getCommentId() != null) {
            queryWrapper = queryWrapper.eq(TScienceCommentLike::getCommentId, input.getCommentId());
       	 }
        if (input.getUpdateTimeRange() != null && !input.getUpdateTimeRange().isEmpty()) {
            queryWrapper = queryWrapper.le(TScienceCommentLike::getUpdateTime, input.getUpdateTimeRange().get(1));
            queryWrapper = queryWrapper.ge(TScienceCommentLike::getUpdateTime, input.getUpdateTimeRange().get(0));
        }
        if (input.getLikeTimeRange() != null && !input.getLikeTimeRange().isEmpty()) {
            queryWrapper = queryWrapper.le(TScienceCommentLike::getLikeTime, input.getLikeTimeRange().get(1));
            queryWrapper = queryWrapper.ge(TScienceCommentLike::getLikeTime, input.getLikeTimeRange().get(0));
        }
        if (input.getCancelLikeTimeRange() != null && !input.getCancelLikeTimeRange().isEmpty()) {
            queryWrapper = queryWrapper.le(TScienceCommentLike::getCancelLikeTime, input.getCancelLikeTimeRange().get(1));
            queryWrapper = queryWrapper.ge(TScienceCommentLike::getCancelLikeTime, input.getCancelLikeTimeRange().get(0));
        }
        if (input.getIsDelete() != null) {
            queryWrapper = queryWrapper.eq(TScienceCommentLike::getIsDelete, input.getIsDelete());
       	 }
      

 
    
      return queryWrapper;
    }
  
    /**
     * 处理科普评论点赞对于的外键数据
     */
   private List<TScienceCommentLikeDto> DispatchItem(List<TScienceCommentLikeDto> items) throws InvocationTargetException, IllegalAccessException {
          
       for (TScienceCommentLikeDto item : items) {           
          	            
           //查询出关联的TScienceComment表信息           
            TScienceComment  CommentEntity= TScienceCommentMapper.selectById(item.getCommentId());
            item.setCommentDto(CommentEntity!=null?CommentEntity.MapToDto():new TScienceCommentDto());              
           
          	            
           //查询出关联的AppUser表信息           
            AppUser  UserEntity= AppUserMapper.selectById(item.getUserId());
            item.setUserDto(UserEntity!=null?UserEntity.MapToDto():new AppUserDto());              
       }
       
     return items; 
   }
  
    /**
     * 科普评论点赞分页查询
     */
    @SneakyThrows
    @Override
    public PagedResult<TScienceCommentLikeDto> List(TScienceCommentLikePagedInput input) {
			//构建where条件+排序
        LambdaQueryWrapper<TScienceCommentLike> queryWrapper = BuilderQuery(input);
        // 动态排序处理
        if (input.getSortItem() != null) {
            // 根据字段名动态排序
            queryWrapper.last("ORDER BY " + input.getSortItem().getFieldName()
                    + (input.getSortItem().getIsAsc() ? " ASC" : " DESC"));
        } else {
            // 默认按创建时间从大到小排序
            queryWrapper = queryWrapper.orderByDesc(TScienceCommentLike::getCreationTime);
        }

        //构建一个分页查询的model
        Page<TScienceCommentLike> page = new Page<>(input.getPage(), input.getLimit());
         //从数据库进行分页查询获取科普评论点赞数据
        IPage<TScienceCommentLike> pageRecords= TScienceCommentLikeMapper.selectPage(page, queryWrapper);
        //获取所有满足条件的数据行数
        Long totalCount= TScienceCommentLikeMapper.selectCount(queryWrapper);
        //把TScienceCommentLike实体转换成TScienceCommentLike传输模型
        List<TScienceCommentLikeDto> items= Extension.copyBeanList(pageRecords.getRecords(),TScienceCommentLikeDto.class);

		   DispatchItem(items);
        //返回一个分页结构给前端
        return PagedResult.GetInstance(items,totalCount);

    }
  
    /**
     * 单个科普评论点赞查询
     */
    @SneakyThrows
    @Override
    public TScienceCommentLikeDto Get(TScienceCommentLikePagedInput input) {
       if(input.getId()==null)
        {
         return new TScienceCommentLikeDto();
        }
      
       PagedResult<TScienceCommentLikeDto> pagedResult = List(input);
        return pagedResult.getItems().stream().findFirst().orElse(new TScienceCommentLikeDto()); 
    }

    /**
     *科普评论点赞创建或者修改
     */
    @SneakyThrows
    @Override
    public TScienceCommentLikeDto CreateOrEdit(TScienceCommentLikeDto input) {
        //声明一个科普评论点赞实体
        TScienceCommentLike TScienceCommentLike=input.MapToEntity();  
        //调用数据库的增加或者修改方法
        saveOrUpdate(TScienceCommentLike);
        //把传输模型返回给前端
        return TScienceCommentLike.MapToDto();
    }
    /**
     * 科普评论点赞删除
     */
    @Override
    public void Delete(IdInput input) {
        TScienceCommentLike entity = TScienceCommentLikeMapper.selectById(input.getId());
        TScienceCommentLikeMapper.deleteById(entity);
    }

    /**
     * 科普评论点赞批量删除
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
     * 科普评论点赞导出
     */
    @Override
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        TScienceCommentLikePagedInput input = mapper.readValue(query, TScienceCommentLikePagedInput.class);
        List<TScienceCommentLikeDto> items =List(input).getItems();
        //声明一个工作簿
        HSSFWorkbook workbook = new HSSFWorkbook();
        //生成一个表格，设置表格名称为"科普评论点赞"
        HSSFSheet sheet = workbook.createSheet("科普评论点赞表");
        //设置表格列宽度为10个字节
        sheet.setDefaultColumnWidth(30);
        //创建标题的显示样式
        HSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.YELLOW.index);
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        //创建第一行表头
        HSSFRow headrow = sheet.createRow(0);
        //表头数据
        String[] header = {"名称" ,"评论内容" ,"软删除标记" ,};   
        //遍历添加表头(下面模拟遍历科普评论点赞，也是同样的操作过程)
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
            TScienceCommentLikeDto dto = items.get(i);
            //创建一行
            HSSFRow row = sheet.createRow(i + 1);    
   
              if (dto.getUserDto()!=null&&Extension.isNotNullOrEmpty(dto.getUserDto().getName())) { 
                row.createCell(0).setCellValue(new HSSFRichTextString(dto.getUserDto().getName()));
              }
              if (dto.getCommentDto()!=null&&Extension.isNotNullOrEmpty(dto.getCommentDto().getCommentContent())) { 
                row.createCell(1).setCellValue(new HSSFRichTextString(dto.getCommentDto().getCommentContent()));
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
