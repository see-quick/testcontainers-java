package org.testcontainers.containers;

import org.jetbrains.annotations.NotNull;
import org.testcontainers.containers.wait.LogMessageWaitStrategy;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import static java.lang.String.format;
import static java.time.temporal.ChronoUnit.SECONDS;

/**
 * @author richardnorth
 */
public class PostgreSQLContainer<SELF extends PostgreSQLContainer<SELF>> extends JdbcDatabaseContainer<SELF> {
    public static final String NAME = "postgresql";
    public static final String IMAGE = "postgres";
    public static final String DEFAULT_TAG = "9.6.12";

    public static final Integer POSTGRESQL_PORT = 5432;

    static final String DEFAULT_USER = "test";

    static final String DEFAULT_PASSWORD = "test";

    private String databaseName = "test";
    private String username = "test";
    private String password = "test";

    private static final String FSYNC_OFF_OPTION = "fsync=off";

    private static final String QUERY_PARAM_SEPARATOR = "&";

    public PostgreSQLContainer() {
        this(IMAGE + ":" + DEFAULT_TAG);
    }

    public PostgreSQLContainer(final String dockerImageName) {
        super(dockerImageName);
        this.waitStrategy = new LogMessageWaitStrategy()
                .withRegEx(".*database system is ready to accept connections.*\\s")
                .withTimes(2)
                .withStartupTimeout(Duration.of(60, SECONDS));
        this.setCommand("postgres", "-c", FSYNC_OFF_OPTION);

        addExposedPort(POSTGRESQL_PORT);
    }

    @NotNull
    @Override
    protected Set<Integer> getLivenessCheckPorts() {
        return new HashSet<>(getMappedPort(POSTGRESQL_PORT));
    }

    @Override
    protected void configure() {
        addEnv("POSTGRES_DB", databaseName);
        addEnv("POSTGRES_USER", username);
        addEnv("POSTGRES_PASSWORD", password);
    }

    @Override
    public String getDriverClassName() {
        return "org.postgresql.Driver";
    }

    @Override
    public String getJdbcUrl() {
        // Disable Postgres driver use of java.util.logging to reduce noise at startup time
        return format("jdbc:postgresql://%s:%d/%s?loggerLevel=OFF", getContainerIpAddress(), getMappedPort(POSTGRESQL_PORT), databaseName);
    }

    @Override
    protected String constructUrlForConnection(String queryString) {
        String baseUrl = getJdbcUrl();

        if ("".equals(queryString)) {
            return baseUrl;
        }

        if (!queryString.startsWith("?")) {
            throw new IllegalArgumentException("The '?' character must be included");
        }

        return baseUrl.contains("?")
            ? baseUrl + QUERY_PARAM_SEPARATOR + queryString.substring(1)
            : baseUrl + queryString;
    }

    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getTestQueryString() {
        return "SELECT 1";
    }

    @Override
    public SELF withDatabaseName(final String databaseName) {
        this.databaseName = databaseName;
        return self();
    }

    @Override
    public SELF withUsername(final String username) {
        this.username = username;
        return self();
    }

    @Override
    public SELF withPassword(final String password) {
        this.password = password;
        return self();
    }

    @Override
    protected void waitUntilContainerStarted() {
        getWaitStrategy().waitUntilReady(this);
    }
}
