package com.example.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.web.dto.TAudioAiReportDto;
import com.example.web.dto.query.TAudioAiReportPagedInput;
import com.example.web.entity.TAudioAiReport;
import com.example.web.mapper.TAudioAiReportMapper;
import com.example.web.service.TAudioAiReportService;
import com.example.web.tools.Extension;
import com.example.web.tools.dto.IdInput;
import com.example.web.tools.dto.IdsInput;
import com.example.web.tools.dto.PagedResult;
import com.example.web.tools.exception.CustomException;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 音频检测AI解读报告表功能实现类
 */
@Service
public class TAudioAiReportServiceImpl extends ServiceImpl<TAudioAiReportMapper, TAudioAiReport>
        implements TAudioAiReportService {

    @Autowired
    private TAudioAiReportMapper TAudioAiReportMapper;

    private LambdaQueryWrapper<TAudioAiReport> BuilderQuery(TAudioAiReportPagedInput input) {
        LambdaQueryWrapper<TAudioAiReport> queryWrapper = Wrappers.<TAudioAiReport>lambdaQuery()
                .eq(input.getId() != null && input.getId() != 0, TAudioAiReport::getId, input.getId())
                .eq(input.getUserId() != null && input.getUserId() != 0, TAudioAiReport::getUserId, input.getUserId())
                .eq(input.getAudioScreenRecordId() != null && input.getAudioScreenRecordId() != 0,
                        TAudioAiReport::getAudioScreenRecordId, input.getAudioScreenRecordId())
                .eq(input.getIsDelete() != null, TAudioAiReport::getIsDelete, input.getIsDelete());

        if (Extension.isNotNullOrEmpty(input.getReportType())) {
            queryWrapper = queryWrapper.eq(TAudioAiReport::getReportType, input.getReportType());
        }
        if (Extension.isNotNullOrEmpty(input.getRiskLevel())) {
            queryWrapper = queryWrapper.eq(TAudioAiReport::getRiskLevel, input.getRiskLevel());
        }
        return queryWrapper;
    }

    @SneakyThrows
    @Override
    public PagedResult<TAudioAiReportDto> List(TAudioAiReportPagedInput input) {
        LambdaQueryWrapper<TAudioAiReport> queryWrapper = BuilderQuery(input);
        queryWrapper = queryWrapper.orderByDesc(TAudioAiReport::getCreationTime);

        Page<TAudioAiReport> page = new Page<>(input.getPage(), input.getLimit());
        IPage<TAudioAiReport> pageRecords = TAudioAiReportMapper.selectPage(page, queryWrapper);
        Long totalCount = TAudioAiReportMapper.selectCount(queryWrapper);
        List<TAudioAiReportDto> items = Extension.copyBeanList(pageRecords.getRecords(), TAudioAiReportDto.class);
        return PagedResult.GetInstance(items, totalCount);
    }

    @SneakyThrows
    @Override
    public TAudioAiReportDto Get(TAudioAiReportPagedInput input) {
        if (input.getId() == null) {
            return new TAudioAiReportDto();
        }
        PagedResult<TAudioAiReportDto> paged = List(input);
        return paged.getItems().stream().findFirst().orElse(new TAudioAiReportDto());
    }

    @SneakyThrows
    @Override
    public TAudioAiReportDto CreateOrEdit(TAudioAiReportDto input) {
        if (input == null) {
            throw new CustomException("参数不能为空");
        }
        TAudioAiReport entity = input.MapToEntity();
        entity.setUpdateTime(LocalDateTime.now());
        if (entity.getIsDelete() == null) {
            entity.setIsDelete(false);
        }
        saveOrUpdate(entity);
        // 回填 id
        input.setId(entity.getId());
        return input;
    }

    @Override
    public void Delete(IdInput input) {
        if (input == null || input.getId() == null) {
            throw new CustomException("缺少Id");
        }
        TAudioAiReport entity = TAudioAiReportMapper.selectById(input.getId());
        if (entity == null) {
            return;
        }
        // 软删
        entity.setIsDelete(true);
        entity.setUpdateTime(LocalDateTime.now());
        TAudioAiReportMapper.updateById(entity);
    }

    @Override
    public void BatchDelete(IdsInput input) {
        if (input == null || input.getIds() == null) {
            return;
        }
        for (Integer id : input.getIds()) {
            IdInput idInput = new IdInput();
            idInput.setId(id);
            Delete(idInput);
        }
    }
}

