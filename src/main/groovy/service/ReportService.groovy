package service

import common.Const
import tool.DBHelper
import tool.DocxHelper

/**
 * Created by Peter.Yang on 2019/6/22.
 */
class ReportService {
    //导出报告单

    static void main(String[] args) {

//        def commentParas = [:]
        def scoreParas = [:]
        def subjectHash = [:]
        def ibHash = [:]  //IB分
        def db = new DBHelper('ruianva.tpddns.cn', 'postgres', 'ruianVA123')

        db.query("select * from reportSum2").each { it -> //科目汇总分数视图
            subjectHash[it.vano] = subjectHash[it.vano] ? subjectHash[it.vano] : [:]
            subjectHash[it.vano][it.subjectno] = subjectHash[it.vano][it.subjectno] ? subjectHash[it.vano][it.subjectno] : [:]
            subjectHash[it.vano][it.subjectno] = it
            scoreParas[it.vano] = scoreParas[it.vano] ? scoreParas[it.vano] : [:]
            scoreParas[it.vano][it.subjectno] = it
        }

        db.query("select * from reportComment").each { it -> //评语视图
            subjectHash[it.vano] = subjectHash[it.vano] ? subjectHash[it.vano] : [:]
            subjectHash[it.vano][it.subjectno] = subjectHash[it.vano][it.subjectno] ? subjectHash[it.vano][it.subjectno] : [:]
            subjectHash[it.vano][it.subjectno]["comment"] = it["评语"]
//            commentParas[it.vano + it.subjectno] = it
        }

        db.query("select t1.*,vascore2(年度总评) as zp,年度总评 as zp100 from \"19-20学年汇总\" t1").each { it -> //评语视图
            subjectHash[it.vano][it.subjectno]["type"] =  it.科目类别
            subjectHash[it.vano][it.subjectno]["name"] =  it.subject
            subjectHash[it.vano][it.subjectno]["zp"] =  it.zp
        }
        db.query("""SELECT classname,t2.classno,t1.* FROM 平均分品格2 t1
left join va3 t2 on t1.vano=t2.vano and sem='2019-2020-2'
order by 1,2""").each { it -> //IB分
            ibHash[it.vano] = it
        }

        subjectHash.each { vano, subjectUnitHash ->
            def paras = [:]
            def unitIbHash = ibHash[vano]
            unitIbHash.each{key,value->
                paras[key] = value
            }
            def xCount = 0
            subjectUnitHash.each { subjectno, hs ->
                try {
                    paras["comment${subjectno}"] = hs.comment?hs.comment:"-"
                } catch (Exception e) {
//                    println "发现异常：" + e
                }
                if (subjectno == "00") return
//                println(paras["s${subjectno}"]  + hs.s)
                paras["s${subjectno}"] = hs.日常7?hs.日常7:""
                paras["t${subjectno}"] = hs.考试7?hs.考试7:"-"
                paras["a${subjectno}"] = hs.zp?hs.zp:"-"
                if(hs.type=='x'){
                    xCount++
                    paras["s10${xCount}"] = hs.日常7?hs.日常7:""
                    paras["t10${xCount}"] = hs.考试7?hs.考试7:"-"
                    paras["a10${xCount}"] = hs.zp?hs.zp:"-"
                    paras["n10${xCount}"] = hs.subject?hs.subject:"-"
                    try {
                        paras["comment10${xCount}"] = hs.comment?hs.comment:"-"
                    } catch (Exception e) {
//                    println "发现异常：" + e
                    }

                }
                paras["enname"] = hs.enname?hs.enname:paras["enname"]
                paras["cnname"] = hs.cnname?hs.cnname:paras["cnname"]
            }

//            paras += outputCommonParas
            def classname=paras.classname.toString()
            def classLevel = Integer.parseInt(classname.substring(0, classname.length()-1))
//            println(paras)
//            return;
            def outputName = "out\\g${classLevel}\\${classname}_${paras.classno}_${vano}_${paras.cnname}.docx"
//            paras=[:]
            def tmpPath = Const.reportJuniorTmpPath
            if (classLevel >= 10) {
                tmpPath = Const.reportHighTmpPath
            }
            else if (classLevel > 6) {
                tmpPath = Const.reportSeniorTmpPath
                if(xCount>0){
                    tmpPath = Const.reportSenior2TmpPath
                }
            } else if (classLevel >= 2) {
                tmpPath = Const.reportMiddleTmpPath
                if(xCount>0){
                    tmpPath = Const.reportMiddle2TmpPath
                }
            } else if (classLevel == 1) {
                tmpPath = Const.reportJuniorTmpPath
            }
//            tmpPath = "D:\\3.ws\\1.idea\\helper\\dat\\tmp\\tmp0.docx"
            new DocxHelper(tmpPath).replace(paras).saveAs(outputName)

        }
    }


    static def n2b(str) {
        str == null ? "" : str
    }

    static String newlineToBreakHack(String r) {

        StringTokenizer st = new StringTokenizer(r, "\n\r\f");
        // tokenize on the newline character, the carriage-return character, and the form-feed character
        StringBuilder sb = new StringBuilder()

        boolean firsttoken = true;
        while (st.hasMoreTokens()) {
            String line = (String) st.nextToken();
            if (firsttoken) {
                firsttoken = false;
            } else {
                sb.append("</w:t><w:br/><w:t>")
            }
            sb.append(line);
        }
        return sb.toString();
    }

}
