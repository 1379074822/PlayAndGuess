package com.example.playguess.manager;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 游戏设置管理器，负责保存和获取游戏设置
 */
public class GameSettingsManager {
    private static final String PREFS_NAME = "game_settings";
    private static final String KEY_GAME_DURATION = "game_duration";
    private static final String KEY_AUDIO_ENABLED = "audio_enabled";
    private static final int DEFAULT_DURATION = 120; // 默认游戏时长为120秒
    private static final boolean DEFAULT_AUDIO_ENABLED = false; // 默认音频关闭
    
    private static GameSettingsManager instance;
    private final SharedPreferences preferences;
    
    private GameSettingsManager(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * 获取GameSettingsManager单例
     */
    public static synchronized GameSettingsManager getInstance(Context context) {
        if (instance == null) {
            instance = new GameSettingsManager(context);
        }
        return instance;
    }
    
    /**
     * 设置游戏时长
     * @param duration 游戏时长（秒）
     */
    public void setGameDuration(int duration) {
        preferences.edit().putInt(KEY_GAME_DURATION, duration).apply();
    }
    
    /**
     * 获取游戏时长
     * @return 游戏时长（秒）
     */
    public int getGameDuration() {
        return preferences.getInt(KEY_GAME_DURATION, DEFAULT_DURATION);
    }
    
    /**
     * 设置音频是否启用
     * @param enabled 是否启用音频
     */
    public void setAudioEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_AUDIO_ENABLED, enabled).apply();
    }
    
    /**
     * 获取音频是否启用
     * @return 音频是否启用
     */
    public boolean isAudioEnabled() {
        return preferences.getBoolean(KEY_AUDIO_ENABLED, DEFAULT_AUDIO_ENABLED);
    }
} 