package com.jb15613.consoleview;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SaveLogToFile extends AsyncTask<String, String, String> {

    Context mContext;
    LinearLayout mContentView;
    Boolean mDeepLogging;

    int children = 0;

    TextView mInfoTv;
    TextView mClassTv;
    TextView mTimeTv;

    String writable = "";

    Boolean success;

    SaveLogToFile(Context context, LinearLayout contentView, Boolean deepLogging) {
        mContext = context;
        mContentView = contentView;
        mDeepLogging = deepLogging;
    } // constructor

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        children = mContentView.getChildCount();

        String time;
        String info;
        String callingClass;

        for (int i = 0; i < children; i++) {
            mTimeTv = (TextView) mContentView.getChildAt(i).findViewWithTag("textViewTime");

            writable += mTimeTv.getText().toString() + " - ";

            if (mDeepLogging) {
                mClassTv = (TextView) mContentView.getChildAt(i).findViewWithTag("textViewClass");

                writable += mClassTv.getText().toString() + " - ";
            }

            mInfoTv = (TextView) mContentView.getChildAt(i).findViewWithTag("textViewInfo");

            writable += mInfoTv.getText().toString() + "/n";

        }

    } // onPreExecute

    @Override
    protected String doInBackground(String... aurl) {

        final File logDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/.ConsoleView/");

        // Make sure the path directory exists.
        if(!logDir.exists()) {
            logDir.mkdirs();
        }

        // Get the time
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm:ss a", Locale.US);

        String time = sdf.format(c.getTime());

        final File log = new File(logDir, "consoleLog " + time + ".txt");

        try
        {
            log.createNewFile();
            FileOutputStream fOut = new FileOutputStream(log);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);

            if (writable.contains("/n")) {

                String[] items = writable.split("/n");

                for (int i = 0; i < items.length; i++) {
                    myOutWriter.append(items[i]);
                    myOutWriter.append(System.getProperty("line.separator"));
                }

            } else {
                myOutWriter.append(writable);
            }

            myOutWriter.close();

            fOut.flush();
            fOut.close();

        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }

        return null;

    } // doInBackground

    protected void onProgressUpdate(String... progress) {
    } // onProgressUpdate

    @Override
    protected void onPostExecute(String info) {
    } // onPostExecute

} // Class