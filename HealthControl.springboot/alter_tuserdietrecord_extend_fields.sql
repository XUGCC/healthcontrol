-- 扩展 tuserdietrecord 表字段，支持区分三餐/加餐与结构化食后感受
-- 设计依据：饮食管理与推荐模块-需求.md（11.2），healthcontrol.sql 中 tuserdietrecord 现有结构
-- 注意：如果某些字段或索引已存在，执行时会报 "Duplicate column name"/"Duplicate key name"，可忽略该错误

USE HealthControl;

ALTER TABLE `tuserdietrecord`
  ADD COLUMN `MealType` TINYINT NULL COMMENT '用餐类型：0=早餐,1=午餐,2=晚餐,3=加餐/零食' AFTER `IntakeFrequency`,
  ADD COLUMN `FeelingCode` TINYINT NULL COMMENT '食后感受枚举：0=未填写,1=缓解,2=无明显变化,3=稍有不适,4=明显加重' AFTER `EatFeeling`,
  ADD COLUMN `SourceType` TINYINT NULL COMMENT '记录来源：0=用户手动,1=系统推荐一键添加' AFTER `FeelingCode`;

-- 按用户+时间的组合索引，便于统计近期饮食行为
ALTER TABLE `tuserdietrecord`
  ADD KEY `idx_diet_user_time` (`UserId`,`IntakeTime`);

