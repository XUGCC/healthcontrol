# 01-3 作品设计说明书 Gemini 流程图提示词

## 统一绘图要求

以下提示词只用于生成流程图、架构图、模块图、调用关系图和 ER 图。说明书里的 Markdown 表格不需要用 Gemini 绘制。

建议每次提示 Gemini 时，在对应图的提示词前追加下面这段统一风格要求：

```text
请生成一张适合放入正式项目说明书、竞赛材料和 PDF 文档的高清信息图。整体白底，医疗健康科技风格，主色为清新绿色、浅蓝色和深灰色，扁平化矢量设计，线条清晰，层级明确，中文文字准确无错别字，不要英文翻译，不要水印，不要卡通人物，不要复杂背景。画面比例 16:9，留出安全边距，适合插入 Word 或 PDF。所有箭头方向必须清楚，节点之间不要交叉混乱。
```

如果 Gemini 生成的中文有错字，建议保留图形结构，再用 PPT、Figma、draw.io 或 ProcessOn 手动替换文字。

## 图 2-1 总体架构图

```text
请生成一张标题为“居家喉部健康音频自查小程序总体架构图”的横版架构图。

一、整体布局
从左到右分为三层：
1. 左侧：接入层
2. 中间：业务服务层
3. 右侧：数据与模型层

二、左侧接入层节点
接入层包含两个上下排列的圆角矩形节点：
1. 用户端 UniApp / 微信小程序 / H5
2. 管理后台 Vue3 / Element Plus

三、中间业务服务层节点
中间放一个较大的核心圆角矩形，文字为：
Spring Boot REST API
在该节点下方用小字标注：
权限校验、文件上传、业务调度、模型调用、报告归档

四、右侧数据与模型层节点
右侧分成两组：

上方为“数据与文件资源”分组，包含：
1. MySQL 数据库
   小字标注：用户、检测记录、健康档案、AI报告、喉镜记录、隐私申请、标注数据
2. external-resources 文件资源
   小字标注：音频、喉镜图片、MFCC/Mel图谱、波形图、热力图、报告文件

下方为“模型与AI能力”分组，包含：
1. Python 音频推理脚本
   小字标注：detect_audio_siamese.py + voice_best_model.pth
2. Python 喉镜预测脚本
   小字标注：predict_larynscope.py + repalexnet_heatmap_n100.pth
3. Qwen / Qwen-VL 辅助解读
   小字标注：图谱解读、喉镜图像分析、健康建议、问答

五、必须绘制的连接关系和箭头方向
1. 用户端 UniApp / 微信小程序 / H5 → Spring Boot REST API
   箭头文字：登录、录音上传、喉镜上传、结果查看、AI解读
2. 管理后台 Vue3 / Element Plus → Spring Boot REST API
   箭头文字：用户管理、内容配置、记录查询、隐私申请处理
3. Spring Boot REST API ↔ MySQL 数据库
   双向箭头文字：业务数据读写
4. Spring Boot REST API ↔ external-resources 文件资源
   双向箭头文字：文件保存与访问
5. Spring Boot REST API → Python 音频推理脚本
   箭头文字：ProcessBuilder 调用音频模型
6. Python 音频推理脚本 → Spring Boot REST API
   箭头文字：返回JSON结果、置信度、图谱文件名
7. Python 音频推理脚本 → external-resources 文件资源
   箭头文字：生成 MFCC / Mel / 波形图
8. Spring Boot REST API → Python 喉镜预测脚本
   箭头文字：ProcessBuilder 调用喉镜模型
9. Python 喉镜预测脚本 → Spring Boot REST API
   箭头文字：返回类别、概率、热力图路径
10. Python 喉镜预测脚本 → external-resources 文件资源
    箭头文字：生成 Grad-CAM 热力图
11. Spring Boot REST API → Qwen / Qwen-VL 辅助解读
    箭头文字：发送图谱/图片Base64 + Prompt
12. Qwen / Qwen-VL 辅助解读 → Spring Boot REST API
    箭头文字：返回结构化JSON报告

六、视觉要求
接入层用浅蓝色，业务服务层用浅绿色并突出，数据与文件资源用浅灰绿色，模型与AI能力用浅青色。MySQL 用数据库圆柱图标，Python 模型节点用芯片或终端图标，Qwen/Qwen-VL 用云端AI图标。不要遗漏任何节点和箭头。
```

## 图 2-2 功能模块分解图

```text
请生成一张标题为“功能模块分解图”的模块分层图。

一、整体布局
中心放置主节点：
居家喉部健康自查小程序

从中心向外分出三大一级模块，建议做成三栏结构，也可以做成中心辐射结构：
1. 用户端
2. 管理端
3. 模型层

二、用户端模块
用户端下面必须包含 9 个子模块，并用箭头或层级线连接到“用户端”：
1. 音频自查
2. AI报告与问答
3. 喉镜影像档案
4. 健康档案与症状日志
5. 饮食建议与记录
6. 科普与问卷
7. 就医辅助与报告
8. 隐私设置与数据权利
9. 确诊标注与授权

三、管理端模块
管理端下面必须包含 5 个子模块，并用箭头或层级线连接到“管理端”：
1. 用户与检测记录管理
2. 食物/科普/问卷配置
3. 医院医生与就医建议
4. 模型日志与统计
5. 标注与隐私申请管理

四、模型层模块
模型层下面必须包含 3 个子模块，并用箭头或层级线连接到“模型层”：
1. Siamese音频检测
2. RepAlexNet喉镜预测
3. Qwen-VL辅助解读

五、补充连接关系
请在三个一级模块之间补充轻量关系线：
1. 用户端 → 模型层：发起自查、预测、AI解读
2. 模型层 → 用户端：返回检测结果、图谱、热力图、建议
3. 管理端 → 用户端：配置科普、饮食、问卷、医院医生等内容
4. 用户端 → 管理端：沉淀检测记录、标注授权、隐私申请等运营数据

六、视觉要求
用户端用绿色，管理端用蓝色，模型层用青色。中心节点最大，一级模块次之，子模块为小圆角卡片。层级关系必须清晰，不要把所有模块堆成一团。
```

## 图 2-3 主要调用关系图

```text
请生成一张标题为“主要调用关系图”的横向泳道时序图。

一、泳道设置
从左到右设置 6 条竖向泳道，每条泳道顶部放参与方名称：
1. 小程序用户
2. UniApp前端
3. Spring Boot后端
4. Python模型
5. MySQL
6. Qwen / Qwen-VL

二、主流程箭头，必须按顺序编号
1. 小程序用户 → UniApp前端
   箭头文字：录音/上传音频或喉镜图片
2. UniApp前端 → Spring Boot后端
   箭头文字：上传文件并发起检测/分析
3. Spring Boot后端 → Python模型
   箭头文字：ProcessBuilder调用本地模型
   在箭头旁用小字标注：音频检测脚本 / 喉镜预测脚本
4. Python模型 → Spring Boot后端
   箭头文字：返回JSON结果、图谱或热力图路径
5. Spring Boot后端 → MySQL
   箭头文字：保存检测记录、模型日志、健康档案
6. 小程序用户 → UniApp前端
   箭头文字：点击生成AI辅助解读
7. UniApp前端 → Spring Boot后端
   箭头文字：请求AI报告或图像分析
8. Spring Boot后端 → Qwen / Qwen-VL
   箭头文字：发送图谱/图片Base64 + 结构化Prompt
9. Qwen / Qwen-VL → Spring Boot后端
   箭头文字：返回JSON报告
10. Spring Boot后端 → MySQL
    箭头文字：保存AI报告和调用状态
11. Spring Boot后端 → UniApp前端
    箭头文字：返回结果、建议、免责声明
12. UniApp前端 → 小程序用户
    箭头文字：展示检测结果和健康建议

三、视觉要求
请使用横向时序图/泳道图，不要使用普通列表。每个箭头必须从源泳道指向目标泳道，方向清楚。同步请求用实线箭头，返回结果用虚线箭头。步骤编号用绿色圆点标注。整体不要拥挤，可以把流程分成“本地检测阶段”和“AI辅助解读阶段”两个浅色背景区域。
```

## 图 3-1 典型使用流程图

```text
请生成一张标题为“用户典型使用流程图”的闭环流程图。

一、主线流程
从左到右绘制主流程：
1. 进入首页
2. 录音或上传音频
3. 本地模型检测
4. 查看良性/疑似风险、置信度和图谱

二、从“查看良性/疑似风险、置信度和图谱”节点分出三条箭头
1. 向右上连接到：生成AI辅助解读
2. 向右连接到：同步健康档案和趋势
3. 向右下连接到：记录症状

三、后续健康管理链路
从“同步健康档案和趋势”继续向右连接：
同步健康档案和趋势 → 查看饮食建议 → 生成就医建议/就诊准备报告 → 就诊后提交确诊标注 → 授权后进入模型优化闭环

四、症状记录连接
从“记录症状”画一条箭头连接回“同步健康档案和趋势”，箭头文字为：更新风险等级。

五、AI辅助解读连接
从“生成AI辅助解读”画一条箭头连接到“生成就医建议/就诊准备报告”，箭头文字为：辅助理解风险与就医时机。

六、闭环连接
从“授权后进入模型优化闭环”画一条回环箭头指向“本地模型检测”，箭头文字为：授权标注数据用于后续模型优化。

七、视觉要求
请突出“自查 → 解释 → 档案管理 → 就医 → 标注优化 → 模型提升”的闭环。流程节点使用圆角矩形，主线箭头用绿色，分支箭头用浅蓝，回环箭头用虚线绿色。不要加入诊断、治疗、处方等字样。
```

## 图 3-2 核心数据库关系 ER 图

```text
请生成一张标题为“核心数据库关系 ER 图”的横版 ER 图。

一、实体节点
请绘制以下 10 个实体，实体框内只写实体名，不展示字段，避免拥挤：
1. APP_USER
2. TAUDIO_SCREEN_RECORD
3. TLARYNGOSCOPE_PHOTO
4. TPERSONAL_LARYNGEAL_HEALTH_RECORD
5. TSYMPTOM_LOG
6. TMODEL_OPTIMIZE_LABEL
7. TAUDIO_AI_REPORT
8. TMODEL_INTERFACE_CALL_LOG
9. TLARYNGOSCOPE_LOCAL_PREDICTION_RECORD
10. TLARYNGOSCOPE_QWEN_ANALYSIS_RECORD

二、实体布局
请把 APP_USER 放在左侧中心。
把用户直接拥有的数据表放在中间一列：
TAUDIO_SCREEN_RECORD、TLARYNGOSCOPE_PHOTO、TPERSONAL_LARYNGEAL_HEALTH_RECORD、TSYMPTOM_LOG、TMODEL_OPTIMIZE_LABEL。
把由检测或喉镜记录派生的结果表放在右侧一列：
TAUDIO_AI_REPORT、TMODEL_INTERFACE_CALL_LOG、TLARYNGOSCOPE_LOCAL_PREDICTION_RECORD、TLARYNGOSCOPE_QWEN_ANALYSIS_RECORD。

三、必须绘制的一对多关系
1. APP_USER 1 → N TAUDIO_SCREEN_RECORD，关系文字：音频自查记录
2. APP_USER 1 → N TLARYNGOSCOPE_PHOTO，关系文字：喉镜影像档案
3. APP_USER 1 → N TPERSONAL_LARYNGEAL_HEALTH_RECORD，关系文字：健康档案
4. APP_USER 1 → N TSYMPTOM_LOG，关系文字：症状日志
5. APP_USER 1 → N TMODEL_OPTIMIZE_LABEL，关系文字：确诊标注
6. TAUDIO_SCREEN_RECORD 1 → N TAUDIO_AI_REPORT，关系文字：生成AI报告
7. TAUDIO_SCREEN_RECORD 1 → N TMODEL_INTERFACE_CALL_LOG，关系文字：记录模型日志
8. TAUDIO_SCREEN_RECORD 1 → N TMODEL_OPTIMIZE_LABEL，关系文字：关联确诊标注
9. TLARYNGOSCOPE_PHOTO 1 → N TLARYNGOSCOPE_LOCAL_PREDICTION_RECORD，关系文字：本地模型预测
10. TLARYNGOSCOPE_PHOTO 1 → N TLARYNGOSCOPE_QWEN_ANALYSIS_RECORD，关系文字：Qwen图像分析

四、视觉要求
使用标准 ER 图或类 ER 图表现，一对多关系要明确，可用“1”和“N”标注在线两端。APP_USER 用浅绿色突出，业务表用浅蓝，派生结果表用浅青。线条清晰，不要交叉过多，不要添加未列出的表。
```

## 图 5-1 安装及使用流程图

```text
请生成一张标题为“安装及使用流程图”的双泳道流程图。

一、整体布局
横版 16:9，分成上下两条横向泳道：
上方泳道标题：默认安装流程
下方泳道标题：典型使用流程

二、上方泳道：默认安装流程
从左到右绘制以下节点，并用箭头依次连接：
1. 初始化数据库
   小字说明：导入 healthcontrol.sql 和增量SQL
2. 配置后端 application.yml
   小字说明：数据库、文件目录、AI Key
3. 启动 Spring Boot 后端
   小字说明：mvnw.cmd spring-boot:run
4. 启动 Vue 管理后台
   小字说明：npm install / npm run dev
5. 运行 UniApp 小程序端
   小字说明：HBuilderX 或微信开发者工具
6. 检查 Python 模型文件与依赖
   小字说明：voice_best_model.pth、detect_audio_siamese.py、repalexnet_heatmap_n100.pth

三、下方泳道：典型使用流程
从左到右绘制以下节点，并用箭头依次连接：
1. 用户登录
2. 音频自查
3. 查看结果和图谱
4. 生成AI辅助解读
5. 查看健康档案趋势
6. 上传喉镜图片并分析
7. 查看饮食/就医建议
8. 提交确诊标注与授权

四、跨泳道关系
请用一条浅色虚线从上方最后一个节点“检查 Python 模型文件与依赖”指向下方“音频自查”，表示环境准备完成后进入使用流程。

五、视觉要求
上方安装流程用浅蓝色节点，下方使用流程用浅绿色节点。箭头从左到右，节点不要交叉。整体风格正式、简洁、清晰，不要添加表格，不要添加无关步骤。
```
