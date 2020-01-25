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
    static def scoreFilePath = "D:\\3.ws\\2.code\\ruianva.github.io\\5.assistant\\vano\\score.txt"
    static def detailSoreFilePath = "D:\\3.ws\\2.code\\ruianva.github.io\\5.assistant\\vano\\detailScore.txt"
    static def commentFilePath = "D:\\3.ws\\2.code\\ruianva.github.io\\5.assistant\\vano\\comment.txt"
    static def commentFilePath2 = "D:\\3.ws\\2.code\\ruianva.github.io\\5.assistant\\vano\\comment2.txt"

    static def ib = "乐于探究\t精于思考\t善于沟通\t勇于尝试\t学识渊博\t重视原则\t关心他人\t开放思维\t均衡不偏\t勤于反思".split("\t")

    static void importIBScore() {
        //生成打分表，按每个学生,科目，pk是(科目,vaNO)
        def op = "品格打分表\n"
        def op2 = "班主任评语\n"
        new File(scoreExcelDir).eachFileMatch(~/.*IB.*\.xlsx$/) { scoreExcelPath ->

            def eh = new Excelhelper(scoreExcelPath.toString())
            0.upto(eh.workbook.size() - 1) {
                eh.setSheet(it)
                def subject = eh.getSheet().getSheetName()
                def t1 = eh.read()
                if (subject == '班主任') {
                    t1.drop(3).each {line->
                        if (line[1]== null ||line[1].toString().length() <= 0) return
                        Double vano = line[1]
                        if(line[16]==null){
                            println(vano)
                        }
                        def comment = line[16]?line[16]:""
//                        if(comment.toString().contains("\t")||comment.toString().contains("\n")){println(comment)}
                        comment=comment.replaceAll("\n"," ").replaceAll("\r"," ").replaceAll("\t"," ").replaceAll("\"","“")
                        op2 += ("${sem}\t${vano.toInteger()}\t${subject}\t${comment}\n")
                    }
                }
                t1.drop(3).each { line ->
                    if (line[1]== null ||line[1].toString().length() <= 0) return
                    Double vano = line[1]
                    ib.eachWithIndex { String ibItem, int i ->
                        if (null == line[6 + i] || line[6 + i].toString().length() <= 0 || line[6 + i].toString().toUpperCase().contains("N"))
                            return
                        op += ("${sem}\t${vano.toInteger()}\t${subject}\t${ibItem}\t${line[6 + i]}\n")
                    }
                }
            }
        }
        TextHelper.printToFile(detailSoreFilePath, op)
        TextHelper.printToFile(commentFilePath2, op2)
    }

    static void importScore() {
        //生成打分表，按每个学生,科目，pk是(科目,vaNO)
        def op = "打分表\n"
        def op2 = "班主任评语\n"
        new File(scoreExcelDir).eachFileMatch(~/^\d\d\..*\.xlsx$/) { scoreExcelPath ->
            def subject = scoreExcelPath.getName()
            subject = subject[3..<subject.lastIndexOf('.')]
            def t1 = new Excelhelper(scoreExcelPath.toString()).read()
            t1.drop(2).each { line ->
                if (line[1]== null ||line[1].toString().length() <= 0) return
                Double vano = line[1]
                op += ("${sem}\t${vano.toInteger()}\t${subject}")
                0.upto(6) { i ->
                    if (null == line[6 + i]) {
                        op += ("\t")
                    } else {
                        String type = line[6 + i].class
//                        println(type)
                        op += ("\t")
                        if (type.contains("Double")) {
                            op += ("${line[6 + i]}")
                        } else if (type.contains("String")) {
                            if(line[6 + i].toString().toUpperCase().contains("N") ||line[6 + i].toString()
                                    .toUpperCase().contains("待") || line[6 + i].toString().length()
                                    <= 0 ){  }
                            else{
                                println(vano+subject)
                                String score= line[6 + i].toString()
                                op +=("${Double.parseDouble(score.trim())}")
                            }
                        }
                    }
                }
                op += ("\t\n")
                if(line[13]==null){
                    if(vano.toString().startsWith("10")){return}//高中部科目跳过
                    if(vano.toString().startsWith("10")){return}//高中部科目跳过
//                    println(vano+subject)
                    return
                }
                def comment = line[13]?line[13]:""

                comment=comment.replaceAll("\n"," ").replaceAll("\r"," ").replaceAll("\t"," ").replaceAll("\"","“")
                op2 += ("${sem}\t${vano.toInteger()}\t${subject}\t${comment}\n")

            }
        }

        TextHelper.printToFile(scoreFilePath, op)
        TextHelper.printToFile(commentFilePath, op2)


    }

    static void main(String[] args) {
        importIBScore()
        importScore()


//        importDetailScore()
        println("记得执行2级汇总、汇总sql")
//        importComment()

    }

}
