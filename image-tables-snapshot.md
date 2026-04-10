# 图片相关表信息整理

- 生成时间：2026-03-31
- 数据来源：本地 MySQL `HealthControl` 当前库
- 整理范围：医生图、科普封面图、喉镜图
- 同目录参考文件：`food-db-snapshot.md`

## 概览

| 类别 | 表名 | 图片字段 | 有效记录数 | 当前图片来源分布 |
| --- | --- | --- | --- | --- |
| 医生图 | `totolaryngologyhospitaldoctor` | `PicUrl` | 10 | `test-fixtures=10` |
| 科普封面图 | `thealthscience` | `CoverUrl` | 14 | `example.com=9`，`local-upload=4`，`test-fixtures=1` |
| 喉镜图 | `tlaryngoscopephoto` | `LaryngoscopePhotoUrl` | 2 | `test-fixtures=2` |

## 医生图

- 表名：`totolaryngologyhospitaldoctor`
- 图片字段：`PicUrl`
- 业务含义：医生头像/门诊配图

| Id | 医院 | 医生 | 职称 | 地区 | 标签 | PicUrl |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | 北京大学第三医院咽喉嗓音中心 | 李晨光 | 主任医师 | 北京市海淀区 | 咽喉专病,嗓音门诊,显微手术 | `http://localhost:7245/test-fixtures/doctors/doctor-01-li-chengguang.png` |
| 2 | 北京同仁医院耳鼻咽喉头颈外科 | 周雅静 | 副主任医师 | 北京市东城区 | 咽喉科,嗓音门诊,反流相关咽炎 | `http://localhost:7245/test-fixtures/doctors/doctor-02-zhou-yajing.png` |
| 3 | 首都医科大学附属北京世纪坛医院耳鼻咽喉科 | 韩立新 | 主任医师 | 北京市海淀区 | 耳鼻咽喉,偏咽喉,嗓音康复 | `http://localhost:7245/test-fixtures/doctors/doctor-03-han-lixin.png` |
| 4 | 上海交通大学医学院附属仁济医院耳鼻咽喉-头颈外科 | 陈珺 | 主任医师 | 上海市浦东新区 | 咽喉专病,声带显微手术,嗓音训练 | `http://localhost:7245/test-fixtures/doctors/doctor-04-chen-jun.png` |
| 5 | 复旦大学附属眼耳鼻喉科医院耳鼻咽喉科 | 王若兰 | 副主任医师 | 上海市徐汇区 | 嗓音门诊,歌唱嗓音,职业用嗓 | `http://localhost:7245/test-fixtures/doctors/doctor-05-wang-ruolan.png` |
| 6 | 广州医科大学附属第一医院耳鼻咽喉头颈外科 | 刘志恒 | 主任医师 | 广东省广州市越秀区 | 咽喉肿瘤,显微外科,喉功能重建 | `http://localhost:7245/test-fixtures/doctors/doctor-06-liu-zhiheng.png` |
| 7 | 中山大学附属第三医院耳鼻咽喉科 | 张悦 | 副主任医师 | 广东省广州市天河区 | 耳鼻咽喉,偏咽喉,慢性咽炎 | `http://localhost:7245/test-fixtures/doctors/doctor-07-zhang-yue.png` |
| 8 | 四川大学华西医院耳鼻咽喉头颈外科 | 唐立 | 主任医师 | 四川省成都市武侯区 | 咽喉科,嗓音外科,儿童咽喉 | `http://localhost:7245/test-fixtures/doctors/doctor-08-tang-li.png` |
| 9 | 浙江大学医学院附属第二医院耳鼻咽喉科 | 施文清 | 主任医师 | 浙江省杭州市上城区 | 咽喉专病,睡眠呼吸障碍,嗓音 | `http://localhost:7245/test-fixtures/doctors/doctor-09-shi-wenqing.png` |
| 10 | 湖南省人民医院耳鼻咽喉头颈外科 | 蒋思敏 | 副主任医师 | 湖南省长沙市芙蓉区 | 咽喉科,声带显微手术,嗓音康复 | `http://localhost:7245/test-fixtures/doctors/doctor-10-jiang-simin.png` |

## 科普封面图

- 表名：`thealthscience`
- 图片字段：`CoverUrl`
- 业务含义：科普列表/详情封面图
- 说明：这里只整理封面图；`ScienceContent` 正文内嵌图片未在本文件展开

| Id | 分类 | 标题 | 图片来源 | CoverUrl | AuditStatus | PublishTime | ReadCount |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 2 | 饮食与饮水 | 嗓子不舒服时，这些食物可以多吃一点 | `example.com` | `https://example.com/cover/throat_food_good.jpg` | 2 | 2026-03-02 10:30:00 | 99 |
| 4 | 急性炎症 | 感冒后嗓子突然嘶哑，是急性喉炎吗？ | `example.com` | `https://example.com/cover/acute_laryngitis.jpg` | 2 | 2026-03-02 11:30:00 | 213 |
| 5 | 慢性病与结节 | 声带小结与声带息肉有什么不同？ | `example.com` | `https://example.com/cover/vocal_nodules_polyps.jpg` | 2 | 2026-03-02 12:00:00 | 189 |
| 6 | 慢性病与结节 | 长期清嗓、干咳，会不会把嗓子“清坏”？ | `example.com` | `https://example.com/cover/clear_throat.jpg` | 2 | 2026-03-02 12:40:00 | 133 |
| 7 | 基础发声训练 | 上班族也能做的3分钟发声热身 | `example.com` | `https://example.com/cover/voice_warmup_office.jpg` | 2 | 2026-03-02 14:30:00 | 111 |
| 8 | 术后康复训练 | 声带手术后，什么时候可以开始练声音？ | `example.com` | `https://example.com/cover/post_surgery_training.jpg` | 2 | 2026-03-02 15:05:00 | 97 |
| 9 | 就诊准备 | 第一次看喉科门诊，需要提前准备哪些信息？ | `example.com` | `https://example.com/cover/first_visit_ent.jpg` | 2 | 2026-03-02 15:40:00 | 79 |
| 10 | 复查与随访 | 做完喉镜检查后，还需要注意哪些事情？ | `example.com` | `https://example.com/cover/post_laryngoscopy.jpg` | 2 | 2026-03-02 16:10:00 | 82 |
| 11 | 日常护嗓习惯 | 经常熬夜、加班，对嗓子有什么影响？ | `example.com` | `https://example.com/cover/overtime_throat.jpg` | 2 | 2026-03-02 16:50:00 | 144 |
| 13 | 复查与随访 | 测试 | `local-upload` | `http://localhost:7245/172482602/shareImage_1770273340705.png` | 3 |  | 5 |
| 14 | 就诊准备 | 123 | `local-upload` | `http://localhost:7245/252895164/shareImage_1770273340705.png` | 2 |  | 9 |
| 15 | 日常护嗓习惯 | 12313123 | `local-upload` | `http://localhost:7245/422620410/Capture001.png` | 2 |  | 5 |
| 16 | 日常护嗓习惯 | 是大概多少 | `local-upload` | `http://localhost:7245/848937823/Capture001.png` | 2 |  | 2 |
| 90001 | 日常护嗓习惯 | 测试用：发声热身与护嗓补水示例 | `test-fixtures` | `http://localhost:7245/test-fixtures/science/science-cover-90001.png` | 2 | 2026-03-31 10:10:00 | 0 |

## 喉镜图

- 表名：`tlaryngoscopephoto`
- 图片字段：`LaryngoscopePhotoUrl`
- 业务含义：用户上传/绑定到检测记录的喉镜照片

| Id | UserId | DetectId | 医院 | 检查方式 | 描述 | LaryngoscopePhotoUrl | UploadTime | CheckTime |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 90001 | 2 | 93 | 测试耳鼻喉门诊 | 电子喉镜 | 测试喉镜图 01：用于验证列表、详情和报告嵌图 | `http://localhost:7245/test-fixtures/laryngoscope/laryngoscope-90001.png` | 2026-03-31 11:01:00 | 2026-03-30 09:30:00 |
| 90002 | 2 | 91 | 测试咽喉专病门诊 | 纤维喉镜 | 测试喉镜图 02：用于验证最近记录排序 | `http://localhost:7245/test-fixtures/laryngoscope/laryngoscope-90002.png` | 2026-03-31 11:11:00 | 2026-03-29 14:20:00 |
