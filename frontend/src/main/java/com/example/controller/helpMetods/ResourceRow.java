package com.example.controller.helpMetods;

public class ResourceRow {
    private Long id;
    private String title;
    private String url;
    private String uploader;

    public ResourceRow(Long id, String title, String url, String uploader) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.uploader = uploader;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getUrl() { return url; }
    public String getUploader() { return uploader; }
}
