package yjh.helper

import common.DBConst

/**
 * Created by Administrator on 3/6/2017.
 */

class TDHelper {
    DBConn3 conn
    def tdsql= """
SELECT    TRIM(A1.databaseNAME),TRIM(A1.TABLENAME),
A3.commentstring TABLE_TITLE, --表comment
    TRIM(A1.ColumnName) COL,
    OREPLACE(A1.ColumnTitle,'0D'Xc) COL_TITLE,
    CASE
     WHEN A1.COLUMNTYPE='BV' THEN 'VARBYTE('||TRIM(columnlength)||')'
     WHEN A1.COLUMNTYPE='CF' THEN 'CHAR('||TRIM(columnlength)||')'
     WHEN A1.COLUMNTYPE='CV' THEN OREPLACE('VARCHAR('||TRIM(columnlength*2)||')' ,',','')
     WHEN A1.COLUMNTYPE='D' THEN 'NUMBER(' ||TRIM(DecimalTotalDigits)||','||TRIM(DecimalFractionalDigits)||')'
     WHEN A1.COLUMNTYPE='N' THEN 'NUMBER(' ||TRIM(DecimalTotalDigits)||','||TRIM(DecimalFractionalDigits)||')'
     WHEN A1.COLUMNTYPE='DA' THEN 'DATE'
     WHEN A1.COLUMNTYPE='F' THEN 'FLOAT'
     WHEN A1.COLUMNTYPE in ('I','I1','I2') THEN 'INTEGER'
     WHEN A1.COLUMNTYPE='TS' THEN 'TIMESTAMP(0)'
     WHEN A1.COLUMNTYPE='I8' THEN 'NUMBER(18,0)'
     END  AS ORA_TYPE
,
    CASE
     WHEN A1.COLUMNTYPE='BV' THEN 'VARBYTE('||TRIM(columnlength)||')'
     WHEN A1.COLUMNTYPE='CF' THEN 'CHAR('||TRIM(columnlength)||')'
     WHEN A1.COLUMNTYPE='CV' THEN OREPLACE('VARCHAR('||TRIM(columnlength)||')' ,',','')
     WHEN A1.COLUMNTYPE='D' THEN 'DECIMAL(' ||TRIM(DecimalTotalDigits)||','||TRIM(DecimalFractionalDigits)||')'
     WHEN A1.COLUMNTYPE='N' THEN 'NUMBER(' ||TRIM(DecimalTotalDigits)||','||TRIM(DecimalFractionalDigits)||')'
     WHEN A1.COLUMNTYPE='DA' THEN 'DATE'
     WHEN A1.COLUMNTYPE='F' THEN 'FLOAT'
     WHEN A1.COLUMNTYPE='I' THEN 'INTEGER'
     WHEN A1.COLUMNTYPE='I1' THEN 'BYTEINT COMPRESS (0,1 ) '
     WHEN A1.COLUMNTYPE='I2' THEN 'SMALLINT'
     WHEN A1.COLUMNTYPE='TS' THEN 'TIMESTAMP(0)'
     WHEN A1.COLUMNTYPE='I8' THEN 'BIGINT'
     END  AS TD_TYPE
,
CASE
     WHEN A1.COLUMNTYPE in('BV','CF','CV') THEN 'STRING'
     WHEN A1.COLUMNTYPE in('D','F') THEN 'DOUBLE'
     WHEN A1.COLUMNTYPE in('DA','TS') THEN 'TIMESTAMP'
     WHEN A1.COLUMNTYPE in ('I','I1','I2') THEN 'INT'
     WHEN A1.COLUMNTYPE='I8' THEN 'BIGINT'
     END  AS SPARK_TYPE,
CASE
    WHEN    A2.COLUMNNAME IS NULL THEN ''
ELSE       'T'
END    AS ORA_PK,
ORA_PK AS TD_PI
FROM       DBC.COLUMNSV  A1 LEFT JOIN DBC.INDICESV A2
    ON  A1.DatabaseName=A2.DatabaseName
    AND A1.TABLENAME = A2.TABLENAME
    AND A1.ColumnName = A2.ColumnName
LEFT JOIN DBC.TABLESV A3
on A1.DatabaseName=A3.DatabaseName
    and A1.TableName = A3.TABLENAME
WHERE      UPPER(A1.databasename)||'.'||UPPER(A1.TABLENAME) IN (#dbtab)
ORDER      BY a1.TABLENAME,a1.columnid;
"""

    private TDHelper(String dbid) {
        conn = DBConn3.getInstance(dbid)
    }

    static void main(String[] args) {
        if(args.length<2){
            println("please input dbsid,mode,db.tabs")
            System.exit(2)
        }
//        args = ['db1',"hive2",'s.SPT_STB_MOD_JT_MON_A'] //打包前请注释
        def tdh = new TDHelper(args[0])
        def mode = args[1]
        def rl
        if(mode == 'hive') {
            rl = tdh.getHiveddl(args[2])
        }else if(mode == 'hivesa'){
            rl = tdh.getHiveSAddl(args[2])
        }else if(mode == 'oracle'){
            rl = tdh.getOracleddl(args[2])
        }else{
            println "unknow mode:$mode"
        }

        rl.each {
            print(it)
        }
    }
    String geFLD(String dbtab){
        def (db, tab) = dbtab.split("\\.")
        def sql = ctlSql
        sql=sql.replaceAll("#tab",tab)
        sql=sql.replaceAll("#db",db)

    }

    def getOracleddl(dbtabs){
        dbtabs = "'"+dbtabs.replaceAll(",","','")+"'"
        def instancetdsql = tdsql.replaceAll(/#dbtab/,"$dbtabs")//metas通过连接TD数据库查询语句，实际上还可以通过CFG/ORACLESQL/...方式获取
        def metas=conn.queryForList(instancetdsql)//由于是多张表，这里要考虑分组获取
        def rl = getUnitArrange3(metas,1)

        def ddlList = []
        rl.each { index->
            int st=index[0]
            int et=index[1]
            def db = metas.get(st)[0]
            def tab = metas.get(st)[1]
            def tabtitle = metas.get(st)[2]
            def cols = new ArrayList<String>()
            st.upto(et){
                def col = metas.get(it)[3]
                DBConst.keyword.each{
                    if(it == col){
                        col= "td_${col}"
                    }
                }
                def coltype = metas.get(it)[5]
                def coltitle = metas.get(it)[4]
                cols.push("$col $coltype")
            }
            def col = cols.join(",\n")
            def template = """
    create table $tab (
    $col
    )row format delimited fields terminated by '\\001' stored as textfile;
    comment on table tab is '$tabtitle';
"""
            ddlList.add(template)
        }
        ddlList
    }


    def getHiveddl(String dbtabs){
        dbtabs = "'"+dbtabs.replaceAll(",","','")+"'"
        def instancetdsql = tdsql.replaceAll(/#dbtab/,"$dbtabs")
//metas通过连接TD数据库查询语句，实际上还可以通过CFG/ORACLESQL/...方式获取
        def metas=conn.queryForList(instancetdsql)
//由于是多张表，这里要考虑分组获取
        def rl = StringHelper.getUnitArrange(metas,1)

        def ddlList = []
        rl.each { index->
            int st=index[0]
            int et=index[1]
            def db = metas.get(st)[0]
            def tab = metas.get(st)[1]
            def tabtitle = metas.get(st)[2]
            def cols = new ArrayList<String>()
            st.upto(et){
                def col = metas.get(it)[3]
                DBConst.keyword.each{
                    if(it == col){
                        col= "td_${col}"
                    }
                }
                def coltype = metas.get(it)[7]
                def coltitle = metas.get(it)[4]
                cols.push("$col $coltype comment '$coltitle' ")
            }
            def col = cols.join(",\n")
            def template = """
    create table $db.$tab (
    $col
    )
    row format delimited
    stored as textfile;
    ALTER TABLE $db.$tab SET TBLPROPERTIES ('comment' = '$tabtitle');
    """
            ddlList.add(template)
        }
        ddlList
    }

    def getHiveSAddl(String dbtabs){
        //从TD知识库导出
        dbtabs = "'"+dbtabs.replaceAll(",","','")+"'"
        def instancetdsql = tdsql.replaceAll(/#dbtab/,"$dbtabs")//metas通过连接TD数据库查询语句，实际上还可以通过CFG/ORACLESQL/...方式获取
        def metas=conn.queryForList(instancetdsql)//由于是多张表，这里要考虑分组获取
        def rl = StringHelper.getUnitArrange(metas,1)

        def ddlList = []
        rl.each { index->
            int st=index[0]
            int et=index[1]
            def db = metas.get(st)[0]
            def tab = metas.get(st)[1]
            def tabtitle = metas.get(st)[2]
            def cols = new ArrayList<String>()
            st.upto(et){
                def col = metas.get(it)[3]
                DBConst.keyword.each{
                    if(it == col){
                        col= "td_${col}"
                    }
                }
                def coltype = 'string'
                def coltitle = metas.get(it)[4]
                cols.push("$col $coltype comment '$coltitle' ")
            }
            def col = cols.join(",\n")
            def template = """
    create table $db.${tab}sa (
    $col
    )
    row format delimited fields terminated by '\\001'
    stored as textfile;
    ALTER TABLE $db.${tab}sa SET TBLPROPERTIES ('comment' = '$tabtitle');
    """
            ddlList.add(template)
        }
        ddlList
    }


    String queryForList(sql){
        def result=conn.queryForList(sql)
        result.each {unit-> unit.each {print(it+"\t")}
            println()
        }
    }
}
