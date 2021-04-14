//package com.newpos.libpay;
//
//import com.newpos.libpay.paras.EmvCapkInfo;
//import com.newpos.libpay.utils.PAYUtils;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Created by zhouqiang on 2018/3/28.
// */
//
//public class Test {
//    public static void build(){
//        EmvCapkInfo emvCapkInfo = new EmvCapkInfo();
//        List<byte[]> capk = new ArrayList<>();
//        capk.add(TLV);
//        capk.add(TLV);
//        capk.add(TLV);
//        capk.add(TLV);
//        emvCapkInfo.setCapkList(capk);
//        try {
//            PAYUtils.object2File(emvCapkInfo , "/sdcard/capk.dat");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}
