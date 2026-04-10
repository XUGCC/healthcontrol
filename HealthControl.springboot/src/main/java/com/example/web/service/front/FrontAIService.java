package com.example.web.service.front;

import com.example.web.dto.front.FrontAIDtos.FrontAIAnalyzeAudioRecordInput;
import com.example.web.dto.front.FrontAIDtos.FrontAIChatInput;
import com.example.web.dto.front.FrontAIDtos.FrontAIChatOutput;
import com.example.web.dto.front.FrontAIDtos.FrontAIReportOutput;
import org.springframework.web.multipart.MultipartFile;

/**
 * 前台 AI 服务（小程序端）
 */
public interface FrontAIService {

    /**
     * 基于检测记录生成/获取 AI 解读报告（使用 Mel/MFCC 图谱）
     */
    FrontAIReportOutput analyzeAudioRecord(FrontAIAnalyzeAudioRecordInput input);

    /**
     * 上传图谱（MFCC/MEL PNG）并解读（不依赖历史记录）
     */
    FrontAIReportOutput analyzeSpectrumUpload(String spectrumType, MultipartFile file);

    /**
     * 医疗咨询对话（健康科普/就医建议）
     */
    FrontAIChatOutput chat(FrontAIChatInput input);
}

