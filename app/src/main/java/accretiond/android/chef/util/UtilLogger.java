package accretiond.android.chef.util;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.engineio.client.EngineIOException;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.github.nkzawa.socketio.client.SocketIOException;
import com.google.gson.Gson;

import org.acra.ReportField;
import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;

import java.net.URISyntaxException;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import timber.log.Timber;

/**
 * Easier way to interact with logcat.
 * @author nolan
 */
public class UtilLogger extends Timber.HollowTree implements ReportSender {

    private static final int LOG_CHUNK_SIZE = 4000;
	public static final boolean DEBUG_MODE = true;
    private static final String NEW_LINE		= System.getProperty("line.separator");
    public static final String LOG_LEVEL_KEY	= "LOGLEVEL";
    private static final String LOG_AVAILABLE = "new_log";
    private static UtilLogger loggerInstance;

    private class Sender extends AsyncTask<String,Void,Void> {



        @Override
        protected Void doInBackground(String... args) {

            e(args[0]);
            return null;
        }




        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            d("onPostExecute()");

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            d("onPreExecute()");


        }
    }
    @Override
    public void send(Context ctx,CrashReportData errorContent) throws ReportSenderException {
        e("ACRA SEND CRASH");


        e(createCrashLog(errorContent));






    }


    private String createCrashLog(CrashReportData report) {
        Date now = new Date();
        StringBuilder log = new StringBuilder();

        log.append("Package: ").append(report.get(ReportField.PACKAGE_NAME)).append("\n");
        log.append("Version: ").append(report.get(ReportField.APP_VERSION_CODE)).append("\n");
        log.append("Android: ").append(report.get(ReportField.ANDROID_VERSION)).append("\n");
        log.append("Manufacturer: ").append(android.os.Build.MANUFACTURER).append("\n");
        log.append("Model: ").append(report.get(ReportField.PHONE_MODEL)).append("\n");
        log.append("Date: ").append(now).append("\n");
        log.append("\n");
        log.append(report.get(ReportField.STACK_TRACE));
        log.append("\n");

        // Print one-liners first
        for( Map.Entry<ReportField, String> reportField : report.entrySet() )
        {
            String value = reportField.getValue();
            if( !value.endsWith( "\n" ) )
            {
                log.append( reportField.getKey().name() + ": " + value + "\n" );
            }
        }

        // Then print big items
        for( Map.Entry<ReportField, String> reportField : report.entrySet() )
        {
            String value = reportField.getValue();
            if( value.endsWith( "\n" ) )
            {
                log.append( "-------------------------------------------------------------------------------------------\n" );
                log.append( reportField.getKey().name() + ":\n" );
                log.append( value );
            }
        }
        return log.toString();
    }
    private static final Gson gson = new Gson();
    public static boolean isJSONValid(String JSON_STRING) {
        try {
            gson.fromJson(JSON_STRING, Object.class);
            return true;
        } catch(com.google.gson.JsonSyntaxException ex) {
            return false;
        }
    }

//    @Override
//    public void log(String message) {
//        if(isJSONValid(message)){
//            logChunk(message);
//        }
//        else{
//            for (int i = 0, len = message.length(); i < len; i += LOG_CHUNK_SIZE) {
//                int end = Math.min(len, i + LOG_CHUNK_SIZE);
//                logChunk(message.substring(i, end));
//            }
//
//        }
//
//    }
    public void logChunk(String chunk) {
        d(chunk);
    }

    class Info{
        public String errorStack;
    }
	class ToJson{
        public int level;
        public String message;
        public Object jsonData;
        public Info info;

    }
	private String tag;
    private Socket socket;

    private static final int MAX_LOG_LENGTH = 4000;
    private static final Pattern ANONYMOUS_CLASS = Pattern.compile("\\$\\d+$");
    private static final ThreadLocal<String> NEXT_TAG = new ThreadLocal<String>();


    /**
     * Returns an explicitly set tag for the next log message or {@code null}. Calling this method
     * clears any set tag so it may only be called once.
     */
    protected final String nextTag() {
        String tag = NEXT_TAG.get();
        if (tag != null) {
            NEXT_TAG.remove();
        }
        return tag;
    }

    public static synchronized UtilLogger getInstance(String tag) {
        if(loggerInstance==null){
            loggerInstance= new UtilLogger();
            loggerInstance.tag = tag;
            Timber.plant(loggerInstance);
            loggerInstance.startSocket();
            NEXT_TAG.set(tag);
        }
        return loggerInstance;

    }

    public  UtilLogger () {

    }

    public UtilLogger(String tag) {

		this.tag = tag;
        Timber.plant(this);
        startSocket();

        NEXT_TAG.set(tag);
	}
	
	/*public UtilLogger(Class<?> clazz) {
		this.tag = clazz.getSimpleName();
	}*/

    private static String maybeFormat(String message, Object... args) {
        // If no varargs are supplied, treat it as a request to log the string without formatting.
        return args.length == 0 ? message : String.format(message, args);
    }


    @Override
    public final void v(String message, Object... args) {
        throwShade(Log.VERBOSE, maybeFormat(message, args), null);


    }

    @Override
    public final void v(Throwable t, String message, Object... args) {

        throwShade(Log.VERBOSE, maybeFormat(message, args), t);
    }

    @Override
    public final void d(String message, Object... args) {
        throwShade(Log.DEBUG, maybeFormat(message, args), null);
    }

    @Override
    public final void d(Throwable t, String message, Object... args) {
        throwShade(Log.DEBUG, maybeFormat(message, args), t);
    }

    @Override
    public final void i(String message, Object... args) {
        throwShade(Log.INFO, maybeFormat(message, args), null);
    }

    @Override
    public final void i(Throwable t, String message, Object... args) {
        throwShade(Log.INFO, maybeFormat(message, args), t);
    }

    @Override
    public final void w(String message, Object... args) {
        throwShade(Log.WARN, maybeFormat(message, args), null);
    }

    @Override
    public final void w(Throwable t, String message, Object... args) {
        throwShade(Log.WARN, maybeFormat(message, args), t);
    }

    @Override
    public final void e(String message, Object... args) {
        throwShade(Log.ERROR, maybeFormat(message, args), null);
    }

    @Override
    public final void e(Throwable t, String message, Object... args) {
        throwShade(Log.ERROR, maybeFormat(message, args), t);
    }




    protected String createTag() {
        String tag = nextTag();
        if (tag != null) {
            return tag;
        }

        // DO NOT switch this to Thread.getCurrentThread().getStackTrace(). The test will pass
        // because Robolectric runs them on the JVM but on Android the elements are different.
        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        if (stackTrace.length < 6) {
            throw new IllegalStateException(
                    "Synthetic stacktrace didn't have enough elements: are you using proguard?");
        }
        tag = stackTrace[5].getClassName();
        Matcher m = ANONYMOUS_CLASS.matcher(tag);
        if (m.find()) {
            tag = m.replaceAll("");
        }
        return tag.substring(tag.lastIndexOf('.') + 1);
    }

    private void throwShade(int priority, String message, Throwable t) {
        if (message == null || message.length() == 0) {
            if (t == null) {
                return; // Swallow message if it's null and there's no throwable.
            }
            message = Log.getStackTraceString(t);
        } else if (t != null) {
            message += "\n" + Log.getStackTraceString(t);
        }
        String tag = createTag();
        logMessage(priority, tag, message);


    }

    /** Log a message! */
    protected void logMessage(int priority, String tag, String message) {
        ToJson json= new ToJson();
        json.level=priority;
        if (message.length() < MAX_LOG_LENGTH) {
            Log.println(priority, tag, message);
            json.message=message;
            //json.jsonData=gson.fromJson(message, Object.class);
            if(socket!=null)
                socket.emit(LOG_AVAILABLE, new Gson().toJson(json));
            return;
        }
        /*
        else if(priority== Log.ERROR){
            Log.println(priority, tag, message);
            Info a= new Info();
            a.errorStack=message;
            json.info=a;
            json.jsonData=gson.fromJson(message, Object.class);
            json.message="CRASH HAPPENED";
            socket.emit(LOG_AVAILABLE, new Gson().toJson(json));

            return;
        }

        else if(isJSONValid(message)){
            Log.println(priority, tag, message);
            json.jsonData=gson.fromJson(message, Object.class);
            json.message="RestKitBody";
            socket.emit(LOG_AVAILABLE, new Gson().toJson(json));
            return;
        }
    */

        // Split by line, then ensure each line can fit into Log's maximum length.
        for (int i = 0, length = message.length(); i < length; i++) {
            int newline = message.indexOf('\n', i);
            newline = newline != -1 ? newline : length;
            do {
                int end = Math.min(newline, i + MAX_LOG_LENGTH);
                json.message=message.substring(i, end);

                socket.emit(LOG_AVAILABLE, new Gson().toJson(json));
                //Log.println(priority, tag, message.substring(i, end));
                i = end;
            } while (i < newline);
        }
    }


    public void startSocket(){
        IO.Options opts = new IO.Options();
        opts.port = 9000;
        try {
            //socket = IO.socket("http://192.168.10.148:9000");

            //socket = IO.socket("http://192.168.1.51:9000");
            socket = IO.socket("http://52.5.123.253:3000");
        } catch (URISyntaxException e) {
            e("Socket was disconnected:" + e.getMessage());

        }
        socket.on(Socket.EVENT_CONNECT_ERROR, new AccretionListener(Socket.EVENT_CONNECT_ERROR));
        socket.on(Socket.EVENT_CONNECT_TIMEOUT, new AccretionListener(Socket.EVENT_CONNECT_TIMEOUT));
        socket.on(Socket.EVENT_CONNECT, new AccretionListener(Socket.EVENT_CONNECT));
        socket.on(Socket.EVENT_DISCONNECT, new AccretionListener(Socket.EVENT_DISCONNECT));
        socket.on(Socket.EVENT_MESSAGE, new AccretionListener(Socket.EVENT_MESSAGE));
        socket.on("Event", new AccretionListener("Event"));
        socket.connect();
    }

    private class AccretionListener implements Emitter.Listener{
        private String event;
        public AccretionListener(String event){
            this.event=event;
        }
        @Override
        public void call(Object... args) {
            if(args.length>0){
                if(args[0] instanceof String){
                    String msg= (String)args[0];
                    d(event,(msg==null)?"string Unknown Error":""+msg);
                }
                else if(args[0] instanceof EngineIOException){
                    EngineIOException e=(EngineIOException) args[0];
                    d(event,(e==null)?"enginoio Unknown Error":""+e.getMessage());
                }
                else if(args[0] instanceof SocketIOException){
                    SocketIOException e=(SocketIOException) args[0];
                    d(event,(e==null)?"Socketio Unknown Error":""+e.getMessage());
                }
                else{
                    Object ret=args[0];
                    d(event,(ret==null)?" OBJECT Unknown Error":ret.getClass()+" ");
                }


            }
            else{
                d(event,"Unknown Error args return nothing");
            }

        }
    }




}
