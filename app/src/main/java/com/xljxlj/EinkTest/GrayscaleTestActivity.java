package com.xljxlj.EinkTest;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

public class GrayscaleTestActivity extends Activity {

    private GrayscaleView grayscaleView;
    private Button exitButton;
    private Button switchDirectionButton;
    private Button clearButton;  // 添加清屏按钮
    private LinearLayout mainLayout;  // 添加主布局引用
    private Handler handler = new Handler();
    private boolean isHorizontalGradient = true;
    private boolean isClearing = false;  // 防止重复点击

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置全屏和隐藏虚拟按键
        setFullScreenImmersive();

        // 直接创建布局，不使用XML
        createUI();
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
        mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        mainLayout.setBackgroundColor(Color.WHITE);

        // 创建灰度视图
        grayscaleView = new GrayscaleView(this);
        LinearLayout.LayoutParams grayscaleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1  // weight = 1，占据剩余空间
        );
        grayscaleView.setLayoutParams(grayscaleParams);
        mainLayout.addView(grayscaleView);

        // 创建按钮容器布局
        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        buttonLayoutParams.setMargins(0, 16, 0, 16);
        buttonLayout.setLayoutParams(buttonLayoutParams);

        // 创建切换方向按钮
        switchDirectionButton = new Button(this);
        switchDirectionButton.setText("切换方向");
        switchDirectionButton.setTextColor(Color.BLACK);
        switchDirectionButton.setBackgroundResource(R.drawable.button_border);
        switchDirectionButton.setTextSize(14);

        LinearLayout.LayoutParams switchButtonParams = new LinearLayout.LayoutParams(
                180,  // 稍微窄一点
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        switchButtonParams.setMargins(0, 0, 10, 0);
        switchDirectionButton.setLayoutParams(switchButtonParams);

        switchDirectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 切换渐变方向
                isHorizontalGradient = !isHorizontalGradient;
                grayscaleView.setGradientDirection(isHorizontalGradient);
                grayscaleView.invalidate();  // 重绘视图
            }
        });

        buttonLayout.addView(switchDirectionButton);

        // 创建退出按钮
        exitButton = new Button(this);
        exitButton.setText("退出测试");
        exitButton.setTextColor(Color.BLACK);
        exitButton.setBackgroundResource(R.drawable.button_border);
        exitButton.setTextSize(14);

        LinearLayout.LayoutParams exitButtonParams = new LinearLayout.LayoutParams(
                180,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        exitButtonParams.setMargins(0, 0, 10, 0);
        exitButton.setLayoutParams(exitButtonParams);

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        buttonLayout.addView(exitButton);

        // 创建清屏按钮
        clearButton = new Button(this);
        clearButton.setText("清屏");
        clearButton.setTextColor(Color.BLACK);
        clearButton.setBackgroundResource(R.drawable.button_border);
        clearButton.setTextSize(14);

        LinearLayout.LayoutParams clearButtonParams = new LinearLayout.LayoutParams(
                180,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        clearButton.setLayoutParams(clearButtonParams);

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isClearing) {
                    startClearSequence();
                }
            }
        });

        buttonLayout.addView(clearButton);

        mainLayout.addView(buttonLayout);

        setContentView(mainLayout);
    }

    private void startClearSequence() {
        isClearing = true;

        // 第一步：全屏变黑
        mainLayout.setBackgroundColor(Color.BLACK);
        grayscaleView.setVisibility(View.GONE);
        hideButtons();

        // 延时1秒后执行第二步
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 第二步：全屏变白
                mainLayout.setBackgroundColor(Color.WHITE);

                // 延时600ms后执行第三步
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // 第三步：恢复原始界面
                        restoreOriginalInterface();
                        isClearing = false;
                    }
                }, 600);
            }
        }, 600);
    }

    private void hideButtons() {
        switchDirectionButton.setVisibility(View.GONE);
        exitButton.setVisibility(View.GONE);
        clearButton.setVisibility(View.GONE);
    }

    private void showButtons() {
        switchDirectionButton.setVisibility(View.VISIBLE);
        exitButton.setVisibility(View.VISIBLE);
        clearButton.setVisibility(View.VISIBLE);
    }

    private void restoreOriginalInterface() {
        mainLayout.setBackgroundColor(Color.WHITE);
        grayscaleView.setVisibility(View.VISIBLE);
        showButtons();
    }

    // 自定义View用于显示灰度渐变
    public static class GrayscaleView extends View {
        private Paint paint;
        private boolean isHorizontalGradient = true;  // 默认水平渐变

        public GrayscaleView(Context context) {
            super(context);
            init();
        }

        // 添加这个构造函数以兼容XML布局（虽然我们没使用）
        public GrayscaleView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        // 添加这个构造函数以兼容旧版本
        public GrayscaleView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init();
        }

        private void init() {
            paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
        }

        // 设置渐变方向的方法
        public void setGradientDirection(boolean isHorizontal) {
            this.isHorizontalGradient = isHorizontal;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            int width = getWidth();
            int height = getHeight();

            if (width > 0 && height > 0) {
                LinearGradient gradient;

                if (isHorizontalGradient) {
                    // 水平渐变：从左到右
                    gradient = new LinearGradient(
                            0, 0, width, 0,
                            Color.BLACK, Color.WHITE,
                            Shader.TileMode.CLAMP
                    );
                } else {
                    // 垂直渐变：从上到下
                    gradient = new LinearGradient(
                            0, 0, 0, height,
                            Color.BLACK, Color.WHITE,
                            Shader.TileMode.CLAMP
                    );
                }

                paint.setShader(gradient);
                canvas.drawRect(0, 0, width, height, paint);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 移除所有延迟任务
        handler.removeCallbacksAndMessages(null);
    }
}