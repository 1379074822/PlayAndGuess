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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.playguess.adapter.LibraryCardAdapter;
import com.example.playguess.manager.WordLibraryManager;
import com.example.playguess.model.WordLibrary;

import java.util.List;

public class GameActivity extends BaseActivity {

    private static final String TAG = "GameActivity";
    
    private RecyclerView recyclerViewLibraries;
    private LibraryCardAdapter adapter;
    private EditText editTextSearch;
    private TextView textViewEmptyLibraries;
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
            return Math.max(3, screenWidth / getResources().getDimensionPixelSize(R.dimen.library_card_width));
        } else {
            // 竖屏显示较少卡片
            return Math.max(2, (screenWidth - 100) / getResources().getDimensionPixelSize(R.dimen.library_card_width));
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
        int verticalPadding = getResources().getDimensionPixelSize(R.dimen.library_card_height) / 12;
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
                params.height = cardHeight + (int)(32 * getResources().getDisplayMetrics().density);
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
} 