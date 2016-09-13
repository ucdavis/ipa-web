package edu.ucdavis.dss.ipa.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
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

	private static String ipaUrl;
	private static String emailProtocol, emailAuth, emailDebug, emailHost, emailFrom;
	private static Integer emailPort;
	private static Boolean errorsFound = null;
	private static String jwtSigningKey;
	private static String dwUrl, dwToken, dwPort;

	//private static HashMap<String,Object> settings;

	private static final Logger log = LogManager.getLogger();

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
		dwUrl = findOrWarnSetting("dw.url");
		dwToken = findOrWarnSetting("dw.token");
		dwPort = findOrWarnSetting("dw.port");
		ipaUrl = findOrWarnSetting("ipa.url");
		emailProtocol = findOrWarnSetting("ipa.email.protocol");
		emailAuth = findOrWarnSetting("ipa.email.auth");
		emailDebug = findOrWarnSetting("ipa.email.debug");
		emailHost = findOrWarnSetting("ipa.email.host");
		emailFrom = findOrWarnSetting("ipa.email.from");
		String sEmailPort = findOrWarnSetting("ipa.email.port");
		if(sEmailPort != null) { emailPort = Integer.parseInt(sEmailPort); }

		// Use ipa.spring.profile to set the Spring Profile
		System.setProperty("spring.profiles.active", sRunningMode);

		log.info("IPA started with the following settings:");
		log.info("\tRunning Mode: " + runningMode);
		log.info("\tE-Mail Host : " + emailHost);
		log.info("\tE-Mail Port : " + emailPort);
		log.info("\tDW URL      : " + dwUrl);
	}

	private static String findOrWarnSetting(String variableName) {
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

	// Should be in the form: http://website:8080 (no trailing slash, include protocol)
	public static String getIpaURL() {
		return ipaUrl;
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
