/*
  健康科普表（thealthscience）补充索引脚本（增量）

  背景：
  - alter_thealthscience_upgrade_content_and_audit.sql 已补充了常用单列索引（Category/Creator/Type/Show/Audit/PublishTime/TopWeight）
  - 但前台列表常用过滤条件包含 IsDelete，且经常组合过滤 + 排序，需要补充组合索引提升性能

  说明：
  - 这是增量脚本，不修改 healthcontrol.sql 原始建表文件
  - 若你已手动建过同名索引，请删除对应语句或改索引名
*/

SET FOREIGN_KEY_CHECKS=0;

-- 1) 软删除过滤索引（前台/后台通用）
ALTER TABLE `thealthscience`
  ADD INDEX `idx_science_isdelete` (`IsDelete`);

-- 2) 前台列表常用组合过滤 + 时间排序
ALTER TABLE `thealthscience`
  ADD INDEX `idx_science_front_list` (`AuditStatus`, `ShowStatus`, `IsDelete`, `PublishTime`);

-- 3) 分类列表常用组合过滤 + 时间排序
ALTER TABLE `thealthscience`
  ADD INDEX `idx_science_category_front_list` (`CategoryId`, `AuditStatus`, `ShowStatus`, `IsDelete`, `PublishTime`);

-- 4) 推荐排序索引（置顶 + 权重 + 发布时间）
ALTER TABLE `thealthscience`
  ADD INDEX `idx_science_recommend_order` (`IsTop`, `RecommendWeight`, `PublishTime`);

SET FOREIGN_KEY_CHECKS=1;

