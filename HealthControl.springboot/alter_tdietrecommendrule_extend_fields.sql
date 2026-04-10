-- 扩展 tdietrecommendrule 表字段，增强规则适用范围控制与启停管理
-- 设计依据：饮食管理与推荐模块-需求.md（11.3），healthcontrol.sql 中 tdietrecommendrule 现有结构
-- 注意：如果字段已存在，执行时会报 "Duplicate column name"，可以忽略该错误

USE HealthControl;

ALTER TABLE `tdietrecommendrule`
  ADD COLUMN `RiskLevelScope` VARCHAR(16) COLLATE utf8mb4_unicode_ci NULL
    COMMENT '适用风险等级：如 LOW,MID,HIGH,ANY，多值用,分隔；对应 tpersonallaryngealhealthrecord.RiskAssessmentLevel'
    AFTER `PrimaryScreenRelation`,
  ADD COLUMN `IsEnabled` TINYINT(1) NULL DEFAULT 1
    COMMENT '是否启用：0=停用,1=启用'
    AFTER `IsDelete`;

