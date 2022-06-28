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


    static def classname = """3e""".split("\n")
    static boolean showOriginal = true

    static void merge(String[] files) {
        classname.eachWithIndex { String classname, int i ->
//
            DocxHelper.toPDF("out/${classname}.docx", "out/${classname}.pdf")
        }
    }

    static void main(String[] args) {
        def db = DBHelper.instance
        OutputStream pdfOs = new FileOutputStream("222.pdf")
//        ['1v', '2v', '3v','3e', '4v', '4e', '5v', '5e', '6v', '7v', '8v', '9v', '10v', '11v', '12v']
        def grades = ['1v', '2v', '3v','3e', '4v', '4e', '5v', '5e', '6v', '7v', '8v', '9v', '10v', '11v']
//        def grades = ['9v']

        getByVano(grades, Const.sem, pdfOs, db.conn)
//        ReportViewService2.showOriginal = true
//        getByVano(grades, '202', pdfOs, db.conn)
//        ['1v', '2v','2e','3v','3e','4v','4e','5v','6v','7v','8v','9v','10v','11v','12v'],
        return
    }

    static void getByVano(def classes, String sem, OutputStream pdfOs, Connection conn) {
//先到处DOCX，最终汇总成PDF
        def subjectMapping = JSON.parseObject(DBHelper.query("""select to_jsonb(json_object_agg(evaluation_name,json2)) as json3 from 
(select 
evaluation_name,to_jsonb(json_object_agg(subject,json1)) as json2
from 
(select 
subject,dim_evaluations.evaluation_name,
to_jsonb(json_object_agg(grade,
ARRAY
[lv7_mapping,
lv6_mapping,
lv5_mapping,
lv4_mapping,
lv3_mapping,
lv2_mapping,
lv1_mapping])) as json1
 from  courses.dim_evaluations
where sem='${Const.sem}'
and dim_evaluations.evaluation_name in ('sum','sus')
GROUP BY 1,2
)t1
group by 1
)t2""", conn)[0][0].value)
        def subjectno = JSON.parseObject(DBHelper.query("""select to_jsonb(json_object_agg( subject,"subjectNo")) from subjects
where enable is true
""", conn)[0][0].value)

        def attendancejson = JSON.parseObject(DBHelper.query("""select to_jsonb(json_object_agg( vano,json1)) from (
select vano,json_object_agg( event_name,count ) as json1 from 
(
SELECT vano,case event_name when '迟到' then 'late' else 'absense' end as event_name ,COUNT(*) FROM events.class_event 
WHERE EVENT_NAME IN ('迟到','旷课')
AND D2S(event_date)='${Const.sem}'
GROUP BY 1,2
UNION
SELECT VALUE1,'leave' as event_name,COUNT(*) AS c1 FROM (
select distinct resultsno,value1  
from 
forms_results
WHERE 
formno='3'
and key1='vano'
AND D2S(createtime::date ) = '${Const.sem}'
ORDER BY 2 DESC
)t1
GROUP BY 1
)t2
group by 1
)t3
""", conn)[0][0].value)

        def sql = """with attendance as (
select vano,json_object_agg( event_name,count ) as attendance_json from 
(
SELECT vano,case event_name when '迟到' then 'late' else 'absense' end as event_name ,COUNT(*) FROM events.class_event 
WHERE EVENT_NAME IN ('迟到','旷课')
AND D2S(event_date)='222'
GROUP BY 1,2
UNION
SELECT VALUE1,'leave' as event_name,COUNT(*) AS c1 FROM (
select distinct resultsno,value1  
from 
forms_results
WHERE 
formno='3'
and key1='vano'
AND D2S(createtime::date ) = '${Const.sem}'
ORDER BY 2 DESC
)t1
GROUP BY 1
)t2
group by 1
),a100 as(
select vano,to_jsonb(json_object_agg( subject,year100)) as a100json
 from
courses.stat_evaluations3_year
group by 1
) 
SELECT classname,array_agg(row_to_json(A1.*)) from(
  SELECT
\tt1.classname,t1.vano,t4.enname,t4.cnname, subject_json,comment_json,ib_json,attendance_json,a100json
FROM
\tcourses.sem_evaluations t1
\tLEFT JOIN courses.sem_comments t2 ON t1.vano = t2.vano 
\tAND t1.sem = t2.sem
\tLEFT JOIN courses.sem_ibs t3 ON t3.vano = t2.vano 
\tAND t3.sem = t2.sem 
\tleft join va1 t4 on t1.vano=t4.vano
\tleft join attendance t5 on t1.vano=t5.vano
\tleft join a100 t6 on t1.vano=t6.vano
WHERE
\tT1.SEM = '${Const.sem}'  
and t1.classname in ('${classes.join("','")}')
)A1
group by 1
"""
        def rs = DBHelper.query(sql, conn)
        def lastyear = "20${Integer.parseInt(sem.substring(0, 2)) - 1}"
        def longtsem = "${lastyear}-20${sem.substring(0, 2)}-${sem.substring(2)}"
        def vanos = [] //用于一个班对应一份汇总报告时记录各个学生学号
        rs.each { classmates ->
            vanos = []
            String classname = classmates[0]
            def classmatesDims = classmates[1].getArray()
            classmatesDims.each { classmateDims ->
                def it = JSON.parseObject(classmateDims.toString())
                def ibs = JSON.parseObject(it.ib_json.toString())
                def subjects = JSON.parseObject(it.subject_json.toString())
                def a100s = JSON.parseObject(it.a100json.toString())
                def attendances = JSON.parseObject(it.attendance_json.toString())
                def comments = JSON.parseObject(it.comment_json.toString())
                vanos += it.vano
                def paras = [sem: longtsem]
                it.each { i ->
                    paras[i.key] = i.value
                }
                def grade = paras.classname.substring(0, paras.classname.length() - 1)
                def sortSubjects = subjects.sort { subjectno[it.key] }
                sortSubjects.eachWithIndex { subject, i ->
                    paras["n${sprintf('%02d', i + 1)}"] = subject.key.toUpperCase().replace("高中", "")
                    def susMapping = subjectMapping["sus"][subject.key] ? subjectMapping["sus"][subject.key][grade] : null
                    def sumMapping = subjectMapping["sum"][subject.key] ? subjectMapping["sum"][subject.key][grade] : null
                    paras["s${sprintf('%02d', i + 1)}"] = subject.value.sus
                    paras["t${sprintf('%02d', i + 1)}"] = subject.value.sum
                    paras["a${sprintf('%02d', i + 1)}"] = a100s[subject.key]
                    paras["a00"] = a100s.英文
                    if (!showOriginal) {
                        paras["s${sprintf('%02d', i + 1)}"] = tool.CalHelper.vascore4(subject.value.sus, susMapping)
                        paras["t${sprintf('%02d', i + 1)}"] = tool.CalHelper.vascore4(subject.value.sum, sumMapping)
                        paras["a${sprintf('%02d', i + 1)}"] = tool.CalHelper.vascore4(a100s[subject.key])
                        paras["a00"] = tool.CalHelper.vascore4(a100s.英文)

                    }
                    if (subject.value.sus == null) {
                        paras["s${sprintf('%02d', i + 1)}"] = '-'
                    }
                    if (subject.value.sum == null) {
                        paras["t${sprintf('%02d', i + 1)}"] = '-'
                    }
                    paras["comment${sprintf('%02d', i + 1)}"] = comments ? comments[subject.key] : "-"
                }
                def minusSubjects = comments.findAll { c ->
                    !subjects.find { s -> c.key == s.key }
                }//有评语但是没有评价的科目
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

                paras.comment00 = comments ? comments['班主任'] : "-"
                ibs.each { ib ->
                    paras[ib.key] = ib.value
                }
                //初始化这三个参数
                paras.late = 0
                paras.leave = 0
                paras.absense = 0
                attendancejson[it.vano] ? paras += attendancejson[it.vano] : 0
                def doc = new DocxHelper(Const.tmpPath)
                if (paras.classname.startsWith("1v") and !showOriginal) {
                    doc.deleteCol("日常")
                    doc.deleteCol("考试")
                    doc.deleteRow("学识渊博")
                    doc.deleteCol("总评")
                } else if (sem.endsWith("1")) {
                    doc.deleteCol("总评")
                }
                //删除多余行
                (subjects.size() + minusCount + 1).upto(18) {
                    doc.deleteRow("n${sprintf('%2d', it)}")
                }
                doc.replace(paras).saveAsOutputStream(new FileOutputStream(new File("out/${it.vano}.docx")))

            }
            def suffix = showOriginal ? "_original" : ""
            def fileList = vanos.collect { vano -> "out/${vano}.docx" }
            DocxHelper.merge(fileList, "out/${classname}${suffix}.docx")
            DocxHelper.toPDF("out/${classname}${suffix}.docx", "out/${classname}${suffix}.pdf")
        }



    }

}

