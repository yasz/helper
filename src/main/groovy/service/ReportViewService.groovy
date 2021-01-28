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

    static void main(String[] args) {

        def db = new DBHelper('ruianva.cn', 'postgres', 'ruianVA123')
        def subjectno=JSON.parseObject(db.query("""select to_jsonb(json_object_agg( subject,"subjectNo")) from subjects
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
\tT1.SEM = '211'  and classname in ('10v','11v')
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
            subjects.sort{subjectno[it.key]}.eachWithIndex { subject, i ->
                paras["n${sprintf('%02d', i + 1)}"] = subject.key
                if (subject.value.sus == null) {
                    paras["s${sprintf('%02d', i + 1)}"] = '-'
                    paras["t${sprintf('%02d', i + 1)}"] = '-'
                } else {
                    paras["s${sprintf('%02d', i + 1)}"] = subject.value.sus
                    paras["t${sprintf('%02d', i + 1)}"] = subject.value.sum
                }
                paras["comment${sprintf('%02d', i + 1)}"] = comments[subject.key]
            }
            paras["comment00"] = comments['班主任']
            ibs.each { ib ->
                paras[ib.key] = ib.value
            }
            def doc = new DocxHelper(Const.tmpPath)
            if (Const.sem.endsWith("1")) {
                doc.deleteCol("总评")
            }
            //删除多余行
            (subjects.size()+1).upto(18){
                doc.deleteRow("n${sprintf('%2d',it)}")
            }
            doc.replace(paras).saveAs("${paras.vano}${paras.cnname}.docx")
        }

    }


}