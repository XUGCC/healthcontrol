-- 新建 tusermedicalvisitplan 表：用户就诊计划/预约记录
-- 用途：记录用户“计划去就诊”的安排，并可与通知联动

USE healthcontrol;

CREATE TABLE `tusermedicalvisitplan` (
  `Id` int(11) NOT NULL AUTO_INCREMENT COMMENT '就诊计划主键',
  `CreationTime` datetime DEFAULT NULL COMMENT '创建时间',
  `UpdateTime` datetime DEFAULT NULL COMMENT '更新时间',
  `UserId` int(11) DEFAULT NULL COMMENT '用户Id',
  `RecommendId` int(11) DEFAULT NULL COMMENT '关联 tusermedicalrecommend.Id（可空）',
  `DoctorId` int(11) DEFAULT NULL COMMENT '关联 totolaryngologyhospitaldoctor.Id（可空）',
  `PlanVisitDate` datetime DEFAULT NULL COMMENT '计划就诊日期（可空）',
  `PlanChannel` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '就诊渠道：电话/平台/现场等',
  `Status` tinyint(1) DEFAULT '0' COMMENT '状态：0=计划中,1=已就诊,2=改期,3=取消',
  `Remark` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注（可选）',
  `IsDelete` tinyint(1) DEFAULT '0' COMMENT '软删除标记：0=未删除，1=已删除',
  PRIMARY KEY (`Id`) USING BTREE,
  KEY `idx_user_status_date` (`UserId`,`Status`,`PlanVisitDate`),
  CONSTRAINT `tusermedicalvisitplan_ibfk_user` FOREIGN KEY (`UserId`) REFERENCES `appuser` (`Id`) ON DELETE SET NULL,
  CONSTRAINT `tusermedicalvisitplan_ibfk_recommend` FOREIGN KEY (`RecommendId`) REFERENCES `tusermedicalrecommend` (`Id`) ON DELETE SET NULL,
  CONSTRAINT `tusermedicalvisitplan_ibfk_doctor` FOREIGN KEY (`DoctorId`) REFERENCES `totolaryngologyhospitaldoctor` (`Id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户就诊计划/预约记录表';

