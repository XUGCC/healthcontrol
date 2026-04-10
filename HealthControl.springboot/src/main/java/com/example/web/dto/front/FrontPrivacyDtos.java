package com.example.web.dto.front;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 前台隐私设置与数据权利模块 DTO
 */
public class FrontPrivacyDtos {

    @Data
    public static class FrontPrivacyExportRequestInput {
        /**
         * 数据类型（数组）
         * 示例：["AudioRecord","HealthRecord",...]
         */
        @JsonProperty("DataType")
        private List<String> DataType;

        /**
         * 导出格式：JSON|Excel|PDF
         */
        @JsonProperty("ExportFormat")
        private String ExportFormat;

        /**
         * 申请说明（可选）
         */
        @JsonProperty("RequestDesc")
        private String RequestDesc;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class FrontPrivacyExportRequestListInput extends FrontPagedInput {
        @JsonProperty("Status")
        private String Status;
    }

    @Data
    public static class FrontPrivacyDeleteRequestInput {
        /**
         * 数据类型（数组）
         */
        @JsonProperty("DataType")
        private List<String> DataType;

        /**
         * 删除原因
         */
        @JsonProperty("DeleteReason")
        private String DeleteReason;

        /**
         * 删除原因说明（可选）
         */
        @JsonProperty("DeleteReasonDesc")
        private String DeleteReasonDesc;

        /**
         * 删除账户等敏感操作可能需要密码（可选）
         */
        @JsonProperty("Password")
        private String Password;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class FrontPrivacyDeleteRequestListInput extends FrontPagedInput {
        @JsonProperty("Status")
        private String Status;
    }
}

