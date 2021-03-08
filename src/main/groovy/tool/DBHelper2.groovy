package tool

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import groovy.sql.GroovyResultSet
import groovy.sql.Sql

import java.sql.Connection
import java.sql.Driver
import java.sql.DriverManager

/**
 * Created by Peter.Yang on 2019/6/24.
 */
@Singleton
class DBHelper2 {

    def ds = new HikariDataSource(new HikariConfig("${System.getProperty("user.dir")}/db.properties"))

    def query(sqlStr){
        def rs = []
        def sql = new Sql(ds.getConnection())
        try {
            sql.eachRow(sqlStr) { line->
                rs<<line.toRowResult()
            }
        } finally {

        }
        return rs
    }
    def close(){
        ds.close()
    }
}
