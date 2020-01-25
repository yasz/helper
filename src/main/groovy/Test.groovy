import tool.DBHelper
import yjh.helper.Excelhelper

import java.text.SimpleDateFormat

/**
 * Created by Peter.Yang on 2019/1/13.
 */
class Test {
    public static void main(String[] args) {

        def db = new DBHelper('192.168.0.149', 'postgres', 'ruianVA123')
        def h = []
        def pool = []
        def sumgrade = 63 //select avg(grade)*14 from gradev;每组288/20= 14人，乘以平均年级 ，大概满足条件为63的14人；

        db.query("select * from gradev where grade>=8 and grade<=9 AND sex = 'M' order by random() ").eachWithIndex {
            it, i ->
                if (i < 20) {
                    h[i] = []
                    h[i] += it
                } else {
                    pool += it
                }
        }

        db.query("select * from gradev where grade>=8 and grade<=9 AND sex = 'F' order by random() ").eachWithIndex {
            it, i ->
                if (i < 20) {
                    h[i] += it
                } else {
                    pool += it
                }
        }
        db.query("select * from gradev where grade<=7   order by random() ").eachWithIndex { it, i ->
            pool += it
        }
        Collections.shuffle(pool)
        pool.eachWithIndex { it, i ->
            int g = i / 12.toInteger()
            h[g] = h[g] ? h[g] : []
            h[g] += it
        }
        h.eachWithIndex {g,i->
            println("""group${i+1}: """)
            g.each {it->
                print("""${it.grade}:${it.cnname}(${it.enname}) """)
            }
            println()
        }

        def a = ""
        a = a ? a : "good"
        println(a)
        System.exit(1)
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyyMMddHHmmss");
//        println(timeFormat.format(Calendar.getInstance().getTime()))

        def eh = new Excelhelper("C:\\Users\\peterjiahao\\Downloads\\浙江省-温州市-瑞安市-瑞安市惟理达书院20190621.xlsx")
        for (def i in 1..eh.getWorkbook().getNumberOfSheets() - 1) {
            eh.setSheet(i)
            eh.read(3).each { row ->
                row.each { it ->
                    print("${it}\t")
                }
                println()
            }
        }


    }

    static Double getNonSubjectScore(def tab, def item) {

    }

}
