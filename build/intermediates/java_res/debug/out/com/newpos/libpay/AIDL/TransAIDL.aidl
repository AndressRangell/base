// TransAIDL.aidl
package com.newpos.libpay.AIDL;

// Declare any non-default types here with import statements

interface TransAIDL {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
//    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
//            double aDouble, String aString);

    String sale(String json);

    String sign(String json);

    String down(String json);
}
