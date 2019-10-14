package cn.ommiao.bestchoice;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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

    private int padding = 40;
    private int radius;
    private int bgOffset = 5;
    private float perChoiceAngle = 1F;

    private Point viewCenter = new Point();

    private float maskSweepAngle = 0;
    private float choiceSweepAngle = 0;

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
        Choice.PER_ANGLE = 360F / size;
        for (int i = 0; i < choices.size(); i++) {
            choices.get(i).setStartAngle(Choice.PER_ANGLE * i);
        }
    }

    private void refreshChoices(){
        perChoiceAngle = Choice.PER_ANGLE;
        choicesCache.clear();
        choicesCache.addAll(choices);
        Log.d(TAG, "refreshColors: color has been refresh.");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        rect.top = padding;
        rect.left = padding;
        rect.right = viewWidth - padding;
        rect.bottom = viewHeight - padding;

        viewWidth = getWidth();
        viewHeight = getWidth();

        radius = viewWidth / 2 - padding;

        viewCenter.x = viewWidth / 2;
        viewCenter.y = viewHeight / 2;

        mPaint.setColor(Color.parseColor("#88888888"));
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);

        //shadow
        canvas.drawCircle(viewCenter.x + bgOffset, viewCenter.y + bgOffset, radius, mPaint);

        //background
        mPaint.setColor(Color.WHITE);
        canvas.drawCircle(viewCenter.x, viewCenter.y, radius, mPaint);

        //choices
        if(choicesCache.size() > 0 && choiceSweepAngle > 0){
            int drawCount = getDrawCount(choiceSweepAngle);
            float left = choiceSweepAngle - perChoiceAngle * (drawCount - 1);
            mPaint.setColor(Color.parseColor(choicesCache.get(drawCount - 1).getColor()));
            canvas.drawArc(rect, 270, left, true, mPaint);
            int j = 0;
            for(int i = drawCount - 2; i >= 0; i--){
                mPaint.setColor(Color.parseColor(choicesCache.get(i).getColor()));
                canvas.drawArc(rect, 270 + left + j * perChoiceAngle, perChoiceAngle, true, mPaint);
                j++;
            }
        }

        //mask
        mPaint.setColor(Color.WHITE);
        canvas.drawArc(rect, 270, maskSweepAngle, true, mPaint);

    }

    private int getDrawCount(float sweepAngle){
        for (int i = 0; i < choicesCache.size(); i++) {
            if(choicesCache.get(i).getStartAngle() > sweepAngle){
                return i;
            }
        }
        return choicesCache.size();
    }

    private void maskAnimation(){
        ValueAnimator maskAnimator = ValueAnimator.ofFloat(0, 360);
        maskAnimator.setDuration(1000L);
        maskAnimator.setInterpolator(new AccelerateInterpolator());
        maskAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                maskSweepAngle = (float) valueAnimator.getAnimatedValue();
                if(maskSweepAngle == 360F){
                    maskSweepAngle = 0;
                }
                invalidate();
            }
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
        choicesAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                choiceSweepAngle = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        choicesAnimator.start();
    }
}