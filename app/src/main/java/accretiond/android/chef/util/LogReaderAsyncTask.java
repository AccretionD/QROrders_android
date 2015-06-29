package accretiond.android.chef.util;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.LinkedList;

import accretiond.android.chef.data.LogLine;
import accretiond.android.chef.reader.LogcatReader;
import accretiond.android.chef.reader.LogcatReaderLoader;

/**
 * Created by meridianc4m1l0 on 5/21/15.
 */
public class LogReaderAsyncTask extends AsyncTask<Void,LogLine,Void> {


    private int counter = 0;
    private  UtilLogger log;
    private volatile boolean paused;
    private final Object lock = new Object();
    private boolean firstLineReceived;
    private boolean killed;
    private boolean collapsedMode;
    private LogcatReader reader;
    private Runnable onFinished;
    Context ctx;

    public LogReaderAsyncTask(Context ctx, UtilLogger log){
        this.ctx=ctx;
        this.log=log;
    }

    @Override
    protected Void doInBackground(Void... params) {
        log.d("doInBackground()");

        try {
            // use "recordingMode" because we want to load all the existing lines at once
            // for a performance boost
            LogcatReaderLoader loader = LogcatReaderLoader.create(ctx, false);
            reader = loader.loadReader();


            String line;
            LinkedList<LogLine> initialLines = new LinkedList<LogLine>();
            while ((line = reader.readLine()) != null) {
                if (paused) {
                    synchronized (lock) {
                        if (paused) {
                            lock.wait();
                        }
                    }
                }
                LogLine logLine = LogLine.newLogLine(line, !collapsedMode);
                if (!reader.readyToRecord()) {
                    // "ready to record" in this case means all the initial lines have been flushed from the reader
                    initialLines.add(logLine);
                    if (initialLines.size() > 2000) {
                        initialLines.removeFirst();
                    }
                } else if (!initialLines.isEmpty()) {
                    // flush all the initial lines we've loaded
                    initialLines.add(logLine);
                    publishProgress(ArrayUtil.toArray(initialLines, LogLine.class));
                    initialLines.clear();
                } else {
                    log.e("did", "log new line  ");
                    // just proceed as normal
                    publishProgress(logLine);
                }
            }
        } catch (InterruptedException e) {
            log.d(e, "expected error");
        } catch (Exception e) {
            log.d(e, "unexpected error");
        } finally {
            killReader();
            log.d("AsyncTask has died");
        }

        return null;
    }

    public void killReader() {
        if (!killed) {
            synchronized (lock) {
                if (!killed && reader != null) {
                    reader.killQuietly();
                    killed = true;
                }
            }
        }

    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        log.d("onPostExecute()");
        doWhenFinished();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        log.d("onPreExecute()");


    }

    @Override
    protected void onProgressUpdate(LogLine... values) {
        Log.println(Log.DEBUG, "any progress?", ""+values.length);
        super.onProgressUpdate(values);

        if (!firstLineReceived) {
            firstLineReceived = true;

        }
        for (LogLine logLine : values) {
            Log.println(Log.DEBUG, "did", logLine.getLogOutput());
            log.d(this.getClass().getSimpleName(), logLine.getLogOutput());
            //socket.emit("yeah", logLine.getLogOutput());

        }




    }

    private void doWhenFinished() {
        if (paused) {
            unpause();
        }
        if (onFinished != null) {
            onFinished.run();
        }
    }

    public void pause() {
        synchronized (lock) {
            paused = true;
        }
    }

    public void unpause() {
        synchronized (lock) {
            paused = false;
            lock.notify();
        }
    }

    public boolean isPaused() {
        return paused;
    }

    public void setOnFinished(Runnable onFinished) {
        this.onFinished = onFinished;
    }


}

