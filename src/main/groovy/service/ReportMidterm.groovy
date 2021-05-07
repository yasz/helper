package service

import common.Const
import tool.DBHelper
import tool.DBHelper2
import tool.DocxHelper
import tool.TextHelper
import yjh.helper.Excelhelper

/**
 * Created by Peter.Yang on 2019/6/22.
 * 解析 考察项比例
 */
class ReportMidterm {

    static void main(String[] args) {



        def sql = """
select classname,cnname,classno,json_object_agg ( subject , json ) as json from
(select cnname,classname,classno,subject,json_object_agg ( key1, value1 ) AS json 
from 
(SELECT t2.cnname,t3.classname,t3.classno,t1.subject,t1.key1,t1.value1 FROM report  t1
left join va1 t2
on t1.vano=t2.vano
left join va3 t3
on t1.sem=t3.sem
and t1.vano=t3.vano
WHERE t1.KEY1 in ('attitude','midtermexam') --attitude
AND t1.SEM = '212' and value1 <> ''
)t1
group by 1,2,3,4
)t2
where classname='7v'
group by 1,2,3

"""
        new DBHelper2("ruianva.cn",'postgres','ruianVA123').query(sql).eachWithIndex( (it,i)->{
            println(it)
            println(i)
        })
    }
}
