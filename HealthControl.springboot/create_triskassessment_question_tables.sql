/*
  风险评估问卷 - 题目体系（可配置问卷）扩展表

  目的：
  - 支持管理员/用户创建“标准化问卷”并配置题目、选项、计分规则
  - 与现有 triskassessmentquestionnaire / tuserquestionnaireanswer 兼容（旧的固定维度字段仍可用）
*/

SET FOREIGN_KEY_CHECKS=0;

-- 问卷题目表
DROP TABLE IF EXISTS `triskassessmentquestion`;
CREATE TABLE `triskassessmentquestion` (
  `Id` int(11) NOT NULL AUTO_INCREMENT COMMENT '问卷题目主键',
  `CreationTime` datetime DEFAULT NULL COMMENT '创建时间',
  `UpdateTime` datetime DEFAULT NULL COMMENT '更新时间',
  `QuestionnaireId` int(11) DEFAULT NULL COMMENT '关联问卷主表Id',
  `DimensionCode` varchar(32) DEFAULT NULL COMMENT '维度编码：SMOKING/DRINKING/OCCUPATIONAL_VOICE/ACID_REFLEX/OTHER 等',
  `QuestionTitle` varchar(255) DEFAULT NULL COMMENT '题干',
  `QuestionDesc` varchar(255) DEFAULT NULL COMMENT '题目说明（可选）',
  `QuestionType` tinyint(1) NOT NULL DEFAULT 1 COMMENT '题型：1=单选，2=多选，3=是/否，4=填空，5=量表(0-10) 等',
  `IsRequired` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否必答：0=否，1=是',
  `SortNum` int(11) NOT NULL DEFAULT 0 COMMENT '排序值：升序',
  `ScoreRuleJson` text DEFAULT NULL COMMENT '计分规则JSON（可选，用于复杂计分）',
  `IsDelete` tinyint(1) NOT NULL DEFAULT 0 COMMENT '软删除：0=未删除，1=已删除',
  PRIMARY KEY (`Id`) USING BTREE,
  KEY `idx_questionnaire` (`QuestionnaireId`),
  KEY `idx_dimension` (`DimensionCode`),
  CONSTRAINT `fk_risk_question_questionnaire` FOREIGN KEY (`QuestionnaireId`) REFERENCES `triskassessmentquestionnaire` (`Id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='风险评估问卷题目表';

-- 题目选项表（用于单选/多选/是非题）
DROP TABLE IF EXISTS `triskassessmentquestionoption`;
CREATE TABLE `triskassessmentquestionoption` (
  `Id` int(11) NOT NULL AUTO_INCREMENT COMMENT '题目选项主键',
  `CreationTime` datetime DEFAULT NULL COMMENT '创建时间',
  `UpdateTime` datetime DEFAULT NULL COMMENT '更新时间',
  `QuestionId` int(11) DEFAULT NULL COMMENT '关联题目Id',
  `OptionText` varchar(255) DEFAULT NULL COMMENT '选项文本',
  `OptionValue` varchar(64) DEFAULT NULL COMMENT '选项值（前端提交用）',
  `ScoreValue` int(11) NOT NULL DEFAULT 0 COMMENT '该选项得分（简单计分）',
  `SortNum` int(11) NOT NULL DEFAULT 0 COMMENT '排序值：升序',
  `IsDelete` tinyint(1) NOT NULL DEFAULT 0 COMMENT '软删除：0=未删除，1=已删除',
  PRIMARY KEY (`Id`) USING BTREE,
  KEY `idx_question` (`QuestionId`),
  CONSTRAINT `fk_risk_option_question` FOREIGN KEY (`QuestionId`) REFERENCES `triskassessmentquestion` (`Id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='风险评估问卷题目选项表';

SET FOREIGN_KEY_CHECKS=1;

