-- 优化 tdietrecommendrule 字段：补齐 RiskLevelScope/IsEnabled，并修正 RecommendPriority 语义为 1~5 数字优先级
-- 适用：HealthControl 项目（饮食管理与推荐模块）
-- 执行前请先备份数据库

USE HealthControl;

-- 1) RecommendPriority：原字段为 tinyint(1)，实际可存 1~5；这里仅确保 COMMENT 正确并允许 NULL
ALTER TABLE `tdietrecommendrule`
  MODIFY COLUMN `RecommendPriority` TINYINT NULL COMMENT '推荐优先级：1=最高，5=最低' AFTER `RecommendCategoryId`;

-- 2) 新增字段（若已存在可忽略 Duplicate column name）
ALTER TABLE `tdietrecommendrule`
  ADD COLUMN `RiskLevelScope` VARCHAR(16) COLLATE utf8mb4_unicode_ci NULL
    COMMENT '适用风险等级：如 LOW,MID,HIGH,ANY，多值用,分隔；也兼容 0/1/2'
    AFTER `PrimaryScreenRelation`,
  ADD COLUMN `IsEnabled` TINYINT(1) NULL DEFAULT 1
    COMMENT '是否启用：0=停用,1=启用'
    AFTER `IsDelete`;

-- 3) 数据修正：把历史 NULL 默认成启用
UPDATE `tdietrecommendrule`
SET `IsEnabled` = 1
WHERE `IsEnabled` IS NULL;

