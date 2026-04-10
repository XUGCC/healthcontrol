-- 为 tusermedicalpreparereport（用户就诊准备报告表）增加增强字段
-- 用途：支持报告标题/编号、主检测 Id、文档类型与内容快照
-- 注意：如果字段已存在，执行时会报错 "Duplicate column name"，可以忽略该错误

USE healthcontrol;

ALTER TABLE `tusermedicalpreparereport`
  ADD COLUMN `UpdateTime` datetime DEFAULT NULL COMMENT '更新时间/生成完成时间' AFTER `CreationTime`,
  ADD COLUMN `PrimaryDetectId` int(11) DEFAULT NULL COMMENT '主关联检测 Id，便于聚合与导出' AFTER `HealthRecordId`,
  ADD COLUMN `ReportTitle` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '报告标题，如就诊准备报告（简版）' AFTER `TemplateType`,
  ADD COLUMN `ReportNo` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '报告编号（便于审计）' AFTER `ReportTitle`,
  ADD COLUMN `StandardDocType` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文档类型：HTML/PDF/IMAGE' AFTER `StandardDocUrl`,
  ADD COLUMN `ReportContentJson` longtext COLLATE utf8mb4_unicode_ci COMMENT '报告内容快照 JSON' AFTER `StandardDocType`,
  ADD COLUMN `HtmlDocUrl` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'HTML 文档 URL（可选）' AFTER `ReportContentJson`,
  ADD COLUMN `PdfDocUrl` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'PDF 文档 URL（可选）' AFTER `HtmlDocUrl`,
  ADD COLUMN `CoverImageUrl` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '报告封面图 URL（可选）' AFTER `PdfDocUrl`;

