package service

import com.alibaba.fastjson.JSON
import common.Const
import tool.DBHelper
import tool.DocxHelper

/**
 * Created by Peter.Yang on 2021/1/22.
 */
class ReportViewService {
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

    static def classname = """3e
6v
""".split("\n")

    static void merge(String[] files) {
        classname.eachWithIndex { String classname, int i ->
            def lists= new File("D:\\3.ws\\1.idea\\helper\\").listFiles().findAll {
                it.getName().startsWith(classname) &&  it.getName().endsWith("docx")
            }
            DocxHelper.merge(lists,"out/${classname}.docx" )

        }
    }

    static void main(String[] args) {


        def db = new DBHelper('ruianva.cn', 'postgres', 'ruianVA123')
        def subjectno = JSON.parseObject(db.query("""select to_jsonb(json_object_agg( subject,"subjectNo")) from subjects
where enable is true
""")[0][0].value)
        db.query("""SELECT
\tt1.classname,t1.vano,t4.enname,t4.cnname, subject_json,comment_json,ib_json
FROM
\tsubjects_subject t1
\tLEFT JOIN subjects_comment t2 ON t1.vano = t2.vano 
\tAND t1.sem = t2.sem
\tLEFT JOIN subjects_ib t3 ON t3.vano = t2.vano 
\tAND t3.sem = t2.sem 
\t
\tleft join va1 t4 on t1.vano=t4.vano
WHERE
\tT1.SEM = '211'  and classname in ('${classname.join("','")}')
ORDER BY
\t1,
\t2;
\t
""").each { it ->

            def lastyear = "20${Integer.parseInt(Const.sem.substring(0, 2)) - 1}"
            def longtsem = "${lastyear}-20${Const.sem.substring(0, 2)}-${Const.sem.substring(2)}"
            def paras = [sem: longtsem]
            it.each { i ->
                paras[i.key] = i.value
            }

            def subjects = JSON.parseObject(it.subject_json.value)
            def comments = JSON.parseObject(it.comment_json.value)
            def ibs = JSON.parseObject(it.ib_json.value)
            //doc模板里的学科是动态生成的，需将【中文,5,6】按照编号顺序组成【n01:中文,s01:5,t01:6】
            //排除【英文】以及【班主任】
            subjects.sort { subjectno[it.key] }.eachWithIndex { subject, i ->
                paras["n${sprintf('%02d', i + 1)}"] = subject.key.toUpperCase().replace("高中", "")
                paras["s${sprintf('%02d', i + 1)}"] = tool.CalHelper.vascore2(subject.value.sus)
                paras["t${sprintf('%02d', i + 1)}"] = tool.CalHelper.vascore3(subject.value.sum, subject.key, paras.classname)
                if (subject.value.sus == null) {
                    paras["s${sprintf('%02d', i + 1)}"] = '-'
                }
                if (subject.value.sum == null) {
                    paras["t${sprintf('%02d', i + 1)}"] = '-'
                }
                paras["comment${sprintf('%02d', i + 1)}"] = comments[subject.key]
            }
            def minusSubjects = comments.findAll { c ->
                !subjects.find { s -> c.key == s.key }
            }
            def minusCount = 0;
            minusSubjects.eachWithIndex { e, j ->
                if (j == 0) {
                    return
                }
                paras["n${sprintf('%02d', subjects.size() + j)}"] = e.key
                paras["comment${sprintf('%02d', subjects.size() + j)}"] = e.value
                paras["s${sprintf('%02d', subjects.size() + j)}"] = "-"
                paras["t${sprintf('%02d', subjects.size() + j)}"] = "-"
                minusCount++
            }

            paras["comment00"] = comments['班主任']
            ibs.each { ib ->
                paras[ib.key] = ib.value
            }

            def doc = new DocxHelper(Const.tmpPath)
            if (Const.sem.endsWith("1")) {
                doc.deleteCol("总评")
            }
            if (paras.classname.startsWith("1v")) {
                doc.deleteCol("日常")
                doc.deleteCol("考试")
                doc.deleteRow("学识渊博")


            }
            //删除多余行
            (subjects.size() +minusCount+ 1).upto(18) {
                doc.deleteRow("n${sprintf('%2d', it)}")
            }
            doc.replace(paras).saveAs("${paras.classname}${paras.cnname}.docx")
        }
        merge()


    }
}
