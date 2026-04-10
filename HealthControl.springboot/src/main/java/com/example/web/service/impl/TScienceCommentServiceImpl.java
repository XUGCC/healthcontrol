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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.lang.reflect.InvocationTargetException;
import java.util.stream.Collectors;
import java.util.List;
import java.util.HashMap;
import lombok.SneakyThrows;
import java.io.IOException;
import com.example.web.tools.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.springframework.web.bind.annotation.RequestParam;
/**
 * 科普评论功能实现类
 */
@Service
public class TScienceCommentServiceImpl extends ServiceImpl<TScienceCommentMapper, TScienceComment> implements TScienceCommentService {

	 /**
     * 操作数据库AppUser表mapper对象
     */
    @Autowired
    private AppUserMapper AppUserMapper;
    /**
     * 操作数据库的TScienceComment表mapper对象
     */
    @Autowired
    private TScienceCommentMapper TScienceCommentMapper;
    @Autowired
    private THealthScienceMapper  THealthScienceMapper;
    @Autowired
    private TScienceCommentLikeMapper TScienceCommentLikeMapper;


  
   /**
     * 构建表查询sql
     */
    private LambdaQueryWrapper<TScienceComment> BuilderQuery(TScienceCommentPagedInput input) {
       //声明一个支持科普评论查询的(拉姆达)表达式
        LambdaQueryWrapper<TScienceComment> queryWrapper = Wrappers.<TScienceComment>lambdaQuery()
                .eq(input.getId() != null && input.getId() != 0, TScienceComment::getId, input.getId());
   //如果前端搜索传入查询条件则拼接查询条件
        if (Extension.isNotNullOrEmpty(input.getCommentContent())) {
             queryWrapper = queryWrapper.like(TScienceComment::getCommentContent, input.getCommentContent());
       	 }
        if (Extension.isNotNullOrEmpty(input.getReplyContent())) {
             queryWrapper = queryWrapper.like(TScienceComment::getReplyContent, input.getReplyContent());
       	 }

        if (input.getUserId() != null) {
            queryWrapper = queryWrapper.eq(TScienceComment::getUserId, input.getUserId());
       	 }

        if (input.getScienceId() != null) {
            queryWrapper = queryWrapper.eq(TScienceComment::getScienceId, input.getScienceId());
       	 }

        if (input.getParentCommentId() != null) {
            queryWrapper = queryWrapper.eq(TScienceComment::getParentCommentId, input.getParentCommentId());
       	 }
        if (input.getUpdateTimeRange() != null && !input.getUpdateTimeRange().isEmpty()) {
            queryWrapper = queryWrapper.le(TScienceComment::getUpdateTime, input.getUpdateTimeRange().get(1));
            queryWrapper = queryWrapper.ge(TScienceComment::getUpdateTime, input.getUpdateTimeRange().get(0));
        }
        if (input.getAuditStatus() != null) {
            queryWrapper = queryWrapper.eq(TScienceComment::getAuditStatus, input.getAuditStatus());
       	 }
        if (input.getIsDelete() != null) {
            queryWrapper = queryWrapper.eq(TScienceComment::getIsDelete, input.getIsDelete());
       	 }
      

 
 
     if(Extension.isNotNullOrEmpty(input.getKeyWord()))
        {
			queryWrapper=queryWrapper.and(i->i
          	   .like(TScienceComment::getCommentContent,input.getKeyWord()).or()   	 
          	   .like(TScienceComment::getReplyContent,input.getKeyWord()).or()   	 
        );
                                       
 		   }
    
      return queryWrapper;
    }
  
    /**
     * 处理科普评论对于的外键数据
     */
   private List<TScienceCommentDto> DispatchItem(List<TScienceCommentDto> items) throws InvocationTargetException, IllegalAccessException {
          
       for (TScienceCommentDto item : items) {           
          	            
           //查询出关联的AppUser表信息           
            AppUser  UserEntity= AppUserMapper.selectById(item.getUserId());
            item.setUserDto(UserEntity!=null?UserEntity.MapToDto():new AppUserDto());              
           
          	            
           //查询出关联的THealthScience表信息           
            THealthScience  ScienceEntity= THealthScienceMapper.selectById(item.getScienceId());
            item.setScienceDto(ScienceEntity!=null?ScienceEntity.MapToDto():new THealthScienceDto());              
           
          	            
           //查询出关联的TScienceComment表信息           
            TScienceComment  ParentCommentEntity= TScienceCommentMapper.selectById(item.getParentCommentId());
            item.setParentCommentDto(ParentCommentEntity!=null?ParentCommentEntity.MapToDto():new TScienceCommentDto());              
       }
       
     return items; 
   }

   /**
    * 为评论补充点赞数量
    */
   private void FillLikeCount(List<TScienceCommentDto> items) {
       if (items == null || items.isEmpty()) return;
       List<Integer> ids = items.stream()
               .map(TScienceCommentDto::getId)
               .filter(x -> x != null && x > 0)
               .distinct()
               .collect(Collectors.toList());
       if (ids.isEmpty()) return;

       List<TScienceCommentLike> likes = TScienceCommentLikeMapper.selectList(
               Wrappers.<TScienceCommentLike>lambdaQuery()
                       .in(TScienceCommentLike::getCommentId, ids)
                       .and(w -> w.isNull(TScienceCommentLike::getIsDelete).or().eq(TScienceCommentLike::getIsDelete, false))
       );
       HashMap<Integer, Long> map = likes.stream()
               .collect(Collectors.groupingBy(TScienceCommentLike::getCommentId, HashMap::new, Collectors.counting()));

       for (TScienceCommentDto dto : items) {
           Integer id = dto.getId();
           if (id == null) continue;
           dto.setLikeCount(map.getOrDefault(id, 0L).intValue());
       }
   }
  
    /**
     * 科普评论分页查询
     */
    @SneakyThrows
    @Override
    public PagedResult<TScienceCommentDto> List(TScienceCommentPagedInput input) {
			//构建where条件+排序
        LambdaQueryWrapper<TScienceComment> queryWrapper = BuilderQuery(input);
        // 动态排序处理
        if (input.getSortItem() != null) {
            // 根据字段名动态排序
            queryWrapper.last("ORDER BY " + input.getSortItem().getFieldName()
                    + (input.getSortItem().getIsAsc() ? " ASC" : " DESC"));
        } else {
            // 默认按创建时间从大到小排序
            queryWrapper = queryWrapper.orderByDesc(TScienceComment::getCreationTime);
        }

        //构建一个分页查询的model
        Page<TScienceComment> page = new Page<>(input.getPage(), input.getLimit());
         //从数据库进行分页查询获取科普评论数据
        IPage<TScienceComment> pageRecords= TScienceCommentMapper.selectPage(page, queryWrapper);
        //获取所有满足条件的数据行数
        Long totalCount= TScienceCommentMapper.selectCount(queryWrapper);
        //把TScienceComment实体转换成TScienceComment传输模型
        List<TScienceCommentDto> items= Extension.copyBeanList(pageRecords.getRecords(),TScienceCommentDto.class);
        DispatchItem(items);
        FillLikeCount(items);
        //返回一个分页结构给前端
        return PagedResult.GetInstance(items,totalCount);

    }
  
    /**
     * 单个科普评论查询
     */
    @SneakyThrows
    @Override
    public TScienceCommentDto Get(TScienceCommentPagedInput input) {
       if(input.getId()==null)
        {
         return new TScienceCommentDto();
        }
      
       PagedResult<TScienceCommentDto> pagedResult = List(input);
        return pagedResult.getItems().stream().findFirst().orElse(new TScienceCommentDto()); 
    }

    /**
     *科普评论创建或者修改
     */
    @SneakyThrows
    @Override
    public TScienceCommentDto CreateOrEdit(TScienceCommentDto input) {
        //声明一个科普评论实体
        TScienceComment TScienceComment=input.MapToEntity();  
        //调用数据库的增加或者修改方法
        saveOrUpdate(TScienceComment);
        //把传输模型返回给前端
        return TScienceComment.MapToDto();
    }
    /**
     * 科普评论删除
     */
    @Override
    public void Delete(IdInput input) {
        TScienceComment entity = TScienceCommentMapper.selectById(input.getId());
        TScienceCommentMapper.deleteById(entity);
    }

    /**
     * 科普评论批量删除
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
     * 科普评论导出
     */
    @Override
    public void Export(@RequestParam String query, HttpServletResponse response) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        TScienceCommentPagedInput input = mapper.readValue(query, TScienceCommentPagedInput.class);
        List<TScienceCommentDto> items =List(input).getItems();
        //声明一个工作簿
        HSSFWorkbook workbook = new HSSFWorkbook();
        //生成一个表格，设置表格名称为"科普评论"
        HSSFSheet sheet = workbook.createSheet("科普评论表");
        //设置表格列宽度为10个字节
        sheet.setDefaultColumnWidth(30);
        //创建标题的显示样式
        HSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.YELLOW.index);
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        //创建第一行表头
        HSSFRow headrow = sheet.createRow(0);
        //表头数据
        String[] header = {"名称" ,"科普标题" ,"评论内容" ,"评论内容" ,"回复内容" ,"审核状态" ,"软删除标记" ,};   
        //遍历添加表头(下面模拟遍历科普评论，也是同样的操作过程)
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
            TScienceCommentDto dto = items.get(i);
            //创建一行
            HSSFRow row = sheet.createRow(i + 1);    
   
              if (dto.getUserDto()!=null&&Extension.isNotNullOrEmpty(dto.getUserDto().getName())) { 
                row.createCell(0).setCellValue(new HSSFRichTextString(dto.getUserDto().getName()));
              }
              if (dto.getScienceDto()!=null&&Extension.isNotNullOrEmpty(dto.getScienceDto().getTitle())) { 
                row.createCell(1).setCellValue(new HSSFRichTextString(dto.getScienceDto().getTitle()));
              }
              if (dto.getParentCommentDto()!=null&&Extension.isNotNullOrEmpty(dto.getParentCommentDto().getCommentContent())) { 
                row.createCell(2).setCellValue(new HSSFRichTextString(dto.getParentCommentDto().getCommentContent()));
              }
              if (Extension.isNotNullOrEmpty(dto.getCommentContent())) { 
                row.createCell(3).setCellValue(new HSSFRichTextString(dto.getCommentContent()));
              }
              if (Extension.isNotNullOrEmpty(dto.getReplyContent())) { 
                row.createCell(4).setCellValue(new HSSFRichTextString(dto.getReplyContent()));
              }
              if (dto.getAuditStatus() != null) { 
                row.createCell(5).setCellValue(new HSSFRichTextString(dto.getAuditStatus() == 1 ? "通过" : dto.getAuditStatus() == 0 ? "待审" : dto.getAuditStatus() == 2 ? "驳回" : "屏蔽"));
              }
              if (dto.getIsDelete() != null) { 
                row.createCell(6).setCellValue(new HSSFRichTextString(dto.getIsDelete() == 1 ? "是" : "否"));
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
