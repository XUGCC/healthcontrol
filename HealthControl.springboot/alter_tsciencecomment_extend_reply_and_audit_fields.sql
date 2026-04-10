/*
  科普评论表（tsciencecomment）补齐“楼中楼 + 审核审计”字段（增量）

  目标：
  - 支持 RootCommentId：主楼/子楼快速查询
  - 支持 ReplyToUserId：展示“回复某人”并用于消息通知
  - 补齐审核审计：AuditorId/AuditTime/RejectReason/AuditRemark

  说明：
  - 本脚本为增量脚本，不修改 healthcontrol.sql 原始建表文件
  - 若你已存在同名字段/索引，请删除对应语句或改名
*/

SET FOREIGN_KEY_CHECKS=0;

-- 1) 楼中楼所需字段
ALTER TABLE `tsciencecomment`
  ADD COLUMN `RootCommentId` int(11) NULL COMMENT '根评论Id（主楼Id）；主楼=自身Id，子楼=主楼Id' AFTER `ParentCommentId`,
  ADD COLUMN `ReplyToUserId` int(11) NULL COMMENT '被回复用户Id（用于展示“回复@某人”与通知）' AFTER `RootCommentId`;

-- 2) 审核审计字段（补齐驳回原因与审核人信息）
ALTER TABLE `tsciencecomment`
  ADD COLUMN `AuditorId` int(11) NULL COMMENT '审核人Id（管理员）' AFTER `AuditStatus`,
  ADD COLUMN `AuditTime` datetime NULL COMMENT '审核时间' AFTER `AuditorId`,
  ADD COLUMN `RejectReason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '驳回原因（对用户可见）' AFTER `AuditTime`,
  ADD COLUMN `AuditRemark` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审核备注（内部）' AFTER `RejectReason`;

-- 3) 常用索引（评论列表/子回复展开/审核列表）
ALTER TABLE `tsciencecomment`
  ADD INDEX `idx_comment_science_parent_time` (`ScienceId`, `ParentCommentId`, `CreationTime`),
  ADD INDEX `idx_comment_root` (`RootCommentId`),
  ADD INDEX `idx_comment_audit_list` (`AuditStatus`, `IsDelete`, `CreationTime`);

-- 4) 兼容性数据修复（尽力而为的 RootCommentId 回填）
-- 主楼：RootCommentId = Id
UPDATE `tsciencecomment`
SET `RootCommentId` = `Id`
WHERE (`ParentCommentId` IS NULL OR `ParentCommentId` = 0) AND `RootCommentId` IS NULL;

-- 子楼：若 RootCommentId 为空，先把 RootCommentId 回填为 ParentCommentId（后续若存在多级嵌套，可在业务层继续修正）
UPDATE `tsciencecomment`
SET `RootCommentId` = `ParentCommentId`
WHERE (`ParentCommentId` IS NOT NULL AND `ParentCommentId` <> 0) AND `RootCommentId` IS NULL;

SET FOREIGN_KEY_CHECKS=1;

