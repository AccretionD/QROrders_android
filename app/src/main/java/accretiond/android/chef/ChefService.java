package accretiond.android.chef;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by meridianc4m1l0 on 3/16/15.
 */
public class ChefService extends Service {

    public static final String LOG_TAG = "DTMService";
    public static final String INTENT_URL = "INTENT_URL";
    public static final String INTENT_STATUS_CODE = "INTENT_STATUS_CODE";
    public static final String INTENT_HEADERS = "INTENT_HEADERS";
    public static final String INTENT_DATA = "INTENT_DATA";
    public static final String INTENT_THROWABLE = "INTENT_THROWABLE";

    public static final String ACTION_START = "SYNC_START";
    public static final String ACTION_RETRY = "SYNC_RETRY";
    public static final String ACTION_CANCEL = "SYNC_CANCEL";
    public static final String ACTION_SUCCESS = "SYNC_SUCCESS";
    public static final String ACTION_FAILURE = "SYNC_FAILURE";
    public static final String ACTION_FINISH = "SYNC_FINISH";
    public static final String[] ALLOWED_ACTIONS = {ACTION_START,
            ACTION_RETRY, ACTION_CANCEL, ACTION_SUCCESS, ACTION_FAILURE, ACTION_FINISH};

    private final IBinder mBinder = new DTMServiceBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class DTMServiceBinder extends Binder {

        public ChefService getDTMService() {
            return ChefService.this;
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //success(1);
        //failure(2);
        return Service.START_STICKY;

    }


    public void success(int statusCode) {
        Intent broadcast = new Intent(ChefService.ACTION_SUCCESS);
        broadcast.putExtra(INTENT_STATUS_CODE, statusCode);
        sendBroadcast(broadcast);
        Log.d(LOG_TAG, "onSuccess");
    }


    public void failure(int statusCode) {
        Log.d(LOG_TAG, "onFailure");
        Intent broadcast = new Intent(ChefService.ACTION_FAILURE);
        broadcast.putExtra(INTENT_STATUS_CODE, statusCode);

        sendBroadcast(broadcast);
        Log.d(LOG_TAG, "onFailure");
    }

    public void cancel() {
        sendBroadcast(new Intent(ChefService.ACTION_CANCEL));
        Log.d(LOG_TAG, "onCancel");
    }

    public void retry(int retryNo) {
        sendBroadcast(new Intent(ChefService.ACTION_RETRY));
        Log.d(LOG_TAG, String.format("onRetry: %d", retryNo));
    }

    public void finish() {
        sendBroadcast(new Intent(ChefService.ACTION_FINISH));
        Log.d(LOG_TAG, "onFinish");
    }
}
