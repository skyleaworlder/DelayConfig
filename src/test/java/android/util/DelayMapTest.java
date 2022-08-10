package android.util;

import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class DelayMapTest {

    private static final String configPath = "./system/config";

    @Test public void updateDelayTimeTest() {
        try {
            DelayConfigHelper.setAppName("com.example.application0");
            DelayConfigHelper.readConfig(configPath);
            DelayMap.updateDelayTime(
                    "com.example.application0", "Thread-1",
                    "android.app.Activity", 250, 450);
            DelayConfigHelper.writeConfig(configPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test public void updateAllDelayTimeTest() {
        try {
            DelayConfigHelper.setAppName("com.example.application0");
            DelayConfigHelper.readConfig(configPath);
            DelayMap.updateAllDelayTime();
            DelayConfigHelper.writeConfig(configPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
