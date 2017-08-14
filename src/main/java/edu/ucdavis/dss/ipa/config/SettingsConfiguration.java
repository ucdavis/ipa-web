package edu.ucdavis.dss.ipa.config;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows enabling/disabling certain program features.
 *
 */
public class SettingsConfiguration {
	/* Declarations with their defaults */
	private static RunningMode runningMode = RunningMode.development;

	private static String ipaApiUrl, ipaFrontendUrl;
	private static String emailProtocol, emailAuth, emailDebug, emailHost, emailFrom;
	private static Integer emailPort, jwtTimeout;
	private static Boolean errorsFound = null;
	private static String jwtSigningKey, downloadSecretKey;
	private static String dwUrl, dwToken, dwPort;

	private static final Logger log = LoggerFactory.getLogger("SettingsConfiguration");

	/**
	 * Ensures environment variables required by application.properties are set.
	 *
	 * Returns true if all required environment variables are found.
	 */
	public static void loadSettings() {
		errorsFound = false;

		findOrWarnSetting("ipa.logging.level");
		findOrWarnSetting("ipa.datasource.url");
		findOrWarnSetting("ipa.datasource.username");
		findOrWarnSetting("ipa.datasource.password");

		/* Load running mode (development, testing, production) */
		String sRunningMode = findOrWarnSetting("ipa.spring.profile");
		if(sRunningMode != null) { runningMode = RunningMode.valueOf(sRunningMode); }
		jwtSigningKey = findOrWarnSetting("ipa.jwt.signingkey");

		String sJwtTimeout = findOrWarnSetting("ipa.jwt.timeout");
		if(sJwtTimeout != null) { jwtTimeout = Integer.parseInt(sJwtTimeout); }

		dwUrl = findOrWarnSetting("dw.url");
		dwToken = findOrWarnSetting("dw.token");
		dwPort = findOrWarnSetting("dw.port");
		ipaApiUrl = findOrWarnSetting("ipa.url.api");
		ipaFrontendUrl = findOrWarnSetting("ipa.url.frontend");
		emailProtocol = findOrWarnSetting("ipa.email.protocol");
		emailAuth = findOrWarnSetting("ipa.email.auth");
		emailDebug = findOrWarnSetting("ipa.email.debug");
		emailHost = findOrWarnSetting("ipa.email.host");
		emailFrom = findOrWarnSetting("ipa.email.from");
		String sEmailPort = findOrWarnSetting("ipa.email.port");
		if(sEmailPort != null) { emailPort = Integer.parseInt(sEmailPort); }

		// Use ipa.spring.profile to set the Spring Profile
		System.setProperty("spring.profiles.active", sRunningMode);

		// Set a random download secret key
		downloadSecretKey = RandomStringUtils.randomAlphanumeric(16).toUpperCase();

		log.info("IPA started with the following settings:");
		log.info("\tRunning Mode: " + runningMode);
		log.info("\tE-Mail Host : " + emailHost);
		log.info("\tE-Mail Port : " + emailPort);
		log.info("\tDW URL      : " + dwUrl);
	}

	public static String findOrWarnSetting(String variableName) {
		String value = System.getProperty(variableName);
		if(value == null) {
			value = System.getenv(variableName);
		}
		if(value == null) {
			System.err.println("Environment variable '" + variableName + "' must be set");
			errorsFound = true;
		}

		return value;
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

	public static String getJwtSigningKey() { return jwtSigningKey; }

	public static String getDownloadSecretKey() {
		return downloadSecretKey;
	}

	// Should be in the form: http://website:8080 (no trailing slash, include protocol)
	public static String getIpaApiURL() {
		return ipaApiUrl;
	}

	// Should be in the form: http://ipa.ucdavis.edu (no trailing slash, include protocol)
	public static String getIpaFrontendURL() {
		return ipaFrontendUrl;
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

	public static String getEmailFrom() {
		return emailFrom;
	}

	public static Integer getEmailPort() {
		return emailPort;
	}

	public static String getEmailHost() {
		return emailHost;
	}

	public static String getDwUrl() {
		return dwUrl;
	}

	public static String getDwToken() {
		return dwToken;
	}

	public static String getDwPort() {
		return dwPort;
	}

	public static Integer getJwtTimeout() { return jwtTimeout; }

	/**
	 * Returns true if errorsFound is true or null.
	 *
	 * errorsFound is null if loadSettings() hasn't been called.
	 *
	 * @return
	 */
	public static boolean isValid() {
		if(errorsFound == null) return false;
		return !errorsFound;
	}
}
