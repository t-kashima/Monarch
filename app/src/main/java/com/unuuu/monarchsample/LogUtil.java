package com.unuuu.monarchsample;

import android.util.Log;

import java.util.regex.Pattern;

/**
 * ログのUtilityクラス
 * http://devlab.isao.co.jp/android%E3%81%AElogcat%E3%81%AEtag%E3%81%AB%E3%82%AF%E3%83%A9%E3%82%B9%E5%90%8D%E3%80%81%E3%83%A1%E3%82%BD%E3%83%83%E3%83%89%E5%90%8D%E3%80%81%E8%A1%8C%E7%95%AA%E5%8F%B7%E3%82%92%E8%A1%A8%E7%A4%BA/
 * Created by t-kashima on 15/06/04.
 */
public class LogUtil {

    private final static String TAG = "Amigo";

    public static void d(String message) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, getTag() + message);
        }
    }

    public static void e(Throwable t) {
        e("", t);
    }

    public static void e(String message) {
        e(message, null);
    }

    public static void e(String message, Throwable t) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, getTag() + message, t);
        }
    }

    private static String getTag() {
        final StackTraceElement trace = Thread.currentThread().getStackTrace()[4];
        final String cla = trace.getClassName();
        Pattern pattern = Pattern.compile("[\\.]+");
        final String[] splitedStr = pattern.split(cla);
        final String simpleClass = splitedStr[splitedStr.length - 1];

        final String mthd = trace.getMethodName();
        final int line = trace.getLineNumber();
        return simpleClass + "#" + mthd + ":" + line + " / ";
    }
}