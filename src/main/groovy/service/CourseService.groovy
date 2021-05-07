package service

import common.Const
import tool.DBHelper
import tool.DocxHelper
import tool.TextHelper
import yjh.helper.Excelhelper
import yjh.helper.StringHelper

/**
 * Created by Peter.Yang on 2019/6/22.
 */
class CourseService {


    static void merge() {
//        合并学科组各人课表，以便安排会议时间
        //1.解析各人课表，按照[小明,小红]格式对课表进行合并
        def eh = new Excelhelper("D:\\3.ws\\2.code\\ruianva.github.io\\5.assistant\\排课\\212\\212-0228-教师课表(以学科分类)-1页1人.xls")
        def t = [:]
        0.upto(eh.workbook.size() - 1) {
            def table = eh.setSheet(it).read()
            if (table.size() == 0) {
                return
            }
            0.upto((table.size()) / 18 - 1) { y ->
                def tlist = table[y * 18..y * 18 + 17]
                if (t.metaClass.hasProperty(tlist[0][0].toString())) {
                    return
                }//已经存在的老师
                def course = [[], [], [], [], [], [], [], []]
                0.upto(7) { cod ->
                    int cur = cod >= 4 ? 3 : 2
                    course[cod][0] = tlist[cod + cur][0 + 1] //第一节课0
                    course[cod][1] = tlist[cod + cur][1 + 1]
                    course[cod][2] = tlist[cod + cur][2 + 1]
                    course[cod][3] = tlist[cod + cur][3 + 1]
                    course[cod][4] = tlist[cod + cur][4 + 1]
                }
                t[tlist[0][0]] = course

            }
        }
        //构造完了，开始合并各人时间表
        def glist = [:]
//        println(t)
//        """语文与文学\t刘婷婷,张仕辉,徐灵瑜,罗长,葛奕,谢倩倩
//数学与逻辑\t徐建利,邵辰辰,陈子豪,胡灵康,陈荣益
//英语\tMichael,伊丽莎白,戴慧君,朱珍敏,朱珍蝶,薛上容,陈锦翔,黄明东
//科学与科技\t朱志光,胡灵康,李光晶,杨家昊,陈敏坚,叶成
//社会科学\tMichael,谢旭善,陈蒙蒙,陈锦翔,朱珍敏
//行政\t,杨家昊,邱啸,戴慧君,朱志光,陈蒙蒙,陈敏坚,谢旭善,欧泽霖
//音体美\t潘子颖,陈荣益,吴晓勇,吴煦,姜仁爱,谢艳芳
//"""
        """班主任\t蔡文极,陈蒙蒙,胡灵康,刘婷婷,谢倩倩,薛上容,徐灵瑜,叶茜茜,张仕辉,朱珍蝶,朱珍敏,朱志光
新老师\t陈子豪,陈锦翔,黄明东,叶成
""".split("\n").each { it ->
            def group = it.split("\t")[0] //按组合并
            it.split("\t")[1].split(",").each { tt ->
                if (glist[group] == null) {
                    glist[group] = [:]
                }
                glist[group][tt] = t[tt]
            }
        }
        def o = ""
        glist.each { groupname, group -> //然后按 组名合并
            def tab = [[], [], [], [], [], [], [], []] //8*4
            def tnames=""
            group.each { tname, ttab ->
//                if()
                tnames+=(tname+",")
                ttab.eachWithIndex { m, x ->
                    m.eachWithIndex { mm, y ->
                        if (tab[x][y] == null) {
                            tab[x][y] = ""
                        }
                        if (mm != "") {
                            tab[x][y] += ("${tname}:${mm.replace("\n"," ")}\n")
                        }
                    }
                }
            }
            o += ("~~~~~start of ${groupname}:${tnames}~~~~~\nMon\tTue\tWed\tThu\tFri\n")
            o += (StringHelper.printLL(tab, "\t"))
            o += ("\n\n")

        }
        TextHelper.printToFile("a.txt", o)
    }

    static void main(String[] args) {
        merge()
    }

    static void exportmeta(String[] args) {
        def eh = new Excelhelper("D:\\veritas坚果云\\2019-2020总课程表-横版-20200408_164.xlsx")
//        def tab = eh.read(0,26)
//        tab.forEach{ it ->
//                println(it)
//        }
//        def grades = []
        def grade = eh.read(1, 1, 1, 12)
        def d1 = eh.read(2, 25, 1, 12)
        d1.eachWithIndex { row, i ->
            if (i % 3 == 0) {
                row.eachWithIndex { ele, j ->
                    println("""insert into "课程表" values('${grade[0][j]}', '${row[j]}', '${d1[i + 1][j]}',1,${(i / 3) + 1});""")
                }
            }
        }


        d1 = eh.read(2, 25, 1 + 13, 12 + 13)
        d1.eachWithIndex { row, i ->
            if (i % 3 == 0) {
                row.eachWithIndex { ele, j ->
                    println("""insert into "课程表" values('${grade[0][j]}', '${row[j]}', '${d1[i + 1][j]}',2,${(i / 3) + 1});""")
                }
            }
        }

        d1 = eh.read(2, 25, 1 + 26, 12 + 26)
        d1.eachWithIndex { row, i ->
            if (i % 3 == 0) {
                row.eachWithIndex { ele, j ->
                    println("""insert into "课程表" values('${grade[0][j]}', '${row[j]}', '${d1[i + 1][j]}',3,${(i / 3) + 1});""")
                }
            }
        }

        d1 = eh.read(2, 25, 1 + 39, 12 + 39)
        d1.eachWithIndex { row, i ->
            if (i % 3 == 0) {
                row.eachWithIndex { ele, j ->
                    println("""insert into "课程表" values('${grade[0][j]}', '${row[j]}', '${d1[i + 1][j]}',4,${(i / 3) + 1});""")
                }
            }
        }
        d1 = eh.read(2, 25, 1 + 52, 12 + 52)
        d1.eachWithIndex { row, i ->
            if (i % 3 == 0) {
                row.eachWithIndex { ele, j ->
                    println("""insert into "课程表" values('${grade[0][j]}', '${row[j]}', '${d1[i + 1][j]}',5,${(i / 3) + 1});""")
                }
            }
        }

    }


}
