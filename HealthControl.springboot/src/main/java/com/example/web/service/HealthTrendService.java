package com.example.web.service;

/**
 * 健康趋势计算服务接口
 * 用于计算健康趋势标签和风险等级
 */
public interface HealthTrendService {
    /**
     * 计算健康趋势标签
     * @param userId 用户ID
     * @return 趋势标签：好转/稳定/恶化/无数据
     */
    String calculateHealthTrend(Integer userId);
    
    /**
     * 计算风险等级
     * @param userId 用户ID
     * @return 风险等级：0=低，1=中，2=高，null=未评定
     */
    Integer calculateRiskLevel(Integer userId);
}
