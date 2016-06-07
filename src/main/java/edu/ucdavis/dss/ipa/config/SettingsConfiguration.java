package edu.ucdavis.dss.ipa.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Allows enabling/disabling certain program features.
 *
 */
public class SettingsConfiguration {
	/* Declarations with their defaults */
	private static RunningMode runningMode = RunningMode.development;
	private static boolean resetDatabaseOnStartup = false;
	private static String emailProtocol, emailAuth, emailDebug, emailHost;
	private static String url;
	private static Integer emailPort;

	private static final Logger log = LogManager.getLogger();

	public static void loadAndApplySettings(ConfigurableEnvironment environment, ServletContext servletContext) {
		/* Load running mode (development, testing, production) */
		String runningModeEnvVar = System.getProperty("edu.ucdavis.dss.ipa.RunningMode");
		if(runningModeEnvVar != null) {
			runningMode = RunningMode.valueOf(runningModeEnvVar);
		}

		log.info("IPA is running in " + runningMode + " mode.");

		switch(runningMode) {
		case development:
			environment.setActiveProfiles("development");
			break;
		case production:
			environment.setActiveProfiles("production");
			break;
		case testing:
			environment.setActiveProfiles("testing");
			break;
		case staging:
			environment.setActiveProfiles("staging");
			break;
		}

		if(runningMode != RunningMode.testing) {
			/* Determine site URL (used by CAS redirect, e-mail composer, and some other areas - see
			 * usage of system property IPA_URL as well as any SettingsConfiguration.getURL() calls.) */
			String contextPath = servletContext.getContextPath();
			String virtualServerName = servletContext.getVirtualServerName();
			
			if(runningMode == RunningMode.production) {
				url = "https://" + virtualServerName + contextPath;
			} else if(runningMode == RunningMode.development) {
				url = "http://" + virtualServerName + ":8080" + contextPath;
			} else if(runningMode == RunningMode.staging) {
				url = "http://" + virtualServerName + ":8080" + contextPath;
			}
			
			System.setProperty("IPA_URL", url);
			
			/* Load configuration from ~/.ipa/settings.properties */
			String filename = System.getProperty("user.home") + File.separator + ".ipa" + File.separator + "settings.properties";
			File propsFile = new File(filename);
			Properties prop = new Properties();

			InputStream is;

			try {
				is = new FileInputStream(propsFile);

				prop.load(is);
				is.close();

				if(prop.getProperty("resetDatabaseOnStartup") != null) {
					resetDatabaseOnStartup = Boolean.parseBoolean(prop.getProperty("resetDatabaseOnStartup"));
				}
				if (prop.getProperty("EMAIL_PROTOCOL") != null) {
					emailProtocol = prop.getProperty("EMAIL_PROTOCOL");
				}
				if (prop.getProperty("EMAIL_AUTH") != null) {
					emailAuth = prop.getProperty("EMAIL_AUTH");
				}
				if (prop.getProperty("EMAIL_DEBUG") != null) {
					emailDebug = prop.getProperty("EMAIL_DEBUG");
				}
				if (prop.getProperty("EMAIL_HOST") != null) {
					emailHost = prop.getProperty("EMAIL_HOST");
				}
				if (prop.getProperty("EMAIL_PORT") != null) {
					emailPort = Integer.parseInt(prop.getProperty("EMAIL_PORT"));
				}

				log.info("Settings file '" + filename + "' found.");
			} catch (FileNotFoundException e) {
				log.warn("Could not find " + filename + ".");
			} catch (IOException e) {
				log.error("An IOException occurred while loading " + filename);
				log.error(e.getStackTrace());
			}

			log.info("Settings are: IPA_URL (" + prop.getProperty("IPA_URL") + ", set as environment variable)");
		}
	}

	public static RunningMode getRunningMode() {
		return runningMode;
	}

	public static boolean runningModeIsDevelopment() {
		return runningMode == RunningMode.development;
	}

	public static boolean runningModeIsStaging() {
		return runningMode == RunningMode.staging;
	}

	public static boolean runningModeIsTesting() {
		return runningMode == RunningMode.testing;
	}

	public static boolean runningModeIsProduction() {
		return runningMode == RunningMode.production;
	}

	public static boolean getResetDatabaseOnStartup() {
		return resetDatabaseOnStartup;
	}

	public static String getEmailProtocol() {
		return emailProtocol;
	}

	public static String getEmailAuth() {
		return emailAuth;
	}

	public static String getEmailDebug() {
		return emailDebug;
	}

	public static Integer getEmailPort() {
		return emailPort;
	}

	public static String getEmailHost() {
		return emailHost;
	}

	public static String getURL() {
		return url;
	}
}
