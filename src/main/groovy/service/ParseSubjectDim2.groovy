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

    static def eh = new Excelhelper('D:\\backup\\FileStorage\\File\\2020-03\\评价项.xlsx')
    static def op = "评价项\n" //标题行
    static void parse2(){
        def table = "reportv11"
        def keyColumn = "t1.subject||type1"
        def valueColumn = "score100"


        def filter = "t1  left join subjects t2 on t1.subject = t2.subject where sem='212'"
        def orderColumn = 'subjectNo'
        def sql = """select DISTINCT "${orderColumn}",${keyColumn} as keyColumn   from ${table} ${filter} order by 1""".toString()
        println(sql)
        def outputSql = " SELECT t1.sem,\n" +
                "    t1.grade,\n" +
                "    t2.cnname,\n" +
                "    t2.enname"
        def comment = ""
        DBHelper.query(sql,
                DBHelper
                .instance.conn)
                .eachWithIndex {it,i ->
                    outputSql+=(""",max(
        CASE
            WHEN ((t1.subject || t1.type1) = '${it.keycolumn}'::text) THEN t1.score100
            ELSE NULL::double precision
        END) AS dim${String.format("%03d",i)}value""")
                    comment+=("""\ncomment on COLUMN bi_${table.split(" ")[0]}.dim${String.format("%03d",i)}value is '${String.format("%03d",i)}${it.keyColumn}';""")
                }
        println(outputSql+"""
FROM (reportv11 t1
     LEFT JOIN va1 t2 ON ((t1.vano = t2.vano)))
  WHERE ((t1.sem = '211'::text) OR (t1.sem = '212'::text))
  GROUP BY t1.sem, t1.grade, t2.cnname, t2.enname;
""")
        println(comment)
        println("""comment on COLUMN bi_reportv11.cnname is '0.姓名';
comment on COLUMN bi_reportv11.enname is '0.英文名';
comment on COLUMN bi_reportv11.grade is '0.班级名';

""")

    }
    static void parse(gradeNum){
        //s1:对于非二维表格，第一个先进行标准化(按块合并为standardTabs)
        eh.read().eachWithIndex { def line, int i ->

        }
        subjects = []
        h = [:]
        subjectDims = []
        //s2:构造结构化的hash对象
        def tab = eh.read()
        tab.eachWithIndex { def line, int i ->
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
        println(op)
    }
    static void main(String[] args) {

//        parse(9)
////        //开始解析初中小学部
        eh.setSheet("评价比例(1-9)")
        parse(9)
//        eh.setSheet("评价比例(10-12)")
//        parse(3)
////        println(h)
        TextHelper.printToFile(wordListPath, op)
    }
}
