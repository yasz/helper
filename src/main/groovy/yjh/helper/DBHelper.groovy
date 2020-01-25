package yjh.helper

import common.DBConst
import yjh.model.ColumnInfo

/**
 * Created by Peter.Yang on 5/4/2017.
 */


class DBHelper {
    static def getOracleLDR(List<ColumnInfo> units,String db){

        def cols = []
        units.each {
            cols.add(it.getColName()+" "+it.getOracleLDRType())
//            if(!it.getOracleLDRType().equals(""))
//            {
//                cols.add(it.getOracleLDRType())
//            }
        }
        def col = cols.join(",\n    ")
        def tab = units[0].getTabName()


        
        def instance = """
load data
infile '${tab}.txt'
append
into table ${db}.${tab}
fields terminated by ','
trailing nullcols
(
    $col
)"""
        return instance
    }


    static def getTdFLD(List<ColumnInfo> units,String db){

        def (fldCols,cols,cols2) = [[],[],[]]

        units.each {
            if(!it.getTdFLDType().equals(""))
            {
                fldCols.add(it.getColName()+" ("+it.getTdFLDType()+",NULLIF='' ")
                cols.add(it.getColName())
                cols2.add(":"+it.getColName())
            }
        }
        def fldCol = fldCols.join(",\n    ")
        def col = cols.join(",\n    ")
        def col2 = cols2.join(",\n    ")
        def tab = units[0].getTabName()

        def instance = """
ERRLIMIT 100;
LOGON dbc/dbc,dbc1;
DATABASE $db;
DROP TABLE $db.${tab}_E1;
DROP TABLE $db.${tab}_E2;
.SET RECORD VARTEXT "," NOSTOP
RECORD 1;
BEGIN LOADING $db.${tab}
ERRORFILES $db.${tab}_E1,$db.${tab}_E2;
DEFINE
    ${fldCol}
FILE=${db}.${tab}.txt;

INSERT INTO $db.${tab}(
    $col
)
VALUES(
    $col2
);
END LOADING;
LOGOFF;
"""
        return instance
    }

    static def getDdl(List metas,String dbType){
//从统一的columns.xls转化的List<List>列表获取各类DDL
        def st = 0
        def et = metas.size()-1
        def db = metas.get(st)[0]
        def tab = metas.get(st)[1]
        def tabtitle = metas.get(st)[2]
        def cols = new ArrayList<String>()
        def oracleColTitles = ""
        //初始化各个字段，cols
        st.upto(et){
            def col = metas.get(it)[3]
            DBConst.keyword.each{
                if(it == col){
                    col= "td_${col}"
                }
            }
            def coltitle = metas.get(it)[4]
            def oracletype = metas.get(it)[5]
            def tdtype = metas.get(it)[6]
            def hivetype = metas.get(it)[7]
            def hiveSAtype = 'string'
            switch (dbType){
                case "oracle":cols.push("$col $oracletype"); oracleColTitles = "$oracleColTitles\ncomment on column $tab.$col is '$coltitle';";break
                case "hivesa":cols.push("$col $hiveSAtype comment '$coltitle'");break
                case "hive":cols.push("$col $hivetype comment '$coltitle'");break
                case "td":cols.push("$col $tdtype TITLE '$coltitle'");break
            }
        }

        def col = cols.join(",\n    ")
        def tdDDL = """
--drop table $db.$tab;
create table $db.$tab (
    $col
);
comment on table tab is '$tabtitle';
"""

        def hiveSADDL = """
--drop table $db.${tab}sa;
create table $db.${tab}sa (
$col
)
row format delimited fields terminated by '\\001'
stored as textfile;
ALTER TABLE $db.${tab}sa SET TBLPROPERTIES ('comment' = '$tabtitle');
    """
        def hiveDDL = """
--drop table $db.${tab};
create table $db.$tab (
$col
)
row format delimited fields terminated by '\\001'
stored as textfile;
ALTER TABLE $db.$tab SET TBLPROPERTIES ('comment' = '$tabtitle');
    """
        def oracleDDL = """
--drop table $tab;115
create table $tab (
$col
);
comment on table tab is '$tabtitle';
$oracleColTitles
"""
        switch (dbType){
            case "oracle":return oracleDDL;break
            case "hivesa":return hiveSADDL;break
            case "hive":return hiveDDL;break
            case "td":return tdDDL;break
        }
    }
}
