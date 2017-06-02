package org.wso2telco.analytics.hub.report.engine.internel.util;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by isuru on 6/2/17.
 */
public class PropertyReader {

    private static final Logger log = Logger.getLogger(PropertyReader.class);
    private static Properties properties = new Properties();

    public static void loadProps() {

        InputStream inputStream = null;
        try {

            String filename = "report-engine.properties";

            inputStream = PropertyReader.class.getClassLoader().getResourceAsStream(filename);

            properties.load(inputStream);

        } catch (IOException e) {
            log.error("Error occurred while reading properties", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("Error occurred while closing input stream", e);
                }
            }
        }
    }

    public static String getAddress(String username) {
        return getProperty(username + ".address");
    }

    public static String getProperty(String propertyKey) {
        return properties.getProperty(propertyKey);
    }

    static {
        loadProps();
    }
}
