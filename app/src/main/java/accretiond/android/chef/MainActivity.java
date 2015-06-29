package accretiond.android.chef;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.IconicsButton;
import android.view.IconicsImageView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;

import javax.inject.Inject;

import accretiond.android.chef.coap.CoapQueueSizeEvent;
import accretiond.android.chef.coap.CoapSuccessEvent;
import accretiond.android.chef.coap.CoapTaskQueue;
import accretiond.android.chef.fragment.Orders;

import accretiond.android.chef.util.LogReaderAsyncTask;
import accretiond.android.chef.util.UtilLogger;


public class MainActivity extends ActionBarActivity implements AdapterView.OnItemClickListener{

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;


    private static final int QUEUE_SIZE = 1;
    private static final int START = 1;
    private static final int STOP = 2;
    private static final int UP = 3;
    private static final int DOWN = 4;
    //    private Button btnStartStop;
    private boolean running = false;
    private Intent coapServerIntent;
    private static final long MILLIS = 1000;
    private static final long SECONDS = 60;
    private static final int SECONDS_AFTER_REBOOT = 10;
    public HashMap<Integer,Integer> ids= new HashMap<Integer,Integer>();
    @Inject
    CoapTaskQueue queue; // NOTE: Injection starts queue processing!
    @Inject Bus bus;
//    private Button btnStartStop1;

//    private Button xm_on;
//    private Button xm_strength;
//    private Button xm_channel_info;

    private TextView xm_status;
    //private TextView xm_info;

    private IconicsImageView start;
    private IconicsImageView stop;

    private Button bt_xm;
    private Button bt_fm;

    private RelativeLayout rl_xm;

    private RelativeLayout rl_fm;
    private int change2;
    private IconicsImageView up;
    private IconicsImageView down;
    private LogReaderAsyncTask task;
    public static UtilLogger log;
    //private TextView xm_signal;
    private ListView chan_list;
    private TextView tv_fm;
    private TextView tv_xm;
    private IconicsButton xm_refresh;
    private int value=-1;
    private long delay=30000;


    private TextView tvJni;

    static SparseArray<String> channelsInfo= new SparseArray<String>();
    ArrayList<ChannelItem> channelsItems= new ArrayList<ChannelItem>();
    private Toolbar toolbar;
    private Orders f_orders;

    private void startUpMainLog() {

        Runnable mainLogRunnable = new Runnable(){

            @Override
            public void run() {
                task = new LogReaderAsyncTask(MainActivity.this, log);
                task.execute((Void)null);
            }
        };

        if (task != null) {
            // do only after current log is depleted, to avoid splicing the streams together
            // (Don't cross the streams!)
            task.unpause();
            task.setOnFinished(mainLogRunnable);
            task.killReader();
            task = null;
        } else {
            // no main log currently running; just start up the main log now
            mainLogRunnable.run();
        }
    }

    private void pauseOrUnpause() {
        LogReaderAsyncTask currentTask = task;

        if (currentTask != null) {
            if (currentTask.isPaused()) {
                currentTask.unpause();
            } else {
                currentTask.pause();
            }

        }
    }

    public void showOrdersFragment() {
        f_orders= Orders.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        ft.add(R.id.content_default, f_orders);
        ft.addToBackStack(Orders.TAG);
        ft.commitAllowingStateLoss();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((ChefApplication) getApplication()).inject(this);
        log = new UtilLogger(MainActivity.class.getSimpleName());
        //coapServerIntent = new Intent(this, AutoDiagnosticsService.class);
        //CoapInstance.observe();
        toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setTitle("Savorite");
        setSupportActionBar(toolbar);
     showOrdersFragment();


    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_done).setIcon(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_account_child).color(Color.WHITE).actionBarSize());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_done) {
            return true;
        }

        return false; //super.onOptionsItemSelected(item);
    }






    public static int randInt(int min, int max) {

        // NOTE: Usually this should be a field rather than a method
        // variable so that it is not re-seeded every call.
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }



    @Override
    protected void onStop() {
        if(running){
            stopService(coapServerIntent);
        }
        super.onStop();
    }




    public void startDiagnosticPollingService(Context context,long repeatTime){
        AlarmManager service = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, Orders.class);
        PendingIntent pending = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        // Start x seconds after boot completed
        cal.add(Calendar.SECOND, SECONDS_AFTER_REBOOT);
        service.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), repeatTime, pending);

    }

    @SuppressWarnings("UnusedDeclaration") // Used by event bus.
    @Subscribe
    public void onQueueSizeChanged(CoapQueueSizeEvent event) {

    }

    @SuppressWarnings("UnusedDeclaration") // Used by event bus.
    @Subscribe public void onUploadSuccess(CoapSuccessEvent event) {


    }

    @Override protected void onResume() {
        super.onResume();
        //gpsActivationFlow();
        bus.register(this); // Register for events when we are becoming the active activity.
        // pauseOrUnpause();

    }

    @Override protected void onPause() {
        super.onPause();
        bus.unregister(this); // Unregister from events when we are no longer active.
    }





}
