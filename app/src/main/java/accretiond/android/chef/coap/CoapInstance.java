package accretiond.android.chef.coap;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;



import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Helper class that is used to provide references to initialized
 * RequestQueue(s) and ImageLoader(s)
 * 
 * @author Ognyan Bankov
 * 
 */
public class CoapInstance {
	// private static final int MAX_IMAGE_CACHE_ENTIRES = 100;
    private static final String COLLECTOR_TESTING4 = "coap://192.168.1.56:5683/r/hello";
    private static final String SERVER_A = "coap://192.168.42.1:5683/r/order";

    //private static final String SERVER_A = "coap://192.168.42.11::5683/r/order";
    //private static final String SERVER_A = "coap://filomenu.order:5683/r/order";
    //private static final String SERVER_PROD = "coap://52.6.145.227:5683/r/";
    //private static final String SERVER_PROD = "coap://52.6.129.243:5683/r/";

    private static final String SERVER_PROD = "coap://52.5.123.253:5683/r/";
    //private static final String SERVER_PROD = "coap://52.7.33.126:5683/r/";
    private static final String SERVER_B = "coap://192.168.2.216:5683/r/";
    private static final String COLLECTOR_PRODUCTION = "coap://54.68.246.223:5683/r/hello";
    private static final String COLLECTOR_TESTING2 = "coap://192.168.10.148:5683/r/hello";

    private static final String policy_report="dismiss";
    private static final String policy_order="new_policy";
    public static final String COLLECTOR_POLICIES_REPORT = SERVER_PROD+policy_report;
    public static final String COLLECTOR_POLICIES_ORDERS = SERVER_PROD+policy_order;

    private static final int EVENT = 0;
    private static URI uri_server;
    private Context californiumCtx;
    private static CoapClient client;
    private static String TAG=CoapInstance.class.getSimpleName();
    public  static CoapListener coapCMD;

    public interface CoapListener{
        public void coap_cmd(String cmd);
    }

    public static void setCoapCMD(CoapListener listen) {
        coapCMD = listen;
    }

    private CoapInstance() {
		// no instances
	}

    public static void connect(Context context) {
        Context coapContext= null;
        try {
            coapContext = context.createPackageContext("accretiond.android.chef", Context.CONTEXT_IGNORE_SECURITY);
            uri_server = new URI(SERVER_A);
            client = new CoapClient(uri_server, coapContext.getFilesDir().getAbsolutePath());

        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG,""+e.getMessage());

        } catch (URISyntaxException e) {
            Log.e(TAG,""+e.getMessage());

        }
    }

	public static void init(Context context) {
        if(client==null){
            connect(context);
        }
        else{
            client.shutdown();
            client=null;
           connect(context);
        }
	}

	public static CoapClient getCoapInstance() {

		if (client != null) {
			return client;
		} else {

			throw new IllegalStateException("Coap not initialized");
		}
	}
    public static void restartListener(){
        if(client!=null){

            client.delete(dismissedListener);
            client.observeAndWait(dismissedListener);
        }

    }
    public static void observe(){
        if(client!=null){

                client.setURI(SERVER_A);
                client.observeAndWait(new CoapHandler() {
                    @Override
                    public void onLoad(CoapResponse response) {
                        if (response != null) {
                            String content = response.getResponseText();
                            if(coapCMD!=null)
                                coapCMD.coap_cmd(content);
                            Log.e(TAG, "POLICY LISTENER  response: " + content + " /" + Utils.prettyPrint(response));
                        }

                    }

                    @Override
                    public void onError() {
                        Log.d(TAG, "ERROR");
                    }
                });


            //client.observeAndWait(dismissedListener);
        }

    }
    public static void setModeCON(){
        if(client==null) {
            client.useCONs();
        }
    }
    public static void setModeNON(){
        if(client==null) {
            client.useNONs();
        }
    }

    public static CoapHandler dismissedListener;
    /*public static CoapHandler dismissedListener= new CoapHandler() {
        @Override public void onLoad(CoapResponse response) {
            String content = response.getResponseText();
            Log.d(TAG, "POLICY LISTENER  response: " + content);
        }

        @Override public void onError() {
            Log.d(TAG, "ERROR" );
        }
    };
    */
}