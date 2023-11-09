package utils

import spock.lang.Specification
import spock.lang.Shared

class DBHelperTest extends Specification {
    @Shared
    DBHelper dbHelper = null

    def setup() {
        // 初始化 DBHelper 实例
        dbHelper = new DBHelper('sqlite1') // 使用正确的数据库别名来初始化
    }

    def cleanup() {
        // 清理资源，关闭数据库连接
        dbHelper?.close()
    }

    def "database query should return results"() {
        given: "A valid SQL query"
        String sqlQuery = "SELECT 1 as col"

        when: "The query is executed"
        List<Map<String, Object>> results = dbHelper.query(sqlQuery)

        then: "The results are not empty"
        !results.isEmpty()

        and: "The query returns the expected value"
        results.get(0).get("col") == 1
    }
}
