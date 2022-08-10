package android.util;

import java.io.*;
import java.util.Properties;

/**
 * @hide
 */
public class DelayProperties {
    private static final String TAG = "DelayProperties";

    private static String APP_NAME;

    public static void setAppName(String aName) {
        APP_NAME = aName;
    }

    private static Properties props = new Properties();

    public static boolean isEmpty() {
        return props.isEmpty();
    }

    public static void load(InputStream ins) throws IOException {
        //// Log.i(TAG, "load file: " + ins);
        props.load(ins);
    }

    public static void store(OutputStream outs) throws IOException {
        //// Log.i(TAG, "store file: " + outs);
        props.store(outs, "");
    }

    /**
     * get delay time of thread
     * @param tName thread name
     * @return null => 0
     */
    public static Integer getDelayTime(String aName, String tName) {
        if (props == null || !APP_NAME.equals(aName)) {
            return 0;
        }

        String value = props.getProperty(tName);
        if (value.equals("null")) {
            return 0;
        }
        Integer res = Integer.parseInt(value);
        return res;
    }

    public static boolean insertDelayPoint(String aName, String tName, Integer delay) {
        if (props == null || !APP_NAME.equals(aName)) {
            return false;
        }
        props.put(tName, delay.toString());
        return true;
    }
}
