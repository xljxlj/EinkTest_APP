package com.xljxlj.EinkTest;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Timer;
import java.util.TimerTask;

public class CircleFrameRateTestActivity extends Activity {

    private CircleFrameRateView circleView;
    private TextView frameRateText;
    private Button decreaseBtn, increaseBtn, exitButton;
    private Handler handler = new Handler();
    private Timer timer;
    private int currentFrameRate = 10;
    private int currentSegment = 0;
    private boolean testRunning = true;
    private long lastUpdateTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置全屏和隐藏虚拟按键
        setFullScreenImmersive();

        // 使用代码创建布局，避免XML问题
        createUI();

        initializeUI();
        startTest();
    }

    private void setFullScreenImmersive() {
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

        // 创建圆环视图 - 增加权重，占据更多空间
        circleView = new CircleFrameRateView(this);
        LinearLayout.LayoutParams circleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1  // weight = 1，占据剩余空间
        );
        circleView.setLayoutParams(circleParams);
        mainLayout.addView(circleView);

        // 创建帧率控制布局
        LinearLayout controlLayout = new LinearLayout(this);
        controlLayout.setOrientation(LinearLayout.HORIZONTAL);
        controlLayout.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams controlParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        controlParams.setMargins(0, 0, 0, 8); // 减少底部边距
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

        // 提示文本 - 减少边距
        TextView hintText = new TextView(this);
        hintText.setText("调节帧率数值，直到每轮刷新均能点亮每个部分");
        hintText.setTextSize(14);
        hintText.setTextColor(Color.BLACK);
        hintText.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams hintParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        hintParams.setMargins(0, 0, 0, 8); // 减少底部边距
        hintText.setLayoutParams(hintParams);
        mainLayout.addView(hintText);

        // 退出按钮 - 减少边距
        exitButton = new Button(this);
        exitButton.setText("退出测试");
        exitButton.setTextColor(Color.BLACK);
        exitButton.setBackgroundResource(R.drawable.button_border);
        exitButton.setTextSize(14);
        LinearLayout.LayoutParams exitParams = new LinearLayout.LayoutParams(240, 100);
        exitParams.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        exitParams.setMargins(0, 0, 0, 100); // 减少底部边距
        exitButton.setLayoutParams(exitParams);
        mainLayout.addView(exitButton);

        setContentView(mainLayout);
    }

    private void initializeUI() {
        // 设置圆环的帧率
        circleView.setFrameRate(currentFrameRate);

        // 减少帧率按钮
        decreaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentFrameRate > 2) {
                    currentFrameRate--;
                    updateFrameRateText();
                    circleView.setFrameRate(currentFrameRate);
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
                    circleView.setFrameRate(currentFrameRate);
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

    private void updateFrameRateText() {
        frameRateText.setText(String.valueOf(currentFrameRate));
    }

    private void startTest() {
        stopTest();
        lastUpdateTime = System.currentTimeMillis();

        timer = new Timer();
        long period = 1000 / currentFrameRate;

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
        currentSegment = 0;
        startTest();
    }

    private void updateAnimation() {
        if (!testRunning) return;

        // 设置当前点亮的分段
        circleView.setCurrentSegment(currentSegment);

        // 更新当前分段索引
        currentSegment = (currentSegment + 1) % currentFrameRate;

        // 检查是否完成一个周期（1秒）
        long currentTime = System.currentTimeMillis();
        if (currentSegment == 0) {
            long actualPeriod = currentTime - lastUpdateTime;
            lastUpdateTime = currentTime;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        testRunning = false;
        stopTest();
    }

    // 自定义View用于显示圆环帧率检测
    public static class CircleFrameRateView extends View {
        private Paint circlePaint;
        private Paint segmentPaint;
        private int frameRate = 10;
        private int currentSegment = 0;
        private RectF circleRect = new RectF();

        public CircleFrameRateView(Context context) {
            super(context);
            init();
        }

        private void init() {
            // 圆环画笔（黑色背景）
            circlePaint = new Paint();
            circlePaint.setColor(Color.BLACK);
            circlePaint.setStyle(Paint.Style.FILL);
            circlePaint.setAntiAlias(true);

            // 分段画笔（白色点亮部分）
            segmentPaint = new Paint();
            segmentPaint.setColor(Color.WHITE);
            segmentPaint.setStyle(Paint.Style.FILL);
            segmentPaint.setAntiAlias(true);
        }

        public void setFrameRate(int frameRate) {
            this.frameRate = frameRate;
            invalidate();
        }

        public void setCurrentSegment(int segment) {
            this.currentSegment = segment;
            invalidate();
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);

            // 计算圆环大小：直径 = 最短边的3/4
            int minSize = Math.min(w, h);
            float circleDiameter = minSize * 3.0f / 4.0f;
            float circleRadius = circleDiameter / 2.0f;

            float centerX = w / 2.0f;
            float centerY = h / 2.0f;

            circleRect.set(
                    centerX - circleRadius,
                    centerY - circleRadius,
                    centerX + circleRadius,
                    centerY + circleRadius
            );
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            if (frameRate <= 0) return;

            // 绘制黑色背景圆环
            canvas.drawCircle(circleRect.centerX(), circleRect.centerY(),
                    circleRect.width() / 2, circlePaint);

            if (currentSegment < frameRate) {
                // 计算每个分段的角度
                float segmentAngle = 360.0f / frameRate;

                // 计算当前点亮分段的角度范围（从12点方向开始）
                float startAngle = currentSegment * segmentAngle - 90;
                float sweepAngle = segmentAngle;

                // 绘制白色点亮分段
                canvas.drawArc(circleRect, startAngle, sweepAngle, true, segmentPaint);
            }
        }
    }
}