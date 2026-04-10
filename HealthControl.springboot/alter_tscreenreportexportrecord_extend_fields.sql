-- 为 tscreenreportexportrecord（自查报告导出记录表）增加增强字段
-- 用途：精确追溯导出的是哪份报告，并记录导出用途与来源
-- 注意：如果字段或索引已存在，执行时会报错 "Duplicate column name / Duplicate key name"，可以忽略该错误

USE healthcontrol;

ALTER TABLE `tscreenreportexportrecord`
  ADD COLUMN `ReportId` int(11) DEFAULT NULL COMMENT '关联 tusermedicalpreparereport.Id' AFTER `DetectId`,
  ADD COLUMN `ExportPurposeText` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '导出用途补充说明' AFTER `ExportPurpose`,
  ADD COLUMN `SourceType` tinyint(1) DEFAULT NULL COMMENT '来源：0=用户手动导出，1=分享导出等' AFTER `ExportPurposeText`;

ALTER TABLE `tscreenreportexportrecord`
  ADD KEY `ReportId` (`ReportId`);

