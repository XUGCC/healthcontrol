-- 为 TAudioScreenRecord 表添加语音性别相关字段
-- 执行前请备份数据库

-- 添加语音性别字段（女性/男性）
ALTER TABLE `TAudioScreenRecord`
ADD COLUMN `VoiceGender` VARCHAR(10) NULL COMMENT '语音性别：女性/男性（来源于 Siamese 多任务模型）' AFTER `PrimaryScreenConfidence`;

-- 添加语音性别编码字段（0=女性，1=男性）
ALTER TABLE `TAudioScreenRecord`
ADD COLUMN `VoiceGenderCode` INT NULL COMMENT '语音性别编码：0=女性，1=男性' AFTER `VoiceGender`;

-- 添加语音性别置信度字段（0-1）
ALTER TABLE `TAudioScreenRecord`
ADD COLUMN `VoiceGenderConfidence` DOUBLE NULL COMMENT '语音性别置信度（0-1）' AFTER `VoiceGenderCode`;

--验证字段是否添加成功
SELECT COLUMN_NAME, DATA_TYPE, COLUMN_COMMENT 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'TAudioScreenRecord' 
AND COLUMN_NAME IN ('VoiceGender', 'VoiceGenderCode', 'VoiceGenderConfidence');
