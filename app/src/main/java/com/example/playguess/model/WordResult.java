package com.example.playguess.model;

import java.io.Serializable;

/**
 * 词语结果模型类，用于记录游戏中每个词语的处理结果
 */
public class WordResult implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String word;        // 词语
    private boolean correct;    // 是否猜对
    private boolean skipped;    // 是否跳过
    private long timestamp;     // 时间戳

    public WordResult(String word, boolean correct, boolean skipped) {
        this.word = word;
        this.correct = correct;
        this.skipped = skipped;
        this.timestamp = System.currentTimeMillis();
    }

    // 获取词语
    public String getWord() {
        return word;
    }

    // 设置词语
    public void setWord(String word) {
        this.word = word;
    }

    // 是否猜对
    public boolean isCorrect() {
        return correct;
    }

    // 设置是否猜对
    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    // 是否跳过
    public boolean isSkipped() {
        return skipped;
    }

    // 设置是否跳过
    public void setSkipped(boolean skipped) {
        this.skipped = skipped;
    }

    // 获取时间戳
    public long getTimestamp() {
        return timestamp;
    }

    // 设置时间戳
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "词语: " + word + 
               ", 猜对: " + correct + 
               ", 跳过: " + skipped;
    }
} 