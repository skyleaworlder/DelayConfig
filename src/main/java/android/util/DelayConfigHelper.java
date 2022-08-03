package android.util;

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

    public static void setStatus(STATUS newStatus) {
        DelayConfigHelper.sStatus = newStatus;
        // TODO: insert Log.i
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
            return;
        } catch (IOException | SAXException e) {
            setStatus(STATUS.CONFIG_FILE_ERROR);
            return;
        }

        // if all delay is zero, then update all delay time;
        // if not all delay zero, means config.xml config well.
        if (DelayMap.isDelayAllZero()) {
            setStatus(STATUS.DELAY_ALL_ZERO);
            DelayMap.updateAllDelayTime();
        } else {
            setStatus(STATUS.DELAY_CONFIG_SETUP);
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
            FileOutputStream fos = new FileOutputStream(getConfigFilePath());
            fos.write(content);
            fos.close();
        }
        FileOutputStream fosLastRun = new FileOutputStream(getLastRunConfigFilePath());
        fosLastRun.write(content);
        fosLastRun.close();
    }

    /**
     * sleep is the only intrusive method call in AOSP framework
     *
     * if sStatus is INITIALIZING, sleep method only insert DelayPoint
     * else sleep method would execute Thread.sleep
     *
     * @param className
     * @param loc
     */
    public static void sleep(String className, Integer loc) {
        String tName = Thread.currentThread().getName();

        // when config is initializing,
        // DO NOT delay
        if (sStatus == STATUS.INITIALIZING) {
            DelayConfigUtil.insertDelayPoint(sAppName, tName, className, loc, 0);
            return;
        }

        Integer delay = DelayConfigUtil.getDelayTime(sAppName, tName, className, loc);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}