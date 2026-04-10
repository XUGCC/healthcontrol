/*
  用户问卷答题表（tuserquestionnaireanswer）扩展：支持“可配置题目体系”的答案与计分明细
*/

SET FOREIGN_KEY_CHECKS=0;

ALTER TABLE `tuserquestionnaireanswer`
  ADD COLUMN `AnswerJson` LONGTEXT NULL COMMENT '用户答案JSON（可配置问卷：题目Id -> 选项/文本）' AFTER `OtherRiskFactor`,
  ADD COLUMN `ScoreDetailJson` TEXT NULL COMMENT '计分明细JSON（可选）' AFTER `AnswerJson`,
  ADD COLUMN `RiskLevel` tinyint(1) NULL COMMENT '风险等级：0=低，1=中，2=高（提交时计算）' AFTER `RiskAssessmentScore`,
  ADD COLUMN `ResultSummary` varchar(255) NULL COMMENT '结果摘要（用于前端快速展示）' AFTER `RiskLevel`;

-- 常用索引：用于查询用户历史、按问卷统计
ALTER TABLE `tuserquestionnaireanswer`
  ADD INDEX `idx_answer_user` (`UserId`),
  ADD INDEX `idx_answer_questionnaire` (`QuestionnaireId`),
  ADD INDEX `idx_answer_time` (`AnswerTime`);

SET FOREIGN_KEY_CHECKS=1;

