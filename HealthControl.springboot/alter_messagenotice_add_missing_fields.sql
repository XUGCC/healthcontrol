-- 为 MessageNotice 表添加缺失的字段
-- 注意：如果字段已存在，执行时会报错 "Duplicate column name"，可以忽略该错误
-- 建议先检查表结构，只执行缺失字段的 ALTER TABLE 语句

USE HealthControl;

-- 添加 RelationNo 字段
-- 如果字段已存在，会报错 "Duplicate column name 'RelationNo'"，可以忽略
ALTER TABLE `MessageNotice` 
ADD COLUMN `RelationNo` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关联单号（预留）';

-- 添加 ActionType 字段
-- 如果字段已存在，会报错 "Duplicate column name 'ActionType'"，可以忽略
ALTER TABLE `MessageNotice` 
ADD COLUMN `ActionType` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '前端点击行为类型：如GOTO_AUDIO_DETECT等';

-- 添加 ActionParam 字段
-- 如果字段已存在，会报错 "Duplicate column name 'ActionParam'"，可以忽略
ALTER TABLE `MessageNotice` 
ADD COLUMN `ActionParam` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '行为参数：如DetectId';
