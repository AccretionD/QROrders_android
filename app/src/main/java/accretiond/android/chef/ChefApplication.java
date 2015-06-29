package accretiond.android.chef;

import android.app.Application;
import android.content.Context;



import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.otto.Bus;


import org.acra.ACRA;
import org.acra.ACRAConfiguration;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import javax.inject.Singleton;

import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;
import accretiond.android.chef.coap.CoapInstance;
import accretiond.android.chef.coap.CoapTaskQueue;
import accretiond.android.chef.coap.CoapTaskService;
import accretiond.android.chef.util.UtilLogger;

@ReportsCrashes(  customReportContent = {
        ReportField.APP_VERSION_CODE, ReportField.ANDROID_VERSION,
        ReportField.PHONE_MODEL, ReportField.THREAD_DETAILS,
        ReportField.STACK_TRACE },mode = ReportingInteractionMode.SILENT,sendReportsAtShutdown=false, deleteOldUnsentReportsOnApplicationStart = false
        ,deleteUnapprovedReportsOnApplicationStart = false)
public class ChefApplication extends Application {
    private ObjectGraph objectGraph;

    @Override
    public void onCreate() {
        final ReportsCrashes reportsCrashes = this.getClass().getAnnotation(ReportsCrashes.class);
        ACRA.init(this, new ACRAConfiguration(reportsCrashes), false);
        ACRA.getErrorReporter().addReportSender(new UtilLogger(ACRA.class.getSimpleName()));
        CoapInit();
        super.onCreate();
        objectGraph = ObjectGraph.create(new CoapModule(this));

    }

    public void inject(Object object) {
        objectGraph.inject(object);
    }


    @Module(
            entryPoints = {
                    MainActivity.class, //
                    CoapTaskQueue.class, //
                    CoapTaskService.class //
            }
    )

    static class CoapModule {
        private final Context appContext;

        CoapModule(Context appContext) {
            this.appContext = appContext;
        }

        @Provides
        @Singleton
        CoapTaskQueue provideTaskQueue(Gson gson, Bus bus) {
            return CoapTaskQueue.create(appContext, gson, bus);
        }

        @Provides @Singleton
        Bus provideBus() {
            return new Bus();
        }

        @Provides @Singleton
        Gson provideGson() {
            return new GsonBuilder().create();
        }
    }

    private void CoapInit() {
        CoapInstance.init(this);
    }
    @Override
    public void onTerminate() {
        super.onTerminate();
    }

}

