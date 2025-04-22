package com.example.playguess;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.playguess.adapter.WordResultAdapter;
import com.example.playguess.model.WordResult;

import java.util.ArrayList;
import java.util.List;

public class GameResultActivity extends BaseActivity {

    // 常量定义
    private static final String TAG = "GameResultActivity";
    public static final String EXTRA_WORD_RESULTS = "extra_word_results";
    public static final String EXTRA_CORRECT_COUNT = "extra_correct_count";
    public static final String EXTRA_SKIPPED_COUNT = "extra_skipped_count";
    public static final String EXTRA_LIBRARY_ID = "extra_library_id";
    
    // 视图对象
    private TextView textViewScore;
    private TextView textViewResultLabel;
    private RecyclerView recyclerViewResults;
    private Button buttonBackToHome;
    private Button buttonPlayAgain;
    private Button buttonSelectLibrary;
    
    // 适配器
    private WordResultAdapter adapter;
    
    // 数据
    private ArrayList<WordResult> wordResults;
    private int correctCount = 0;
    private int skippedCount = 0;
    private String libraryId; // 保存词库ID以便再次游戏使用

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_result);
        
        // 初始化视图
        initViews();
        
        // 获取传递的数据
        getData();
        
        // 输出WordResults的每个元素用于调试
        logWordResults();
        
        // 设置结果列表
        setupRecyclerView();
        
        // 更新UI
        updateUI();
        
        // 设置事件监听
        setupListeners();
    }
    
    /**
     * 打印词语结果列表用于调试
     */
    private void logWordResults() {
        if (wordResults == null || wordResults.isEmpty()) {
            Log.e(TAG, "词语结果列表为空");
            return;
        }
        
        Log.d(TAG, "词语结果列表: 共" + wordResults.size() + "个元素");
        for (int i = 0; i < wordResults.size(); i++) {
            WordResult result = wordResults.get(i);
            Log.d(TAG, "第" + i + "个词: " + result.getWord() + 
                  ", 猜对=" + result.isCorrect() + 
                  ", 跳过=" + result.isSkipped());
        }
    }
    
    /**
     * 初始化视图
     */
    private void initViews() {
        textViewScore = findViewById(R.id.textViewScore);
        textViewResultLabel = findViewById(R.id.textViewResultLabel);
        recyclerViewResults = findViewById(R.id.recyclerViewResults);
        buttonBackToHome = findViewById(R.id.buttonBackToHome);
        buttonPlayAgain = findViewById(R.id.buttonPlayAgain);
        buttonSelectLibrary = findViewById(R.id.buttonSelectLibrary);
    }
    
    /**
     * 获取传递的数据
     */
    private void getData() {
        Intent intent = getIntent();
        if (intent != null) {
            // 获取词语结果列表
            if (intent.hasExtra(EXTRA_WORD_RESULTS)) {
                wordResults = (ArrayList<WordResult>) intent.getSerializableExtra(EXTRA_WORD_RESULTS);
                Log.d(TAG, "成功获取词语结果列表，大小: " + (wordResults != null ? wordResults.size() : 0));
            } else {
                Log.e(TAG, "没有找到词语结果列表，Extra key: " + EXTRA_WORD_RESULTS);
            }
            
            // 获取猜对数量
            if (intent.hasExtra(EXTRA_CORRECT_COUNT)) {
                correctCount = intent.getIntExtra(EXTRA_CORRECT_COUNT, 0);
                Log.d(TAG, "获取到猜对数量: " + correctCount);
            } else {
                Log.e(TAG, "没有找到猜对数量，Extra key: " + EXTRA_CORRECT_COUNT);
            }
            
            // 获取跳过数量
            if (intent.hasExtra(EXTRA_SKIPPED_COUNT)) {
                skippedCount = intent.getIntExtra(EXTRA_SKIPPED_COUNT, 0);
                Log.d(TAG, "获取到跳过数量: " + skippedCount);
            } else {
                Log.e(TAG, "没有找到跳过数量，Extra key: " + EXTRA_SKIPPED_COUNT);
            }
            
            // 获取词库ID
            if (intent.hasExtra(EXTRA_LIBRARY_ID)) {
                libraryId = intent.getStringExtra(EXTRA_LIBRARY_ID);
                Log.d(TAG, "获取到词库ID: " + libraryId);
            } else {
                Log.e(TAG, "没有找到词库ID，Extra key: " + EXTRA_LIBRARY_ID);
            }
            
            // 记录获取到的数据
            Log.d(TAG, "获取数据: 总词数=" + (wordResults != null ? wordResults.size() : 0) + 
                   ", 猜对数=" + correctCount + ", 跳过数=" + skippedCount + ", 词库ID=" + libraryId);
        } else {
            Log.e(TAG, "获取Intent失败，可能是由于其他方式启动活动");
        }
        
        // 如果没有传递结果数据，创建空列表
        if (wordResults == null) {
            wordResults = new ArrayList<>();
            Log.w(TAG, "未收到结果数据，创建空列表");
            
            // 提示用户
            Toast.makeText(this, "没有获取到游戏结果数据", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 设置RecyclerView
     */
    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewResults.setLayoutManager(layoutManager);
        
        // 添加分割线
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                recyclerViewResults.getContext(), layoutManager.getOrientation());
        recyclerViewResults.addItemDecoration(dividerItemDecoration);
        
        // 设置适配器
        adapter = new WordResultAdapter(this);
        adapter.setData(wordResults);
        recyclerViewResults.setAdapter(adapter);
        
        Log.d(TAG, "设置RecyclerView完成，显示" + adapter.getItemCount() + "个词语结果");
        
        // 检查适配器数据
        if (adapter.getItemCount() == 0) {
            Log.w(TAG, "适配器中没有数据");
        }
    }
    
    /**
     * 更新UI
     */
    private void updateUI() {
        // 显示猜对和跳过词语数量
        String scoreText = getString(R.string.correct_count, correctCount) + 
                           "  " + 
                           getString(R.string.skipped_count, skippedCount);
        textViewScore.setText(scoreText);
        
        // 设置结果标签
        textViewResultLabel.setText(getString(R.string.game_result));
    }
    
    /**
     * 设置事件监听
     */
    private void setupListeners() {
        // 点击返回主页按钮
        buttonBackToHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 返回主页
                Intent intent = new Intent(GameResultActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });

        // 再来一次按钮点击事件
        buttonPlayAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (libraryId != null && !libraryId.isEmpty()) {
                    // 使用当前词库再次开始游戏
                    GamePlayActivity.start(GameResultActivity.this, libraryId);
                    finish();
                } else {
                    Toast.makeText(GameResultActivity.this, "无法找到词库信息", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 选择词库按钮点击事件
        buttonSelectLibrary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到选择词库页面
                Intent intent = new Intent(GameResultActivity.this, GameActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
} 