# 喉镜影像档案：本地模型预测与 Qwen-VL 图像分析功能需求文档

版本：v0.2
日期：2026-04-12
位置：`README.md` 同级目录

## 1. 背景

当前“喉镜影像档案”已经支持用户上传喉镜图片，补充检查时间、医院/科室、检查方式、视角/体位、病变侧别和备注，并在小程序端按检查批次展示。

本次功能建设需要在该模块中新增两类相互独立的能力：

1. 本地模型预测：使用本地喉镜预测模型 `laryngoscope prediction/repalexnet_heatmap_n100.pth` 对历史喉镜图片进行分类预测，并生成中间热力图。
2. Qwen-VL 图像分析：调用已有 Qwen-VL 多模态模型能力，对历史喉镜图片进行图像解读，输出结构化观察结果、健康建议和就医提醒。

术语约定：

- “本地模型预测”专指本地 `RepAlexNet` 权重推理链路，模型文件为 `repalexnet_heatmap_n100.pth`，输出分类、置信度、类别概率和热力图。
- “Qwen-VL 图像分析”专指调用外部多模态大模型对喉镜图片进行分析和建议生成，输出文本化、结构化报告。
- 两者属于同一“喉镜影像档案”模块下的两个独立功能入口，用户可以只使用其中一个，也可以分别使用两个功能；状态、记录、重试和结果展示互不覆盖。

相关材料：

- 模型目录：`laryngoscope prediction`
- 本地模型权重：`laryngoscope prediction/repalexnet_heatmap_n100.pth`
- 本地推理参考脚本：`laryngoscope prediction/predict_larynscope.py`
- 样例喉镜图：`laryngoscope prediction/store/larynscope`
- 样例热力图：`laryngoscope prediction/store/store/heatmap`
- 样例预测输出：`laryngoscope prediction/store/store/predictions.json`

## 2. 目标

在不破坏现有喉镜档案上传、编辑、删除、分组展示逻辑的前提下，新增以下能力：

1. 用户可以从历史喉镜影像记录中选择单张图片，发起本地模型预测。
2. 系统调用本地 `RepAlexNet` 喉镜模型，生成分类结果、置信度、6 类概率分布和 Grad-CAM 热力图。
3. 用户可以从同一张历史喉镜图片发起 Qwen-VL 图像分析。
4. 系统调用已有 Qwen-VL 多模态能力，生成结构化分析报告，包括图像观察、风险提示、居家建议、复查建议和就医提醒。
5. 本地模型预测结果与 Qwen-VL 分析结果分别归档到该喉镜照片记录下，刷新页面后仍可查看。
6. 用户端清晰提示“结果仅供健康参考，不替代医生诊断”。
7. 本期先完成小程序端功能；管理端页面暂不改动，后续如需排查可先通过数据库记录和服务日志定位问题。

## 3. 现有基础

### 3.1 喉镜影像档案

当前核心表为 `TLaryngoscopePhoto`，字段包括：

| 字段 | 说明 |
| --- | --- |
| `Id` | 喉镜照片记录主键 |
| `UserId` | 用户 ID |
| `DetectId` | 关联音频自查记录 ID，可为空 |
| `LaryngoscopePhotoUrl` | 喉镜原图 URL |
| `PhotoDesc` | 照片描述/备注 |
| `UploadTime` | 照片上传时间 |
| `CheckTime` | 实际检查时间 |
| `HospitalName` | 检查医院名称 |
| `CheckType` | 检查方式 |
| `ViewPosition` | 视角/体位 |
| `LesionSide` | 病变侧别 |
| `IsDelete` | 软删除标记 |

用户端相关页面：

- `HealthControl.uniapp/pages/Front/LaryngoscopePhotoList.vue`
- `HealthControl.uniapp/pages/Front/LaryngoscopePhotoEdit.vue`

后端已有接口：

- `POST /Front/Laryngoscope/List`
- `POST /Front/Laryngoscope/SavePhoto`
- `POST /Front/Laryngoscope/Get`
- `POST /Front/Laryngoscope/Delete`

### 3.2 本地模型脚本能力

`predict_larynscope.py` 当前已经具备以下能力：

- 加载 `RepAlexNet` 模型结构和 `repalexnet_heatmap_n100.pth` 权重。
- 支持单张图片或目录输入。
- 支持 `--model-path`、`--output-dir`、`--device`、`--recursive` 参数。
- 对输入图片执行 `224 x 224` 预处理。
- 输出 `predicted_class_id`、`predicted_label`、`confidence`、`probabilities`。
- 使用 `GradCAM` 和 `model.stage5` 生成热力图。
- 生成 `predictions.json` 和 `predictions_summary.txt`。

当前标签映射来自脚本：

| 类别 ID | 显示标签 |
| --- | --- |
| 0 | 健康 |
| 1 | 喉鳞状细胞癌 |
| 2 | 不典型增生 |
| 3 | 炎症角化 |
| 4 | 不典型增生 |
| 5 | 不典型增生 |

说明：类别 `2`、`4`、`5` 当前均显示为“不典型增生”。后续如果补齐更精细的 6 类中文标签，需要同步更新脚本、后端枚举和前端展示文案。

### 3.3 已有 Qwen-VL 多模态能力

项目中已经实现了基于 Qwen-VL 的 Mel/MFCC 图谱分析能力，可作为本次喉镜图像分析的复用参考：

- 后端控制器：`HealthControl.springboot/src/main/java/com/example/web/controller/FrontAIController.java`
- 后端服务：`HealthControl.springboot/src/main/java/com/example/web/service/front/impl/FrontAIServiceImpl.java`
- DTO 汇总：`HealthControl.springboot/src/main/java/com/example/web/dto/front/FrontAIDtos.java`
- Qwen 客户端：`HealthControl.springboot/src/main/java/com/example/web/tools/ai/QwenBailianClient.java`
- 现有配置项：`ai.qwen.api-key`、`ai.qwen.base-url`、`ai.qwen.vl-model`、`ai.qwen.text-model`
- 现有默认视觉模型：`qwen-vl-plus`

当前 Mel/MFCC 图谱分析链路已包含：

- 根据历史检测记录读取图谱 PNG。
- 将图片转为 Base64。
- 调用 `qwenClient.chatWithPngBase64(prompt, pngBase64)`。
- 要求模型返回严格 JSON。
- 解析 `risk_level`、`plain_summary`、`suggestions`、`when_to_see_doctor` 等字段。
- 保存成功/失败报告与模型调用日志。
- 用户端已有 `AudioAIReport.vue`、`AudioAIToolResult.vue`、`AudioAISpectrumUpload.vue` 等页面可参考展示模式。

本次喉镜图像分析应复用上述成熟链路，但不要复用“音频图谱报告”的业务语义。建议新增喉镜图像分析专用 DTO、服务方法、报告类型和数据表，避免把喉镜报告混入音频报告。

## 4. 功能范围

### 4.1 本期纳入

- 在“喉镜影像档案”列表或详情中，对单张历史喉镜图发起本地模型预测。
- 保存本地模型预测的最新结果，并保留历史预测记录。
- 展示原图、预测标签、置信度、概率分布、模型信息、预测时间和热力图。
- 在同一张喉镜图片上发起 Qwen-VL 图像分析。
- 保存 Qwen-VL 图像分析的最新结果，并保留历史分析记录。
- 展示 Qwen-VL 结构化报告，包括图像观察、可能解释、建议、复查提示、就医提醒和免责声明。
- 本地模型预测与 Qwen-VL 分析分别支持重试/重新生成。
- 两类功能均做用户权限校验和失败状态展示；管理端页面本期暂不改动。
- 前端页面与现有小程序总体设计风格保持一致。

### 4.2 本期暂不纳入

- 批量预测用户全部历史喉镜图片。
- 自动生成医院诊断结论。
- 训练集标注闭环和模型再训练。
- 多个本地模型的对比预测。
- 热力图手动标注、编辑或病灶区域量化测量。
- 让 Qwen-VL 直接替代本地模型的分类结果。
- 让本地模型预测结果自动覆盖 Qwen-VL 分析结论。
- 管理端页面新增、Qwen-VL 报告下载等后台功能。

## 5. 用户流程

### 5.1 从历史记录选择图片

1. 用户进入“喉镜影像档案”列表。
2. 页面展示历史喉镜照片卡片，保留现有编辑、删除、上传入口。
3. 对包含喉镜图片的记录，卡片新增两个独立操作：
   - `本地模型预测`
   - `Qwen 图像分析`
4. 两个操作可以分别显示自己的状态摘要：
   - 本地预测：未预测、预测中、预测成功、预测失败。
   - Qwen 分析：未分析、分析中、分析成功、分析失败。
5. 如果图片 URL 为空、图片文件不存在或用户无权限，不展示操作入口或展示明确错误提示。

### 5.2 本地模型预测流程

1. 用户点击 `本地模型预测`。
2. 系统提示：预测结果仅供健康参考，不替代医生诊断。
3. 用户确认后，前端调用本地预测接口。
4. 后端校验用户权限和喉镜图片文件可读性。
5. 后端调用本地 `RepAlexNet` 推理能力。
6. 推理完成后，后端保存结构化结果和热力图路径。
7. 前端展示预测标签、置信度、概率分布、热力图入口和重新预测按钮。

### 5.3 Qwen-VL 图像分析流程

1. 用户点击 `Qwen 图像分析`。
2. 系统提示：图像分析建议仅供健康参考，不作为诊断依据。
3. 用户确认后，前端调用 Qwen-VL 图像分析接口。
4. 后端校验用户权限和喉镜图片文件可读性。
5. 后端读取喉镜图片并转为 Base64。
6. 后端构造喉镜图像分析 Prompt，调用 `qwenClient.chatWithPngBase64`。
7. 后端解析模型返回的 JSON，保存分析报告。
8. 前端展示风险提示、通俗摘要、关键观察、居家建议、复查建议、就医提醒和免责声明。

### 5.4 重新运行

- 本地模型预测失败或完成后，用户可以点击 `重新预测`。
- Qwen-VL 图像分析失败或完成后，用户可以点击 `重新分析`。
- 重新运行应新建一条记录，并将其作为该照片的最新结果返回。
- 两类历史记录均保留，便于追踪模型版本、Prompt 版本和失败原因。

## 6. 功能需求

### FR-01 独立入口与状态

- 喉镜照片卡片新增两个独立入口：`本地模型预测`、`Qwen 图像分析`。
- 两个入口在前端布局上相邻展示，但状态互不影响。
- 本地预测成功不自动触发 Qwen 分析。
- Qwen 分析成功不自动触发本地预测。
- 任一功能失败时，不影响另一功能继续使用。

### FR-02 权限控制

- 前台用户只能预测和分析自己名下的喉镜照片。
- 后端必须校验 `TLaryngoscopePhoto.UserId` 与当前登录用户一致。
- 本期管理端页面暂不改动；后续如实现管理端查询，可查看全部用户的本地预测记录与 Qwen 分析记录，但不得改写用户原始照片。

### FR-03 本地模型预测

- 后端根据 `TLaryngoscopePhoto.LaryngoscopePhotoUrl` 找到本地原图文件。
- 后端调用本地 Python 推理能力，输入单张图片、模型路径和输出目录。
- 模型输出必须解析为结构化数据：
  - 类别 ID
  - 预测标签
  - 置信度
  - 6 类概率数组
  - 热力图本地路径
  - 热力图可访问 URL
  - 模型名称和模型版本
- 推理失败不得影响喉镜照片原始记录的保存和展示。
- 推理过程应支持超时控制，避免长时间阻塞 Web 请求。

### FR-04 Qwen-VL 图像分析

- 后端根据 `TLaryngoscopePhoto.LaryngoscopePhotoUrl` 读取原始喉镜图片。
- 后端复用当前已经用于 Mel/MFCC 图谱分析的 Qwen API 接口和 `QwenBailianClient.chatWithPngBase64` 调用方式。
- Qwen-VL 模型名沿用现有配置 `qwen-vl-plus`，不要在本功能中另起一套模型配置。
- Prompt 应明确要求模型只输出 JSON，不输出 Markdown。
- Prompt 医学措辞需要更严谨：只能给出基于图片可见信息的风险提示、可能性分析和就医建议，不得给出“确诊”“一定是”“排除疾病”“良恶性定论”等结论性表达。
- Prompt 应明确提示模型不可替代医生诊断；如图片质量不足、角度遮挡或医学依据不足，应返回“无法判断”并建议复查或就诊。
- Qwen-VL 返回内容至少包含：
  - `risk_level`：低 / 中 / 高 / 无法判断
  - `plain_summary`：面向用户的通俗摘要
  - `image_observations`：图像层面的观察点
  - `possible_explanations`：可能解释，避免定性诊断
  - `suggestions`：居家观察和后续处理建议
  - `when_to_see_doctor`：需要尽快就医的情况
  - `retest_tips`：后续拍摄或复查建议
  - `disclaimer`：免责声明
- Qwen-VL 原始返回、解析后的报告 JSON、模型名、Prompt 版本和耗时应归档。
- Qwen-VL 失败时应保存失败记录，包含错误码和错误信息。

建议的喉镜图像分析 JSON 结构：

```json
{
  "risk_level": "低|中|高|无法判断",
  "plain_summary": "string",
  "image_observations": ["string"],
  "possible_explanations": ["string"],
  "suggestions": ["string"],
  "when_to_see_doctor": ["string"],
  "retest_tips": ["string"],
  "disclaimer": "本结果仅供健康参考，不替代医生诊断。"
}
```

建议 Prompt 约束要点：

```text
你将收到一张用户上传的喉镜图片。请基于图片中可见的喉部影像信息，给出健康风险提示和就医建议。

重要约束：
1. 你不是医生，不能做出医疗确诊，也不能替代耳鼻喉科医生的面诊、喉镜检查、病理检查或其他临床判断。
2. 不要使用“确诊”“必然”“一定是”“已经排除”“良性/恶性定论”等措辞。
3. 对疑似异常只能表述为“可能提示”“建议进一步检查”“建议结合症状和医生评估”。
4. 如果图片模糊、遮挡、角度不足、光线不佳或信息不足，请把 risk_level 设为“无法判断”，并说明需要重新拍摄或线下就诊。
5. 输出必须是 JSON，不要输出 Markdown、代码块或额外解释。
```

### FR-05 可选上下文

调用 Qwen-VL 时可将以下非敏感上下文加入 Prompt：

- 检查时间 `CheckTime`
- 检查医院/科室 `HospitalName`
- 检查方式 `CheckType`
- 视角/体位 `ViewPosition`
- 病变侧别 `LesionSide`
- 用户填写的照片备注 `PhotoDesc`

允许用户在 Qwen-VL 分析时手动选择“带入本地模型预测结果作为参考”。默认不带入，避免 Qwen-VL 把本地分类结果当作诊断事实。用户选择带入时，前端传入 `IncludeLocalPredictionContext=true`，后端只将最新本地预测标签、置信度和模型版本作为“参考信息”写入 Prompt，并明确提示“本地模型结果仅供参考，不可作为诊断结论”。

### FR-06 结果展示

本地模型预测结果展示：

- 原始喉镜图。
- 预测标签。
- 类别 ID。
- 置信度。
- 6 类概率分布。
- Grad-CAM 热力图。
- 模型名称、模型权重路径或版本号。
- 预测时间。
- 免责声明。

Qwen-VL 图像分析结果展示：

- 原始喉镜图。
- 风险等级。
- 通俗摘要。
- 图像观察点。
- 可能解释。
- 居家建议。
- 就医提醒。
- 复查/重拍建议。
- 模型名称、Prompt 版本、生成时间。
- 免责声明。

### FR-07 管理端范围

本期先完成小程序端功能，管理端页面暂不改动，不增加 Qwen-VL 报告下载能力。后续如果需要做后台排查页，再基于新增的两张记录表补充查询、筛选和 JSON 查看入口。

## 7. 数据设计

### 7.1 本地模型预测表

建议新增 `TLaryngoscopeLocalPredictionRecord`，专门保存本地 `RepAlexNet` 推理结果。

| 字段 | 类型建议 | 说明 |
| --- | --- | --- |
| `Id` | long | 主键 |
| `CreationTime` | datetime | 创建时间 |
| `UpdateTime` | datetime | 更新时间 |
| `UserId` | long | 用户 ID |
| `LaryngoscopePhotoId` | long | 关联 `TLaryngoscopePhoto.Id` |
| `SourcePhotoUrl` | varchar | 原图 URL |
| `SourcePhotoLocalPath` | varchar | 原图本地路径 |
| `PredictionStatus` | int/varchar | 处理中、成功、失败 |
| `PredictedClassId` | int | 预测类别 ID |
| `PredictedLabel` | varchar | 预测标签 |
| `Confidence` | decimal | 置信度 |
| `ProbabilitiesJson` | text | 概率分布 JSON |
| `HeatmapUrl` | varchar | 热力图访问 URL |
| `HeatmapLocalPath` | varchar | 热力图本地路径 |
| `ModelName` | varchar | `RepAlexNet` |
| `ModelVersion` | varchar | 如 `repalexnet_heatmap_n100` |
| `ModelPath` | varchar | 权重路径 |
| `ScriptPath` | varchar | 推理脚本路径 |
| `RawOutputJson` | text | `predictions.json` 中的原始结果 |
| `ErrorCode` | varchar | 错误码 |
| `ErrorMessage` | text | 错误信息 |
| `DurationMs` | int | 推理耗时 |
| `IsDelete` | boolean/int | 软删除 |

### 7.2 Qwen-VL 图像分析表

建议新增 `TLaryngoscopeQwenAnalysisRecord`，专门保存 Qwen-VL 图像分析结果。不要直接复用音频图谱报告表，除非后续统一重构为通用 `AIReport` 表。

| 字段 | 类型建议 | 说明 |
| --- | --- | --- |
| `Id` | long | 主键 |
| `CreationTime` | datetime | 创建时间 |
| `UpdateTime` | datetime | 更新时间 |
| `UserId` | long | 用户 ID |
| `LaryngoscopePhotoId` | long | 关联 `TLaryngoscopePhoto.Id` |
| `SourcePhotoUrl` | varchar | 原图 URL |
| `SourcePhotoLocalPath` | varchar | 原图本地路径 |
| `AnalysisStatus` | int/varchar | 处理中、成功、失败 |
| `RiskLevel` | varchar | 低 / 中 / 高 / 无法判断 |
| `SummaryText` | text | 通俗摘要 |
| `ReportJson` | text | 解析后的结构化报告 |
| `RawRequest` | text | 脱敏后的请求信息，可选 |
| `RawResponse` | text | Qwen 原始返回 |
| `ModelProvider` | varchar | `DashScope` |
| `ModelName` | varchar | 如 `qwen-vl-plus` |
| `PromptVersion` | varchar | Prompt 版本 |
| `ErrorCode` | varchar | 错误码 |
| `ErrorMessage` | text | 错误信息 |
| `DurationMs` | int | 调用耗时 |
| `IsDelete` | boolean/int | 软删除 |

### 7.3 列表 DTO 扩展

`TLaryngoscopePhotoDto` 或列表查询 DTO 建议追加两个只读字段：

```json
{
  "LatestLocalPrediction": {
    "Id": 1001,
    "Status": "SUCCESS",
    "PredictedLabel": "健康",
    "Confidence": 0.92,
    "HeatmapUrl": "/uploads/laryngoscope/heatmap/xxx.png",
    "CreatedTime": "2026-04-12 18:20:00"
  },
  "LatestQwenAnalysis": {
    "Id": 2001,
    "Status": "SUCCESS",
    "RiskLevel": "低",
    "SummaryText": "当前图片未见明显高风险提示，仍建议结合症状和医生检查判断。",
    "CreatedTime": "2026-04-12 18:22:00"
  }
}
```

## 8. 接口设计

### 8.1 本地模型预测

#### 发起预测

`POST /Front/Laryngoscope/LocalPredict`

请求：

```json
{
  "Id": 123,
  "ForceRegenerate": false
}
```

响应：

```json
{
  "PredictionId": 1001,
  "LaryngoscopePhotoId": 123,
  "Status": "SUCCESS",
  "PredictedClassId": 0,
  "PredictedLabel": "健康",
  "Confidence": 0.92,
  "Probabilities": [0.92, 0.03, 0.02, 0.01, 0.01, 0.01],
  "HeatmapUrl": "/uploads/laryngoscope/heatmap/xxx.png",
  "ModelName": "RepAlexNet",
  "ModelVersion": "repalexnet_heatmap_n100",
  "CreatedTime": "2026-04-12 18:20:00",
  "ErrorCode": null,
  "ErrorMessage": null
}
```

#### 获取预测结果

`POST /Front/Laryngoscope/GetLocalPrediction`

请求：

```json
{
  "Id": 123
}
```

说明：`Id` 为 `TLaryngoscopePhoto.Id`，默认返回该照片最新本地预测结果；如需历史记录，可增加 `ListLocalPredictions` 接口。

### 8.2 Qwen-VL 图像分析

#### 发起分析

`POST /Front/Laryngoscope/QwenAnalyze`

请求：

```json
{
  "Id": 123,
  "ForceRegenerate": false,
  "IncludeLocalPredictionContext": false
}
```

响应：

```json
{
  "AnalysisId": 2001,
  "LaryngoscopePhotoId": 123,
  "Status": "SUCCESS",
  "RiskLevel": "低",
  "SummaryText": "当前图片未见明显高风险提示，仍建议结合症状和医生检查判断。",
  "ReportJson": {
    "risk_level": "低",
    "plain_summary": "当前图片未见明显高风险提示，仍建议结合症状和医生检查判断。",
    "image_observations": ["图像清晰度可用于初步观察"],
    "possible_explanations": ["需结合医生喉镜检查和症状判断"],
    "suggestions": ["保持记录，若症状持续请复诊"],
    "when_to_see_doctor": ["声音嘶哑持续超过两周", "吞咽痛或出血"],
      "retest_tips": ["下次拍摄时保持光线均匀，避免遮挡"],
      "disclaimer": "本结果仅供健康参考，不替代医生诊断。"
  },
  "ModelName": "qwen-vl-plus",
  "PromptVersion": "laryngoscope-image-v1",
  "IncludeLocalPredictionContext": false,
  "CreatedTime": "2026-04-12 18:22:00",
  "ErrorCode": null,
  "ErrorMessage": null
}
```

#### 获取分析结果

`POST /Front/Laryngoscope/GetQwenAnalysis`

请求：

```json
{
  "Id": 123
}
```

说明：`Id` 为 `TLaryngoscopePhoto.Id`，默认返回该照片最新 Qwen-VL 图像分析结果；如需历史记录，可增加 `ListQwenAnalyses` 接口。

### 8.3 与现有列表接口的关系

`POST /Front/Laryngoscope/List` 和 `POST /Front/Laryngoscope/Get` 建议返回最新本地预测摘要和最新 Qwen-VL 分析摘要，方便前端在卡片中直接展示状态，不必为每张卡片额外请求两次接口。

## 9. 后端实现建议

### 9.1 本地模型预测服务

建议新增：

- `LaryngoscopeLocalPredictionService`
- `LaryngoscopeLocalPredictionServiceImpl`
- `LaryngoscopeLocalPredictionRecordDto`
- `LaryngoscopeLocalPredictInput`
- `LaryngoscopeLocalPredictOutput`

核心逻辑：

1. 校验登录用户。
2. 查询 `TLaryngoscopePhoto` 并校验归属。
3. 将 `LaryngoscopePhotoUrl` 映射到可读本地文件。
4. 创建预测记录，状态为处理中。
5. 调用 Python 脚本或封装后的 Python 推理服务。
6. 读取 `predictions.json`，定位当前图片结果。
7. 将热力图移动或复制到系统统一静态资源目录。
8. 更新预测记录为成功。
9. 异常时更新预测记录为失败，并返回错误信息。

模型路径建议配置化：

```yaml
laryngoscope:
  local-prediction:
    enabled: true
    python-path: python
    script-path: laryngoscope prediction/predict_larynscope.py
    model-path: laryngoscope prediction/repalexnet_heatmap_n100.pth
    output-root: external-resources/laryngoscope-predictions
    device: same-as-audio-model
    timeout-seconds: 120
```

说明：本地喉镜模型运行环境应与当前音频模型推理环境保持一致，不单独引入大范围环境改造；如当前音频模型链路已使用固定 Python 环境或 Conda 环境，本功能优先复用该环境。

### 9.2 Qwen-VL 图像分析服务

建议在现有 `FrontAIService` 基础上扩展，或新增喉镜专用服务：

- `LaryngoscopeQwenAnalysisService`
- `LaryngoscopeQwenAnalysisServiceImpl`
- `LaryngoscopeQwenAnalyzeInput`
- `LaryngoscopeQwenAnalyzeOutput`

核心逻辑：

1. 校验登录用户。
2. 查询 `TLaryngoscopePhoto` 并校验归属。
3. 将 `LaryngoscopePhotoUrl` 映射到可读本地文件。
4. 读取图片并转为 Base64。
5. 构造喉镜图像分析 Prompt。
6. 调用 `qwenClient.chatWithPngBase64(prompt, imageBase64)`。
7. 解析模型返回 JSON。
8. 保存 `TLaryngoscopeQwenAnalysisRecord`。
9. 异常时保存失败记录，错误码按类型区分。

建议报告类型常量：

```java
private static final String REPORT_TYPE_LARYNGOSCOPE_IMAGE = "LARYNGOSCOPE_IMAGE_ANALYSIS";
private static final String PROMPT_VERSION_LARYNGOSCOPE_IMAGE = "laryngoscope-image-v1";
private static final String MODEL_NAME_LARYNGOSCOPE_IMAGE = "qwen-vl-plus";
```

### 9.3 错误码建议

本地模型预测：

| 错误码 | 说明 |
| --- | --- |
| `LOCAL_PHOTO_NOT_FOUND` | 喉镜记录不存在或无权限 |
| `LOCAL_IMAGE_FILE_MISSING` | 图片文件不存在 |
| `LOCAL_MODEL_CONFIG_MISSING` | 模型路径或脚本路径未配置 |
| `LOCAL_MODEL_PROCESS_FAILED` | Python 推理进程失败 |
| `LOCAL_MODEL_TIMEOUT` | 推理超时 |
| `LOCAL_MODEL_OUTPUT_INVALID` | `predictions.json` 缺失或格式错误 |
| `LOCAL_HEATMAP_MISSING` | 热力图文件缺失 |

Qwen-VL 图像分析：

| 错误码 | 说明 |
| --- | --- |
| `QWEN_PHOTO_NOT_FOUND` | 喉镜记录不存在或无权限 |
| `QWEN_IMAGE_FILE_MISSING` | 图片文件不存在 |
| `QWEN_CONFIG_MISSING` | Qwen API 配置缺失 |
| `QWEN_CALL_FAILED` | Qwen 接口调用失败 |
| `QWEN_JSON_PARSE_FAILED` | Qwen 返回内容不是合法 JSON |
| `QWEN_REPORT_SCHEMA_INVALID` | Qwen JSON 缺少必要字段 |

## 10. 前端设计要求

本功能前端风格必须参考 `readme text/主页UI设计风格说明.md`，并以 `HealthControl.uniapp/pages/Front/Index.vue`、`HealthControl.uniapp/App.vue`、`HealthControl.uniapp/components/footer-bar/footer-bar.vue` 的现有主页体系为风格基准。喉镜影像档案不是后台管理列表，也不是单纯白底表单页，应作为主页风格在“影像档案 + 结果解读”场景下的延展。

### 10.1 页面位置

功能应放在“喉镜影像档案”模块内，不新增独立营销页或跳出式主流程。

建议入口：

- 在 `LaryngoscopePhotoList.vue` 的每张喉镜照片卡片中增加两个操作按钮。
- 新增喉镜图片详情页，建议页面为 `HealthControl.uniapp/pages/Front/LaryngoscopePhotoDetail.vue`，从列表卡片点击进入。
- 详情页展示原始喉镜图、检查信息、备注、本地模型预测区块和 Qwen 图像分析区块。
- 详情页顶部或图片下方提供 `本地模型预测` 与 `Qwen 图像分析` 两个操作入口。
- 结果详情可采用独立页面或底部弹层，但本地预测和 Qwen 分析的结果页/结果面板需要清楚区分。

### 10.2 与现有风格一致

前端需命中主页风格关键词：

- 清新健康
- 轻医疗
- 年轻化
- 高保真移动端
- 深浅对比明确
- 大圆角
- 卡片化
- 胶囊化
- 模块化分发
- 信息摘要优先
- 操作导向清晰

具体规则：

- 页面基础骨架优先使用 `hc-mobile-shell`、`hc-screen`、`hc-stack`，沿用主页的轻渐变浅绿背景、安全区留白和底部导航预留空间。
- 页面外壳不能回退成传统纯白列表页，也不能改成沉重纯深色背景。
- 继续使用现有全局样式类，如 `hc-card-dark`、`hc-card-soft`、`hc-card-lime`、`hc-pill-button`、`hc-pill-button-dark`、`hc-pill-button-soft`、`hc-interactive-pill`、`hc-interactive-card`、`hc-reveal-up`、`hc-reveal-fade`、`hc-shine`。
- 与 `LaryngoscopePhotoList.vue`、`LaryngoscopePhotoEdit.vue` 的卡片、按钮、间距和信息层级保持一致。
- 可参考 `AudioAIReport.vue` 的 AI 报告分段展示方式，但视觉语言需适配喉镜影像档案模块。
- 色彩以主页绿色体系为主：`#8ec94f`、`#b5ea63`、`#cff28f`、`#f7fbe9`；深色锚点使用 `#151d17` 或现有 `--bg-card-contrast`。
- 状态色只用于状态提示：低风险/成功使用绿色，处理中使用品牌浅绿或中性色，中风险使用 `#efb34d`，高风险/失败使用 `#e46c49`；橙色和红色只做小面积提示，不作为页面主色。
- 一屏内一般只保留一个最强深色模块，避免多个 `hc-card-dark` 同时抢主视觉。
- 大卡片、结果摘要卡和按钮应延续主页的大圆角与胶囊化基因，不要使用后台式直角、小按钮和硬描边。
- 卡片应作为“叙事单元”：每张卡只承担一个摘要、一个结果、一个趋势或一个行动建议，不把所有内容塞进一个大容器。
- 本地预测结果中的热力图应作为图片结果展示，不要被过度装饰成外部预览，也不要做成复杂专业图表。
- Qwen-VL 分析报告应使用清晰分段展示，重点突出“摘要、观察、建议、就医提醒、免责声明”。
- 不引入与现有设计明显冲突的颜色主题、复杂渐变、硬边框、花哨插画、复杂 3D 图形或卡片套卡片结构。
- 移动端长文本必须自动换行，不遮挡图片、按钮或后续内容。

### 10.3 信息组织规则

信息表达必须先摘要，再展开：

- 列表页只展示每张喉镜照片的核心摘要、最新本地预测状态、最新 Qwen 分析状态和进入详情的操作，不在列表中塞完整概率分布或完整分析报告。
- 详情页先展示图片与关键状态摘要，再展示本地预测和 Qwen 分析的细节。
- 本地预测区先给预测标签和置信度，再展开概率分布和热力图。
- Qwen 分析区先给风险等级和通俗摘要，再展开观察点、可能解释、建议和就医提醒。
- 免责声明放在结果区末尾，但必须可见，不得隐藏在二级菜单中。
- 所有医学相关长内容应拆成短句列表，避免整段长文堆叠。
- 用户可手动选择“带入本地预测结果作为参考”时，开关文案要说明“仅作为参考”，不要暗示会提高诊断准确性。

### 10.4 列表页卡片布局

喉镜照片卡片建议新增两个能力区块：

```text
喉镜照片信息
原有元信息

本地模型预测
状态 / 最新标签 / 置信度 / 查看热力图 / 重新预测

Qwen 图像分析
状态 / 风险等级 / 摘要 / 查看分析 / 重新分析
```

按钮文案建议：

- 本地模型预测
- 查看预测结果
- 查看热力图
- 重新预测
- Qwen 图像分析
- 查看分析结果
- 重新分析

列表页视觉要求：

- 卡片列表使用 `hc-card-soft` 作为主容器，保持轻、透、可扫读。
- 每张卡片只展示 1 张缩略图、2 到 4 项检查元信息和两个功能状态摘要。
- 如果本地预测和 Qwen 分析都未生成，主操作优先引导进入详情页，而不是在列表里铺满多个强按钮。
- 如果已有结果，列表只显示简短状态，如“本地预测：健康 92%”“Qwen 分析：低风险”，完整结果进入详情页查看。
- 编辑、删除等原有操作保持弱一级，不与“查看详情”抢主视觉。

### 10.5 喉镜图片详情页布局

新增 `HealthControl.uniapp/pages/Front/LaryngoscopePhotoDetail.vue`，页面结构建议：

```text
顶部头部区
页面标题 / 返回 / 检查时间摘要

原图摘要区
喉镜图片 / 检查医院 / 检查方式 / 视角体位 / 病变侧别 / 备注

结果摘要双卡区
本地模型预测摘要
Qwen 图像分析摘要

本地模型预测详情
预测标签 / 置信度 / 概率分布 / 热力图 / 重新预测

Qwen 图像分析详情
带入本地预测参考开关 / 风险等级 / 摘要 / 观察 / 建议 / 就医提醒 / 重新分析

免责声明与下一步行动
```

详情页视觉规则：

- 页面头部可使用轻量标题区，不做营销 Hero。
- 原图区域是功能内容，不要做成装饰性预览卡；图片必须清晰、稳定、可点击预览。
- 结果摘要双卡建议遵循主页“一深一浅”结构：当前最重要或用户刚完成的结果用深色摘要卡，另一项用浅色摘要卡，避免两个强卡同时竞争。
- 概率分布可使用轻条形图表达，不使用坐标轴、复杂图例或专业报表式图表。
- Qwen 分析长报告按模块拆为 `观察`、`可能解释`、`建议`、`就医提醒`、`复查/重拍建议`，每个模块用短标题和短句列表。
- “重新预测”“重新分析”属于次级操作，除非当前状态为失败，否则不要同时做成两个最强深色按钮。
- 底部可保留一个主要行动，如“返回档案”或“继续咨询”，其他操作用浅色胶囊按钮。

### 10.6 文案与动效要求

- 标题尽量控制在 6 到 14 个字，例如“喉镜影像详情”“本地模型预测”“Qwen 图像分析”。
- 描述尽量控制在 1 到 2 行，面向普通用户，避免长段医学术语直抛。
- CTA 文案尽量 2 到 6 个字，如“查看详情”“重新预测”“重新分析”“查看热力图”。
- 本地模型相关文案统一使用“本地模型预测”，不要写成“AI 预测”。
- Qwen-VL 相关文案可写成“Qwen 图像分析”或“AI 图像分析建议”，但结果必须强调“建议/参考”，避免“诊断结论”。
- 动效优先使用 `hc-reveal-up`、`hc-reveal-fade`、`hc-interactive-card`、`hc-interactive-pill`，模块按层级递进出现，不做大幅旋转、长时间弹簧或高频闪烁。

## 11. 非功能需求

### 11.1 性能

- 本地推理和 Qwen-VL 调用都可能耗时，应在前端展示加载状态。
- 后端应配置超时，避免单次请求长时间挂起。
- 本地热力图应保存为静态资源 URL，避免每次查看重复生成。
- 列表接口只返回摘要字段，完整概率分布、完整报告 JSON 可在详情接口中返回。

### 11.2 安全与隐私

- 所有图片读取、预测、分析接口均必须校验用户身份与数据归属。
- Qwen-VL 请求中只传入必要图像和非敏感上下文。
- 不在日志中输出完整 Base64 图片内容。
- 错误日志中避免泄露用户隐私和完整文件路径。
- Qwen-VL 报告必须包含免责声明。

### 11.3 可维护性

- 本地模型路径、输出目录、设备、超时时间应配置化。
- Qwen-VL Prompt 应有版本号，后续修改 Prompt 后便于追踪。
- 模型名、模型版本、脚本路径、Prompt 版本均应随记录归档。
- 本地预测表与 Qwen 分析表分离，避免后续统计和排查混淆。

## 12. 测试与验收

### 12.1 本地模型预测验收

- 选择历史喉镜照片后可以发起本地模型预测。
- 后端使用 `repalexnet_heatmap_n100.pth` 完成推理。
- 预测成功后展示预测标签、置信度、概率分布和热力图。
- 预测失败时展示明确失败信息，并允许重新预测。
- 重新预测会生成新记录，历史记录不被删除。
- 用户无法预测他人喉镜照片。

### 12.2 Qwen-VL 图像分析验收

- 选择历史喉镜照片后可以发起 Qwen-VL 图像分析。
- 后端复用现有 Qwen-VL 客户端完成图片分析。
- 分析成功后展示风险等级、摘要、观察点、建议、就医提醒和免责声明。
- Qwen-VL 返回非 JSON 或字段缺失时，后端能记录失败并给出可理解错误。
- 重新分析会生成新记录，历史记录不被删除。
- 用户无法分析他人喉镜照片。

### 12.3 独立性验收

- 只做本地模型预测时，Qwen-VL 分析状态仍保持未分析。
- 只做 Qwen-VL 分析时，本地模型预测状态仍保持未预测。
- 本地模型预测失败不影响 Qwen-VL 分析。
- Qwen-VL 分析失败不影响本地模型预测。
- 两类结果在列表卡片和详情展示中名称清晰，不混称、不覆盖。

### 12.4 前端验收

- 新增入口与结果展示符合 `readme text/主页UI设计风格说明.md` 的主页风格规范。
- 列表页与详情页使用 `hc-mobile-shell`、`hc-screen`、`hc-stack` 等移动端页面基座，不回退成纯白后台式列表。
- 页面能体现绿色健康感、轻医疗、大圆角、胶囊控件、深浅对比、摘要优先和操作导向。
- 一屏内只有一个明确最强视觉锚点，不出现多个同强度深色卡片互相抢焦点。
- 喉镜图片详情页已经新增，且从列表可进入。
- 列表页只展示摘要，完整本地预测结果和 Qwen 分析报告在详情页承接。
- 本地预测结果使用“本地模型预测”命名，不出现“AI 预测”混称。
- Qwen-VL 分析结果使用“建议/参考”类措辞，不展示为诊断结论。
- 热力图作为图片结果稳定展示，不做成复杂专业图表或外部预览壳。
- Qwen-VL 报告按“摘要、观察、可能解释、建议、就医提醒、复查/重拍建议、免责声明”分段展示。
- 移动端不同宽度下按钮、图片、热力图和长文本不溢出。
- 卡片不出现卡片套卡片的拥挤结构。
- 加载、成功、失败、空状态都有明确文案。
- 免责声明在本地预测结果和 Qwen-VL 分析结果中均可见。
- 动效只使用轻量进入和点击反馈，不出现大幅旋转、高频闪烁或长时间弹簧动画。

### 12.5 建议测试用例

- 后端单元测试：本地模型输出 JSON 解析。
- 后端单元测试：Qwen-VL JSON 报告解析与字段兜底。
- 后端单元测试：用户权限校验。
- 后端集成测试：图片不存在、模型路径不存在、Qwen 配置缺失。
- 前端页面测试：未预测、预测成功、预测失败、未分析、分析成功、分析失败。
- 前端回归测试：原有上传、编辑、删除、列表分页不受影响。

## 13. 实施里程碑

1. 文档和术语确认：明确“本地模型预测”与“Qwen-VL 图像分析”为两个独立功能。
2. 数据库扩展：新增本地预测记录表与 Qwen 分析记录表。
3. 本地模型服务：封装 `predict_larynscope.py` 调用、结果解析、热力图归档。
4. Qwen-VL 服务：复用现有 Qwen 客户端，新增喉镜图像 Prompt、报告解析与持久化。
5. 前台接口：新增本地预测和 Qwen 分析接口，并扩展列表摘要字段。
6. 用户端页面：新增喉镜图片详情页，并在列表与详情页中加入本地预测、热力图展示、Qwen 分析和手动带入本地预测结果的交互。
7. 测试验收：完成权限、异常、样例图预测、Qwen Mock、前端移动端适配验证。
8. 管理端页面：本期暂缓，不纳入小程序功能首版交付。

## 14. 已确认设计约束

- Qwen-VL 模型名沿用当前配置 `qwen-vl-plus`，API 接口也复用之前 Mel/MFCC 图谱分析使用的 Qwen 调用接口。
- Qwen-VL 喉镜图像 Prompt 的医学措辞需要进一步审核并写得更严谨，只能作为健康建议和风险提示，不替代医生下定论。
- 需要新增喉镜图片详情页。
- 本地模型运行环境与当前音频模型运行环境保持一致，不做大范围改造。
- 热力图输出目录统一放入 `external-resources/laryngoscope-predictions`。
- 本期先完成小程序功能，管理端暂不改动，也不支持下载 Qwen-VL 报告 JSON。
- 允许用户在 Qwen-VL 分析时手动选择“带入本地模型预测结果作为参考”，默认不带入。

## 15. 数据库变更说明

本次功能建议新增两张业务结果表，原有 `tlaryngoscopephoto` 表本期不需要改字段：

| 表名 | 类型 | 用途 |
| --- | --- | --- |
| `tlaryngoscope_local_prediction_record` | 新增表 | 保存本地 `RepAlexNet` 模型预测结果、概率分布和热力图路径 |
| `tlaryngoscope_qwen_analysis_record` | 新增表 | 保存 Qwen-VL 喉镜图像分析报告、Prompt 版本、是否带入本地预测参考和调用结果 |
| `tlaryngoscopephoto` | 原表 | 不改字段，通过新增表的 `LaryngoscopePhotoId` 关联 |

可直接导入的 SQL 变更文件见：`laryngoscope_prediction_db_changes.sql`。
