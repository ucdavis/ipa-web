package edu.ucdavis.dss.jobs;

import edu.ucdavis.dss.ipa.Application;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * Entry point for the scheduled ECS jobs. Loads the app's beans without Application, so there's no
 * web server, @EnableScheduling or Flyway (the web app owns migrations). Kept outside
 * edu.ucdavis.dss.ipa so the app's scan skips it.
 *
 *   java -cp ipa-api-0.1.0.jar -Dloader.main=edu.ucdavis.dss.jobs.JobApplication \
 *     org.springframework.boot.loader.launch.PropertiesLauncher --runBudgetReconciliationReportTask
 */
@SpringBootConfiguration
@EnableAutoConfiguration(exclude = FlywayAutoConfiguration.class)
// JPA entity and repository scanning defaults to this package; point it at the app's
@AutoConfigurationPackage(basePackages = "edu.ucdavis.dss.ipa")
@ComponentScan(basePackages = "edu.ucdavis.dss.ipa",
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = Application.class))
public class JobApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(JobApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        System.exit(SpringApplication.exit(app.run(args)));
    }
}
