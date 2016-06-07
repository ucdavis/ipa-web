package edu.ucdavis.dss.ipa.config;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;

public class MigrationProcessor {
	public void migrate(DataSource dataSource, boolean resetDatabaseOnStartup) {
		Flyway flyway = new Flyway();

		flyway.setDataSource(dataSource);
		flyway.setLocations("classpath:database/migrations");
		
		if(resetDatabaseOnStartup) {
			flyway.clean();
		}

		flyway.migrate();
	}
}
