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

        def commentParas = [:]
        def scoreParas = [:]
        def h = [:]
        def h2 = [:]
        def db = new DBHelper('ruianva.tpddns.cn', 'postgres', 'ruianVA123')

        db.query("select * from reportSum2").each { it -> //科目汇总分数视图
            h[it.vano] = h[it.vano] ? h[it.vano] : [:]
            h[it.vano][it.subjectno] = h[it.vano][it.subjectno] ? h[it.vano][it.subjectno] : [:]
            h[it.vano][it.subjectno] = it
//            scoreParas[it.vano][it.subjectno] = it
        }

        db.query("select * from reportComment").each { it -> //评语视图
            h[it.vano] = h[it.vano] ? h[it.vano] : [:]
            h[it.vano][it.subjectno] = h[it.vano][it.subjectno] ? h[it.vano][it.subjectno] : [:]
            h[it.vano][it.subjectno]["comment"] = it["评语"]
//            commentParas[it.vano + it.subjectno] = it
        }
        db.query("""SELECT t1.vano,classname,t2.classno,vascore3(i0) i0,vascore3(i1) i1,  vascore3(i2) i2,vascore3(i3)
 i3,
vascore3(i4) i4,vascore3(i5) i5,vascore3(i6) i6,vascore3(i7) i7,vascore3(i8) i8,vascore3(i9)i9 FROM 平均分品格2 t1
left join va3 t2 on t1.vano=t2.vano and sem='2019-2020'""").each { it -> //评语视图
            h2[it.vano] = it
        }

        h.each { vano, hv ->
            def paras = [:]
            def hh2 = h2[vano]
            hh2.each{key,value->
                paras[key] = value
            }
            hv.each { subjectno, hs ->
                try {
                    paras["comment${subjectno}"] = hs.comment
                } catch (Exception e) {
//                    println "发现异常：" + e
                }
                if (subjectno == "00") return
                paras["s${subjectno}"] = hs.s?hs.s:""
                paras["t${subjectno}"] = hs.t?hs.t:"-"

                paras["enname"] = hs.enname?hs.enname:""
                paras["cnname"] = hs.cnname
            }

//            paras += outputCommonParas
            def classname=paras.classname.toString()
            def classLevel = Integer.parseInt(classname.substring(0, classname.length()-1))
            def outputName = "out\\g${classLevel}\\${classname}_${paras.classno}_${vano}_${paras.cnname}.docx"

            def tmpPath = Const.reportJuniorTmpPath
            if (classLevel >= 10) {
                tmpPath = Const.reportHighTmpPath
            }
            else if (classLevel >= 7) {
                tmpPath = Const.reportSeniorTmpPath
            } else if (classLevel >= 2) {
                tmpPath = Const.reportMiddleTmpPath
            } else if (classLevel == 1) {
                tmpPath = Const.reportJuniorTmpPath
            }

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
