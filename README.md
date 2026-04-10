## 居家喉部健康音频自查小程序（HealthControl）

> 一个面向普通用户的「在家嗓音自查 + 喉部健康管理」一体化系统：用户在小程序里录音/上传音频，系统先通过**本地音频识别模型**完成嗓音风险初筛并生成波形/MFCC/Mel 图谱；同时提供健康档案、症状追踪、个性化提醒、饮食与科普、问卷风险评估、就医辅助、隐私与数据权利等能力；在核心检测结果之上，再接入 **Qwen-VL 多模态 AI 辅助解读**，帮助用户更容易理解结果与建议。管理端用于运营/医生/管理员查看数据、复核与管理内容、追踪模型调用与标注闭环。

---

### 0. 这是什么？能解决什么问题？

- **它是什么**：一个“自查 + 解释 + 管理 + 隐私合规”的完整小程序产品原型（含管理后台与后端服务），核心检测依赖本地 Python 音频模型推理，外部 Qwen-VL 仅用于结果解读、建议生成和辅助问答。
- **它能做什么**：
  - 让用户在家完成嗓音自查（录音/上传），得到**初筛结果 + 置信度 + 图谱可视化**，并把结果沉淀为可追踪的健康记录。
  - 把“检测结果”进一步升级为“可读的建议”：对 Mel/MFCC 图谱调用 **通义千问 Qwen-VL**，生成结构化 **AI 解读报告**（风险等级、摘要、建议），但 AI 不参与核心初筛判定。
  - 围绕喉部健康的长期管理：档案、症状日志、饮食管理、科普内容、问卷评估、就医辅助、消息提醒，以及隐私与数据权利闭环。

---

### 1. 项目特色（定位与整体风格）

- **端到端闭环（一个仓库打通全链路）**：小程序端（用户使用）+ 管理端（运营/医生/管理员）+ 后端（业务与数据）+ 本地模型检测 + AI 辅助解读 + 数据库（全量表结构）统一在一个仓库，适合做“产品原型 → 演示 → 验证 → 迭代”。
- **面向“长期健康管理”而非一次性检测**：除了音频自查结果，还提供档案、症状追踪、消息提醒、饮食/科普/问卷/就医辅助等模块，目标是把自查结果沉淀为可追踪的健康数据。
- **数据与能力“可组合”**：检测记录、档案、症状、问卷、饮食、提醒等通过数据库表结构串联，后续可扩展更多规则引擎（如随访频率、饮食推荐、风险分层）而不破坏整体架构。
- **双端 UI 适配**：用户侧以 UniApp 为主（微信小程序/H5），管理侧以 Element Plus 为主（后台运营管理），让“用户体验”和“数据运营”两条线都可落地。
- **定位明确：检测是核心，AI 是亮点但不是并列主线**：本项目真正的核心能力是本地模型完成喉部音频初筛，AI 的价值在于把结果讲清楚、补充建议、辅助趋势分析，而不是替代检测本身。

### 2. 项目亮点（可演示、可运营、可追溯）

- **核心检测 + 辅助解读（检测为主，AI 为辅）**
  - **Python 音频推理**：`voice/detect_audio.py` 统一输出一条 JSON（stdout 最后一段），并生成 MFCC/Mel 图谱，便于前端可视化展示与人工复核。这一部分是项目的核心检测能力。
  - **Qwen-VL 多模态辅助解读**：对 Mel/MFCC 图谱进行解释，生成结构化 **AI 解读报告**并落库到 `taudio_ai_report`，包含摘要、风险等级、建议、原始请求/响应、耗时与错误信息，支持审计与复盘。这一部分是结果解释层，不参与核心初筛判定。
- **可追踪可运营**：有 **模型接口调用日志**（`tmodelinterfacecalllog`）与统计入口（`/TModelInterfaceCallLog/StatsSummary`），便于看调用量、失败率、耗时分布等；配合管理端页面可直接演示运营看板能力。
- **标注闭环能力**：`tmodeloptimizelabel` + 管理端“模型优化标注”页面，为后续模型迭代提供样本筛选与标注落地路径（从“跑模型”走向“迭代模型”）。
- **合规视角落地**：隐私偏好（`tuserprivacysetting`）+ 数据导出/删除申请（`tdataexportrequest`/`tdatadeleterequest`）+ 历史记录，让系统具备“产品可落地”的合规闭环，而非 demo。

### 3. 项目重难点（工程挑战与解决思路）

- **长耗时任务与用户体验**
  - 检测链路同时支持：记录式异步检测（`/TAudioScreenRecord/StartDetect`）与同步上传即检测（`/api/audio/detect`），兼顾“产品体验（可轮询/可重试）”与“调试/联调效率（直接返回结果）”。
- **文件与图谱的生成、存储与访问**
  - 后端上传把文件落到 `external-resources/`（或临时目录），并通过 URL 暴露给前端；同时生成波形/MFCC/Mel 图谱文件，涉及目录结构、命名、以及跨端展示的一致性问题。
- **跨语言调用稳定性（Java ↔ Python）**
  - Python 脚本约束为“stdout 最后一段 JSON”，后端只需取尾部 JSON 即可稳定解析；并提供 `stage/errorType/errorMessage` 等字段，便于定位失败阶段（load_audio/load_model/preprocess/inference/postprocess）。
- **AI 解读的“可追溯与可回放”**
  - 多模态模型调用把请求/响应与解析结果落库（`taudio_ai_report`），才能做到：失败可查、结果可复盘、版本可追踪；这比“只把文本展示在页面上”更难但更有工程价值。
- **权限与数据隔离**
  - 管理端路由通过 `RoleType == "管理员"` 做访问控制；前台 AI 报告接口在后端按当前用户做可见性限制（仅本人可见），避免越权查看报告/记录。
- **一致的字段命名与跨端契约**
  - 后端 Jackson 使用 `UPPER_CAMEL_CASE`，前端也必须使用大驼峰字段名进行请求与渲染；这对团队协作很关键，但也容易踩坑，需要在文档和代码约定中明确。

---

### 4. 项目结构（Monorepo）

```text
.
├─HealthControl.uniapp        # 小程序端（UniApp + Vue3 + Pinia + uni-ui）
├─HealthControl.elementui     # 管理后台（Vue3 + Vite + Element Plus）
├─HealthControl.springboot    # 后端服务（Spring Boot 3 + MyBatis Plus + MySQL）
├─voice                       # Python 音频模型与脚本（DenseNet 推理 + 图谱生成）
├─healthcontrol.sql           # MySQL 初始化脚本（表结构 + 示例数据）
├─readme text                 # 业务模块需求/实现/测试/接入文档（Markdown）
└─README.md                   # 本说明文档
```

---

### 5. 功能模块总览

- **音频自查与检测记录**
  - 录音/上传音频 → 后端触发检测 → 返回初筛结果与置信度，并生成可视化图谱（波形/MFCC/Mel）
  - 结果页展示：基础信息（时长/采样率/性别识别等）、失败原因与重试提示、图谱缩略图与放大查看
  - 历史记录：按时间查看每次检测结果，支持重复检测、对比变化（用于长期跟踪）
  - 报告导出：对检测记录进行导出留存（用于就医沟通或个人留档）
  - 相关表：`taudioscreenrecord`、`tscreenreportexportrecord`
  - 相关管理端页面：`TAudioScreenRecordList.vue`、`TScreenReportExportRecordList.vue`

- **AI 智能解读（辅助模块）**
  - 基于 Mel/MFCC 图谱调用 **通义千问 Qwen-VL** 生成「AI 解读报告」（风险等级、摘要、建议、注意事项等）
  - 这一模块的职责是解释本地模型已经产出的检测结果，让用户更容易理解，不参与核心初筛判断
  - 支持两种入口：
    - 从检测结果页进入：对单条检测记录生成/复用 AI 报告
    - 上传图谱 PNG 直接解读：不绑定检测记录，适合外部图谱或离线图片
  - 支持健康咨询对话：围绕“护嗓/就医建议/科普理解”等进行问答
  - 相关表：`taudio_ai_report`

- **健康档案与症状追踪**
  - 健康档案：沉淀个人基础健康信息与关键指标，作为风险评估与提醒策略的基础
  - 症状日志：用结构化方式记录“疼痛/异物感/嘶哑/吞咽不适”等日常变化，可与检测记录联动回看
  - 详情页：支持查看单次记录的完整信息，便于长期追踪与趋势观察
  - 相关表：`tpersonallaryngealhealthrecord`、`tsymptomlog`

- **消息通知中心与个性化健康提醒**
  - 定期自查提醒：按用户风险等级与策略推送“建议何时再测”
  - 护嗓小贴士：结合科普/饮食等内容进行轻量触达
  - 风险随访提醒：当风险偏高或近期变化明显时，提示关注与必要时就医
  - 消息中心：支持已读/未读、置顶/重要、点击跳转到对应业务（自查/档案等）
  - 相关表：`messagenotice`、`tpersonalhealthremind`、`tuserremindsetting`

- **健康科普（内容 + 互动）**
  - 内容管理：文章/内容的发布、分类与展示，提供用户侧浏览入口
  - 互动与统计：收藏、点赞、评论、阅读记录、搜索记录与举报，支持运营复盘内容效果
  - 相关表：`thealthscience`、`thealthsciencecategory`、`tsciencereadlog`、`tsciencecollect`、`tsciencelike`、`tsciencecomment`、`tsciencecommentlike`、`tsciencereport`、`tsciencesearchlog`

- **风险评估与问卷**
  - 题库与问卷：配置风险评估题目与选项，形成可发放的问卷
  - 用户答卷：填写与提交问卷，生成结果并可查看历史记录
  - 用途：与检测记录/档案结合，为风险分层、提醒频率、就医建议提供依据
  - 相关表：`triskassessmentquestion`、`triskassessmentquestionnaire`、`triskassessmentquestionoption`、`tuserquestionnaireanswer`

- **饮食管理与推荐**
  - 饮食知识库：维护“对喉部友好/需注意”的食物与分类
  - 饮食记录：用户记录饮食行为，便于关联症状与检测变化
  - 推荐与模板：根据规则/模板生成建议，并可解释“为什么推荐这些”
  - 相关表：`tlaryngealfood`、`tlaryngealfoodcategory`、`tuserdietrecord`、`tdietplantemplate`、`tdietplantemplateitem`

- **就医辅助 & 喉镜照片**
  - 医院医生库：维护耳鼻喉医院/医生信息，支持收藏与查询
  - 就医辅助：就诊准备报告、就医推荐记录、随访计划等，帮助用户把“自查结果”转化为“就医行动”
  - 喉镜照片：上传/编辑喉镜影像与信息，可与检测记录、档案联动
  - 相关表：`totolaryngologyhospitaldoctor`、`tusermedicalfavorite`、`tusermedicalvisitplan`、`tusermedicalrecommend`、`tlaryngoscopephoto`

- **隐私设置与数据权利**
  - 隐私偏好：用户可配置数据使用偏好与授权明细
  - 数据权利：发起数据导出/删除申请，并可查询申请历史与处理状态
  - 相关表：`tuserprivacysetting`、`tdataexportrequest`、`tdatadeleterequest`

- **模型调用日志与标注闭环**
  - 调用日志：记录每次模型调用的请求/响应/耗时/错误，便于排障与统计分析
  - 标注闭环：将“高风险/不确定/典型样本”沉淀为标注记录，支撑后续模型训练与版本迭代
  - 相关表：`tmodelinterfacecalllog`、`tmodeloptimizelabel`
  - 相关管理端页面：`TModelInterfaceCallLogList.vue`、`ModelInterfaceStats.vue`、`TModelOptimizeLabelList.vue`

- **用户与账号**
  - 用户注册/登录/角色（管理员/普通用户）与基础资料
  - 相关表：`appuser`
  - 管理端页面：`UserList.vue`

---

### 6. 小程序端（`HealthControl.uniapp`）

- **技术栈**：UniApp + Vue3（`<script setup>`）+ Pinia + `uni-ui` + SCSS
- **主要目录**
  - `pages/Front/`：业务页面
  - `components/`：通用组件（上传、地址选择、底部栏等）
  - `utils/http.js`：统一请求封装（`Post` 等）

- **核心页面（按功能分组，详见 `pages.json`）**
  - **音频自查**：`AudioRecord.vue`、`AudioResult.vue`、`AudioRecordList.vue`
  - **AI 智能解读**：`AudioAIReport.vue`、`AudioAIChat.vue`、`AudioAIToolResult.vue`（以及图谱上传页面 `AudioAISpectrumUpload.vue`）
  - **健康档案/症状**：`HealthRecord.vue`、`SymptomLog*`
  - **提醒/消息**：`HealthReminder.vue`、`ReminderMessage*`
  - **科普/问卷/饮食/就医/喉镜/隐私/标注闭环**：对应 `pages/Front/*`

> 你不需要看代码也能理解：小程序端负责“用户交互与展示”，所有数据都来自后端接口；接口返回字段使用**大驼峰**（后端 Jackson 已统一配置），因此前端提交与读取同样使用大驼峰字段名。

---

### 7. 管理后台（`HealthControl.elementui`）

- **技术栈**：Vue3 + Vite + Element Plus + Pinia + Axios
- **职责**：面向运营/医生/管理员，进行检测记录、图谱与业务数据的后台管理与查询；支撑标注闭环、内容管理、提醒策略、日志与统计等。
- **配置要点**：后端地址通常在 `HealthControl.elementui/base.js` 中配置，例如：
  - `http://localhost:7245`
- **后台入口与权限**：
  - 管理端路由统一在 `src/router/index.js`，`/Admin/*` 需要 `RoleType == "管理员"` 才可访问。
  - 默认进入后台会跳转到用户表（见 `views/Admin/Home.vue`）。

- **管理端功能菜单（对应 `views/Admin/*`，全量）**
  - 用户与隐私：`UserList`、`TUserPrivacySettingList`、`DataExportRequestList`、`DataDeleteRequestList`
  - 音频与模型：`TAudioScreenRecordList`、`TModelInterfaceCallLogList`、`ModelInterfaceStats`、`TModelOptimizeLabelList`
  - 档案与追踪：`TPersonalLaryngealHealthRecordList`、`TSymptomLogList`
  - 健康科普：`THealthScienceCategoryList`、`THealthScienceList`、`TScienceLikeList`、`TScienceCollectList`、`TScienceCommentList`、`TScienceCommentLikeList`
  - 饮食管理：`TLaryngealFoodCategoryList`、`TLaryngealFoodList`、`TUserDietRecordList`
  - 问卷风险：`TRiskAssessmentQuestionnaireList`、`TRiskAssessmentQuestionList`、`TRiskAssessmentQuestionOptionList`、`TUserQuestionnaireAnswerList`
  - 就医辅助：`TOtolaryngologyHospitalDoctorList`、`TUserMedicalPrepareReportList`、`TUserMedicalRecommendList`
  - 喉镜照片：`TLaryngoscopePhotoList`
  - 导出与记录：`TScreenReportExportRecordList`

---

### 8. 后端服务（`HealthControl.springboot`）

- **技术栈**：Spring Boot 3.3.x + MyBatis Plus + MySQL 8.x + JWT + Mail + POI + PDF 导出
- **端口**：默认 `7245`（见 `src/main/resources/application.yml`）

#### 8.1 核心数据流（先看这个就能理解系统怎么跑）

```text
小程序录音/上传
  └─(1) /File/BatchUpload 或 /api/audio/detect（multipart）
       └─ 保存到 external-resources/ 或临时目录
       └─ 调用 Python：voice/detect_audio.py 生成 JSON + MFCC/Mel 图谱
  └─(2) /TAudioScreenRecord/CreateOrEdit 写入检测记录（音频URL、图谱URL、结果、置信度…）
  └─(3) /TAudioScreenRecord/StartDetect 触发异步检测（后台跑完更新记录）
  └─(4) 结果页轮询 /TAudioScreenRecord/Get 展示结果
  └─(5) AI 解读：/FrontAI/AnalyzeAudioRecord 读取图谱 → 调用 Qwen-VL → 写入 taudio_ai_report
```

#### 8.2 前台（小程序端）AI 接口

- `POST /FrontAI/AnalyzeAudioRecord`：基于检测记录生成/获取 AI 解读报告（Mel/MFCC 图谱 → Qwen-VL）
- `POST /FrontAI/AnalyzeSpectrumUpload`：上传图谱 PNG 并解读（multipart）
- `POST /FrontAI/Chat`：健康咨询/科普对话（文本）
- `POST /FrontAI/ReportGet`：获取 AI 报告详情（仅本人）
- `POST /FrontAI/ReportList`：按检测记录获取 AI 报告列表（仅本人）
- `POST /FrontAI/ReportDelete`：删除 AI 报告（软删，仅本人）

#### 8.3 主要 Controller（按文件）

后端 Controller 位于 `HealthControl.springboot/src/main/java/com/example/web/controller/`，包含（节选）：

- **账号/上传/检测**
  - `AppUserController.java`
  - `FileController.java`（`POST /File/BatchUpload`）
  - `TAudioScreenRecordController.java`（`/TAudioScreenRecord/List|Get|CreateOrEdit|StartDetect|Delete|BatchDelete|Export`）
- **AI 智能解读**
  - `FrontAIController.java`（前台 AI 接口）
  - `TAudioAiReportController.java`（后台/通用 CRUD：`/TAudioAiReport/*`）
- **科普/问卷/饮食/就医/喉镜/隐私/提醒/标注**：对应 `Front*Controller` 与 `T*Controller`

---

### 9. 数据库（`healthcontrol.sql`，表清单）

- 初始化：创建库 `HealthControl` 后执行根目录 `healthcontrol.sql`
- 全量表（按脚本出现顺序）：
  - `appuser`
  - `messagenotice`
  - `taudioscreenrecord`
  - `taudio_ai_report`
  - `tdatadeleterequest`
  - `tdataexportrequest`
  - `tdietplantemplate`、`tdietplantemplateitem`
  - `thealthscience`、`thealthsciencecategory`
  - `tlaryngealfood`、`tlaryngealfoodcategory`
  - `tlaryngoscopephoto`
  - `tmedicaldictionary`
  - `tmodelinterfacecalllog`、`tmodeloptimizelabel`
  - `totolaryngologyhospitaldoctor`
  - `tpersonalhealthremind`、`tpersonallaryngealhealthrecord`
  - `triskassessmentquestion`、`triskassessmentquestionnaire`、`triskassessmentquestionoption`
  - `tsciencecollect`、`tsciencecomment`、`tsciencecommentlike`、`tsciencelike`、`tsciencereadlog`、`tsciencereport`、`tsciencesearchlog`
  - `tscreenreportexportrecord`
  - `tsymptomlog`
  - `tuserdietrecord`
  - `tusermedicalfavorite`、`tusermedicalpreparereport`、`tusermedicalrecommend`、`tusermedicalvisitplan`
  - `tuserprivacysetting`
  - `tuserquestionnaireanswer`
  - `tuserremindsetting`

- **AI 解读报告表（重点）**
  - `taudio_ai_report`：存储 AI 解读报告（结构化 JSON、摘要、风险等级、原始请求/响应、耗时、错误信息等）

---

### 10. 核心本地模型与 AI 辅助解读

#### 10.1 Python 音频推理（`voice/`，核心检测能力）

- **核心脚本**：`voice/detect_audio.py`
  - 统一 CLI：`python -u voice/detect_audio.py --audio <音频绝对路径> --outdir <输出目录>`
  - **输出约束**：stdout 最后一段保证是一条 JSON（前面允许中文日志），便于后端稳定解析
  - **结果字段示例**：`predicted_class`、`confidence`、`prob0/prob1`、`mfcc_file`、`mel_file`、`stage`、错误信息等
- **模型文件**：`voice/voice_best_model.pth`（73MB）
- **图谱生成**：复用 `mp3toMFCC.py` 的 `generate_mfcc_and_mel`
- **这一部分的职责**：负责真正的音频检测、风险初筛和结构化输出，是整个项目最核心的技术能力。

#### 10.2 通义千问 / 百炼 Qwen-VL（AI 辅助解读）

后端配置在 `HealthControl.springboot/src/main/resources/application.yml`：

- `ai.qwen.api-key`
- `ai.qwen.base-url`（兼容 OpenAI Chat Completions 的 compatible-mode）
- `ai.qwen.vl-model`（默认 `qwen-vl-plus`）
- `ai.qwen.text-model`（默认 `qwen-plus`）

**这一部分的职责是辅助，而不是替代检测：**

- 它读取本地模型已经生成的图谱和结果
- 它负责生成更适合普通用户阅读的解释、建议和提醒
- 它不直接参与本地模型的初筛判定
- 它不替代医生诊断

**安全注意：**

- 强烈建议使用环境变量注入密钥（例如 `AI_QWEN_API_KEY`），不要把真实 key 固化在仓库里。
- 当前 `application.yml` 中同时存在邮件密码与 AI Key 的明文配置，若用于任何共享/公开场景，请先完成**密钥替换/迁移到环境变量**。

相关接入说明文档：

- `readme text/AI模块接入实施指南-通义千问Qwen-VL多模态.md`
- `readme text/AI智能诊断模块-需求文档（Qwen-VL多模态Base64版）.md`

---

### 11. 如何使用（给“不懂代码的人”）

#### 11.1 普通用户（小程序端）能怎么用？

1. 登录/注册
2. 进入“音频自查”进行录音或上传音频
3. 查看检测结果：初筛结论、置信度、波形/MFCC/Mel 图谱
4. 点击「AI 智能解读」：生成更可读的风险等级与建议
5. 把结果沉淀到健康档案/症状日志里，长期追踪变化
6. 在“消息提醒”里接收定期自查提醒与护嗓建议
7. 使用饮食/科普/问卷/就医辅助模块进行综合管理

#### 11.2 管理员（管理后台）能怎么用？

1. 管理员账号登录管理端
2. 查看用户、检测记录与图谱，复核模型结果
3. 查看模型接口调用日志与统计、做异常追踪
4. 使用标注闭环模块沉淀高价值样本，用于后续模型优化
5. 管理科普内容、分类、互动数据；管理饮食库、问卷题库、就医资源等
6. 处理隐私设置、数据导出/删除申请与记录

---

### 12. 本地运行与联调（开发者）

#### 12.1 环境要求

- Windows 10+
- JDK 17、Maven 3.x
- Node.js ≥ 16
- MySQL 8.x
- Python 3.8–3.11（可选：ffmpeg）
- HBuilderX + 微信开发者工具（运行小程序）

#### 12.2 启动后端（Spring Boot）

```bash
cd HealthControl.springboot
mvnw.cmd spring-boot:run
```

默认服务：`http://localhost:7245`

#### 12.3 启动管理后台（可选）

```bash
cd HealthControl.elementui
npm install
npm run dev
```

#### 12.4 启动小程序端（UniApp）

1. HBuilderX 打开 `HealthControl.uniapp`
2. 配置 API 根地址（见 `utils/http.js` / `utils/config.js` 等封装）
3. 运行到 **微信小程序**（录音能力更完整），或 H5（适合纯上传测试）

#### 12.5（可选）独立测试 Python 推理脚本

```bash
cd voice
python detect_audio.py --audio "你的音频绝对路径" --outdir "输出目录"
```

---

### 13. 端到端流程（含 AI 解读，开发者视角）

1. **录音/上传音频** → `POST /File/BatchUpload`
2. **创建检测记录** → `POST /TAudioScreenRecord/CreateOrEdit`
3. **触发检测** → `POST /TAudioScreenRecord/StartDetect`
4. **结果页轮询展示** → `POST /TAudioScreenRecord/Get`
5. **AI 智能解读**
   - 结果页点击「AI 智能解读」→ `POST /FrontAI/AnalyzeAudioRecord`
   - 可在 AI 页面查看报告列表/详情：`/FrontAI/ReportList`、`/FrontAI/ReportGet`
   - 也可上传图谱 PNG 解读：`/FrontAI/AnalyzeSpectrumUpload`

---

### 14. 需求/实现/测试文档索引（`readme text/`）

- `readme text/需求.md`
- `readme text/健康档案与筛查追踪模块-需求.md` / `readme text/健康档案与筛查追踪模块-实现介绍.md`
- `readme text/健康管理与教育模块-需求.md` / `readme text/健康管理与教育模块-实现介绍.md`
- `readme text/饮食管理与推荐模块-需求.md` / `readme text/饮食管理与推荐模块-实现介绍.md`
- `readme text/就医辅助模块-需求.md` / `readme text/就医辅助模块-实现介绍.md`
- `readme text/消息通知中心模块-需求文档.md` / `readme text/消息通知中心模块-实现介绍.md`
- `readme text/个性化健康提醒模块-需求.md` / `readme text/个性化健康提醒模块-实现介绍.md` / `readme text/个性化健康提醒模块-测试方案.md`
- `readme text/隐私设置与数据权利模块-需求文档.md` / `readme text/隐私设置与数据权利模块-实现介绍.md` / `readme text/隐私设置与数据权利模块-测试方案.md`
- `readme text/模型优化标注闭环模块-需求文档.md` / `readme text/模型优化标注闭环模块-实现介绍.md`
- `readme text/模型调用与喉镜照片与饮食规则扩展功能-需求文档.md` / `readme text/模型调用与喉镜照片与饮食规则扩展功能-测试方案.md`
- `readme text/AI模块接入实施指南-通义千问Qwen-VL多模态.md`
- `readme text/AI智能诊断模块-需求文档（Qwen-VL多模态Base64版）.md`
- `readme text/后续功能迭代路线图-推荐优先级.md` / `readme text/下一步功能开发建议.md`

---

### 15. 常见问题（快速排查）

- **结果页一直显示“检测中”**
  - 看后端日志是否成功调用 Python，检查 `external-resources/` 是否产出图谱文件
- **AI 解读失败**
  - 检查 `ai.qwen.api-key` 是否配置正确、网络是否可访问 `dashscope.aliyuncs.com`
  - 查看 `taudio_ai_report.ErrorCode/ErrorMessage/RawResponse` 定位失败原因

---

### 16. 建议如何向第一次接触项目的人介绍本系统

如果你要把这个项目介绍给老师、同学、评审或者合作方，建议不要一上来先讲模型、接口和数据库，而是按照“用户能得到什么”的顺序来讲：

1. 先说明这是一套面向家庭场景的喉部健康自查与管理平台。
2. 再说明用户最先接触的是“音频自查”，这是项目的核心入口。
3. 然后说明系统会把结果沉淀到健康档案、历史记录和随访体系里。
4. 最后再补充 AI 解读、饮食建议、问卷、提醒、就医辅助等增强模块。

这样介绍的好处是逻辑很清楚：

- 先让别人知道核心功能是什么；
- 再让别人知道平台为什么不是一次性工具；
- 最后再让别人理解 AI 为什么有价值但不是项目主线。

### 17. 为什么这个项目不是“做一次检测就结束”

很多人第一次看到音频检测类项目，会默认认为它只是“上传音频 → 返回结果”的一次性流程。  
但本项目设计时，重点解决的是“检测后怎么办”的问题，所以专门把后续管理能力做成了独立模块。

这体现在几个方面：

- **结果会保存**：不是看完就结束，而是会进入检测记录和健康档案。
- **变化可追踪**：用户可以通过历史记录观察风险是否波动。
- **行为可干预**：饮食、提醒、问卷和科普内容会帮助用户做后续调整。
- **决策可衔接**：如果风险较高，可以进一步使用就医辅助和报告导出功能。

也就是说，检测只是起点，平台真正的价值在于把“单次检测”变成“连续管理”。

### 18. 为什么要一直强调“本地模型是核心，AI 是辅助”

这是本项目最容易被误解、也最需要统一口径的地方。

如果不加说明，别人很容易把项目理解成：

- 一个主要依赖外部大模型的 AI 医疗项目；
- 一个把“AI 会解释”误当成“AI 会检测”的产品；
- 一个更偏展示效果、而不是偏工程闭环的原型。

但项目真实情况并不是这样：

- **本地模型负责初筛**：这是核心检测能力。
- **平台负责记录和管理**：这是产品成立的基础。
- **AI 负责解释和增强**：这是提升用户体验和可理解性的补充能力。

之所以在 README 中反复强调这点，是为了确保：

- 开发团队内部理解一致；
- 对外展示时口径一致；
- 后续写申报书、PPT 和答辩稿时不会出现概念偏差。

### 19. 推荐阅读顺序

如果你是第一次接手这个仓库，建议按下面的顺序看：

1. 先看本 README，建立整体认知。
2. 再看 `项目申报材料-居家喉部健康音频自查小程序.md`，理解正式对外表述方式。
3. 然后看 `readme text/` 中各模块的需求与实现文档，理解业务细节。
4. 最后再进入前端、后端和模型代码目录。

这样会比直接打开代码更容易理解项目的设计意图和整体结构。
