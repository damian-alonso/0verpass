package com.overpass.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class DataSource {
    private static final HikariDataSource ds;

    static {
        Properties props = new Properties();
        try (InputStream is = DataSource.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (is != null) props.load(is);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar application.properties", e);
        }
        String url = props.getProperty("jdbc.url", "jdbc:mysql://localhost:3306/0verpass");
        String user = props.getProperty("jdbc.user", "0verpass");
        String password = props.getProperty("jdbc.password", "0verpass");
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);
        config.setMaximumPoolSize(Integer.parseInt(props.getProperty("jdbc.pool.size", "5")));
        ds = new HikariDataSource(config);
    }

    public static javax.sql.DataSource get() {
        return ds;
    }

    private DataSource() {}
}
