package android.util;

//// import android.util.Log;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * ActivityThread <-> app / process <-> DelayConfigHelper
 * @hide
 */
public class DelayConfigHelper {

    private static final String TAG = "DelayConfigHelper";

    /**
     * INITIALIZING: config xml not exist, all config need emit
     * DELAY_ALL_ZERO: config xml well initialized, execute & sleep normally
     * DELAY_CONFIG_SETUP: config manually, no using random to sleep
     * CONFIG_FILE_ERROR: unknown error
     */
    public enum STATUS {
        INITIALIZING,
        DELAY_ALL_ZERO,
        DELAY_CONFIG_SETUP,
        CONFIG_FILE_ERROR,
        PERMISSION_DENIED
    }

    public static STATUS sStatus = STATUS.INITIALIZING;

    public static String parseStatus(STATUS status) {
        switch (status) {
            case INITIALIZING: return "INITIALIZING";
            case DELAY_ALL_ZERO: return "DELAY_ALL_ZERO";
            case DELAY_CONFIG_SETUP: return "DELAY_CONFIG_SETUP";
            case CONFIG_FILE_ERROR: return "CONFIG_FILE_ERROR";
            default: return "UNKNOWN";
        }
    }

    public static void setStatus(STATUS newStatus) {
        DelayConfigHelper.sStatus = newStatus;
        //// //// Log.i(TAG, "set status: " + parseStatus(newStatus));
    }

    private static String sAppName;

    public static String getAppName() {
        return sAppName;
    }

    public static void setAppName(String aName) {
        sAppName = aName;
        DelayProperties.setAppName(aName);
    }

    /**
     * read config
     * @param configPath writable folder path of current app.
     *                   use context.getFilesDir().getPath().toString(),
     *                   normally "/data/user/0/${debug app name}/files"
     */
    public static void readConfig(String configPath) {
        // read config xml file to HashMap
        File file = new File(configPath);
        try (InputStream ins = new FileInputStream(file)) {
            DelayProperties.load(ins);
            //// Log.i(TAG, "read config to DelayMap: " + path);
        } catch (FileNotFoundException e) {
            // Permission denied would also throw FileNotFoundException
            Path path = Paths.get(configPath);
            if (Files.exists(path) && !file.canRead()) {
                setStatus(STATUS.PERMISSION_DENIED);
                return;
            }
            // config not found means that
            // this run aims to push some k-v pair to HashMap
            // we don't need to delay when executing sleep
            setStatus(STATUS.INITIALIZING);
            //// Log.i(TAG, "no config file. normally initialize " + sAppName);
            return;
        } catch (IOException e) {
            setStatus(STATUS.CONFIG_FILE_ERROR);
            //// Log.e(TAG, "unexpected error:");
            e.printStackTrace();
            return;
        }

        if (DelayProperties.isEmpty()) {
            setStatus(STATUS.INITIALIZING);
            //// Log.i(TAG, "no delay point in config.xml");
            return;
        }
        setStatus(STATUS.DELAY_CONFIG_SETUP);
        //// Log.i(TAG, "delay config has already setup");
        return;
    }

    /**
     * write config xml to disk
     *
     * only write to config.xml when STATUS.INITIALIZING
     *
     * @param configPath writable folder path of current app.
     *                   use context.getFilesDir().getPath().toString(),
     *                   normally "/data/user/0/${debug app name}/files"
     * @throws IOException
     */
    public static void writeConfig(String configPath)
            throws IOException {
        if (sStatus == STATUS.INITIALIZING) {
            //// Log.i(TAG, "write config to config.xml (normally): " + configPath);
            FileOutputStream fos = new FileOutputStream(configPath);
            DelayProperties.store(fos);
            fos.close();
        }
    }

    /**
     * sleep is the only intrusive method call in AOSP framework
     *
     * if sStatus is INITIALIZING, sleep method only insert DelayPoint
     * else sleep method would execute Thread.sleep
     */
    public static void sleep() {
        String tName = Thread.currentThread().getName();

        // only sleep method insert DelayPoint,
        // if sAppName has not been set, it would be uncontrollable.
        // check sAppName is necessary.
        if (sAppName == null) {
            //// Log.i(TAG, "sleep called unexpected, return: " + "(" + tName + "|" + className + ":" + loc + ")");
            return;
        }

        //// Log.i(TAG, "sleep called normally: " + sAppName + " (" + tName + "|" + className + ":" + loc + ")");

        // when config is initializing,
        // DO NOT delay
        if (sStatus == STATUS.INITIALIZING) {
            //// Log.i(TAG, "helper insert a point(" + sAppName + "." + className + ":" + loc + ")");
            DelayConfigUtil.insertDelayPointToDelayProperties(sAppName, tName, 0);
            return;
        }

        Integer delay = DelayConfigUtil.getDelayTimeFromDelayProperties(sAppName, tName);
        try {
            //// Log.i(TAG, "helper let " + sAppName + " sleep " + delay + " ms at " + className + ":" + loc);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}