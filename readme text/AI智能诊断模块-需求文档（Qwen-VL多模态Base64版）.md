# AI 智能诊断模块（Qwen-VL 多模态 · Base64 方案）需求文档

> 适用项目：居家喉部健康音频自查小程序（`HealthControl.uniapp` + `HealthControl.springboot` + `HealthControl.elementui` + `voice/`）  
> 模型选择：阿里云 DashScope 通义千问 **Qwen-VL 系列**多模态模型  
> 图片输入方式：**Base64**（开发/本机联调优先）  
> 数据设计：新建 `taudio_ai_report` 表承载 AI 解读报告；**不再复用** `taudioscreenrecord.Deepseek*` 字段（计划移除）  
> 文档目的：定义 AI 模块的范围、页面、接口、流程、异常、权限、审计、隐私与数据权利等要求，确保与当前系统框架一致、功能覆盖全面。

---

## 1. 背景与目标

### 1.1 背景

现有系统已实现「录音/上传 → 生成 MFCC/Mel 图谱 → DenseNet 初筛 → 入库 → 小程序/后台展示」的检测链路，并具备：

- 检测记录：`taudioscreenrecord`
- 图谱链接：`MfccSpectrumUrl` / `MelSpectrumUrl`
- 模型调用日志：`tmodelinterfacecalllog`
- 标注闭环：`tmodeloptimizelabel`
- 隐私设置与数据权利：`tuserprivacysetting`、`tdataexportrequest`、`tdatadeleterequest`

本需求新增一个独立的「AI 智能诊断/解读」模块，基于已生成的 Mel/MFCC 图谱，调用 Qwen-VL 多模态模型给出**非医疗诊断性质**的风险解读、建议与问答支持，并与历史记录联动。

### 1.2 目标

- 对单次检测记录提供「AI 图谱解读报告」（结构化 + 文本）
- 支持从历史检测记录选择并复用数据生成 AI 报告
- 与现有功能闭环一致：报告可复看、可导出/删除、可审计、可在管理后台查看
- 对外部大模型调用做安全控制（Key 不下发前端、记录审计、频控）

### 1.3 非目标（本期不做）

- 不承诺“医学确诊”；不输出“诊断结论”或“病名确诊”
- 不训练/微调专用医疗模型（仅接入通用多模态 API）
- 不在本期实现 OSS 公网 URL 图像方案（本期以 Base64 为主；上线可升级）
- 不做复杂多轮对话记忆（可做轻量问答，历史对话存储可后续迭代）

---

## 2. 术语与约束

- **检测记录**：`taudioscreenrecord` 中的一次录音检测数据。
- **图谱**：由 `voice/detect_audio.py` 生成并由后端保存/映射为 URL 的 `_mel.png`、`_mfcc.png`。
- **AI 报告**：对某条检测记录（或多条）生成的结构化解读与建议，落库于 `taudio_ai_report`。
- **免责声明**：所有 AI 输出必须带「仅供健康参考，不构成医疗诊断」等提示。
- **隐私约束**：若用户关闭外部模型调用权限，则不得调用 Qwen-VL。

---

## 3. 角色与权限

### 3.1 角色

- **普通用户（小程序端）**：发起 AI 解读、查看/删除自己的 AI 报告、基于历史记录复用生成报告、提出问答。
- **管理员/医生（管理后台）**：查看 AI 报告、检索与导出、质量复核、对疑似样本发起标注闭环。

### 3.2 权限要求

- 小程序端仅可访问自己的 `taudioscreenrecord` 和 `taudio_ai_report`
- 管理后台可按权限查看所有用户记录（与现有后台权限体系一致）

---

## 4. 功能范围（用户侧：`HealthControl.uniapp`）

### 4.1 入口与导航

#### 4.1.1 单次结果页入口

- 页面：`pages/Front/AudioResult.vue`
- 新增按钮：**AI 智能解读（试验性）**
- 按钮状态：
  - 若该记录已有最新 AI 报告：显示“查看 AI 解读”
  - 若无报告：显示“生成 AI 解读”
  - 若用户隐私设置禁止：按钮置灰，提示“已关闭外部 AI 解读（可在隐私设置开启）”

#### 4.1.2 历史记录入口

- 页面：`pages/Front/AudioRecordList.vue`
- 每条记录增加入口：
  - “AI 解读”
  - 或在详情页统一入口

### 4.2 新增页面：AI 报告页

- 建议新增：`pages/Front/AudioAIReport.vue`

#### 4.2.1 页面内容要求

- **基础信息区**：
  - 检测时间、记录 ID（可隐藏但可复制）、音频时长
  - DenseNet 初筛结果与置信度（如现有字段有展示）
- **图谱区**：
  - 展示 Mel 图谱缩略图，可点击放大
  - （可选）显示 MFCC 图谱 tab
- **AI 解读区**（结构化展示）：
  - 风险等级：低/中/高
  - 关键观察点（key observations）
  - 可能解释（possible explanations）
  - 生活方式建议（suggestions）
  - 何时就医（when to see doctor）
  - 免责声明（disclaimer）
- **操作区**：
  - “重新生成”（受频控与计费保护）
  - “删除 AI 报告”
  - “复制文本”

#### 4.2.2 交互与状态

- 生成时显示 loading，并明确提示“AI 解读可能需要 5-30 秒”
- 失败时展示可理解的错误原因（网络/Key/图片读取/模型限流等）
- 成功后落库并可离线复看（仅从数据库读取，不再重复调用模型）

### 4.3 历史记录复用生成报告（必做功能 3）

在 `AudioRecordList` 与 `AudioAIReport` 中，支持从历史检测记录（包括图谱）复用生成 AI 报告。

#### 4.3.1 操作方式

- `AudioRecordList` 中：
  - 多选历史记录（最多 N 条，建议 N≤10）
  - 点击“AI 趋势解读”按钮
- `AudioAIReport` 中：
  - 提供“基于最近 N 次检测生成趋势分析”按钮

#### 4.3.2 数据范围

- 至少包含：
  - 每次检测日期、DenseNet 初筛结果与置信度
  - 若已有 AI 报告，则可附上历史风险等级（AI 视角）
  - 可选：图谱关键信息（如图谱分辨率、声学特征指标）
- 对于“复用图谱”的场景：
  - 后端可选择仅用结构化数据（文本输入大模型）；
  - 或针对代表性几条记录重新传入 Mel/MFCC 图谱给 Qwen-VL。

#### 4.3.3 输出要求

- AI 趋势报告至少包含：
  - 过去一段时间（例如 3~12 个月）检测次数与风险分布
  - 风险变化趋势：改善/稳定/略有波动/升高
  - 与生活习惯、职业用声（如已在问卷中采集）可能的关联分析
  - 未来建议：自查频率、何时应复查、何种情况下建议就医
  - 免责声明（同单次报告）

#### 4.3.4 界面展示

- 在 `AudioAIReport.vue` 中增加“趋势分析”卡片：
  - 文字总结 + 简单图表（如风险等级时间轴，后期可在管理后台进一步可视化）
- 历史趋势报告同样落库到 `taudio_ai_report`，`ReportType=HISTORY_TREND`。

### 4.4 医疗咨询对话（必做功能 1）

本功能为“正常的医疗咨询对话”，但定位为**健康科普与就医建议**，不提供确诊。

#### 4.4.1 页面与入口

- 新增页面：`pages/Front/AudioAIChat.vue`
- 入口位置：
  - 个人中心或首页增加入口卡片：“AI 健康咨询（试验性）”
  - `AudioResult.vue`、`AudioAIReport.vue` 下方增加“就本次结果继续咨询”按钮，点击跳转到 `AudioAIChat.vue` 并自动绑定当前记录。

#### 4.4.2 功能要求

- 支持用户输入自由文本问题，例如：
  - 嗓子总是干痒，这次检测结果是中风险，我需要注意什么？
  - 我是教师/主播，这种工作对嗓子有什么长期影响？
  - 哪些食物对喉部比较友好？哪些应尽量少吃？
- 支持绑定上下文：
  - 若从检测结果/AI 报告页进入，则自动携带最近一条或当前 `RecordId`；
  - 后端根据 `RecordId` 取 DenseNet 初筛结果 + 最新 AI 报告摘要作为上下文传给大模型。
- 对话形式：
  - 支持多轮对话（会话内保留最近若干轮 Q&A，前期可以只在前端做简单记录并每次整体发给后端）。

#### 4.4.3 约束与文案

- 必须在页面顶部与每轮回答下方显著展示免责声明：
  - “AI 答复仅供健康参考，不构成医疗诊断或个性化医疗建议，不能替代医生就诊。”
- 不得出现：
  - 明确诊断：“你患有××病”“这是恶性肿瘤”等措辞；
  - 指定处方或药物用量。
- 建议输出重点：
  - 自我观察要点（症状变化、时间、诱因）
  - 生活方式与用声、饮食建议
  - 何种情况下应尽快就医（如出现呼吸困难、吞咽困难、声音急剧变化等）

---

## 5. 功能范围（管理后台：`HealthControl.elementui`）

### 5.1 AI 报告管理

在 Admin 模块新增菜单（或挂载到“检测记录”详情中）：

- 列表：
  - 用户、检测记录 ID、报告类型、风险等级、模型名称、生成时间、生成耗时、状态（成功/失败）
  - 支持筛选：风险等级、时间范围、模型版本、失败原因关键词
- 详情：
  - 展示 Mel/MFCC 图谱
  - 展示结构化 AI 输出与原始响应（可折叠）
  - 展示“免责声明是否存在”（自动校验）
- 操作：
  - 导出（JSON/Excel，至少支持 JSON）
  - 标记“需复核/可疑”并联动标注闭环（写入 `tmodeloptimizelabel` 或创建标注任务）

### 5.2 与模型调用日志联动

后台可从 AI 报告详情跳转查看对应的 `tmodelinterfacecalllog` 记录（按关联 id/trace id）。

---

## 6. 后端接口需求（`HealthControl.springboot`）

> 说明：遵循你当前接口风格（大驼峰字段、`POST` 为主、返回统一结构）。

### 6.1 生成单条检测记录的 AI 报告

- **接口**：`POST /FrontAI/AnalyzeAudioRecord`
- **鉴权**：必须登录（携带 token，复用现有鉴权方式）
- **入参**：
  - `RecordId`（必填，音频检测记录主键）
  - `ForceRegenerate`（可选，默认 false；true 表示重新生成并覆盖“最新报告”）
  - `SpectrumType`（可选，默认 `MEL`；可选 `MEL|MFCC|BOTH`）
- **处理逻辑**：
  1. 校验 `RecordId` 存在且属于当前用户（后台管理员可跳过所有权限制）
  2. 校验隐私设置：若禁止外部模型调用 → 返回明确错误码与提示
  3. 若存在最新成功报告且 `ForceRegenerate=false` → 直接返回该报告（不调用外部模型）
  4. 准备输入：
     - Base64 图谱：优先从本地文件读取（推荐），若只能通过 URL 访问则先下载后转 base64
  5. 构造 Prompt（见 7.1），调用 DashScope Qwen-VL
  6. 解析模型输出：
     - 必须能解析为 JSON（若解析失败，做降级：把原文落库并标记 `ParseStatus=FAILED`）
  7. 落库到 `taudio_ai_report`（成功/失败都要落库）
  8. 写入 `tmodelinterfacecalllog` 审计日志（请求/响应摘要/耗时/错误）
  9. 返回报告
- **出参**：
  - `ReportId`
  - `RecordId`
  - `ReportType=RECORD_INTERPRETATION`
  - `RiskLevel`
  - `ReportJson`（结构化 JSON）
  - `SummaryText`（用于小程序展示）
  - `CreatedTime`
  - `ModelName/ModelVersion`

### 6.2 获取某条检测记录的 AI 报告列表

- **接口**：`POST /FrontAI/ReportList`
- **入参**：`RecordId`、分页参数
- **出参**：报告列表（支持查看历史多次生成）

### 6.3 获取 AI 报告详情

- **接口**：`POST /FrontAI/ReportGet`
- **入参**：`ReportId`
- **出参**：报告详情（含原始响应、结构化字段、错误信息）

### 6.4 删除 AI 报告

- **接口**：`POST /FrontAI/ReportDelete`
- **入参**：`ReportId`
- **行为**：软删除（建议）并记录审计

### 6.5 历史趋势解读（可选）

- **接口**：`POST /FrontAI/AnalyzeHistory`
- **入参**：
  - `RecordIds[]`（或时间范围 `StartTime/EndTime`）
  - `ForceRegenerate`（可选）
- **处理**：
  - 汇总 DenseNet 结果、置信度、时间序列、声学指标（如 MPT/HNR 等）
  - 调用大模型（可用 Qwen 文本模型，不必多模态）
  - 落库 `taudio_ai_report`，`ReportType=HISTORY_TREND`

### 6.6 AI 问答（可选）

- **接口**：`POST /FrontAI/Chat`
- **入参**：
  - `Question`
  - `BindRecordId`（可选）
- **出参**：回答文本 + 免责声明
- **要求**：
  - 若绑定记录，则带上最新 AI 报告摘要与 DenseNet 结果作为上下文

---

## 7. Qwen-VL 调用与提示词要求

### 7.1 Prompt 规范（强制 JSON 输出）

后端必须使用“强约束输出 JSON”的系统提示，建议包含：

- 输入说明：这是喉部嗓音的 Mel/MFCC 频谱图
- 约束：非医疗诊断、不得确诊、建议就医条件
- 输出 JSON schema（字段固定）

**结构化字段（建议）：**

- `risk_level`: `"低"|"中"|"高"`
- `key_observations`: string[]
- `possible_explanations`: string[]
- `suggestions`: string[]
- `when_to_see_doctor`: string[]
- `retest_tips`: string[]（如何重录提升可靠性）
- `disclaimer`: string（必须包含）

### 7.2 输出解析与容错

若模型输出不符合 JSON：

1. 记录原始输出到 `RawText`
2. `ParseStatus=FAILED`
3. `SummaryText` 使用固定模板提示“本次解读解析失败，请重试/稍后重试”
4. 仍需写入 `tmodelinterfacecalllog`

---

## 8. 频控、缓存与成本控制

### 8.1 单记录缓存策略

- 默认：同一 `RecordId` 若已有最新成功报告，则直接返回，不重复调用外部模型
- 用户点击“重新生成”才会调用（并受频控）

### 8.2 频控策略（建议）

- 同一用户同一记录：
  - 60 秒内最多 1 次重新生成
- 同一用户全局：
  - 10 分钟内最多 5 次生成/重试（防止误触或恶意刷接口）

频控命中时返回明确错误码与提示。

---

## 9. 安全与隐私、数据权利

### 9.1 API Key 安全

- Key 仅存在于后端配置（环境变量/安全配置中心）
- 前端不得出现 Key

### 9.2 隐私设置联动

读取 `tuserprivacysetting`：

- 若用户关闭“外部 AI 模型调用/数据出境/第三方处理”（具体字段按你现有表设计），则：
  - 禁止调用 DashScope
  - 前端按钮置灰并引导到隐私设置页面

### 9.3 数据导出/删除联动

当用户申请导出/删除数据时：

- 导出：应包含 AI 报告数据（`taudio_ai_report`）的 JSON
- 删除：应支持删除 AI 报告（软删或物理删，按当前策略）

---

## 10. 失败场景与提示文案

### 10.1 常见失败原因（后端需分类）

- `PRIVACY_DISABLED`：用户隐私设置禁止
- `RECORD_NOT_FOUND`：记录不存在或无权限
- `SPECTRUM_NOT_READY`：Mel/MFCC 图谱尚未生成（检测未完成）
- `IMAGE_READ_FAILED`：图谱读取/下载失败
- `DASHSCOPE_AUTH_FAILED`：Key 无效/权限不足
- `DASHSCOPE_RATE_LIMIT`：被限流
- `MODEL_TIMEOUT`：调用超时
- `MODEL_OUTPUT_PARSE_FAILED`：输出不可解析

### 10.2 小程序提示要求

提示必须可行动：

- 隐私禁用：提示去隐私设置开启
- 图谱未生成：提示先等待检测完成
- 限流/超时：提示稍后重试

---

## 11. 与现有模块的耦合点（必须契合你的框架）

### 11.1 与 `taudioscreenrecord` 的关系

- `taudioscreenrecord` 继续作为“检测记录主表”
- AI 报告通过 `AudioScreenRecordId` 外键关联

### 11.2 与 `tmodelinterfacecalllog` 的关系

- 每次 AI 报告生成必须写入调用日志
- 日志中建议记录：
  - `Provider=DashScope`
  - `Model=qwen-vl-*`
  - `InputType=mel|mfcc|both`
  - `ReportId` / `RecordId` 作为关联字段

### 11.3 与标注闭环（`tmodeloptimizelabel`）

后台可对“高风险/低置信/解析失败”的报告打标，形成训练或策略优化数据闭环。

---

## 12. 验收标准（可测试）

### 12.1 单次报告生成

- 给定已完成检测且生成 Mel 图谱的记录：
  - 点击“生成 AI 解读”成功返回
  - 数据库新增 `taudio_ai_report` 记录
  - `tmodelinterfacecalllog` 新增一条调用日志
  - 报告页可复看且不重复扣费（同记录再次进入优先读库）

### 12.2 权限与隐私

- 关闭隐私开关后：
  - 不能发起外部模型调用
  - 前端显示正确提示

### 12.3 失败容错

- 模型返回非 JSON：
  - 仍落库，标记解析失败
  - 前端给出重试提示

---



## 14. 交付物清单

- 小程序端：
  - `AudioResult.vue` 增加 AI 入口
  - 新增 `AudioAIReport.vue`（报告展示与操作）
  - （可选）`AudioAIChat.vue`
- 后端：
  - `FrontAI` 相关 Controller + Service + DashScope Client
  - `taudio_ai_report` 的实体/Mapper/Service/Controller
  - 调用日志写入 `tmodelinterfacecalllog`
- 管理后台：
  - AI 报告列表/详情页（挂载到检测记录或独立菜单）
- 数据库：
  - 新表 `taudio_ai_report`
  - （按你要求）移除 `taudioscreenrecord.Deepseek*`（建议按迁移策略执行）

