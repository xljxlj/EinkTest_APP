package com.xljxlj.EinkTest;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Timer;
import java.util.TimerTask;

public class CheckerboardRefreshTestActivity extends Activity {

    private CheckerboardView checkerboardView;
    private LinearLayout controlLayout;
    private TextView frameRateText;
    private Button decreaseBtn, increaseBtn, exitButton;
    // 添加方格大小控制相关
    private TextView boxSizeText;
    private Button decreaseBoxBtn, increaseBoxBtn;
    private Handler handler = new Handler();
    private Timer timer;
    private int currentFrameRate = 2;
    private boolean testRunning = true;
    private boolean controlsVisible = false;
    // 添加方格大小设置
    private int currentBoxSizeIndex = 5; // 对应2^5=32
    private int[] boxSizes = {1, 2, 4, 8, 16, 32};

    private static final String TAG = "CheckerboardRefreshTest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate started");

        try {
            setFullScreenImmersive();
            createUI();
            initializeUI();
            startTest();

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
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        mainLayout.setBackgroundColor(Color.BLACK);

        // 创建方格视图
        checkerboardView = new CheckerboardView(this);
        LinearLayout.LayoutParams checkerboardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1
        );
        checkerboardView.setLayoutParams(checkerboardParams);
        mainLayout.addView(checkerboardView);

        // 创建控制布局（初始隐藏）
        controlLayout = new LinearLayout(this);
        controlLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams controlParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        controlLayout.setLayoutParams(controlParams);
        controlLayout.setVisibility(View.GONE);

        // 创建控制按钮的横向布局
        LinearLayout controlButtonsLayout = new LinearLayout(this);
        controlButtonsLayout.setOrientation(LinearLayout.HORIZONTAL);
        controlButtonsLayout.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams buttonsLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        buttonsLayoutParams.setMargins(0, 16, 0, 0);
        controlButtonsLayout.setLayoutParams(buttonsLayoutParams);

        // 方格大小控制布局
        LinearLayout boxSizeContainer = new LinearLayout(this);
        boxSizeContainer.setOrientation(LinearLayout.VERTICAL);
        boxSizeContainer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams boxSizeContainerParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        boxSizeContainer.setLayoutParams(boxSizeContainerParams);

        // 方格大小文本标签
        TextView boxSizeLabel = new TextView(this);
        boxSizeLabel.setText("方格大小");
        boxSizeLabel.setTextSize(16);
        boxSizeLabel.setTextColor(Color.WHITE);
        boxSizeLabel.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams boxLabelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        boxLabelParams.setMargins(0, 0, 0, 8);
        boxSizeLabel.setLayoutParams(boxLabelParams);
        boxSizeContainer.addView(boxSizeLabel);

        LinearLayout boxSizeLayout = new LinearLayout(this);
        boxSizeLayout.setOrientation(LinearLayout.HORIZONTAL);
        boxSizeLayout.setGravity(Gravity.CENTER);
        boxSizeLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // 方格大小减少按钮
        decreaseBoxBtn = new Button(this);
        decreaseBoxBtn.setText("-");
        decreaseBoxBtn.setTextSize(20);
        decreaseBoxBtn.setTextColor(Color.BLACK);
        decreaseBoxBtn.setBackgroundResource(R.drawable.button_border);
        LinearLayout.LayoutParams decreaseBoxParams = new LinearLayout.LayoutParams(100, 100);
        decreaseBoxParams.setMargins(0, 0, 10, 0);
        decreaseBoxBtn.setLayoutParams(decreaseBoxParams);
        boxSizeLayout.addView(decreaseBoxBtn);

        // 方格大小显示
        boxSizeText = new TextView(this);
        boxSizeText.setText("32");
        boxSizeText.setTextSize(18);
        boxSizeText.setTextColor(Color.WHITE);
        boxSizeText.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams boxTextParams = new LinearLayout.LayoutParams(
                100,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        boxSizeText.setLayoutParams(boxTextParams);
        boxSizeLayout.addView(boxSizeText);

        // 方格大小增加按钮
        increaseBoxBtn = new Button(this);
        increaseBoxBtn.setText("+");
        increaseBoxBtn.setTextSize(20);
        increaseBoxBtn.setTextColor(Color.BLACK);
        increaseBoxBtn.setBackgroundResource(R.drawable.button_border);
        LinearLayout.LayoutParams increaseBoxParams = new LinearLayout.LayoutParams(100, 100);
        increaseBoxParams.setMargins(10, 0, 0, 0);
        increaseBoxBtn.setLayoutParams(increaseBoxParams);
        boxSizeLayout.addView(increaseBoxBtn);

        boxSizeContainer.addView(boxSizeLayout);
        controlButtonsLayout.addView(boxSizeContainer);

        // 退出按钮容器
        LinearLayout exitButtonContainer = new LinearLayout(this);
        exitButtonContainer.setOrientation(LinearLayout.VERTICAL);
        exitButtonContainer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams exitContainerParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        exitButtonContainer.setLayoutParams(exitContainerParams);

        // 退出按钮
        exitButton = new Button(this);
        exitButton.setText("退出测试");
        exitButton.setTextColor(Color.BLACK);
        exitButton.setBackgroundResource(R.drawable.button_border);
        exitButton.setTextSize(16);
        LinearLayout.LayoutParams exitParams = new LinearLayout.LayoutParams(280, 100);
        exitButton.setLayoutParams(exitParams);
        exitButtonContainer.addView(exitButton);

        controlButtonsLayout.addView(exitButtonContainer);

        // 帧率控制布局容器
        LinearLayout frameRateContainer = new LinearLayout(this);
        frameRateContainer.setOrientation(LinearLayout.VERTICAL);
        frameRateContainer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams frameRateContainerParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        frameRateContainer.setLayoutParams(frameRateContainerParams);

        // 帧率文本标签
        TextView frameRateLabel = new TextView(this);
        frameRateLabel.setText("帧率");
        frameRateLabel.setTextSize(16);
        frameRateLabel.setTextColor(Color.WHITE);
        frameRateLabel.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams frameLabelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        frameLabelParams.setMargins(0, 0, 0, 8);
        frameRateLabel.setLayoutParams(frameLabelParams);
        frameRateContainer.addView(frameRateLabel);

        LinearLayout frameRateLayout = new LinearLayout(this);
        frameRateLayout.setOrientation(LinearLayout.HORIZONTAL);
        frameRateLayout.setGravity(Gravity.CENTER);
        frameRateLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // 减少按钮
        decreaseBtn = new Button(this);
        decreaseBtn.setText("-");
        decreaseBtn.setTextSize(20);
        decreaseBtn.setTextColor(Color.BLACK);
        decreaseBtn.setBackgroundResource(R.drawable.button_border);
        LinearLayout.LayoutParams decreaseParams = new LinearLayout.LayoutParams(100, 100);
        decreaseParams.setMargins(0, 0, 10, 0);
        decreaseBtn.setLayoutParams(decreaseParams);
        frameRateLayout.addView(decreaseBtn);

        // 帧率显示
        frameRateText = new TextView(this);
        frameRateText.setText("10");
        frameRateText.setTextSize(18);
        frameRateText.setTextColor(Color.WHITE);
        frameRateText.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(100, LinearLayout.LayoutParams.WRAP_CONTENT);
        frameRateText.setLayoutParams(textParams);
        frameRateLayout.addView(frameRateText);

        // 增加按钮
        increaseBtn = new Button(this);
        increaseBtn.setText("+");
        increaseBtn.setTextSize(20);
        increaseBtn.setTextColor(Color.BLACK);
        increaseBtn.setBackgroundResource(R.drawable.button_border);
        LinearLayout.LayoutParams increaseParams = new LinearLayout.LayoutParams(100, 100);
        increaseParams.setMargins(10, 0, 0, 0);
        increaseBtn.setLayoutParams(increaseParams);
        frameRateLayout.addView(increaseBtn);

        frameRateContainer.addView(frameRateLayout);
        controlButtonsLayout.addView(frameRateContainer);

        controlLayout.addView(controlButtonsLayout);
        mainLayout.addView(controlLayout);

        setContentView(mainLayout);
    }

    private void initializeUI() {
        // 设置方格大小文本
        updateBoxSizeText();

        // 设置帧率文本
        updateFrameRateText();

        // 减少方格大小按钮
        decreaseBoxBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentBoxSizeIndex > 0) {
                    currentBoxSizeIndex--;
                    updateBoxSizeText();
                    checkerboardView.setBoxSize(boxSizes[currentBoxSizeIndex]);
                    checkerboardView.recreatePatternBitmaps();
                    showControlsTemporarily();
                }
            }
        });

        // 增加方格大小按钮
        increaseBoxBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentBoxSizeIndex < boxSizes.length - 1) {
                    currentBoxSizeIndex++;
                    updateBoxSizeText();
                    checkerboardView.setBoxSize(boxSizes[currentBoxSizeIndex]);
                    checkerboardView.recreatePatternBitmaps();
                    showControlsTemporarily();
                }
            }
        });

        // 减少帧率按钮
        decreaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentFrameRate > 2) {
                    currentFrameRate--;
                    updateFrameRateText();
                    restartTest();
                    showControlsTemporarily();
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
                    restartTest();
                    showControlsTemporarily();
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

        // 设置方格视图的触摸监听
        checkerboardView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    showControlsTemporarily();
                }
                return true;
            }
        });
    }

    private void updateBoxSizeText() {
        boxSizeText.setText(String.valueOf(boxSizes[currentBoxSizeIndex]));
    }

    private void updateFrameRateText() {
        frameRateText.setText(String.valueOf(currentFrameRate));
    }

    private void showControlsTemporarily() {
        if (!controlsVisible) {
            controlsVisible = true;
            controlLayout.setVisibility(View.VISIBLE);
        }

        // 移除之前的隐藏任务
        handler.removeCallbacks(hideControlsRunnable);
        // 2秒后隐藏控制界面
        handler.postDelayed(hideControlsRunnable, 2000);
    }

    private Runnable hideControlsRunnable = new Runnable() {
        @Override
        public void run() {
            controlsVisible = false;
            controlLayout.setVisibility(View.GONE);
        }
    };

    private void startTest() {
        stopTest();

        // 降低帧率上限，避免性能问题
        int safeFrameRate = Math.min(currentFrameRate, 30);
        long period = Math.max(33, 1000 / safeFrameRate); // 最小33ms，最大30fps

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateCheckerboard();
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
        startTest();
    }

    private void updateCheckerboard() {
        if (!testRunning) return;

        try {
            checkerboardView.togglePattern();
            checkerboardView.invalidate();
        } catch (Exception e) {
            Log.e(TAG, "Error in updateCheckerboard: " + e.getMessage());
        }
    }

    private void showErrorScreen() {
        LinearLayout errorLayout = new LinearLayout(this);
        errorLayout.setOrientation(LinearLayout.VERTICAL);
        errorLayout.setGravity(Gravity.CENTER);
        errorLayout.setBackgroundColor(Color.WHITE);

        TextView errorText = new TextView(this);
        errorText.setText("方格刷新测试功能暂不可用\n请尝试其他检测功能");
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

    // 自定义View用于显示方格图案 - 优化版本
    public static class CheckerboardView extends View {
        private Paint whitePaint;
        private Paint blackPaint;
        private boolean patternState = false;
        private Bitmap patternBitmap1;
        private Bitmap patternBitmap2;
        private boolean bitmapsCreated = false;
        private int lastWidth = 0;
        private int lastHeight = 0;
        private int boxsize = 32;

        public CheckerboardView(Context context) {
            super(context);
            init();
        }

        private void init() {
            whitePaint = new Paint();
            whitePaint.setColor(Color.WHITE);
            whitePaint.setStyle(Paint.Style.FILL);

            blackPaint = new Paint();
            blackPaint.setColor(Color.BLACK);
            blackPaint.setStyle(Paint.Style.FILL);
        }

        public void setBoxSize(int size) {
            if (boxsize != size) {
                boxsize = size;
            }
        }

        public void recreatePatternBitmaps() {
            bitmapsCreated = false;
            createPatternBitmaps(getWidth(), getHeight());
            invalidate();
        }

        public void togglePattern() {
            patternState = !patternState;
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            if (w != lastWidth || h != lastHeight) {
                lastWidth = w;
                lastHeight = h;
                createPatternBitmaps(w, h);
            }
        }

        private void createPatternBitmaps(int width, int height) {
            if (width <= 0 || height <= 0) return;

            // 创建小尺寸的图案块，然后平铺
            int blockSize = 64; // 64x64像素的块
            patternBitmap1 = createPatternBlock(blockSize, false);
            patternBitmap2 = createPatternBlock(blockSize, true);
            bitmapsCreated = true;
        }

        private Bitmap createPatternBlock(int size, boolean alternate) {
            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            boolean isWhite=alternate;
            for (int y = 0; y < size; y += boxsize) {
                for (int x = 0; x < size; x += boxsize) {
                    isWhite=!isWhite;
                    Paint paint = isWhite ? whitePaint : blackPaint;
                    canvas.drawRect(x, y, x + boxsize, y + boxsize, paint);
                }
                isWhite=!isWhite;
            }

            return bitmap;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            int width = getWidth();
            int height = getHeight();

            if (width <= 0 || height <= 0) return;

            // 确保位图已创建
            if (!bitmapsCreated) {
                createPatternBitmaps(width, height);
            }

            if (patternBitmap1 != null && patternBitmap2 != null) {
                // 使用平铺方式绘制图案
                int blockSize = patternBitmap1.getWidth();
                // 平铺绘制
                for (int y = 0; y < height; y += blockSize) {
                    for (int x = 0; x < width; x += blockSize) {
                        Bitmap patternToDraw = patternState ? patternBitmap2 : patternBitmap1;

                        int drawWidth = Math.min(blockSize, width - x);
                        int drawHeight = Math.min(blockSize, height - y);

                        if (drawWidth == blockSize && drawHeight == blockSize) {
                            canvas.drawBitmap(patternToDraw, x, y, null);
                        } else {
                            // 边缘处理
                            canvas.drawBitmap(
                                    Bitmap.createBitmap(patternToDraw, 0, 0, drawWidth, drawHeight),
                                    x, y, null
                            );
                        }
                    }
                }
            }
        }
    }
}