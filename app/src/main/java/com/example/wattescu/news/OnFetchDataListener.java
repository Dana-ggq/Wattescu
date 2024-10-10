package com.example.wattescu.news;

import java.util.List;

public interface OnFetchDataListener<NewsApiResponse> {
    void onFetchData(List<NewsHeadline> headlineList, String response);
    void onError(String response);

}
