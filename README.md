# 居家喉部健康自查小程序

`HealthControl` 是一个面向居家场景的喉部健康自查与长期管理平台。项目整合微信小程序 / UniApp 用户端、Vue 3 管理后台、Spring Boot 后端、Python 本地模型推理，以及 Qwen 多模态 AI 辅助解读，覆盖从音频筛查、喉镜影像上传、风险提示、健康档案沉淀到随访管理的完整流程。

> 本项目用于学习、演示和产品原型验证。系统中的模型预测、AI 解读和健康建议仅供参考，不构成医疗诊断、治疗意见或替代专业医生判断。

## 当前功能

### 用户端小程序

- 账号注册、登录、个人资料维护、密码修改、微信绑定。
- 居家喉部音频录制、上传、筛查结果查看。
- AI 智能问答、音频频谱上传解读、AI 解读报告查看。
- 喉镜照片上传、编辑、列表分组、详情查看、删除。
- 喉镜影像本地模型预测，返回预测类别、置信度、概率分布和 Grad-CAM 热力图。
- 喉镜影像 Qwen-VL 分析，支持结合本地预测结果生成结构化风险提示。
- 健康档案、症状日志、个性化提醒、消息通知。
- 风险问卷评估、历史答卷、评估结果查看。
- 饮食管理、食物库、饮食记录、健康科普、收藏与发布。
- 就医辅助、医院医生信息、就医准备报告、推荐记录。
- 隐私设置、授权使用明细、数据导出申请、数据删除申请和历史记录。
- 模型优化标注闭环，支持用户侧对样本和预测结果进行反馈。

### 管理后台

- 用户、筛查记录、音频 AI 报告、喉镜照片、模型调用日志管理。
- 健康科普、科普分类、评论、点赞、收藏管理。
- 饮食类别、食物库、饮食记录管理。
- 风险问卷、问题、选项、用户答卷管理。
- 就医医院医生、就医推荐、就医准备报告、导出记录管理。
- 健康提醒、消息通知、隐私设置、数据权利申请处理。
- 模型优化标签与标注数据维护。

### 后端服务

- Spring Boot 3.3.1 提供 REST API。
- MyBatis Plus 负责数据访问。
- MySQL 存储用户、健康档案、筛查、问卷、饮食、科普、喉镜影像与 AI 报告等数据。
- JWT 登录鉴权、邮件能力、文件上传和外部资源访问。
- 音频模型推理、频谱生成、模型调用日志与 AI 解读链路。
- 喉镜影像本地预测接口：
  - `/Front/Laryngoscope/LocalPredict`
  - `/Front/Laryngoscope/GetLocalPrediction`
- 喉镜影像 Qwen-VL 分析接口：
  - `/Front/Laryngoscope/QwenAnalyze`
  - `/Front/Laryngoscope/GetQwenAnalysis`

### AI 与模型能力

- 音频筛查：本地 Python 模型用于喉部音频初筛，并生成 MFCC / Mel 等频谱图。
- 音频 AI 解读：调用 Qwen 多模态能力，对频谱图和初筛上下文进行结构化健康风险解读。
- 喉镜本地预测：使用 RepAlexNet 权重对喉镜影像进行分类预测，并生成 Grad-CAM 热力图。
- 喉镜 Qwen-VL 分析：基于喉镜图片、用户填写信息和可选本地模型结果，生成风险级别、图像观察、可能解释、建议和复查提示。

## 技术栈

| 层级 | 技术 |
| --- | --- |
| 用户端 | UniApp, Vue 3, Pinia, uni-ui, SCSS |
| 管理后台 | Vue 3, Vite, Element Plus, Pinia, Axios |
| 后端 | Spring Boot 3.3.1, MyBatis Plus, MySQL, JWT, Mail, POI |
| 音频推理 | Python, PyTorch, librosa, matplotlib |
| 喉镜影像推理 | Python, PyTorch, torchvision, OpenCV, Pillow, Grad-CAM |
| AI 解读 | Qwen / Qwen-VL, DashScope compatible API |
| 大文件管理 | Git LFS |

## 仓库结构

```text
.
├── HealthControl.uniapp          # 用户端小程序 / H5
├── HealthControl.elementui       # Vue 3 + Element Plus 管理后台
├── HealthControl.springboot      # Spring Boot 后端服务
├── laryngoscope prediction       # 喉镜影像本地预测脚本与模型权重
├── voice                         # 音频筛查相关 Python 脚本和资源
├── scripts                       # 数据处理、图片生成等辅助脚本
├── readme text                   # 需求、实现、测试、申报和演示材料
├── .gitattributes                # Git LFS 规则
├── .gitignore
└── README.md
```

## 克隆与大文件

喉镜模型权重已使用 Git LFS 管理。首次克隆后请先安装 Git LFS，并拉取模型文件：

```bash
git lfs install
git clone https://github.com/XUGCC/healthcontrol.git
cd healthcontrol
git lfs pull
```

当前 LFS 管理的主要文件包括：

- `laryngoscope prediction/repalexnet_heatmap_n100.pth`
- `laryngoscope prediction/larynscope/net.pth`
- `laryngoscope prediction/store/repalexnet_heatmap_n100.pth`
- `laryngoscope prediction/store/store/repalexnet_heatmap_n100.pth`

## 快速开始

### 1. 环境准备

- JDK 17
- Node.js 16+
- MySQL 8.x
- Python 3.8+
- Git LFS
- HBuilderX 或微信开发者工具

### 2. 后端配置

公开仓库不包含真实运行配置。请复制示例配置：

```bash
copy HealthControl.springboot\src\main\resources\application.example.yml HealthControl.springboot\src\main\resources\application.yml
```

然后在 `application.yml` 中配置：

- MySQL 数据库连接
- 邮件账号和授权码
- Qwen / DashScope API Key
- 文件访问地址和端口

推荐使用环境变量注入 Qwen Key：

```bash
set AI_QWEN_API_KEY=your-api-key
```

喉镜本地预测也可按需覆盖默认配置：

```yaml
laryngoscope:
  local-prediction:
    python-path: python
    script-path: laryngoscope prediction/predict_larynscope.py
    model-path: laryngoscope prediction/repalexnet_heatmap_n100.pth
    output-root: external-resources/laryngoscope-predictions
    device: auto
    timeout-seconds: 120
```

### 3. 启动后端

Windows:

```bash
cd HealthControl.springboot
mvnw.cmd spring-boot:run
```

macOS / Linux:

```bash
cd HealthControl.springboot
./mvnw spring-boot:run
```

默认服务地址：

```text
http://localhost:7245
```

### 4. 启动管理后台

```bash
cd HealthControl.elementui
npm install
npm run dev
```

### 5. 运行小程序端

1. 使用 HBuilderX 打开 `HealthControl.uniapp`。
2. 根据本地后端地址调整前端请求配置。
3. 运行到微信小程序、浏览器 H5 或移动端调试环境。

### 6. 单独运行喉镜预测脚本

```bash
cd "laryngoscope prediction"
python predict_larynscope.py --input store/larynscope/202012010042005.jpg --device cpu
```

常用参数：

- `--input`：单张图片或图片目录。
- `--output-dir`：指定输出目录。
- `--recursive`：递归扫描子目录图片。
- `--device`：`auto`、`cpu` 或 `cuda`。

输出内容：

- `heatmaps/`：Grad-CAM 热力图。
- `predictions.json`：结构化预测结果。
- `predictions_summary.txt`：文字汇总。

### 7. 单独运行音频筛查

```bash
cd voice
python detect_audio.py --audio "你的音频绝对路径" --outdir "输出目录"
```

## 数据库说明

本地存在的 `healthcontrol.sql` 是运行环境数据库快照，可能包含运行数据和敏感信息，因此不上传到公开仓库。部署或演示时建议准备一份脱敏后的初始化 SQL，至少包含：

- 表结构。
- 基础字典和菜单数据。
- 脱敏后的示例用户和演示记录。
- 不含真实 API Key、邮箱授权码、用户隐私和完整业务日志。

## 安全与隐私边界

仓库默认排除了以下内容：

- `HealthControl.springboot/src/main/resources/application.yml`
- `healthcontrol.sql`
- `HealthControl.springboot/external-resources/`
- `HealthControl.springboot/target/`
- `HealthControl.elementui/node_modules/`
- `HealthControl.uniapp/unpackage/`
- 本地 JDK、缓存、运行输出和临时文件

如果需要公开演示，请确保所有配置、数据库快照、上传文件和日志都已经脱敏。

## 重要文档

- [需求文档](./readme%20text/%E9%9C%80%E6%B1%82.md)
- [喉镜影像 AI 预测功能需求文档](./readme%20text/%E5%96%89%E9%95%9C%E5%BD%B1%E5%83%8FAI%E9%A2%84%E6%B5%8B%E5%8A%9F%E8%83%BD%E9%9C%80%E6%B1%82%E6%96%87%E6%A1%A3.md)
- [喉镜预测脚本说明](./laryngoscope%20prediction/%E5%96%89%E9%95%9C%E9%A2%84%E6%B5%8B%E8%84%9A%E6%9C%AC%E8%AF%B4%E6%98%8E.md)
- [喉镜预测项目结构说明](./laryngoscope%20prediction/%E9%A1%B9%E7%9B%AE%E7%BB%93%E6%9E%84%E8%AF%B4%E6%98%8E.md)
- [AI 模块接入实施指南](./readme%20text/AI%E6%A8%A1%E5%9D%97%E6%8E%A5%E5%85%A5%E5%AE%9E%E6%96%BD%E6%8C%87%E5%8D%97-%E9%80%9A%E4%B9%89%E5%8D%83%E9%97%AEQwen-VL%E5%A4%9A%E6%A8%A1%E6%80%81.md)
- [AI 智能诊断模块需求文档](./readme%20text/AI%E6%99%BA%E8%83%BD%E8%AF%8A%E6%96%AD%E6%A8%A1%E5%9D%97-%E9%9C%80%E6%B1%82%E6%96%87%E6%A1%A3%EF%BC%88Qwen-VL%E5%A4%9A%E6%A8%A1%E6%80%81Base64%E7%89%88%EF%BC%89.md)
- [项目申报材料](./readme%20text/%E9%A1%B9%E7%9B%AE%E7%94%B3%E6%8A%A5%E6%9D%90%E6%96%99-%E5%B1%85%E5%AE%B6%E5%96%89%E9%83%A8%E5%81%A5%E5%BA%B7%E9%9F%B3%E9%A2%91%E8%87%AA%E6%9F%A5%E5%B0%8F%E7%A8%8B%E5%BA%8F.md)
- [小程序演示视频讲解流程](./readme%20text/%E5%B0%8F%E7%A8%8B%E5%BA%8F%E6%BC%94%E7%A4%BA%E8%A7%86%E9%A2%91%E8%AE%B2%E8%A7%A3%E6%B5%81%E7%A8%8B.md)

## 已验证

后端当前通过以下检查：

```bash
cd HealthControl.springboot
mvnw.cmd clean test
```

测试结果：

```text
BUILD SUCCESS
Tests run: 5, Failures: 0, Errors: 0
```

## 后续优化方向

- 补充脱敏数据库初始化脚本。
- 增加 Docker Compose 一键部署。
- 给前端补充环境变量模板和截图说明。
- 为喉镜预测脚本补充 `requirements.txt`。
- 将模型输出目录、推理环境和任务状态进一步服务化。
- 增加更多后端接口测试和小程序端关键流程测试。

## 免责声明

本项目不提供医学诊断结论。音频筛查、喉镜影像预测、AI 解读和健康建议均只能作为健康管理参考。若出现声音嘶哑持续不缓解、吞咽困难、呼吸困难、咳血、颈部肿块或其他不适，请及时前往正规医院耳鼻喉科就诊。
