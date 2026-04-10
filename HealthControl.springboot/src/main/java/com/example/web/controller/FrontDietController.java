package com.example.web.controller;

import com.example.web.dto.front.FrontDietDtos.*;
import com.example.web.service.front.FrontDietService;
import com.example.web.tools.dto.PagedResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台饮食管理与推荐接口（小程序端）
 */
@RestController
@RequestMapping("/Front/Diet")
public class FrontDietController {

    @Autowired
    private FrontDietService frontDietService;

    @RequestMapping(value = "/Home", method = RequestMethod.POST)
    public FrontDietHomeOutput Home() {
        return frontDietService.home();
    }

    @RequestMapping(value = "/CategoryList", method = RequestMethod.POST)
    public java.util.List<FrontDietCategoryListItem> CategoryList(@RequestBody FrontDietCategoryListInput input) {
        return frontDietService.categoryList(input);
    }

    @RequestMapping(value = "/FoodList", method = RequestMethod.POST)
    public PagedResult<FrontDietFoodListItem> FoodList(@RequestBody FrontDietFoodListInput input) {
        return frontDietService.foodList(input);
    }

    @RequestMapping(value = "/FoodDetail", method = RequestMethod.POST)
    public FrontDietFoodDetail FoodDetail(@RequestBody FrontDietFoodDetailInput input) {
        return frontDietService.foodDetail(input);
    }

    @RequestMapping(value = "/RecordCreateOrEdit", method = RequestMethod.POST)
    public void RecordCreateOrEdit(@RequestBody FrontDietRecordCreateOrEditInput input) {
        frontDietService.recordCreateOrEdit(input);
    }

    @RequestMapping(value = "/RecordList", method = RequestMethod.POST)
    public PagedResult<FrontDietRecordItem> RecordList(@RequestBody FrontDietRecordListInput input) {
        return frontDietService.recordList(input);
    }

    @RequestMapping(value = "/RecordDelete", method = RequestMethod.POST)
    public void RecordDelete(@RequestBody FrontDietRecordDeleteInput input) {
        frontDietService.recordDelete(input);
    }
}

