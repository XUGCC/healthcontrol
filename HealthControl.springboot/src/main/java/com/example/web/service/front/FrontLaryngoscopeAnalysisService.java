package com.example.web.service.front;

import com.example.web.dto.front.LaryngoscopeAnalysisDtos.LocalPredictInput;
import com.example.web.dto.front.LaryngoscopeAnalysisDtos.LocalPredictionOutput;
import com.example.web.dto.front.LaryngoscopeAnalysisDtos.QwenAnalysisOutput;
import com.example.web.dto.front.LaryngoscopeAnalysisDtos.QwenAnalyzeInput;
import com.example.web.tools.dto.IdInput;

public interface FrontLaryngoscopeAnalysisService {
    LocalPredictionOutput localPredict(LocalPredictInput input);

    LocalPredictionOutput getLocalPrediction(IdInput input);

    QwenAnalysisOutput qwenAnalyze(QwenAnalyzeInput input);

    QwenAnalysisOutput getQwenAnalysis(IdInput input);
}
