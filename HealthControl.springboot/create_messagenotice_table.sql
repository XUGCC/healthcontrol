-- 创建 MessageNotice 消息通知表
-- 执行前请根据实际库名调整 USE 语句或在合适的库下执行

USE HealthControl;

DROP TABLE IF EXISTS `MessageNotice`;
CREATE TABLE `MessageNotice` (
  `Id` int(11) NOT NULL AUTO_INCREMENT COMMENT '消息通知主键',
  `CreationTime` datetime DEFAULT NULL COMMENT '创建时间',
  `Title` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '通知标题',
  `Content` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '通知内容',
  `Type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '通知类型：CHECKUP/VOICE_CARE/FOLLOWUP/ANNOUNCEMENT/邮箱等',
  `TargetKey` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '目标业务主键或邮箱等：如检测Id/档案Id/邮箱地址',
  `PlanSendTime` datetime DEFAULT NULL COMMENT '计划发送时间',
  `ActualSendTime` datetime DEFAULT NULL COMMENT '实际发送时间',
  `UserId` int(11) DEFAULT NULL COMMENT '接收用户Id',
  `IsSend` tinyint(1) DEFAULT '0' COMMENT '是否已触发发送：0=未发送，1=已发送',
  `IsSuccess` tinyint(1) DEFAULT '1' COMMENT '发送是否成功：0=失败，1=成功',
  `IsRead` tinyint(1) DEFAULT '0' COMMENT '是否已读：0=未读，1=已读',
  `ResultMsg` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '发送结果说明或失败原因',
  `RelationNo` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关联单号（预留）',
  `ActionType` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '前端点击行为类型：如GOTO_AUDIO_DETECT等',
  `ActionParam` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '行为参数：如DetectId',
  PRIMARY KEY (`Id`) USING BTREE,
  KEY `UserId` (`UserId`),
  KEY `PlanSendTime` (`PlanSendTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息通知表';

