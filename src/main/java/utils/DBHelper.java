package utils;
import groovy.sql.Sql;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class DBHelper {
    private static final Map<String, Map<String, String>> config;
    private Connection connection;
    private Sql sql;
    static {
        Yaml yaml = new Yaml();
        InputStream inputStream = DBHelper.class.getClassLoader().getResourceAsStream("db.yml");
        config = yaml.load(inputStream);
    }
    public DBHelper(String dbAlias) throws SQLException {
        Map<String, String> dbConfig = config.get(dbAlias);
        if (dbConfig == null) {
            throw new SQLException("No configuration found for " + dbAlias);
        }
        String url = dbConfig.get("jdbc");
        String username = dbConfig.get("username");
        String password = dbConfig.get("password");
        this.connection = DriverManager.getConnection(url, username, password);
        this.sql = new Sql(connection);
    }

    public List<Map<String, Object>> query(String sqlStr) throws SQLException {
        List<Map<String, Object>> resultList = new ArrayList<>();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = this.connection.createStatement();
            rs = stmt.executeQuery(sqlStr);
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                int cols = rs.getMetaData().getColumnCount();
                for (int i = 1; i <= cols; i++) {
                    row.put(rs.getMetaData().getColumnLabel(i), rs.getObject(i));
                }
                resultList.add(row);
            }
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* Ignored */ }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* Ignored */ }
            // Note: Do not close the connection here if you intend to reuse it.
        }
        return resultList;
    }
    public long executeInsertAndGetId(String sqlStr) throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;
        try {
            // 执行插入操作
            stmt = this.connection.createStatement();
            int affectedRows = stmt.executeUpdate(sqlStr);

            // 检查是否成功插入了数据
            if (affectedRows == 0) {
                throw new SQLException("Insert failed, no rows affected.");
            }

            // 查询最后插入行的ID
            rs = stmt.executeQuery("SELECT last_insert_rowid()");
            if (rs.next()) {
                return rs.getLong(1); // 返回自增主键的值
            } else {
                throw new SQLException("Insert failed, no ID obtained.");
            }
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* Ignored */ }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* Ignored */ }
        }
    }


    public boolean executeUpdate(String sqlStr) throws SQLException {
        Statement stmt = null;
        try {
            // 创建Statement对象
            stmt = this.connection.createStatement();

            // 执行SQL语句
            int affectedRows = stmt.executeUpdate(sqlStr);

            // 如果受影响的行数大于0，则操作成功
            return affectedRows > 0;
        } finally {
            // 确保Statement对象被关闭
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* Ignored */ }
            // Note: Do not close the connection here if you intend to reuse it.
        }
    }


    // Other utility methods (like close) if necessary
    public void close() throws SQLException {
        if (this.connection != null && !this.connection.isClosed()) {
            this.connection.close();
        }
    }

}
