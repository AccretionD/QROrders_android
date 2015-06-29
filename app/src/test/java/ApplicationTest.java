//
//import org.junit.Test;
//import static org.hamcrest.CoreMatchers.is;
//import static org.hamcrest.MatcherAssert.assertThat;
//
//public class ApplicationTest {
//    @Test
//    public void checkJUnitWork() {
//        // failing test gives much better feedback
//        // to show that all works correctly ;)
//        assertThat(true, is(true));
//    }
//}
//
//import android.app.Activity;
//import android.widget.TextView;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.robolectric.Robolectric;
//
//import to.ideas.dtmclient.MainActivity;
//
//import static org.hamcrest.CoreMatchers.equalTo;
//import static org.hamcrest.MatcherAssert.assertThat;
//
//@RunWith(CustomRobolectricRunner.class)
//public class ApplicationTest {
//    @Test
//    public void testIt() {
//        Activity activity =
//                Robolectric.setupActivity(MainActivity.class);
//
//        TextView results =
//                (TextView) activity.findViewById(R.id.textView);
//        String resultsText = results.getText().toString();
//
//        // failing test gives much better feedback
//        // to show that all works correctly ;)
//        assertThat(resultsText, equalTo("Testing Android Rocks!"));
//    }
//}
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import accretiond.android.chef.MainActivity;
import accretiond.android.chef.R;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = "app/src/main/AndroidManifest.xml", resourceDir = "res", emulateSdk = 19)
public class ApplicationTest {

    @Test
    public void testActivity() {
        MainActivity splashActivity = new MainActivity();
        String appName = splashActivity.getString(R.string.app_name); // HERE, line 20
       // assertEquals(appName, "DTMClient");
        assertThat(appName, equalTo("DTMClient"));
    }

}