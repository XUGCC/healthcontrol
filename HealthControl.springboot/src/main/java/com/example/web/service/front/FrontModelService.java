package com.example.web.service.front;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.web.dto.front.FrontModelDtos.FrontModelCallLogItem;
import com.example.web.dto.front.FrontModelDtos.FrontModelCallLogSummaryInput;
import com.example.web.dto.front.FrontModelDtos.FrontModelCallLogSummaryOutput;
import com.example.web.entity.TAudioScreenRecord;
import com.example.web.entity.TModelInterfaceCallLog;
import com.example.web.mapper.TAudioScreenRecordMapper;
import com.example.web.mapper.TModelInterfaceCallLogMapper;
import com.example.web.tools.BaseContext;
import com.example.web.tools.Extension;
import com.example.web.tools.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class FrontModelService {

    @Autowired
    private TModelInterfaceCallLogMapper callLogMapper;

    @Autowired
    private TAudioScreenRecordMapper audioScreenRecordMapper;

    public FrontModelCallLogSummaryOutput callLogSummary(FrontModelCallLogSummaryInput input) {
        if (input == null || input.getDetectId() == null || input.getDetectId() <= 0) {
            throw new CustomException("Invalid DetectId");
        }

        Integer detectId = input.getDetectId();
        Integer currentUserId = BaseContext.getCurrentUserId();

        TAudioScreenRecord record = audioScreenRecordMapper.selectById(detectId);
        if (record == null) {
            throw new CustomException("Detect record not found");
        }
        if (currentUserId != null && record.getUserId() != null && !record.getUserId().equals(currentUserId)) {
            throw new CustomException("No permission for this detect record");
        }

        LambdaQueryWrapper<TModelInterfaceCallLog> query = Wrappers.<TModelInterfaceCallLog>lambdaQuery()
                .eq(TModelInterfaceCallLog::getDetectId, detectId)
                .and(w -> w.isNull(TModelInterfaceCallLog::getIsDelete).or().ne(TModelInterfaceCallLog::getIsDelete, true))
                .orderByAsc(TModelInterfaceCallLog::getCreationTime);

        List<TModelInterfaceCallLog> logs = callLogMapper.selectList(query);

        FrontModelCallLogSummaryOutput res = new FrontModelCallLogSummaryOutput();
        if (logs == null || logs.isEmpty()) {
            res.setStatusLevel("NONE");
            res.setTotalCalls(0);
            res.setSuccessCalls(0);
            res.setFailCalls(0);
            res.setAvgCost(0);
            res.setItems(new ArrayList<>());
            return res;
        }

        int total = logs.size();
        int success = 0;
        int fail = 0;
        long costSum = 0;
        int costCount = 0;
        List<FrontModelCallLogItem> items = new ArrayList<>();

        for (TModelInterfaceCallLog log : logs) {
            if (Boolean.TRUE.equals(log.getCallStatus())) {
                success++;
            } else {
                fail++;
            }
            if (log.getCallCost() != null) {
                costSum += log.getCallCost();
                costCount++;
            }

            FrontModelCallLogItem item = new FrontModelCallLogItem();
            Integer callLinkInt = toInt(log.getCallLink());
            item.setCallLink(callLinkInt);

            String callLinkName = buildCallLinkName(callLinkInt);
            String serviceName = log.getServiceName();
            if (serviceName != null && serviceName.contains("result_persist")) {
                callLinkName = "Result persist/report";
            }
            item.setCallLinkName(callLinkName);

            item.setModelInterfaceType(toInt(log.getModelInterfaceType()));
            item.setSourceType(resolveSourceType(serviceName));
            item.setServiceName(serviceName);
            item.setModelVersion(log.getModelVersion());
            item.setCallTime(resolveCallTimeText(log));
            item.setCallStatus(log.getCallStatus());
            item.setCallCost(log.getCallCost());
            item.setResultSummary(log.getResultSummary());
            item.setFailErrorCode(log.getFailErrorCode());
            items.add(item);
        }

        int avgCost = costCount > 0 ? (int) (costSum / costCount) : 0;

        res.setTotalCalls(total);
        res.setSuccessCalls(success);
        res.setFailCalls(fail);
        res.setAvgCost(avgCost);
        res.setItems(items);

        if (success == 0) {
            res.setStatusLevel("ERROR");
        } else if (fail > 0 || avgCost > 5000) {
            res.setStatusLevel("WARNING");
        } else {
            res.setStatusLevel("NORMAL");
        }

        return res;
    }

    private Integer toInt(Boolean value) {
        if (value == null) {
            return null;
        }
        return Boolean.TRUE.equals(value) ? 1 : 0;
    }

    private String buildCallLinkName(Integer callLink) {
        if (callLink == null) {
            return "Diagnostic flow";
        }
        switch (callLink) {
            case 0:
                return "Preprocess";
            case 1:
                return "Feature extract / inference";
            default:
                return "Diagnostic flow-" + callLink;
        }
    }

    private String resolveSourceType(String serviceName) {
        if (serviceName == null) {
            return "LOCAL_MODEL";
        }
        return serviceName.toLowerCase(Locale.ROOT).contains("qwen") ? "QWEN_API" : "LOCAL_MODEL";
    }

    private String resolveCallTimeText(TModelInterfaceCallLog log) {
        LocalDateTime callTime = log.getUpdateTime() != null ? log.getUpdateTime() : log.getCreationTime();
        return Extension.LocalDateTimeConvertString(callTime, "yyyy-MM-dd HH:mm:ss");
    }
}

