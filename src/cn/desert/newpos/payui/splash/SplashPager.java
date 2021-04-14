package cn.desert.newpos.payui.splash;

import android.content.Context;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Creado por zhouqiang el 6/7/2017.
 */

public class SplashPager extends ViewPager{
    public SplashPager(Context context) {
        super(context);
    }

    public SplashPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false ;
    }
}
