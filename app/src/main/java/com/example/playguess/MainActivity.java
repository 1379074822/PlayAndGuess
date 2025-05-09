package com.example.playguess;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.playguess.manager.GameSettingsManager;

public class MainActivity extends BaseActivity {

    private Button btnStartGame;
    private Button btnAddWords;
    private GameSettingsManager settingsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化设置管理器
        settingsManager = GameSettingsManager.getInstance(this);
        
        // 初始化按钮
        btnStartGame = findViewById(R.id.btnStartGame);
        btnAddWords = findViewById(R.id.btnAddWords);

        // 设置点击事件监听器
        btnStartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到游戏页面
                Intent intent = new Intent(MainActivity.this, GameActivity.class);
                startActivity(intent);
            }
        });

        btnAddWords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到添加词库页面
                Intent intent = new Intent(MainActivity.this, AddWordsActivity.class);
                startActivity(intent);
            }
        });
        
        // 设置标题
        setTitle(R.string.game_title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_home) {
            // 已经在主页面，不需要操作
            return true;
        } else if (id == R.id.action_settings) {
            // 显示设置对话框
            showSettingsDialog();
            return true;
        } else if (id == R.id.action_info) {
            // 显示更新日志
            showChangelogDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * 显示游戏设置对话框
     */
    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_game_settings, null);
        builder.setView(dialogView);
        
        // 初始化对话框控件
        RadioGroup radioGroupDuration = dialogView.findViewById(R.id.radioGroupDuration);
        RadioButton radioButton120 = dialogView.findViewById(R.id.radioButton120);
        RadioButton radioButton300 = dialogView.findViewById(R.id.radioButton300);
        RadioButton radioButtonCustom = dialogView.findViewById(R.id.radioButtonCustom);
        LinearLayout layoutCustomDuration = dialogView.findViewById(R.id.customDurationLayout);
        EditText editTextCustomDuration = dialogView.findViewById(R.id.editTextCustomDuration);
        androidx.appcompat.widget.SwitchCompat switchAudio = dialogView.findViewById(R.id.switchAudio);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        Button buttonOk = dialogView.findViewById(R.id.buttonOk);
        
        // 获取当前设置
        int currentDuration = settingsManager.getGameDuration();
        boolean isAudioEnabled = settingsManager.isAudioEnabled();
        
        // 设置当前音频状态
        switchAudio.setChecked(isAudioEnabled);
        
        // 根据当前设置选中对应的选项
        if (currentDuration == 120) {
            radioButton120.setChecked(true);
        } else if (currentDuration == 300) {
            radioButton300.setChecked(true);
        } else {
            radioButtonCustom.setChecked(true);
            layoutCustomDuration.setVisibility(View.VISIBLE);
            editTextCustomDuration.setText(String.valueOf(currentDuration));
        }
        
        // 设置选项变化监听器
        radioGroupDuration.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // 根据选中的选项显示或隐藏自定义时长输入框
                if (checkedId == R.id.radioButtonCustom) {
                    layoutCustomDuration.setVisibility(View.VISIBLE);
                } else {
                    layoutCustomDuration.setVisibility(View.GONE);
                }
            }
        });
        
        // 创建对话框
        AlertDialog dialog = builder.create();
        
        // 设置取消按钮点击事件
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        
        // 设置确定按钮点击事件
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 保存设置
                int selectedDuration;
                
                if (radioButton120.isChecked()) {
                    selectedDuration = 120;
                } else if (radioButton300.isChecked()) {
                    selectedDuration = 300;
                } else {
                    // 自定义时长
                    String durationStr = editTextCustomDuration.getText().toString();
                    if (TextUtils.isEmpty(durationStr)) {
                        Toast.makeText(MainActivity.this, R.string.please_enter_valid_duration, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    try {
                        selectedDuration = Integer.parseInt(durationStr);
                        // 验证时长范围（10秒到10分钟）
                        if (selectedDuration < 10 || selectedDuration > 600) {
                            Toast.makeText(MainActivity.this, R.string.please_enter_valid_duration, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(MainActivity.this, R.string.please_enter_valid_duration, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                
                // 保存游戏时长设置
                settingsManager.setGameDuration(selectedDuration);
                
                // 保存音频设置
                settingsManager.setAudioEnabled(switchAudio.isChecked());
                
                Toast.makeText(MainActivity.this, R.string.settings_saved, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        
        // 显示对话框
        dialog.show();
    }

    /**
     * 显示更新日志对话框
     */
    private void showChangelogDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.changelog_title);
        
        // 从raw资源文件中读取更新日志内容
        String changelogText = readRawTextFile(R.raw.changelog);
        
        // 创建ScrollView以支持滚动
        ScrollView scrollView = new ScrollView(this);
        scrollView.setPadding(30, 10, 30, 10);
        
        // 创建一个TextView来显示更新日志
        TextView textView = new TextView(this);
        textView.setPadding(0, 10, 0, 10);
        textView.setText(changelogText);
        textView.setTextIsSelectable(true); // 可以让用户选择和复制文本
        
        // 设置TextView的布局参数，确保它能正确填充ScrollView
        ScrollView.LayoutParams layoutParams = new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(layoutParams);
        
        // 将TextView添加到ScrollView中
        scrollView.addView(textView);
        
        // 设置ScrollView作为对话框的视图
        builder.setView(scrollView);
        builder.setPositiveButton(R.string.changelog_dialog_close, null);
        
        // 显示对话框
        builder.create().show();
    }
    
    /**
     * 从raw资源读取文本文件
     * @param resourceId 资源ID
     * @return 文件内容字符串
     */
    private String readRawTextFile(int resourceId) {
        StringBuilder content = new StringBuilder();
        try {
            java.io.InputStream inputStream = getResources().openRawResource(resourceId);
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream));
            
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            content.append("无法读取更新日志内容");
        }
        
        return content.toString();
    }
} 