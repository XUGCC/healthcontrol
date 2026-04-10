/*
  新增：科普内容举报表（tsciencereport）

  用途：
  - 用户举报文章/评论
  - 管理端处理：记录处理人/时间/备注，可联动下架/屏蔽与通知
*/

SET FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS `tsciencereport`;
CREATE TABLE `tsciencereport` (
  `Id` int(11) NOT NULL AUTO_INCREMENT COMMENT '举报主键',
  `CreationTime` datetime DEFAULT NULL COMMENT '创建时间',
  `ReporterUserId` int(11) DEFAULT NULL COMMENT '举报人用户Id',
  `TargetType` tinyint(1) NOT NULL DEFAULT 1 COMMENT '举报对象类型：1=文章，2=评论',
  `TargetId` int(11) DEFAULT NULL COMMENT '对象Id（文章Id或评论Id）',
  `ReasonType` tinyint(1) DEFAULT NULL COMMENT '原因类型：1=不实/夸大，2=广告营销，3=隐私泄露，4=辱骂攻击，9=其他',
  `ReasonDesc` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '原因说明（可选）',
  `HandleStatus` tinyint(1) NOT NULL DEFAULT 0 COMMENT '处理状态：0=待处理，1=已处理，2=无效/驳回',
  `HandlerId` int(11) DEFAULT NULL COMMENT '处理人Id（管理员）',
  `HandleTime` datetime DEFAULT NULL COMMENT '处理时间',
  `HandleRemark` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '处理备注（内部）',
  `IsDelete` tinyint(1) NOT NULL DEFAULT 0 COMMENT '软删除：0=未删除，1=已删除',
  PRIMARY KEY (`Id`) USING BTREE,
  KEY `idx_report_status_time` (`HandleStatus`, `CreationTime`),
  KEY `idx_report_target` (`TargetType`, `TargetId`),
  KEY `idx_report_reporter` (`ReporterUserId`, `CreationTime`),
  CONSTRAINT `tsciencereport_ibfk_1` FOREIGN KEY (`ReporterUserId`) REFERENCES `appuser` (`Id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='科普内容举报表';

SET FOREIGN_KEY_CHECKS=1;

