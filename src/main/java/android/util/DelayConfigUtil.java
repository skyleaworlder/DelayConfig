package android.util;

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

    /* package */ static Integer getLineNumber() {
        Thread t = Thread.currentThread();
        StackTraceElement[] stackTraces = t.getStackTrace();
        if (stackTraces.length < 3) {
            return 0;
        }
        return Thread.currentThread().getStackTrace()[2].getLineNumber();
    }
}
