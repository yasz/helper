package tool

import groovy.sql.Sql

import java.sql.Connection
import java.sql.Driver
import java.sql.DriverManager

/**
 * Created by Peter.Yang on 2019/6/24.
 */
@Singleton
class DBHelper {
    Sql sql
    Connection conn = DriverManager.getConnection("jdbc:postgresql://ruianva.cn:5432/postgres","postgres","ruianVA123")
    static def query(sqlStr,Connection conn){
        def sql = new Sql(conn)
        def rs = []
        try {
            sql.eachRow(sqlStr) { line->
                rs<<line.toRowResult()
            }
        } finally {
        }
        return rs
    }

    def close(){
        this.conn.close()
    }
}
