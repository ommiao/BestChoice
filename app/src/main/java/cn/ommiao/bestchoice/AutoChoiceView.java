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
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import java.util.ArrayList;
import java.util.Random;

public class AutoChoiceView extends View {

    private static final String TAG = "AutoChoiceView";

    //是否是过渡状态
    private boolean inProgress = false;
    //view的宽度高度
    private int viewWidth, viewHeight;
    //唯一的画笔
    private Paint mPaint = new Paint();
    //扇形、圆形绘制区域
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
    //中心点占比
    private float centerPointScale;
    //前置圈数
    private int preCircleNumber;

    //指针路径，用完记得reset
    private Path pointerPath = new Path();

    private ArrayList<Choice> choices = new ArrayList<>();
    private ArrayList<Choice> choicesCache = new ArrayList<>();
    private ValueAnimator maskAnimator;
    private ValueAnimator choicesAnimator;
    private float pointerSweepAngle;

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
        innerCircleColor = typedArray.getColor(R.styleable.AutoChoiceView_innerCircleColor, Color.WHITE);
        innerCircleBorderColor = typedArray.getColor(R.styleable.AutoChoiceView_innerCircleBorderColor, Color.WHITE);
        innerCircleBorderScale = typedArray.getFloat(R.styleable.AutoChoiceView_innerCircleBorderScale, 0.2F);
        pointerLengthScale = typedArray.getFloat(R.styleable.AutoChoiceView_pointerLengthScale, 0.5F);
        pointerColor = typedArray.getColor(R.styleable.AutoChoiceView_pointerColor, Color.GRAY);
        pointerBorderColor = typedArray.getColor(R.styleable.AutoChoiceView_pointerBorderColor, Color.WHITE);
        centerPointScale = typedArray.getFloat(R.styleable.AutoChoiceView_centerPointScale, 0.1F);
        preCircleNumber = typedArray.getInteger(R.styleable.AutoChoiceView_preCircleNumber, 5);
        typedArray.recycle();

        correctData();
        initPaint();
        initAnimators();
    }

    private void initPaint() {
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
    }

    private void initAnimators(){

        maskAnimator = ValueAnimator.ofFloat(0, 360);
        maskAnimator = ValueAnimator.ofFloat(0, 360);
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

        choicesAnimator = ValueAnimator.ofFloat(0, 360);
        choicesAnimator.setDuration(1200L);
        choicesAnimator.setInterpolator(new DecelerateInterpolator());
        choicesAnimator.addUpdateListener(valueAnimator -> {
            choiceSweepAngle = (float) valueAnimator.getAnimatedValue();
            invalidate();
        });
        choicesAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                inProgress = false;
            }
        });
    }

    private void correctData(){
        if(innerCircleScale > 0.9F){
            innerCircleScale = 0.9F;
        }
        if(innerCircleBorderScale > 0.9F){
            innerCircleBorderScale = 0.9F;
        }
        if(pointerLengthScale > 0.9F){
            pointerLengthScale = 0.9F;
        }
        if(centerPointScale > 0.9F){
            centerPointScale = 0.9F;
        }
        if(preCircleNumber < 1){
            preCircleNumber = 1;
        }
    }

    public void refreshData( ArrayList<Choice> choices){
        if(inProgress){
            return;
        }
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

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int fullSize = MeasureSpec.getSize(widthMeasureSpec) <= MeasureSpec.getSize(heightMeasureSpec) ? widthMeasureSpec : heightMeasureSpec;
        super.onMeasure(fullSize, fullSize);
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
        pointerPath.reset();
        double pointerLength = radius * pointerLengthScale;
        double pointerSideLength = pointerLength / (4 * Math.sqrt(3)) * 2;

        double pointerSweepAngleH = pointerSweepAngle / 360 * (Math.PI * 2);

        float pointerEndX, pointerEndY;
        pointerEndX = (float) (viewCenter.x + Math.sin(pointerSweepAngleH) * pointerLength);
        pointerEndY = (float) (viewCenter.y - Math.cos(pointerSweepAngleH) * pointerLength);

        float centerPointRadius = (float) (pointerLength * centerPointScale);
        rect.left = viewCenter.x - centerPointRadius;
        rect.top = viewCenter.y - centerPointRadius;
        rect.right = viewCenter.x + centerPointRadius;
        rect.bottom = viewCenter.y + centerPointRadius;
        pointerPath.addArc(rect, pointerSweepAngle - 60, 300);
        if(centerPointRadius >= pointerSideLength){
            pointerPath.lineTo(pointerEndX, pointerEndY);
            pointerPath.close();
        } else {
            double pointerLeftAngleH = (pointerSweepAngle - 30) / 360 * (Math.PI * 2);
            float pointerLeftX, pointerLeftY;
            pointerLeftX = (float) (viewCenter.x + Math.sin(pointerLeftAngleH) * pointerSideLength);
            pointerLeftY = (float) (viewCenter.y - Math.cos(pointerLeftAngleH) * pointerSideLength);
            pointerPath.lineTo(pointerLeftX, pointerLeftY);

            pointerPath.lineTo(pointerEndX, pointerEndY);

            double pointerRightAngleH = (pointerSweepAngle + 30) / 360 * (Math.PI * 2);
            float pointerRightX, pointerRightY;
            pointerRightX = (float) (viewCenter.x + Math.sin(pointerRightAngleH) * pointerSideLength);
            pointerRightY = (float) (viewCenter.y - Math.cos(pointerRightAngleH) * pointerSideLength);
            pointerPath.lineTo(pointerRightX, pointerRightY);

            pointerPath.close();
        }

        mPaint.setColor(pointerColor);
        canvas.drawPath(pointerPath, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth((float) (radius * pointerLengthScale * 0.02));
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
        inProgress = true;
        maskAnimator.start();
    }

    private void choicesAnimation(){
        inProgress = true;
        refreshChoices();
        choicesAnimator.start();
    }

    public void select(){
        if(choices.size() == 0 || inProgress){
            return;
        }
        inProgress = true;
        pointerRandomAnimation();
    }

    private void pointerRandomAnimation(){
        float randomAngle = getRandomAngle();
        float totalAngle = 360 * preCircleNumber + randomAngle;
        ValueAnimator pointerRandomAnimator = ValueAnimator.ofFloat(pointerSweepAngle, pointerSweepAngle + totalAngle);
        pointerRandomAnimator.setDuration((long) (600L * (totalAngle / 360F)));
        pointerRandomAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        pointerRandomAnimator.addUpdateListener(valueAnimator -> {
            pointerSweepAngle = (float) valueAnimator.getAnimatedValue();
            if(pointerSweepAngle >= 360F){
                pointerSweepAngle -= 360F;
            }
            invalidate();
        });
        pointerRandomAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                inProgress = false;
                if(onPointerStopListener != null){
                    int index = getDrawCount(360F - (pointerSweepAngle % 360F)) - 1;
                    onPointerStopListener.onPointerStop(choicesCache.get(index));
                }
            }
        });
        pointerRandomAnimator.start();
    }

    private float getRandomAngle(){
        Random random = new Random();
        return random.nextFloat() * 359.9999F;
    }

    private OnPointerStopListener onPointerStopListener;

    public void setOnPointerStopListener(OnPointerStopListener onPointerStopListener) {
        this.onPointerStopListener = onPointerStopListener;
    }

    public interface OnPointerStopListener {
        void onPointerStop(Choice choice);
    }
}
