package com.example.playguess;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.playguess.adapter.LibraryCardAdapter;
import com.example.playguess.manager.WordLibraryManager;
import com.example.playguess.model.WordLibrary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class GameActivity extends BaseActivity {

    private static final String TAG = "GameActivity";
    // 添加全部词库和随机词库的特殊ID标识
    public static final String ALL_LIBRARIES_ID = "all_libraries_special_id";
    public static final String RANDOM_LIBRARY_ID = "random_library_special_id";
    
    private RecyclerView recyclerViewLibraries;
    private LibraryCardAdapter adapter;
    private EditText editTextSearch;
    private TextView textViewEmptyLibraries;
    private ImageButton imageButtonAllLibraries;
    private ImageButton imageButtonRandomLibrary;
    private WordLibraryManager libraryManager;
    private int screenWidth;
    private int screenHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        
        // 获取屏幕尺寸
        calculateScreenSize();
        
        // 初始化词库管理器
        libraryManager = WordLibraryManager.getInstance(this);
        
        // 初始化视图
        recyclerViewLibraries = findViewById(R.id.recyclerViewLibraries);
        editTextSearch = findViewById(R.id.editTextSearch);
        textViewEmptyLibraries = findViewById(R.id.textViewEmptyLibraries);
        imageButtonAllLibraries = findViewById(R.id.imageButtonAllLibraries);
        imageButtonRandomLibrary = findViewById(R.id.imageButtonRandomLibrary);
        
        // 设置标题
        setTitle(R.string.game_title);
        
        // 设置返回按钮
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        
        // 设置RecyclerView
        setupRecyclerView();
        
        // 设置搜索功能
        setupSearch();
        
        // 设置特殊模式按钮
        setupSpecialButtons();
        
        // 加载词库
        loadLibraries();
    }
    
    /**
     * 计算屏幕尺寸
     */
    private void calculateScreenSize() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
        
        // 记录屏幕尺寸用于调试
        Log.d(TAG, "屏幕尺寸: " + screenWidth + "x" + screenHeight + ", 密度: " + displayMetrics.density);
    }
    
    /**
     * 根据屏幕尺寸动态计算卡片显示数量和大小
     */
    private int calculateCardCount() {
        // 根据屏幕宽度和方向确定显示的卡片数量
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // 横屏显示更多卡片
            return Math.max(4, screenWidth / getResources().getDimensionPixelSize(R.dimen.library_card_width));
        } else {
            // 竖屏显示较少卡片
            return Math.max(3, (screenWidth - 80) / getResources().getDimensionPixelSize(R.dimen.library_card_width));
        }
    }
    
    private void setupRecyclerView() {
        // 使用LinearLayoutManager代替GridLayoutManager，确保卡片横向排列
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                this, RecyclerView.HORIZONTAL, false);
        recyclerViewLibraries.setLayoutManager(layoutManager);
        
        // 计算屏幕可见的卡片数量
        final int visibleCardCount = calculateCardCount();
        
        // 添加项目装饰，确保卡片之间有适当的间距
        recyclerViewLibraries.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                // 设置卡片之间的间距
                int cardSpacing = (int)(12 * getResources().getDisplayMetrics().density);
                outRect.right = cardSpacing;
                
                // 为第一个项目增加左边距
                if (parent.getChildAdapterPosition(view) == 0) {
                    outRect.left = cardSpacing / 2;
                }
                
                // 为最后一个项目增加右边距
                if (parent.getChildAdapterPosition(view) == parent.getAdapter().getItemCount() - 1) {
                    outRect.right = cardSpacing / 2;
                }
                
                // 添加上下边距以确保卡片在垂直方向上居中
                int verticalMargin = (int)(6 * getResources().getDisplayMetrics().density);
                outRect.top = verticalMargin;
                outRect.bottom = verticalMargin;
            }
        });
        
        // 设置适配器
        adapter = new LibraryCardAdapter(this, libraryManager.getAllLibraries());
        recyclerViewLibraries.setAdapter(adapter);
        
        // 设置点击事件
        adapter.setOnLibraryClickListener(new LibraryCardAdapter.OnLibraryClickListener() {
            @Override
            public void onLibraryClick(WordLibrary library) {
                // 选中词库，启动游戏页面
                Toast.makeText(GameActivity.this, 
                    "已选择词库: " + library.getTitle(), 
                    Toast.LENGTH_SHORT).show();
                
                // 跳转到游戏页面
                GamePlayActivity.start(GameActivity.this, library.getId());
            }
        });

        // 添加渐变效果
        recyclerViewLibraries.setHasFixedSize(true);
        recyclerViewLibraries.setNestedScrollingEnabled(false);
        
        // 设置RecyclerView的上下边距，确保卡片完全可见
        int verticalPadding = getResources().getDimensionPixelSize(R.dimen.library_card_height) / 16;
        recyclerViewLibraries.setPadding(
            recyclerViewLibraries.getPaddingLeft(),
            verticalPadding,
            recyclerViewLibraries.getPaddingRight(),
            verticalPadding
        );
        
        // 当布局完成后，确保卡片在屏幕上正确显示
        recyclerViewLibraries.post(new Runnable() {
            @Override
            public void run() {
                // 调整RecyclerView高度确保卡片完全显示
                ViewGroup.LayoutParams params = recyclerViewLibraries.getLayoutParams();
                int cardHeight = getResources().getDimensionPixelSize(R.dimen.library_card_height);
                // 设置RecyclerView的高度为卡片高度加上一定的边距
                params.height = cardHeight + (int)(24 * getResources().getDisplayMetrics().density);
                recyclerViewLibraries.setLayoutParams(params);
                
                Log.d(TAG, "RecyclerView高度已调整为: " + params.height + 
                      ", 卡片高度: " + cardHeight +
                      ", 可见卡片数: " + visibleCardCount);
            }
        });
    }
    
    private void setupSearch() {
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 不需要实现
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 搜索词库
                adapter.filter(s.toString());
                
                // 显示空结果提示
                if (adapter.getItemCount() == 0 && !s.toString().isEmpty()) {
                    textViewEmptyLibraries.setText(R.string.search_no_result);
                    textViewEmptyLibraries.setVisibility(View.VISIBLE);
                } else if (adapter.getItemCount() == 0) {
                    textViewEmptyLibraries.setText(R.string.no_libraries_yet);
                    textViewEmptyLibraries.setVisibility(View.VISIBLE);
                } else {
                    textViewEmptyLibraries.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 不需要实现
            }
        });
    }
    
    private void loadLibraries() {
        List<WordLibrary> libraries = libraryManager.getAllLibraries();
        adapter.setData(libraries);
        
        // 显示空库提示
        if (libraries.isEmpty()) {
            textViewEmptyLibraries.setVisibility(View.VISIBLE);
        } else {
            textViewEmptyLibraries.setVisibility(View.GONE);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // 每次页面恢复时重新加载词库
        loadLibraries();
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
     * 设置特殊模式按钮
     */
    private void setupSpecialButtons() {
        // 全部词库按钮
        imageButtonAllLibraries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAllLibrariesGame();
            }
        });
        
        // 随机词库按钮
        imageButtonRandomLibrary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRandomLibraryGame();
            }
        });
        
        // 显示按钮说明提示
        TextView textViewButtonHint = findViewById(R.id.textViewButtonHint);
        if (textViewButtonHint != null) {
            imageButtonAllLibraries.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    textViewButtonHint.setText(getString(R.string.all_libraries));
                    textViewButtonHint.setVisibility(View.VISIBLE);
                    // 3秒后隐藏提示
                    textViewButtonHint.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            textViewButtonHint.setVisibility(View.GONE);
                        }
                    }, 3000);
                    return true;
                }
            });
            
            imageButtonRandomLibrary.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    textViewButtonHint.setText(getString(R.string.random_library));
                    textViewButtonHint.setVisibility(View.VISIBLE);
                    // 3秒后隐藏提示
                    textViewButtonHint.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            textViewButtonHint.setVisibility(View.GONE);
                        }
                    }, 3000);
                    return true;
                }
            });
        }
    }
    
    /**
     * 启动全部词库游戏
     */
    private void startAllLibrariesGame() {
        List<WordLibrary> libraries = libraryManager.getAllLibraries();
        if (libraries.isEmpty()) {
            Toast.makeText(this, "没有可用的词库", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 创建包含所有词库词语的虚拟词库
        WordLibrary allLibrary = createAllLibrariesWordLibrary(libraries);
        
        // 显示确认对话框
        if (allLibrary.getWords().isEmpty()) {
            Toast.makeText(this, "所有词库中没有词语", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Toast.makeText(this, "已选择全部词库，共" + allLibrary.getWords().size() + "个词", Toast.LENGTH_SHORT).show();
        
        // 使用特殊ID启动游戏
        GamePlayActivity.start(this, ALL_LIBRARIES_ID);
    }
    
    /**
     * 启动随机词库游戏
     */
    private void startRandomLibraryGame() {
        List<WordLibrary> libraries = libraryManager.getAllLibraries();
        if (libraries.isEmpty()) {
            Toast.makeText(this, "没有可用的词库", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 随机选择一个词库
        int randomIndex = new Random().nextInt(libraries.size());
        WordLibrary randomLibrary = libraries.get(randomIndex);
        
        if (randomLibrary.getWords().isEmpty()) {
            Toast.makeText(this, "随机选择的词库中没有词语", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Toast.makeText(this, "已随机选择词库: " + randomLibrary.getTitle(), Toast.LENGTH_SHORT).show();
        
        // 启动游戏
        GamePlayActivity.start(this, randomLibrary.getId());
    }
    
    /**
     * 创建包含所有词库词语的词库
     */
    private WordLibrary createAllLibrariesWordLibrary(List<WordLibrary> libraries) {
        // 创建新的合并词库
        WordLibrary allLibrary = new WordLibrary();
        allLibrary.setId(ALL_LIBRARIES_ID);
        allLibrary.setTitle(getString(R.string.all_libraries));
        
        // 合并所有词语
        List<String> allWords = new ArrayList<>();
        for (WordLibrary library : libraries) {
            allWords.addAll(library.getWords());
        }
        
        // 去重
        Set<String> uniqueWords = new HashSet<>(allWords);
        allWords = new ArrayList<>(uniqueWords);
        
        // 打乱顺序
        Collections.shuffle(allWords);
        
        allLibrary.setWords(allWords);
        return allLibrary;
    }
} 