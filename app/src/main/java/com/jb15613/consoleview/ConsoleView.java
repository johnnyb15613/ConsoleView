package com.jb15613.consoleview;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.support.v4.widget.NestedScrollView;
import android.text.Html;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

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
    // ScrollView Child
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
        mClearLogButton.setText(R.string.clearLog);
        mSaveLogButton.setText(R.string.saveLog);

        mClearLogButton.setTextColor(Color.WHITE);
        mSaveLogButton.setTextColor(Color.WHITE);

        mClearLogButton.setOnClickListener(clearLogListener);
        mSaveLogButton.setOnClickListener(saveLogListener);

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

    OnClickListener clearLogListener = new OnClickListener() {
        public void onClick(View v) {
            clearConsole();
        }
    };

    OnClickListener saveLogListener = new OnClickListener() {
        public void onClick(View v) {
            writeToConsole("d", "Console", "Save Log Button Clicked");
        }
    };

    public void writeToConsole(String logLevel, String key, String message) {
        new WriteToConsoleLog(mContext, logLevel, key, message, mContentView, mIsLightTheme).execute();
    }

    public void clearConsole() {
        mContentView.removeAllViews();
    }

    public class WriteToConsoleLog extends AsyncTask<String, String, ArrayList<String>> {

        Context mcontext;
        String mloglevel;
        String mkey;
        String mmessage;
        LinearLayout mcontentview;
        LinearLayout mtextcontainer;
        TextView mtextviewL;
        TextView mtextviewR;
        boolean mlighttheme;
        int shadowColor;

        WriteToConsoleLog(Context context, String logLevel, String key, String message, LinearLayout contentView, boolean lightTheme) {
            mcontext = context;
            mloglevel = logLevel;
            mkey = key;
            mmessage = message;
            mcontentview = contentView;
            mlighttheme = lightTheme;
            shadowColor = 0;
        } // constructor

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mtextcontainer = new LinearLayout(mcontext);
            LinearLayout.LayoutParams textContainerParams =new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            mtextcontainer.setLayoutParams(textContainerParams);
            mtextcontainer.setOrientation(LinearLayout.HORIZONTAL);

            mtextviewL = new TextView(mcontext);
            mtextviewR = new TextView(mcontext);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            mtextviewL.setLayoutParams(textParams);
            mtextviewL.setPadding(8, 4, 8, 0);
            mtextviewR.setLayoutParams(textParams);
            mtextviewL.setPadding(0, 4, 8, 4);

            if (mlighttheme) {
                mtextviewL.setTextColor(0xff000000);
                mtextviewR.setTextColor(0xff000000);
            } else {
                mtextviewL.setTextColor(0xffffffff);
                mtextviewR.setTextColor(0xffffffff);
            }

        } // onPreExecute

        @Override
        protected ArrayList<String> doInBackground(String... aurl) {

            String levelColor = "";

            switch (mloglevel) {
                case "d":

                    if (mDebugColor != null) {
                        if (mDebugColor.length() > 7) {
                            // alpha present, strip it for string
                            levelColor = "#" + mDebugColor.substring(3);
                            shadowColor = Color.parseColor(mDebugColor);
                        } else {
                            // no alpha present, add it for color
                            levelColor = mDebugColor;
                            String newColor = "#ff" + mDebugColor.substring(1);
                            shadowColor = Color.parseColor(newColor);
                        }

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
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm:ss a", Locale.US);

            String time = sdf.format(c.getTime());

            ArrayList<String> items = new ArrayList<>();
            items.add("<font color='" + levelColor + "'> " + time + " " + mloglevel.toUpperCase() + ": " + mkey + "     -     " + "</font>");
            items.add(mmessage);

            return items;
        } // doInBackground

        protected void onProgressUpdate(String... progress) {
        } // onProgressUpdate

        @Override
        protected void onPostExecute(ArrayList<String> info) {

            mtextviewL.setTextSize(12);
            mtextviewR.setTextSize(12);

            if (shadowColor != 0) {
                mtextviewR.setShadowLayer(2, 3, 3, shadowColor);
            }
            mtextviewL.setText(Html.fromHtml(info.get(0)), TextView.BufferType.SPANNABLE);
            mtextviewR.setText(info.get(1));

            mtextcontainer.addView(mtextviewL);
            mtextcontainer.addView(mtextviewR);
            mcontentview.addView(mtextcontainer);

        } // onPostExecute

    } // Class

} // Class