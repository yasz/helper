package yjh.model;

import java.util.List;

public class ColumnInfo {
    public String getTabName() {
        return tabName;
    }

    public void setTabName(String tabName) {
        this.tabName = tabName;
    }

    public String getTabCnName() {
        return tabCnName;
    }

    public void setTabCnName(String tabCnName) {
        this.tabCnName = tabCnName;
    }

    public String getColName() {
        return colName;
    }

    public void setColName(String colName) {
        this.colName = colName;
    }

    public String getColCnName() {
        return colCnName;
    }

    public void setColCnName(String colCnName) {
        this.colCnName = colCnName;
    }

    public String getOracleType() {
        return oracleType;
    }

    public void setOracleType(String oracleType) {
        this.oracleType = oracleType;
    }

    public String getTdType() {
        return tdType;
    }

    public void setTdType(String tdType) {
        this.tdType = tdType;
    }

    public String getHiveType() {
        return hiveType;
    }

    public void setHiveType(String hiveType) {
        this.hiveType = hiveType;
    }

    public String getIsPK() {
        return isPK;
    }

    public void setIsPK(String isPK) {
        this.isPK = isPK;
    }

    public ColumnInfo(List<String> metas){
        this.tabName = metas.get(0);
        this.tabCnName = metas.get(1);
        this.colName = metas.get(2);
        this.colCnName = metas.get(3);
        this.oracleType = metas.get(4);
        this.tdType = metas.get(5);
        this.hiveType = metas.get(6);
        this.isPK = metas.size()>7?metas.get(7):"";

        switch (this.tdType) {
            case ~/^CHAR.*/:  this.tdFLDType="VAR"+this.tdType; break;
            case ~/^(VARCHAR|VARBYTE).*/:  this.tdFLDType=this.tdType; break;
            default: this.tdFLDType="VARCHAR(20)";
        }

        switch (this.oracleType) {
            case ~/^VARCHAR.*/:  this.oracleLDRType=this.oracleType.replaceAll("VARCHAR2?(.*?)","CHAR\$1"); break;
            case ~/^CHAR.*/:  this.oracleLDRType=this.oracleType; break;
            case ~/^DATE.*/:  this.oracleLDRType='DATE "yyyy-mm-dd"'; break;
            case ~/^TIMESTAMP.*/:  this.oracleLDRType='TIMESTAMP "yyyy-mm-dd hh24:mi:ss"'; break;
            default: this.oracleLDRType="";
        }
    }
    public ColumnInfo(String tabName, String tabCnName, String colName, String colCnName, String oracleType, String tdType, String hiveType, String isPK) {
        this.tabName = tabName;
        this.tabCnName = tabCnName;
        this.colName = colName;
        this.colCnName = colCnName;
        this.oracleType = oracleType;
        this.tdType = tdType;
        this.hiveType = hiveType;
        this.isPK = isPK;
    }

    private String tabName;
    private String tabCnName;

    private String colName;
    private String colCnName;



    private String oracleType;
    private String tdType;
    private String hiveType;

    private String oracleLDRType

    String getOracleLDRType() {
        return oracleLDRType
    }

    void setOracleLDRType(String oracleLDRType) {
        this.oracleLDRType = oracleLDRType
    }

    String getTdFLDType() {
        return tdFLDType
    }

    void setTdFLDType(String tdFLDType) {
        this.tdFLDType = tdFLDType
    };
    private String tdFLDType;

    private String isPK;

}
