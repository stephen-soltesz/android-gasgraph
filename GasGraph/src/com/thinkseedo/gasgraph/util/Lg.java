package com.thinkseedo.gasgraph.util;

import android.util.Log;

public class Lg {
    private static final int CLIENT_CODE_STACK_INDEX;
    public static final boolean DEBUG=false;

    static {
        // Finds out the index of "this code" in the returned stack trace - funny but it differs in JDK 1.5 and 1.6
        int i = 0;
        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
            i++;
            if (ste.getClassName().equals(Lg.class.getName())) {
                break;
            }
        }
        CLIENT_CODE_STACK_INDEX = i;
    }

    public static String methodName() {
        return Thread.currentThread().getStackTrace()[CLIENT_CODE_STACK_INDEX].getMethodName();
    }
    public static String methodName(int i) {
        return Thread.currentThread().getStackTrace()[CLIENT_CODE_STACK_INDEX + i].getMethodName();
    }
    public static void d(String s) {
    	if ( DEBUG ) {
    		Log.d(methodName(1), s);
    	}
    }
    public static void w(String s) {
    	if ( DEBUG ) {
    		Log.w(methodName(1), s);
    	}
    }
    public static void i(String s) {
    	if ( DEBUG ) {
    		Log.i(methodName(1), s);
    	}
    }
    public static void e(String s) {
    	if ( DEBUG ) {
    		Log.e(methodName(1), s);
    	}
    }
    public static void wtf(String s) {
    	if ( DEBUG ) {
    		Log.e(methodName(1), s);
    	}
    }
}