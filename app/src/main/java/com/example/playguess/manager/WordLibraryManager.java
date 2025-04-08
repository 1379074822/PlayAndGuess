package com.example.playguess.manager;

import android.content.Context;
import android.util.Log;

import com.example.playguess.model.WordLibrary;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 词库管理类，负责词库的保存、加载和管理
 */
public class WordLibraryManager {
    private static final String TAG = "WordLibraryManager";
    private static final String FILE_NAME = "word_libraries.json";
    
    private static WordLibraryManager instance;
    private final Context context;
    private final Gson gson;
    private List<WordLibrary> libraries;
    
    private WordLibraryManager(Context context) {
        this.context = context.getApplicationContext();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.libraries = loadLibraries();
    }
    
    /**
     * 获取WordLibraryManager单例
     */
    public static synchronized WordLibraryManager getInstance(Context context) {
        if (instance == null) {
            instance = new WordLibraryManager(context);
        }
        return instance;
    }
    
    /**
     * 加载所有词库
     */
    private List<WordLibrary> loadLibraries() {
        File file = new File(context.getFilesDir(), FILE_NAME);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        
        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<List<WordLibrary>>(){}.getType();
            List<WordLibrary> loadedLibraries = gson.fromJson(reader, type);
            return loadedLibraries != null ? loadedLibraries : new ArrayList<>();
        } catch (IOException e) {
            Log.e(TAG, "Error loading libraries: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * 保存所有词库
     */
    private boolean saveLibraries() {
        File file = new File(context.getFilesDir(), FILE_NAME);
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(libraries, writer);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error saving libraries: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 为词库中的词语去重
     */
    private List<String> removeDuplicates(List<String> words) {
        // 使用HashSet去重
        Set<String> uniqueWordsSet = new HashSet<>(words);
        return new ArrayList<>(uniqueWordsSet);
    }
    
    /**
     * 添加新词库
     */
    public boolean addLibrary(WordLibrary library) {
        if (library == null || library.getTitle() == null || library.getTitle().isEmpty()) {
            return false;
        }
        
        // 为词库中的词语去重
        List<String> uniqueWords = removeDuplicates(library.getWords());
        library.setWords(uniqueWords);
        
        libraries.add(library);
        return saveLibraries();
    }
    
    /**
     * 获取所有词库
     */
    public List<WordLibrary> getAllLibraries() {
        return new ArrayList<>(libraries);
    }
    
    /**
     * 根据ID获取词库
     */
    public WordLibrary getLibraryById(String id) {
        for (WordLibrary library : libraries) {
            if (library.getId().equals(id)) {
                return library;
            }
        }
        return null;
    }
    
    /**
     * 更新词库
     */
    public boolean updateLibrary(WordLibrary library) {
        if (library == null || library.getId() == null) {
            return false;
        }
        
        // 为词库中的词语去重
        List<String> uniqueWords = removeDuplicates(library.getWords());
        library.setWords(uniqueWords);
        
        for (int i = 0; i < libraries.size(); i++) {
            if (libraries.get(i).getId().equals(library.getId())) {
                libraries.set(i, library);
                return saveLibraries();
            }
        }
        
        return false;
    }
    
    /**
     * 删除词库
     */
    public boolean deleteLibrary(String id) {
        if (id == null) {
            return false;
        }
        
        for (int i = 0; i < libraries.size(); i++) {
            if (libraries.get(i).getId().equals(id)) {
                libraries.remove(i);
                return saveLibraries();
            }
        }
        
        return false;
    }
    
    /**
     * 刷新词库数据
     */
    public void refreshLibraries() {
        this.libraries = loadLibraries();
    }
} 