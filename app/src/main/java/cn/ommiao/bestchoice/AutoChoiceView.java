package cn.ommiao.bestchoice;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import java.util.ArrayList;

public class AutoChoiceView extends View {

    private static final String TAG = "AutoChoiceView";

    private int viewWidth, viewHeight;
    private Paint mPaint = new Paint();
    private RectF rect = new RectF();

    //留白宽度
    private int padding;
    //边框线宽度
    private int borderWidth;
    //每份占的角度，360 / 总的权重
    private float perChoiceAngle = 1F;
    //view中心位置
    private Point viewCenter = new Point();
    //遮罩扫过的角度
    private float maskSweepAngle = 0;
    //绘制选项时扫过的角度
    private float choiceSweepAngle = 0;
    //背景色
    private int backgroundColor;
    //边框线色
    private int borderColor;
    //开始绘制选项和刷新时开始的角度
    private float startAngle;
    //内部圆盘比例，相对于大圆盘而言
    private float innerCircleScale;
    //内部圆盘颜色
    private int innerCircleColor;
    //内部圆盘边框颜色
    private int innerCircleBorderColor;
    //内部圆盘边框比例，相对于内部圆盘而言
    private float innerCircleBorderScale;
    //指针长度占比，相对于大圆盘
    private float pointerLengthScale;
    //指针颜色
    private int pointerColor;
    //指针边框颜色
    private int pointerBorderColor;

    private Path pointerPath = new Path();

    private ArrayList<Choice> choices = new ArrayList<>();
    private ArrayList<Choice> choicesCache = new ArrayList<>();

    public AutoChoiceView(Context context) {
        this(context, null);
    }

    public AutoChoiceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoChoiceView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AutoChoiceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AutoChoiceView);
        backgroundColor = typedArray.getColor(R.styleable.AutoChoiceView_backgroundColor, Color.WHITE);
        borderColor = typedArray.getColor(R.styleable.AutoChoiceView_borderColor, Color.WHITE);
        padding = typedArray.getDimensionPixelSize(R.styleable.AutoChoiceView_paddingWidth, 40);
        borderWidth = typedArray.getDimensionPixelOffset(R.styleable.AutoChoiceView_borderWidth, 20);
        startAngle = typedArray.getFloat(R.styleable.AutoChoiceView_startAngle, 270F);
        innerCircleScale = typedArray.getFloat(R.styleable.AutoChoiceView_innerCircleScale, 0.25F);
        if(innerCircleScale > 0.9F){
            innerCircleScale = 0.9F;
        }
        innerCircleColor = typedArray.getColor(R.styleable.AutoChoiceView_innerCircleColor, Color.WHITE);
        innerCircleBorderColor = typedArray.getColor(R.styleable.AutoChoiceView_innerCircleBorderColor, Color.WHITE);
        innerCircleBorderScale = typedArray.getFloat(R.styleable.AutoChoiceView_innerCircleBorderScale, 0.2F);
        pointerLengthScale = typedArray.getFloat(R.styleable.AutoChoiceView_pointerLengthScale, 0.5F);
        pointerColor = typedArray.getColor(R.styleable.AutoChoiceView_pointerColor, Color.GRAY);
        pointerBorderColor = typedArray.getColor(R.styleable.AutoChoiceView_pointerBorderColor, Color.WHITE);
        typedArray.recycle();

        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
    }

    public void refreshData( ArrayList<Choice> choices){
        Log.d(TAG, "refreshData: old size -> " + choicesCache.size());
        Log.d(TAG, "refreshData: new size -> " + choices.size());
        boolean needMask = this.choices.size() > 0;
        this.choices.clear();
        this.choices.addAll(choices);
        calculateAngles();
        if(needMask){
            maskAnimation();
        } else {
            choicesAnimation();
        }
    }

    private void calculateAngles() {
        int size = choices.size();
        Choice.PER_ANGLE = 360F / getAllWeight();
        float startAngle = 0F;
        for (int i = 0; i < size; i++) {
            if(i > 0){
                startAngle += choices.get(i - 1).getWeight() * Choice.PER_ANGLE;
            }
            choices.get(i).setStartAngle(startAngle);
        }
    }

    private float getAllWeight(){
        int weight = 0;
        for (Choice choice : choices) {
            weight += choice.getWeight();
        }
        return weight;
    }

    private void refreshChoices(){
        perChoiceAngle = Choice.PER_ANGLE;
        choicesCache.clear();
        choicesCache.addAll(choices);
        Log.d(TAG, "refreshColors: color has been refresh.");
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mPaint.setStyle(Paint.Style.FILL);

        viewWidth = getWidth();
        viewHeight = getWidth();

        viewCenter.x = viewWidth / 2;
        viewCenter.y = viewHeight / 2;

        float radius = viewWidth / 2 - padding;
        mPaint.setColor(borderColor);
        canvas.drawCircle(viewCenter.x, viewCenter.y, radius, mPaint);

        int allMargin = padding + borderWidth;
        rect.top = allMargin;
        rect.left = allMargin;
        rect.right = viewWidth - allMargin;
        rect.bottom = viewHeight - allMargin;

        radius = viewWidth / 2 - allMargin;

        //background
        mPaint.setColor(backgroundColor);
        canvas.drawCircle(viewCenter.x, viewCenter.y, radius, mPaint);

        //choices
        if(choicesCache.size() > 0 && choiceSweepAngle > 0){
            int drawCount = getDrawCount(choiceSweepAngle);
            float left = choiceSweepAngle - choicesCache.get(drawCount - 1).getStartAngle();
            mPaint.setColor(Color.parseColor(choicesCache.get(drawCount - 1).getColor()));
            canvas.drawArc(rect, startAngle, left, true, mPaint);
            for(int i = drawCount - 2; i >= 0; i--){
                mPaint.setColor(Color.parseColor(choicesCache.get(i).getColor()));
                float anglePrevious = 0F;
                for(int j = drawCount - 2; j > i; j--){
                    anglePrevious += choicesCache.get(j).getWeight() * perChoiceAngle;
                }
                canvas.drawArc(rect, startAngle + left + anglePrevious, perChoiceAngle * choicesCache.get(i).getWeight(), true, mPaint);
            }
        }

        //mask
        mPaint.setColor(backgroundColor);
        canvas.drawArc(rect, startAngle, maskSweepAngle, true, mPaint);

        //inner circle
        mPaint.setColor(innerCircleBorderColor);
        canvas.drawCircle(viewCenter.x, viewCenter.y, radius * innerCircleScale * (1 + innerCircleBorderScale), mPaint);
        mPaint.setColor(innerCircleColor);
        canvas.drawCircle(viewCenter.x, viewCenter.y, radius * innerCircleScale, mPaint);

        //pointer
        double pointerLength = radius * pointerLengthScale;
        double pointerSideLength = pointerLength / (4 * Math.sqrt(3)) * 2;
        float pointerSweepAngle = 180;
        double pointerSweepAngleH = pointerSweepAngle / 360 * (Math.PI * 2);
        double pointerLeftAngleH = (pointerSweepAngle - 30) / 360 * (Math.PI * 2);
        double pointerRightAngleH = (pointerSweepAngle + 30) / 360 * (Math.PI * 2);

        float pointerEndX, pointerEndY;
        pointerEndX = (float) (viewCenter.x + Math.sin(pointerSweepAngleH) * pointerLength);
        pointerEndY = (float) (viewCenter.y + Math.cos(pointerSweepAngleH) * pointerLength);

        float pointerLeftX, pointerLeftY;
        pointerLeftX = (float) (viewCenter.x + Math.sin(pointerLeftAngleH) * pointerSideLength);
        pointerLeftY = (float) (viewCenter.y + Math.cos(pointerLeftAngleH) * pointerSideLength);

        float pointerRightX, pointerRightY;
        pointerRightX = (float) (viewCenter.x + Math.sin(pointerRightAngleH) * pointerSideLength);
        pointerRightY = (float) (viewCenter.y + Math.cos(pointerRightAngleH) * pointerSideLength);

        pointerPath.moveTo(viewCenter.x, viewCenter.y);
        pointerPath.lineTo(pointerLeftX, pointerLeftY);
        pointerPath.lineTo(pointerEndX, pointerEndY);
        pointerPath.lineTo(pointerRightX, pointerRightY);
        pointerPath.close();

        mPaint.setColor(pointerColor);
        canvas.drawPath(pointerPath, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth((float) (radius * pointerLengthScale * 0.03));
        mPaint.setStrokeJoin(Paint.Join.MITER);
        mPaint.setColor(pointerBorderColor);
        canvas.drawPath(pointerPath, mPaint);
    }

    private int getDrawCount(float sweepAngle){
        int size = choicesCache.size();
        for (int i = size - 1; i >= 0 ; i--) {
            if(sweepAngle >= choicesCache.get(i).getStartAngle()){
                return i + 1;
            }
        }
        return 0;
    }

    private void maskAnimation(){
        ValueAnimator maskAnimator = ValueAnimator.ofFloat(0, 360);
        maskAnimator.setDuration(1000L);
        maskAnimator.setInterpolator(new AccelerateInterpolator());
        maskAnimator.addUpdateListener(valueAnimator -> {
            maskSweepAngle = (float) valueAnimator.getAnimatedValue();
            if(maskSweepAngle == 360F){
                maskSweepAngle = 0;
            }
            invalidate();
        });
        maskAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                choicesAnimation();
            }
        });
        maskAnimator.start();
    }

    private void choicesAnimation(){
        refreshChoices();
        ValueAnimator choicesAnimator = ValueAnimator.ofFloat(0, 360);
        choicesAnimator.setDuration(1500L);
        choicesAnimator.setInterpolator(new DecelerateInterpolator());
        choicesAnimator.addUpdateListener(valueAnimator -> {
            choiceSweepAngle = (float) valueAnimator.getAnimatedValue();
            invalidate();
        });
        choicesAnimator.start();
    }
}
