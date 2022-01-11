package service

import com.alibaba.fastjson.JSON
import common.Const
import tool.DBHelper
import tool.DBHelper2
import tool.DocxHelper
import tool.CalHelper
import tool.TextHelper
import yjh.helper.Excelhelper
import yjh.helper.Excelhelper2

/**
 * Created by Peter.Yang on 2019/6/22.
 * 解析 考察项比例
 */
class ReportMidterm {

    static void main(String[] args) {
//期中报告以导出 1.班级各科目分析pdf 2.个人总评

//第一部分按班级
        def sql =
"""SELECT * from 
courses.stat_221mid4
"""

        new DBHelper2("ruianva.cn",'postgres','ruianVA123').query(sql).eachWithIndex( (row,i)->{
            def paras = [:]
            def title = "${row.classname}-${row.subject}"
            Excelhelper2.s1(title,row.string_agg,new FileOutputStream("221mid"+title+".pdf"))

        })
    }
}