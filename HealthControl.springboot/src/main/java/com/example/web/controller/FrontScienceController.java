package com.example.web.controller;

import com.example.web.dto.THealthScienceDto;
import com.example.web.dto.TScienceCommentDto;
import com.example.web.dto.front.*;
import com.example.web.service.front.FrontScienceService;
import com.example.web.tools.dto.PagedResult;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前台科普业务接口（小程序端）
 */
@RestController
@RequestMapping("/Front/Science")
public class FrontScienceController {

    @Autowired
    private FrontScienceService frontScienceService;

    @RequestMapping(value = "/CategoryList", method = RequestMethod.POST)
    public List<FrontScienceCategoryNodeDto> CategoryList(@RequestBody(required = false) FrontScienceCategoryListInput input) {
        return frontScienceService.categoryList(input);
    }

    @RequestMapping(value = "/List", method = RequestMethod.POST)
    public PagedResult<FrontScienceListItemDto> List(@RequestBody FrontScienceListInput input) {
        return frontScienceService.list(input);
    }

    @SneakyThrows
    @RequestMapping(value = "/Detail", method = RequestMethod.POST)
    public FrontScienceDetailDto Detail(@RequestBody FrontScienceDetailInput input) {
        return frontScienceService.detail(input);
    }

    @RequestMapping(value = "/Read", method = RequestMethod.POST)
    public FrontScienceReadResultDto Read(@RequestBody FrontScienceReadInput input) {
        return frontScienceService.read(input);
    }

    @RequestMapping(value = "/LikeToggle", method = RequestMethod.POST)
    public FrontScienceToggleResultDto LikeToggle(@RequestBody FrontScienceToggleInput input) {
        return frontScienceService.likeToggle(input);
    }

    @RequestMapping(value = "/CollectToggle", method = RequestMethod.POST)
    public FrontScienceToggleResultDto CollectToggle(@RequestBody FrontScienceToggleInput input) {
        return frontScienceService.collectToggle(input);
    }

    @RequestMapping(value = "/CommentLikeToggle", method = RequestMethod.POST)
    public FrontScienceToggleResultDto CommentLikeToggle(@RequestBody FrontScienceCommentLikeToggleInput input) {
        return frontScienceService.commentLikeToggle(input);
    }

    @RequestMapping(value = "/CommentCreate", method = RequestMethod.POST)
    public FrontScienceCommentCreateResultDto CommentCreate(@RequestBody FrontScienceCommentCreateInput input) {
        return frontScienceService.commentCreate(input);
    }

    @RequestMapping(value = "/CommentList", method = RequestMethod.POST)
    public PagedResult<TScienceCommentDto> CommentList(@RequestBody FrontScienceCommentListInput input) {
        return frontScienceService.commentList(input);
    }

    @RequestMapping(value = "/MyPublishList", method = RequestMethod.POST)
    public PagedResult<FrontScienceListItemDto> MyPublishList(@RequestBody(required = false) FrontScienceMyPublishListInput input) {
        return frontScienceService.myPublishList(input);
    }

    @SneakyThrows
    @RequestMapping(value = "/CreateOrEdit", method = RequestMethod.POST)
    public THealthScienceDto CreateOrEdit(@RequestBody FrontScienceCreateOrEditInput input) {
        return frontScienceService.createOrEdit(input);
    }

    @SneakyThrows
    @RequestMapping(value = "/MyDetail", method = RequestMethod.POST)
    public THealthScienceDto MyDetail(@RequestBody FrontScienceDetailInput input) {
        return frontScienceService.myDetail(input);
    }

    @RequestMapping(value = "/MyCollectList", method = RequestMethod.POST)
    public PagedResult<FrontScienceListItemDto> MyCollectList(@RequestBody(required = false) FrontScienceMyCollectListInput input) {
        return frontScienceService.myCollectList(input);
    }
}

