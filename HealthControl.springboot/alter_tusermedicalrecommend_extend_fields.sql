-- 为 tusermedicalrecommend（用户就医推荐记录表）增加增强字段
-- 用途：支持推荐解释、反馈、去重与来源标识
-- 注意：如果字段或索引已存在，执行时会报错 "Duplicate column name / Duplicate key name"，可以忽略该错误

USE healthcontrol;

ALTER TABLE `tusermedicalrecommend`
  ADD COLUMN `PrimaryScreenResult` tinyint(1) DEFAULT NULL COMMENT '推荐时初筛结果快照：0=良性,1=恶性' AFTER `DetectId`,
  ADD COLUMN `PrimaryScreenConfidence` double(20,8) DEFAULT NULL COMMENT '推荐时初筛置信度快照' AFTER `PrimaryScreenResult`,
  ADD COLUMN `SymptomSnapshot` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '症状快照文本/JSON' AFTER `RiskLevel`,
  ADD COLUMN `RecommendReason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '推荐原因解释文案' AFTER `SymptomSnapshot`,
  ADD COLUMN `FeedbackType` tinyint(1) DEFAULT NULL COMMENT '用户反馈：0=未反馈,1=已就医,2=暂不需要,3=信息不准等' AFTER `ViewStatus`,
  ADD COLUMN `FeedbackRemark` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户反馈备注' AFTER `FeedbackType`,
  ADD COLUMN `DedupKey` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '推荐去重键，如用户+时间窗口' AFTER `FeedbackRemark`,
  ADD COLUMN `SourceType` tinyint(1) DEFAULT NULL COMMENT '推荐来源：0=系统自动,1=管理员手动,2=用户自主创建' AFTER `DedupKey`;

-- 一次检测只生成一条推荐（推荐使用）
ALTER TABLE `tusermedicalrecommend`
  ADD UNIQUE KEY `uk_medical_recommend_user_detect` (`UserId`,`DetectId`);

-- 可选：按用户 + 去重键控制推荐频率（如 7 天窗口）
ALTER TABLE `tusermedicalrecommend`
  ADD KEY `idx_medical_recommend_user_dedup` (`UserId`,`DedupKey`);

