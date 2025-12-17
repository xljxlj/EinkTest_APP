package com.xljxlj.EinkTest;

import android.app.Activity;
import android.content.Context;  // 添加这个import
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Timer;
import java.util.TimerTask;

public class GhostingFrameRateTestActivity extends Activity {

    private static final int ROWS = 10;
    private static final int COLS = 10;
    private static final int TOTAL_BLOCKS = ROWS * COLS;

    private LinearLayout mainLayout;
    private LinearLayout gridContainer;
    private TextView[][] blockTexts = new TextView[ROWS][COLS];
    private TextView hintText;
    private Button startButton, exitButton;
    private LinearLayout buttonLayout;
    private Handler handler = new Handler();
    private Timer timer;
    private int currentBlock = 0;
    private boolean testRunning = false;
    private boolean testCompleted = false;
    private int screenType = 0; // 0:黑白屏幕, 1:滤光片彩色屏幕

    private static final String TAG = "GhostingFrameRateTest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate started");

        try {
            setFullScreenImmersive();
            // 显示屏幕类型选择界面
            showScreenTypeSelection();

            Log.d(TAG, "onCreate completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            showErrorScreen();
        }
    }

    private void showScreenTypeSelection() {
        // 创建主布局
        LinearLayout selectionLayout = new LinearLayout(this);
        selectionLayout.setOrientation(LinearLayout.VERTICAL);
        selectionLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        selectionLayout.setBackgroundColor(Color.WHITE);
        selectionLayout.setGravity(Gravity.CENTER);

        // 提示文本
        TextView selectionText = new TextView(this);
        selectionText.setText("选择屏幕种类");
        selectionText.setTextSize(20);
        selectionText.setTextColor(Color.BLACK);
        selectionText.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        textParams.setMargins(0, 0, 0, 50);
        selectionText.setLayoutParams(textParams);
        selectionLayout.addView(selectionText);

        // 创建按钮容器（改为垂直布局）
        LinearLayout buttonContainer = new LinearLayout(this);
        buttonContainer.setOrientation(LinearLayout.VERTICAL);
        buttonContainer.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        buttonContainer.setLayoutParams(containerParams);

        // 黑白屏幕按钮
        Button blackWhiteButton = new Button(this);
        blackWhiteButton.setText("黑白屏幕");
        blackWhiteButton.setTextColor(Color.BLACK);
        blackWhiteButton.setBackgroundResource(R.drawable.button_border);
        blackWhiteButton.setTextSize(16);

        LinearLayout.LayoutParams bwButtonParams = new LinearLayout.LayoutParams(
                300,  // 宽度设为300
                100
        );
        bwButtonParams.setMargins(0, 0, 0, 20);  // 底部边距20
        blackWhiteButton.setLayoutParams(bwButtonParams);

        blackWhiteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                screenType = 0; // 黑白屏幕模式
                startMainTest();
            }
        });

        buttonContainer.addView(blackWhiteButton);

        // 彩色屏幕按钮
        Button colorButton = new Button(this);
        colorButton.setText("滤光片彩色屏幕");
        colorButton.setTextColor(Color.BLACK);
        colorButton.setBackgroundResource(R.drawable.button_border);
        colorButton.setTextSize(16);

        LinearLayout.LayoutParams colorButtonParams = new LinearLayout.LayoutParams(
                300,  // 宽度设为300
                100
        );
        colorButton.setLayoutParams(colorButtonParams);

        colorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                screenType = 1; // 彩色屏幕模式
                startMainTest();
            }
        });

        buttonContainer.addView(colorButton);

        selectionLayout.addView(buttonContainer);
        setContentView(selectionLayout);
    }

    private void startMainTest() {
        createUI();
        initializeUI();
        initializeGrid();
        // 打开界面时自动开始测试
        startTest();
    }

    private void setFullScreenImmersive() {
        try {
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
                View decorView = getWindow().getDecorView();
                int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN;
                decorView.setSystemUiVisibility(uiOptions);
            } else {
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }

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
            setFullScreenImmersive();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setFullScreenImmersive();
    }

    private void createUI() {
        // 创建主布局
        mainLayout = new LinearLayout(this);
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
                1
        );
        gridContainer.setLayoutParams(containerParams);
        gridContainer.setBackgroundColor(Color.WHITE);
        mainLayout.addView(gridContainer);

        // 提示文本
        hintText = new TextView(this);
        hintText.setText("查看有多少格留下残影");
        hintText.setTextSize(14);
        hintText.setTextColor(Color.BLACK);
        hintText.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams hintParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        hintParams.setMargins(0, 16, 0, 16);
        hintText.setLayoutParams(hintParams);
        mainLayout.addView(hintText);

        // 按钮布局
        buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        buttonLayoutParams.setMargins(0, 0, 0, 100);
        buttonLayout.setLayoutParams(buttonLayoutParams);

        // 开始测试按钮
        startButton = new Button(this);
        startButton.setText("开始测试");
        startButton.setTextColor(Color.BLACK);
        startButton.setBackgroundResource(R.drawable.button_border);
        startButton.setTextSize(14);
        LinearLayout.LayoutParams startParams = new LinearLayout.LayoutParams(
                200,
                100
        );
        startParams.setMargins(0, 0, 50, 0);
        startButton.setLayoutParams(startParams);
        buttonLayout.addView(startButton);

        // 退出按钮
        exitButton = new Button(this);
        exitButton.setText("退出测试");
        exitButton.setTextColor(Color.BLACK);
        exitButton.setBackgroundResource(R.drawable.button_border);
        exitButton.setTextSize(14);
        LinearLayout.LayoutParams exitParams = new LinearLayout.LayoutParams(
                200,
                100
        );
        exitButton.setLayoutParams(exitParams);
        buttonLayout.addView(exitButton);

        mainLayout.addView(buttonLayout);

        setContentView(mainLayout);
    }

    private void initializeUI() {
        // 开始测试按钮
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTest();
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

        // 先获取屏幕信息
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        android.view.Display display = windowManager.getDefaultDisplay();
        android.graphics.Point size = new android.graphics.Point();
        display.getSize(size);

        // 计算每个格子的大小（基于屏幕宽度）
        int screenWidth = size.x;
        int screenHeight = size.y;

        // 预留空间给按钮和提示文本（大约200像素）
        int availableHeight = screenHeight - 200;
        int cellWidth = (screenWidth - (COLS + 1) * 1) / COLS;
        int cellHeight = (availableHeight - (ROWS + 1) * 1) / ROWS;

        Log.d(TAG, "Screen: " + screenWidth + "x" + screenHeight +
                ", Cell: " + cellWidth + "x" + cellHeight);

        // 确保最小尺寸
        if (cellWidth < 10) cellWidth = 10;
        if (cellHeight < 10) cellHeight = 10;

        // 创建共享图案
        Bitmap sharedPatternBitmap = null;
        if (screenType == 0) {
            sharedPatternBitmap = createDiagonalPatternBitmap(cellWidth, cellHeight);
        }

        // 计算字体大小
        float textSizeByHeight = cellHeight * 2.0f / 3.0f;
        float textSizeByWidth = cellWidth * 1.0f / 2.0f;
        float finalTextSize = Math.min(textSizeByHeight, textSizeByWidth);
        float scaledTextSize = finalTextSize / getResources().getDisplayMetrics().scaledDensity;

        // 创建网格
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
                TextView blockText = new TextView(this);
                int blockNumber = i * COLS + j + 1;

                // 关键：使用固定尺寸而不是权重
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        cellWidth,
                        cellHeight
                );
                params.setMargins(1, 1, 1, 1);
                // 居中显示
                params.gravity = Gravity.CENTER;

                blockText.setLayoutParams(params);
                blockText.setText(String.valueOf(blockNumber));

                // 设置背景
                if (screenType == 0 && sharedPatternBitmap != null) {
                    android.graphics.drawable.BitmapDrawable drawable =
                            new android.graphics.drawable.BitmapDrawable(getResources(), sharedPatternBitmap);
                    drawable.setTargetDensity(getResources().getDisplayMetrics().densityDpi);
                    blockText.setBackground(drawable);
                } else if (screenType == 1) {
                    blockText.setBackgroundColor(Color.BLUE);
                }

                // 初始设置为透明（不可见）
                blockText.setTextColor(Color.TRANSPARENT);
                blockText.setGravity(Gravity.CENTER);
                blockText.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
                blockText.setTextSize(scaledTextSize);

                blockTexts[i][j] = blockText;
                rowLayout.addView(blockText);
            }

            gridContainer.addView(rowLayout);
        }
    }

    private Bitmap createDiagonalPatternBitmap(int width, int height) {
        if (width <= 0 || height <= 0) {
            return null;
        }

        try {
            // 创建与格子大小完全相同的位图
            Bitmap patternBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(patternBitmap);

            // 绘制白色背景
            canvas.drawColor(Color.WHITE);

            // 创建黑色画笔
            android.graphics.Paint blackPaint = new android.graphics.Paint();
            blackPaint.setColor(Color.BLACK);
            blackPaint.setStyle(android.graphics.Paint.Style.FILL);
            blackPaint.setAntiAlias(false);
            blackPaint.setFilterBitmap(false);

            // 绘制斜线图案
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if ((x + y) % 2 == 0) {
                        canvas.drawRect(x, y, x + 1, y + 1, blackPaint);
                    }
                }
            }

            return patternBitmap;

        } catch (OutOfMemoryError e) {
            Log.e(TAG, "Out of memory creating pattern bitmap: " + width + "x" + height);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error creating pattern bitmap: " + e.getMessage());
            return null;
        }
    }

    private void startTest() {
        if (testRunning) return;

        testRunning = true;
        testCompleted = false;
        currentBlock = 0;

        // 隐藏所有UI元素进行全屏刷新
        hideAllUI();

        // 清除残影序列
        startClearSequence();
    }

    private void hideAllUI() {
        // 隐藏网格、提示文本和按钮
        gridContainer.setVisibility(View.INVISIBLE);
        hintText.setVisibility(View.INVISIBLE);
        buttonLayout.setVisibility(View.INVISIBLE);

        // 设置全屏背景色
        mainLayout.setBackgroundColor(Color.WHITE);
    }

    private void showAllUI() {
        // 显示所有UI元素
        gridContainer.setVisibility(View.VISIBLE);
        hintText.setVisibility(View.VISIBLE);
        buttonLayout.setVisibility(View.VISIBLE);

        // 恢复背景色
        mainLayout.setBackgroundColor(Color.WHITE);
    }

    private void startClearSequence() {
        stopTest();

        final int[] clearStep = {0};

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateClearSequence(clearStep);
                    }
                });
            }
        }, 0, 600); // 600ms切换一次
    }

    private void updateClearSequence(int[] clearStep) {
        switch (clearStep[0]) {
            case 0:
                // 第一步：全屏变白
                mainLayout.setBackgroundColor(Color.WHITE);
                clearStep[0] = 1;
                break;
            case 1:
                // 第二步：全屏变黑
                mainLayout.setBackgroundColor(Color.BLACK);
                clearStep[0] = 2;
                break;
            case 2:
                // 第三步：全屏变白（清除残影）
                mainLayout.setBackgroundColor(Color.WHITE);
                clearStep[0] = 3;
                break;
            case 3:
                // 清除序列完成，开始残影测试
                showAllUI();
                clearStep[0] = 4;
                break;
            case 4:
                stopTest();
                startGhostingTest();
                break;
        }
    }

    private void startGhostingTest() {
        stopTest();

        timer = new Timer();
        // 固定10ms切换周期
        long period = 10;

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateGhostingAnimation();
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

    private void updateGhostingAnimation() {
        if (!testRunning) return;

        try {
            // 将所有方块的文字设为透明（不可见）
            setAllBlocksTextColor(Color.TRANSPARENT);

            // 将当前方块的文字设为黑色
            int row = currentBlock / COLS;
            int col = currentBlock % COLS;
            if (row < ROWS && col < COLS) {
                TextView currentBlockText = blockTexts[row][col];
                if (currentBlockText != null) {
                    currentBlockText.setTextColor(Color.BLACK);
                }
            }

            // 更新当前方块索引
            currentBlock++;

            // 检查是否完成一个周期（100个方块）
            if (currentBlock >= TOTAL_BLOCKS) {
                stopTest();
                testRunning = false;
                testCompleted = true;

                // 测试完成后将所有方块的文字变透明，观察残影
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setAllBlocksTextColor(Color.TRANSPARENT);
                    }
                }, 100);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in updateGhostingAnimation: " + e.getMessage());
            stopTest();
            testRunning = false;
        }
    }

    private void setAllBlocksTextColor(int color) {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (blockTexts[i][j] != null) {
                    blockTexts[i][j].setTextColor(color);
                }
            }
        }
    }

    private void showErrorScreen() {
        LinearLayout errorLayout = new LinearLayout(this);
        errorLayout.setOrientation(LinearLayout.VERTICAL);
        errorLayout.setGravity(Gravity.CENTER);
        errorLayout.setBackgroundColor(Color.WHITE);

        TextView errorText = new TextView(this);
        errorText.setText("残影检测功能暂不可用\n请尝试其他检测功能");
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