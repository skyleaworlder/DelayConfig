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

    /**
     * INITIALIZING: all config
     */
    public enum STATUS {
        INITIALIZING,
        DELAY_ALL_ZERO,
        CONFIG_FILE_ERROR,
    }

    private static final String sSystemConfigPath = "./system/config/";
    public static STATUS sStatus = STATUS.INITIALIZING;
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
    public static String getConfigFilePath() {
        if (sAppName == null) {
            return sSystemConfigPath + "config.xml";
        }
        return sSystemConfigPath + sAppName + ".config.xml";
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
        try {
            InputStream ins = new FileInputStream(file);
            InputSource source = new InputSource(ins);
            xmlReader.parse(source);
        } catch (FileNotFoundException e) {
            // config not found means that
            // this run aims to push some k-v pair to HashMap
            // we don't need to delay when executing sleep
            sStatus = STATUS.INITIALIZING;
        } catch (IOException | SAXException e) {
            sStatus = STATUS.CONFIG_FILE_ERROR;
        }
        return;
    }

    public static void syncConfig()
            throws IOException {
        FileOutputStream fos = new FileOutputStream(getConfigFilePath());
        fos.write(DelayMap.serialize().getBytes());
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
            insertDelayPoint(tName, className, loc, 0);
            return;
        }

        Integer delay = getDelayTime(tName, className, loc);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static Integer getDelayTime(
            String tName, String className, Integer loc) {
        return DelayMap.getDelayTime(sAppName, tName, className, loc);
    }

    public static boolean insertDelayPoint(
            String tName, String className, Integer loc, Integer delay) {
        DelayMap.DelayPoint dp = DelayMap.DelayPoint.newInstance(className, loc, delay);
        return DelayMap.insertDelayPoint(sAppName, tName, dp);
    }

}