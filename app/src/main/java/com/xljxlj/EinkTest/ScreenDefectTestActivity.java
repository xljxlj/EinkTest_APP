package com.xljxlj.EinkTest;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;

public class ScreenDefectTestActivity extends Activity {

    private RelativeLayout mainLayout;
    private int screenState = 0; // 0:白屏, 1:黑屏, 2:边框检测

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置全屏和隐藏虚拟按键
        setFullScreenImmersive();

        setContentView(R.layout.activity_screen_defect_test);

        initializeUI();
        showWhiteScreen();
    }

    private void setFullScreenImmersive() {
        // 隐藏状态栏和虚拟按键
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Android 4.4 (API 19) 及以上使用沉浸式模式
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

    private void initializeUI() {
        mainLayout = findViewById(R.id.mainLayout);

        // 设置点击监听器
        mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (screenState) {
                    case 0:
                        showBlackScreen();
                        break;
                    case 1:
                        showBorderTest();
                        break;
                    case 2:
                        // 点击边框测试界面时回到白屏
                        showWhiteScreen();
                        break;
                }
            }
        });
    }

    private void showWhiteScreen() {
        screenState = 0;
        mainLayout.setBackgroundColor(Color.WHITE);
        // 移除所有子视图
        mainLayout.removeAllViews();
    }

    private void showBlackScreen() {
        screenState = 1;
        mainLayout.setBackgroundColor(Color.BLACK);
        // 移除所有子视图
        mainLayout.removeAllViews();
    }

    private void showBorderTest() {
        screenState = 2;
        mainLayout.setBackgroundColor(Color.WHITE);
        mainLayout.removeAllViews();

        // 添加退出按钮（宽度增大一倍）
        Button exitButton = new Button(this);
        exitButton.setText("退出测试");
        exitButton.setTextColor(Color.BLACK);
        exitButton.setBackgroundResource(R.drawable.button_border);
        exitButton.setTextSize(14);

        RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams(
                240, // 宽度增大一倍
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        buttonParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        exitButton.setLayoutParams(buttonParams);

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mainLayout.addView(exitButton);

        // 绘制五圈边框，间隔2像素
        drawBorders();
    }

    private void drawBorders() {
        // 绘制5圈边框，每圈间隔2像素
        for (int i = 0; i < 5; i++) {
            int margin = 2 + (i * 4); // 2, 6, 10, 14, 18像素
            drawSingleBorder(margin);
        }
    }

    private void drawSingleBorder(int margin) {
        // 上边框
        View borderTop = new View(this);
        borderTop.setBackgroundColor(Color.BLACK);
        RelativeLayout.LayoutParams paramsTop = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                1
        );
        paramsTop.topMargin = margin;
        paramsTop.leftMargin = margin;
        paramsTop.rightMargin = margin;
        borderTop.setLayoutParams(paramsTop);
        mainLayout.addView(borderTop);

        // 下边框
        View borderBottom = new View(this);
        borderBottom.setBackgroundColor(Color.BLACK);
        RelativeLayout.LayoutParams paramsBottom = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                1
        );
        paramsBottom.bottomMargin = margin;
        paramsBottom.leftMargin = margin;
        paramsBottom.rightMargin = margin;
        paramsBottom.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        borderBottom.setLayoutParams(paramsBottom);
        mainLayout.addView(borderBottom);

        // 左边框
        View borderLeft = new View(this);
        borderLeft.setBackgroundColor(Color.BLACK);
        RelativeLayout.LayoutParams paramsLeft = new RelativeLayout.LayoutParams(
                1,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );
        paramsLeft.topMargin = margin;
        paramsLeft.bottomMargin = margin;
        paramsLeft.leftMargin = margin;
        borderLeft.setLayoutParams(paramsLeft);
        mainLayout.addView(borderLeft);

        // 右边框
        View borderRight = new View(this);
        borderRight.setBackgroundColor(Color.BLACK);
        RelativeLayout.LayoutParams paramsRight = new RelativeLayout.LayoutParams(
                1,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );
        paramsRight.topMargin = margin;
        paramsRight.bottomMargin = margin;
        paramsRight.rightMargin = margin;
        paramsRight.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        borderRight.setLayoutParams(paramsRight);
        mainLayout.addView(borderRight);
    }
}