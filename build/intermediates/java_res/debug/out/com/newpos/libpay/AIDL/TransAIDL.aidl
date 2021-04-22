// TransAIDL.aidl
package com.newpos.libpay.AIDL;

// Declare aquí cualquier tipo no predeterminado con declaraciones de importación

interface TransAIDL {
    /**
     * Demuestra algunos tipos básicos que puede usar como parámetros.
     * y devuelve valores en AIDL.
     */
//    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
//            double aDouble, String aString);

    String sale(String json);

    String sign(String json);

    String down(String json);
}
