package com.etiennelawlor.minesweeper.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.etiennelawlor.minesweeper.R;


public class CustomFontTextView extends TextView {
    public CustomFontTextView(Context context) {
        super(context);
    }

    public CustomFontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            init(context, attrs);
        }
    }

    public CustomFontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (!isInEditMode()) {
            init(context, attrs);
        }
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes( attrs, R.styleable.CustomFontTextView, 0, 0);
        try {
            String fontName = getFontName(a.getInteger(R.styleable.CustomFontTextView_textFont, 0));
            if (!fontName.equals("")) {
                try {
                    setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/" + fontName));
                } catch (Exception e) {
                    Log.e("CustomFontTextView", e.getMessage());
                }
            }

        } finally {
            a.recycle();
        }
    }

    private String getFontName(int index) {

        switch (index) {
            case 0 :
                return "DS-DIGII.ttf";
            case 1 :
                return "DS-DIGIT.ttf";
            case 2 :
                return "Digital_Dismay.otf";
            case 3 :
                return "digital-7.ttf";
            case 4 :
                return "digital-7-italic.ttf";
            case 5 :
                return "digital-7-mono.ttf";
            case 6 :
                return "TickingTimebombBB.ttf";
            case 7 :
                return "advanced_pixel_lcd_7.ttf";
            case 8 :
                return "digital-readout-heavy-oblique.ttf";
            case 9 :
                return "digital_readout_heavy.ttf";
            default:
                return "";
        }
    }
}
