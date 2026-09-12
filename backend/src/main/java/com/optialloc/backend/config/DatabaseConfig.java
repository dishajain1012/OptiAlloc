package com.optialloc.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    @Bean
    @Primary
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");

        String rawUrl = System.getenv("OPTIALLOC_DB_URL");
        if (rawUrl == null || rawUrl.isBlank()) {
            rawUrl = System.getenv("DATABASE_URL");
        }
        if (rawUrl == null || rawUrl.isBlank()) {
            rawUrl = System.getenv("SPRING_DATASOURCE_URL");
        }

        if (rawUrl != null && !rawUrl.isBlank()) {
            if (rawUrl.startsWith("postgres://") || rawUrl.startsWith("postgresql://")) {
                try {
                    String httpUrl = rawUrl.replaceFirst("postgres(ql)?://", "http://");
                    URI uri = new URI(httpUrl);

                    String host = uri.getHost();
                    int port = uri.getPort() == -1 ? 5432 : uri.getPort();
                    String path = uri.getPath();

                    String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + path;
                    dataSource.setUrl(jdbcUrl);
                    logger.info("Transformed Render postgres:// URL into JDBC URL: jdbc:postgresql://{}:{}{}", host, port, path);

                    if (uri.getUserInfo() != null) {
                        String[] userInfo = uri.getUserInfo().split(":");
                        dataSource.setUsername(userInfo[0]);
                        if (userInfo.length > 1) {
                            dataSource.setPassword(userInfo[1]);
                        }
                    }
                    return dataSource;
                } catch (Exception e) {
                    logger.warn("Could not parse postgres:// URL, falling back to raw property", e);
                }
            } else if (rawUrl.startsWith("jdbc:postgresql://")) {
                dataSource.setUrl(rawUrl);

                String user = System.getenv("OPTIALLOC_DB_USERNAME");
                if (user == null || user.isBlank()) user = System.getenv("DATABASE_USERNAME");
                if (user != null && !user.isBlank()) dataSource.setUsername(user);

                String pass = System.getenv("OPTIALLOC_DB_PASSWORD");
                if (pass == null || pass.isBlank()) pass = System.getenv("DATABASE_PASSWORD");
                if (pass != null && !pass.isBlank()) dataSource.setPassword(pass);

                return dataSource;
            }
        }

        // Fallback for local development
        String envUser = System.getenv("OPTIALLOC_DB_USERNAME");
        String envPass = System.getenv("OPTIALLOC_DB_PASSWORD");

        dataSource.setUrl("jdbc:postgresql://localhost:5432/optialloc");
        dataSource.setUsername(envUser != null && !envUser.isBlank() ? envUser : "postgres");
        dataSource.setPassword(envPass != null && !envPass.isBlank() ? envPass : "postgres");
        return dataSource;
    }
}
