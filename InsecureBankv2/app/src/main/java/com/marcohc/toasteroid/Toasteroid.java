package com.marcohc.toasteroid;

import android.content.Context;
import android.widget.Toast;

public class Toasteroid {
    public enum STYLES {
        SUCCESS,
        WARNING,
        ERROR,
        INFO
    }

    public static final int LENGTH_SHORT = Toast.LENGTH_SHORT;
    public static final int LENGTH_LONG = Toast.LENGTH_LONG;

    public static void show(Context context, String text, STYLES style, int duration) {
        Toast.makeText(context, text, duration).show();
    }

    public static void show(Context context, String text, STYLES style) {
        show(context, text, style, LENGTH_SHORT);
    }
}
