package com.example.web.service.front;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.web.dto.front.FrontDietDtos.*;
import com.example.web.entity.*;
import com.example.web.mapper.*;
import com.example.web.tools.BaseContext;
import com.example.web.tools.Extension;
import com.example.web.tools.dto.PagedResult;
import com.example.web.tools.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FrontDietService {

    @Autowired
    private TLaryngealFoodCategoryMapper categoryMapper;

    @Autowired
    private TLaryngealFoodMapper foodMapper;

    @Autowired
    private TUserDietRecordMapper dietRecordMapper;

    @Autowired
    private TAudioScreenRecordMapper audioScreenRecordMapper;

    @Autowired
    private TPersonalLaryngealHealthRecordMapper healthRecordMapper;

    @Autowired
    private TSymptomLogMapper symptomLogMapper;

    public FrontDietHomeOutput home() {
        Integer userId = getCurrentUserId();

        FrontDietHomeOutput res = new FrontDietHomeOutput();
        FrontDietTodayStatus todayStatus = buildTodayStatus(userId);
        res.setTodayStatus(todayStatus);

        List<FrontDietRecommendCategory> friendly = new ArrayList<>();
        List<FrontDietRecommendCategory> avoid = new ArrayList<>();
        buildRecommendCategories(friendly, avoid);
        res.setRecommendFriendlyCategories(friendly);
        res.setRecommendAvoidCategories(avoid);

        FrontDietRecordStat stat = new FrontDietRecordStat();
        stat.setTodayTotal(calcTodayRecordCount(userId));
        res.setTodayRecordStat(stat);

        return res;
    }

    public List<FrontDietCategoryListItem> categoryList(FrontDietCategoryListInput input) {
        if (input == null) input = new FrontDietCategoryListInput();

        LambdaQueryWrapper<TLaryngealFoodCategory> qw = Wrappers.<TLaryngealFoodCategory>lambdaQuery()
                .eq(TLaryngealFoodCategory::getShowStatus, true)
                .and(w -> w.isNull(TLaryngealFoodCategory::getIsDelete)
                        .or().ne(TLaryngealFoodCategory::getIsDelete, true));

        if (input.getCategoryType() != null) {
            // 表字段为 Boolean：false=友好, true=忌口
            qw.eq(TLaryngealFoodCategory::getCategoryType, input.getCategoryType() == 1);
        }

        qw.orderByAsc(TLaryngealFoodCategory::getSortNum)
                .orderByAsc(TLaryngealFoodCategory::getId);

        List<TLaryngealFoodCategory> categories = categoryMapper.selectList(qw);
        if (categories == null || categories.isEmpty()) return Collections.emptyList();

        List<FrontDietCategoryListItem> res = new ArrayList<>();
        for (TLaryngealFoodCategory c : categories) {
            FrontDietCategoryListItem dto = new FrontDietCategoryListItem();
            dto.setCategoryId(c.getId());
            dto.setCategoryName(c.getCategoryName());
            dto.setCategoryDesc(c.getCategoryDesc());
            dto.setSortNum(c.getSortNum());
            if (c.getCategoryType() == null) dto.setCategoryType(null);
            else dto.setCategoryType(Boolean.TRUE.equals(c.getCategoryType()) ? 1 : 0);
            res.add(dto);
        }
        return res;
    }

    private FrontDietTodayStatus buildTodayStatus(Integer userId) {
        FrontDietTodayStatus s = new FrontDietTodayStatus();
        s.setIsGeneralRecommend(true);

        if (userId == null || userId <= 0) {
            s.setSummaryText("未登录用户将看到通用饮食建议，登录后可结合您的自查结果与档案给出更个性化的推荐。");
            s.setRiskLevel(null);
            s.setRiskLevelText("未知");
            s.setPrimaryScreenResultText("未知");
            s.setPrimaryScreenConfidence(null);
            s.setMainSymptom(null);
            return s;
        }

        // 健康档案
        TPersonalLaryngealHealthRecord record = healthRecordMapper.selectOne(
                Wrappers.<TPersonalLaryngealHealthRecord>lambdaQuery()
                        .eq(TPersonalLaryngealHealthRecord::getUserId, userId)
                        .and(w -> w.isNull(TPersonalLaryngealHealthRecord::getIsDelete)
                                .or().ne(TPersonalLaryngealHealthRecord::getIsDelete, true))
                        .orderByDesc(TPersonalLaryngealHealthRecord::getCreationTime)
                        .last("LIMIT 1")
        );

        Integer riskLevel = record == null ? null : record.getRiskAssessmentLevel();
        String riskText;
        if (riskLevel == null) riskText = "未知";
        else if (riskLevel == 0) riskText = "低风险";
        else if (riskLevel == 1) riskText = "中风险";
        else if (riskLevel == 2) riskText = "高风险";
        else riskText = "未知";

        // 最近检测
        TAudioScreenRecord audio = audioScreenRecordMapper.selectOne(
                Wrappers.<TAudioScreenRecord>lambdaQuery()
                        .eq(TAudioScreenRecord::getUserId, userId)
                        .and(w -> w.isNull(TAudioScreenRecord::getIsDelete)
                                .or().ne(TAudioScreenRecord::getIsDelete, true))
                        .orderByDesc(TAudioScreenRecord::getCreationTime)
                        .last("LIMIT 1")
        );
        String primaryResultText = "未知";
        Double primaryConf = null;
        if (audio != null) {
            Boolean r = audio.getPrimaryScreenResult();
            if (r != null) {
                if (!r) primaryResultText = "良性倾向";
                else primaryResultText = "恶性倾向";
            }
            primaryConf = audio.getPrimaryScreenConfidence();
        }

        // 最近症状
        TSymptomLog symptom = symptomLogMapper.selectOne(
                Wrappers.<TSymptomLog>lambdaQuery()
                        .eq(TSymptomLog::getUserId, userId)
                        .and(w -> w.isNull(TSymptomLog::getIsDelete)
                                .or().ne(TSymptomLog::getIsDelete, true))
                        .orderByDesc(TSymptomLog::getCreationTime)
                        .last("LIMIT 1")
        );
        String mainSymptom = symptom == null ? null : symptom.getSymptomType();

        s.setRiskLevel(riskLevel);
        s.setRiskLevelText(riskText);
        s.setPrimaryScreenResultText(primaryResultText);
        s.setPrimaryScreenConfidence(primaryConf);
        s.setMainSymptom(mainSymptom);

        // 根据是否有检测/症状，判断是否为“个性化”推荐
        boolean hasPersonal = audio != null || symptom != null || record != null;
        s.setIsGeneralRecommend(!hasPersonal);

        StringBuilder sb = new StringBuilder();
        if (!hasPersonal) {
            sb.append("当前基于通用护嗓原则为您推荐饮食建议，建议尽量多选喉部友好食物，减少明显刺激性饮食。");
        } else {
            sb.append("综合最近的自查与症状，当前喉部风险评估为：").append(riskText).append("；");
            if (Extension.isNotNullOrEmpty(mainSymptom)) {
                sb.append("主要不适症状：").append(mainSymptom).append("；");
            }
            if (!"未知".equals(primaryResultText)) {
                sb.append("音频初筛结果偏向：").append(primaryResultText).append("。");
            }
            sb.append("推荐您多选择润喉、温和类食物，减少辛辣油炸等可能加重刺激的饮食。");
        }
        s.setSummaryText(sb.toString());
        return s;
    }

    private void buildRecommendCategories(List<FrontDietRecommendCategory> friendly,
                                          List<FrontDietRecommendCategory> avoid) {
        // 饮食“规则驱动推荐”已下线：这里改为基于食物分类（友好/忌口）提供通用建议
        LambdaQueryWrapper<TLaryngealFoodCategory> catQw = Wrappers.<TLaryngealFoodCategory>lambdaQuery()
                .eq(TLaryngealFoodCategory::getShowStatus, true)
                .and(w -> w.isNull(TLaryngealFoodCategory::getIsDelete)
                        .or().ne(TLaryngealFoodCategory::getIsDelete, true))
                .orderByAsc(TLaryngealFoodCategory::getSortNum)
                .orderByAsc(TLaryngealFoodCategory::getId);

        List<TLaryngealFoodCategory> categories = categoryMapper.selectList(catQw);
        if (categories == null) categories = Collections.emptyList();

        if (categories.isEmpty()) return;

        Map<Integer, TLaryngealFoodCategory> catMap = categories.stream()
                .collect(Collectors.toMap(TLaryngealFoodCategory::getId, c -> c));
        Set<Integer> visibleCatIds = catMap.keySet();

        // 查询各分类下代表性食物（每类取 3 个）
        List<TLaryngealFood> foods = foodMapper.selectList(
                Wrappers.<TLaryngealFood>lambdaQuery()
                        .in(TLaryngealFood::getCategoryId, visibleCatIds)
                        .eq(TLaryngealFood::getShowStatus, 1)
                        .and(w -> w.isNull(TLaryngealFood::getIsDelete).or().ne(TLaryngealFood::getIsDelete, 1))
        );
        Map<Integer, List<TLaryngealFood>> foodByCat = foods.stream()
                .collect(Collectors.groupingBy(TLaryngealFood::getCategoryId));

        for (TLaryngealFoodCategory c : categories) {
            List<TLaryngealFood> list = foodByCat.getOrDefault(c.getId(), Collections.emptyList());
            if (list.isEmpty()) continue;

            List<FrontDietFoodItem> foodDtos = list.stream().limit(3).map(f -> {
                FrontDietFoodItem dto = new FrontDietFoodItem();
                dto.setFoodId(f.getId());
                dto.setFoodName(f.getFoodName());
                dto.setFoodAlias(f.getFoodAlias());
                dto.setPicUrl(f.getPicUrl());
                String summary = f.getEffectHarmDesc();
                if (summary != null && summary.length() > 40) {
                    summary = summary.substring(0, 40) + "...";
                }
                dto.setSummary(summary);
                return dto;
            }).collect(Collectors.toList());

            if (foodDtos.isEmpty()) continue;

            FrontDietRecommendCategory dto = new FrontDietRecommendCategory();
            dto.setCategoryId(c.getId());
            dto.setCategoryName(c.getCategoryName());
            dto.setCategoryDesc(c.getCategoryDesc());
            if (c.getCategoryType() == null) {
                dto.setCategoryType(null);
            } else {
                dto.setCategoryType(Boolean.TRUE.equals(c.getCategoryType()) ? 1 : 0);
            }
            dto.setFoods(foodDtos);

            Integer type = dto.getCategoryType();
            if (type != null && type == 1) {
                avoid.add(dto);
            } else {
                friendly.add(dto);
            }
        }
    }

    // riskLevelToText 已不再使用（规则驱动推荐下线后无需在此处拼装规则解释文案）

    private Integer calcTodayRecordCount(Integer userId) {
        if (userId == null || userId <= 0) return 0;
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.atTime(LocalTime.MAX);
        Long count = dietRecordMapper.selectCount(
                Wrappers.<TUserDietRecord>lambdaQuery()
                        .eq(TUserDietRecord::getUserId, userId)
                        .between(TUserDietRecord::getIntakeTime, start, end)
                        .and(w -> w.isNull(TUserDietRecord::getIsDelete).or().ne(TUserDietRecord::getIsDelete, true))
        );
        return count == null ? 0 : count.intValue();
    }

    public PagedResult<FrontDietFoodListItem> foodList(FrontDietFoodListInput input) {
        if (input == null) input = new FrontDietFoodListInput();
        long page = input.getPage() == null || input.getPage() <= 0 ? 1 : input.getPage();
        long limit = input.getLimit() == null || input.getLimit() <= 0 ? 10 : input.getLimit();

        LambdaQueryWrapper<TLaryngealFood> qw = Wrappers.<TLaryngealFood>lambdaQuery()
                .eq(TLaryngealFood::getShowStatus, 1)
                .and(w -> w.isNull(TLaryngealFood::getIsDelete).or().ne(TLaryngealFood::getIsDelete, 1));

        if (input.getCategoryId() != null && input.getCategoryId() > 0) {
            qw.eq(TLaryngealFood::getCategoryId, input.getCategoryId());
        }

        if (input.getCategoryType() != null) {
            // 通过分类类型过滤
            List<TLaryngealFoodCategory> cats = categoryMapper.selectList(
                    Wrappers.<TLaryngealFoodCategory>lambdaQuery()
                            .eq(TLaryngealFoodCategory::getShowStatus, true)
                            .and(w -> w.isNull(TLaryngealFoodCategory::getIsDelete)
                                    .or().ne(TLaryngealFoodCategory::getIsDelete, true))
                            .eq(TLaryngealFoodCategory::getCategoryType, input.getCategoryType() == 1)
            );
            if (!cats.isEmpty()) {
                Set<Integer> ids = cats.stream().map(TLaryngealFoodCategory::getId).collect(Collectors.toSet());
                qw.in(TLaryngealFood::getCategoryId, ids);
            } else {
                return PagedResult.GetEmptyInstance();
            }
        }

        if (Extension.isNotNullOrEmpty(input.getKeyword())) {
            String kw = input.getKeyword().trim();
            if (!kw.isEmpty()) {
                qw.and(w -> w.like(TLaryngealFood::getFoodName, kw)
                        .or().like(TLaryngealFood::getFoodAlias, kw));
            }
        }

        qw.orderByAsc(TLaryngealFood::getCategoryId)
                .orderByAsc(TLaryngealFood::getFoodName);

        Page<TLaryngealFood> mpPage = new Page<>(page, limit);
        Page<TLaryngealFood> result = foodMapper.selectPage(mpPage, qw);
        long total = foodMapper.selectCount(qw);

        List<TLaryngealFood> records = result.getRecords();
        if (records == null || records.isEmpty()) {
            return PagedResult.GetEmptyInstance();
        }

        Set<Integer> catIds = records.stream()
                .map(TLaryngealFood::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Integer, TLaryngealFoodCategory> catMap = new HashMap<>();
        if (!catIds.isEmpty()) {
            List<TLaryngealFoodCategory> cats = categoryMapper.selectBatchIds(catIds);
            for (TLaryngealFoodCategory c : cats) {
                catMap.put(c.getId(), c);
            }
        }

        List<FrontDietFoodListItem> items = new ArrayList<>();
        for (TLaryngealFood f : records) {
            FrontDietFoodListItem dto = new FrontDietFoodListItem();
            dto.setFoodId(f.getId());
            dto.setFoodName(f.getFoodName());
            dto.setFoodAlias(f.getFoodAlias());
            dto.setCategoryId(f.getCategoryId());
            TLaryngealFoodCategory c = catMap.get(f.getCategoryId());
            if (c != null) {
                dto.setCategoryName(c.getCategoryName());
            if (c.getCategoryType() == null) {
                dto.setCategoryType(null);
            } else {
                dto.setCategoryType(Boolean.TRUE.equals(c.getCategoryType()) ? 1 : 0);
            }
            }
            dto.setPicUrl(f.getPicUrl());
            String summary = f.getEffectHarmDesc();
            if (summary != null && summary.length() > 40) {
                summary = summary.substring(0, 40) + "...";
            }
            dto.setSummary(summary);
            items.add(dto);
        }

        return PagedResult.GetInstance(items, total);
    }

    public FrontDietFoodDetail foodDetail(FrontDietFoodDetailInput input) {
        if (input == null || input.getFoodId() == null || input.getFoodId() <= 0) {
            throw new CustomException("FoodId不能为空");
        }
        TLaryngealFood food = foodMapper.selectOne(
                Wrappers.<TLaryngealFood>lambdaQuery()
                        .eq(TLaryngealFood::getId, input.getFoodId())
                        .eq(TLaryngealFood::getShowStatus, 1)
                        .and(w -> w.isNull(TLaryngealFood::getIsDelete).or().ne(TLaryngealFood::getIsDelete, 1))
                        .last("LIMIT 1")
        );
        if (food == null) {
            throw new CustomException("食物不存在或不可见");
        }

        TLaryngealFoodCategory category = null;
        if (food.getCategoryId() != null) {
            category = categoryMapper.selectById(food.getCategoryId());
        }

        Integer userId = getCurrentUserId();
        FrontDietFoodDetail dto = new FrontDietFoodDetail();
        dto.setFoodId(food.getId());
        dto.setFoodName(food.getFoodName());
        dto.setFoodAlias(food.getFoodAlias());
        dto.setPicUrl(food.getPicUrl());
        dto.setEffectHarmDesc(food.getEffectHarmDesc());
        dto.setSuggestContent(food.getSuggestContent());
        if (category != null) {
            dto.setCategoryId(category.getId());
            dto.setCategoryName(category.getCategoryName());
            dto.setCategoryType(category.getCategoryType() == null ? null : (category.getCategoryType() ? 1 : 0));
        }

        String riskTip = "以下饮食提示仅供参考，不能替代医生诊断与处方，如有明显不适请及时就医。";
        if (userId != null && userId > 0) {
            TPersonalLaryngealHealthRecord record = healthRecordMapper.selectOne(
                    Wrappers.<TPersonalLaryngealHealthRecord>lambdaQuery()
                            .eq(TPersonalLaryngealHealthRecord::getUserId, userId)
                            .and(w -> w.isNull(TPersonalLaryngealHealthRecord::getIsDelete)
                                    .or().ne(TPersonalLaryngealHealthRecord::getIsDelete, true))
                            .orderByDesc(TPersonalLaryngealHealthRecord::getCreationTime)
                            .last("LIMIT 1")
            );
            Integer risk = record == null ? null : record.getRiskAssessmentLevel();
            if (category != null && category.getCategoryType() != null) {
                boolean avoid = category.getCategoryType();
                if (avoid) {
                    if (risk != null && risk >= 1) {
                        riskTip = "根据您当前的喉部风险状况，建议近期尽量减少此类食物的摄入，若进食后明显不适，请及时就医。";
                    } else {
                        riskTip = "此类食物可能对喉部有一定刺激，建议适量或减少摄入，注意观察自身感受。";
                    }
                } else {
                    riskTip = "此类食物整体对喉部较为友好，在不过量的前提下可以作为日常饮食的优先选择。";
                }
            }
        }
        dto.setRiskTip(riskTip);
        return dto;
    }

    public void recordCreateOrEdit(FrontDietRecordCreateOrEditInput input) {
        Integer userId = getCurrentUserId();
        if (userId == null || userId <= 0) {
            throw new CustomException("请先登录后再记录饮食");
        }
        if (input == null || input.getFoodId() == null || input.getFoodId() <= 0) {
            throw new CustomException("FoodId不能为空");
        }
        TLaryngealFood food = foodMapper.selectById(input.getFoodId());
        if (food == null) throw new CustomException("食物不存在");

        LocalDateTime intakeTime = null;
        if (Extension.isNotNullOrEmpty(input.getIntakeTime())) {
            String raw = input.getIntakeTime().trim();
            // 兼容前端可能传入的 "yyyy-MM-dd HH:mm:ss" 或 "yyyy-MM-ddTHH:mm:ss"
            raw = raw.replace("T", " ");
            try {
                intakeTime = LocalDateTime.parse(raw, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (DateTimeParseException e) {
                throw new CustomException("饮食摄入时间格式不正确，应为：yyyy-MM-dd HH:mm:ss");
            }
        }
        if (intakeTime == null) {
            intakeTime = LocalDateTime.now();
        }
        Integer freq = input.getIntakeFrequency();
        if (freq == null || freq <= 0) freq = 1;
        if (freq > 10) freq = 10;

        if (input.getId() == null || input.getId() <= 0) {
            TUserDietRecord r = new TUserDietRecord();
            r.setUserId(userId);
            r.setFoodId(input.getFoodId());
            r.setIntakeTime(intakeTime);
            r.setIntakeFrequency(freq);
            r.setEatFeeling(input.getEatFeeling());
            r.setIsDelete(false);
            dietRecordMapper.insert(r);
        } else {
            TUserDietRecord r = dietRecordMapper.selectOne(
                    Wrappers.<TUserDietRecord>lambdaQuery()
                            .eq(TUserDietRecord::getId, input.getId())
                            .eq(TUserDietRecord::getUserId, userId)
                            .and(w -> w.isNull(TUserDietRecord::getIsDelete).or().ne(TUserDietRecord::getIsDelete, true))
                            .last("LIMIT 1")
            );
            if (r == null) throw new CustomException("记录不存在或无权编辑");
            r.setIntakeTime(intakeTime);
            r.setIntakeFrequency(freq);
            r.setEatFeeling(input.getEatFeeling());
            dietRecordMapper.updateById(r);
        }
    }

    public PagedResult<FrontDietRecordItem> recordList(FrontDietRecordListInput input) {
        Integer userId = getCurrentUserId();
        if (userId == null || userId <= 0) {
            throw new CustomException("请先登录后查看饮食记录");
        }
        if (input == null) input = new FrontDietRecordListInput();
        long page = input.getPage() == null || input.getPage() <= 0 ? 1 : input.getPage();
        long limit = input.getLimit() == null || input.getLimit() <= 0 ? 10 : input.getLimit();

        LambdaQueryWrapper<TUserDietRecord> qw = Wrappers.<TUserDietRecord>lambdaQuery()
                .eq(TUserDietRecord::getUserId, userId)
                .and(w -> w.isNull(TUserDietRecord::getIsDelete).or().ne(TUserDietRecord::getIsDelete, true));

        if (Extension.isNotNullOrEmpty(input.getDateStart()) && Extension.isNotNullOrEmpty(input.getDateEnd())) {
            LocalDate start = LocalDate.parse(input.getDateStart());
            LocalDate end = LocalDate.parse(input.getDateEnd());
            qw.between(TUserDietRecord::getIntakeTime,
                    start.atStartOfDay(),
                    end.atTime(LocalTime.MAX));
        }

        // 友好/忌口筛选：0=友好（categoryType=false）, 1=忌口（categoryType=true）
        if (input.getCategoryType() != null && (input.getCategoryType() == 0 || input.getCategoryType() == 1)) {
            List<TLaryngealFoodCategory> cats = categoryMapper.selectList(
                    Wrappers.<TLaryngealFoodCategory>lambdaQuery()
                            .and(w -> w.isNull(TLaryngealFoodCategory::getIsDelete)
                                    .or().ne(TLaryngealFoodCategory::getIsDelete, true))
                            .eq(TLaryngealFoodCategory::getCategoryType, input.getCategoryType() == 1)
            );
            if (cats == null || cats.isEmpty()) return PagedResult.GetEmptyInstance();

            Set<Integer> catIds = cats.stream()
                    .map(TLaryngealFoodCategory::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (catIds.isEmpty()) return PagedResult.GetEmptyInstance();

            List<TLaryngealFood> foods = foodMapper.selectList(
                    Wrappers.<TLaryngealFood>lambdaQuery()
                            .select(TLaryngealFood::getId)
                            .in(TLaryngealFood::getCategoryId, catIds)
                            .eq(TLaryngealFood::getShowStatus, 1)
                            .and(w -> w.isNull(TLaryngealFood::getIsDelete)
                                    .or().ne(TLaryngealFood::getIsDelete, 1))
            );
            if (foods == null || foods.isEmpty()) return PagedResult.GetEmptyInstance();

            Set<Integer> foodIds = foods.stream()
                    .map(TLaryngealFood::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (foodIds.isEmpty()) return PagedResult.GetEmptyInstance();

            qw.in(TUserDietRecord::getFoodId, foodIds);
        }

        qw.orderByDesc(TUserDietRecord::getIntakeTime);

        Page<TUserDietRecord> mpPage = new Page<>(page, limit);
        Page<TUserDietRecord> result = dietRecordMapper.selectPage(mpPage, qw);
        long total = dietRecordMapper.selectCount(qw);

        List<TUserDietRecord> records = result.getRecords();
        if (records == null || records.isEmpty()) return PagedResult.GetEmptyInstance();

        Set<Integer> foodIds = records.stream()
                .map(TUserDietRecord::getFoodId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Integer, TLaryngealFood> foodMap = new HashMap<>();
        if (!foodIds.isEmpty()) {
            List<TLaryngealFood> foods = foodMapper.selectBatchIds(foodIds);
            for (TLaryngealFood f : foods) {
                foodMap.put(f.getId(), f);
            }
        }

        Set<Integer> catIds = foodMap.values().stream()
                .map(TLaryngealFood::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Integer, TLaryngealFoodCategory> catMap = new HashMap<>();
        if (!catIds.isEmpty()) {
            List<TLaryngealFoodCategory> cats = categoryMapper.selectBatchIds(catIds);
            for (TLaryngealFoodCategory c : cats) {
                catMap.put(c.getId(), c);
            }
        }

        List<FrontDietRecordItem> items = new ArrayList<>();
        for (TUserDietRecord r : records) {
            FrontDietRecordItem dto = new FrontDietRecordItem();
            dto.setId(r.getId());
            dto.setFoodId(r.getFoodId());
            dto.setIntakeTime(r.getIntakeTime());
            dto.setIntakeFrequency(r.getIntakeFrequency());
            dto.setEatFeeling(r.getEatFeeling());
            TLaryngealFood f = foodMap.get(r.getFoodId());
            if (f != null) {
                dto.setFoodName(f.getFoodName());
                TLaryngealFoodCategory c = catMap.get(f.getCategoryId());
                if (c != null && c.getCategoryType() != null) {
                    dto.setCategoryType(c.getCategoryType() ? 1 : 0);
                }
            }
            items.add(dto);
        }

        return PagedResult.GetInstance(items, total);
    }

    public void recordDelete(FrontDietRecordDeleteInput input) {
        Integer userId = getCurrentUserId();
        if (userId == null || userId <= 0) {
            throw new CustomException("请先登录后删除饮食记录");
        }
        if (input == null || input.getId() == null || input.getId() <= 0) {
            throw new CustomException("Id不能为空");
        }
        TUserDietRecord r = dietRecordMapper.selectOne(
                Wrappers.<TUserDietRecord>lambdaQuery()
                        .eq(TUserDietRecord::getId, input.getId())
                        .eq(TUserDietRecord::getUserId, userId)
                        .and(w -> w.isNull(TUserDietRecord::getIsDelete).or().ne(TUserDietRecord::getIsDelete, true))
                        .last("LIMIT 1")
        );
        if (r == null) throw new CustomException("记录不存在或无权删除");
        r.setIsDelete(true);
        dietRecordMapper.updateById(r);
    }

    private Integer getCurrentUserId() {
        if (BaseContext.getCurrentUserDto() == null || BaseContext.getCurrentUserDto().getUserId() == null) return 0;
        return BaseContext.getCurrentUserDto().getUserId();
    }
}

