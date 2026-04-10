# AI 模块接入实施指南（通义千问 Qwen-VL 多模态）

> 目标：在现有「居家喉部健康音频自查小程序」中新增 **AI 智能解读/问答模块**。  
> 技术路线：使用 **阿里云 DashScope（通义千问）Qwen-VL 系列多模态模型 API**，对现有检测流程产生的 **Mel/MFCC 图谱（PNG）** 做“图谱解读 + 建议”，并支持基于历史记录复用分析。  
> 适用工程：  
> - 小程序端：`HealthControl.uniapp`（UniApp + Vue3）  
> - 后端：`HealthControl.springboot`（Spring Boot 3 + MyBatis Plus + MySQL）  
> - 现有检测记录表：`taudioscreenrecord`（含 `MelSpectrumUrl`/`MfccSpectrumUrl`/`Deepseek*` 字段）  

---

## 1. 接入前你需要理解的“最小闭环”

你现在的系统已经有完整的音频检测闭环（**上传音频 → 创建记录 → 触发检测 → 生成 Mel/MFCC 图 → 入库 → 前端展示**）。新增 AI 模块最推荐的做法是：

- **不改动**现有 Python 推理脚本（`voice/detect_audio.py`）的主要流程；
- 直接使用 `taudioscreenrecord` 里的 `MelSpectrumUrl`（或 `MfccSpectrumUrl`）作为“图片输入”；
- 由 **Spring Boot 后端**去调用 Qwen-VL 多模态 API，拿到 AI 的解读文本；
- 将 AI 结果写回数据库（建议复用 `DeepseekDetailReport` / `DeepseekMedicalSummary` 字段，或后续再拆新表）。

这样做的好处：**实现成本最低**、和你现有 “模型调用日志/标注闭环/隐私设置” 的工程结构最一致。

---

## 2. 账号开通与 API Key 获取（DashScope）

### 2.1 注册/登录阿里云账号

1. 打开阿里云控制台并登录（如果没有账号先注册）。
2. 确保你的账号已完成必要的实名/安全设置（部分能力可能需要）。

### 2.2 开通 DashScope（通义千问模型服务）

1. 在阿里云中搜索 **DashScope（通义千问）** 服务并进入控制台。
2. 找到 **API Key 管理**（或“访问密钥/应用 Key”一类入口，名称可能随控制台版本调整）。
3. 创建一个新的 Key：
   - 建议命名：`HealthControl-QwenVL-dev`
   - 权限：默认即可（如果可选范围权限，建议只开放需要的模型调用权限）

### 2.3 保存 API Key（重要）

创建后你会拿到一串 Key（类似 `sk-xxxxx`）。请遵循：

- **不要**把 Key 写死在前端（小程序/H5/后台网页）里；
- **不要**提交到 Git 仓库；
- Key 只放在 **后端服务的环境变量** 或 **本地不入库的配置文件** 中；
- 如果泄露，立刻在 DashScope 控制台 **作废并重建**。

---

## 3. 联调前检查：你的图片是否“能被外部模型访问”

Qwen-VL 接收图片通常有两种方式：

- **方式 A：图片 URL（推荐）**  
  传入 `https://.../xxx.png`，模型从公网拉取图片。  
  要求：这个 URL **能被外网访问**，且最好是 HTTPS。

- **方式 B：Base64 图片（通用）**  
  后端读取 PNG 文件 → base64 编码 → 直接放在请求体里。  
  优点：不依赖公网可访问；缺点：请求体会变大，吞吐更差。

你现在 `MelSpectrumUrl` 样例是 `http://localhost:7245/..._mel.png`，这对云端模型来说通常 **不可访问**（云端无法访问你本机 localhost）。

因此你需要做一个选择：

- **开发阶段（本机联调）**：建议先走 **Base64**（最稳）；  
- **上线阶段**：建议把 `external-resources` 中的图谱文件放到可公网访问的对象存储（OSS/MinIO/腾讯云 COS 等），然后存入公网 URL，再用 **URL 输入**。

> 小结：如果你现在就想快速跑通，先按“Base64”方案实现；上线再切换为“URL 方案”。

---

## 4. 设计“AI 解读”的后端接口（推荐最小集）

建议在 `HealthControl.springboot` 新增一个 Controller（名称可自定）：

- `FrontAIAssistantController`

提供 2 个最小接口即可完成核心功能：

### 4.1 单条记录 AI 解读

- **接口**：`POST /FrontAI/AnalyzeAudioRecord`
- **输入**：`RecordId`
- **处理**：
  - 查 `taudioscreenrecord` 获取 `MelSpectrumUrl`（必要时也可带 `PrimaryScreenResult`、`PrimaryScreenConfidence`）
  - 读取图谱 PNG（URL 下载或磁盘读取）→ base64
  - 调用 Qwen-VL 多模态 API
  - 把结果写回 `DeepseekDetailReport`、`DeepseekMedicalSummary`（或新表）
- **输出**：返回 AI 的总结 + 结构化字段（风险等级、建议等）

### 4.2 基于历史记录复用分析（可后做）

- **接口**：`POST /FrontAI/AnalyzeHistory`
- **输入**：`RecordIds[]` 或时间范围
- **处理**：把历史检测关键字段整理为文本输入（不一定需要多模态）
- **输出**：趋势与建议

> 你可以先实现 4.1，把“AI 解读”按钮挂在 `AudioResult.vue` 上，形成可演示 MVP。

---

## 5. Qwen-VL 调用方式（你需要掌握的 3 件事）

你最终要做的事情只有三个：

1. **把图片（Mel/MFCC PNG）准备好**（URL 或 base64）
2. **写一个提示词（prompt）**，让模型输出你想要的字段与语气
3. **用 API Key 调用 DashScope 接口**，拿到返回内容

### 5.1 建议的 Prompt（适配你项目的“非医疗诊断”风格）

建议在 prompt 里强约束输出结构，便于前端展示/入库：

```text
你将收到一张“喉部嗓音 Mel 频谱图（由用户录音生成）”，请从信号特征角度进行健康风险解读。

重要约束：
1) 你不是医生，不能做出医疗确诊，只能给出健康风险提示与建议。
2) 不要使用“确诊/必然/一定是XX病”等措辞；如有风险请建议用户到耳鼻喉科就诊。
3) 输出必须为 JSON，字段如下：
{
  "risk_level": "低|中|高",
  "key_observations": ["...","..."],
  "possible_explanations": ["...","..."],
  "suggestions": ["...","..."],
  "when_to_see_doctor": ["...","..."],
  "disclaimer": "..."
}

如果信息不足，请在 key_observations 中说明“无法仅凭频谱图得出结论”，并给出如何重新采集更可靠的录音建议（安静环境、统一发音、时长等）。
```

### 5.2 API Key 在 Spring Boot 中如何存放（推荐方式）

**开发本机（Windows）**：用环境变量保存 Key（示例）

```powershell
setx DASHSCOPE_API_KEY "你的DashScope_API_Key"
```

然后重启 IDE/终端，让环境变量生效。

**后端读取**：在 `application.yml` 用占位符引用环境变量：

```yaml
ai:
  dashscope:
    api-key: ${DASHSCOPE_API_KEY:}
    # 你实际选用的模型名（示例写法，按 DashScope 文档为准）
    model: qwen-vl-plus
```

> 注意：不要把真实 Key 写进 `application.yml` 并提交仓库。

### 5.3 Spring Boot 调用 DashScope 的基本形式（伪代码）

DashScope 的具体接口路径/字段可能会随版本变化，以你实际控制台文档为准。下面给的是**实现思路**：

```java
String apiKey = dashscopeApiKeyFromConfig;

// 1) 组装 messages：包含文本 + 图片（base64 或 URL）
// 2) POST 到 DashScope 的多模态对话接口
// 3) 解析 response，拿到模型输出的 JSON 字符串
// 4) 落库（taudioscreenrecord.DeepseekDetailReport / DeepseekMedicalSummary）
```

你实现时建议封装成一个服务类，例如 `DashscopeQwenVlClient`，只暴露一个方法：

- `analyzeMelPng(byte[] pngBytes, Map<String,Object> context) -> AiReport`

这样未来换成文心/智谱也只需要替换 client。

---

## 6. 在你当前后端里落地的“最小改动”建议

### 6.1 复用你现有表结构

你已经在 `taudioscreenrecord` 预留了：

- `DeepseekApiVersion`
- `DeepseekDetailReport`
- `DeepseekMedicalSummary`

建议你在第一期直接复用：

- `DeepseekApiVersion`：存 `dashscope-qwen-vl-plus`（或你实际用的模型版本）
- `DeepseekDetailReport`：存模型输出的完整 JSON（上面 prompt 的 JSON）
- `DeepseekMedicalSummary`：存一个给用户看的 80~200 字总结（可从 JSON 中拼）

> 这不会破坏你的现有结构，后续如要更规范，再新建 `taudio_ai_report` 表即可。

### 6.2 记录模型调用日志（强烈建议）

你已有 `tmodelinterfacecalllog`，建议每次 AI 解读都写一条：

- 请求：RecordId、模型名、输入图片来源（mel/mfcc）、耗时
- 响应：成功/失败、错误信息、返回摘要

这样便于你做费用统计、错误排查、以及后续“标注闭环”。

---

## 7. UniApp 小程序端如何加入口（最小两处改动）

### 7.1 在结果页增加按钮

在 `pages/Front/AudioResult.vue` 增加一个按钮：

- 文案：`AI 智能解读（试验性）`
- 点击后：
  - 传当前记录 `Id` 到新页面 `AudioAIReport.vue`
  - 或直接请求后端生成报告，再展示

### 7.2 新增一个 AI 报告页

新增页面：`pages/Front/AudioAIReport.vue`，页面内容建议：

- 顶部：显示本次检测时间 + 风险等级（低/中/高）
- 中部：展示 Mel 图（用 `MelSpectrumUrl`）
- 下部：展示建议列表、就医提醒、免责声明
- 最底部：增加“重新解读”（可选，避免重复扣费可加冷却时间）

> AI 报告页的数据来源：调用 `POST /FrontAI/AnalyzeAudioRecord` 或 `GET /TAudioScreenRecord/Get` 读回已写入的 `Deepseek*` 字段。

---

## 8. 开发阶段的常见坑与排查

### 8.1 云端模型看不到你的 `localhost` 图片

现象：模型调用失败 / 下载图片失败。  
解决：

- 开发阶段：用 **Base64** 传图最稳；
- 上线阶段：把图谱上传到 OSS（公网 URL）再传 URL。

### 8.2 Key 无效 / 401 / 403

- 检查 Key 是否复制完整、是否有空格
- Key 是否已在控制台被禁用/过期
- DashScope 服务是否已开通

### 8.3 费用与频控

建议：

- 同一条记录的 AI 解读结果落库后，默认优先读取数据库，不要每次都重新调用；
- 增加“重新生成”按钮时，做频控（例如 1 分钟内只允许一次）。

---

## 9. 上线前的合规与产品提示（务必做）

你这个场景属于“健康风险提示”，强烈建议：

- 前端显著提示：**“仅供健康参考，不构成医疗诊断”**
- 输出语气约束：禁止“确诊”类语句
- 用户隐私设置联动：如果用户关闭外部 AI 调用，则不调用外部 API

---

## 10. 你可以按这个顺序开始做（推荐执行清单）

1. **开通 DashScope 并创建 API Key**（完成后先在本地环境变量保存）
2. **后端增加 `FrontAI/AnalyzeAudioRecord`**：
   - 入参 `RecordId`
   - 查表拿 `MelSpectrumUrl`
   - 读取 PNG → base64
   - 调用 Qwen-VL
   - 落库到 `DeepseekDetailReport/DeepseekMedicalSummary`
3. **小程序端在 `AudioResult.vue` 加按钮** → 跳 `AudioAIReport.vue`
4. **AI 报告页展示**（读后端返回或从记录里取 `Deepseek*`）
5. （可选）做历史趋势分析接口 `FrontAI/AnalyzeHistory`
6. （可选）做 AI 问答页 `AudioAIChat.vue`

---

## 11. 需要你补充给我的信息（我可以进一步把代码也写好）

如果你希望我下一步直接在你的仓库里把后端/前端代码也补齐，我需要你确认两点：

1. **你希望开发阶段先用哪种传图方式？**
   - A：Base64（最稳，适合本机联调）
   - B：URL（需要把图谱放到公网可访问地址）
2. **你希望 AI 解读结果落库复用现有 `Deepseek*` 字段吗？**
   - A：先复用（最快）
   - B：新建 `taudio_ai_report` 表（更规范）

