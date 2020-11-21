package service

import common.Const
import tool.TextHelper
import yjh.helper.Excelhelper

/**
 * Created by Peter.Yang on 2019/6/22.
 */
class ParseScore {

    static def scoreExcelDir = Const.scoreExcelDir
    static def sem = Const.sem
//    static def scoreFilePath = "D:\\3.ws\\2.code\\ruianva.github.io\\5.assistant\\vano\\${sem}\\score.txt"
//    static def detailSoreFilePath = "D:\\3.ws\\2.code\\ruianva.github.io\\5.assistant\\vano\\${sem}\\detailScore.txt"
//    static def commentFilePath = "D:\\3.ws\\2.code\\ruianva.github.io\\5.assistant\\vano\\${sem}\\comment.txt"

    static def scoreFilePath = "${scoreExcelDir}\\score.txt"
    static def IBSoreFilePath = "${scoreExcelDir}\\IBScore.txt"
    static def commentFilePath = "${scoreExcelDir}\\comment.txt"

//    static def commentFilePath2 = "D:\\3.ws\\2.code\\ruianva.github.io\\5.assistant\\vano\\${sem}\\comment2.txt"

    static def ib = "乐于探究\t精于思考\t善于沟通\t勇于尝试\t学识渊博\t重视原则\t关心他人\t开放思维\t均衡不偏\t勤于反思".split("\t")

    static void importIBScore() {
        //生成打分表，按每个学生,科目，pk是(科目,vaNO)
        def op = "品格打分表\n"
        def op2 = "评语\n"
        new File(scoreExcelDir).eachFileMatch(~/^\d.*IB.*\.xlsx$/) { scoreExcelPath ->
            def eh = new Excelhelper(scoreExcelPath.toString())
            0.upto(eh.workbook.size() - 1) {
                eh.setSheet(it)
                println(scoreExcelPath.getName())
                def subject = (scoreExcelPath.getName() =~ /\.(.*)\-IB/)[0][1]
                def t1 = eh.read()
                t1.drop(3).each { line ->
                    if (line[1] == null || line[1].toString().length() <= 0) return
                    Double vano = line[1]
                    ib.eachWithIndex { String ibItem, int i ->
                        if (null == line[5 + i] || line[5 + i].toString().length() <= 0 || line[5 + i].toString()
                                .toUpperCase().contains("N"))
                            return
                        op += ("${sem}\t${vano.toInteger()}\t${subject}\t${ibItem}\t${line[5 + i]}\n")
                    }
                    def comment = line[15] ? line[15] : ""
//                    comment = comment
                    comment = comment.toString().replaceAll("\n", " ").replaceAll("\r", " ").replaceAll("\t", " ").replaceAll("\"", "“")

                    op2 += ("${sem}\t${vano.toInteger()}\t${subject}\t${comment}\t2020-06-30 16:00:00\tps\n")
                }
            }
        }

        TextHelper.printToFile(IBSoreFilePath, op)
        TextHelper.printToFile(commentFilePath, op2)
    }

    static void importScore() {
        //生成打分表，按每个学生,科目，pk是(科目,vaNO)
        def op = "打分表\n"

        new File(scoreExcelDir).eachFileMatch(~/^\d\d\..*\.xlsx$/) { scoreExcelPath ->
            if (scoreExcelPath.getName() ==~ /.*IB.*\.xlsx/) {
                return
            }
            def subject = scoreExcelPath.getName()
            subject = subject[3..<subject.lastIndexOf('.')]
            println(scoreExcelPath.toString())
            def t1 = new Excelhelper(scoreExcelPath.toString()).read()
            t1.drop(2).each { line ->
                if (line[1] == null || line[1].toString().length() <= 0) return
                Double vano = line[1]
                op += ("${sem}\t${vano.toInteger()}\t${subject}")
                0.upto(6) { i -> //共7项
                    if (null == line[5 + i]) {
                        op += ("\t")
                    } else {
                        String type = line[5 + i].class
//                        println(type)
                        op += ("\t")
                        if (type.contains("Double")) {
                            op += ("${line[5 + i]}")
                        } else if (type.contains("String")) {
                            String scoreStr = line[5 + i].toString()
                            if (line[5 + i].toString().toUpperCase().contains("不适用") || line[5 + i].toString().toUpperCase
                            ().contains("N") || line[5 + i].toString()
                                    .toUpperCase().contains("待") || line[5 + i].toString().length()
                                    <= 0) {
                            } else if (scoreStr.trim().length() <= 0) {
                            } else {
                                println(vano + subject)
                                String score = line[5 + i].toString()
                                op += ("${Double.parseDouble(score.trim().replaceAll("[\\xC2\\xA0]", ""))}")
                            }
                        }
                    }
                }
                op += ("\t\n")
            }
        }

        TextHelper.printToFile(scoreFilePath, op)


    }

    static void main(String[] args) {
        importIBScore()
        importScore()
        println("记得执行2级汇总、汇总sql")


    }

}
