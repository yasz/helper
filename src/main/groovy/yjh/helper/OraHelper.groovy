package yjh.helper




/**
 * Created by Administrator on 3/6/2017.
 */

class OraHelper {
    DBConn3 conn
    static def ctlSql= """
    SELECT COLUMN_NAME,CASE
WHEN DATA_TYPE LIKE '%CHAR%' THEN 'CHAR('||DATA_LENGTH||')'
--WHEN DATA_TYPE LIKE '%NUMBER%' AND DATA_PRECISION IS NULL THEN 'INT'
--WHEN DATA_TYPE LIKE '%NUMBER%' AND DATA_PRECISION IS NOT NULL THEN DATA_TYPE||'('||DATA_PRECISION||','||DATA_SCALE||')'
WHEN DATA_TYPE LIKE '%TIMESTAMP%' THEN 'TIMESTAMP "yyyy-mm-dd hh24:mi:ss"'
WHEN DATA_TYPE LIKE '%DATE%' THEN 'DATE "yyyy-mm-dd"'
ELSE ' '
END
 FROM
ALL_TAB_COLUMNS  WHERE OWNER = UPPER('#db')
AND TABLE_NAME = UPPER('#tab')
ORDER BY COLUMN_ID
"""

    private OraHelper(String dbid) {
        conn = DBConn3.getInstance(dbid)
    }
    private static OraHelper uniqueObj
    synchronized  static OraHelper getInstance(String dbid){
        if (uniqueObj == null) {
            uniqueObj = new OraHelper(dbid);
            return uniqueObj;
        }
        return uniqueObj;
    }


    String getCTL(String dbtab){


        def (db, tab) = dbtab.split("\\.")

        def sql = ctlSql
        sql=sql.replaceAll("#tab",tab)
        sql=sql.replaceAll("#db",db)

        def coll=uniqueObj.queryForList(sql)
        def cols = StringUtils.join(coll, ",\n  ");
        def tmp = """
load data
infile '${tab}.txt'
append
into table $db.$tab
fields terminated by ','
trailing nullcols
(
  $cols
)"""
        println tmp
        println "sqlldr "+uniqueObj.conn.dbUsername+"/"+uniqueObj.conn.dbPasswd+"@"+uniqueObj.conn.serviceName+" control=${tab}.ctl"
        tmp
    }

    static void main(String[] args) {

    }
    def query(sql){
        def ll=conn.queryForList(sql)

    }

    def queryForList(sql){
        def ll=conn.queryForList(sql)
        def l = []
        ll.each { unit->
            def result = ""
            unit.each {
                result +=(it+"\t")
            }
            l.add(result)
        }
        l
    }
}
