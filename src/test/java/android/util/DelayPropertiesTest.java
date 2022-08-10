package android.util;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class DelayPropertiesTest {
    @Test public void loadPropsTest() {
        DelayConfigHelper.setAppName("hahahah");
        DelayConfigHelper.readConfig("./system/props/config.properties");
        Integer res0 = DelayProperties.getDelayTime("hahahah", "Espresso Key Event #0");
        assert res0 == 0;
    }

    @Test public void sleepTest() throws IOException {
        DelayConfigHelper.setAppName("com.example.application0");
        DelayConfigHelper.readConfig("./system/config/config.properties");
        DelayConfigHelper.sleep();
        DelayConfigHelper.writeConfig("./system/config/config.properties");
    }
}
