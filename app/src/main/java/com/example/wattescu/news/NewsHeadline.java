package com.example.wattescu.news;

import java.io.Serializable;

public class NewsHeadline implements Serializable {

    private Source source;
    private String author;
    private String title;
    private String description;
    private  String url;
    private  String urlToImage;
    private String publishedAt;
    private String content;

    public NewsHeadline() {
    }

    public NewsHeadline(Source source,String publishedAt, String author, String title, String description, String url, String urlToImage, String content) {
        this.source = source;
        this.author = author;
        this.publishedAt = publishedAt;
        this.title = title;
        this.description = description;
        this.url = url;
        this.urlToImage = urlToImage;
        this.content = content;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrlToImage() {
        return urlToImage;
    }

    public void setUrlToImage(String urlToImage) {
        this.urlToImage = urlToImage;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
