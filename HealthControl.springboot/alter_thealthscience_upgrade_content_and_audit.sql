/*
  健康科普表（thealthscience）字段升级：支持富文本 + 审核/发布/推荐

  说明：
  - 本脚本为“增量脚本”，不修改 healthcontrol.sql 原始建表文件
  - 如果你的数据库表名是大小写敏感（Linux 且 lower_case_table_names=0），请把表名改为实际表名
*/

SET FOREIGN_KEY_CHECKS=0;

-- 1) 扩展正文为富文本（原 ScienceContent 为 varchar(128)）
ALTER TABLE `thealthscience`
  MODIFY COLUMN `ScienceContent` LONGTEXT NULL COMMENT '科普内容（富文本/HTML/Markdown，建议存 HTML）';

-- 2) 新增摘要（用于列表展示与 SEO）
ALTER TABLE `thealthscience`
  ADD COLUMN `Summary` varchar(512) NULL COMMENT '科普摘要（列表页使用，若为空可由前端/后端从正文截取）' AFTER `ScienceContent`;

-- 3) 审核/发布字段
ALTER TABLE `thealthscience`
  ADD COLUMN `AuditStatus` tinyint(1) NOT NULL DEFAULT 1 COMMENT '审核状态：0=草稿，1=待审核，2=通过，3=驳回，4=下架/屏蔽' AFTER `ShowStatus`,
  ADD COLUMN `AuditorId` int(11) NULL COMMENT '审核人Id（管理员）' AFTER `AuditStatus`,
  ADD COLUMN `AuditTime` datetime NULL COMMENT '审核时间' AFTER `AuditorId`,
  ADD COLUMN `RejectReason` varchar(255) NULL COMMENT '驳回原因（对作者可见）' AFTER `AuditTime`,
  ADD COLUMN `AuditRemark` varchar(255) NULL COMMENT '审核备注（内部）' AFTER `RejectReason`,
  ADD COLUMN `PublishTime` datetime NULL COMMENT '发布时间（通过后写入）' AFTER `AuditRemark`;

-- 4) 运营推荐字段
ALTER TABLE `thealthscience`
  ADD COLUMN `IsTop` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否置顶：0=否，1=是' AFTER `PublishTime`,
  ADD COLUMN `RecommendWeight` int(11) NOT NULL DEFAULT 0 COMMENT '推荐权重（越大越靠前，用于首页/列表推荐）' AFTER `IsTop`;

-- 5) 互动计数字段（可选：用于提升列表性能；也可不使用、改为聚合统计）
ALTER TABLE `thealthscience`
  ADD COLUMN `LikeCount` int(11) NOT NULL DEFAULT 0 COMMENT '点赞数（冗余，可选）' AFTER `RecommendWeight`,
  ADD COLUMN `CollectCount` int(11) NOT NULL DEFAULT 0 COMMENT '收藏数（冗余，可选）' AFTER `LikeCount`,
  ADD COLUMN `CommentCount` int(11) NOT NULL DEFAULT 0 COMMENT '评论数（冗余，可选）' AFTER `CollectCount`;

-- 6) 常用索引（用于前台列表/管理端筛选）
ALTER TABLE `thealthscience`
  ADD INDEX `idx_science_category` (`CategoryId`),
  ADD INDEX `idx_science_creator` (`CreatorId`),
  ADD INDEX `idx_science_type` (`KnowledgeType`),
  ADD INDEX `idx_science_show` (`ShowStatus`),
  ADD INDEX `idx_science_audit` (`AuditStatus`),
  ADD INDEX `idx_science_publish_time` (`PublishTime`),
  ADD INDEX `idx_science_top_weight` (`IsTop`, `RecommendWeight`);

SET FOREIGN_KEY_CHECKS=1;

