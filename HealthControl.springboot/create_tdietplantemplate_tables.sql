-- 新增饮食计划模板相关表：tdietplantemplate、tdietplantemplateitem
-- 用于实现“一周护嗓饮食计划”等功能
-- 设计依据：饮食管理与推荐模块-需求.md（11.4）

USE HealthControl;

-- 为避免外键依赖问题，先删明细表再删主表（如已存在）
DROP TABLE IF EXISTS `tdietplantemplateitem`;
DROP TABLE IF EXISTS `tdietplantemplate`;

CREATE TABLE `tdietplantemplate` (
  `Id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '饮食计划模板主键',
  `CreationTime` DATETIME DEFAULT NULL COMMENT '创建时间',
  `CreatorId` INT(11) DEFAULT NULL COMMENT '创建人 Id（管理员/医生）',
  `TemplateName` VARCHAR(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '模板名称，如7天护嗓计划（轻度症状）',
  `TargetRiskLevel` VARCHAR(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '目标风险等级：LOW/MID/HIGH/ANY',
  `TargetSymptom` VARCHAR(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '主要适用症状，多值用,分隔',
  `Desc` VARCHAR(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '模板说明',
  `IsDelete` TINYINT(1) DEFAULT '0' COMMENT '软删除标记：0=未删除，1=已删除',
  PRIMARY KEY (`Id`) USING BTREE,
  KEY `idx_dietplan_creator` (`CreatorId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='饮食计划模板主表';

CREATE TABLE `tdietplantemplateitem` (
  `Id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '饮食计划模板明细主键',
  `CreationTime` DATETIME DEFAULT NULL COMMENT '创建时间',
  `TemplateId` INT(11) DEFAULT NULL COMMENT '关联模板主表',
  `DayIndex` TINYINT(1) DEFAULT NULL COMMENT '第几天：1-7',
  `MealType` TINYINT(1) DEFAULT NULL COMMENT '用餐类型：0=早餐,1=午餐,2=晚餐,3=加餐',
  `RecommendCategoryId` INT(11) DEFAULT NULL COMMENT '推荐的食物分类 Id（tlaryngealfoodcategory.Id）',
  `RecommendFoodIds` VARCHAR(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '推荐食物 Id 列表，逗号分隔（可选）',
  `SuggestText` VARCHAR(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '推荐说明文案',
  `IsDelete` TINYINT(1) DEFAULT '0' COMMENT '软删除标记：0=未删除，1=已删除',
  PRIMARY KEY (`Id`) USING BTREE,
  KEY `idx_dietplan_template` (`TemplateId`,`DayIndex`,`MealType`),
  CONSTRAINT `tdietplantemplateitem_ibfk_1` FOREIGN KEY (`TemplateId`)
    REFERENCES `tdietplantemplate` (`Id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='饮食计划模板明细表';

