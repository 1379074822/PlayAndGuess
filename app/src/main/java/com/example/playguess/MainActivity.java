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
        LinearLayout layoutCustomDuration = dialogView.findViewById(R.id.layoutCustomDuration);
        EditText editTextCustomDuration = dialogView.findViewById(R.id.editTextCustomDuration);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        Button buttonConfirm = dialogView.findViewById(R.id.buttonConfirm);
        
        // 获取当前设置的游戏时长
        int currentDuration = settingsManager.getGameDuration();
        
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
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
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
                
                // 保存设置
                settingsManager.setGameDuration(selectedDuration);
                Toast.makeText(MainActivity.this, R.string.settings_saved, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        
        // 显示对话框
        dialog.show();
    }
} 