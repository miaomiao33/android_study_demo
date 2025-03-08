package com.example.android_study_demo_project.internetImageUsage.model;

/**
 * 网络图片model
 */
public class InternetImageModel {
    int id;
    String url;
    int width;
    int height;

    public InternetImageModel() {
    }

    public InternetImageModel(String url) {
        this.url = url;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "InternetImageModel{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}
