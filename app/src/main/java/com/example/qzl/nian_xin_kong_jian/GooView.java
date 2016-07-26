package com.example.qzl.nian_xin_kong_jian;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;

/**
 * Created by Qzl on 2016-07-26.
 */
public class GooView extends View{
    private Paint paint;
    private float dragRadius = 12f;//拖拽圆的半径
    private float stickyRadius = 12f;//固定圆的半径
    private PointF dragCenter = new PointF(100f,120f);//拖拽圆的圆心
    private PointF stickyCenter = new PointF(150f,120f);//固定圆的圆心

    private PointF[] stickyPoint = {new PointF(150f,108f),new PointF(150f,132f)};
    private PointF[] dragPoint = {new PointF(100f,108f),new PointF(100,132f)};

    private PointF controPoint = new PointF(125f,120f);//控制点
    private double linek;//斜率
    private float maxDistance = 80;//两圆心之间的最大距离

    private boolean isDragOutOfRange = false;
    public GooView(Context context) {
        super(context);
        init();
    }

    public GooView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GooView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);//设置抗锯齿
        paint.setColor(Color.RED);
    }

    /**
     * 绘制
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //让整体画布往上偏移
        canvas.translate(0,-Utils.getStatusBarHeight(getResources()));

        stickyRadius = getStickyRadius();
        //dragPoint:2圆圆心连线的垂线与drag圆的交点
        //通过dragCenter动态求出dragPoint的stickyPoint
        float xOffset = dragCenter.x - stickyCenter.x;
        float yOffset = dragCenter.y - stickyCenter.y;
        if(xOffset != 0){
            linek = yOffset/xOffset;
        }
        dragPoint = GeometryUtil.getIntersectionPoints(dragCenter,dragRadius,linek);
        stickyPoint = GeometryUtil.getIntersectionPoints(stickyCenter,stickyRadius,linek);
        //1 绘制两个圆
        canvas.drawCircle(dragCenter.x,dragCenter.y,dragRadius,paint);//绘制拖拽圆
        //动态计算控制点
        controPoint = GeometryUtil.getPointByPercent(dragCenter,stickyCenter,0.618f);
        if (!isDragOutOfRange){//没有超出范围则绘制
            canvas.drawCircle(stickyCenter.x,stickyCenter.y,stickyRadius,paint);//绘制固定圆
            //2 使用贝塞尔曲线绘制链接部分
            Path path = new Path();
            path.moveTo(stickyPoint[0].x,stickyPoint[0].y);//设置起点
            path.quadTo(controPoint.x,controPoint.y,dragPoint[0].x,dragPoint[0].y);
            path.lineTo(dragPoint[1].x,dragPoint[1].y);//连线
            path.quadTo(controPoint.x,controPoint.y,stickyPoint[1].x,stickyPoint[1].y);
    //        path.close();//默认会闭合，所以不用调
            canvas.drawPath(path,paint);
        }

        //绘制圈圈，以固定圆圆心为圆心，以80为半径
        paint.setStyle(Paint.Style.STROKE);//设置只有边线
        canvas.drawCircle(stickyCenter.x,stickyCenter.y,maxDistance,paint);
        paint.setStyle(Paint.Style.FILL);
    }

    /**
     * 动态求出固定圆的半径
     */
    private float getStickyRadius(){
        float radius;
        //求出两个圆心之间的距离
        float centerdistance = GeometryUtil.getDistanceBetween2Points(dragCenter,stickyCenter);
        float fraction = centerdistance/maxDistance;//圆心距离占总距离的百分比
        radius = GeometryUtil.evaluateValue(fraction,12f,4f);
        return radius;
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                isDragOutOfRange = false;
                dragCenter.set(event.getRawX(),event.getRawY());
                break;
            case MotionEvent.ACTION_MOVE:
                dragCenter.set(event.getRawX(),event.getRawY());
                if (GeometryUtil.getDistanceBetween2Points(dragCenter,stickyCenter) > maxDistance){
                    //超出范围，应该断掉，不在绘制白塞尔曲线部分
                    isDragOutOfRange = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (GeometryUtil.getDistanceBetween2Points(dragCenter,stickyCenter) > maxDistance){
                    dragCenter.set(stickyCenter.x,stickyCenter.y);
                }else {
                    if (isDragOutOfRange){
                        //如果曾经超出范围过
                        dragCenter.set(stickyCenter.x,stickyCenter.y);
                    }else {
                        //动画的形式回去
                        ValueAnimator valueAnimator = ObjectAnimator.ofFloat(1);
                        final PointF startPointF = new PointF(dragCenter.x,dragCenter.y);
                        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                //动画执行的百分比
                                float animatedFraction = valueAnimator.getAnimatedFraction();
                                PointF pointF = GeometryUtil.getPointByPercent(startPointF,stickyCenter,animatedFraction);
                                dragCenter.set(pointF);
                                invalidate();
                            }
                        });
                        valueAnimator.setDuration(500);
                        valueAnimator.setInterpolator(new OvershootInterpolator(3));
                        valueAnimator.start();
                    }
                }
                break;
        }
        invalidate();//重绘
        return true;
    }
}
