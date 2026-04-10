-- 修复 TSymptomLog 表结构
-- 将 SymptomType 从整数类型改为 VARCHAR 类型
-- 确保 SymptomLevel 是 INT 类型

USE HealthControl;

-- 1. 修改 SymptomType 字段为 VARCHAR(50)
ALTER TABLE `TSymptomLog` 
MODIFY COLUMN `SymptomType` VARCHAR(50) COMMENT '症状类型：如嘶哑、疼痛、吞咽困难等';

-- 2. 确保 SymptomLevel 是 INT 类型（如果还不是的话）
ALTER TABLE `TSymptomLog` 
MODIFY COLUMN `SymptomLevel` INT COMMENT '症状轻重：1=轻度，2=中度，3=重度';

-- 3. 查看修改后的表结构（验证用）
DESCRIBE `TSymptomLog`;
