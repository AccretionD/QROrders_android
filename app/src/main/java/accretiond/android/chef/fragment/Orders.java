package accretiond.android.chef.fragment;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.dexafree.materialList.cards.BasicButtonsCard;
import com.dexafree.materialList.cards.BasicImageButtonsCard;
import com.dexafree.materialList.cards.BasicListCard;
import com.dexafree.materialList.cards.BigImageButtonsCard;
import com.dexafree.materialList.cards.BigImageCard;
import com.dexafree.materialList.cards.OnButtonPressListener;
import com.dexafree.materialList.cards.SimpleCard;
import com.dexafree.materialList.cards.SmallImageCard;
import com.dexafree.materialList.cards.WelcomeCard;
import com.dexafree.materialList.controller.OnDismissCallback;
import com.dexafree.materialList.controller.RecyclerItemClickListener;
import com.dexafree.materialList.model.Card;
import com.dexafree.materialList.model.CardItemView;
import com.dexafree.materialList.view.MaterialListView;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import accretiond.android.chef.MainActivity;
import accretiond.android.chef.R;
import accretiond.android.chef.coap.CoapInstance;
import accretiond.android.chef.md.OrderItem;
import accretiond.android.chef.md.OrderItems;
import accretiond.android.chef.util.UtilLogger;


public class Orders extends Fragment implements CoapInstance.CoapListener{
    private Context mContext;
    private MaterialListView mListView;

    public static final String TAG = Orders.class.getSimpleName();
    private UtilLogger log;

    public static Orders newInstance() {
        // TODO Auto-generated method stub

        Orders instance= new Orders();
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.orders, container, false);
        log = new UtilLogger(MainActivity.class.getSimpleName());
        // Save a reference to the context
        mContext = getActivity();

        // Bind the MaterialListView to a variable
        mListView = (MaterialListView) rootView.findViewById(R.id.material_listview);

        // Set the dismiss listener
        mListView.setOnDismissCallback(new OnDismissCallback() {
            @Override
            public void onDismiss(Card card, int position) {

                // Recover the tag linked to the Card
                String tag = card.getTag().toString();

                // Show a toast
                Toast.makeText(mContext, "You have dismissed a " + tag, Toast.LENGTH_SHORT).show();
            }
        });

        // Add the ItemTouchListener
        mListView.addOnItemTouchListener(new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(CardItemView view, int position) {
                Log.d("CARD_TYPE", view.getTag().toString());
            }

            @Override
            public void onItemLongClick(CardItemView view, int position) {
                Log.d("LONG_CLICK", view.getTag().toString());
            }
        });
        CoapInstance.observe();
        CoapInstance.setCoapCMD(this);
        return  rootView;

    }




    private Card createCard(String text, String table,String n) {

        WelcomeCard card = new WelcomeCard(getActivity());
        card.setTitle("table #"+table);
        card.setDescription("Cantidad:"+n);
        card.setTag("WELCOME_CARD");
        ((WelcomeCard) card).setSubtitle(text);
        //((WelcomeCard) card).setButtonText("Okay!");
        //((WelcomeCard) card).setOnButtonPressedListener(new OnButtonPressListener() {
         //   @Override
          //  public void onButtonPressedListener(View view, Card card) {
          //      Toast.makeText(mContext, "Welcome!", Toast.LENGTH_SHORT).show();
          //  }
        //});


        ((WelcomeCard) card).setBackgroundColorRes(R.color.background_material_dark);
        card.setDismissible(true);



        return card;
    }






    @Override
    public void coap_cmd(final String cmd) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONArray ar = new JSONArray(cmd);
                    for (int i=0 ;i< ar.length();i++) {
                        JSONObject obj= (JSONObject) ar.get(i);
                        OrderItem a =  getOrderFromJSON(obj.toString());
                        Card card = createCard(a.description,a.table, a.qty);
                        mListView.add(card);
                    }
                } catch (JSONException e) {
                    log.e(e.getMessage());
                }


            }
        });


    }

    public  String getJSONFeature(OrderItems orders){
        if(orders != null) return new Gson().toJson(orders);
        return null;
    }

    public  OrderItems getOrdersFromJSON(String jSON){
        if(jSON.equals(""))return null;
        else return new Gson().fromJson(jSON, OrderItems.class);
    }

    public  OrderItem getOrderFromJSON(String jSON){
        if(jSON.equals(""))return null;
        else return new Gson().fromJson(jSON, OrderItem.class);
    }
}
