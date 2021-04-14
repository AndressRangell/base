package cn.desert.newpos.payui.splash;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.View;

import java.util.ArrayList;

/**
 * Creado por zhouqiang el 6/7/2017.
 */

public class SplashAdapter extends PagerAdapter {

    private ArrayList<View> mList = new ArrayList<>();

    public SplashAdapter(ArrayList<View> mList) {
        this.mList = mList;
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public void destroyItem(View container, int position, Object object) {
        ((ViewPager)container).removeView(mList.get(position));
    }

    @Override
    public Object instantiateItem(View container, int position) {
        ((ViewPager)container).addView(mList.get(position));
        return mList.get(position);
    }
}
