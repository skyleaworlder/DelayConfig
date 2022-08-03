package android.util;

import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class DelayMapTest {
    @Test public void updateDelayTimeTest() {
        try {
            DelayConfigHelper.setAppName("com.example.application0");
            DelayConfigHelper.readConfig();
            DelayMap.updateDelayTime(
                    "com.example.application0", "Thread-1",
                    "android.app.Activity", 250, 450);
            DelayConfigHelper.syncConfig();
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new RuntimeException(e);
        }
    }
}
