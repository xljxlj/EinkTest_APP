package com.xljxlj.EinkTest;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends Activity {

    private LinearLayout testItemsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置全屏，隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        initializeUI();
        setupTestItems();
    }

    private void initializeUI() {
        testItemsLayout = findViewById(R.id.testItemsLayout);
    }

    private void setupTestItems() {
        // 创建VCOM连接性检测按钮
        addTestButton("VCOM连接性检测", VcomTestActivity.class);

        // 创建屏幕坏点/安装位置检测按钮
        addTestButton("屏幕坏点/安装位置检测", ScreenDefectTestActivity.class);

        // 创建帧率检测（残影）按钮
        addTestButton("屏幕帧率检测（残影法，推荐使用）", GhostingFrameRateTestActivity.class);

        // 创建屏幕帧率检测（方块）按钮
        addTestButton("屏幕帧率检测（方块）", FrameRateTestActivity.class);

        // 创建屏幕帧率检测（圆环）按钮
        addTestButton("屏幕帧率检测（圆环）", CircleFrameRateTestActivity.class);

        // 创建方格刷新测试按钮
        addTestButton("方格刷新测试", CheckerboardRefreshTestActivity.class);

        // 创建灰度测试按钮，灰度增加上下灰度变换
        addTestButton("灰度测试", GrayscaleTestActivity.class);
    }

    private void addTestButton(String buttonText, final Class<?> activityClass) {
        Button testBtn = new Button(this);
        testBtn.setText(buttonText);
        testBtn.setTextSize(16);
        testBtn.setTextColor(Color.BLACK);
        testBtn.setBackgroundResource(R.drawable.button_border);
        testBtn.setPadding(32, 32, 32, 32);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 32);
        testBtn.setLayoutParams(params);

        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, activityClass);
                startActivity(intent);
            }
        });

        testItemsLayout.addView(testBtn);
    }
}