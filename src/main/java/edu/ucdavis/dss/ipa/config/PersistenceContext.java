package edu.ucdavis.dss.ipa.config;

import java.util.Hashtable;
import java.util.Map;

import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.sql.DataSource;

import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.Ordered;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement(
		mode = AdviceMode.PROXY, proxyTargetClass = false,
		order = Ordered.LOWEST_PRECEDENCE
		)
@EnableJpaRepositories(
		basePackages = "edu.ucdavis.dss.ipa.repositories",
		entityManagerFactoryRef = "entityManagerFactoryBean",
		transactionManagerRef = "jpaTransactionManager"
		)
public class PersistenceContext {
	@Bean
	public DataSource jpaDataSource()
	{
		//org.apache.tomcat.jdbc.pool.DataSource ds = new org.apache.tomcat.jdbc.pool.DataSource();
		
//		ds.setDriverClassName("com.mysql.jdbc.Driver");
//		ds.setName("jdbc/IPA");
//		ds.setUsername("root");
//		ds.setPassword("");
//		ds.setUrl("jdbc:mysql://localhost/IPA");
//		ds.setMaxActive(8);
//		ds.setMaxWait(15000); // milliseconds
//		ds.setRemoveAbandoned(true);

		//return ds;

//		DriverManagerDataSource ds = new DriverManagerDataSource();
//		
//		ds.setDriverClassName("com.mysql.jdbc.Driver");
//		ds.setUrl("jdbc:mysql://localhost/IPA");
//		ds.setUsername("root");
//		ds.setPassword("");
		
		JndiDataSourceLookup lookup = new JndiDataSourceLookup();
		return lookup.getDataSource("jdbc/IPA");
		
	}
	
	@Bean
	@DependsOn("migrationProcessorBean")
	public PlatformTransactionManager jpaTransactionManager()
	{
		return new JpaTransactionManager(
				this.entityManagerFactoryBean().getObject()
				);
	}
	
	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean()
	{
		Map<String, Object> properties = new Hashtable<>();
		properties.put("javax.persistence.schema-generation.database.action",
				"none");

		HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
		adapter.setDatabasePlatform("org.hibernate.dialect.MySQL5InnoDBDialect");

		LocalContainerEntityManagerFactoryBean factory =
				new LocalContainerEntityManagerFactoryBean();
		factory.setJpaVendorAdapter(adapter);
		factory.setDataSource(this.jpaDataSource());
		factory.setPackagesToScan("edu.ucdavis.dss.ipa.entities");
		factory.setSharedCacheMode(SharedCacheMode.ENABLE_SELECTIVE);
		factory.setValidationMode(ValidationMode.NONE);
		factory.setJpaPropertyMap(properties);
		
		return factory;
	}
	
	@Bean
	@DependsOn("jpaDataSource")
	public MigrationProcessor migrationProcessorBean() {
		MigrationProcessor migrationProcessor = new MigrationProcessor();
		
		migrationProcessor.migrate(this.jpaDataSource(), SettingsConfiguration.getResetDatabaseOnStartup());
		
		return migrationProcessor;
	}
}
