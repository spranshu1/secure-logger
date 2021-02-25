package com.github.spranshu1.api.log.mask.test;

import org.apache.logging.log4j.core.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.test.context.TestPropertySource;
import com.github.spranshu1.api.log.mask.MaskLog;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@TestPropertySource(locations="classpath:application.properties")
/**
 * Test class for log masking
 *
 */
@RunWith(MockitoJUnitRunner.class)
public final class MaskLogTest {

	/** Logger object. */
	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MaskLogTest.class);
	/** Marker to skip log masking. */
	private static final Marker LOGMASKING_MARKER = MaskLog.SKIP_MARKER;	
	/** Custom Marker. */
	private static final Marker CUSTOM_MARKER = MarkerFactory.getMarker("MYMARKER");	
	
	/**  Mocking the appender*/
	@Mock
	private Appender mockAppender;  

	/**  Variable to create logging event */
	@Captor
	private ArgumentCaptor<LogEvent> capLoggingEvent;

	/** Logger object used for mocking */
	public static final Logger LOG=(Logger)LogManager.getLogger(MaskLogTest.class);;
	
	/** Creating environment variable using rule*/
	@Rule
	public final EnvironmentVariables envVar = new EnvironmentVariables();
	
	/** Log message to be printed in log*/
	private final String logMessage="CustomerModel [customerName=" + "Jake" + ", address=" + "Chicago"+", accountNumber="
			+ "Az1234562"+", customerId=" + "J@@@1P"+", password=" + "pp!@1335"
			+ "]";

	/** */
	@Before
	public void setUp() throws Exception {
		envVar.set("VCAP_APPLICATION", "{\n" + 
				"  \"space_name\": \"test_space\",\n" +
				"  \"application_version\": \"0.0.1\",\n" +
				"  \"application_name\": \"commons-logger\"}");
		envVar.set("CF_INSTANCE_GUID", "11224dff4rthbh");
		when(mockAppender.getName()).thenReturn("MockAppender");
		when(mockAppender.isStarted()).thenReturn(true);
		when(mockAppender.isStopped()).thenReturn(false);
		LOG.addAppender(mockAppender);
		LOG.info("No masking");
	}

	/** Cleanup of appender */
	@After
	public void tearDown() {
		/** the appender we added will sit in the singleton logger forever
	         slowing future things down - so remove it */
		LOG.removeAppender(mockAppender);
	}

	/** No log masking test */
	@Test
	public void noLogMasking(){

		verifyLogMessages("No masking");
	}

	/** Test for masking log */
	@Test
	public void maskLog(){

		LOGGER.info(logMessage);
		LOGGER.info(LOGMASKING_MARKER,logMessage);
		LOGGER.info(CUSTOM_MARKER,logMessage);
		assertNotEquals(logMessage,null);
	}

	/** Test thread context with MDC */
	@Test
	public void systemPropertyWithOutClearingThreadContext(){
		
		verifySystemPropertyInformation();
	}

	/** Test thread context with MDC */
	@Test
	public void systemPropertyClearingThreadContext(){

		ThreadContext.clearAll();
		verifySystemPropertyInformation();	
	}

	/** Method for verifying log message*/
	private void verifyLogMessages(final String messages) {

		verify(mockAppender).append(capLoggingEvent.capture());
		for(final LogEvent loggingEvent:capLoggingEvent.getAllValues()) {
			assertEquals("Verifying log message",loggingEvent.getMessage().getFormattedMessage(),messages);
		}
	}

	/** Method for verifying information in MDC*/
	private void verifySystemPropertyInformation() {

			assertEquals("Verifying MDC information",System.getProperty("mdc_cf_app_version"),"0.0.1");
	}
}
