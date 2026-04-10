-- 新建 tmedicaldictionary 表：医疗字典表（地区/科室/标签等）
-- 用途：统一医院/医生相关的地区、科室、标签编码，减少脏数据，便于前端筛选
-- 该表为增强能力，MVP 可按需选择是否执行本脚本

USE healthcontrol;

CREATE TABLE `tmedicaldictionary` (
  `Id` int(11) NOT NULL AUTO_INCREMENT COMMENT '字典主键',
  `CreationTime` datetime DEFAULT NULL COMMENT '创建时间',
  `DictType` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '字典类型：Region/Department/Tag 等',
  `Code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '编码值',
  `Name` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '显示名称',
  `SortNum` int(11) DEFAULT '0' COMMENT '排序值：升序',
  `IsEnabled` tinyint(1) DEFAULT '1' COMMENT '是否启用：0=停用，1=启用',
  `IsDelete` tinyint(1) DEFAULT '0' COMMENT '软删除标记：0=未删除，1=已删除',
  PRIMARY KEY (`Id`) USING BTREE,
  KEY `idx_dict_type` (`DictType`,`IsEnabled`,`IsDelete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医疗字典表（地区/科室/标签等）';

