# Food DB Snapshot

- GeneratedAt: 2026-03-31 14:21:47
- Source: local MySQL database HealthControl, tables tlaryngealfood and tlaryngealfoodcategory
- Note: this file is exported from the live database, not from healthcontrol.sql

## Overview

| Metric | Value |
| --- | --- |
| Active food rows | 42 |
| Friendly categories | 5 |
| Caution categories | 5 |
| Images on test-fixtures/foods | 42 |
| Images still on /static/food | 0 |

## Category Summary

| CategoryId | Type | CategoryName | Count | CategoryDesc |
| --- | --- | --- | --- | --- |
| 1001 | friendly | 润喉补水 | 5 | 补充水分、缓解咽干，适合日常护嗓与用嗓多的人群。包括温水、蜂蜜水、梨类等温和润喉食物。 |
| 1002 | friendly | 清淡易吞咽 | 5 | 质地柔软、刺激性低，适合咽喉不适期。包括粥类、蒸蛋、嫩豆腐等易消化食物。 |
| 1003 | friendly | 温补修复 | 4 | 温和滋养、帮助恢复，适合恢复期（避免过燥）。包括山药、莲藕汤、少量红枣等。 |
| 1004 | friendly | 维C/抗氧化 | 4 | 富含维生素C与抗氧化物质，帮助黏膜修复。注意选择不过酸、不过冰的品种。 |
| 1005 | friendly | 优质蛋白 | 4 | 提供优质蛋白，帮助组织修复与免疫支持。建议清淡烹饪，避免煎炸重口。 |
| 2001 | caution | 辛辣刺激 | 4 | 强刺激咽喉黏膜，易加重疼痛、咳嗽、反酸等症状。包括辣椒、芥末、麻辣调料等。 |
| 2002 | caution | 油炸烧烤 | 4 | 高油高温加工，易上火、刺激咽喉。包括炸鸡、烧烤、薯条等油炸烧烤类食物。 |
| 2003 | caution | 过烫饮食 | 4 | 高温可烫伤黏膜，延缓恢复。包括刚出锅的热汤、过烫茶水等。 |
| 2004 | caution | 酒精/咖啡因 | 4 | 可能加重干燥与反酸，影响睡眠恢复。包括白酒、浓咖啡、含酒精饮料等。 |
| 2005 | caution | 高糖甜腻 | 4 | 可能增加黏腻感、痰感，诱发不适（因人而异）。包括高糖奶茶、大量巧克力等。 |

## Image Summary

| PicType | Count |
| --- | --- |
| test-fixtures/foods | 42 |

## FriendlyLevel Summary

| FriendlyLevel | Count | Meaning |
| --- | --- | --- |
| 1 | 5 | very friendly |
| 2 | 12 | friendly |
| 3 | 5 | neutral |
| 4 | 11 | mildly irritating |
| 5 | 9 | strongly irritating |

## Details

### friendly - 润喉补水 (1001)

| Id | FoodName | FoodAlias | FriendlyLevel | TasteTag | PicType |
| --- | --- | --- | --- | --- | --- |
| 30001 | 温水 | 白开水 | 1 | 清淡,温热,补水 | test-fixtures/foods |
| 30002 | 蜂蜜温水 | 蜂蜜水 | 2 | 清淡,温热,润喉,甜 | test-fixtures/foods |
| 30003 | 雪梨（蒸） | 蒸梨 | 2 | 清淡,温热,润燥,水果 | test-fixtures/foods |
| 30004 | 银耳羹（少糖） | 银耳汤 | 2 | 清淡,温热,润喉,胶质 | test-fixtures/foods |
| 30005 | 柠檬蜂蜜水（淡） | 柠檬水 | 3 | 清淡,维C,酸甜 | test-fixtures/foods |

- [30001] 温水 | SuitPeople=讲话多、咽干人群，所有人群日常 | AvoidPeople=无特殊禁忌 | EffectHarmDesc=补充水分，减轻咽干与用嗓疲劳。是最基础、最安全的护嗓饮品。 | SuggestContent=少量多次，小口慢饮；避免一次大量灌水。建议每天保证充足水分摄入。 | PicUrl=http://localhost:7245/test-fixtures/foods/30001-warm-water.png
- [30002] 蜂蜜温水 | SuitPeople=咽干、干痒人群，轻度不适 | AvoidPeople=糖尿病/严格控糖者谨慎 | EffectHarmDesc=温和润喉，缓解干痒感。蜂蜜含有天然抗菌成分，对部分人有帮助。 | SuggestContent=蜂蜜用温水冲泡（不超过60℃），避免高温破坏营养成分；糖尿病或严格控糖人群谨慎使用。 | PicUrl=http://localhost:7245/test-fixtures/foods/30002-honey-water.png
- [30003] 雪梨（蒸） | SuitPeople=咽干、轻度刺激性咳嗽，干燥季节 | AvoidPeople=腹泻/胃寒明显者少量 | EffectHarmDesc=润燥感较好，适合干燥季节或用嗓多时。蒸制后更温和，减少生冷刺激。 | SuggestContent=建议温热食用；胃寒者更适合蒸熟后少量食用。可搭配少量冰糖（控糖者免）。 | PicUrl=http://localhost:7245/test-fixtures/foods/30003-steamed-pear.png
- [30004] 银耳羹（少糖） | SuitPeople=咽部干燥人群，日常护嗓 | AvoidPeople=控糖人群减少糖量 | EffectHarmDesc=口感温和，部分人感觉对咽部更舒适。银耳富含胶质，有助黏膜保湿。 | SuggestContent=控制糖量，避免过甜；建议温热食用，避免冰镇。可搭配少量红枣、枸杞（上火者减量）。 | PicUrl=http://localhost:7245/test-fixtures/foods/30004-silver-fungus.png
- [30005] 柠檬蜂蜜水（淡） | SuitPeople=轻度不适、日常护嗓 | AvoidPeople=胃酸过多/反酸明显者谨慎 | EffectHarmDesc=维C丰富，口感清新。注意浓度不宜过高，避免过酸刺激。 | SuggestContent=柠檬切片用温水冲泡，加入少量蜂蜜；避免过酸，胃酸过多者谨慎。 | PicUrl=http://localhost:7245/test-fixtures/foods/30005-lemon-honey.png

### friendly - 清淡易吞咽 (1002)

| Id | FoodName | FoodAlias | FriendlyLevel | TasteTag | PicType |
| --- | --- | --- | --- | --- | --- |
| 30011 | 小米粥 | 米粥 | 1 | 清淡,温热,易消化,软食 | test-fixtures/foods |
| 30012 | 鸡蛋羹 | 蒸蛋 | 1 | 清淡,温热,软食,高蛋白 | test-fixtures/foods |
| 30013 | 嫩豆腐（清炖） | 豆腐 | 2 | 清淡,软食,植物蛋白 | test-fixtures/foods |
| 30014 | 南瓜（清蒸） | 蒸南瓜 | 2 | 清淡,软食,温补 | test-fixtures/foods |
| 30015 | 白粥 | 大米粥 | 1 | 清淡,温热,易消化 | test-fixtures/foods |

- [30011] 小米粥 | SuitPeople=吞咽不适、嗓子疼、恢复期 | AvoidPeople=无特殊禁忌 | EffectHarmDesc=温和易消化，减少咽喉摩擦刺激。适合不适期作为主食。 | SuggestContent=温热食用；少油少盐。可搭配少量清淡小菜。 | PicUrl=http://localhost:7245/test-fixtures/foods/30011-millet-porridge.png
- [30012] 鸡蛋羹 | SuitPeople=不适期恢复、吞咽困难 | AvoidPeople=鸡蛋过敏者避免 | EffectHarmDesc=柔软好吞咽，补充优质蛋白。质地细腻，刺激性极低。 | SuggestContent=少盐调味；避免过烫入口。可加入少量温水或高汤增加口感。 | PicUrl=http://localhost:7245/test-fixtures/foods/30012-steamed-egg.png
- [30013] 嫩豆腐（清炖） | SuitPeople=不适期、反酸倾向、素食者 | AvoidPeople=大豆过敏者避免 | EffectHarmDesc=刺激性低，适合清淡饮食。富含植物蛋白，易消化。 | SuggestContent=清炖/煮汤为主；避免麻辣重口味。可搭配少量青菜。 | PicUrl=http://localhost:7245/test-fixtures/foods/30013-tofu.png
- [30014] 南瓜（清蒸） | SuitPeople=吞咽不适、嗓子干、恢复期 | AvoidPeople=无特殊禁忌 | EffectHarmDesc=软糯易吞咽，减少刺激。富含β-胡萝卜素，有助黏膜健康。 | SuggestContent=清蒸/水煮为主；避免油炸。可少量加盐或蜂蜜调味。 | PicUrl=http://localhost:7245/test-fixtures/foods/30014-steamed-pumpkin.png
- [30015] 白粥 | SuitPeople=急性不适期、肠胃敏感 | AvoidPeople=无特殊禁忌 | EffectHarmDesc=最基础的易消化食物，刺激性极低。适合急性期。 | SuggestContent=温热食用；可搭配少量咸菜或清淡小菜。 | PicUrl=http://localhost:7245/test-fixtures/foods/30015-rice-porridge.png

### friendly - 温补修复 (1003)

| Id | FoodName | FoodAlias | FriendlyLevel | TasteTag | PicType |
| --- | --- | --- | --- | --- | --- |
| 30021 | 山药（蒸煮） | 淮山 | 2 | 温补,清淡,软食 | test-fixtures/foods |
| 30022 | 莲藕排骨汤（清淡） | 排骨汤 | 2 | 温补,清淡,汤类,高蛋白 | test-fixtures/foods |
| 30023 | 红枣（少量） | 大枣 | 3 | 温补,甜,补血 | test-fixtures/foods |
| 30024 | 枸杞（少量） | 枸杞子 | 3 | 温补,清淡 | test-fixtures/foods |

- [30021] 山药（蒸煮） | SuitPeople=恢复期、食欲差、脾胃虚弱 | AvoidPeople=山药过敏者避免 | EffectHarmDesc=口感细腻，适合恢复期。有助脾胃功能，间接支持黏膜修复。 | SuggestContent=蒸煮为主；不过量。可搭配少量红枣或枸杞（上火者减量）。 | PicUrl=http://localhost:7245/test-fixtures/foods/30021-yam.png
- [30022] 莲藕排骨汤（清淡） | SuitPeople=恢复期、体力下降、食欲恢复 | AvoidPeople=高尿酸/脂肪控制者减少油脂 | EffectHarmDesc=温和滋养，帮助恢复体力。莲藕有助润燥，排骨提供蛋白。 | SuggestContent=少油少盐；体质燥热者减少频次。建议撇去浮油。 | PicUrl=http://localhost:7245/test-fixtures/foods/30022-lotus-soup.png
- [30023] 红枣（少量） | SuitPeople=恢复期、贫血倾向 | AvoidPeople=上火/口干明显者少量 | EffectHarmDesc=少量温补，口感温和。富含铁质与维C，有助恢复。 | SuggestContent=每天少量（3-5颗）即可；上火/口干明显时暂停。可搭配粥类或汤类。 | PicUrl=http://localhost:7245/test-fixtures/foods/30023-red-date.png
- [30024] 枸杞（少量） | SuitPeople=日常护嗓、恢复期 | AvoidPeople=上火明显者暂停 | EffectHarmDesc=少量可作日常搭配，口感温和。有助明目与免疫支持。 | SuggestContent=每次少量（10-15粒）；上火明显时暂停。可泡水或加入粥汤。 | PicUrl=http://localhost:7245/test-fixtures/foods/30024-goji-berry.png

### friendly - 维C/抗氧化 (1004)

| Id | FoodName | FoodAlias | FriendlyLevel | TasteTag | PicType |
| --- | --- | --- | --- | --- | --- |
| 30031 | 蓝莓 | 莓果 | 2 | 清淡,抗氧化,水果 | test-fixtures/foods |
| 30032 | 西兰花（清炒/水煮） | 花椰菜 | 2 | 清淡,抗氧化,蔬菜 | test-fixtures/foods |
| 30033 | 猕猴桃（熟，少量） | 奇异果 | 3 | 偏酸,维C,水果 | test-fixtures/foods |
| 30034 | 苹果（熟/蒸） | 蒸苹果 | 2 | 清淡,维C,水果 | test-fixtures/foods |

- [30031] 蓝莓 | SuitPeople=日常护嗓、轻度不适 | AvoidPeople=肠胃敏感者少量 | EffectHarmDesc=抗氧化食物之一，口感温和。富含花青素，有助黏膜健康。 | SuggestContent=常温或温热搭配；避免冰镇。可少量加入酸奶或粥类。 | PicUrl=http://localhost:7245/test-fixtures/foods/30031-blueberry.png
- [30032] 西兰花（清炒/水煮） | SuitPeople=日常饮食、恢复期 | AvoidPeople=胀气明显者少量 | EffectHarmDesc=营养密度高，建议清淡烹饪。富含维C、叶酸等。 | SuggestContent=少油少盐；避免辛辣调味。可水煮后少量调味。 | PicUrl=http://localhost:7245/test-fixtures/foods/30032-broccoli.png
- [30033] 猕猴桃（熟，少量） | SuitPeople=轻度不适、恢复期 | AvoidPeople=反酸/胃溃疡倾向者谨慎 | EffectHarmDesc=维C丰富，有助黏膜修复；过酸可能刺激部分人。 | SuggestContent=选熟果少量；反酸/胃溃疡倾向者谨慎。可搭配其他食物。 | PicUrl=http://localhost:7245/test-fixtures/foods/30033-kiwi.png
- [30034] 苹果（熟/蒸） | SuitPeople=日常护嗓、轻度不适 | AvoidPeople=无特殊禁忌 | EffectHarmDesc=温和水果，蒸制后更易消化。富含果胶与维C。 | SuggestContent=可生吃或蒸熟；胃寒者建议蒸熟后少量。 | PicUrl=http://localhost:7245/test-fixtures/foods/30034-steamed-apple.png

### friendly - 优质蛋白 (1005)

| Id | FoodName | FoodAlias | FriendlyLevel | TasteTag | PicType |
| --- | --- | --- | --- | --- | --- |
| 30041 | 鱼肉（清蒸） | 清蒸鱼 | 1 | 清淡,高蛋白,低脂 | test-fixtures/foods |
| 30042 | 鸡胸肉（清煮/清蒸） | 鸡肉 | 2 | 清淡,高蛋白,低脂 | test-fixtures/foods |
| 30043 | 酸奶（无糖/低糖） | 低糖酸奶 | 3 | 发酵,低糖,益生菌 | test-fixtures/foods |
| 30044 | 豆浆（无糖/低糖） | 豆奶 | 2 | 清淡,植物蛋白 | test-fixtures/foods |

- [30041] 鱼肉（清蒸） | SuitPeople=恢复期、日常护嗓 | AvoidPeople=海鲜过敏者避免 | EffectHarmDesc=优质蛋白，清淡烹饪刺激低。富含Omega-3，有助抗炎。 | SuggestContent=温热食用；避免辣酱与过多油。建议选择刺少的鱼类。 | PicUrl=http://localhost:7245/test-fixtures/foods/30041-steamed-fish.png
- [30042] 鸡胸肉（清煮/清蒸） | SuitPeople=恢复期、体力下降 | AvoidPeople=痛风/高尿酸人群适量 | EffectHarmDesc=补充蛋白，有助修复。低脂高蛋白，适合恢复期。 | SuggestContent=避免煎炸与重辣；切小块更易吞咽。可搭配清淡汤类。 | PicUrl=http://localhost:7245/test-fixtures/foods/30042-chicken-breast.png
- [30043] 酸奶（无糖/低糖） | SuitPeople=肠胃耐受者、日常护嗓 | AvoidPeople=乳糖不耐受者选择无乳糖版本 | EffectHarmDesc=部分人感觉更舒适；个别人会增痰感（因人而异）。富含益生菌。 | SuggestContent=选择低糖或无糖版本；若喝后痰感增多可减少或暂停。 | PicUrl=http://localhost:7245/test-fixtures/foods/30043-yogurt.png
- [30044] 豆浆（无糖/低糖） | SuitPeople=素食者、乳糖不耐受者 | AvoidPeople=大豆过敏者避免 | EffectHarmDesc=植物蛋白来源，口感温和。适合素食者或乳糖不耐受者。 | SuggestContent=选择无糖或低糖；温热饮用。避免过甜。 | PicUrl=http://localhost:7245/test-fixtures/foods/30044-soy-milk.png

### caution - 辛辣刺激 (2001)

| Id | FoodName | FoodAlias | FriendlyLevel | TasteTag | PicType |
| --- | --- | --- | --- | --- | --- |
| 40001 | 辣椒 | 辣味调料 | 5 | 辛辣,刺激 | test-fixtures/foods |
| 40002 | 麻辣火锅 | 重辣火锅 | 5 | 辛辣,油腻,高温 | test-fixtures/foods |
| 40003 | 芥末 | wasabi | 5 | 辛辣,刺激 | test-fixtures/foods |
| 40004 | 花椒 | 麻椒 | 5 | 辛辣,麻,刺激 | test-fixtures/foods |

- [40001] 辣椒 | SuitPeople=无 | AvoidPeople=咽痛/嘶哑/反酸人群避免 | EffectHarmDesc=强刺激咽喉黏膜，易加重疼痛、咳嗽、反酸等症状。辣椒素会直接刺激黏膜。 | SuggestContent=不适期尽量避免；改用葱姜少量提味。恢复后可少量尝试，观察反应。 | PicUrl=http://localhost:7245/test-fixtures/foods/40001-chili.png
- [40002] 麻辣火锅 | SuitPeople=无 | AvoidPeople=反酸、咽喉炎症期避免 | EffectHarmDesc=辛辣+高温+油脂叠加刺激，易诱发反酸与不适。多重刺激叠加，风险较高。 | SuggestContent=选择清汤锅、少辣少油；不适期暂停。恢复后也建议降低频次。 | PicUrl=http://localhost:7245/test-fixtures/foods/40002-hotpot.png
- [40003] 芥末 | SuitPeople=无 | AvoidPeople=咽喉敏感人群避免 | EffectHarmDesc=刺激性强，易引起咽喉灼热。芥末的挥发性成分会刺激黏膜。 | SuggestContent=咽喉不适时避免。恢复后可少量尝试，观察反应。 | PicUrl=http://localhost:7245/test-fixtures/foods/40003-wasabi.png
- [40004] 花椒 | SuitPeople=无 | AvoidPeople=咽喉敏感人群避免 | EffectHarmDesc=麻味刺激，可能加重咽喉不适。与辣椒搭配时刺激更强。 | SuggestContent=不适期避免；恢复后可少量尝试。 | PicUrl=http://localhost:7245/test-fixtures/foods/40004-peppercorn.png

### caution - 油炸烧烤 (2002)

| Id | FoodName | FoodAlias | FriendlyLevel | TasteTag | PicType |
| --- | --- | --- | --- | --- | --- |
| 40011 | 炸鸡 | 油炸鸡 | 5 | 油炸,重口,高脂 | test-fixtures/foods |
| 40012 | 烧烤（重口） | 烤串 | 5 | 烧烤,烟熏,高温 | test-fixtures/foods |
| 40013 | 薯条 | 油炸土豆 | 4 | 油炸,高盐 | test-fixtures/foods |
| 40014 | 油条 | 油炸面食 | 4 | 油炸,高脂 | test-fixtures/foods |

- [40011] 炸鸡 | SuitPeople=无 | AvoidPeople=咽痛/痰多/反酸人群避免 | EffectHarmDesc=高油重口，可能加重咽痛与痰感。油炸过程产生有害物质，刺激咽喉。 | SuggestContent=不适期避免；改清蒸/水煮蛋白来源。恢复后也建议降低频次。 | PicUrl=http://localhost:7245/test-fixtures/foods/40011-fried-chicken.png
- [40012] 烧烤（重口） | SuitPeople=无 | AvoidPeople=炎症期、口干上火人群避免 | EffectHarmDesc=烟熏与高温加工刺激，易上火。烧烤产生的烟雾与高温会刺激咽喉。 | SuggestContent=不适期避免；恢复后也建议降低频次。选择清淡烧烤或减少调味。 | PicUrl=http://localhost:7245/test-fixtures/foods/40012-bbq.png
- [40013] 薯条 | SuitPeople=无 | AvoidPeople=反酸、咽干人群减少 | EffectHarmDesc=高油高盐，可能刺激咽喉与反酸。油炸过程增加热量与有害物质。 | SuggestContent=改蒸土豆/烤土豆（少油）。不适期避免。 | PicUrl=http://localhost:7245/test-fixtures/foods/40013-fries.png
- [40014] 油条 | SuitPeople=无 | AvoidPeople=反酸、咽干人群减少 | EffectHarmDesc=高油高温，可能刺激咽喉。油炸面食较难消化，可能加重不适。 | SuggestContent=不适期避免；恢复后可少量尝试。 | PicUrl=http://localhost:7245/test-fixtures/foods/40014-fried-dough.png

### caution - 过烫饮食 (2003)

| Id | FoodName | FoodAlias | FriendlyLevel | TasteTag | PicType |
| --- | --- | --- | --- | --- | --- |
| 40021 | 刚出锅热汤 | 滚烫汤水 | 5 | 高温,刺激 | test-fixtures/foods |
| 40022 | 过烫茶水 | 热茶 | 4 | 高温 | test-fixtures/foods |
| 40023 | 刚出锅热粥 | 滚烫粥 | 4 | 高温,刺激 | test-fixtures/foods |
| 40024 | 过烫火锅/汤锅 | 滚烫锅物 | 5 | 高温,刺激 | test-fixtures/foods |

- [40021] 刚出锅热汤 | SuitPeople=无 | AvoidPeople=所有咽喉不适人群避免 | EffectHarmDesc=高温烫伤黏膜，延缓恢复。过烫食物会直接损伤咽喉黏膜。 | SuggestContent=放温再喝；入口不烫为宜（建议温度在40-50℃）。 | PicUrl=http://localhost:7245/test-fixtures/foods/40021-hot-soup.png
- [40022] 过烫茶水 | SuitPeople=无 | AvoidPeople=咽喉炎症期避免 | EffectHarmDesc=温度过高会刺激咽喉。过烫茶水可能烫伤黏膜，影响恢复。 | SuggestContent=改为温茶/温水；避免烫口。建议温度在50-60℃。 | PicUrl=http://localhost:7245/test-fixtures/foods/40022-hot-tea.png
- [40023] 刚出锅热粥 | SuitPeople=无 | AvoidPeople=所有咽喉不适人群避免过烫 | EffectHarmDesc=高温可能烫伤咽喉黏膜，导致疼痛加重并延缓恢复。粥类看似温和，但温度过高同样会造成损伤。 | SuggestContent=盛出后静置降温再吃；入口不烫为宜。建议慢慢小口进食。 | PicUrl=http://localhost:7245/test-fixtures/foods/40023-hot-porridge.png
- [40024] 过烫火锅/汤锅 | SuitPeople=无 | AvoidPeople=咽痛/吞咽困难人群避免 | EffectHarmDesc=食物温度过高会直接刺激并烫伤黏膜，尤其在炎症期更明显。 | SuggestContent=务必放温再入口；优先选择温热、清淡的食物。 | PicUrl=http://localhost:7245/test-fixtures/foods/40024-hot-pot-hot.png

### caution - 酒精/咖啡因 (2004)

| Id | FoodName | FoodAlias | FriendlyLevel | TasteTag | PicType |
| --- | --- | --- | --- | --- | --- |
| 40031 | 白酒 | 烈酒 | 5 | 酒精,刺激 | test-fixtures/foods |
| 40032 | 咖啡（浓） | 浓咖啡 | 4 | 咖啡因,偏酸 | test-fixtures/foods |
| 40033 | 啤酒 | 酒精饮料 | 4 | 酒精,气泡 | test-fixtures/foods |
| 40034 | 能量饮料 | 功能饮料 | 4 | 咖啡因,高糖 | test-fixtures/foods |

- [40031] 白酒 | SuitPeople=无 | AvoidPeople=咽痛/反酸/睡眠差人群避免 | EffectHarmDesc=酒精刺激黏膜，可能加重干燥、炎症反应，并诱发反酸。 | SuggestContent=不适期避免；恢复后也建议控制频次与量。 | PicUrl=http://localhost:7245/test-fixtures/foods/40031-baijiu.png
- [40032] 咖啡（浓） | SuitPeople=无 | AvoidPeople=反酸明显者减少 | EffectHarmDesc=咖啡因可能加重反酸与干燥感（因人而异），并影响睡眠。 | SuggestContent=不适期减少；可换温水/淡茶。避免空腹饮用。 | PicUrl=http://localhost:7245/test-fixtures/foods/40032-coffee.png
- [40033] 啤酒 | SuitPeople=无 | AvoidPeople=咽喉炎症期、睡眠差人群减少 | EffectHarmDesc=酒精与气泡可能刺激咽喉，且易影响睡眠恢复。 | SuggestContent=不适期避免；恢复后少量即可。 | PicUrl=http://localhost:7245/test-fixtures/foods/40033-beer.png
- [40034] 能量饮料 | SuitPeople=无 | AvoidPeople=反酸/控糖人群减少 | EffectHarmDesc=常含咖啡因与较高糖分，可能加重干燥、反酸与痰感。 | SuggestContent=不适期避免；日常也建议减少频次。 | PicUrl=http://localhost:7245/test-fixtures/foods/40034-energy-drink.png

### caution - 高糖甜腻 (2005)

| Id | FoodName | FoodAlias | FriendlyLevel | TasteTag | PicType |
| --- | --- | --- | --- | --- | --- |
| 40041 | 奶茶（高糖） | 甜饮 | 4 | 高糖,甜腻 | test-fixtures/foods |
| 40042 | 巧克力（多） | 巧克力 | 4 | 甜腻,高能量 | test-fixtures/foods |
| 40043 | 蛋糕（奶油多） | 甜点 | 4 | 高糖,高脂,甜腻 | test-fixtures/foods |
| 40044 | 冰淇淋（甜） | 冷甜品 | 4 | 高糖,偏冷,甜腻 | test-fixtures/foods |

- [40041] 奶茶（高糖） | SuitPeople=无 | AvoidPeople=痰多/控糖人群减少 | EffectHarmDesc=高糖甜腻可能增加黏腻感与痰感，部分人会明显不适。 | SuggestContent=减少频次；改无糖/少糖，或温水。 | PicUrl=http://localhost:7245/test-fixtures/foods/40041-milk-tea.png
- [40042] 巧克力（多） | SuitPeople=无 | AvoidPeople=反酸明显者减少 | EffectHarmDesc=部分人会增加反酸或黏腻感，并带来口干/刺激感。 | SuggestContent=少量即可；反酸明显者减少。 | PicUrl=http://localhost:7245/test-fixtures/foods/40042-chocolate.png
- [40043] 蛋糕（奶油多） | SuitPeople=无 | AvoidPeople=反酸/控糖人群减少 | EffectHarmDesc=高糖高脂甜点可能增加反酸、黏腻与痰感。 | SuggestContent=不适期减少；选择低糖少油版本。 | PicUrl=http://localhost:7245/test-fixtures/foods/40043-cake.png
- [40044] 冰淇淋（甜） | SuitPeople=无 | AvoidPeople=咽喉敏感/反酸人群减少 | EffectHarmDesc=高糖+偏冷可能刺激咽喉，且增加黏腻感；冷刺激会让部分人更不适。 | SuggestContent=不适期避免；恢复后也建议少量，避免冰冷入口。 | PicUrl=http://localhost:7245/test-fixtures/foods/40044-ice-cream.png

