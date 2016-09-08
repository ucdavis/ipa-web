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

	// Should be in the form: http://website:8080 (no trailing slash, include protocol)
	public static void setURL(String url) {
		SettingsConfiguration.url = url;
	}
	public static String getURL() {
		return url;
	}
}
