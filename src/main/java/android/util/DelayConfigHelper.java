package android.util;

//// import android.util.Log;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;

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
        //// Log.i(TAG, "set status: " + parseStatus(newStatus));
    }

    private static final String sSystemConfigPath = "./system/config/";
    private static String sAppName;

    public static String getAppName() {
        return sAppName;
    }

    public static void setAppName(String aName) {
        sAppName = aName;
        DelayMap.APP_NAME = aName;
    }

    /**
     * return config path.
     * if app name is null => ./system/config/config.xml
     * else => ./system/config/${sAppName}.config.xml
     * @return
     */
    private static String getConfigFilePath() {
        if (sAppName == null) {
            return sSystemConfigPath + "config.xml";
        }
        return sSystemConfigPath + sAppName + ".config.xml";
    }

    /**
     * return last run config store path.
     * every time test apk run, record the delay config in the following path:
     * if app name is null => ./system/config/lastrun.config.xml
     * else => ./system/config/${sAppName}.lastrun.config.xml
     * @return
     */
    private static String getLastRunConfigFilePath() {
        if (sAppName == null) {
            return sSystemConfigPath + "lastrun.config.xml";
        }
        return sSystemConfigPath + sAppName + ".lastrun.config.xml";
    }

    public static void readConfig()
            throws ParserConfigurationException, SAXException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        XMLReader xmlReader = factory.newSAXParser().getXMLReader();

        // use self-defined ContentHandler & parse xml
        ContentHandler handler = new DelayContentHandler();
        xmlReader.setContentHandler(handler);

        // hard code config xml file
        File file = new File(getConfigFilePath());
        try (InputStream ins = new FileInputStream(file)) {
            InputSource source = new InputSource(ins);
            xmlReader.parse(source);
        } catch (FileNotFoundException e) {
            // config not found means that
            // this run aims to push some k-v pair to HashMap
            // we don't need to delay when executing sleep
            setStatus(STATUS.INITIALIZING);
            //// Log.i(TAG, "no config file. normally initialize " + sAppName);
            return;
        } catch (IOException | SAXException e) {
            setStatus(STATUS.CONFIG_FILE_ERROR);
            //// Log.e(TAG, "unexpected error:");
            //// e.printStackTrace();
            return;
        }

        // if all delay is zero, then update all delay time;
        // if not all delay zero, means config.xml config well.
        if (DelayMap.isDelayAllZero()) {
            setStatus(STATUS.DELAY_ALL_ZERO);
            DelayMap.updateAllDelayTime();
            //// Log.i(TAG, "update all delay time");
        } else {
            setStatus(STATUS.DELAY_CONFIG_SETUP);
            //// Log.i(TAG, "delay config has already setup");
        }
        return;
    }

    /**
     * write config xml to disk
     * not matter what happen, write xml to "lastrun config.xml"
     *
     * only write to config.xml when STATUS.INITIALIZING
     *
     * @throws IOException
     */
    public static void writeConfig()
            throws IOException {
        byte[] content = DelayMap.serialize().getBytes();
        if (sStatus == STATUS.INITIALIZING) {
            String path = getConfigFilePath();
            //// Log.i(TAG, "write config to config.xml (normally): " + path);
            FileOutputStream fos = new FileOutputStream(path);
            fos.write(content);
            fos.close();
        }
        String path = getLastRunConfigFilePath();
        //// Log.i(TAG, "write config to config.xml (lastrun): " + path);
        FileOutputStream fosLastRun = new FileOutputStream(path);
        fosLastRun.write(content);
        fosLastRun.close();
    }

    /**
     * sleep is the only intrusive method call in AOSP framework
     *
     * if sStatus is INITIALIZING, sleep method only insert DelayPoint
     * else sleep method would execute Thread.sleep
     */
    public static void sleep() {
        String tName = Thread.currentThread().getName();
        String className = DelayConfigUtil.getOuterCallerClassName();
        Integer loc = DelayConfigUtil.getOuterCallerLineNumber();

        // when config is initializing,
        // DO NOT delay
        if (sStatus == STATUS.INITIALIZING) {
            DelayConfigUtil.insertDelayPoint(sAppName, tName, className, loc, 0);
            return;
        }

        Integer delay = DelayConfigUtil.getDelayTime(sAppName, tName, className, loc);
        try {
            //// Log.i(TAG, "helper let " + sAppName + " sleep " + delay + " ms at " + className + ":" + loc);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}