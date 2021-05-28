package service

import com.alibaba.fastjson.JSON
import common.Const
import tool.DBHelper
import tool.DBHelper2
import tool.DocxHelper
import tool.CalHelper
import tool.TextHelper
import yjh.helper.Excelhelper

/**
 * Created by Peter.Yang on 2019/6/22.
 * 解析 考察项比例
 */
class ReportMidterm {

    static void main(String[] args) {

        def sql =
"""SELECT T2.vano,t1.classname,t2.cnname,t2.enname,t1.subject_json FROM subjects_subject t1
left join va1 t2 on t1.vano=t2.vano
where t1.sem='212'
and t1.classname = '7v';
"""
//        """
//SELECT t2.cnname,t2.enname,t3.classname,t3.classno,to_json(json_object_agg(t1.key1||t1.subject,t1.value1)) as json1 FROM report  t1
//left join va1 t2
//on t1.vano=t2.vano
//left join va3 t3
//on t1.sem=t3.sem
//and t1.vano=t3.vano
//WHERE t1.KEY1 in ('attitude','midtermexam') --attitude
//AND t1.SEM = '212' and value1 <> ''
//and classname in('7v','8v','9v')
//group by 1,2,3,4
//"""
        new DBHelper2("ruianva.cn",'postgres','ruianVA123').query(sql).eachWithIndex( (row,i)->{
            def paras = [:]

            paras.cnname=row.cnname
            paras.enname=row.enname
//            paras.midterm中文=3
//
//            paras.classname=row.classname
            JSON.parseObject(row.subject_json.value).each{
//                println(it)
                paras["sus"+it.key]=CalHelper.vascore2(it.value.sus)
                paras["sum"+it.key]=CalHelper.vascore2(it.value.sum)
                if(it.value.sum == null) paras["sum"+it.key] = ''
                if(it.value.sus == null) paras["sus"+it.key] = ''

            }

                def doc = new DocxHelper("C:\\Users\\peterjiahao\\Desktop\\midterm.docx")
            doc.replace(paras).saveAs("${row.classname}-${row.cnname}.docx")

//            System.exit(0)
//            println(1)
        })
    }
}
