package android.util;

/**
 * @hide
 */
public class DelayConfigUtil {
    private static final String TAG = "DelayConfigUtil";

    /* package */ static Integer getDelayTime(
            String aName, String tName, String className, Integer loc) {
        return DelayMap.getDelayTime(aName, tName, className, loc);
    }

    /* package */ static boolean insertDelayPoint(
            String aName, String tName, String className, Integer loc, Integer delay) {
        DelayMap.DelayPoint dp = DelayMap.DelayPoint.newInstance(className, loc, delay);
        return DelayMap.insertDelayPoint(aName, tName, dp);
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
            Log.i(TAG, "(" + i + ") " + elem);
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
