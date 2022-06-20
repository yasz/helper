package service

import com.alibaba.fastjson.JSON
import common.Const
import tool.DBHelper
import tool.DocxHelper
import tool.YamlHelper
import yjh.helper.DocHelper
import yjh.helper.Excelhelper
import yjh.helper.StringHelper

import java.lang.reflect.Field
import java.sql.Connection

/**
 * Created by Peter.Yang on 2021/1/22.
 * 作为标准导出服务
 */
class CourseReport {

    static HashMap ymlHash = YamlHelper.file2hash("C:\\2.dev\\1.java\\helper\\build\\resources\\main\\job1.yml")
    static String file = "C:\\Users\\yangj\\Downloads\\课程.xlsx"
    static String docPath = CourseReport.ymlHash.get("courseReportTmplate")

    static void main(String[] args) {
        def eh = new Excelhelper(this.file)
        def classList = eh.setSheet("课").read()
//        println(classList)
        def h = StringHelper.array2hash(classList[0], classList)
        def unitList = eh.setSheet("单元").read()
        h.each {HashMap e ->
            {
                println(e)
                if (e["科目(勿填,自动)"] == CourseReport.ymlHash.get("subject")) {
                    def doc = new DocxHelper(this.docPath)
                    def hh = [:]
                    e.each{ k, v->
                        hh.put(k, DocxHelper.newlineToBreakHack(v.toString()    ))
//                        hh.put(k,v)
                    }
                    hh.put("grade",StringHelper.int2chineseNum(Integer.parseInt(e["年级(勿填,自动)"].toString().substring(0,1))) )
                    println(hh)
                    doc.replace(hh)
                    doc.saveAs("${e["课程编号(勿填,自动)"]}-${e["科目(勿填,自动)"]}-${e["课程名"].toString().replaceAll("\n","")}.docx")
                    println("export " + "${e["课程名"]}.docx")
//                    System.exit(1)
                }
            }
        }
        println(classList)
    }
}