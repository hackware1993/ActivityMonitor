# ActivityMonitor
用于App前后台状态变化的工具类

# 引入

```groovy
repositories {
  ...
  maven {
      url "https://jitpack.io"
  }
}

dependencies {
  ...
  compile 'com.github.hackware1993:ActivityMonitor:1.0'
}
```

# 集成

```
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
    }
}
```

或者

```
public class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMonitor.getInstance().onActivityEvent(this, ActivityState.CREATED);
    }

    @Override
    protected void onStart() {
        super.onStart();
        ActivityMonitor.getInstance().onActivityEvent(this, ActivityState.STARTED);
    }

    ...
}
```

# 用法

1. 监听App的前后台变化

```
ActivityMonitor.getInstance().registerAppStateChangeListener(new ActivityMonitor.OnAppStateChangeListener() {
    @Override
    public void onAppStateChange(boolean foreground) {
        if (foreground) {
            ...
        } else {
            ...
        }
    }
});
```

2. 获取当前的Activity：可用于解决Activity跳转时，由于层层封装导致无法拿到当前Activity的引用从而无法重载过渡动画的问题

```
Activity currentActivity = ActivityMonitor.getInstance().getTopActivity();
if (currentActivity != null) {
    currentActivity.overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim);
}
```
