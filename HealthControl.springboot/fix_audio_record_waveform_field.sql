-- 为 TAudioScreenRecord 表添加波形图 URL 字段
-- 执行前请备份数据库

ALTER TABLE `TAudioScreenRecord`
  ADD COLUMN `WaveformUrl` VARCHAR(255) NULL COMMENT '音频波形图URL' AFTER `AudioFileSize`;

-- 可选：验证字段是否添加成功
-- SELECT COLUMN_NAME, DATA_TYPE, COLUMN_COMMENT
-- FROM INFORMATION_SCHEMA.COLUMNS
-- WHERE TABLE_NAME = 'TAudioScreenRecord'
--   AND COLUMN_NAME = 'WaveformUrl';

