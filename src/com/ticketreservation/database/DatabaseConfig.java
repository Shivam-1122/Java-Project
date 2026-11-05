package com.ticketreservation.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Configuration holder for JDBC Database connections (MySQL & SQLite fallback).
 */
public class DatabaseConfig {
    public enum DatabaseType {
        MYSQL,
        SQLITE
    }

    private static final String CONFIG_FILE = "db.properties";

    private DatabaseType dbType = DatabaseType.MYSQL; // Set to MySQL as requested
    private String mysqlHost = "localhost";
    private int mysqlPort = 3306;
    private String mysqlDatabase = "ticket_reservation_db";
    private String mysqlUser = "root";
    private String mysqlPassword = "YOUR_PASSWORD_HERE";
    private String sqlitePath = "ticket_reservation.db";

    public DatabaseConfig() {
        loadProperties();
    }

    public void loadProperties() {
        File file = new File(CONFIG_FILE);
        if (!file.exists()) {
            saveProperties(); // write default template
            return;
        }

        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(file)) {
            props.load(in);
            String type = props.getProperty("db.type", "SQLITE").trim().toUpperCase();
            if ("MYSQL".equalsIgnoreCase(type)) {
                this.dbType = DatabaseType.MYSQL;
            } else {
                this.dbType = DatabaseType.SQLITE;
            }
            this.mysqlHost = props.getProperty("mysql.host", "localhost").trim();
            this.mysqlPort = Integer.parseInt(props.getProperty("mysql.port", "3306").trim());
            this.mysqlDatabase = props.getProperty("mysql.database", "ticket_reservation_db").trim();
            this.mysqlUser = props.getProperty("mysql.user", "root").trim();
            this.mysqlPassword = props.getProperty("mysql.password", "");
            this.sqlitePath = props.getProperty("sqlite.path", "ticket_reservation.db").trim();
        } catch (Exception e) {
            System.err.println("Error reading db.properties, using defaults: " + e.getMessage());
        }
    }

    public void saveProperties() {
        Properties props = new Properties();
        props.setProperty("db.type", dbType.name());
        props.setProperty("mysql.host", mysqlHost);
        props.setProperty("mysql.port", String.valueOf(mysqlPort));
        props.setProperty("mysql.database", mysqlDatabase);
        props.setProperty("mysql.user", mysqlUser);
        props.setProperty("mysql.password", mysqlPassword);
        props.setProperty("sqlite.path", sqlitePath);

        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            props.store(out, "Bus & Train Ticket Reservation System Database Configuration\n" +
                    "Set db.type to MYSQL or SQLITE\n" +
                    "Configure your MySQL Workbench credentials below:");
        } catch (IOException e) {
            System.err.println("Error saving db.properties: " + e.getMessage());
        }
    }

    public DatabaseType getDbType() {
        return dbType;
    }

    public void setDbType(DatabaseType dbType) {
        this.dbType = dbType;
    }

    public String getMysqlHost() {
        return mysqlHost;
    }

    public void setMysqlHost(String mysqlHost) {
        this.mysqlHost = mysqlHost;
    }

    public int getMysqlPort() {
        return mysqlPort;
    }

    public void setMysqlPort(int mysqlPort) {
        this.mysqlPort = mysqlPort;
    }

    public String getMysqlDatabase() {
        return mysqlDatabase;
    }

    public void setMysqlDatabase(String mysqlDatabase) {
        this.mysqlDatabase = mysqlDatabase;
    }

    public String getMysqlUser() {
        return mysqlUser;
    }

    public void setMysqlUser(String mysqlUser) {
        this.mysqlUser = mysqlUser;
    }

    public String getMysqlPassword() {
        return mysqlPassword;
    }

    public void setMysqlPassword(String mysqlPassword) {
        this.mysqlPassword = mysqlPassword;
    }

    public String getSqlitePath() {
        return sqlitePath;
    }

    public void setSqlitePath(String sqlitePath) {
        this.sqlitePath = sqlitePath;
    }

    public String getMysqlUrl(boolean includeDatabase) {
        if (includeDatabase) {
            return "jdbc:mysql://" + mysqlHost + ":" + mysqlPort + "/" + mysqlDatabase +
                    "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        } else {
            return "jdbc:mysql://" + mysqlHost + ":" + mysqlPort +
                    "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        }
    }

    public String getSqliteUrl() {
        return "jdbc:sqlite:" + sqlitePath;
    }
}
