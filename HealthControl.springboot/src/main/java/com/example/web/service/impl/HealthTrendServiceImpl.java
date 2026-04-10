package com.example.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.web.entity.TAudioScreenRecord;
import com.example.web.entity.TSymptomLog;
import com.example.web.mapper.TAudioScreenRecordMapper;
import com.example.web.mapper.TSymptomLogMapper;
import com.example.web.service.HealthTrendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * 健康趋势计算服务实现类
 */
@Service
public class HealthTrendServiceImpl implements HealthTrendService {
    
    @Autowired
    private TAudioScreenRecordMapper audioScreenRecordMapper;
    
    @Autowired
    private TSymptomLogMapper symptomLogMapper;
    
    @Override
    public String calculateHealthTrend(Integer userId) {
        // 1. 查询最近3次有效检测记录（状态为完成）
        LambdaQueryWrapper<TAudioScreenRecord> queryWrapper = Wrappers
            .<TAudioScreenRecord>lambdaQuery()
            .eq(TAudioScreenRecord::getUserId, userId)
            .eq(TAudioScreenRecord::getDetectTotalStatus, true) // 只查询已完成的
            .eq(TAudioScreenRecord::getIsDelete, false)
            .orderByDesc(TAudioScreenRecord::getCreationTime)
            .last("LIMIT 3");
        
        List<TAudioScreenRecord> records = audioScreenRecordMapper.selectList(queryWrapper);
        
        // 2. 判断数据量
        if (records == null || records.size() < 3) {
            return "无数据";
        }
        
        // 3. 获取最近一次和前一次的结果
        TAudioScreenRecord latest = records.get(0);
        TAudioScreenRecord previous = records.get(1);
        
        // 4. 判定趋势
        Boolean latestResult = latest.getPrimaryScreenResult(); // true=异常，false=正常
        Boolean previousResult = previous.getPrimaryScreenResult();
        Double latestConfidence = latest.getPrimaryScreenConfidence();
        Double previousConfidence = previous.getPrimaryScreenConfidence();
        
        // 情况1：结果反转（异常→正常 = 好转，正常→异常 = 恶化）
        if (latestResult == false && previousResult == true) {
            return "好转";
        } else if (latestResult == true && previousResult == false) {
            return "恶化";
        }
        
        // 情况2：结果一致，看置信度变化
        if (latestResult == previousResult) {
            if (latestResult == true) {
                // 都是异常，置信度下降>10% = 好转
                if (previousConfidence != null && latestConfidence != null) {
                    double diff = previousConfidence - latestConfidence;
                    if (diff > 0.1) {
                        return "好转";
                    } else if (diff < -0.1) {
                        return "恶化";
                    }
                }
            } else {
                // 都是正常，置信度变化在±5%以内 = 稳定
                if (previousConfidence != null && latestConfidence != null) {
                    double diff = Math.abs(previousConfidence - latestConfidence);
                    if (diff <= 0.05) {
                        return "稳定";
                    }
                }
            }
        }
        
        // 默认返回稳定
        return "稳定";
    }
    
    @Override
    public Integer calculateRiskLevel(Integer userId) {
        int score = 0;
        
        // 1. 检测结果得分（0-60分）
        LambdaQueryWrapper<TAudioScreenRecord> detectQuery = Wrappers
            .<TAudioScreenRecord>lambdaQuery()
            .eq(TAudioScreenRecord::getUserId, userId)
            .eq(TAudioScreenRecord::getDetectTotalStatus, true)
            .eq(TAudioScreenRecord::getIsDelete, false)
            .orderByDesc(TAudioScreenRecord::getCreationTime)
            .last("LIMIT 1");
        
        TAudioScreenRecord latestDetect = audioScreenRecordMapper.selectOne(detectQuery);
        
        if (latestDetect != null && latestDetect.getPrimaryScreenResult() != null) {
            Boolean isMalignant = latestDetect.getPrimaryScreenResult();
            if (isMalignant != null && isMalignant) {
                // 异常（恶性）
                Double confidence = latestDetect.getPrimaryScreenConfidence();
                if (confidence != null) {
                    if (confidence > 0.7) {
                        score += 60; // 高置信度
                    } else if (confidence > 0.5) {
                        score += 40; // 中置信度
                    } else {
                        score += 20; // 低置信度
                    }
                } else {
                    score += 40; // 默认中置信度
                }
            }
            // 正常（良性）不加分
        }
        
        // 2. 症状严重程度得分（0-30分）
        LambdaQueryWrapper<TSymptomLog> symptomQuery = Wrappers
            .<TSymptomLog>lambdaQuery()
            .eq(TSymptomLog::getUserId, userId)
            .eq(TSymptomLog::getIsDelete, false)
            .orderByDesc(TSymptomLog::getCreationTime)
            .last("LIMIT 1");
        
        TSymptomLog latestSymptom = symptomLogMapper.selectOne(symptomQuery);
        
        // 注意：当前实体类中 SymptomLevel 是 Boolean 类型，但根据需求应该是 Integer（1=轻度，2=中度，3=重度）
        // 这里暂时跳过症状严重程度的得分计算，等待数据库字段类型确认
        // TODO: 需要修改 TSymptomLog 实体类和 TSymptomLogDto 中的 SymptomLevel 字段类型为 Integer
        // 然后可以按以下逻辑计算得分：
        // if (latestSymptom != null && latestSymptom.getSymptomLevel() != null) {
        //     Integer level = latestSymptom.getSymptomLevel();
        //     if (level == 3) {
        //         score += 30; // 重度
        //     } else if (level == 2) {
        //         score += 15; // 中度
        //     }
        //     // 轻度（1）不加分
        // }
        
        // 3. 持续时间得分（-5到10分）
        if (latestSymptom != null && latestSymptom.getSymptomDuration() != null) {
            String duration = latestSymptom.getSymptomDuration();
            int durationDays = parseDurationToDays(duration);
            
            if (durationDays > 30) {
                score += 10; // >1个月
            } else if (durationDays < 7) {
                score = Math.max(0, score - 5); // <1周，最低0分
            }
            // 1周-1个月不加分
        }
        
        // 4. 评定等级
        if (score < 40) {
            return 0; // 低风险
        } else if (score < 70) {
            return 1; // 中风险
        } else {
            return 2; // 高风险
        }
    }
    
    /**
     * 解析持续时间字符串为天数
     * 如："3天" -> 3, "2周" -> 14, "1个月" -> 30
     */
    private int parseDurationToDays(String duration) {
        if (duration == null || duration.isEmpty()) {
            return 0;
        }
        
        Pattern pattern = Pattern.compile("(\\d+)(天|周|个月|月)");
        Matcher matcher = pattern.matcher(duration);
        
        if (matcher.find()) {
            int number = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);
            
            switch (unit) {
                case "天":
                    return number;
                case "周":
                    return number * 7;
                case "个月":
                case "月":
                    return number * 30;
                default:
                    return 0;
            }
        }
        
        return 0;
    }
}
