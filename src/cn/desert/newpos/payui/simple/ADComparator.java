package cn.desert.newpos.payui.simple;

import android.util.Log;

import java.util.Comparator;

/**
 * Creado por zhouqiang el 10/11/2017.
 */

public class ADComparator implements Comparator<String> {
    @Override
    public int compare(String pathAD1, String pathAD2) {
        return getAdNumber(pathAD1)-getAdNumber(pathAD2);
    }

    private int getAdNumber(String pathAD){
        Log.d("ad" , "pathAD:"+pathAD);
        int beg = pathAD.lastIndexOf("/")+1;
        int end = pathAD.lastIndexOf(".");
        return Integer.parseInt(pathAD.substring(beg, end));
    }
}
