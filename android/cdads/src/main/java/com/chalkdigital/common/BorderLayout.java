package com.chalkdigital.common;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.chalkdigital.common.util.Utils;

public class BorderLayout extends FrameLayout {

    @VisibleForTesting
    static float BORDER_WIDTH_DP = 1.0f;

    static int BORDER_COLOR = Color.BLACK;

    View contentView;

    private Context mContext;

    public BorderLayout(Context context) {
        this(context, null);
    }

    public BorderLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BorderLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public BorderLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
        mContext = context;

    }

    @Override
    public void draw(@NonNull final Canvas canvas) {
        super.draw(canvas);
        Paint paint = new Paint();
        paint.setColor( BORDER_COLOR );
        paint.setStrokeWidth( Utils.convertDpToPixels(BORDER_WIDTH_DP, mContext) );
        paint.setStyle( Paint.Style.STROKE );

        canvas.drawRect( 0, 0, getWidth(), getHeight(), paint );
        if (contentView!=null){
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            contentView.setLayoutParams(layoutParams);
        }

    }

    public void setContentView(View v){

        addView(v);
        contentView = v;


    }

    public void setBorderWidth(float widthInDP){
        BORDER_WIDTH_DP = widthInDP;
    }

    public void setBorderColor(int color){
        BORDER_COLOR = color;
    }

}
