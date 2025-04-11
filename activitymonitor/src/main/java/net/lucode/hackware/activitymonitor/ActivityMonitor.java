package com.toolmatrix.feedback.utils;

import android.app.Activity;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 用于获取当前用户交互的Activity的辅助类
 * 用户当前交互的Activity：取最近一个状态为resumed的Activity，如果没有一个Activity处于resumed状态
 * 则取最近一个状态为paused的Activity，如果没有一个Activity处于paused状态，则说明App被切换到了后台
 * Created by hackware on 2016/11/8.
 */

public class ActivityMonitor {
    private static final String TAG = "ActivityMonitor";
    private static final ActivityState[] ACTIVE_ORDER = {ActivityState.DESTROYED,
            ActivityState.STOPPED, ActivityState.CREATED, ActivityState.STARTED,
            ActivityState.PAUSED, ActivityState.RESUMED};
    private static final ActivityState[] STRICT_ACTIVE_ORDER = {ActivityState.DESTROYED,
            ActivityState.STOPPED, ActivityState.PAUSED, ActivityState.CREATED,
            ActivityState.STARTED, ActivityState.RESUMED};
    private static boolean sDebug = false;
    private static boolean sStrictForeground = true;
    private final List<ActivityEntry> mActivityEntries = new ArrayList<>();
    private boolean mAppForeground;
    private final List<OnAppStateChangeListener> mOnAppStateChangeListeners
            = new CopyOnWriteArrayList<>();

    private ActivityMonitor() {
    }

    public static ActivityMonitor getInstance() {
        return SingletonHolder.sInstance;
    }

    public static void setDebug(boolean debug) {
        sDebug = debug;
    }

    /**
     * 开启严格的前后台判断，只要有Activity处于created或started状态，就认为App在前台
     */
    public static void setStrictForeground(boolean strictForeground) {
        sStrictForeground = strictForeground;
    }

    private static int compare(ActivityState left, ActivityState right) {
        ActivityState[] order = sStrictForeground ? STRICT_ACTIVE_ORDER : ACTIVE_ORDER;
        return getOrder(order, left) - getOrder(order, right);
    }

    private static int getOrder(ActivityState[] order, ActivityState activityState) {
        for (int i = 0, j = order.length; i < j; i++) {
            if (order[i] == activityState) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 将Activity的生命周期回调传递进来，可使用BaseActivity的方式，也可使用lifeCircleListener
     *
     * @param activity      当前生命周期变化的Activity
     * @param activityState Activity当前的生命周期状态
     */
    public void onActivityEvent(Activity activity, ActivityState activityState) {
        updateActivityState(activity, activityState);
    }

    /**
     * 获取当前栈顶的Activity
     */
    public Activity getTopActivity() {
        if (mActivityEntries.isEmpty()) {
            return null;
        }
        return mActivityEntries.get(mActivityEntries.size() - 1).mActivityRef.get();
    }

    /**
     * 判断App是否在前台
     */
    public boolean isAppForeground() {
        if (mActivityEntries.isEmpty()) {
            return false;
        }
        ActivityEntry activityEntry = mActivityEntries.get(mActivityEntries.size() - 1);
        if (!sStrictForeground) {
            return activityEntry.mState == ActivityState.RESUMED ||
                    activityEntry.mState == ActivityState.PAUSED;
        } else {
            return activityEntry.mState == ActivityState.RESUMED ||
                    activityEntry.mState == ActivityState.PAUSED ||
                    activityEntry.mState == ActivityState.STARTED ||
                    activityEntry.mState == ActivityState.CREATED;
        }
    }

    private void updateActivityState(Activity activity, ActivityState activityState) {
        ActivityEntry activityEntry = new ActivityEntry(activity, activityState);
        mActivityEntries.remove(activityEntry);
        mActivityEntries.add(activityEntry);

        clearAndSort();

        boolean foreground = isAppForeground();
        if (mAppForeground != foreground) {
            mAppForeground = foreground;
            notifyAppStateChanged();
        }

        if (sDebug) {
            Activity topActivity = getTopActivity();
            Log.d(TAG, activity.getClass().getSimpleName() + " " + activityEntry.mState
                    + ", top activity is " + (topActivity == null ? "null"
                    : topActivity.getClass().getSimpleName()) + ", foreground = "
                    + isAppForeground() + ", activities = " + mActivityEntries.size());
        }
    }

    /**
     * 根据Activity的状态排序，并移除已销毁Activity的状态，末尾的Activity即是当前用户交互的Activity
     */
    private void clearAndSort() {
        mActivityEntries.removeIf(activityEntry -> activityEntry.mState == ActivityState.DESTROYED);
        mActivityEntries.sort((lhs, rhs) -> compare(lhs.mState, rhs.mState));
    }

    public void registerAppStateChangeListener(OnAppStateChangeListener onAppStateChangeListener) {
        if (!mOnAppStateChangeListeners.contains(onAppStateChangeListener)) {
            mOnAppStateChangeListeners.add(onAppStateChangeListener);
        }
    }

    public void removeAppStateChangeListener(OnAppStateChangeListener onAppStateChangeListener) {
        mOnAppStateChangeListeners.remove(onAppStateChangeListener);
    }

    private void notifyAppStateChanged() {
        for (OnAppStateChangeListener onAppStateChangeListener : mOnAppStateChangeListeners) {
            onAppStateChangeListener.onAppStateChange(mAppForeground);
        }
    }

    /**
     * App前后台状态变化的回调
     */
    public interface OnAppStateChangeListener {
        void onAppStateChange(boolean foreground);
    }

    private static class SingletonHolder {
        static ActivityMonitor sInstance = new ActivityMonitor();
    }

    private static class ActivityEntry {
        WeakReference<Activity> mActivityRef;
        ActivityState mState;

        ActivityEntry(Activity activity, ActivityState activityState) {
            mActivityRef = new WeakReference<>(activity);
            mState = activityState;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o instanceof ActivityEntry) {
                ActivityEntry activityEntry = (ActivityEntry) o;
                return mActivityRef == activityEntry.mActivityRef
                        || !(mActivityRef == null || activityEntry.mActivityRef == null)
                        && mActivityRef.get() == activityEntry.mActivityRef.get();
            }
            return false;
        }
    }
}
