package com.example.web.dto.front;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 饮食管理与推荐模块前台 DTO 汇总
 * 为了简化文件数量，这里放在一个文件中多个内部类，均为 public。
 */
public class FrontDietDtos {

    @Data
    public static class FrontDietCategoryListInput {
        /**
         * 分类类型：0=友好，1=忌口；为空表示全部
         */
        @JsonProperty("CategoryType")
        private Integer CategoryType;
    }

    @Data
    public static class FrontDietCategoryListItem {
        @JsonProperty("CategoryId")
        private Integer CategoryId;

        @JsonProperty("CategoryName")
        private String CategoryName;

        @JsonProperty("CategoryType")
        private Integer CategoryType;

        @JsonProperty("CategoryDesc")
        private String CategoryDesc;

        @JsonProperty("SortNum")
        private Integer SortNum;
    }

    @Data
    public static class FrontDietHomeOutput {
        @JsonProperty("TodayStatus")
        private FrontDietTodayStatus TodayStatus;

        @JsonProperty("RecommendFriendlyCategories")
        private List<FrontDietRecommendCategory> RecommendFriendlyCategories;

        @JsonProperty("RecommendAvoidCategories")
        private List<FrontDietRecommendCategory> RecommendAvoidCategories;

        @JsonProperty("TodayRecordStat")
        private FrontDietRecordStat TodayRecordStat;
    }

    @Data
    public static class FrontDietTodayStatus {
        @JsonProperty("RiskLevel")
        private Integer RiskLevel;

        @JsonProperty("RiskLevelText")
        private String RiskLevelText;

        @JsonProperty("PrimaryScreenResultText")
        private String PrimaryScreenResultText;

        @JsonProperty("PrimaryScreenConfidence")
        private Double PrimaryScreenConfidence;

        @JsonProperty("MainSymptom")
        private String MainSymptom;

        @JsonProperty("SummaryText")
        private String SummaryText;

        @JsonProperty("IsGeneralRecommend")
        private Boolean IsGeneralRecommend;
    }

    @Data
    public static class FrontDietRecommendCategory {
        @JsonProperty("CategoryId")
        private Integer CategoryId;

        @JsonProperty("CategoryName")
        private String CategoryName;

        @JsonProperty("CategoryType")
        private Integer CategoryType;

        @JsonProperty("CategoryDesc")
        private String CategoryDesc;

        @JsonProperty("Foods")
        private List<FrontDietFoodItem> Foods;
    }

    @Data
    public static class FrontDietFoodItem {
        @JsonProperty("FoodId")
        private Integer FoodId;

        @JsonProperty("FoodName")
        private String FoodName;

        @JsonProperty("FoodAlias")
        private String FoodAlias;

        @JsonProperty("PicUrl")
        private String PicUrl;

        @JsonProperty("Summary")
        private String Summary;
    }

    @Data
    public static class FrontDietRecordStat {
        @JsonProperty("TodayTotal")
        private Integer TodayTotal;
    }

    // 说明：
    // - 饮食“规则驱动推荐”功能已下线，不再返回 MatchedRules/规则命中解释。
    // - 前台仍保留“喉部食物库 + 饮食记录”能力，饮食建议页展示通用建议（基于食物分类的友好/忌口）。

    @Data
    public static class FrontDietFoodListInput {
        @JsonProperty("Page")
        private Long Page;

        @JsonProperty("Limit")
        private Long Limit;

        @JsonProperty("CategoryType")
        private Integer CategoryType;

        @JsonProperty("CategoryId")
        private Integer CategoryId;

        @JsonProperty("Keyword")
        private String Keyword;
    }

    @Data
    public static class FrontDietFoodListItem {
        @JsonProperty("FoodId")
        private Integer FoodId;

        @JsonProperty("FoodName")
        private String FoodName;

        @JsonProperty("FoodAlias")
        private String FoodAlias;

        @JsonProperty("CategoryId")
        private Integer CategoryId;

        @JsonProperty("CategoryName")
        private String CategoryName;

        @JsonProperty("CategoryType")
        private Integer CategoryType;

        @JsonProperty("PicUrl")
        private String PicUrl;

        @JsonProperty("Summary")
        private String Summary;
    }

    @Data
    public static class FrontDietFoodDetailInput {
        @JsonProperty("FoodId")
        private Integer FoodId;
    }

    @Data
    public static class FrontDietFoodDetail {
        @JsonProperty("FoodId")
        private Integer FoodId;

        @JsonProperty("FoodName")
        private String FoodName;

        @JsonProperty("FoodAlias")
        private String FoodAlias;

        @JsonProperty("CategoryId")
        private Integer CategoryId;

        @JsonProperty("CategoryName")
        private String CategoryName;

        @JsonProperty("CategoryType")
        private Integer CategoryType;

        @JsonProperty("PicUrl")
        private String PicUrl;

        @JsonProperty("EffectHarmDesc")
        private String EffectHarmDesc;

        @JsonProperty("SuggestContent")
        private String SuggestContent;

        @JsonProperty("RiskTip")
        private String RiskTip;
    }

    @Data
    public static class FrontDietRecordCreateOrEditInput {
        @JsonProperty("Id")
        private Integer Id;

        @JsonProperty("FoodId")
        private Integer FoodId;

        /**
         * 前端传入的摄入时间字符串，格式统一为 yyyy-MM-dd HH:mm:ss
         * 例如：2026-03-07 23:34:05
         *
         * 说明：
         * - 这里使用 String，避免 LocalDateTime 反序列化格式不一致导致的 400 错误
         * - 实际入库时在 Service 中统一解析为 LocalDateTime
         */
        @JsonProperty("IntakeTime")
        private String IntakeTime;

        @JsonProperty("IntakeFrequency")
        private Integer IntakeFrequency;

        @JsonProperty("EatFeeling")
        private String EatFeeling;
    }

    @Data
    public static class FrontDietRecordListInput {
        @JsonProperty("Page")
        private Long Page;

        @JsonProperty("Limit")
        private Long Limit;

        @JsonProperty("DateStart")
        private String DateStart;

        @JsonProperty("DateEnd")
        private String DateEnd;

        @JsonProperty("CategoryType")
        private Integer CategoryType;
    }

    @Data
    public static class FrontDietRecordItem {
        @JsonProperty("Id")
        private Integer Id;

        @JsonProperty("FoodId")
        private Integer FoodId;

        @JsonProperty("FoodName")
        private String FoodName;

        @JsonProperty("CategoryType")
        private Integer CategoryType;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @JsonProperty("IntakeTime")
        private LocalDateTime IntakeTime;

        @JsonProperty("IntakeFrequency")
        private Integer IntakeFrequency;

        @JsonProperty("EatFeeling")
        private String EatFeeling;
    }

    @Data
    public static class FrontDietRecordDeleteInput {
        @JsonProperty("Id")
        private Integer Id;
    }

    // 说明：饮食“规则命中解释”功能已下线，对应 DTO 已移除。
}

