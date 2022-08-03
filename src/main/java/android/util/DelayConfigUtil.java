package android.util;

/**
 * @hide
 */
public class DelayConfigUtil {
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
        // 0 -> getStackTrace
        // 1 -> getLineNumber
        // 2 -> sleep
        // 3 -> outside
        if (stackTraces.length < 4) {
            return 0;
        }
        return Thread.currentThread().getStackTrace()[3].getLineNumber();
    }

    /* package */ static String getOuterCallerClassName() {
        Thread t = Thread.currentThread();
        StackTraceElement[] stackTraces = t.getStackTrace();
        // 0 -> getStackTrace
        // 1 -> getLineNumber
        // 2 -> sleep
        // 3 -> outside
        if (stackTraces.length < 4) {
            return "";
        }
        return Thread.currentThread().getStackTrace()[3].getClassName();
    }
}
