package com.wiresafe.front.matrix;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class SqlConnectionPool {

    public static interface SqlFunction<T, R> {

        R run(T connection) throws SQLException;

    }

    public static interface SqlConnectionConsumer<T> {

        void run(T conn) throws SQLException;

    }

    public static interface SqlConsumer {

        void run() throws SQLException;

    }

    private ComboPooledDataSource ds;

    public SqlConnectionPool(SynapseConfig.Database cfg) {
        ds = new ComboPooledDataSource();
        ds.setJdbcUrl("jdbc:postgresql:" + cfg.getConnection());
        ds.setMinPoolSize(1);
        ds.setMaxPoolSize(10);
        ds.setAcquireIncrement(2);
    }

    public Connection get() throws SQLException {
        return ds.getConnection();
    }

    public <T> T withConnFunction(SqlFunction<Connection, T> function) {
        try (Connection conn = get()) {
            return function.run(conn);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void withConsumer(SqlConsumer consumer) {
        try {
            consumer.run();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void withConnConsumer(SqlConnectionConsumer<Connection> consumer) {
        try (Connection conn = get()) {
            consumer.run(conn);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
