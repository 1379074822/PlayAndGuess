package com.example.playguess.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 词库模型类
 */
public class WordLibrary {
    private String id;          // 唯一标识
    private String title;       // 词库标题
    private List<String> words; // 词库内容，每个词语为一个字符串
    private long createTime;    // 创建时间戳

    public WordLibrary() {
        this.id = UUID.randomUUID().toString();
        this.words = new ArrayList<>();
        this.createTime = System.currentTimeMillis();
    }

    public WordLibrary(String title, List<String> words) {
        this();
        this.title = title;
        this.words = words;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getWords() {
        return words;
    }

    public void setWords(List<String> words) {
        this.words = words;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
} 