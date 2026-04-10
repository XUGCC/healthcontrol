/*
  健康科普分类表（thealthsciencecategory）升级：支持多级分类（ParentId/Path/Level）
*/

SET FOREIGN_KEY_CHECKS=0;

-- 1) 多级分类字段
ALTER TABLE `thealthsciencecategory`
  ADD COLUMN `ParentId` int(11) NULL DEFAULT NULL COMMENT '父级分类Id；NULL/0 表示一级分类' AFTER `Id`,
  ADD COLUMN `Level` int(11) NOT NULL DEFAULT 1 COMMENT '层级：1=一级，2=二级...' AFTER `ParentId`,
  ADD COLUMN `Path` varchar(255) NULL COMMENT '路径：如 /1/12/（用于快速查询子树，可选）' AFTER `Level`,
  ADD COLUMN `IconUrl` varchar(255) NULL COMMENT '分类图标URL（可选）' AFTER `CategoryDesc`;

-- 2) 索引
ALTER TABLE `thealthsciencecategory`
  ADD INDEX `idx_science_category_parent` (`ParentId`),
  ADD INDEX `idx_science_category_sort` (`SortNum`),
  ADD INDEX `idx_science_category_show` (`ShowStatus`);

SET FOREIGN_KEY_CHECKS=1;

