package com.example.qzl.nian_xin_kong_jian;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Qzl on 2016-07-26.
 */
public class GooView extends View{
    private Paint paint;
    private float dragRadius = 12f;//拖拽圆的半径
    private float stickyRadius = 12f;//固定圆的半径
    private PointF dragCenter = new PointF(100f,120f);//拖拽圆的圆心
    private PointF stickyCenter = new PointF(180f,120f);//固定圆的圆心

    private PointF[] stickyPoint = {new PointF(180f,108f),new PointF(180f,132f)};
    private PointF[] dragPoint = {new PointF(100f,108f),new PointF(100,132f)};

    private PointF controPoint = new PointF(140f,120f);//控制点
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
        //1 绘制两个圆
        canvas.drawCircle(dragCenter.x,dragCenter.y,dragRadius,paint);//绘制拖拽圆
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
}
