-- 扩展 tlaryngealfood 表字段以支持更精细的饮食标签与分级展示
-- 设计依据：饮食管理与推荐模块-需求.md（11.1），healthcontrol.sql 中 tlaryngealfood 现有结构
-- 注意：如果某些字段或索引已存在，执行时会报 "Duplicate column name"/"Duplicate key name"，可忽略该错误

USE HealthControl;

-- 新增友好程度与标签等字段
ALTER TABLE `tlaryngealfood`
  ADD COLUMN `FriendlyLevel` TINYINT NULL COMMENT '友好程度：1=非常友好,2=较友好,3=中性,4=轻度刺激,5=强刺激' AFTER `SuggestContent`,
  ADD COLUMN `TasteTag` VARCHAR(64) COLLATE utf8mb4_unicode_ci NULL COMMENT '口味/特征标签：如清淡,辛辣,油炸,寒凉,温补，多值用,分隔' AFTER `FriendlyLevel`,
  ADD COLUMN `SuitPeople` VARCHAR(128) COLLATE utf8mb4_unicode_ci NULL COMMENT '适宜人群说明（可选）' AFTER `TasteTag`,
  ADD COLUMN `AvoidPeople` VARCHAR(128) COLLATE utf8mb4_unicode_ci NULL COMMENT '不宜人群说明（可选）' AFTER `SuitPeople`;

-- 补充索引（若已存在可忽略错误）
ALTER TABLE `tlaryngealfood`
  ADD KEY `idx_lfood_category` (`CategoryId`);

ALTER TABLE `tlaryngealfood`
  ADD KEY `idx_lfood_friendly` (`FriendlyLevel`);

