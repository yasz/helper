package tool

import groovy.sql.Sql

import java.sql.Driver
import java.sql.DriverManager

/**
 * Created by Peter.Yang on 2019/6/24.
 */
class DBHelper {
    def conn
    def sql
//    String ip
//    String user
//    String password
    DBHelper(ip,user,password){
        Class.forName('org.postgresql.Driver').newInstance() as Driver
        conn = DriverManager.getConnection("jdbc:postgresql://${ip}:5432/postgres","${user}","${password}")
        sql = new Sql(conn)
    }


    def query(sqlStr){

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

    }
    public static void main(String[] args) {
//        println(new DBHelper(args[0],args[1],args[2]).query("select 123 from where id = '${dis}'")[0][0])
    }
}
