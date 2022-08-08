package android.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @hide
 */
public class DelayMap {
    private static final String TAG = "DelayMap";
    private static final int DELAY_LOW_BOUND = 0;
    private static final int DELAY_HIGH_BOUND = 1000;

    public static class DelayPoint {
        public String className;
        public Integer loc;
        public Integer delay;

        public String key() {
            return className + ":" + loc;
        }

        public String toString() {
            return key() + "(" + delay + ")";
        }

        public static String composeKey(String className, Integer loc) {
            return className + ":" + loc;
        }

        public static DelayPoint newInstance(String className, Integer loc, Integer delay) {
            DelayPoint dp = new DelayPoint();
            dp.className = className;
            dp.loc = loc;
            dp.delay = delay;
            return dp;
        }
    }

    public static String APP_NAME;

    // store threads->delay points
    public static Map<String, Map<String, DelayPoint>> M = new HashMap<>();


    /**
     * insertDelayPoint only insert DelayPoint.
     * 1. if identical DelayPoint already exists, update it.
     * 2. if app name not existed, create a k-v.
     * 3. if thread name not existed, craete a k-v.
     *
     * this method don't throw exception.
     * @param aName
     * @param tName
     * @param dp
     * @return return false when input DelayPoint already existed or app name not paired
     */
    public static boolean insertDelayPoint(String aName, String tName, DelayPoint dp) {
        boolean existed = false;
        if (!APP_NAME.equals(aName)) {
            return true;
        }
        if (!M.containsKey(tName)) {
            M.put(tName, new HashMap<>());
        }
        Map<String, DelayPoint> threadConfig = M.get(tName);
        existed = threadConfig.containsKey(dp.key());
        // update even if key already existed
        threadConfig.put(dp.key(), dp);
        return !existed;
    }

    /**
     * getDelayTime (millisecond)
     * 1. if app not existed, return 0.
     * 2. if thread not existed, return 0.
     * 3. if dp not existed, return 0.
     *
     * this method don't throw exception.
     * @param aName
     * @param tName
     * @param className
     * @param loc
     * @return delay time (Integer)
     */
    public static Integer getDelayTime(
            String aName, String tName, String className, Integer loc) {
        if (!APP_NAME.equals(aName)) {
            return 0;
        }
        if (!M.containsKey(tName)) {
            return 0;
        }
        Map<String, DelayPoint> threadConfig = M.get(tName);
        String key = DelayPoint.composeKey(className, loc);
        if (!threadConfig.containsKey(key)) {
            return 0;
        }
        return threadConfig.get(key).delay;
    }

    /**
     * update all delay time using random generator.
     *
     * @return
     */
    public static void updateAllDelayTime() {
        for (Map.Entry<String, Map<String, DelayPoint>> entry : M.entrySet()) {
            String tName = entry.getKey();
            Map<String, DelayPoint> threadConfig = entry.getValue();
            for (Map.Entry<String, DelayPoint> entry1 : threadConfig.entrySet()) {
                Integer newDelayTime = RandomGenerator.rand(DELAY_LOW_BOUND, DELAY_HIGH_BOUND);
                DelayPoint dp = entry1.getValue();
                Log.i(TAG, "update delay time: " + dp + " to " + newDelayTime);
                dp.delay = newDelayTime;
            }
        }
    }

    /**
     * update new delay
     * @param aName
     * @param tName
     * @param className
     * @param loc
     * @param delay
     * @return
     */
    public static boolean updateDelayTime(
            String aName, String tName, String className, Integer loc, Integer delay) {
        if (!APP_NAME.equals(aName)) {
            return false;
        }
        if (!M.containsKey(tName)) {
            return false;
        }
        Map<String, DelayPoint> threadConfig = M.get(tName);
        String key = DelayPoint.composeKey(className, loc);
        if (!threadConfig.containsKey(key)) {
            return false;
        }
        threadConfig.get(key).delay = delay;
        return true;
    }

    /**
     * DelayMap.M => XML String
     * @return XML content
     */
    public static String serialize() {
        StringBuilder builder = new StringBuilder();
        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");

        builder.append("<app>\n");
        String aname = DelayConfigHelper.getAppName();
        builder.append("\t<aname>" + aname + "</aname>\n");
        builder.append("\t<threads>\n");

        for (Map.Entry<String, Map<String, DelayPoint>> entry1 : M.entrySet()) {
            builder.append("\t\t<thread>\n");
            String tname = entry1.getKey();
            builder.append("\t\t\t<tname>" + tname + "</tname>\n");
            builder.append("\t\t\t<delaypoints>\n");

            for (Map.Entry<String, DelayPoint> entry2 : entry1.getValue().entrySet()) {
                builder.append("\t\t\t\t<delaypoint>\n");
                DelayPoint dp = entry2.getValue();
                builder.append("\t\t\t\t\t<class>" + dp.className + "</class>\n");
                builder.append("\t\t\t\t\t<loc>" + dp.loc + "</loc>\n");
                builder.append("\t\t\t\t\t<delay>" + dp.delay + "</delay>\n");
                builder.append("\t\t\t\t</delaypoint>\n");
            }

            builder.append("\t\t\t</delaypoints>\n");
            builder.append("\t\t</thread>\n");
        }

        builder.append("\t</threads>\n");
        builder.append("</app>\n");

        return builder.toString();
    }

    /**
     * to check whether M is empty (no delay point)
     * @return if there is no delay point
     */
    public static boolean isEmpty() {
        int dpCount = 0;
        for (Map.Entry<String, Map<String, DelayPoint>> entry : M.entrySet()) {
            Map<String, DelayPoint> threadConfig = entry.getValue();
            // threadConfig size is the number of delay point in this thread
            dpCount += threadConfig.size();
        }
        return dpCount == 0;
    }

    /**
     * to check whether delay value in DelayMap.M all ZERO
     * if all zero => use random to update config
     * else => config setup manually, don't need to update config, just run
     * @return
     */
    public static boolean isDelayAllZero() {
        for (Map.Entry<String, Map<String, DelayPoint>> entry : M.entrySet()) {
            Map<String, DelayPoint> threadConfig = entry.getValue();
            for (Map.Entry<String, DelayPoint> entry1 : threadConfig.entrySet()) {
                DelayPoint dp = entry1.getValue();
                if (dp.delay != 0) {
                    return false;
                }
            }
        }
        return true;
    }
}
