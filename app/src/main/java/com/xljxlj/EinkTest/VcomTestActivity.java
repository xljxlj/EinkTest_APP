package com.xljxlj.EinkTest;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class VcomTestActivity extends Activity {

    private static final int GRID_SIZE = 9;
    private static final int TOTAL_BLOCKS = GRID_SIZE * GRID_SIZE;
    private static final int CENTER_INDEX = 40;

    private LinearLayout gridContainer;
    private View[][] blocks = new View[GRID_SIZE][GRID_SIZE];
    private TextView centerTextView;
    private RelativeLayout mainLayout;
    private RelativeLayout resultLayout;
    private TextView resultText;
    private Button exitButton;
    private Handler handler = new Handler();
    private int currentBlock = 0;
    private boolean testRunning = false;

    private static final String TAG = "VcomTestActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate started");

        try {
            setFullScreenImmersive();
            createUI();
            initializeGrid();
            initializeResultLayout();
            startTestSequence();

            Log.d(TAG, "onCreate completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            showErrorScreen();
        }
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

    private void createUI() {
        // 创建主布局
        mainLayout = new RelativeLayout(this);
        mainLayout.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        ));
        mainLayout.setBackgroundColor(Color.BLACK);

        // 创建网格容器
        gridContainer = new LinearLayout(this);
        gridContainer.setOrientation(LinearLayout.VERTICAL);
        RelativeLayout.LayoutParams containerParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );
        gridContainer.setLayoutParams(containerParams);
        gridContainer.setBackgroundColor(Color.BLACK);
        mainLayout.addView(gridContainer);

        // 创建结果布局（覆盖在网格上方）
        resultLayout = new RelativeLayout(this);
        RelativeLayout.LayoutParams resultParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );
        resultLayout.setLayoutParams(resultParams);
        resultLayout.setBackgroundColor(Color.TRANSPARENT); // 透明背景
        resultLayout.setVisibility(View.GONE);
        mainLayout.addView(resultLayout);

        setContentView(mainLayout);
    }

    private void initializeGrid() {
        gridContainer.removeAllViews();

        // 创建9x9网格 - 使用多个LinearLayout模拟GridLayout
        for (int i = 0; i < GRID_SIZE; i++) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1.0f
            );
            rowLayout.setLayoutParams(rowParams);
            rowLayout.setBackgroundColor(Color.BLACK);

            for (int j = 0; j < GRID_SIZE; j++) {
                final View block = new View(this);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1.0f
                );
                params.setMargins(0, 0, 0, 0);

                block.setLayoutParams(params);
                block.setBackgroundColor(Color.WHITE);

                blocks[i][j] = block;
                rowLayout.addView(block);
            }

            gridContainer.addView(rowLayout);
        }

        // 创建中心NG文字 - 透明背景，白色文字
        centerTextView = new TextView(this);
        centerTextView.setText("NG");
        centerTextView.setTextSize(36);
        centerTextView.setTextColor(Color.WHITE); // 白色文字
        centerTextView.setGravity(Gravity.CENTER);
        centerTextView.setBackgroundColor(Color.TRANSPARENT); // 透明背景

        // 将NG文字添加到主布局中，覆盖在网格上方
        RelativeLayout.LayoutParams centerParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        centerParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        centerTextView.setLayoutParams(centerParams);
        centerTextView.setVisibility(View.INVISIBLE);

        mainLayout.addView(centerTextView);

        // 为中心文字设置点击监听器
        centerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishTest();
            }
        });
    }

    private void initializeResultLayout() {
        resultLayout.removeAllViews();

        // 提示文本
        resultText = new TextView(this);
        resultText.setText("观察屏幕中心，若\"NG\"字样越深，则屏幕损坏越严重");
        resultText.setTextSize(14);
        resultText.setTextColor(Color.BLACK);
        resultText.setGravity(Color.BLACK);
        resultText.setBackgroundColor(Color.WHITE);

        RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        textParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        textParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        textParams.bottomMargin = 150; // 在退出按钮上方
        textParams.leftMargin = 20;
        textParams.rightMargin = 20;
        resultText.setLayoutParams(textParams);
        resultText.setPadding(16, 8, 16, 8);
        resultLayout.addView(resultText);

        // 退出按钮 - 使用带边框的样式
        exitButton = new Button(this);
        exitButton.setText("退出测试");
        exitButton.setTextColor(Color.BLACK);
        exitButton.setBackgroundResource(R.drawable.button_border); // 使用边框样式
        exitButton.setTextSize(14);

        RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams(
                240,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        buttonParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        buttonParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        buttonParams.bottomMargin = 50;
        exitButton.setLayoutParams(buttonParams);

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        resultLayout.addView(exitButton);
    }

    private void startTestSequence() {
        testRunning = true;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 第一步：全屏变白
                setAllBlocksColor(Color.WHITE);
                // centerTextView 保持透明背景
                centerTextView.setVisibility(View.INVISIBLE);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // 第二步：全屏变黑
                        setAllBlocksColor(Color.BLACK);
                        // centerTextView 保持透明背景
                        centerTextView.setVisibility(View.INVISIBLE);

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // 第三步：全屏变白（清除残影）
                                setAllBlocksColor(Color.WHITE);
                                // centerTextView 保持透明背景
                                centerTextView.setVisibility(View.INVISIBLE);

                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        // 开始顺序点亮测试，显示NG字样
                                        centerTextView.setVisibility(View.VISIBLE);
                                        startSequentialTest();
                                    }
                                }, 600);
                            }
                        }, 600);
                    }
                }, 600);
            }
        }, 500);
    }
    private void startSequentialTest() {
        currentBlock = 0;
        handler.post(sequentialTestRunnable);
    }

    private Runnable sequentialTestRunnable = new Runnable() {
        @Override
        public void run() {
            if (!testRunning || currentBlock >= TOTAL_BLOCKS) {
                if (currentBlock >= TOTAL_BLOCKS) {
                    // 所有块都处理完毕，同时变白
                    setAllBlocksColor(Color.WHITE);
                    // centerTextView 保持透明背景
                    testRunning = false;

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            showResultMessage();
                        }
                    }, 200);
                }
                return;
            }

            try {
                int row = currentBlock / GRID_SIZE;
                int col = currentBlock % GRID_SIZE;

                // 包括中心块在内的所有块都变黑
                if (row < GRID_SIZE && col < GRID_SIZE && blocks[row][col] != null) {
                    blocks[row][col].setBackgroundColor(Color.BLACK);
                }

                // 中心位置的NG文字保持透明背景，无需设置背景色
                currentBlock++;
                handler.postDelayed(this, 200);
            } catch (Exception e) {
                Log.e(TAG, "Error in sequentialTestRunnable: " + e.getMessage());
                testRunning = false;
            }
        }
    };

    private void setAllBlocksColor(int color) {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (blocks[i][j] != null) {
                    blocks[i][j].setBackgroundColor(color);
                }
            }
        }
    }

    private void showResultMessage() {
        if (resultLayout != null) {
            resultLayout.setVisibility(View.VISIBLE);
        }
    }

    private void finishTest() {
        if (testRunning) {
            handler.removeCallbacks(sequentialTestRunnable);
            testRunning = false;
        }
        finish();
    }

    private void showErrorScreen() {
        LinearLayout errorLayout = new LinearLayout(this);
        errorLayout.setOrientation(LinearLayout.VERTICAL);
        errorLayout.setGravity(Gravity.CENTER);
        errorLayout.setBackgroundColor(Color.WHITE);

        TextView errorText = new TextView(this);
        errorText.setText("测试功能暂不可用\n请尝试其他检测功能");
        errorText.setTextSize(18);
        errorText.setTextColor(Color.BLACK);
        errorText.setGravity(Color.WHITE);

        Button backButton = new Button(this);
        backButton.setText("返回主界面");
        backButton.setTextColor(Color.BLACK);
        backButton.setBackgroundColor(Color.WHITE);

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
        handler.removeCallbacksAndMessages(null);
    }
}