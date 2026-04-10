-- 新建 tusermedicalfavorite 表：用户收藏医院/医生
-- 用途：支撑小程序端“收藏医生/医院”和“我的收藏”功能

USE healthcontrol;

CREATE TABLE `tusermedicalfavorite` (
  `Id` int(11) NOT NULL AUTO_INCREMENT COMMENT '用户收藏主键',
  `CreationTime` datetime DEFAULT NULL COMMENT '创建时间',
  `UserId` int(11) DEFAULT NULL COMMENT '用户Id',
  `DoctorId` int(11) DEFAULT NULL COMMENT '关联 totolaryngologyhospitaldoctor.Id',
  `Remark` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户备注（可选）',
  `IsDelete` tinyint(1) DEFAULT '0' COMMENT '软删除标记：0=未删除，1=已删除',
  PRIMARY KEY (`Id`) USING BTREE,
  UNIQUE KEY `uk_user_doctor` (`UserId`,`DoctorId`),
  KEY `idx_favorite_doctor` (`DoctorId`),
  CONSTRAINT `tusermedicalfavorite_ibfk_user` FOREIGN KEY (`UserId`) REFERENCES `appuser` (`Id`) ON DELETE SET NULL,
  CONSTRAINT `tusermedicalfavorite_ibfk_doctor` FOREIGN KEY (`DoctorId`) REFERENCES `totolaryngologyhospitaldoctor` (`Id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏医院/医生表';

