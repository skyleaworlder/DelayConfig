package android.util;

import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class DelayConfigHelperTest {

    private static final String configPath = "./system/config";

    @Test public void syncConfigTest() {
        try {
            DelayConfigHelper.setAppName("com.example.application0");
            DelayConfigHelper.readConfig(configPath);
            DelayConfigHelper.writeConfig(configPath);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    @Test public void emptyConfigTest() {
        try {
            DelayConfigHelper.setAppName("com.example.emptyconfig");
            DelayConfigHelper.readConfig(configPath);
            DelayConfigUtil.insertDelayPoint(
                    "com.example.emptyconfig", "Thread1", "android.app.ActivityThread", 1000, 100);
            DelayConfigHelper.writeConfig(configPath);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    @Test public void nonExistedConfigTest() {
        try {
            DelayConfigHelper.setAppName("com.example.nonexisted");
            DelayConfigHelper.readConfig(configPath);
            DelayConfigUtil.insertDelayPoint(
                    "com.example.nonexisted", "Thread1", "android.app.ActivityThread", 1000, 100);
            DelayConfigHelper.writeConfig(configPath);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    @Test public void sleepTest() {
        try {
            DelayConfigHelper.setAppName("com.example.nonexisted");
            DelayConfigHelper.readConfig(configPath);
            DelayConfigHelper.sleep();
            DelayConfigHelper.writeConfig(configPath);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new RuntimeException(e);
        }
    }
}
