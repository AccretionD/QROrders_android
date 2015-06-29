package accretiond.android.chef;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import accretiond.android.chef.fragment.Orders;


public class ChefReceiver extends BroadcastReceiver {
	


	@Override
	public void onReceive(Context context, Intent intent) {

        Log.d(ChefService.class.getSimpleName(), "awake  "+intent.getAction());
        //Intent service = new Intent(context, DTMService.class);
		Intent service = new Intent(context, Orders.class);
		context.startService(service);
	}
}
