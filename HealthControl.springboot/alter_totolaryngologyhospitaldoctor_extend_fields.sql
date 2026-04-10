-- 为 totolaryngologyhospitaldoctor（耳鼻喉医院医生表）增加增强字段
-- 用途：支持地图导航、标签、医院等级、擅长方向等前台展示与筛选
-- 注意：如果字段已存在，执行时会报错 "Duplicate column name"，可以忽略该错误

USE healthcontrol;

ALTER TABLE `totolaryngologyhospitaldoctor`
  ADD COLUMN `HospitalLevel` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '医院等级/类型：三甲/专科等',
  ADD COLUMN `Tags` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标签：如嗓音门诊,肿瘤方向，逗号分隔',
  ADD COLUMN `SpecialtyDesc` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '擅长方向简介',
  ADD COLUMN `Longitude` decimal(10,6) DEFAULT NULL COMMENT '经度',
  ADD COLUMN `Latitude` decimal(10,6) DEFAULT NULL COMMENT '纬度',
  ADD COLUMN `OutpatientTime` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '门诊时间说明',
  ADD COLUMN `DoctorTitle` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '职称，如主任医师',
  ADD COLUMN `PicUrl` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像/门诊配图',
  ADD COLUMN `UpdateTime` datetime DEFAULT NULL COMMENT '记录更新时间';

