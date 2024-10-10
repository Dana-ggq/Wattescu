package com.example.wattescu.news;

import java.io.Serializable;
import java.util.List;

public class NewsApiResponse implements Serializable {

    private String status;
    private int totalResults;
    List<NewsHeadline> articles;

    public NewsApiResponse() {
    }

    public NewsApiResponse(String status, int totalResults, List<NewsHeadline> articles) {
        this.status = status;
        this.totalResults = totalResults;
        this.articles = articles;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    public List<NewsHeadline> getArticles() {
        return articles;
    }

    public void setArticles(List<NewsHeadline> articles) {
        this.articles = articles;
    }
}
