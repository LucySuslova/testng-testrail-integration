package io.github.lucysuslova.testrail.api;

import com.codepine.api.testrail.TestRail;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TestRailClient {

    static TestRail testRail;
    static Properties properties;

    public static TestRail getInstance() {
        if (null == testRail) {
            return TestRail
                    .builder(getProperties("testrail.baseurl"),
                            getProperties("testrail.username"),
                            getProperties("testrail.password"))
                    .build();
        } else {
            return testRail;
        }
    }

    private static String getProperties(String name) {
        if (null == properties) {
            try (InputStream inputStream = TestRailClient.class.getClassLoader().getResourceAsStream("testrail.properties")) {
                properties = new Properties();
                properties.load(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return properties.getProperty(name);
    }

}
