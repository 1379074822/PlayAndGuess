package com.example.playguess;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;

import com.example.playguess.manager.GameSettingsManager;
import com.example.playguess.manager.WordLibraryManager;
import com.example.playguess.model.WordLibrary;
import com.example.playguess.model.WordResult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class GamePlayActivity extends BaseActivity implements GestureDetector.OnGestureListener, SensorEventListener {

    // 常量定义
    private static final String TAG = "GamePlayActivity";
    private static final String EXTRA_LIBRARY_ID = "extra_library_id";
    private static final long GAME_DURATION_MS = 60000; // 游戏持续时间，1分钟
    private static final long BUFFER_DURATION_MS = 3000; // 缓冲时间，3秒
    private static final int DEFAULT_ANIMATION_DURATION = 300; // 动画持续时间，300毫秒
    private static final float MAX_DISTANCE = 0.4f; // 最大滑动距离为屏幕的40%
    private static final int MIN_VELOCITY = 200; // 最小滑动速度
    
    // 传感器相关常量
    private static final float FLIP_THRESHOLD = 7.0f; // 翻转阈值，重力加速度变化
    private static final long SENSOR_COOLDOWN_MS = 1000; // 传感器冷却时间，防止连续触发
    
    // 传感器相关
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean sensorEnabled = false;
    private static final float FLIP_DOWN_THRESHOLD = -9.0f; // 向下翻转阈值
    private static final float FLIP_UP_THRESHOLD = 9.0f;    // 向上翻转阈值
    private static final float NORMAL_POSITION_THRESHOLD = 5.0f; // 正常位置阈值
    private static final long SENSOR_COOLDOWN = 1000;       // 传感器冷却时间（毫秒）
    private long lastSensorActionTime = 0;                  // 上次传感器动作时间
    private boolean isFlippedDown = false;                  // 是否处于向下翻转状态
    private boolean isFlippedUp = false;                    // 是否处于向上翻转状态
    
    // 视图对象
    private TextView textViewTimer;
    private TextView textViewLibraryTitle;
    private TextView textViewWord;
    private TextView textViewSkipHint;
    private TextView textViewCorrectHint;
    private TextView textViewBuffering;
    private View viewSkipArea;
    private View viewCorrectArea;
    private ImageButton buttonPause;

    // 手势检测器
    private GestureDetectorCompat gestureDetector;

    // 数据
    private WordLibrary wordLibrary;
    private List<String> wordList;
    private ArrayList<WordResult> wordResults;
    private int currentWordIndex = 0;
    private int gameTimeInSeconds;
    private boolean gameInProgress = false;
    private int correctCount = 0;
    private int skippedCount = 0;
    private CountDownTimer gameTimer;
    private CountDownTimer bufferTimer; // 用于缓冲倒计时
    private Random random = new Random();
    private long timeRemaining; // 保存暂停时剩余的时间
    private boolean isPaused = false; // 游戏是否处于暂停状态
    private boolean isBuffering = false; // 是否处于缓冲倒计时阶段
    private Dialog pauseDialog; // 暂停对话框

    // 传感器事件监听器
    private final SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && gameInProgress) {
                float z = event.values[2]; // Z轴数据
                long currentTime = System.currentTimeMillis();
                
                // 检测手机是否回到正常位置（既不是向上翻转也不是向下翻转的状态）
                if (Math.abs(z) < NORMAL_POSITION_THRESHOLD) {
                    isFlippedDown = false;
                    isFlippedUp = false;
                    return;
                }
                
                // 检查是否超过冷却时间，避免连续触发
                if (currentTime - lastSensorActionTime > SENSOR_COOLDOWN) {
                    // 向下翻转手机（屏幕朝下）- 表示猜对了
                    if (z < FLIP_DOWN_THRESHOLD && !isFlippedDown) {
                        isFlippedDown = true;
                        lastSensorActionTime = currentTime;
                        Log.d(TAG, "检测到向下翻转动作，z值: " + z);
                        runOnUiThread(() -> wordGuessedCorrectly());
                    }
                    
                    // 向上翻转手机（屏幕朝上抬起）- 表示跳过
                    else if (z > FLIP_UP_THRESHOLD && !isFlippedUp) {
                        isFlippedUp = true;
                        lastSensorActionTime = currentTime;
                        Log.d(TAG, "检测到向上翻转动作，z值: " + z);
                        runOnUiThread(() -> wordSkipped());
                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // 传感器精度变化时的处理，通常可以忽略
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_play);

        // 初始化视图
        initViews();

        // 初始化手势检测器
        gestureDetector = new GestureDetectorCompat(this, this);

        // 获取传递的数据
        String libraryId = getIntent().getStringExtra(EXTRA_LIBRARY_ID);
        if (libraryId == null) {
            Toast.makeText(this, "词库加载失败", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 加载词库
        loadWordLibrary(libraryId);

        // 初始化游戏数据
        initGameData();

        // 初始化传感器
        initSensors();

        // 开始缓冲倒计时
        startBuffering();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        textViewTimer = findViewById(R.id.textViewTimer);
        textViewLibraryTitle = findViewById(R.id.textViewLibraryTitle);
        textViewWord = findViewById(R.id.textViewWord);
        textViewSkipHint = findViewById(R.id.textViewSkipHint);
        textViewCorrectHint = findViewById(R.id.textViewCorrectHint);
        textViewBuffering = findViewById(R.id.textViewBuffering);
        viewSkipArea = findViewById(R.id.viewSkipArea);
        viewCorrectArea = findViewById(R.id.viewCorrectArea);
        buttonPause = findViewById(R.id.buttonPause);

        // 隐藏滑动提示
        textViewSkipHint.setVisibility(View.GONE);
        textViewCorrectHint.setVisibility(View.GONE);
        viewSkipArea.setVisibility(View.GONE);
        viewCorrectArea.setVisibility(View.GONE);

        // 初始化词条文本视图
        textViewWord.setVisibility(View.INVISIBLE);
        textViewWord.setAlpha(0f); // 确保透明度初始化

        // 显示加载中
        textViewBuffering.setVisibility(View.VISIBLE);
        textViewBuffering.setText(R.string.word_hint);
        
        // 设置暂停按钮点击事件
        buttonPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseGame();
            }
        });
    }

    /**
     * 加载词库
     */
    private void loadWordLibrary(String libraryId) {
        WordLibraryManager libraryManager = WordLibraryManager.getInstance(this);
        wordLibrary = libraryManager.getLibraryById(libraryId);

        if (wordLibrary == null || wordLibrary.getWords() == null || wordLibrary.getWords().isEmpty()) {
            Toast.makeText(this, "词库为空", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 显示词库标题
        textViewLibraryTitle.setText(wordLibrary.getTitle());
    }

    /**
     * 初始化游戏数据
     */
    private void initGameData() {
        // 获取游戏时长设置
        gameTimeInSeconds = GameSettingsManager.getInstance(this).getGameDuration();
        
        // 复制词库中的词语，确保不包含重复词语
        wordList = new ArrayList<>();
        
        // 使用HashSet去重
        HashSet<String> uniqueWords = new HashSet<>(wordLibrary.getWords());
        wordList.addAll(uniqueWords);
        
        // 打乱顺序
        Collections.shuffle(wordList);

        // 初始化结果列表
        wordResults = new ArrayList<>();

        // 更新时间显示
        updateTimerText(gameTimeInSeconds);
    }

    /**
     * 初始化传感器
     */
    private void initSensors() {
        // 初始化传感器管理器
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer != null) {
                sensorEnabled = true;
                Log.d(TAG, "加速度传感器初始化成功");
            } else {
                sensorEnabled = false;
                Log.e(TAG, "设备不支持加速度传感器");
                Toast.makeText(this, "您的设备不支持加速度传感器，将使用触摸操作", Toast.LENGTH_LONG).show();
            }
        } else {
            sensorEnabled = false;
            Log.e(TAG, "无法获取传感器服务");
        }
    }

    /**
     * 开始缓冲倒计时
     */
    private void startBuffering() {
        isBuffering = true;
        
        bufferTimer = new CountDownTimer(BUFFER_DURATION_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // 显示倒计时
                textViewBuffering.setText(String.valueOf(millisUntilFinished / 1000 + 1));
            }

            @Override
            public void onFinish() {
                isBuffering = false;
                
                // 隐藏缓冲提示，显示游戏元素
                textViewBuffering.setVisibility(View.GONE);
                textViewWord.setVisibility(View.VISIBLE);
                
                // 开始游戏
                startGame();
            }
        };
        bufferTimer.start();
    }

    /**
     * 开始游戏
     */
    private void startGame() {
        // 标记游戏开始
        gameInProgress = true;
        isPaused = false;

        // 提前重置文本视图状态
        textViewWord.setTranslationX(0f);
        textViewWord.setAlpha(1f);
        textViewWord.setVisibility(View.VISIBLE);

        // 显示第一个词语
        showNextWord();

        // 开始游戏计时器
        gameTimer = new CountDownTimer(gameTimeInSeconds * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // 保存剩余时间（用于暂停）
                timeRemaining = millisUntilFinished;
                // 更新时间显示
                updateTimerText((int) (millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                // 游戏结束
                endGame();
            }
        }.start();
        
        // 记录游戏开始
        Log.d(TAG, "游戏正式开始，第一个词条已显示");
    }

    /**
     * 更新时间显示
     */
    private void updateTimerText(int seconds) {
        // 格式化为分:秒
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        textViewTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, remainingSeconds));
    }

    /**
     * 显示下一个词语
     */
    private void showNextWord() {
        if (currentWordIndex < wordList.size()) {
            String word = wordList.get(currentWordIndex);
            textViewWord.setText(word);
            
            // 确保文本视图是可见的
            textViewWord.setVisibility(View.VISIBLE);
            
            // 显示单词的动画效果
            textViewWord.setAlpha(0f);
            textViewWord.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            // 确保动画结束后词条是可见的
                            textViewWord.setAlpha(1f);
                        }
                    })
                    .start();
            
            // 500ms后再次确认词条可见性
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (textViewWord != null && textViewWord.getAlpha() < 1f) {
                        textViewWord.setAlpha(1f);
                    }
                }
            }, 500);
            
            // 记录日志
            Log.d(TAG, "显示词条: " + word);
        } else {
            // 所有词语已经用完，游戏结束
            endGame();
        }
    }

    /**
     * 处理猜对的词语
     */
    private void wordGuessedCorrectly() {
        if (!gameInProgress) return;
        
        String currentWord = wordList.get(currentWordIndex);
        WordResult result = new WordResult(currentWord, true, false);
        wordResults.add(result);
        correctCount++;
        
        // 输出调试信息
        Log.d(TAG, "猜对: " + currentWord + ", 总计猜对: " + correctCount + ", 总列表大小: " + wordResults.size());
        
        // 显示猜对提示
        Toast.makeText(this, "猜对了：" + currentWord, Toast.LENGTH_SHORT).show();
        
        // 显示下一个词语
        currentWordIndex++;
        showWordChangeAnimation(true);
    }

    /**
     * 处理跳过的词语
     */
    private void wordSkipped() {
        if (!gameInProgress) return;
        
        String currentWord = wordList.get(currentWordIndex);
        WordResult result = new WordResult(currentWord, false, true);
        wordResults.add(result);
        skippedCount++;
        
        // 输出调试信息
        Log.d(TAG, "跳过: " + currentWord + ", 总计跳过: " + skippedCount + ", 总列表大小: " + wordResults.size());
        
        // 显示跳过提示
        Toast.makeText(this, "跳过：" + currentWord, Toast.LENGTH_SHORT).show();
        
        // 显示下一个词语
        currentWordIndex++;
        showWordChangeAnimation(false);
    }

    /**
     * 显示词语切换动画
     */
    private void showWordChangeAnimation(boolean correct) {
        // 向左或向右滑出
        float direction = correct ? -1000f : 1000f;
        
        final ObjectAnimator animator = ObjectAnimator.ofFloat(textViewWord, "translationX", 0f, direction);
        animator.setDuration(200);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // 重置位置并显示下一个词
                textViewWord.setTranslationX(-direction);
                showNextWord();
                
                // 滑入动画
                ObjectAnimator slideIn = ObjectAnimator.ofFloat(textViewWord, "translationX", -direction, 0f);
                slideIn.setDuration(200);
                slideIn.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        // 确保滑入动画结束后文字可见
                        textViewWord.setTranslationX(0f);
                        textViewWord.setAlpha(1f);
                    }
                });
                slideIn.start();
            }
            
            @Override
            public void onAnimationCancel(Animator animation) {
                // 如果动画被取消，确保重置位置
                textViewWord.setTranslationX(0f);
                textViewWord.setAlpha(1f);
            }
        });
        animator.start();
    }

    /**
     * 暂停游戏
     */
    private void pauseGame() {
        // 允许在缓冲倒计时阶段或游戏进行阶段暂停
        if ((isBuffering || gameInProgress) && !isPaused) {
            // 暂停游戏状态
            isPaused = true;
            
            // 在缓冲阶段暂停
            if (isBuffering && bufferTimer != null) {
                bufferTimer.cancel();
            }
            
            // 在游戏进行阶段暂停
            if (gameInProgress && gameTimer != null) {
                gameTimer.cancel();
            }
            
            // 显示暂停对话框
            showPauseDialog();
        }
    }

    /**
     * 显示暂停对话框
     */
    private void showPauseDialog() {
        pauseDialog = new Dialog(this);
        pauseDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        pauseDialog.setContentView(R.layout.dialog_pause_menu);
        pauseDialog.setCancelable(false);

        // 设置对话框宽度
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(pauseDialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        pauseDialog.getWindow().setAttributes(lp);

        // 继续游戏按钮
        Button buttonResume = pauseDialog.findViewById(R.id.buttonResume);
        buttonResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resumeGame();
            }
        });

        // 重新开始按钮
        Button buttonRestart = pauseDialog.findViewById(R.id.buttonRestart);
        buttonRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restartGame();
            }
        });

        // 返回词库选择按钮
        Button buttonBackToLibrary = pauseDialog.findViewById(R.id.buttonBackToLibrary);
        buttonBackToLibrary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToLibrary();
            }
        });

        pauseDialog.show();
    }

    /**
     * 继续游戏
     */
    private void resumeGame() {
        if (!isPaused) return;

        // 关闭暂停对话框
        if (pauseDialog != null && pauseDialog.isShowing()) {
            pauseDialog.dismiss();
        }

        // 恢复游戏状态
        isPaused = false;
        
        // 根据当前阶段恢复相应的计时器
        if (isBuffering) {
            // 重新开始缓冲倒计时
            startBuffering();
        } else if (gameInProgress) {
            // 重新开始游戏计时器，从暂停时的剩余时间继续
            gameTimer = new CountDownTimer(timeRemaining, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    timeRemaining = millisUntilFinished;
                    updateTimerText((int) (millisUntilFinished / 1000));
                }

                @Override
                public void onFinish() {
                    endGame();
                }
            }.start();
        }
    }

    /**
     * 重新开始游戏
     */
    private void restartGame() {
        // 关闭暂停对话框
        if (pauseDialog != null && pauseDialog.isShowing()) {
            pauseDialog.dismiss();
        }

        // 重置游戏数据
        currentWordIndex = 0;
        correctCount = 0;
        skippedCount = 0;
        wordResults.clear();
        
        // 重新打乱词语顺序
        Collections.shuffle(wordList);
        
        // 重置状态
        isPaused = false;
        isBuffering = false;
        gameInProgress = false;
        
        // 取消当前计时器
        if (gameTimer != null) {
            gameTimer.cancel();
        }
        if (bufferTimer != null) {
            bufferTimer.cancel();
        }
        
        // 重置UI
        textViewWord.setVisibility(View.INVISIBLE);
        textViewBuffering.setVisibility(View.VISIBLE);
        updateTimerText(gameTimeInSeconds);
        
        // 重新开始游戏，从缓冲倒计时开始
        startBuffering();
    }

    /**
     * 返回词库选择页面
     */
    private void backToLibrary() {
        // 关闭暂停对话框
        if (pauseDialog != null && pauseDialog.isShowing()) {
            pauseDialog.dismiss();
        }
        
        // 取消当前计时器
        if (gameTimer != null) {
            gameTimer.cancel();
        }
        
        // 直接结束当前活动，返回到词库选择页面
        finish();
    }

    /**
     * 结束游戏
     */
    private void endGame() {
        gameInProgress = false;
        
        // 取消计时器
        if (gameTimer != null) {
            gameTimer.cancel();
        }
        
        // 记录游戏结果
        Log.d(TAG, "游戏结束，猜对: " + correctCount + "个, 跳过: " + skippedCount + "个");
        
        // 创建结果列表
        ArrayList<WordResult> results = new ArrayList<>();
        
        // 为已经处理过的单词创建WordResult对象
        for (int i = 0; i < wordList.size() && i <= currentWordIndex; i++) {
            String word = wordList.get(i);
            
            // 搜索这个词在wordResults列表中是否存在
            boolean isCorrect = false;
            boolean isSkipped = false;
            
            for (WordResult existingResult : wordResults) {
                if (existingResult.getWord().equals(word)) {
                    isCorrect = existingResult.isCorrect();
                    isSkipped = existingResult.isSkipped();
                    break;
                }
            }
            
            // 创建新的WordResult对象
            WordResult newResult = new WordResult(word, isCorrect, isSkipped);
            results.add(newResult);
        }
        
        // 跳转到结果页面
        Intent intent = new Intent(this, GameResultActivity.class);
        intent.putExtra(GameResultActivity.EXTRA_WORD_RESULTS, results);
        intent.putExtra(GameResultActivity.EXTRA_CORRECT_COUNT, correctCount);
        intent.putExtra(GameResultActivity.EXTRA_SKIPPED_COUNT, skippedCount);
        intent.putExtra(GameResultActivity.EXTRA_LIBRARY_ID, wordLibrary.getId());
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 将触摸事件传递给手势检测器
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    // 实现GestureDetector.OnGestureListener接口方法
    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        // 不需要实现
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        // 不需要实现
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (!gameInProgress) return false;
        
        float diffY = e2.getY() - e1.getY();
        float diffX = e2.getX() - e1.getX();
        
        // 确保是垂直方向的滑动（Y方向的距离大于X方向）
        if (Math.abs(diffY) > Math.abs(diffX)) {
            // 向下滑动 - 跳过
            if (diffY > FLIP_THRESHOLD && Math.abs(velocityY) > MIN_VELOCITY) {
                wordSkipped();
                return true;
            }
            // 向上滑动 - 猜对
            else if (-diffY > FLIP_THRESHOLD && Math.abs(velocityY) > MIN_VELOCITY) {
                wordGuessedCorrectly();
                return true;
            }
        }
        
        return false;
    }
    
    // 实现SensorEventListener接口方法
    @Override
    public void onSensorChanged(SensorEvent event) {
        // 不做任何处理，因为我们使用了单独的SensorEventListener实例
    }
    
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // 不做任何处理，传感器精度变化时的回调
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // 注册传感器监听器
        if (sensorEnabled && sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_GAME);
            Log.d(TAG, "传感器监听器已注册");
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
        // 取消注册传感器监听器，避免电池消耗
        if (sensorManager != null) {
            sensorManager.unregisterListener(sensorEventListener);
            Log.d(TAG, "传感器监听器已取消注册");
        }
        
        // 如果游戏正在进行，自动暂停
        if ((isBuffering || gameInProgress) && !isPaused) {
            pauseGame();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 取消所有计时器
        if (gameTimer != null) {
            gameTimer.cancel();
            gameTimer = null;
        }
        if (bufferTimer != null) {
            bufferTimer.cancel();
            bufferTimer = null;
        }
        
        // 关闭对话框
        if (pauseDialog != null && pauseDialog.isShowing()) {
            pauseDialog.dismiss();
            pauseDialog = null;
        }
        
        // 清理动画
        if (textViewWord != null) {
            textViewWord.clearAnimation();
            textViewWord.animate().cancel();
        }
    }
    
    /**
     * 启动游戏活动的静态方法
     */
    public static void start(AppCompatActivity activity, String libraryId) {
        Intent intent = new Intent(activity, GamePlayActivity.class);
        intent.putExtra(EXTRA_LIBRARY_ID, libraryId);
        activity.startActivity(intent);
    }
}