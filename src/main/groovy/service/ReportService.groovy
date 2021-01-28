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
        def studentInfoHash = [:]
        def db = new DBHelper('ruianva.cn', 'postgres', 'ruianVA123')

        //学科分数视图。顺带把科目编号、类型保存

        db.query("select * from report11 where sem='${Const.sem}'").each { it ->
            subjectHash[it.vano] = subjectHash[it.vano] ? subjectHash[it.vano] : [:]
            subjectHash[it.vano][it.subjectno] = subjectHash[it.vano][it.subjectno] ? subjectHash[it.vano][it.subjectno] : [:]
            subjectHash[it.vano][it.subjectno] = it
            scoreParas[it.vano] = scoreParas[it.vano] ? scoreParas[it.vano] : [:]
            scoreParas[it.vano][it.subjectno] = it
        }

        db.query("select * from report11 where sem='${Const.sem}'").each { it ->
            subjectHash[it.vano] = subjectHash[it.vano] ? subjectHash[it.vano] : [:]
            subjectHash[it.vano][it.subjectno] = subjectHash[it.vano][it.subjectno] ? subjectHash[it.vano][it.subjectno] : [:]
            subjectHash[it.vano][it.subjectno] = it
            scoreParas[it.vano] = scoreParas[it.vano] ? scoreParas[it.vano] : [:]
            scoreParas[it.vano][it.subjectno] = it
        }

        db.query("""select t3.cnname,t3.enname,t1.*,t2."subjectNo" from report20 t1 left join subjects t2 on t1.subject=t2.subject
left join va1 t3 on t1.vano =t3.vano  where sem='${Const.sem}' """).each { it -> //评语视图
            subjectHash[it.vano] = subjectHash[it.vano] ? subjectHash[it.vano] : [:]
            subjectHash[it.vano][it.subjectno] = subjectHash[it.vano][it.subjectno] ? subjectHash[it.vano][it.subjectno] : [:]
            subjectHash[it.vano][it.subjectno]["comment"] = it["comment1"]
        }
        if (Const.sem.endsWith("2")) {
            db.query("select t1.*,vascore2(年度总评) as zp,年度总评 as zp100 from \"19-20学年汇总\" t1").each { it -> //评语视图
                subjectHash[it.vano][it.subjectno]["type"] = it.科目类别
                subjectHash[it.vano][it.subjectno]["name"] = it.subject
                subjectHash[it.vano][it.subjectno]["zp"] = it.zp
            }
        }
        db.query("""SELECT classname,t2.classno,t1.* FROM report01 t1
left join va3 t2 on t1.vano=t2.vano and t1.sem=t2.sem where t1.sem='${Const.sem}' """).each { it -> //IB分
            ibHash[it.vano] = it
        }
        db.query("""select * from va1""").each { it ->
            studentInfoHash[it.vano] = it
        }

        subjectHash.each { vano, subjectUnitHash ->
            def paras = [:]
            paras["enname"] = studentInfoHash[vano]["enname"] ? studentInfoHash[vano]["enname"] : paras["enname"]
            paras["cnname"] = studentInfoHash[vano]["cnname"] ? studentInfoHash[vano]["cnname"] : paras["cnname"]

            ibHash[vano].each { key, value ->
                paras[key] = value
            }
            def xCount = 0
            subjectUnitHash.eachWithIndex { subjectno, hs, i ->
                def subjectnoSeq = sprintf('%02d', i)
                try {
                    paras["comment${subjectnoSeq}"] = hs.comment ? hs.comment : "-"
                } catch (Exception e) {
//                    println "发现异常：" + e
                }
                //使用统一模板后全部改造
                if (subjectno == "00") return

                paras["s${subjectnoSeq}"] = hs.日常7 ? hs.日常7 : ""
                paras["t${subjectnoSeq}"] = hs.考试7 ? hs.考试7 : "-"
                paras["n${subjectnoSeq}"] = hs.subject ? hs.subject : "-"

            }

//            paras += outputCommonParas
            def classname = paras.classname.toString()
            def classLevel = Integer.parseInt(classname.substring(0, classname.length() - 1))
//            println(paras)
//            return;
            def outputName = "out\\g${classLevel}\\${classname}_${paras.classno}_${vano}_${paras.cnname}.docx"
//            paras=[:]
            def tmpPath = Const.reportJuniorTmpPath
            if (classLevel != 9) {
                return
            }
            if (classLevel >= 10) {
                tmpPath = Const.reportHighTmpPath
            } else if (classLevel > 6) {
                tmpPath = Const.reportSeniorTmpPath
                if (xCount > 0) {
                    tmpPath = Const.reportSenior2TmpPath
                }
            } else if (classLevel >= 2) {
                tmpPath = Const.reportMiddleTmpPath
                if (xCount > 0) {
                    tmpPath = Const.reportMiddle2TmpPath
                }
            } else if (classLevel == 1) {
                tmpPath = Const.reportJuniorTmpPath
            }
//            tmpPath = "D:\\3.ws\\1.idea\\helper\\dat\\tmp\\tmp0.docx"
            def doc = new DocxHelper(tmpPath)
            if (Const.sem.endsWith("1")) {
                doc.deleteCol("年度总评")
            }
            if (classLevel == 1) {
                doc.deleteCol("日常").deleteCol("考试")
            }
            doc.replace(paras).saveAs(outputName)
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
