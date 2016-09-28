package com.jb15613.consoleview;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.support.v4.widget.NestedScrollView;
import android.text.Html;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import android.util.TypedValue;
import android.graphics.Color;

public class ConsoleView extends LinearLayout {

    // Parent View (vertical)
    private LinearLayout mContainer;

    // Buttons Layout (horizontal)
    LinearLayout mButtonContainer;
    // Clear Button
    Button mClearLogButton;
    // Save Button
    Button mSaveLogButton;

    // ScrollView
    private NestedScrollView mScrollView;
    // ScrollView Child (add textviews here) (vertical)
    private LinearLayout mContentView;

    public Context mContext;

    String mVerboseColor;
    String mDebugColor;
    String mWarningColor;
    String mErrorColor;
    boolean mIsLightTheme;

    public ConsoleView(Context context) {
        this(context, null);
        mContext = context;
    }

    public ConsoleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        mContext = context;
    }

    public ConsoleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mContext = context;

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ConsoleView, 0, 0);
        try {
            mDebugColor = ta.getString(R.styleable.ConsoleView_debugLogColor);
            mVerboseColor = ta.getString(R.styleable.ConsoleView_verboseLogColor);
            mWarningColor = ta.getString(R.styleable.ConsoleView_warningLogColor);
            mErrorColor = ta.getString(R.styleable.ConsoleView_errorLogColor);
            mIsLightTheme = ta.getBoolean(R.styleable.ConsoleView_consoleIsLightTheme, false);
        } finally {
            ta.recycle();
        }

        mContainer = new LinearLayout(mContext);
        LinearLayout.LayoutParams containerParams =new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        mContainer.setLayoutParams(containerParams);
        mContainer.setOrientation(LinearLayout.VERTICAL);

        mButtonContainer = new LinearLayout(mContext);
        LinearLayout.LayoutParams buttonContainerParams =new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mButtonContainer.setLayoutParams(buttonContainerParams);
        mButtonContainer.setOrientation(LinearLayout.HORIZONTAL);
        mButtonContainer.setWeightSum(2);

        mClearLogButton = new Button(mContext);
        mSaveLogButton = new Button(mContext);
        LinearLayout.LayoutParams buttonParams =new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonParams.weight = 1.0f;
        mClearLogButton.setLayoutParams(buttonParams);
        mSaveLogButton.setLayoutParams(buttonParams);
        mClearLogButton.setText("Clear Log");
        mSaveLogButton.setText("Save Log");

        // TypedValue typedValue = new TypedValue();

        int textColor = Color.WHITE;

		/*
		if (context.getTheme().resolveAttribute(R.attr.colorAccent, typedValue, true)) {
			textColor = typedValue.data;
		} else {
			textColor = Color.WHITE;
		}
		*/

        mClearLogButton.setTextColor(textColor);
        mSaveLogButton.setTextColor(textColor);

        mScrollView = new NestedScrollView(mContext);
        NestedScrollView.LayoutParams scrollParams = new NestedScrollView.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        mScrollView.setLayoutParams(scrollParams);

        mContentView = new LinearLayout(mContext);
        LinearLayout.LayoutParams contentParams =new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        mContentView.setLayoutParams(contentParams);
        mContentView.setOrientation(LinearLayout.VERTICAL);
        mContentView.setGravity(Gravity.CENTER_VERTICAL);

        mButtonContainer.addView(mClearLogButton);
        mButtonContainer.addView(mSaveLogButton);

        mScrollView.addView(mContentView);

        mContainer.addView(mButtonContainer);
        mContainer.addView(mScrollView);

        if (mIsLightTheme) {
            mContainer.setBackgroundColor(0xffffffff);
        } else {
            mContainer.setBackgroundColor(0xff101010);
        }

        this.addView(mContainer);

    }

    public void writeToConsole(String logLevel, String key, String message) {
        new WriteToConsoleLog(mContext, logLevel, key, message, mContentView, mIsLightTheme).execute();
    }

    public void clearConsole() {
        mContentView.removeAllViews();
    }

    public class WriteToConsoleLog extends AsyncTask<String, String, String> {

        Context mcontext;
        String mloglevel;
        String mkey;
        String mmessage;
        LinearLayout mcontentview;
        TextView mtextview;
        boolean mlighttheme;

        public WriteToConsoleLog(Context context, String logLevel, String key, String message, LinearLayout contentView, boolean lightTheme) {
            mcontext = context;
            mloglevel = logLevel;
            mkey = key;
            mmessage = message;
            mcontentview = contentView;
            mlighttheme = lightTheme;
        } // constructor

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mtextview = new TextView(mcontext);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            mtextview.setLayoutParams(textParams);
            mtextview.setPadding(8, 4, 8, 4);

            if (mlighttheme) {
                mtextview.setTextColor(0xff000000);
            } else {
                mtextview.setTextColor(0xffffffff);
            }

        } // onPreExecute

        @Override
        protected String doInBackground(String... aurl) {

            String levelColor = "";

            switch (mloglevel) {
                case "d":

                    if (mDebugColor != null) {
                        levelColor = mDebugColor;
                    } else {
                        levelColor = "#0000ff";
                    }

                    break;

                case "v":

                    if (mVerboseColor != null) {
                        levelColor = mVerboseColor;
                    } else {
                        levelColor = "#00ff00";
                    }

                    break;

                case "w":

                    if (mWarningColor != null) {
                        levelColor = mWarningColor;
                    } else {
                        levelColor = "#ffff00";
                    }

                    break;

                case "e":

                    if (mErrorColor != null) {
                        levelColor = mErrorColor;
                    } else {
                        levelColor = "#ff0000";
                    }

                    break;

            }

            // Get the time
            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm:ss a");

            String time = sdf.format(c.getTime());

            // Create new info
            String debugInfo = "<font color='" + levelColor + "'> " + time + " " + mloglevel.toUpperCase() + ": " + mkey + "     -     " + "</font>" + mmessage;


            return debugInfo;
        } // doInBackground

        protected void onProgressUpdate(String... progress) {
        } // onProgressUpdate

        @Override
        protected void onPostExecute(String info) {

            mtextview.setTextSize(12);
            mtextview.setText(Html.fromHtml(info), TextView.BufferType.SPANNABLE);
            mcontentview.addView(mtextview);

        } // onPostExecute

    } // Class

} // Class