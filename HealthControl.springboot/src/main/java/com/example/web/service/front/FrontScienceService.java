package com.example.web.service.front;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.web.dto.AppUserDto;
import com.example.web.dto.THealthScienceDto;
import com.example.web.dto.TScienceCommentDto;
import com.example.web.dto.front.*;
import com.example.web.entity.*;
import com.example.web.mapper.*;
import com.example.web.tools.BaseContext;
import com.example.web.tools.Extension;
import com.example.web.tools.dto.PagedResult;
import com.example.web.tools.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FrontScienceService {

    @Autowired
    private THealthScienceCategoryMapper categoryMapper;
    @Autowired
    private THealthScienceMapper scienceMapper;
    @Autowired
    private TScienceReadLogMapper readLogMapper;
    @Autowired
    private TScienceLikeMapper likeMapper;
    @Autowired
    private TScienceCollectMapper collectMapper;
    @Autowired
    private TScienceCommentMapper commentMapper;
    @Autowired
    private TScienceCommentLikeMapper commentLikeMapper;
    @Autowired
    private AppUserMapper appUserMapper;

    public List<FrontScienceCategoryNodeDto> categoryList(FrontScienceCategoryListInput input) {
        boolean includeEmpty = input == null || input.getIncludeEmpty() == null || input.getIncludeEmpty();

        List<THealthScienceCategory> all = categoryMapper.selectList(
                Wrappers.<THealthScienceCategory>lambdaQuery()
                        .eq(THealthScienceCategory::getShowStatus, 1)
                        .and(w -> w.isNull(THealthScienceCategory::getIsDelete).or().ne(THealthScienceCategory::getIsDelete, 1))
                        .orderByAsc(THealthScienceCategory::getSortNum)
                        .orderByAsc(THealthScienceCategory::getId)
        );

        if (!includeEmpty) {
            // 只保留至少存在 1 篇可见文章的分类
            List<THealthScience> visibles = scienceMapper.selectList(
                    Wrappers.<THealthScience>lambdaQuery()
                            .select(THealthScience::getCategoryId)
                            .eq(THealthScience::getAuditStatus, 2)
                            .eq(THealthScience::getShowStatus, 1)
                            .and(w -> w.isNull(THealthScience::getIsDelete).or().ne(THealthScience::getIsDelete, 1))
            );
            Set<Integer> usedCategoryIds = visibles.stream()
                    .map(THealthScience::getCategoryId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            all = all.stream().filter(c -> usedCategoryIds.contains(c.getId())).collect(Collectors.toList());
        }

        // build tree
        Map<Integer, FrontScienceCategoryNodeDto> nodeMap = new HashMap<>();
        for (THealthScienceCategory c : all) {
            FrontScienceCategoryNodeDto n = new FrontScienceCategoryNodeDto();
            n.setId(c.getId());
            n.setCategoryName(c.getCategoryName());
            n.setCategoryDesc(c.getCategoryDesc());
            n.setSortNum(c.getSortNum());
            n.setShowStatus(c.getShowStatus());
            n.setIsDelete(c.getIsDelete());
            n.setParentId(c.getParentId());
            n.setLevel(c.getLevel());
            n.setPath(c.getPath());
            n.setIconUrl(c.getIconUrl());
            nodeMap.put(n.getId(), n);
        }

        List<FrontScienceCategoryNodeDto> roots = new ArrayList<>();
        for (FrontScienceCategoryNodeDto n : nodeMap.values()) {
            Integer pid = n.getParentId();
            if (pid == null || pid == 0 || !nodeMap.containsKey(pid)) {
                roots.add(n);
            } else {
                nodeMap.get(pid).getChildren().add(n);
            }
        }

        // sort children by SortNum
        sortTree(roots);
        roots.sort(Comparator.comparing(FrontScienceCategoryNodeDto::getSortNum, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(FrontScienceCategoryNodeDto::getId, Comparator.nullsLast(Integer::compareTo)));
        return roots;
    }

    private void sortTree(List<FrontScienceCategoryNodeDto> nodes) {
        if (nodes == null) return;
        for (FrontScienceCategoryNodeDto n : nodes) {
            if (n.getChildren() != null && !n.getChildren().isEmpty()) {
                n.getChildren().sort(Comparator.comparing(FrontScienceCategoryNodeDto::getSortNum, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(FrontScienceCategoryNodeDto::getId, Comparator.nullsLast(Integer::compareTo)));
                sortTree(n.getChildren());
            }
        }
    }

    public PagedResult<FrontScienceListItemDto> list(FrontScienceListInput input) {
        if (input == null) input = new FrontScienceListInput();
        long page = input.getPage() == null || input.getPage() <= 0 ? 1 : input.getPage();
        long limit = input.getLimit() == null || input.getLimit() <= 0 ? 10 : input.getLimit();

        LambdaQueryWrapper<THealthScience> qw = Wrappers.<THealthScience>lambdaQuery()
                .eq(THealthScience::getAuditStatus, 2)
                .eq(THealthScience::getShowStatus, 1)
                .and(w -> w.isNull(THealthScience::getIsDelete).or().ne(THealthScience::getIsDelete, 1));

        if (input.getKnowledgeType() != null) {
            qw.eq(THealthScience::getKnowledgeType, input.getKnowledgeType());
        }

        if (Extension.isNotNullOrEmpty(input.getKeyword())) {
            String kw = input.getKeyword().trim();
            if (!kw.isEmpty()) {
                qw.and(w -> w.like(THealthScience::getTitle, kw).or().like(THealthScience::getSummary, kw));
            }
        }

        if (input.getCategoryId() != null && input.getCategoryId() > 0) {
            Set<Integer> ids = getCategorySubtreeIds(input.getCategoryId());
            if (!ids.isEmpty()) {
                qw.in(THealthScience::getCategoryId, ids);
            } else {
                qw.eq(THealthScience::getCategoryId, input.getCategoryId());
            }
        }

        String sort = input.getSort() == null ? "RECOMMEND" : input.getSort().trim().toUpperCase(Locale.ROOT);
        if ("LATEST".equals(sort)) {
            qw.orderByDesc(THealthScience::getPublishTime).orderByDesc(THealthScience::getCreationTime);
        } else if ("HOT_7D".equals(sort) || "HOT_30D".equals(sort)) {
            // MVP：未做分时热度统计时，先用 ReadCount 兜底
            qw.orderByDesc(THealthScience::getReadCount).orderByDesc(THealthScience::getPublishTime);
        } else {
            // RECOMMEND
            qw.orderByDesc(THealthScience::getIsTop)
                    .orderByDesc(THealthScience::getRecommendWeight)
                    .orderByDesc(THealthScience::getPublishTime)
                    .orderByDesc(THealthScience::getCreationTime);
        }

        Page<THealthScience> mpPage = new Page<>(page, limit);
        IPage<THealthScience> result = scienceMapper.selectPage(mpPage, qw);
        long total = scienceMapper.selectCount(qw);

        // 批量补齐作者名称，避免 N+1 查询
        List<THealthScience> records = result.getRecords();
        List<FrontScienceListItemDto> items = new ArrayList<>();
        if (records != null && !records.isEmpty()) {
            Set<Integer> creatorIds = records.stream()
                    .map(THealthScience::getCreatorId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            Map<Integer, AppUser> userMap = new HashMap<>();
            if (!creatorIds.isEmpty()) {
                List<AppUser> users = appUserMapper.selectBatchIds(creatorIds);
                for (AppUser u : users) {
                    userMap.put(u.getId(), u);
                }
            }

            for (THealthScience s : records) {
                FrontScienceListItemDto dto = new FrontScienceListItemDto();
                dto.setId(s.getId());
                dto.setTitle(s.getTitle());
                dto.setCoverUrl(s.getCoverUrl());
                dto.setSummary(buildSummary(s.getSummary(), s.getScienceContent()));
                dto.setCategoryId(s.getCategoryId());
                dto.setKnowledgeType(s.getKnowledgeType());
                dto.setPublishTime(s.getPublishTime());
                dto.setReadCount(nvl(s.getReadCount()));
                dto.setLikeCount(nvl(s.getLikeCount()));
                dto.setCollectCount(nvl(s.getCollectCount()));
                dto.setCommentCount(nvl(s.getCommentCount()));
                dto.setIsTop(nvl(s.getIsTop()));
                dto.setRecommendWeight(nvl(s.getRecommendWeight()));

                AppUser author = userMap.get(s.getCreatorId());
                if (author != null) {
                    String name = author.getName();
                    if (Extension.isNullOrEmpty(name)) name = author.getUserName();
                    dto.setAuthorName(name);
                }

                items.add(dto);
            }
        }

        return PagedResult.GetInstance(items, total);
    }

    private Integer nvl(Integer v) {
        return v == null ? 0 : v;
    }

    private String buildSummary(String summary, String content) {
        if (Extension.isNotNullOrEmpty(summary)) return summary;
        if (Extension.isNullOrEmpty(content)) return "";
        String plain = content
                .replaceAll("<[^>]+>", "")
                .replaceAll("&nbsp;", " ")
                .trim();
        if (plain.length() <= 120) return plain;
        return plain.substring(0, 120) + "...";
    }

    private Set<Integer> getCategorySubtreeIds(Integer categoryId) {
        List<THealthScienceCategory> all = categoryMapper.selectList(
                Wrappers.<THealthScienceCategory>lambdaQuery()
                        .and(w -> w.isNull(THealthScienceCategory::getIsDelete).or().ne(THealthScienceCategory::getIsDelete, 1))
        );
        Map<Integer, List<THealthScienceCategory>> children = new HashMap<>();
        for (THealthScienceCategory c : all) {
            Integer pid = c.getParentId();
            if (pid == null) pid = 0;
            children.computeIfAbsent(pid, k -> new ArrayList<>()).add(c);
        }

        Set<Integer> ids = new HashSet<>();
        Deque<Integer> dq = new ArrayDeque<>();
        dq.add(categoryId);
        while (!dq.isEmpty()) {
            Integer id = dq.poll();
            if (id == null || ids.contains(id)) continue;
            ids.add(id);
            List<THealthScienceCategory> ch = children.getOrDefault(id, Collections.emptyList());
            for (THealthScienceCategory c : ch) dq.add(c.getId());
        }
        return ids;
    }

    public FrontScienceDetailDto detail(FrontScienceDetailInput input) throws Exception {
        if (input == null || input.getScienceId() == null || input.getScienceId() <= 0) {
            throw new CustomException("ScienceId不能为空");
        }
        THealthScience s = scienceMapper.selectOne(
                Wrappers.<THealthScience>lambdaQuery()
                        .eq(THealthScience::getId, input.getScienceId())
                        .eq(THealthScience::getAuditStatus, 2)
                        .eq(THealthScience::getShowStatus, 1)
                        .and(w -> w.isNull(THealthScience::getIsDelete).or().ne(THealthScience::getIsDelete, 1))
                        .last("LIMIT 1")
        );
        if (s == null) throw new CustomException("文章不存在或不可见");

        THealthScienceDto scienceDto = s.MapToDto();
        scienceDto.setSummary(buildSummary(scienceDto.getSummary(), scienceDto.getScienceContent()));

        Integer userId = getCurrentUserId();

        FrontScienceUserStateDto state = new FrontScienceUserStateDto();
        if (userId != null && userId > 0) {
            boolean liked = likeMapper.selectCount(
                    Wrappers.<TScienceLike>lambdaQuery()
                            .eq(TScienceLike::getUserId, userId)
                            .eq(TScienceLike::getScienceId, s.getId())
                            .and(w -> w.isNull(TScienceLike::getIsDelete).or().eq(TScienceLike::getIsDelete, false))
            ) > 0;
            boolean collected = collectMapper.selectCount(
                    Wrappers.<TScienceCollect>lambdaQuery()
                            .eq(TScienceCollect::getUserId, userId)
                            .eq(TScienceCollect::getScienceId, s.getId())
                            .and(w -> w.isNull(TScienceCollect::getIsDelete).or().eq(TScienceCollect::getIsDelete, false))
            ) > 0;
            state.setIsLiked(liked);
            state.setIsCollected(collected);
        } else {
            state.setIsLiked(false);
            state.setIsCollected(false);
        }

        FrontScienceDetailDto dto = new FrontScienceDetailDto();
        dto.setScience(scienceDto);
        dto.setUserState(state);
        return dto;
    }

    @Transactional
    public FrontScienceReadResultDto read(FrontScienceReadInput input) {
        if (input == null || input.getScienceId() == null || input.getScienceId() <= 0) {
            throw new CustomException("ScienceId不能为空");
        }

        Integer userId = getCurrentUserId();
        String clientKey = input.getClientKey();
        if ((userId == null || userId <= 0) && Extension.isNullOrEmpty(clientKey)) {
            throw new CustomException("未登录时ClientKey不能为空");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowStart = floorTo30Min(now);

        boolean deduped = false;
        try {
            TScienceReadLog log = new TScienceReadLog();
            log.setUserId(userId != null && userId > 0 ? userId : null);
            log.setClientKey(userId != null && userId > 0 ? null : clientKey);
            log.setScienceId(input.getScienceId());
            log.setWindowStartTime(windowStart);
            log.setReadScene(input.getReadScene());
            log.setReadDurationSec(input.getReadDurationSec());
            log.setIsDelete(0);
            readLogMapper.insert(log);
        } catch (DuplicateKeyException ex) {
            deduped = true;
        }

        if (!deduped) {
            UpdateWrapper<THealthScience> uw = new UpdateWrapper<>();
            uw.eq("Id", input.getScienceId());
            uw.setSql("ReadCount = IFNULL(ReadCount,0) + 1");
            scienceMapper.update(null, uw);
        }

        THealthScience s = scienceMapper.selectById(input.getScienceId());
        FrontScienceReadResultDto res = new FrontScienceReadResultDto();
        res.setDeduped(deduped);
        res.setReadCount(s == null ? 0 : nvl(s.getReadCount()));
        return res;
    }

    private LocalDateTime floorTo30Min(LocalDateTime t) {
        LocalDateTime truncated = t.truncatedTo(ChronoUnit.HOURS);
        int minute = t.getMinute();
        int bucket = (minute / 30) * 30;
        return truncated.plusMinutes(bucket);
    }

    @Transactional
    public FrontScienceToggleResultDto likeToggle(FrontScienceToggleInput input) {
        Integer userId = getCurrentUserId();
        if (userId == null || userId <= 0) throw new CustomException("请先登录");
        if (input == null || input.getScienceId() == null || input.getScienceId() <= 0) throw new CustomException("ScienceId不能为空");

        TScienceLike exist = likeMapper.selectOne(
                Wrappers.<TScienceLike>lambdaQuery()
                        .eq(TScienceLike::getUserId, userId)
                        .eq(TScienceLike::getScienceId, input.getScienceId())
                        .last("LIMIT 1")
        );

        boolean current = exist != null && (exist.getIsDelete() == null || !exist.getIsDelete());
        Boolean target = parseTargetState(input.getAction(), current, "LIKE", "UNLIKE");
        boolean changed = target != current;

        if (exist == null) {
            if (Boolean.FALSE.equals(target)) {
                return toggleResult(false, getScienceLikeCount(input.getScienceId()));
            }
            TScienceLike nl = new TScienceLike();
            nl.setUserId(userId);
            nl.setScienceId(input.getScienceId());
            nl.setLikeTime(LocalDateTime.now());
            nl.setCancelLikeTime(null);
            nl.setIsDelete(false);
            try {
                likeMapper.insert(nl);
            } catch (DuplicateKeyException ex) {
                // 并发场景下可能已经被插入：改为更新为“已点赞”
                TScienceLike re = likeMapper.selectOne(
                        Wrappers.<TScienceLike>lambdaQuery()
                                .eq(TScienceLike::getUserId, userId)
                                .eq(TScienceLike::getScienceId, input.getScienceId())
                                .last("LIMIT 1")
                );
                if (re != null) {
                    re.setIsDelete(false);
                    re.setLikeTime(LocalDateTime.now());
                    likeMapper.updateById(re);
                }
            }
        } else {
            if (Boolean.TRUE.equals(target)) {
                exist.setIsDelete(false);
                exist.setLikeTime(LocalDateTime.now());
            } else {
                exist.setIsDelete(true);
                exist.setCancelLikeTime(LocalDateTime.now());
            }
            likeMapper.updateById(exist);
        }

        if (changed) {
            UpdateWrapper<THealthScience> uw = new UpdateWrapper<>();
            uw.eq("Id", input.getScienceId());
            uw.setSql("LikeCount = GREATEST(IFNULL(LikeCount,0) " + (target ? "+ 1" : "- 1") + ", 0)");
            scienceMapper.update(null, uw);
        }

        return toggleResult(target, getScienceLikeCount(input.getScienceId()));
    }

    @Transactional
    public FrontScienceToggleResultDto collectToggle(FrontScienceToggleInput input) {
        Integer userId = getCurrentUserId();
        if (userId == null || userId <= 0) throw new CustomException("请先登录");
        if (input == null || input.getScienceId() == null || input.getScienceId() <= 0) throw new CustomException("ScienceId不能为空");

        TScienceCollect exist = collectMapper.selectOne(
                Wrappers.<TScienceCollect>lambdaQuery()
                        .eq(TScienceCollect::getUserId, userId)
                        .eq(TScienceCollect::getScienceId, input.getScienceId())
                        .last("LIMIT 1")
        );

        boolean current = exist != null && (exist.getIsDelete() == null || !exist.getIsDelete());
        Boolean target = parseTargetState(input.getAction(), current, "COLLECT", "UNCOLLECT");
        boolean changed = target != current;

        if (exist == null) {
            if (Boolean.FALSE.equals(target)) {
                return toggleResult(false, getScienceCollectCount(input.getScienceId()));
            }
            TScienceCollect nc = new TScienceCollect();
            nc.setUserId(userId);
            nc.setScienceId(input.getScienceId());
            nc.setCollectTime(LocalDateTime.now());
            nc.setCancelCollectTime(null);
            nc.setIsDelete(false);
            try {
                collectMapper.insert(nc);
            } catch (DuplicateKeyException ex) {
                TScienceCollect re = collectMapper.selectOne(
                        Wrappers.<TScienceCollect>lambdaQuery()
                                .eq(TScienceCollect::getUserId, userId)
                                .eq(TScienceCollect::getScienceId, input.getScienceId())
                                .last("LIMIT 1")
                );
                if (re != null) {
                    re.setIsDelete(false);
                    re.setCollectTime(LocalDateTime.now());
                    collectMapper.updateById(re);
                }
            }
        } else {
            if (Boolean.TRUE.equals(target)) {
                exist.setIsDelete(false);
                exist.setCollectTime(LocalDateTime.now());
            } else {
                exist.setIsDelete(true);
                exist.setCancelCollectTime(LocalDateTime.now());
            }
            collectMapper.updateById(exist);
        }

        if (changed) {
            UpdateWrapper<THealthScience> uw = new UpdateWrapper<>();
            uw.eq("Id", input.getScienceId());
            uw.setSql("CollectCount = GREATEST(IFNULL(CollectCount,0) " + (target ? "+ 1" : "- 1") + ", 0)");
            scienceMapper.update(null, uw);
        }

        return toggleResult(target, getScienceCollectCount(input.getScienceId()));
    }

    @Transactional
    public FrontScienceToggleResultDto commentLikeToggle(FrontScienceCommentLikeToggleInput input) {
        Integer userId = getCurrentUserId();
        if (userId == null || userId <= 0) throw new CustomException("请先登录");
        if (input == null || input.getCommentId() == null || input.getCommentId() <= 0)
            throw new CustomException("CommentId不能为空");

        TScienceCommentLike exist = commentLikeMapper.selectOne(
                Wrappers.<TScienceCommentLike>lambdaQuery()
                        .eq(TScienceCommentLike::getUserId, userId)
                        .eq(TScienceCommentLike::getCommentId, input.getCommentId())
                        .last("LIMIT 1")
        );

        boolean current = exist != null && (exist.getIsDelete() == null || !exist.getIsDelete());
        Boolean target = parseTargetState(input.getAction(), current, "LIKE", "UNLIKE");
        boolean changed = target != current;

        if (exist == null) {
            if (Boolean.FALSE.equals(target)) {
                return toggleResult(false, getCommentLikeCount(input.getCommentId()));
            }
            TScienceCommentLike nl = new TScienceCommentLike();
            nl.setUserId(userId);
            nl.setCommentId(input.getCommentId());
            nl.setLikeTime(LocalDateTime.now());
            nl.setCancelLikeTime(null);
            nl.setIsDelete(false);
            try {
                commentLikeMapper.insert(nl);
            } catch (DuplicateKeyException ex) {
                // 并发下可能已经有记录，兜底更新为点赞状态
                TScienceCommentLike re = commentLikeMapper.selectOne(
                        Wrappers.<TScienceCommentLike>lambdaQuery()
                                .eq(TScienceCommentLike::getUserId, userId)
                                .eq(TScienceCommentLike::getCommentId, input.getCommentId())
                                .last("LIMIT 1")
                );
                if (re != null) {
                    re.setIsDelete(false);
                    re.setLikeTime(LocalDateTime.now());
                    commentLikeMapper.updateById(re);
                }
            }
        } else {
            if (Boolean.TRUE.equals(target)) {
                exist.setIsDelete(false);
                exist.setLikeTime(LocalDateTime.now());
            } else {
                exist.setIsDelete(true);
                exist.setCancelLikeTime(LocalDateTime.now());
            }
            commentLikeMapper.updateById(exist);
        }

        return toggleResult(target, getCommentLikeCount(input.getCommentId()));
    }

    private FrontScienceToggleResultDto toggleResult(Boolean state, Integer count) {
        FrontScienceToggleResultDto res = new FrontScienceToggleResultDto();
        res.setState(state);
        res.setCount(count);
        return res;
    }

    private Boolean parseTargetState(String action, boolean current, String on, String off) {
        if (action == null || action.trim().isEmpty()) return !current;
        String a = action.trim().toUpperCase(Locale.ROOT);
        if (on.equals(a)) return true;
        if (off.equals(a)) return false;
        return !current;
    }

    private Integer getScienceLikeCount(Integer scienceId) {
        THealthScience s = scienceMapper.selectById(scienceId);
        return s == null ? 0 : nvl(s.getLikeCount());
    }

    private Integer getScienceCollectCount(Integer scienceId) {
        THealthScience s = scienceMapper.selectById(scienceId);
        return s == null ? 0 : nvl(s.getCollectCount());
    }

    private Integer getCommentLikeCount(Integer commentId) {
        Long cnt = commentLikeMapper.selectCount(
                Wrappers.<TScienceCommentLike>lambdaQuery()
                        .eq(TScienceCommentLike::getCommentId, commentId)
                        .and(w -> w.isNull(TScienceCommentLike::getIsDelete).or().eq(TScienceCommentLike::getIsDelete, false))
        );
        return cnt == null ? 0 : cnt.intValue();
    }

    @Transactional
    public FrontScienceCommentCreateResultDto commentCreate(FrontScienceCommentCreateInput input) {
        Integer userId = getCurrentUserId();
        if (userId == null || userId <= 0) throw new CustomException("请先登录");
        if (input == null || input.getScienceId() == null || input.getScienceId() <= 0) throw new CustomException("ScienceId不能为空");
        if (Extension.isNullOrEmpty(input.getCommentContent()) || input.getCommentContent().trim().isEmpty()) {
            throw new CustomException("评论内容不能为空");
        }
        String content = input.getCommentContent().trim();
        if (content.length() > 200) throw new CustomException("评论内容不能超过200字");

        // 确认文章可评论（仅可见文章）
        THealthScience s = scienceMapper.selectOne(
                Wrappers.<THealthScience>lambdaQuery()
                        .eq(THealthScience::getId, input.getScienceId())
                        .eq(THealthScience::getAuditStatus, 2)
                        .eq(THealthScience::getShowStatus, 1)
                        .and(w -> w.isNull(THealthScience::getIsDelete).or().ne(THealthScience::getIsDelete, 1))
                        .last("LIMIT 1")
        );
        if (s == null) throw new CustomException("文章不存在或不可评论");

        Integer parentId = input.getParentCommentId();
        Integer rootId = input.getRootCommentId();
        Integer replyToUserId = input.getReplyToUserId();

        if (parentId != null && parentId > 0) {
            TScienceComment parent = commentMapper.selectById(parentId);
            if (parent == null || !Objects.equals(parent.getScienceId(), input.getScienceId())) {
                throw new CustomException("父评论不存在");
            }
            if (rootId == null || rootId <= 0) {
                if (parent.getRootCommentId() != null && parent.getRootCommentId() > 0) rootId = parent.getRootCommentId();
                else rootId = parent.getId();
            }
        } else {
            // 顶级评论：ParentCommentId 应为 NULL，避免触发自关联外键约束错误
            parentId = null;
            rootId = null; // 主楼：插入后回填为自身Id
            replyToUserId = null;
        }

        // 评论改为“免审核”，统一直接通过
        int auditStatus = 1;

        TScienceComment c = new TScienceComment();
        c.setUserId(userId);
        c.setScienceId(input.getScienceId());
        c.setParentCommentId(parentId);
        c.setRootCommentId(rootId);
        c.setReplyToUserId(replyToUserId);
        c.setCommentContent(content);
        c.setReplyContent(null);
        c.setAuditStatus(auditStatus);
        c.setUpdateTime(LocalDateTime.now());
        c.setIsDelete(0);
        commentMapper.insert(c);

        if (rootId == null) {
            UpdateWrapper<TScienceComment> uw = new UpdateWrapper<>();
            uw.eq("Id", c.getId());
            uw.set("RootCommentId", c.getId());
            commentMapper.update(null, uw);
        }

        // 新评论直接计入文章评论数
        UpdateWrapper<THealthScience> uw2 = new UpdateWrapper<>();
        uw2.eq("Id", input.getScienceId());
        uw2.setSql("CommentCount = IFNULL(CommentCount,0) + 1");
        scienceMapper.update(null, uw2);

        FrontScienceCommentCreateResultDto res = new FrontScienceCommentCreateResultDto();
        res.setCommentId(c.getId());
        res.setAuditStatus(auditStatus);
        res.setMsgTip("评论已发布");
        return res;
    }

    public PagedResult<TScienceCommentDto> commentList(FrontScienceCommentListInput input) {
        if (input == null || input.getScienceId() == null || input.getScienceId() <= 0) {
            throw new CustomException("ScienceId不能为空");
        }
        long page = input.getPage() == null || input.getPage() <= 0 ? 1 : input.getPage();
        long limit = input.getLimit() == null || input.getLimit() <= 0 ? 10 : input.getLimit();

        Integer userId = getCurrentUserId();
        Integer parentId = input.getParentCommentId();
        long previewLimit = input.getPreviewLimit() == null || input.getPreviewLimit() <= 0 ? 2L : input.getPreviewLimit();

        LambdaQueryWrapper<TScienceComment> qw = Wrappers.<TScienceComment>lambdaQuery()
                .eq(TScienceComment::getScienceId, input.getScienceId())
                .and(w -> w.isNull(TScienceComment::getIsDelete).or().ne(TScienceComment::getIsDelete, 1));

        if (parentId == null || parentId <= 0) {
            // 主楼
            qw.and(w -> w.isNull(TScienceComment::getParentCommentId).or().eq(TScienceComment::getParentCommentId, 0));
            qw.orderByDesc(TScienceComment::getCreationTime);
        } else {
            qw.eq(TScienceComment::getParentCommentId, parentId);
            qw.orderByAsc(TScienceComment::getCreationTime);
        }

        if (userId != null && userId > 0) {
            qw.and(w -> w.eq(TScienceComment::getAuditStatus, 1).or().eq(TScienceComment::getUserId, userId));
        } else {
            qw.eq(TScienceComment::getAuditStatus, 1);
        }

        Page<TScienceComment> mpPage = new Page<>(page, limit);
        IPage<TScienceComment> result = commentMapper.selectPage(mpPage, qw);
        long total = commentMapper.selectCount(qw);

        List<TScienceCommentDto> dtos = Extension.copyBeanList(result.getRecords(), TScienceCommentDto.class);
        fillUsers(dtos);
        fillCommentLikes(dtos, userId);

        // 主楼：补齐 ReplyCount + 子回复预览 Children（默认 2 条）
        if (parentId == null || parentId <= 0) {
            for (TScienceCommentDto main : dtos) {
                if (main.getId() == null) continue;
                // ReplyCount：只统计“对他人可见”的通过回复（不包含主楼本身）
                long rcLong = commentMapper.selectCount(
                        Wrappers.<TScienceComment>lambdaQuery()
                                .eq(TScienceComment::getScienceId, input.getScienceId())
                                .eq(TScienceComment::getRootCommentId, main.getId())
                                .isNotNull(TScienceComment::getParentCommentId)
                                .ne(TScienceComment::getParentCommentId, 0)
                                .and(w -> w.isNull(TScienceComment::getIsDelete).or().ne(TScienceComment::getIsDelete, 1))
                                .eq(TScienceComment::getAuditStatus, 1)
                );
                main.setReplyCount((int) Math.min(Integer.MAX_VALUE, rcLong));

                // Children preview
                List<TScienceComment> replies = commentMapper.selectList(
                        Wrappers.<TScienceComment>lambdaQuery()
                                .eq(TScienceComment::getScienceId, input.getScienceId())
                                .eq(TScienceComment::getParentCommentId, main.getId())
                                .and(w -> w.isNull(TScienceComment::getIsDelete).or().ne(TScienceComment::getIsDelete, 1))
                                .and(w -> {
                                    if (userId != null && userId > 0) {
                                        w.eq(TScienceComment::getAuditStatus, 1).or().eq(TScienceComment::getUserId, userId);
                                    } else {
                                        w.eq(TScienceComment::getAuditStatus, 1);
                                    }
                                })
                                .orderByAsc(TScienceComment::getCreationTime)
                                .last("LIMIT " + previewLimit)
                );
                List<TScienceCommentDto> childDtos = Extension.copyBeanList(replies, TScienceCommentDto.class);
                fillUsers(childDtos);
                fillCommentLikes(childDtos, userId);
                main.setChildren(childDtos);
            }
        }

        return PagedResult.GetInstance(dtos, total);
    }

    private void fillUsers(List<TScienceCommentDto> items) {
        if (items == null || items.isEmpty()) return;
        Set<Integer> userIds = items.stream().map(TScienceCommentDto::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (userIds.isEmpty()) return;
        List<AppUser> users = appUserMapper.selectBatchIds(userIds);
        Map<Integer, AppUserDto> map = new HashMap<>();
        for (AppUser u : users) {
            try {
                map.put(u.getId(), u.MapToDto());
            } catch (Exception ignore) {
            }
        }
        for (TScienceCommentDto c : items) {
            c.setUserDto(map.getOrDefault(c.getUserId(), new AppUserDto()));
        }
    }

    /**
     * 为评论列表补充点赞数量与当前用户是否已点赞
     */
    private void fillCommentLikes(List<TScienceCommentDto> items, Integer currentUserId) {
        if (items == null || items.isEmpty()) return;
        Set<Integer> ids = items.stream()
                .map(TScienceCommentDto::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) return;

        List<TScienceCommentLike> likes = commentLikeMapper.selectList(
                Wrappers.<TScienceCommentLike>lambdaQuery()
                        .in(TScienceCommentLike::getCommentId, ids)
                        .and(w -> w.isNull(TScienceCommentLike::getIsDelete).or().eq(TScienceCommentLike::getIsDelete, false))
        );
        Map<Integer, Long> countMap = likes.stream()
                .collect(Collectors.groupingBy(TScienceCommentLike::getCommentId, Collectors.counting()));

        Set<Integer> likedIds = new HashSet<>();
        if (currentUserId != null && currentUserId > 0) {
            likedIds = likes.stream()
                    .filter(l -> Objects.equals(l.getUserId(), currentUserId))
                    .map(TScienceCommentLike::getCommentId)
                    .collect(Collectors.toSet());
        }

        for (TScienceCommentDto c : items) {
            Integer id = c.getId();
            if (id == null) continue;
            c.setLikeCount(countMap.getOrDefault(id, 0L).intValue());
            c.setIsLiked(currentUserId != null && currentUserId > 0 && likedIds.contains(id));
        }
    }

    public PagedResult<FrontScienceListItemDto> myPublishList(FrontScienceMyPublishListInput input) {
        Integer userId = getCurrentUserId();
        if (userId == null || userId <= 0) throw new CustomException("请先登录");

        if (input == null) input = new FrontScienceMyPublishListInput();
        long page = input.getPage() == null || input.getPage() <= 0 ? 1 : input.getPage();
        long limit = input.getLimit() == null || input.getLimit() <= 0 ? 10 : input.getLimit();

        LambdaQueryWrapper<THealthScience> qw = Wrappers.<THealthScience>lambdaQuery()
                .eq(THealthScience::getCreatorId, userId)
                .and(w -> w.isNull(THealthScience::getIsDelete).or().ne(THealthScience::getIsDelete, 1))
                .orderByDesc(THealthScience::getCreationTime);

        if (input.getAuditStatus() != null) {
            qw.eq(THealthScience::getAuditStatus, input.getAuditStatus());
        }

        Page<THealthScience> mpPage = new Page<>(page, limit);
        IPage<THealthScience> result = scienceMapper.selectPage(mpPage, qw);
        long total = scienceMapper.selectCount(qw);

        List<FrontScienceListItemDto> items = new ArrayList<>();
        for (THealthScience s : result.getRecords()) {
            FrontScienceListItemDto dto = new FrontScienceListItemDto();
            dto.setId(s.getId());
            dto.setTitle(s.getTitle());
            dto.setCoverUrl(s.getCoverUrl());
            dto.setSummary(buildSummary(s.getSummary(), s.getScienceContent()));
            dto.setCategoryId(s.getCategoryId());
            dto.setKnowledgeType(s.getKnowledgeType());
            dto.setPublishTime(s.getPublishTime());
            dto.setReadCount(nvl(s.getReadCount()));
            dto.setLikeCount(nvl(s.getLikeCount()));
            dto.setCollectCount(nvl(s.getCollectCount()));
            dto.setCommentCount(nvl(s.getCommentCount()));
            dto.setIsTop(nvl(s.getIsTop()));
            dto.setRecommendWeight(nvl(s.getRecommendWeight()));
            dto.setAuditStatus(s.getAuditStatus());
            dto.setRejectReason(s.getRejectReason());
            items.add(dto);
        }

        return PagedResult.GetInstance(items, total);
    }

    public PagedResult<FrontScienceListItemDto> myCollectList(FrontScienceMyCollectListInput input) {
        Integer userId = getCurrentUserId();
        if (userId == null || userId <= 0) throw new CustomException("请先登录");

        long page = input == null || input.getPage() == null || input.getPage() <= 0 ? 1 : input.getPage();
        long limit = input == null || input.getLimit() == null || input.getLimit() <= 0 ? 10 : input.getLimit();

        LambdaQueryWrapper<TScienceCollect> qw = Wrappers.<TScienceCollect>lambdaQuery()
                .eq(TScienceCollect::getUserId, userId)
                .and(w -> w.isNull(TScienceCollect::getIsDelete).or().eq(TScienceCollect::getIsDelete, false))
                .orderByDesc(TScienceCollect::getCollectTime)
                .orderByDesc(TScienceCollect::getCreationTime);

        Page<TScienceCollect> mpPage = new Page<>(page, limit);
        IPage<TScienceCollect> result = collectMapper.selectPage(mpPage, qw);
        long total = collectMapper.selectCount(qw);

        List<Integer> scienceIds = result.getRecords().stream()
                .map(TScienceCollect::getScienceId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (scienceIds.isEmpty()) return PagedResult.GetEmptyInstance();

        List<THealthScience> sciences = scienceMapper.selectList(
                Wrappers.<THealthScience>lambdaQuery()
                        .in(THealthScience::getId, scienceIds)
                        .eq(THealthScience::getAuditStatus, 2)
                        .eq(THealthScience::getShowStatus, 1)
                        .and(w -> w.isNull(THealthScience::getIsDelete).or().ne(THealthScience::getIsDelete, 1))
        );
        Map<Integer, THealthScience> map = new HashMap<>();
        for (THealthScience s : sciences) map.put(s.getId(), s);

        List<FrontScienceListItemDto> items = new ArrayList<>();
        for (Integer sid : scienceIds) {
            THealthScience s = map.get(sid);
            if (s == null) continue;
            FrontScienceListItemDto dto = new FrontScienceListItemDto();
            dto.setId(s.getId());
            dto.setTitle(s.getTitle());
            dto.setCoverUrl(s.getCoverUrl());
            dto.setSummary(buildSummary(s.getSummary(), s.getScienceContent()));
            dto.setCategoryId(s.getCategoryId());
            dto.setKnowledgeType(s.getKnowledgeType());
            dto.setPublishTime(s.getPublishTime());
            dto.setReadCount(nvl(s.getReadCount()));
            dto.setLikeCount(nvl(s.getLikeCount()));
            dto.setCollectCount(nvl(s.getCollectCount()));
            dto.setCommentCount(nvl(s.getCommentCount()));
            dto.setIsTop(nvl(s.getIsTop()));
            dto.setRecommendWeight(nvl(s.getRecommendWeight()));
            items.add(dto);
        }

        return PagedResult.GetInstance(items, total);
    }

    @Transactional
    public THealthScienceDto createOrEdit(FrontScienceCreateOrEditInput input) throws Exception {
        Integer userId = getCurrentUserId();
        if (userId == null || userId <= 0) throw new CustomException("请先登录");
        if (input == null) throw new CustomException("参数不能为空");

        if (Extension.isNullOrEmpty(input.getTitle()) || input.getTitle().trim().isEmpty()) throw new CustomException("标题不能为空");
        if (input.getTitle().trim().length() > 40) throw new CustomException("标题不能超过40字");
        if (Extension.isNullOrEmpty(input.getCoverUrl())) throw new CustomException("封面不能为空");
        if (input.getCategoryId() == null || input.getCategoryId() <= 0) throw new CustomException("分类不能为空");
        if (input.getKnowledgeType() == null) throw new CustomException("知识类型不能为空");
        if (Extension.isNullOrEmpty(input.getScienceContent()) || input.getScienceContent().trim().isEmpty()) throw new CustomException("正文不能为空");

        String submitType = input.getSubmitType() == null ? "SUBMIT" : input.getSubmitType().trim().toUpperCase(Locale.ROOT);
        int auditStatus = "DRAFT".equals(submitType) ? 0 : 1;

        THealthScience entity;
        if (input.getId() != null && input.getId() > 0) {
            entity = scienceMapper.selectById(input.getId());
            if (entity == null) throw new CustomException("文章不存在");
            if (!Objects.equals(entity.getCreatorId(), userId)) throw new CustomException("无权限编辑该文章");
        } else {
            entity = new THealthScience();
            entity.setCreatorId(userId);
            entity.setReadCount(0);
            entity.setLikeCount(0);
            entity.setCollectCount(0);
            entity.setCommentCount(0);
            entity.setIsTop(0);
            entity.setRecommendWeight(0);
            entity.setShowStatus(1);
            entity.setIsDelete(0);
        }

        entity.setUpdateTime(LocalDateTime.now());
        entity.setCategoryId(input.getCategoryId());
        entity.setTitle(input.getTitle().trim());
        entity.setCoverUrl(input.getCoverUrl());
        entity.setScienceContent(input.getScienceContent());
        entity.setSummary(input.getSummary());
        entity.setKnowledgeType(input.getKnowledgeType());

        // 重新提交审核时，状态回到待审；草稿则为草稿
        entity.setAuditStatus(auditStatus);

        if (entity.getId() == null || entity.getId() == 0) {
            scienceMapper.insert(entity);
        } else {
            scienceMapper.updateById(entity);
        }

        return entity.MapToDto();
    }

    public THealthScienceDto myDetail(FrontScienceDetailInput input) throws Exception {
        Integer userId = getCurrentUserId();
        if (userId == null || userId <= 0) throw new CustomException("请先登录");
        if (input == null || input.getScienceId() == null || input.getScienceId() <= 0) throw new CustomException("ScienceId不能为空");

        THealthScience s = scienceMapper.selectOne(
                Wrappers.<THealthScience>lambdaQuery()
                        .eq(THealthScience::getId, input.getScienceId())
                        .eq(THealthScience::getCreatorId, userId)
                        .and(w -> w.isNull(THealthScience::getIsDelete).or().ne(THealthScience::getIsDelete, 1))
                        .last("LIMIT 1")
        );
        if (s == null) throw new CustomException("文章不存在或无权限查看");
        return s.MapToDto();
    }

    private Integer getCurrentUserId() {
        if (BaseContext.getCurrentUserDto() == null || BaseContext.getCurrentUserDto().getUserId() == null) return 0;
        return BaseContext.getCurrentUserDto().getUserId();
    }
}

