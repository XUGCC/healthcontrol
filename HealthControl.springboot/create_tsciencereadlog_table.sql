/*
  新增：科普阅读去重日志表（tsciencereadlog）

  用途：
  - 支撑 /Front/Science/Read 的“30 分钟窗口去重”
  - 支撑热度统计（7/30 天趋势），防刷

  去重策略：
  - 登录用户：UNIQUE(UserId, ScienceId, WindowStartTime)
  - 未登录用户：UNIQUE(ClientKey, ScienceId, WindowStartTime)
*/

SET FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS `tsciencereadlog`;
CREATE TABLE `tsciencereadlog` (
  `Id` int(11) NOT NULL AUTO_INCREMENT COMMENT '阅读日志主键',
  `CreationTime` datetime DEFAULT NULL COMMENT '创建时间',
  `UserId` int(11) DEFAULT NULL COMMENT '用户Id（登录时写入）',
  `ClientKey` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '客户端去重Key（未登录时写入，前端本地生成并持久化）',
  `ScienceId` int(11) DEFAULT NULL COMMENT '科普Id',
  `WindowStartTime` datetime DEFAULT NULL COMMENT '30分钟窗口起始时间（用于去重）',
  `ReadScene` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '阅读入口：HOME/LIST/SHARE',
  `ReadDurationSec` int(11) DEFAULT NULL COMMENT '阅读时长（秒，可选）',
  `IsDelete` tinyint(1) NOT NULL DEFAULT 0 COMMENT '软删除：0=未删除，1=已删除',
  PRIMARY KEY (`Id`) USING BTREE,
  UNIQUE KEY `uk_read_user_science_window` (`UserId`, `ScienceId`, `WindowStartTime`),
  UNIQUE KEY `uk_read_client_science_window` (`ClientKey`, `ScienceId`, `WindowStartTime`),
  KEY `idx_read_science_time` (`ScienceId`, `WindowStartTime`),
  KEY `idx_read_user_time` (`UserId`, `WindowStartTime`),
  KEY `idx_read_client_time` (`ClientKey`, `WindowStartTime`),
  CONSTRAINT `tsciencereadlog_ibfk_1` FOREIGN KEY (`UserId`) REFERENCES `appuser` (`Id`) ON DELETE SET NULL,
  CONSTRAINT `tsciencereadlog_ibfk_2` FOREIGN KEY (`ScienceId`) REFERENCES `thealthscience` (`Id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='科普阅读去重日志表';

SET FOREIGN_KEY_CHECKS=1;

