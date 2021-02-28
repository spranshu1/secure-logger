package com.github.spranshu1.api.log.pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * This class create log pattern (meta data) for CF environment
 */
public final class LogPattern {

    /**
     * The Constant logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LogPattern.class);

    /**
     * Variable to identify the environment
     */
    public static String environment = "";

    /**
     * CF variable that contain details about application
     */
    public static final String VCAP_APPLICATION = "VCAP_APPLICATION";

    /**
     * CF variable for instance id
     */
    public static final String CF_INSTANCE_GUID = "CF_INSTANCE_GUID";

    /**
     * Prevent instantiation
     */
    private LogPattern() {

    }

    static {
        initialize();
    }

    /**
     * method to create meta data for CF environment
     */
    public static void initialize() {

        final String vcapAppEnv = System.getenv(VCAP_APPLICATION);
        if (vcapAppEnv != null && !vcapAppEnv.equals("")) {
            environment = "cloud";
            final ObjectMapper mapper = new ObjectMapper();
            JsonNode actualObj;
            try {
                actualObj = mapper.readTree(vcapAppEnv);
                final JsonNode appName = actualObj.get("application_name");
                final JsonNode spaceName = actualObj.get("space_name");
                final JsonNode appVersion = actualObj.get("application_version");
                System.setProperty("mdc_cf_app_name", appName.textValue());
                System.setProperty("mdc_cf_space_name", spaceName.textValue());
                System.setProperty("mdc_cf_app_version", appVersion.textValue());
                System.setProperty("mdc_cf_instance_id", System.getenv(CF_INSTANCE_GUID));
            } catch (IOException e) {
                LOGGER.error("Exception - ", e);
            }
        }
    }
}
