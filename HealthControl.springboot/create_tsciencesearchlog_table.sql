/*
  新增：科普搜索日志表（tsciencesearchlog）

  用途：
  - 支撑“搜索历史/热词/运营分析”
  - 不影响前台主链路：可异步写入（增强）
*/

SET FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS `tsciencesearchlog`;
CREATE TABLE `tsciencesearchlog` (
  `Id` int(11) NOT NULL AUTO_INCREMENT COMMENT '搜索日志主键',
  `CreationTime` datetime DEFAULT NULL COMMENT '创建时间',
  `UserId` int(11) DEFAULT NULL COMMENT '用户Id（登录时写入）',
  `ClientKey` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '客户端Key（未登录时写入）',
  `Keyword` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关键词（建议做 trim + 长度限制）',
  `SearchScene` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '搜索入口：HOME/LIST',
  `ResultCount` int(11) DEFAULT NULL COMMENT '本次搜索结果数量（可选）',
  `IsDelete` tinyint(1) NOT NULL DEFAULT 0 COMMENT '软删除：0=未删除，1=已删除',
  PRIMARY KEY (`Id`) USING BTREE,
  KEY `idx_search_keyword_time` (`Keyword`, `CreationTime`),
  KEY `idx_search_user_time` (`UserId`, `CreationTime`),
  KEY `idx_search_client_time` (`ClientKey`, `CreationTime`),
  CONSTRAINT `tsciencesearchlog_ibfk_1` FOREIGN KEY (`UserId`) REFERENCES `appuser` (`Id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='科普搜索日志表';

SET FOREIGN_KEY_CHECKS=1;

