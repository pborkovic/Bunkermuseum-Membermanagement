package com.bunkermuseum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuration class for Hibernate ORM 7.1 integration with Spring Boot.
 *
 * <p>This configuration class provides custom beans and settings for Hibernate ORM 7.1,
 * enabling advanced features such as second-level caching, query optimization, and
 * performance monitoring. It works in conjunction with the application.properties
 * configuration to provide a complete Hibernate setup.</p>
 *
 * <p>Key features configured:</p>
 * <ul>
 *   <li>HikariCP connection pooling for optimal database connection management</li>
 *   <li>Second-level caching with JCache for improved performance</li>
 *   <li>Query caching for frequently executed queries</li>
 *   <li>SQL logging and formatting for development and debugging</li>
 *   <li>Automatic DDL generation in development mode</li>
 *   <li>Performance statistics and slow query monitoring</li>
 * </ul>
 *
 * @see org.hibernate.cfg.Configuration
 * @see org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
 *
 * @version 1.0
 * @since 1.0
 *
 * @author Philipp Borkovic
 */
@Configuration
@EnableTransactionManagement
public class HibernateConfig {

    /**
     * Creates and configures a HibernateJpaVendorAdapter bean with optimized settings
     * for the Bunker Museum application.
     *
     * <p>This adapter bridges Spring's JPA abstraction with Hibernate ORM 7.1,
     * providing vendor-specific configuration options. The adapter is configured
     * to show SQL statements for debugging purposes and enable DDL generation
     * for automatic schema management in development environments.</p>
     *
     * <p><strong>Configuration details:</strong></p>
     * <ul>
     *   <li><code>showSql(true)</code> - Enables SQL statement logging to console</li>
     *   <li><code>generateDdl(true)</code> - Enables automatic DDL generation</li>
     * </ul>
     *
     * <p><strong>Note:</strong> In production environments, consider disabling
     * SQL logging and DDL generation for security and performance reasons.
     * Use proper database migration tools like Flyway for production deployments.</p>
     *
     * @see HibernateJpaVendorAdapter#setShowSql(boolean)
     * @see HibernateJpaVendorAdapter#setGenerateDdl(boolean)
     *
     * @return a configured {@link HibernateJpaVendorAdapter} instance optimized
     *         for development and debugging with Hibernate ORM 7.1
     *
     * @author Philipp Borkovic
     */
    @Bean
    public HibernateJpaVendorAdapter jpaVendorAdapter() {
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();

        adapter.setShowSql(true);
        adapter.setGenerateDdl(true);

        return adapter;
    }
}