package android.util;

/**
 * @hide
 */
public class DelayConfigUtil {
    private static final String TAG = "DelayConfigUtil";

    /* package */ static Integer getDelayTimeFromDelayProperties(
            String aName, String tName) {
        return DelayProperties.getDelayTime(aName, tName);
    }

    /* package */ static boolean insertDelayPointToDelayProperties(
            String aName, String tName, Integer delay) {
        return DelayProperties.insertDelayPoint(aName, tName, delay);
    }

    /* package */ static Integer getOuterCallerLineNumber() {
        Thread t = Thread.currentThread();
        StackTraceElement[] stackTraces = t.getStackTrace();
        // 0 -> dalvik.system.VMStack.getThreadStackTrace(Native Method)
        // 1 -> java.lang.Thread.getStackTrace
        // 2 -> android.util.DelayConfigUtil.getOuterCallerLineNumber
        // 3 -> android.util.DelayConfigHelper.sleep
        // 4 -> outside
        if (stackTraces.length < 5) {
            return 0;
        }

        int i = 0;
        for (StackTraceElement elem : stackTraces) {
            //// Log.i(TAG, "(" + i + ") " + elem);
            i++;
        }
        return stackTraces[4].getLineNumber();
    }

    /* package */ static String getOuterCallerClassName() {
        Thread t = Thread.currentThread();
        StackTraceElement[] stackTraces = t.getStackTrace();
        // 0 -> dalvik.system.VMStack.getThreadStackTrace(Native Method)
        // 1 -> java.lang.Thread.getStackTrace
        // 2 -> android.util.DelayConfigUtil.getOuterCallerLineNumber
        // 3 -> android.util.DelayConfigHelper.sleep
        // 4 -> outside
        if (stackTraces.length < 5) {
            return "";
        }
        return stackTraces[4].getClassName();
    }
}
