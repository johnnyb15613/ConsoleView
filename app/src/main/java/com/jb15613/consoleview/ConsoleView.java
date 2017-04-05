package com.jb15613.consoleview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.widget.NestedScrollView;
import android.text.Html;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import me.grantland.widget.AutofitTextView;

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
    // Context
    public Context mContext;
    // Logging level
    Boolean mDeepLogging = false;

    /* XML Attributes */
    String mClassColor;
    String mMethodColor;
    String mInfoColor;
    String mVerboseColor;
    String mDebugColor;
    String mWarningColor;
    String mErrorColor;
    String mTextColor;
    String mBackgroundColor;
    boolean mIsLightTheme;
    int mTextSize;

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
            mClassColor = ta.getString(R.styleable.ConsoleView_classLogColor);
            mMethodColor = ta.getString(R.styleable.ConsoleView_methodLogColor);
            mInfoColor = ta.getString(R.styleable.ConsoleView_infoLogColor);
            mDebugColor = ta.getString(R.styleable.ConsoleView_debugLogColor);
            mVerboseColor = ta.getString(R.styleable.ConsoleView_verboseLogColor);
            mWarningColor = ta.getString(R.styleable.ConsoleView_warningLogColor);
            mErrorColor = ta.getString(R.styleable.ConsoleView_errorLogColor);
            mTextColor = ta.getString(R.styleable.ConsoleView_consoleTextColor);
            mBackgroundColor = ta.getString(R.styleable.ConsoleView_consoleBackgroundColor);
            mIsLightTheme = ta.getBoolean(R.styleable.ConsoleView_consoleIsLightTheme, false);
            mTextSize = ta.getInteger(R.styleable.ConsoleView_consoleTextSize, 12);
        } finally {
            ta.recycle();
        }

        mContainer = new LinearLayout(mContext);
        LinearLayout.LayoutParams containerParams =new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        mContainer.setLayoutParams(containerParams);
        mContainer.setOrientation(LinearLayout.VERTICAL);
        mContainer.setPadding(8, 8, 8, 8);
        setViewId(mContainer);

        mButtonContainer = new LinearLayout(mContext);
        LinearLayout.LayoutParams buttonContainerParams =new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mButtonContainer.setLayoutParams(buttonContainerParams);
        mButtonContainer.setOrientation(LinearLayout.HORIZONTAL);
        mButtonContainer.setWeightSum(2);
        setViewId(mButtonContainer);

        mClearLogButton = new Button(mContext);
        mSaveLogButton = new Button(mContext);
        LinearLayout.LayoutParams buttonParams =new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonParams.weight = 1.0f;
        mClearLogButton.setLayoutParams(buttonParams);
        setViewId(mClearLogButton);
        mSaveLogButton.setLayoutParams(buttonParams);
        setViewId(mSaveLogButton);
        mClearLogButton.setText(R.string.clearLog);
        mSaveLogButton.setText(R.string.saveLog);

        mClearLogButton.setTextColor(Color.WHITE);
        mSaveLogButton.setTextColor(Color.WHITE);

        mClearLogButton.setOnClickListener(clearLogListener);
        mSaveLogButton.setOnClickListener(saveLogListener);

        mScrollView = new NestedScrollView(mContext);
        NestedScrollView.LayoutParams scrollParams = new NestedScrollView.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        mScrollView.setLayoutParams(scrollParams);
        setViewId(mScrollView);

        mContentView = new LinearLayout(mContext);
        LinearLayout.LayoutParams contentParams =new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        mContentView.setLayoutParams(contentParams);
        setViewId(mContentView);
        mContentView.setOrientation(LinearLayout.VERTICAL);
        mContentView.setGravity(Gravity.CENTER_VERTICAL);

        mButtonContainer.addView(mClearLogButton);
        mButtonContainer.addView(mSaveLogButton);

        mScrollView.addView(mContentView);

        mContainer.addView(mButtonContainer);
        mContainer.addView(mScrollView);

        if (mBackgroundColor != null) {

            mContainer.setBackgroundColor(Color.parseColor(mBackgroundColor));

        } else {

            if (mIsLightTheme) {
                mContainer.setBackgroundColor(0xffffffff);
            } else {
                mContainer.setBackgroundColor(0xff000000);
            }

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
            saveConsole();
        }
    };

    public void writeToConsole(String logLevel, String key, String message) {
        new WriteToConsoleLog(mContext, logLevel, key, message, mContentView, mIsLightTheme, mTextColor).execute();
    }

    public void writeToConsole(String logLevel, String className, String methodName, String key, String message) {
        new WriteToConsoleLog(mContext, logLevel, className, methodName, key, message, mContentView, mIsLightTheme, mTextColor).execute();
    }

    public void saveConsole() {
        new SaveLogToFile(mContext, mContentView, mDeepLogging).execute();
    }

    public void clearConsole() {
        mContentView.removeAllViews();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.childrenStates = new SparseArray();
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).saveHierarchyState(ss.childrenStates);
        }
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).restoreHierarchyState(ss.childrenStates);
        }
    }

    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        dispatchFreezeSelfOnly(container);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    static class SavedState extends BaseSavedState {
        SparseArray childrenStates;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in, ClassLoader classLoader) {
            super(in);
            childrenStates = in.readSparseArray(classLoader);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeSparseArray(childrenStates);
        }

        public static final ClassLoaderCreator<SavedState> CREATOR
                = new ClassLoaderCreator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel source, ClassLoader loader) {
                return new SavedState(source, loader);
            }

            @Override
            public SavedState createFromParcel(Parcel source) {
                return createFromParcel(null);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    public class WriteToConsoleLog extends AsyncTask<String, String, ArrayList<String>> {

        Context mcontext;

        String mloglevel;
        String mclassname;
        String mmethodname;
        String mkey;
        String mmessage;

        // parent passed in constructor
        LinearLayout mcontentview;
        // layout to put all views into (H)
        LinearLayout mtextparentcontainer;
        // layout on right side wc-wc (V)
        LinearLayout mtextrightcontainer;

        // text view for both brands of logs
        TextView mtextviewTime;
        TextView mtextviewInfo;

        // text view for class and method name
        TextView mtextviewClass;


        boolean mlighttheme;
        boolean mdeeplogging;

        int shadowColor;

        String mtextcolor;

        WriteToConsoleLog(Context context, String logLevel, String key, String message, LinearLayout contentView, boolean lightTheme, String textColor) {
            mcontext = context;
            mloglevel = logLevel;
            mkey = key;
            mmessage = message;
            mcontentview = contentView;
            mlighttheme = lightTheme;
            shadowColor = 0;
            mtextcolor = textColor;
        } // constructor

        WriteToConsoleLog(Context context, String logLevel, String className, String methodName, String key, String message, LinearLayout contentView, boolean lightTheme, String textColor) {
            mcontext = context;
            mloglevel = logLevel;
            mclassname = className;
            mmethodname = methodName;
            mkey = key;
            mmessage = message;
            mcontentview = contentView;
            mlighttheme = lightTheme;
            shadowColor = 0;
            mtextcolor = textColor;
        } // constructor

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        } // onPreExecute

        @Override
        protected ArrayList<String> doInBackground(String... aurl) {

            if ((mclassname == null) && (mmethodname == null)) {
                mdeeplogging = false;
                mDeepLogging = false;
            } else {
                mdeeplogging = true;
                mDeepLogging = true;
            }

            return null;

        } // doInBackground

        protected void onProgressUpdate(String... progress) {
        } // onProgressUpdate

        @Override
        protected void onPostExecute(ArrayList<String> infos) {

            final LinearLayout.LayoutParams containerParams =new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            final LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            mtextparentcontainer = new LinearLayout(mcontext);
            mtextparentcontainer.setLayoutParams(containerParams);
            setViewId(mtextparentcontainer);

            mtextrightcontainer = new LinearLayout(mcontext);
            mtextrightcontainer.setLayoutParams(containerParams);
            setViewId(mtextrightcontainer);
            mtextrightcontainer.setOrientation(LinearLayout.VERTICAL);

            if (!mdeeplogging) {

                mtextviewTime = new TextView(mcontext);
                mtextviewInfo = new TextView(mcontext);

                mtextviewTime.setTag("textViewTime");
                mtextviewInfo.setTag("textViewInfo");

                mtextviewTime.setLayoutParams(textParams);
                setViewId(mtextviewTime);
                mtextviewTime.setPadding(8, 4, 4, 4);
                mtextviewInfo.setLayoutParams(textParams);
                setViewId(mtextviewInfo);
                mtextviewInfo.setPadding(4, 4, 8, 4);

            } else {

                mtextviewTime = new TextView(mcontext);
                mtextviewClass = new AutofitTextView(mcontext);
                mtextviewInfo = new TextView(mcontext);

                mtextviewTime.setTag("textViewTime");
                mtextviewClass.setTag("textViewClass");
                mtextviewInfo.setTag("textViewInfo");

                mtextviewTime.setLayoutParams(textParams);
                setViewId(mtextviewTime);
                mtextviewTime.setPadding(8, 4, 4, 4);

                mtextviewClass.setLayoutParams(textParams);
                setViewId(mtextviewClass);
                mtextviewClass.setPadding(4, 4, 8, 4);

                mtextviewInfo.setLayoutParams(textParams);
                setViewId(mtextviewInfo);
                mtextviewInfo.setPadding(8, 4, 8, 4);

            }

            if ((mtextcolor != null) && (!mtextcolor.equals(""))) {

                mtextviewInfo.setTextColor(Color.parseColor(mtextcolor));

            } else {

                if (mlighttheme) {
                    mtextviewTime.setTextColor(0xff404040);
                    mtextviewInfo.setTextColor(0xff404040);

                    if (mdeeplogging) {
                        mtextviewClass.setTextColor(0xff404040);
                    }

                } else {
                    mtextviewTime.setTextColor(0xffffffff);
                    mtextviewInfo.setTextColor(0xffffffff);

                    if (mdeeplogging) {
                        mtextviewClass.setTextColor(0xffffffff);
                    }

                }

            }

            String levelColor = "";
            String classColor = "";
            String methodColor = "";

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
                        levelColor = "#00cc00";
                        String newColor = "#ff" + levelColor.substring(1);
                        shadowColor = Color.parseColor(newColor);
                    }

                    break;

                case "v":

                    if (mVerboseColor != null) {
                        if (mVerboseColor.length() > 7) {
                            // alpha present, strip it for string
                            levelColor = "#" + mVerboseColor.substring(3);
                            shadowColor = Color.parseColor(mVerboseColor);
                        } else {
                            // no alpha present, add it for color
                            levelColor = mVerboseColor;
                            String newColor = "#ff" + mVerboseColor.substring(1);
                            shadowColor = Color.parseColor(newColor);
                        }
                    } else {
                        levelColor = "#00e7ff";
                        String newColor = "#ff" + levelColor.substring(1);
                        shadowColor = Color.parseColor(newColor);
                    }

                    break;

                case "w":

                    if (mWarningColor != null) {
                        if (mWarningColor.length() > 7) {
                            // alpha present, strip it for string
                            levelColor = "#" + mWarningColor.substring(3);
                            shadowColor = Color.parseColor(mWarningColor);
                        } else {
                            // no alpha present, add it for color
                            levelColor = mWarningColor;
                            String newColor = "#ff" + mWarningColor.substring(1);
                            shadowColor = Color.parseColor(newColor);
                        }
                    } else {
                        levelColor = "#ff7100";
                        String newColor = "#ff" + levelColor.substring(1);
                        shadowColor = Color.parseColor(newColor);
                    }

                    break;

                case "e":

                    if (mErrorColor != null) {
                        if (mErrorColor.length() > 7) {
                            // alpha present, strip it for string
                            levelColor = "#" + mErrorColor.substring(3);
                            shadowColor = Color.parseColor(mErrorColor);
                        } else {
                            // no alpha present, add it for color
                            levelColor = mErrorColor;
                            String newColor = "#ff" + mErrorColor.substring(1);
                            shadowColor = Color.parseColor(newColor);
                        }
                    } else {
                        levelColor = "#ff0000";
                        String newColor = "#ff" + levelColor.substring(1);
                        shadowColor = Color.parseColor(newColor);
                    }

                    break;

                case "i":

                    if (mInfoColor != null) {
                        if (mInfoColor.length() > 7) {
                            // alpha present, strip it for string
                            levelColor = "#" + mInfoColor.substring(3);
                            shadowColor = Color.parseColor(mInfoColor);
                        } else {
                            // no alpha present, add it for color
                            levelColor = mInfoColor;
                            String newColor = "#ff" + mInfoColor.substring(1);
                            shadowColor = Color.parseColor(newColor);
                        }
                    } else {
                        levelColor = "#d500ff";
                        String newColor = "#ff" + levelColor.substring(1);
                        shadowColor = Color.parseColor(newColor);
                    }

                    break;

            }

            if (mClassColor != null) {
                if (mClassColor.length() > 7) {
                    // alpha present, strip it for string
                    classColor = "#" + mClassColor.substring(3);
                } else {
                    // no alpha present
                    classColor = mClassColor;
                }
            } else {
                classColor = "#0080ff";
            }

            if (mMethodColor != null) {
                if (mMethodColor.length() > 7) {
                    // alpha present, strip it for string
                    methodColor = "#" + mMethodColor.substring(3);
                } else {
                    // no alpha present
                    methodColor = mMethodColor;
                }
            } else {
                methodColor = "#ff00c3";
            }

            // Get the time
            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm:ss a", Locale.US);

            String time = sdf.format(c.getTime());

            ArrayList<String> items = new ArrayList<>();
            items.add("<font color='" +  levelColor + "'> " + mloglevel.toUpperCase() + ": " + time + "</font>");

            items.add(mkey + " - " + mmessage);

            if (mdeeplogging) {
                items.add("<font color='" +  classColor + "'>" + mclassname + ".</font>");
                items.add("<font color='" +  methodColor + "'>" + mmethodname + "</font>");
            }

            mtextviewTime.setTextSize(mTextSize);
            mtextviewInfo.setTextSize(mTextSize);

            mtextviewTime.setText(Html.fromHtml(items.get(0)), TextView.BufferType.SPANNABLE);
            mtextviewInfo.setText(items.get(1));

            if (shadowColor != 0) {
                mtextviewInfo.setShadowLayer(2, 2, 2, shadowColor);
            }

            if (mdeeplogging) {

                mtextviewClass.setTextSize(mTextSize);
                mtextviewClass.setLines(1);
                String classMethod = items.get(2) + items.get(3);

                mtextviewClass.setText(Html.fromHtml(classMethod), TextView.BufferType.SPANNABLE);

                mtextparentcontainer.addView(mtextviewTime);

                mtextrightcontainer.addView(mtextviewClass);
                mtextrightcontainer.addView(mtextviewInfo);

                mtextparentcontainer.addView(mtextrightcontainer);

            } else {
                mtextparentcontainer.addView(mtextviewTime);
                mtextrightcontainer.addView(mtextviewInfo);

                mtextparentcontainer.addView(mtextrightcontainer);
            }

            mcontentview.addView(mtextparentcontainer);

        } // onPostExecute

    } // Class

    public void setViewId(View view) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            view.setId(Utilities.generateViewId());
        } else {
            view.setId(View.generateViewId());
        }
    } // setViewId

} // Class