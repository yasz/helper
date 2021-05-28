package service

import com.alibaba.fastjson.JSON
import common.Const
import tool.DBHelper
import tool.TextHelper
import yjh.helper.Excelhelper

/**
 * Created by Peter.Yang on 2019/6/22.
 * 解析 评价项比例
 */
class ParseSubjectDim2 {

    static def sem = Const.sem
    static def wordListPath = "D:\\3.ws\\2.code\\ruianva.github.io\\5.assistant\\vano\\subjectDim2.txt"

    static def subjects = []
    static def h = [:]
    static def subjectDims = []

    static def eh = new Excelhelper('')
    static def op = "评价项\n" //标题行
    static void parse2(){
        def table = "reportv11"
        def keyColumn = "t1.subject||type1"
        def valueColumn = "score100"


        def filter = "t1  left join subjects t2 on t1.subject = t2.subject where sem='211'"
        def orderColumn = 'subjectNo'

        DBHelper.query("""select DISTINCT "orderColumn",${keyColumn} as keyColumn   from ${table} ${filter} order by 1""",
                DBHelper
                .instance.conn)
                .each {it->
                """create view bi_${table.split(" ")[0]} as select ${valueColumn},from ${table}"""
                }

    }
    static void parse(gradeNum){
        //s1:对于非二维表格，第一个先进行标准化(按块合并为standardTabs)
        eh.read().eachWithIndex { def line, int i ->

        }
        subjects = []
        h = [:]
        subjectDims = []
        //s2:构造结构化的hash对象
        eh.read().eachWithIndex { def line, int i ->
            if (i % (gradeNum+2) == 0) {  //要求每个单元格都有[9+2]行,这儿是首行
                line.findAll { it.toString().length() > 0 && it.toString() != '科目' }.each {
                    h[it.toString()] = [:]
                    subjects += it
                }
            }
            else if (i % (gradeNum+2) == 1) { // 指标项行
                line.findAll { it.toString().length() > 0 && it.toString() != '年级' }.eachWithIndex { it, j ->
                    subjectDims += it //科目下的指标  or 科目下的年级
                }
            } else { //具体的评价项行
                line.findAll {  it.toString().length() > 0 && !it.toString().startsWith("g") }.eachWithIndex{ it, j ->
                    def subjectNo = i.intdiv((gradeNum+2))*4 + j.intdiv(8) //取余对位科目
                    def rate = Float.parseFloat(it.toString())?Float.parseFloat(it.toString()):0
                    def hs = [(subjectDims[subjectNo*8 + j%8]):rate] // 指标 : 指标比例
                    println(subjects[subjectNo]+line[0] +h[subjects[subjectNo]]."${line[0]}")
                    if(subjects[subjectNo]=='生物'){
                        println("debugger")
                    }
                    if(h[subjects[subjectNo]]."${line[0]}" ==null){
                        h[subjects[subjectNo]]."${line[0]}" = [:]
                    }
                    h[subjects[subjectNo]]."${line[0]}" += hs
                }
            }
        }
        h.each { subject->

            subject.value.each { grade->
                op+=(sem+"\t"+grade.key+"\t"+subject.key)
                grade.value.each {item->
                    op+=("\t"+item.key)
                }
                grade.value.each {item->
                    op+=("\t"+item.value)
                }
                op+="\n"
            }
        }
    }
    static void main(String[] args) {

        parse2()
//        //开始解析初中小学部
//        eh.setSheet("评价比例(1-9)")
//        parse(9)
//        eh.setSheet("评价比例(10-12)")
//        parse(3)
////        println(h)
//        TextHelper.printToFile(wordListPath, op)
    }
}
