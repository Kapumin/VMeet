package com.abjt.vmeet.utils;

import android.content.Context;

import com.abjt.vmeet.R;

import io.github.muddz.styleabletoast.StyleableToast;

public class Toaster {
    private final Context context;

    public Toaster(Context context) {
        this.context = context;
    }

    public void showToast(String message) {
        StyleableToast.makeText(context, message, R.style.myToastTheme).show();
    }
}
