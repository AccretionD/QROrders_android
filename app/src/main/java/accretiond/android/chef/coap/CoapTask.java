/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package accretiond.android.chef.coap;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.squareup.tape.Task;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.msgpack.MessagePack;
import org.msgpack.packer.Packer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.regex.Pattern;

public class CoapTask implements Task<CoapTask.Callback> {

    private static final String TAG = "Tape:ImageUploadTask";
    private static final String IMGUR_API_KEY = "74e20e836f0307a90683c4643a2b656e";
    private static final String IMGUR_UPLOAD_URL = "http://api.imgur.com/2/upload";
    private static final Pattern IMGUR_URL_REGEX = Pattern.compile("<imgur_page>(.+?)</imgur_page>");
    private static final Handler MAIN_THREAD = new Handler(Looper.getMainLooper());
    private static final int EVENT = 0;
    private int var=-1;
    private String value="";
    private int state=-1;
    private int context=-1;

    public interface Callback {
        void onSuccess(String url);

        void onFailure();

    }
    public CoapTask(int var, String value, int context){
        this.var=var;
        this.value=value;
        this.context=context;


    }

    public CoapTask(int state, int context){
        this.state=state;
        this.context=context;



    }


    @Override public void execute(final Callback callback) {
        // Image uploading is slow. Execute HTTP POST on a background thread.
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {


                    MessagePack msgpack = new MessagePack();
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    Packer packer = msgpack.createPacker(out);
                    packer.write("");

                    byte[] bytes = out.toByteArray();

                    CoapClient coapCli= CoapInstance.getCoapInstance();
                    coapCli.setURI(CoapInstance.COLLECTOR_POLICIES_REPORT);

                    CoapResponse response = CoapInstance.getCoapInstance().put(bytes, MediaTypeRegistry.APPLICATION_OCTET_STREAM);

                    MAIN_THREAD.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess("");
                        }
                    });
                    if (response != null) {
                        Log.e(TAG, "response received" + "  _> " + response.getResponseText() + " /" + Utils.prettyPrint(response));
                        // Get back to the main thread before invoking a callback.
//                        MAIN_THREAD.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                callback.onSuccess("");
//                            }
//                        });
                    } else {
                        Log.e(TAG, "nothing received" + response);
                       // MAIN_THREAD.post(new Runnable() {
                        //    @Override
                        //    public void run() {
                        //        callback    .onFailure();
                        //    }
                        //});
                    }


                } catch (RuntimeException e) {
                    e.printStackTrace();
                    throw e;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
}
