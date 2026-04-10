package com.example.web.tools.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;
@NoArgsConstructor
@Data
public class PagedResult<T> {

    private Long TotalCount;


    private List<T> Items;

    /**
     * 统计信息（可选，用于通知列表等场景）
     */
    private Object Stats;


    public static <T> PagedResult<T> GetInstance(List<T> items, Long totalCount)
    {
        PagedResult<T> tPagedReuslt = new PagedResult<>();
        tPagedReuslt.setItems(items);
        tPagedReuslt.setTotalCount(totalCount);
        return tPagedReuslt;
    }
	  public static <T> PagedResult<T> GetEmptyInstance() {
        PagedResult<T> tPagedReuslt = new PagedResult<>();
        tPagedReuslt.setItems(new ArrayList<>());
        tPagedReuslt.setTotalCount(0L);
        return tPagedReuslt;
    }


}
