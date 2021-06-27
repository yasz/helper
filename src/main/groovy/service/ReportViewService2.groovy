package service

import com.alibaba.fastjson.JSON
import common.Const
import tool.DBHelper
import tool.DocxHelper

import java.sql.Connection

/**
 * Created by Peter.Yang on 2021/1/22.
 */
class ReportViewService2 {
    //导出报告单
//    9v
//    8v
//    7v
//    6v
//    5v
//    4v
//    4e
//    3v
//    3e
//    2v
//    2e
//    1v
//    11v
//    10v

    static def classname = """9v
8v
7v
6v
5v
4v
4e
3v
3e
2v
2e
1v
11v
10v""".split("\n")


    static void merge(String[] files) {
        classname.eachWithIndex { String classname, int i ->
//
            DocxHelper.toPDF("out/${classname}.docx", "out/${classname}.pdf")
        }
    }

    static void main(String[] args) {
        def db = DBHelper.instance
        OutputStream pdfOs = new FileOutputStream("out\\190124.pdf")
        getByVano(['6v'], '212', pdfOs, db.conn)
//        ['1v', '2v','2e','3v','3e','4v','4e','5v','6v','7v','8v','9v','10v','11v'],
        return
    }

    static void getByVano(def classes, String sem, OutputStream pdfOs, Connection conn) {
//先到处DOCX，最终汇总成PDF
        def subjectno = JSON.parseObject(DBHelper.query("""select to_jsonb(json_object_agg( subject,"subjectNo")) from subjects
where enable is true
""", conn)[0][0].value)
        def allSum = JSON.parseObject(DBHelper.query("""select to_jsonb(json_object_agg(VANO,A1)) FROM 
(SELECT vano,to_jsonb(json_object_agg( subject,sumscore )) as a1 FROM bi_graduate
group by vano
)T1
""", conn)[0][0].value)

        classes.each { String class1 ->
            def sql = """SELECT
\tt1.classname,t1.vano,t4.enname,t4.cnname, subject_json,comment_json,ib_json
FROM
\tsubjects_subject t1
\tLEFT JOIN subjects_comment t2 ON t1.vano = t2.vano 
\tAND t1.sem = t2.sem
\tLEFT JOIN subjects_ib t3 ON t3.vano = t2.vano 
\tAND t3.sem = t2.sem 

\tleft join va1 t4 on t1.vano=t4.vano
WHERE
\tT1.SEM = '${sem}'  
and t1.classname in ('${class1}')

ORDER BY
\t1,
\t2;
\t
"""
            println(sql)
            def vanos = []

            def rs = DBHelper.query(sql, conn)
            rs.each { it ->
                def lastyear = "20${Integer.parseInt(sem.substring(0, 2)) - 1}"
                def longtsem = "${lastyear}-20${sem.substring(0, 2)}-${sem.substring(2)}"
                def paras = [sem: longtsem]
                it.each { i ->
                    paras[i.key] = i.value
                }
                def subjects = JSON.parseObject(it.subject_json.value)

                def comments = JSON.parseObject(it.comment_json ? it.comment_json.value : "")
                def ibs = JSON.parseObject(it.ib_json ? it.ib_json.value : "")
                //doc模板里的学科是动态生成的，需将【中文,5,6】按照编号顺序组成【n01:中文,s01:5,t01:6】
                //排除【英文】以及【班主任】
                def sortSubjects = subjects.sort { subjectno[it.key] }
                sortSubjects.eachWithIndex { subject, i ->
                    paras["n${sprintf('%02d', i + 1)}"] = subject.key.toUpperCase().replace("高中", "")
                    paras["s${sprintf('%02d', i + 1)}"] = tool.CalHelper.vascore2(subject.value.sus)
                    paras["t${sprintf('%02d', i + 1)}"] = tool.CalHelper.vascore3(subject.value.sum, subject.key, paras.classname)
                    paras["a${sprintf('%02d', i + 1)}"] = tool.CalHelper.vascore2(allSum[it.vano][subject.key])
//                    if(subject.key =='英文-听'){
//                    paras["a${sprintf('%02d', i + 1)}"] = tool.CalHelper.vascore2(allSum[it.vano][subject.key])
//                    }

//                    paras["s${sprintf('%02d', i + 1)}"] = subject.value.sus
//                    paras["t${sprintf('%02d', i + 1)}"] = subject.value.sum

                    if (subject.value.sus == null) {
                        paras["s${sprintf('%02d', i + 1)}"] = '-'
                    }
                    if (subject.value.sum == null) {
                        paras["t${sprintf('%02d', i + 1)}"] = '-'
                    }
                    if (!allSum[it.vano][subject.key] ||  allSum[it.vano][subject.key]== null) {
                        paras["a${sprintf('%02d', i + 1)}"] = '-'
                    }

                    paras["comment${sprintf('%02d', i + 1)}"] = comments[subject.key]
                }
                def minusSubjects = comments.findAll { c ->
                    !subjects.find { s -> c.key == s.key }
                }
                def minusCount = 0
                minusSubjects.sort { subjectno[it.key] }.eachWithIndex { e, j ->
                    if (j == 0) {
                        return
                    }
                    paras["n${sprintf('%02d', subjects.size() + j)}"] = e.key
                    paras["comment${sprintf('%02d', subjects.size() + j)}"] = e.value
                    paras["s${sprintf('%02d', subjects.size() + j)}"] = "-"
                    paras["t${sprintf('%02d', subjects.size() + j)}"] = "-"
                    paras["a${sprintf('%02d', subjects.size() + j)}"] = "-"

                    minusCount++
                }

                paras.comment00 = comments['班主任']
                ibs.each { ib ->
                    paras[ib.key] = ib.value
                }

                def doc = new DocxHelper(Const.tmpPath)
            if (sem.endsWith("1")) {
                doc.deleteCol("总评")
            }
//                doc.deleteCol("总评")
                if (paras.classname.startsWith("1v")) {
                    doc.deleteCol("总评")
                    doc.deleteCol("日常")
                    doc.deleteCol("考试")
                    doc.deleteRow("学识渊博")
                }
                //删除多余行
                (subjects.size() + minusCount + 1).upto(18) {
                    doc.deleteRow("n${sprintf('%2d', it)}")
                }
                doc.replace(paras).saveAsOutputStream(new FileOutputStream(new File(it.vano + ".docx")))
                vanos += it.vano
            }
            def fileList = vanos.collect { e -> e + ".docx" }
            DocxHelper.merge(fileList, "${class1}.docx")
            DocxHelper.toPDF("${class1}.docx","${class1}.pdf")
        }

//        doc.replace(paras).saveAsPDFOutputStream(pdfOs)
    }
}