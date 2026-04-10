/*
  科普互动表：增加“防重复”唯一键与常用索引

  注意：
  - 如果历史数据存在重复（同一 UserId + ScienceId 多条记录），加唯一键会失败
  - 建议先清理重复数据（保留最早或最新一条），再执行本脚本
*/

SET FOREIGN_KEY_CHECKS=0;

-- 1) 科普点赞：同一用户同一文章只保留一条记录（通过 IsDelete/CancelLikeTime 表示取消）
ALTER TABLE `tsciencelike`
  ADD UNIQUE KEY `uk_sciencelike_user_science` (`UserId`, `ScienceId`),
  ADD INDEX `idx_sciencelike_science` (`ScienceId`),
  ADD INDEX `idx_sciencelike_user` (`UserId`),
  ADD INDEX `idx_sciencelike_like_time` (`LikeTime`);

-- 2) 科普收藏：同一用户同一文章只保留一条记录
ALTER TABLE `tsciencecollect`
  ADD UNIQUE KEY `uk_sciencecollect_user_science` (`UserId`, `ScienceId`),
  ADD INDEX `idx_sciencecollect_science` (`ScienceId`),
  ADD INDEX `idx_sciencecollect_user` (`UserId`),
  ADD INDEX `idx_sciencecollect_collect_time` (`CollectTime`);

-- 3) 科普评论：索引（用于按文章拉评论、按父评论取楼中楼）
ALTER TABLE `tsciencecomment`
  ADD INDEX `idx_sciencecomment_science` (`ScienceId`),
  ADD INDEX `idx_sciencecomment_user` (`UserId`),
  ADD INDEX `idx_sciencecomment_parent` (`ParentCommentId`),
  ADD INDEX `idx_sciencecomment_audit` (`AuditStatus`),
  ADD INDEX `idx_sciencecomment_create_time` (`CreationTime`);

-- 4) 科普评论点赞：同一用户同一评论只保留一条记录
ALTER TABLE `tsciencecommentlike`
  ADD UNIQUE KEY `uk_sciencecommentlike_user_comment` (`UserId`, `CommentId`),
  ADD INDEX `idx_sciencecommentlike_comment` (`CommentId`),
  ADD INDEX `idx_sciencecommentlike_user` (`UserId`),
  ADD INDEX `idx_sciencecommentlike_like_time` (`LikeTime`);

SET FOREIGN_KEY_CHECKS=1;

