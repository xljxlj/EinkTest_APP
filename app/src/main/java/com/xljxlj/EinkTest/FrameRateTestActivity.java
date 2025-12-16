package com.xljxlj.EinkTest;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Timer;
import java.util.TimerTask;

public class FrameRateTestActivity extends Activity {

    private static final int ROWS = 10;
    private static final int COLS = 10;
    private static final int TOTAL_BLOCKS = ROWS * COLS;

    private LinearLayout gridContainer; // 改用LinearLayout作为容器
    private TextView[][] blockTexts = new TextView[ROWS][COLS];
    private TextView frameRateText;
    private Button decreaseBtn, increaseBtn, exitButton;
    private Handler handler = new Handler();
    private Timer timer;
    private int currentFrameRate = 10;
    private int currentBlock = 0;
    private boolean testRunning = true;

    private static final String TAG = "FrameRateTest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate started");

        try {
            // 设置全屏和隐藏虚拟按键
            setFullScreenImmersive();

            // 使用代码创建布局，避免XML问题
            createUI();

            initializeUI();
            initializeGrid();
            startTest();

            Log.d(TAG, "onCreate completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            showErrorScreen();
        }
    }

    private void setFullScreenImmersive() {
        try {
            // 隐藏状态栏和虚拟按键
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                View decorView = getWindow().getDecorView();
                int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
                decorView.setSystemUiVisibility(uiOptions);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                // Android 4.1-4.3 (API 16-18) 使用传统全屏方式
                View decorView = getWindow().getDecorView();
                int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN;
                decorView.setSystemUiVisibility(uiOptions);
            } else {
                // Android 4.0 及以下使用传统全屏方式
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }

            // 设置窗口标志，确保全屏（所有版本都适用）
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } catch (Exception e) {
            Log.e(TAG, "Error in setFullScreenImmersive: " + e.getMessage());
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // 重新应用沉浸式模式，确保虚拟按键保持隐藏
            setFullScreenImmersive();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 在onResume中重新应用沉浸式模式
        setFullScreenImmersive();
    }

    private void createUI() {
        // 创建主布局
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        mainLayout.setBackgroundColor(Color.WHITE);

        // 创建网格容器
        gridContainer = new LinearLayout(this);
        gridContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1  // weight = 1，占据剩余空间
        );
        gridContainer.setLayoutParams(containerParams);
        gridContainer.setBackgroundColor(Color.WHITE);
        mainLayout.addView(gridContainer);

        // 创建帧率控制布局
        LinearLayout controlLayout = new LinearLayout(this);
        controlLayout.setOrientation(LinearLayout.HORIZONTAL);
        controlLayout.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams controlParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        controlParams.setMargins(0, 16, 0, 16);
        controlLayout.setLayoutParams(controlParams);

        // 减少按钮
        decreaseBtn = new Button(this);
        decreaseBtn.setText("-");
        decreaseBtn.setTextSize(20);
        decreaseBtn.setTextColor(Color.BLACK);
        decreaseBtn.setBackgroundResource(R.drawable.button_border);
        LinearLayout.LayoutParams decreaseParams = new LinearLayout.LayoutParams(100, 100);
        decreaseParams.setMargins(0, 0, 20, 0);
        decreaseBtn.setLayoutParams(decreaseParams);
        controlLayout.addView(decreaseBtn);

        // 帧率显示
        frameRateText = new TextView(this);
        frameRateText.setText("10");
        frameRateText.setTextSize(24);
        frameRateText.setTextColor(Color.BLACK);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        textParams.setMargins(20, 0, 20, 0);
        frameRateText.setLayoutParams(textParams);
        controlLayout.addView(frameRateText);

        // 增加按钮
        increaseBtn = new Button(this);
        increaseBtn.setText("+");
        increaseBtn.setTextSize(20);
        increaseBtn.setTextColor(Color.BLACK);
        increaseBtn.setBackgroundResource(R.drawable.button_border);
        LinearLayout.LayoutParams increaseParams = new LinearLayout.LayoutParams(100, 100);
        increaseParams.setMargins(20, 0, 0, 0);
        increaseBtn.setLayoutParams(increaseParams);
        controlLayout.addView(increaseBtn);

        mainLayout.addView(controlLayout);

        // 提示文本
        TextView hintText = new TextView(this);
        hintText.setText("调节帧率数值，直到每轮刷新均能点亮每个方格");
        hintText.setTextSize(14);
        hintText.setTextColor(Color.BLACK);
        hintText.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams hintParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        hintParams.setMargins(0, 0, 0, 16);
        hintText.setLayoutParams(hintParams);
        mainLayout.addView(hintText);

        // 退出按钮
        exitButton = new Button(this);
        exitButton.setText("退出测试");
        exitButton.setTextColor(Color.BLACK);
        exitButton.setBackgroundResource(R.drawable.button_border);
        exitButton.setTextSize(14);
        LinearLayout.LayoutParams exitParams = new LinearLayout.LayoutParams(240, 100);
        exitParams.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        exitParams.setMargins(0, 0, 0, 100);
        exitButton.setLayoutParams(exitParams);
        mainLayout.addView(exitButton);

        setContentView(mainLayout);
    }

    private void initializeUI() {
        // 设置帧率文本
        updateFrameRateText();

        // 减少帧率按钮
        decreaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentFrameRate > 2) {
                    currentFrameRate--;
                    updateFrameRateText();
                    updateGridBorders();
                    restartTest();
                }
            }
        });

        // 增加帧率按钮
        increaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentFrameRate < 100) {
                    currentFrameRate++;
                    updateFrameRateText();
                    updateGridBorders();
                    restartTest();
                }
            }
        });

        // 退出按钮
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initializeGrid() {
        // 清除容器
        gridContainer.removeAllViews();

        // 创建10x10网格，使用多个LinearLayout模拟GridLayout
        for (int i = 0; i < ROWS; i++) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1.0f
            );
            rowLayout.setLayoutParams(rowParams);
            rowLayout.setBackgroundColor(Color.WHITE);

            for (int j = 0; j < COLS; j++) {
                final TextView blockText = new TextView(this);
                int blockNumber = i * COLS + j + 1; // 序列号从1开始

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1.0f
                );
                params.setMargins(1, 1, 1, 1); // 小边距形成边框效果

                blockText.setLayoutParams(params);
                blockText.setText(String.valueOf(blockNumber));

                // 添加布局完成监听器来计算合适的字体大小
                blockText.post(new Runnable() {
                    @Override
                    public void run() {
                        int blockHeight = blockText.getHeight();
                        int blockWidth = blockText.getWidth();

                        if (blockHeight > 0 && blockWidth > 0) {
                            // 计算字体大小：方块高度的2/3和方块宽度的1/2的最小值
                            float textSizeByHeight = blockHeight * 2.0f / 3.0f;
                            float textSizeByWidth = blockWidth * 1.0f / 2.0f;
                            float finalTextSize = Math.min(textSizeByHeight, textSizeByWidth);

                            // 设置字体大小（转换为sp单位）
                            float scaledTextSize = finalTextSize / getResources().getDisplayMetrics().scaledDensity;
                            blockText.setTextSize(scaledTextSize);
                        }
                    }
                });

                blockText.setTextColor(Color.BLACK);
                blockText.setGravity(Gravity.CENTER);
                blockText.setBackgroundColor(Color.WHITE);

                blockTexts[i][j] = blockText;
                rowLayout.addView(blockText);
            }

            gridContainer.addView(rowLayout);
        }

        // 初始更新边框显示
        updateGridBorders();
    }

    private void updateFrameRateText() {
        frameRateText.setText(String.valueOf(currentFrameRate));
    }

    private void updateGridBorders() {
        // 重置所有方块
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                int blockNumber = i * COLS + j + 1;
                TextView blockText = blockTexts[i][j];

                if (blockText != null) {
                    if (blockNumber <= currentFrameRate) {
                        // 启用的方块：黑色边框，黑色文字（在黑色背景上不可见）
                        blockText.setBackgroundColor(Color.BLACK);
                        blockText.setTextColor(Color.BLACK);
                    } else {
                        // 未启用的方块：白色背景，白色文字
                        blockText.setBackgroundColor(Color.WHITE);
                        blockText.setTextColor(Color.WHITE);
                    }
                }
            }
        }
    }

    private void startTest() {
        stopTest();

        timer = new Timer();
        long period = Math.max(10, 1000 / currentFrameRate); // 最小间隔10ms

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateAnimation();
                    }
                });
            }
        }, 0, period);
    }

    private void stopTest() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void restartTest() {
        currentBlock = 0;
        startTest();
    }

    private void updateAnimation() {
        if (!testRunning) return;

        try {
            // 将所有方块恢复为边框状态
            updateGridBorders();

            // 将当前方块变白
            int row = currentBlock / COLS;
            int col = currentBlock % COLS;
            if (currentBlock < currentFrameRate && row < ROWS && col < COLS) {
                TextView currentBlockText = blockTexts[row][col];
                if (currentBlockText != null) {
                    currentBlockText.setBackgroundColor(Color.WHITE);
                    // 文字保持黑色，在白色背景上可见
                }
            }

            // 更新当前方块索引
            currentBlock = (currentBlock + 1) % currentFrameRate;
        } catch (Exception e) {
            Log.e(TAG, "Error in updateAnimation: " + e.getMessage());
        }
    }

    private void showErrorScreen() {
        LinearLayout errorLayout = new LinearLayout(this);
        errorLayout.setOrientation(LinearLayout.VERTICAL);
        errorLayout.setGravity(Gravity.CENTER);
        errorLayout.setBackgroundColor(Color.WHITE);

        TextView errorText = new TextView(this);
        errorText.setText("帧率检测功能暂不可用\n请尝试其他检测功能");
        errorText.setTextSize(18);
        errorText.setTextColor(Color.BLACK);
        errorText.setGravity(Gravity.CENTER);

        Button backButton = new Button(this);
        backButton.setText("返回主界面");
        backButton.setTextColor(Color.BLACK);
        backButton.setBackgroundColor(Color.LTGRAY);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        errorLayout.addView(errorText);
        errorLayout.addView(backButton);

        setContentView(errorLayout);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        testRunning = false;
        stopTest();
        handler.removeCallbacksAndMessages(null);
    }
}