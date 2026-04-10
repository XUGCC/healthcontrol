SET NAMES utf8mb4;

START TRANSACTION;

SET @fixture_base = 'http://localhost:7245/test-fixtures';

/*
  1. 用户头像
  覆盖现有前台用户，导入后个人中心/健康档案可直接看到头像。
*/
UPDATE `appuser`
SET `ImageUrls` = CASE `Id`
    WHEN 2 THEN CONCAT(@fixture_base, '/avatars/appuser-2.png')
    WHEN 3 THEN CONCAT(@fixture_base, '/avatars/appuser-3.png')
    WHEN 4 THEN CONCAT(@fixture_base, '/avatars/appuser-4.png')
    ELSE `ImageUrls`
END
WHERE `Id` IN (2, 3, 4);

/*
  2. 科普封面图 + 正文插图
  使用独立测试文章，避免污染现有文章内容；同时覆盖 CoverUrl 和 ScienceContent 内嵌图片。
*/
SET @science_cover = CONCAT(@fixture_base, '/science/science-cover-90001.png');
SET @science_inline_1 = CONCAT(@fixture_base, '/science/science-inline-breathing.png');
SET @science_inline_2 = CONCAT(@fixture_base, '/science/science-inline-hydration.png');
SET @science_content = CONCAT(
    '<p>这是一篇用于联调的测试科普文章，目标是验证小程序中的科普封面、正文段落与内嵌图片都能正常显示。</p>',
    '<p>建议在发声前先做 1 到 3 分钟轻柔热身，例如鼻吸口呼、唇颤音和轻声哼鸣，避免一上来就高强度用嗓。</p>',
    '<p><img src="', @science_inline_1, '" alt="发声热身测试图" style="width:100%;display:block;margin:16px 0;border-radius:12px;" /></p>',
    '<p>日常护嗓的基础动作是补水。少量多次饮用温水，比一次性大量灌水更容易保持咽喉湿润感。</p>',
    '<p><img src="', @science_inline_2, '" alt="补水步骤测试图" style="width:100%;display:block;margin:16px 0;border-radius:12px;" /></p>',
    '<p>这条数据可用于校验首页推荐、列表卡片、详情富文本与收藏列表的图片渲染链路。</p>'
);

INSERT INTO `thealthscience` (
    `Id`, `CreationTime`, `UpdateTime`, `CreatorId`, `CategoryId`, `Title`, `CoverUrl`, `ScienceContent`,
    `Summary`, `ReadCount`, `KnowledgeType`, `ShowStatus`, `AuditStatus`, `AuditorId`, `AuditTime`,
    `RejectReason`, `AuditRemark`, `PublishTime`, `IsTop`, `RecommendWeight`, `LikeCount`,
    `CollectCount`, `CommentCount`, `IsDelete`
) VALUES (
    90001, '2026-03-31 10:00:00', '2026-03-31 10:05:00', 2, 5,
    '测试用：发声热身与护嗓补水示例',
    @science_cover,
    @science_content,
    '用于验证科普封面、正文富文本插图和详情页渲染的测试文章。',
    0, 2, 1, 2, 1, '2026-03-31 10:06:00',
    NULL, '测试夹具文章', '2026-03-31 10:10:00', 1, 999, 0, 0, 0, 0
)
ON DUPLICATE KEY UPDATE
    `UpdateTime` = VALUES(`UpdateTime`),
    `CreatorId` = VALUES(`CreatorId`),
    `CategoryId` = VALUES(`CategoryId`),
    `Title` = VALUES(`Title`),
    `CoverUrl` = VALUES(`CoverUrl`),
    `ScienceContent` = VALUES(`ScienceContent`),
    `Summary` = VALUES(`Summary`),
    `ReadCount` = VALUES(`ReadCount`),
    `KnowledgeType` = VALUES(`KnowledgeType`),
    `ShowStatus` = VALUES(`ShowStatus`),
    `AuditStatus` = VALUES(`AuditStatus`),
    `AuditorId` = VALUES(`AuditorId`),
    `AuditTime` = VALUES(`AuditTime`),
    `RejectReason` = VALUES(`RejectReason`),
    `AuditRemark` = VALUES(`AuditRemark`),
    `PublishTime` = VALUES(`PublishTime`),
    `IsTop` = VALUES(`IsTop`),
    `RecommendWeight` = VALUES(`RecommendWeight`),
    `LikeCount` = VALUES(`LikeCount`),
    `CollectCount` = VALUES(`CollectCount`),
    `CommentCount` = VALUES(`CommentCount`),
    `IsDelete` = VALUES(`IsDelete`);

/*
  3. 饮食食物配图
  覆盖每个可见分类的前 3 个示例食物，便于首页推荐、列表页和详情页稳定展示。
*/
UPDATE `tlaryngealfood`
SET `PicUrl` = CASE `Id`
    WHEN 30001 THEN CONCAT(@fixture_base, '/foods/30001-warm-water.png')
    WHEN 30002 THEN CONCAT(@fixture_base, '/foods/30002-honey-water.png')
    WHEN 30003 THEN CONCAT(@fixture_base, '/foods/30003-steamed-pear.png')
    WHEN 30011 THEN CONCAT(@fixture_base, '/foods/30011-millet-porridge.png')
    WHEN 30012 THEN CONCAT(@fixture_base, '/foods/30012-steamed-egg.png')
    WHEN 30013 THEN CONCAT(@fixture_base, '/foods/30013-tofu.png')
    WHEN 30021 THEN CONCAT(@fixture_base, '/foods/30021-yam.png')
    WHEN 30022 THEN CONCAT(@fixture_base, '/foods/30022-lotus-soup.png')
    WHEN 30023 THEN CONCAT(@fixture_base, '/foods/30023-red-date.png')
    WHEN 30031 THEN CONCAT(@fixture_base, '/foods/30031-blueberry.png')
    WHEN 30032 THEN CONCAT(@fixture_base, '/foods/30032-broccoli.png')
    WHEN 30033 THEN CONCAT(@fixture_base, '/foods/30033-kiwi.png')
    WHEN 30041 THEN CONCAT(@fixture_base, '/foods/30041-steamed-fish.png')
    WHEN 30042 THEN CONCAT(@fixture_base, '/foods/30042-chicken-breast.png')
    WHEN 30043 THEN CONCAT(@fixture_base, '/foods/30043-yogurt.png')
    WHEN 40001 THEN CONCAT(@fixture_base, '/foods/40001-chili.png')
    WHEN 40002 THEN CONCAT(@fixture_base, '/foods/40002-hotpot.png')
    WHEN 40003 THEN CONCAT(@fixture_base, '/foods/40003-wasabi.png')
    WHEN 40011 THEN CONCAT(@fixture_base, '/foods/40011-fried-chicken.png')
    WHEN 40012 THEN CONCAT(@fixture_base, '/foods/40012-bbq.png')
    WHEN 40013 THEN CONCAT(@fixture_base, '/foods/40013-fries.png')
    WHEN 40021 THEN CONCAT(@fixture_base, '/foods/40021-hot-soup.png')
    WHEN 40022 THEN CONCAT(@fixture_base, '/foods/40022-hot-tea.png')
    WHEN 40023 THEN CONCAT(@fixture_base, '/foods/40023-hot-porridge.png')
    WHEN 40031 THEN CONCAT(@fixture_base, '/foods/40031-baijiu.png')
    WHEN 40032 THEN CONCAT(@fixture_base, '/foods/40032-coffee.png')
    WHEN 40033 THEN CONCAT(@fixture_base, '/foods/40033-beer.png')
    WHEN 40041 THEN CONCAT(@fixture_base, '/foods/40041-milk-tea.png')
    WHEN 40042 THEN CONCAT(@fixture_base, '/foods/40042-chocolate.png')
    WHEN 40043 THEN CONCAT(@fixture_base, '/foods/40043-cake.png')
    ELSE `PicUrl`
END
WHERE `Id` IN (
    30001, 30002, 30003, 30011, 30012, 30013, 30021, 30022, 30023, 30031,
    30032, 30033, 30041, 30042, 30043, 40001, 40002, 40003, 40011, 40012,
    40013, 40021, 40022, 40023, 40031, 40032, 40033, 40041, 40042, 40043
);

/*
  4. 医院/医生配图
  覆盖现有 10 条医生数据，去掉示例域名图片，改成项目本地可访问的静态图。
*/
UPDATE `totolaryngologyhospitaldoctor`
SET `PicUrl` = CASE `Id`
    WHEN 1 THEN CONCAT(@fixture_base, '/doctors/doctor-01-li-chengguang.png')
    WHEN 2 THEN CONCAT(@fixture_base, '/doctors/doctor-02-zhou-yajing.png')
    WHEN 3 THEN CONCAT(@fixture_base, '/doctors/doctor-03-han-lixin.png')
    WHEN 4 THEN CONCAT(@fixture_base, '/doctors/doctor-04-chen-jun.png')
    WHEN 5 THEN CONCAT(@fixture_base, '/doctors/doctor-05-wang-ruolan.png')
    WHEN 6 THEN CONCAT(@fixture_base, '/doctors/doctor-06-liu-zhiheng.png')
    WHEN 7 THEN CONCAT(@fixture_base, '/doctors/doctor-07-zhang-yue.png')
    WHEN 8 THEN CONCAT(@fixture_base, '/doctors/doctor-08-tang-li.png')
    WHEN 9 THEN CONCAT(@fixture_base, '/doctors/doctor-09-shi-wenqing.png')
    WHEN 10 THEN CONCAT(@fixture_base, '/doctors/doctor-10-jiang-simin.png')
    ELSE `PicUrl`
END
WHERE `Id` IN (1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

/*
  5. 喉镜照片
  使用独立测试主键插入两条记录，关联现有 DetectId=93/91，便于在历史资料和就诊准备报告里验证。
*/
INSERT INTO `tlaryngoscopephoto` (
    `Id`, `CreationTime`, `UpdateTime`, `UserId`, `DetectId`, `LaryngoscopePhotoUrl`,
    `PhotoDesc`, `UploadTime`, `CheckTime`, `HospitalName`, `CheckType`,
    `ViewPosition`, `LesionSide`, `AuditStatus`, `IsDelete`
) VALUES
(
    90001, '2026-03-31 11:00:00', '2026-03-31 11:05:00', 2, 93,
    CONCAT(@fixture_base, '/laryngoscope/laryngoscope-90001.png'),
    '测试喉镜图 01：用于验证列表、详情和报告嵌图',
    '2026-03-31 11:01:00', '2026-03-30 09:30:00', '测试耳鼻喉门诊', '电子喉镜',
    '正视', '双侧', 1, 0
),
(
    90002, '2026-03-31 11:10:00', '2026-03-31 11:12:00', 2, 91,
    CONCAT(@fixture_base, '/laryngoscope/laryngoscope-90002.png'),
    '测试喉镜图 02：用于验证最近记录排序',
    '2026-03-31 11:11:00', '2026-03-29 14:20:00', '测试咽喉专病门诊', '纤维喉镜',
    '正视', '未见明确', 1, 0
)
ON DUPLICATE KEY UPDATE
    `UpdateTime` = VALUES(`UpdateTime`),
    `UserId` = VALUES(`UserId`),
    `DetectId` = VALUES(`DetectId`),
    `LaryngoscopePhotoUrl` = VALUES(`LaryngoscopePhotoUrl`),
    `PhotoDesc` = VALUES(`PhotoDesc`),
    `UploadTime` = VALUES(`UploadTime`),
    `CheckTime` = VALUES(`CheckTime`),
    `HospitalName` = VALUES(`HospitalName`),
    `CheckType` = VALUES(`CheckType`),
    `ViewPosition` = VALUES(`ViewPosition`),
    `LesionSide` = VALUES(`LesionSide`),
    `AuditStatus` = VALUES(`AuditStatus`),
    `IsDelete` = VALUES(`IsDelete`);

COMMIT;
