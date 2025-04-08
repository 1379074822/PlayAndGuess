package com.example.playguess;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.playguess.adapter.WordLibraryAdapter;
import com.example.playguess.manager.WordLibraryManager;
import com.example.playguess.model.WordLibrary;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AddWordsActivity extends BaseActivity {

    private EditText editTextLibraryTitle;
    private EditText editTextLibraryContent;
    private Button buttonConfirm;
    private Button buttonCancel;
    private RecyclerView recyclerViewLibraries;
    private WordLibraryAdapter adapter;
    private WordLibraryManager libraryManager;

    // 当前正在编辑的词库ID，如果为null则表示创建新词库
    private String currentEditingLibraryId = null;
    // 是否是新建词库状态
    private boolean isCreatingNew = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_words);

        // 初始化词库管理器
        libraryManager = WordLibraryManager.getInstance(this);

        // 初始化视图
        editTextLibraryTitle = findViewById(R.id.editTextLibraryTitle);
        editTextLibraryContent = findViewById(R.id.editTextLibraryContent);
        buttonConfirm = findViewById(R.id.buttonConfirm);
        buttonCancel = findViewById(R.id.buttonCancel);
        recyclerViewLibraries = findViewById(R.id.recyclerViewLibraries);

        // 设置确定按钮文本为"保存新词库"
        buttonConfirm.setText(R.string.save_new_library);

        // 设置确定按钮点击事件
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveWordLibrary();
            }
        });

        // 设置取消按钮点击事件
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 如果是创建新词库状态，返回主页
                // 如果是编辑已有词库状态，返回主页
                finish();
            }
        });

        // 设置词库列表
        setupRecyclerView();

        // 加载词库列表
        loadLibraries();
        
        // 设置标题
        setTitle(R.string.add_word_library);
        
        // 设置返回按钮
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_secondary, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // 点击了返回按钮
            finish();
            return true;
        } else if (id == R.id.action_home) {
            // 点击了主页按钮，跳转到主页
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 设置RecyclerView
     */
    private void setupRecyclerView() {
        recyclerViewLibraries.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WordLibraryAdapter(this, new ArrayList<>());
        recyclerViewLibraries.setAdapter(adapter);

        // 设置列表项点击事件
        adapter.setOnItemClickListener(new WordLibraryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(WordLibrary library) {
                // 加载词库到编辑区域
                loadLibraryToEdit(library);
            }

            @Override
            public void onCreateNewClicked() {
                // 切换到创建新词库状态
                switchToCreateNewMode();
            }
        });
    }

    /**
     * 切换到创建新词库状态
     */
    private void switchToCreateNewMode() {
        isCreatingNew = true;
        currentEditingLibraryId = null;
        editTextLibraryTitle.setText("");
        editTextLibraryContent.setText("");
        buttonConfirm.setText(R.string.save_new_library);
    }

    /**
     * 将词库加载到编辑区域
     */
    private void loadLibraryToEdit(WordLibrary library) {
        if (library == null) return;

        isCreatingNew = false;
        // 保存当前编辑的词库ID
        currentEditingLibraryId = library.getId();

        // 设置标题
        editTextLibraryTitle.setText(library.getTitle());

        // 将词语列表转换为文本，每行一个词语
        StringBuilder contentBuilder = new StringBuilder();
        List<String> words = library.getWords();
        for (int i = 0; i < words.size(); i++) {
            contentBuilder.append(words.get(i));
            if (i < words.size() - 1) {
                contentBuilder.append("\n");
            }
        }
        editTextLibraryContent.setText(contentBuilder.toString());

        // 更新确定按钮文本
        buttonConfirm.setText(R.string.update_library);
    }

    /**
     * 加载词库列表
     */
    private void loadLibraries() {
        List<WordLibrary> libraries = libraryManager.getAllLibraries();
        adapter.setData(libraries);
    }

    /**
     * 检查词库标题是否已存在
     * @return 如果标题已存在且不是当前编辑的词库，则返回true；否则返回false
     */
    private boolean isTitleAlreadyExists(String title) {
        List<WordLibrary> libraries = libraryManager.getAllLibraries();
        for (WordLibrary library : libraries) {
            // 如果是编辑模式，跳过当前编辑的词库
            if (currentEditingLibraryId != null && library.getId().equals(currentEditingLibraryId)) {
                continue;
            }
            
            if (library.getTitle().equals(title)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 保存词库内容
     */
    private void saveWordLibrary() {
        String title = editTextLibraryTitle.getText().toString().trim();
        String content = editTextLibraryContent.getText().toString().trim();

        // 验证输入
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "请输入词库标题", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(content)) {
            Toast.makeText(this, "请输入词库内容", Toast.LENGTH_SHORT).show();
            return;
        }

        // 检查标题是否重复
        if (isTitleAlreadyExists(title)) {
            Toast.makeText(this, R.string.title_already_exists, Toast.LENGTH_SHORT).show();
            return;
        }

        // 处理词库内容，按行分割成词语列表并去重
        Set<String> wordSet = new HashSet<>(); // 使用Set去重
        String[] lines = content.split("\\n");
        for (String line : lines) {
            String word = line.trim();
            if (!TextUtils.isEmpty(word)) {
                wordSet.add(word);
            }
        }

        if (wordSet.isEmpty()) {
            Toast.makeText(this, "词库内容不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        // 转换为List
        List<String> words = new ArrayList<>(wordSet);
        
        boolean success;
        
        // 根据是否有当前编辑的词库ID来决定是更新还是创建
        if (currentEditingLibraryId != null) {
            // 更新现有词库
            WordLibrary existingLibrary = libraryManager.getLibraryById(currentEditingLibraryId);
            if (existingLibrary != null) {
                existingLibrary.setTitle(title);
                existingLibrary.setWords(words);
                success = libraryManager.updateLibrary(existingLibrary);
                
                if (success) {
                    Toast.makeText(this, "词库更新成功", Toast.LENGTH_SHORT).show();
                    
                    // 更新成功后保持当前选中状态，重新加载词库内容
                    loadLibraryToEdit(existingLibrary);
                } else {
                    Toast.makeText(this, "词库更新失败，请重试", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                // 如果找不到词库，创建新的
                WordLibrary newLibrary = new WordLibrary(title, words);
                success = libraryManager.addLibrary(newLibrary);
                
                if (success) {
                    Toast.makeText(this, "词库保存成功", Toast.LENGTH_SHORT).show();
                    // 保存成功后刷新列表，并选中新增的词库
                    loadLibraries();
                    // 找到新增词库的位置并选中
                    selectLibraryByTitle(title);
                } else {
                    Toast.makeText(this, "词库保存失败，请重试", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        } else {
            // 创建新词库
            WordLibrary newLibrary = new WordLibrary(title, words);
            success = libraryManager.addLibrary(newLibrary);
            
            if (success) {
                Toast.makeText(this, "词库保存成功", Toast.LENGTH_SHORT).show();
                // 保存成功后刷新列表，并继续保持"添加新词库"状态
                loadLibraries();
                switchToCreateNewMode();
                // 确保"添加新词库"选项仍然是选中状态
                adapter.setSelectedPosition(0);
            } else {
                Toast.makeText(this, "词库保存失败，请重试", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        
        // 刷新列表
        loadLibraries();
    }
    
    /**
     * 通过标题查找并选中词库
     */
    private void selectLibraryByTitle(String title) {
        List<WordLibrary> libraries = libraryManager.getAllLibraries();
        for (int i = 0; i < libraries.size(); i++) {
            if (libraries.get(i).getTitle().equals(title)) {
                // 因为列表的第一项是"添加新词库"，所以实际位置需要+1
                adapter.setSelectedPosition(i + 1);
                break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 每次页面恢复时刷新词库列表
        loadLibraries();
    }
} 