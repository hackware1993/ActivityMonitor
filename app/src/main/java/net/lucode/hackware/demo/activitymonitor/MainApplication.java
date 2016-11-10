package net.lucode.hackware.demo.activitymonitor;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.widget.Toast;

import net.lucode.hackware.activitymonitor.ActivityMonitor;
import net.lucode.hackware.activitymonitor.ActivityState;

/**
 * Created by hackware on 2016/11/10.
 */

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {

            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                ActivityMonitor.getInstance().onActivityEvent(activity, ActivityState.CREATED);
            }

            @Override
            public void onActivityStarted(Activity activity) {
                ActivityMonitor.getInstance().onActivityEvent(activity, ActivityState.STARTED);
            }

            @Override
            public void onActivityResumed(Activity activity) {
                ActivityMonitor.getInstance().onActivityEvent(activity, ActivityState.RESUMED);
            }

            @Override
            public void onActivityPaused(Activity activity) {
                ActivityMonitor.getInstance().onActivityEvent(activity, ActivityState.PAUSED);
            }

            @Override
            public void onActivityStopped(Activity activity) {
                ActivityMonitor.getInstance().onActivityEvent(activity, ActivityState.STOPPED);
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                ActivityMonitor.getInstance().onActivityEvent(activity, ActivityState.DESTROYED);
            }
        });

        final Toast toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        ActivityMonitor.getInstance().registerAppStateChangeListener(new ActivityMonitor.OnAppStateChangeListener() {
            @Override
            public void onAppStateChange(boolean foreground) {
                toast.setText("foreground: " + foreground);
                toast.show();
            }
        });
    }
}
