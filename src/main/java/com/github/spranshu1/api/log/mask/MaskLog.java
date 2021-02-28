package com.github.spranshu1.api.log.mask;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * converting/overriding the default key m.
 * Now message will be masked before writing to the actual log
 * please refer PatternLayout in log4j2.xml to see how %mask is used
 */

@Plugin(name = "MaskLog", category = "Converter")
@ConverterKeys({"mask"})
public class MaskLog extends LogEventPatternConverter {

    /**
     * The Constant logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MaskLog.class);

    /**
     * marker for skipping the log masking.
     */
    public static final Marker SKIP_MARKER = MarkerFactory.getMarker("SKIP_MASKING_MARKER");

    /**
     * Property variable.
     */
    private static Properties prop;

    /**
     * HashMap for storing masking keys and corresponding search and replace pattern
     */
    private static final Map<String, String> hm = new HashMap<String, String>();

    /**
     * ArrayList for storing search pattern
     */
    private static final List<String> searchList = new ArrayList<String>();

    /**
     * ArrayList for storing replacement pattern
     */
    private static final List<String> replaceList = new ArrayList<String>();

    /**
     * Variable for storing length of keys
     */
    private static int length;

    /**
     * Variable for storing attribute name of keys
     */
    private static final String LOGMASKINGKEYS = "LogMaskingKeys";

    /**
     * Variable for storing commons property name
     */
    private static final String COMMON_PROP_FILE = "common-log-masking.properties";

    /**
     * Variable for storing application property name
     */
    private static final String APP_PROP_FILE = "application.properties";


    static {
        createMaskingKeysMap();
    }

    /**
     * Default constructor to call super class constructor.
     */
    public MaskLog() {
        super("m", "m");
    }

    /**
     * Method to get the new instance.
     */
    public static MaskLog newInstance() {
        return new MaskLog();
    }

    /**
     * this method is responsible for actually masking the log.
     */
    @Override
    public void format(final LogEvent logEvent, final StringBuilder outputMsg) {

        String message = logEvent.getMessage().getFormattedMessage();
        // mask log only if no marker is set in the log statement or marker other then SKIP_MARKER
        if (logEvent.getMarker() == null || !logEvent.getMarker().getName().equalsIgnoreCase("SKIP_MASKING_MARKER")) {
            for (int i = 0; i < length; i++) {
                message = message.replaceAll(searchList.get(i), replaceList.get(i));
            }
        }
        outputMsg.append(message);
    }

    private static void createMaskingKeysMap() {

        loadPropertyFile(COMMON_PROP_FILE);
        // get the keys from system property of consumer else load the properties file
        if (System.getProperty(LOGMASKINGKEYS) == null || System.getProperty(LOGMASKINGKEYS).equalsIgnoreCase("")) {
            loadPropertyFile(APP_PROP_FILE);
        } else {
            final String appRegexKeys[] = System.getProperty(LOGMASKINGKEYS).split(",");
            for (final String regexKey : appRegexKeys) {
                hm.put(regexKey, prop.getProperty(regexKey + ".search") + regexKey + prop.getProperty(regexKey + ".replace"));
            }
        }
        // forEach(action) method to iterate map
        hm.forEach((key, value) -> {
                    final String obj[] = value.split(key);
                    searchList.add(obj[0]);
                    replaceList.add(obj[1]);
                }
        );
        length = searchList.size();
    }

    private static void loadPropertyFile(final String fileName) {

        final Properties prop = new Properties();
        InputStream input;
        try {
            Thread.currentThread().getContextClassLoader();
            input = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
            if (input != null) {
                prop.load(input);
            }
            if (prop.getProperty(LOGMASKINGKEYS) != null) {
                final String appRegexKeys[] = prop.getProperty(LOGMASKINGKEYS).split(",");
                for (int i = 0; i < appRegexKeys.length; i++) {
                    hm.put(appRegexKeys[i], prop.getProperty(appRegexKeys[i] + ".search") + appRegexKeys[i] + prop.getProperty(appRegexKeys[i] + ".replace"));
                }
            }
        } catch (IOException ex) {
            LOGGER.error("Exception - {}", ex);
        }
    }
}
