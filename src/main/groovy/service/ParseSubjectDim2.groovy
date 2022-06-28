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
    static def wordListPath = "subjectDim2.txt"

    static def subjects = []
    static def h = [:]
    static def subjectDims = []

    static def eh = new Excelhelper('C:\\Users\\yangj\\Downloads\\评价项.xlsx')
    static def op = "评价项\n" //标题行
    static void parse2() {
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
                .eachWithIndex { it, i ->
                    outputSql += (""",max(
        CASE
            WHEN ((t1.subject || t1.type1) = '${it.keycolumn}'::text) THEN t1.score100
            ELSE NULL::double precision
        END) AS dim${String.format("%03d", i)}value""")
                    comment += ("""\ncomment on COLUMN bi_${table.split(" ")[0]}.dim${String.format("%03d", i)}value is '${String.format("%03d", i)}${it.keyColumn}';""")
                }
        println(outputSql + """
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
    static void parse22(gradeNum) {
        def tab = eh.setSheet("年度评价比例(1-9)").read()
        def h = [:]
        for (int i = 0; i < tab.size(); i += (gradeNum + 2)) {
            List subjectRow = tab[i]
            for (j in 0..(subjectRow.size() / 4) - 1) {
                def subject = tab[i][j*4 + 1]
                def list = eh.read(i+1,i+gradeNum+1,j*4+1,j*4+4)
                println(subject)
                println(list)
                h[subject]=list
//                System.exit(1)
            }
        }
        h.eachWithIndex { def entry, int i ->
            def subject = entry.key
            def list = entry.value

            def mapping = "90\t80\t70\t60\t40\t20\t0"
            list.eachWithIndex { def e, int j ->
                if (j == 0) {
                    return
                }
                op+=("${Integer.parseInt(Const.sem)-1}\t${subject}\t${j}\t\tsus\t${e[0]}\t${mapping}\t1\n")
                op+=("${Integer.parseInt(Const.sem)-1}\t${subject}\t${j}\t\tsum\t${e[1]}\t${mapping}\t2\n")
                op+=("${Const.sem}\t${subject}\t${j}\t\tsus\t${e[2]}\t${mapping}\t3\n")
                op+=("${Const.sem}\t${subject}\t${j}\t\tsum\t${e[3]}\t${mapping}\t4\n")


            }
        }
        println("***************")
        println(op)
        println("***************")
    }

    static void parse221(gradeNum) {
        //s1:对于非标准二维表格，預檢查先以统一大小合并各个科目方块，主要每行大小為33行、25行
        def tab = eh.read()
        def h = [:]
        if (tab.size() % (gradeNum + 2) != 0) {
            throw new Exception("【错误】：表格行数${tab.size()}不是${gradeNum + 2}的倍数，请检查")
        }
        tab.eachWithIndex { List entry, int i ->
            if ((entry.size() - 1) % 8 != 0) {
                throw new Exception("【错误】：表格第${i + 1}行的列数${entry.size()}-1不是8(评价项)的倍数")
            }
        }
        println("检测完毕，开始处理大卸八塊 进行装填")
        for (int i = 0; i < tab.size(); i += (gradeNum + 2)) {
            List subjectRow = tab[i]
            for (j in 0..(subjectRow.size() / 8) - 1) {
                def subject = tab[i][j*8 + 1]
                def list = eh.read(i+1,i+gradeNum,j*8+1,j*8+8)
                println(subject)
                println(list)
                h[subject]=list
//                System.exit(1)

            }
        }
        println("s1:拆卸完毕开始标准化输出")
        h.eachWithIndex{ def entry, int i ->
            def subject = entry.key
            def list = entry.value
            def dimrow = list[0]
            def mapping = "90\t80\t70\t60\t40\t20\t0"
            list.eachWithIndex{ def e, int j ->
                if(j==0){return}
                if(gradeNum==3){j=j+9} //G10 G11 G12
                op +=("${Const.sem}\t${subject}\t${j}\tsus\t${dimrow[0]}\t${e[0]}\t${mapping}\t1\n")
                op +=("${Const.sem}\t${subject}\t${j}\tsus\t${dimrow[1]}\t${e[1]}\t${mapping}\t2\n")
                op +=("${Const.sem}\t${subject}\t${j}\tsus\t${dimrow[2]}\t${e[2]}\t${mapping}\t3\n")
                op +=("${Const.sem}\t${subject}\t${j}\tsus\t${dimrow[3]}\t${e[3]}\t${mapping}\t4\n")
                op +=("${Const.sem}\t${subject}\t${j}\tsus\t${dimrow[4]}\t${e[4]}\t${mapping}\t5\n")
                op +=("${Const.sem}\t${subject}\t${j}\tsum\t${dimrow[5]}\t${e[5]}\t${mapping}\t6\n")
                op +=("${Const.sem}\t${subject}\t${j}\tsum\t${dimrow[6]}\t${e[6]}\t${mapping}\t7\n")
                op +=("${Const.sem}\t${subject}\t${j}\tsum\t${dimrow[7]}\t${e[7]}\t${mapping}\t8\n")
            }
        }
        println(op)

    }
    static void parse(gradeNum) {

        subjects = []
        h = [:] //最終
        subjectDims = []
        def tab = eh.read()
        eh.read().eachWithIndex { def line, int i ->
            println("line${i}:" + line)
            if (i == 34) {
                print('halt')
            }
            if (i % (gradeNum + 2) == 0) {  //要求每个单元格都有[9+2]行,这儿是首行
                line.findAll { it.toString().length() > 0 && it.toString() != '科目' }.each {
                    h[it.toString()] = [:]
                    subjects += it
                }
            } else if (i % (gradeNum + 2) == 1) { // 指标项行
                def ll = line.findAll { it.toString().length() > 0 && it.toString() != '年级' }
                ll.eachWithIndex { it, j ->
                    subjectDims += it //科目下的指标  or 科目下的年级
                }
            } else { //具体的评价项行
                line.findAll { it.toString().length() > 0 && !it.toString().startsWith("g") }.eachWithIndex { it, j ->
                    def subjectNo = i.intdiv((gradeNum + 2)) * 4 + j.intdiv(8) //取余对位科目
                    def rate = Float.parseFloat(it.toString()) ? Float.parseFloat(it.toString()) : 0
                    def hs = [(subjectDims[subjectNo * 8 + j % 8]): rate] // 指标 : 指标比例
                    if (h[subjects[subjectNo]]."${line[0]}" == null) {
                        h[subjects[subjectNo]]."${line[0]}" = [:]
                    }
                    h[subjects[subjectNo]]."${line[0]}" += hs
                }
            }
            print("parse over")
        }
        h.each { subject ->

            subject.value.each { grade ->
                op += (sem + "\t" + grade.key + "\t" + subject.key)
                grade.value.each { item ->
                    op += ("\t" + item.key)
                }
                grade.value.each { item ->
                    op += ("\t" + item.value)
                }
                op += "\n"
            }
        }
        println(op)
    }

    static void main(String[] args) {

//        parse(9)
////        //开始解析初中小学部
        eh.setSheet("评价比例(1-9)")
//        parse221(9)
        parse22(9)

//        eh.setSheet("评价比例(10-12)")
//        parse221(3)
////        println(h)
        TextHelper.printToFile(wordListPath, op)
    }
}
