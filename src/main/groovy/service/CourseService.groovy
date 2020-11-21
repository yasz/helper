package service

import common.Const
import tool.DBHelper
import tool.DocxHelper
import yjh.helper.Excelhelper

/**
 * Created by Peter.Yang on 2019/6/22.
 */
class CourseService {
    //导出报告单

    static void main(String[] args) {
        def eh = new Excelhelper("D:\\veritas坚果云\\2019-2020总课程表-横版-20200408_164.xlsx")
//        def tab = eh.read(0,26)
//        tab.forEach{ it ->
//                println(it)
//        }
//        def grades = []
        def grade = eh.read(1, 1, 1, 12)
        def d1 = eh.read(2, 25, 1, 12)
        d1.eachWithIndex { row, i ->
            if(i%3 ==0 ){
                row.eachWithIndex {ele,j->
                    println("""insert into "课程表" values('${grade[0][j]}', '${row[j]}', '${d1[i+1][j]}',1,${(i/3)+1});""")
                }
            }
        }


        d1 = eh.read(2, 25, 1+13, 12+13)
        d1.eachWithIndex { row, i ->
            if(i%3 ==0 ){
                row.eachWithIndex {ele,j->
                    println("""insert into "课程表" values('${grade[0][j]}', '${row[j]}', '${d1[i+1][j]}',2,${(i/3)+1});""")
                }
            }
        }

        d1 = eh.read(2, 25, 1+26, 12+26)
        d1.eachWithIndex { row, i ->
            if(i%3 ==0 ){
                row.eachWithIndex {ele,j->
                    println("""insert into "课程表" values('${grade[0][j]}', '${row[j]}', '${d1[i+1][j]}',3,${(i/3)+1});""")
                }
            }
        }

        d1 = eh.read(2, 25, 1+39, 12+39)
        d1.eachWithIndex { row, i ->
            if(i%3 ==0 ){
                row.eachWithIndex {ele,j->
                    println("""insert into "课程表" values('${grade[0][j]}', '${row[j]}', '${d1[i+1][j]}',4,${(i/3)+1});""")
                }
            }
        }
        d1 = eh.read(2, 25, 1+52, 12+52)
        d1.eachWithIndex { row, i ->
            if(i%3 ==0 ){
                row.eachWithIndex {ele,j->
                    println("""insert into "课程表" values('${grade[0][j]}', '${row[j]}', '${d1[i+1][j]}',5,${(i/3)+1});""")
                }
            }
        }

    }


}
