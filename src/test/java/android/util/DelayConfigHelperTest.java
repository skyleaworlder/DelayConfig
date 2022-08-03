package android.util;

import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class DelayConfigHelperTest {
    @Test public void syncTest() {
        try {
            DelayConfigHelper.setAppName("com.example.application0");
            DelayConfigHelper.readConfig();
            DelayConfigHelper.writeConfig();
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    @Test public void emptyConfig() {
        try {
            DelayConfigHelper.setAppName("com.example.emptyconfig");
            DelayConfigHelper.readConfig();
            DelayConfigHelper.insertDelayPoint(
                    "Thread1", "android.app.ActivityThread", 1000, 100);
            DelayConfigHelper.writeConfig();
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    @Test public void nonExistedConfig() {
        try {
            DelayConfigHelper.setAppName("com.example.nonexisted");
            DelayConfigHelper.readConfig();
            DelayConfigHelper.insertDelayPoint(
                    "Thread1", "android.app.ActivityThread", 1000, 100);
            DelayConfigHelper.writeConfig();
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new RuntimeException(e);
        }
    }
}
