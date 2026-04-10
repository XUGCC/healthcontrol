package com.example.web.service.front;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.web.dto.front.FrontLatestQuestionnaireResultDto;
import com.example.web.dto.front.FrontPagedInput;
import com.example.web.dto.front.FrontQuestionDto;
import com.example.web.dto.front.FrontQuestionOptionDto;
import com.example.web.dto.front.FrontQuestionnaireAnswerItemDto;
import com.example.web.dto.front.FrontQuestionnaireDetailDto;
import com.example.web.dto.front.FrontQuestionnaireDetailInput;
import com.example.web.dto.front.FrontQuestionnaireHistoryAnswerDto;
import com.example.web.dto.front.FrontQuestionnaireHistoryItemDto;
import com.example.web.dto.front.FrontQuestionnaireSubmitInput;
import com.example.web.dto.front.FrontQuestionnaireSubmitResultDto;
import com.example.web.entity.TPersonalLaryngealHealthRecord;
import com.example.web.entity.TRiskAssessmentQuestion;
import com.example.web.entity.TRiskAssessmentQuestionOption;
import com.example.web.entity.TRiskAssessmentQuestionnaire;
import com.example.web.entity.TUserQuestionnaireAnswer;
import com.example.web.mapper.TPersonalLaryngealHealthRecordMapper;
import com.example.web.mapper.TRiskAssessmentQuestionMapper;
import com.example.web.mapper.TRiskAssessmentQuestionOptionMapper;
import com.example.web.mapper.TRiskAssessmentQuestionnaireMapper;
import com.example.web.mapper.TUserQuestionnaireAnswerMapper;
import com.example.web.tools.BaseContext;
import com.example.web.tools.dto.PagedResult;
import com.example.web.tools.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FrontQuestionnaireService {

    @Autowired
    private TRiskAssessmentQuestionnaireMapper questionnaireMapper;
    @Autowired
    private TUserQuestionnaireAnswerMapper answerMapper;
    @Autowired
    private TPersonalLaryngealHealthRecordMapper recordMapper;
    @Autowired
    private TRiskAssessmentQuestionMapper questionMapper;
    @Autowired
    private TRiskAssessmentQuestionOptionMapper optionMapper;

    public PagedResult<TRiskAssessmentQuestionnaire> list() {
        List<TRiskAssessmentQuestionnaire> items = questionnaireMapper.selectList(
                Wrappers.<TRiskAssessmentQuestionnaire>lambdaQuery()
                        .eq(TRiskAssessmentQuestionnaire::getShowStatus, 1)
                        .and(w -> w.isNull(TRiskAssessmentQuestionnaire::getIsDelete).or().ne(TRiskAssessmentQuestionnaire::getIsDelete, 1))
                        .orderByDesc(TRiskAssessmentQuestionnaire::getCreationTime)
        );
        return PagedResult.GetInstance(items, (long) items.size());
    }

    public FrontQuestionnaireDetailDto detail(FrontQuestionnaireDetailInput input) {
        if (input == null || input.getQuestionnaireId() == null || input.getQuestionnaireId() <= 0) {
            throw new CustomException("QuestionnaireId不能为空");
        }
        TRiskAssessmentQuestionnaire q = questionnaireMapper.selectOne(
                Wrappers.<TRiskAssessmentQuestionnaire>lambdaQuery()
                        .eq(TRiskAssessmentQuestionnaire::getId, input.getQuestionnaireId())
                        .eq(TRiskAssessmentQuestionnaire::getShowStatus, 1)
                        .and(w -> w.isNull(TRiskAssessmentQuestionnaire::getIsDelete).or().ne(TRiskAssessmentQuestionnaire::getIsDelete, 1))
                        .last("LIMIT 1")
        );
        if (q == null) throw new CustomException("问卷不存在或不可用");

        // 查询题目
        List<TRiskAssessmentQuestion> questions = questionMapper.selectList(
                Wrappers.<TRiskAssessmentQuestion>lambdaQuery()
                        .eq(TRiskAssessmentQuestion::getQuestionnaireId, q.getId())
                        .and(w -> w.isNull(TRiskAssessmentQuestion::getIsDelete).or().ne(TRiskAssessmentQuestion::getIsDelete, 1))
                        .orderByAsc(TRiskAssessmentQuestion::getSortNum)
        );

        if (questions == null) {
            questions = new ArrayList<>();
        }

        // 查询所有题目的选项
        List<Integer> qIds = new ArrayList<>();
        for (TRiskAssessmentQuestion question : questions) {
            if (question.getId() != null) {
                qIds.add(question.getId());
            }
        }

        Map<Integer, List<TRiskAssessmentQuestionOption>> optionMap = new HashMap<>();
        if (!qIds.isEmpty()) {
            List<TRiskAssessmentQuestionOption> options = optionMapper.selectList(
                    Wrappers.<TRiskAssessmentQuestionOption>lambdaQuery()
                            .in(TRiskAssessmentQuestionOption::getQuestionId, qIds)
                            .and(w -> w.isNull(TRiskAssessmentQuestionOption::getIsDelete).or().ne(TRiskAssessmentQuestionOption::getIsDelete, 1))
                            .orderByAsc(TRiskAssessmentQuestionOption::getSortNum)
            );
            for (TRiskAssessmentQuestionOption op : options) {
                if (op.getQuestionId() == null) continue;
                optionMap.computeIfAbsent(op.getQuestionId(), k -> new ArrayList<>()).add(op);
            }
        }

        // 组装前端 DTO
        FrontQuestionnaireDetailDto dto = new FrontQuestionnaireDetailDto();
        dto.setId(q.getId());
        dto.setQuestionnaireTitle(q.getQuestionnaireTitle());
        dto.setQuestionnaireDesc(q.getQuestionnaireDesc());
        dto.setQuestionCount(q.getQuestionCount());
        dto.setUpdateTime(q.getUpdateTime());

        List<FrontQuestionDto> questionDtos = new ArrayList<>();
        for (TRiskAssessmentQuestion question : questions) {
            FrontQuestionDto qDto = new FrontQuestionDto();
            qDto.setId(question.getId());
            qDto.setQuestionTitle(question.getQuestionTitle());
            qDto.setQuestionDesc(question.getQuestionDesc());
            qDto.setQuestionType(question.getQuestionType());
            qDto.setIsRequired(question.getIsRequired());

            List<FrontQuestionOptionDto> optDtos = new ArrayList<>();
            List<TRiskAssessmentQuestionOption> ops = optionMap.getOrDefault(question.getId(), new ArrayList<>());
            for (TRiskAssessmentQuestionOption op : ops) {
                FrontQuestionOptionDto od = new FrontQuestionOptionDto();
                od.setId(op.getId());
                od.setOptionText(op.getOptionText());
                od.setOptionValue(op.getOptionValue());
                od.setScoreValue(op.getScoreValue());
                optDtos.add(od);
            }
            qDto.setOptions(optDtos);
            questionDtos.add(qDto);
        }
        dto.setQuestions(questionDtos);
        return dto;
    }

    @Transactional
    public FrontQuestionnaireSubmitResultDto submit(FrontQuestionnaireSubmitInput input) {
        Integer userId = getCurrentUserId();
        if (userId == null || userId <= 0) throw new CustomException("请先登录");
        if (input == null || input.getQuestionnaireId() == null || input.getQuestionnaireId() <= 0) {
            throw new CustomException("QuestionnaireId不能为空");
        }

        // 查询问卷与题目
        FrontQuestionnaireDetailInput qInput = new FrontQuestionnaireDetailInput();
        qInput.setQuestionnaireId(input.getQuestionnaireId());
        FrontQuestionnaireDetailDto detailDto = detail(qInput);

        // 计算得分
        double score = 0;
        List<FrontQuestionnaireAnswerItemDto> answers = input.getAnswers();
        if (answers == null) {
            answers = new ArrayList<>();
        }

        Map<Integer, FrontQuestionnaireAnswerItemDto> answerMap = new HashMap<>();
        for (FrontQuestionnaireAnswerItemDto a : answers) {
            if (a.getQuestionId() != null) {
                answerMap.put(a.getQuestionId(), a);
            }
        }

        List<Map<String, Object>> scoreDetails = new ArrayList<>();

        if (detailDto.getQuestions() != null) {
            for (FrontQuestionDto qDto : detailDto.getQuestions()) {
                FrontQuestionnaireAnswerItemDto a = answerMap.get(qDto.getId());
                double qScore = 0;
                if (a != null && a.getAnswerValues() != null) {
                    // 单选/多选/是非题：按选项 ScoreValue 求和
                    if (qDto.getQuestionType() != null && qDto.getQuestionType() != 4) {
                        List<String> values = a.getAnswerValues();
                        if (values != null && qDto.getOptions() != null) {
                            for (String v : values) {
                                for (FrontQuestionOptionDto od : qDto.getOptions()) {
                                    if (od.getOptionValue() != null && od.getOptionValue().equals(v)) {
                                        if (od.getScoreValue() != null) {
                                            qScore += od.getScoreValue();
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // 填空题：当前版本不计分（预留扩展）
                }
                score += qScore;

                Map<String, Object> item = new HashMap<>();
                item.put("QuestionId", qDto.getId());
                item.put("Title", qDto.getQuestionTitle());
                item.put("Score", qScore);
                scoreDetails.add(item);
            }
        }

        if (score < 0) score = 0;
        if (score > 100) score = 100;

        int level;
        if (score <= 34) level = 0;
        else if (score <= 64) level = 1;
        else level = 2;

        // 写答题记录（保留历史）
        TUserQuestionnaireAnswer a = new TUserQuestionnaireAnswer();
        a.setUserId(userId);
        a.setQuestionnaireId(input.getQuestionnaireId());
        a.setAnswerTime(LocalDateTime.now());
        a.setRiskAssessmentScore(score);
        a.setRiskLevel(level);
        a.setResultSummary(level == 0 ? "低风险：建议保持良好用嗓习惯，定期自查。" :
                level == 1 ? "中风险：建议加强护嗓管理，必要时进行自查并关注症状变化。" :
                        "高风险：建议尽快完成自查，若不适持续请及时就医。");
        try {
            a.setAnswerJson(com.fasterxml.jackson.databind.json.JsonMapper.builder().build().writeValueAsString(input));
            a.setScoreDetailJson(com.fasterxml.jackson.databind.json.JsonMapper.builder().build().writeValueAsString(scoreDetails));
        } catch (Exception ex) {
            // 序列化失败不影响主流程
            a.setAnswerJson(null);
            a.setScoreDetailJson(null);
        }
        a.setIsDelete(false);
        answerMapper.insert(a);

        // 同步档案 RiskAssessmentLevel
        TPersonalLaryngealHealthRecord record = recordMapper.selectOne(
                Wrappers.<TPersonalLaryngealHealthRecord>lambdaQuery()
                        .eq(TPersonalLaryngealHealthRecord::getUserId, userId)
                        .and(w -> w.isNull(TPersonalLaryngealHealthRecord::getIsDelete).or().eq(TPersonalLaryngealHealthRecord::getIsDelete, false))
                        .last("LIMIT 1")
        );
        if (record == null) {
            record = new TPersonalLaryngealHealthRecord();
            record.setUserId(userId);
            record.setRiskAssessmentLevel(level);
            record.setIsDelete(false);
            recordMapper.insert(record);
        } else {
            record.setRiskAssessmentLevel(level);
            recordMapper.updateById(record);
        }

        FrontQuestionnaireSubmitResultDto res = new FrontQuestionnaireSubmitResultDto();
        res.setScore(score);
        res.setRiskAssessmentLevel(level);
        res.setResultSummary(a.getResultSummary());
        return res;
    }

    public PagedResult<FrontQuestionnaireHistoryItemDto> myHistory(FrontPagedInput input) {
        Integer userId = getCurrentUserId();
        if (userId == null || userId <= 0) throw new CustomException("请先登录");

        long page = input == null || input.getPage() == null || input.getPage() <= 0 ? 1 : input.getPage();
        long limit = input == null || input.getLimit() == null || input.getLimit() <= 0 ? 10 : input.getLimit();

        var qw = Wrappers.<TUserQuestionnaireAnswer>lambdaQuery()
                .eq(TUserQuestionnaireAnswer::getUserId, userId)
                .and(w -> w.isNull(TUserQuestionnaireAnswer::getIsDelete).or().eq(TUserQuestionnaireAnswer::getIsDelete, false))
                .orderByDesc(TUserQuestionnaireAnswer::getAnswerTime)
                .orderByDesc(TUserQuestionnaireAnswer::getCreationTime);

        var mpPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<TUserQuestionnaireAnswer>(page, limit);
        var result = answerMapper.selectPage(mpPage, qw);
        long total = answerMapper.selectCount(qw);

        List<TUserQuestionnaireAnswer> records = result.getRecords();
        if (records == null) {
            records = new ArrayList<>();
        }

        // 预加载所有涉及到的问卷及题目/选项，用于把 AnswerJson 映射成可读文本
        List<Integer> questionnaireIds = new ArrayList<>();
        for (TUserQuestionnaireAnswer a : records) {
            if (a.getQuestionnaireId() != null && !questionnaireIds.contains(a.getQuestionnaireId())) {
                questionnaireIds.add(a.getQuestionnaireId());
            }
        }

        Map<Integer, TRiskAssessmentQuestionnaire> questionnaireMap = new HashMap<>();
        if (!questionnaireIds.isEmpty()) {
            List<TRiskAssessmentQuestionnaire> qs = questionnaireMapper.selectBatchIds(questionnaireIds);
            for (TRiskAssessmentQuestionnaire q : qs) {
                questionnaireMap.put(q.getId(), q);
            }
        }

        // 题目与选项的缓存：QuestionnaireId -> questions/options
        Map<Integer, List<TRiskAssessmentQuestion>> questionCache = new HashMap<>();
        Map<Integer, Map<Integer, List<TRiskAssessmentQuestionOption>>> optionCache = new HashMap<>();

        List<FrontQuestionnaireHistoryItemDto> items = new ArrayList<>();
        for (TUserQuestionnaireAnswer a : records) {
            FrontQuestionnaireHistoryItemDto item = new FrontQuestionnaireHistoryItemDto();
            item.setId(a.getId());
            item.setQuestionnaireId(a.getQuestionnaireId());
            item.setRiskAssessmentScore(a.getRiskAssessmentScore());
            item.setRiskLevel(a.getRiskLevel());
            item.setAnswerTime(a.getAnswerTime());
            item.setResultSummary(a.getResultSummary());

            TRiskAssessmentQuestionnaire q = questionnaireMap.get(a.getQuestionnaireId());
            if (q != null) {
                item.setQuestionnaireTitle(q.getQuestionnaireTitle());
            }

            // 解析 AnswerJson -> 前台提交结构
            List<FrontQuestionnaireHistoryAnswerDto> historyAnswers = new ArrayList<>();
            try {
                if (a.getAnswerJson() != null && a.getQuestionnaireId() != null) {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    FrontQuestionnaireSubmitInput submitInput = mapper.readValue(a.getAnswerJson(), FrontQuestionnaireSubmitInput.class);
                    List<FrontQuestionnaireAnswerItemDto> ans = submitInput.getAnswers();
                    if (ans == null) {
                        ans = new ArrayList<>();
                    }

                    // 准备题目与选项缓存
                    if (!questionCache.containsKey(a.getQuestionnaireId())) {
                        List<TRiskAssessmentQuestion> qs = questionMapper.selectList(
                                Wrappers.<TRiskAssessmentQuestion>lambdaQuery()
                                        .eq(TRiskAssessmentQuestion::getQuestionnaireId, a.getQuestionnaireId())
                        );
                        questionCache.put(a.getQuestionnaireId(), qs);

                        List<Integer> qIds = new ArrayList<>();
                        for (TRiskAssessmentQuestion qEntity : qs) {
                            if (qEntity.getId() != null) {
                                qIds.add(qEntity.getId());
                            }
                        }
                        Map<Integer, List<TRiskAssessmentQuestionOption>> optMap = new HashMap<>();
                        if (!qIds.isEmpty()) {
                            List<TRiskAssessmentQuestionOption> ops = optionMapper.selectList(
                                    Wrappers.<TRiskAssessmentQuestionOption>lambdaQuery()
                                            .in(TRiskAssessmentQuestionOption::getQuestionId, qIds)
                            );
                            for (TRiskAssessmentQuestionOption op : ops) {
                                if (op.getQuestionId() == null) continue;
                                optMap.computeIfAbsent(op.getQuestionId(), k -> new ArrayList<>()).add(op);
                            }
                        }
                        optionCache.put(a.getQuestionnaireId(), optMap);
                    }

                    List<TRiskAssessmentQuestion> qs = questionCache.getOrDefault(a.getQuestionnaireId(), new ArrayList<>());
                    Map<Integer, TRiskAssessmentQuestion> qMap = new HashMap<>();
                    for (TRiskAssessmentQuestion qEntity : qs) {
                        qMap.put(qEntity.getId(), qEntity);
                    }
                    Map<Integer, List<TRiskAssessmentQuestionOption>> optMap = optionCache.getOrDefault(a.getQuestionnaireId(), new HashMap<>());

                    for (FrontQuestionnaireAnswerItemDto ansItem : ans) {
                        if (ansItem.getQuestionId() == null) continue;
                        TRiskAssessmentQuestion qEntity = qMap.get(ansItem.getQuestionId());
                        if (qEntity == null) continue;

                        FrontQuestionnaireHistoryAnswerDto ha = new FrontQuestionnaireHistoryAnswerDto();
                        ha.setQuestionId(qEntity.getId());
                        ha.setQuestionTitle(qEntity.getQuestionTitle());
                        ha.setQuestionType(qEntity.getQuestionType());

                        List<String> answerTexts = new ArrayList<>();
                        List<String> values = ansItem.getAnswerValues();
                        if (values != null) {
                            if (qEntity.getQuestionType() != null && qEntity.getQuestionType() == 4) {
                                // 填空题：直接取文本
                                if (!values.isEmpty()) {
                                    answerTexts.add(values.get(0));
                                }
                            } else {
                                // 选择题：按 OptionValue 匹配 OptionText
                                List<TRiskAssessmentQuestionOption> ops = optMap.getOrDefault(qEntity.getId(), new ArrayList<>());
                                for (String v : values) {
                                    for (TRiskAssessmentQuestionOption op : ops) {
                                        if (op.getOptionValue() != null && op.getOptionValue().equals(v)) {
                                            answerTexts.add(op.getOptionText());
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        ha.setAnswerTextList(answerTexts);
                        historyAnswers.add(ha);
                    }
                }
            } catch (Exception ex) {
                // 解析失败不影响主流程，返回空答案列表
            }

            item.setAnswers(historyAnswers);
            items.add(item);
        }

        return PagedResult.GetInstance(items, total);
    }

    public FrontLatestQuestionnaireResultDto latestResult() {
        Integer userId = getCurrentUserId();
        if (userId == null || userId <= 0) throw new CustomException("请先登录");

        TUserQuestionnaireAnswer latest = answerMapper.selectOne(
                Wrappers.<TUserQuestionnaireAnswer>lambdaQuery()
                        .eq(TUserQuestionnaireAnswer::getUserId, userId)
                        .and(w -> w.isNull(TUserQuestionnaireAnswer::getIsDelete).or().eq(TUserQuestionnaireAnswer::getIsDelete, false))
                        .orderByDesc(TUserQuestionnaireAnswer::getAnswerTime)
                        .orderByDesc(TUserQuestionnaireAnswer::getCreationTime)
                        .last("LIMIT 1")
        );
        if (latest == null) return null;

        double score = latest.getRiskAssessmentScore() == null ? 0 : latest.getRiskAssessmentScore();
        int level;
        if (score <= 34) level = 0;
        else if (score <= 64) level = 1;
        else level = 2;

        FrontLatestQuestionnaireResultDto dto = new FrontLatestQuestionnaireResultDto();
        dto.setQuestionnaireId(latest.getQuestionnaireId());
        dto.setScore(score);
        dto.setRiskAssessmentLevel(level);
        dto.setAnswerTime(latest.getAnswerTime());
        return dto;
    }

    private Integer getCurrentUserId() {
        if (BaseContext.getCurrentUserDto() == null || BaseContext.getCurrentUserDto().getUserId() == null) return 0;
        return BaseContext.getCurrentUserDto().getUserId();
    }
}

