-- 创建 TPersonalHealthRemind 个性化健康提醒表
-- 执行前请根据实际库名调整 USE 语句或在合适的库下执行

USE HealthControl;

DROP TABLE IF EXISTS `TPersonalHealthRemind`;
CREATE TABLE `TPersonalHealthRemind` (
  `Id` int(11) NOT NULL AUTO_INCREMENT COMMENT '个性化健康提醒主键',
  `CreationTime` datetime DEFAULT NULL COMMENT '创建时间',
  `UpdateTime` datetime DEFAULT NULL COMMENT '推送时间更新',
  `UserId` int(11) DEFAULT NULL COMMENT '关联用户表主键t_user.',
  `RemindType` tinyint(1) DEFAULT NULL COMMENT '提醒类型：0=定期自查，1=护喉饮食，2=发音训练，3=饮水',
  `RemindTime` varchar(8) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '提醒时间：如08:00',
  `RepeatFrequency` tinyint(1) DEFAULT NULL COMMENT '重复频率：0=每天，1=每周一至五，2=每周一次',
  `RemindStatus` tinyint(1) DEFAULT NULL COMMENT '提醒状态：0=关闭，1=开启',
  `LastPushTime` datetime DEFAULT NULL COMMENT '最后推送时间',
  `RemindContent` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '提醒内容',
  `IsDelete` tinyint(1) DEFAULT NULL COMMENT '软删除标记：0=未删除，1=已删除',
  PRIMARY KEY (`Id`) USING BTREE,
  KEY `UserId` (`UserId`),
  CONSTRAINT `tpersonalhealthremind_ibfk_1` FOREIGN KEY (`UserId`) REFERENCES `appuser` (`Id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='个性化健康提醒表';

