package service

import common.Const
import tool.TextHelper
import yjh.helper.Excelhelper

/**
 * Created by Peter.Yang on 2019/6/22.
 */
class Report2Service {
    static def scoreExcelPath = Const.scoreExcelPath
    static def sem = Const.sem
    static def scoreFilePath = "D:\\3.ws\\2.code\\ruianva.github.io\\5.assistant\\vano\\score.txt"
    static def detailSoreFilePath = "D:\\3.ws\\2.code\\ruianva.github.io\\5.assistant\\vano\\detailScore.txt"
    static def commentFilePath = "D:\\3.ws\\2.code\\ruianva.github.io\\5.assistant\\vano\\comment.txt"


    static void importDetailScore() {
        //生成打分表，按每个学生,科目，pk是(科目,vaNO)
        def eh = new Excelhelper(scoreExcelPath)
        eh.setSheet("2018-2019-2学期明细打分表")
        def t1 = eh.read()
        def subjectsDims = t1[0].findAll { it.toString().length() > 0 }
        def op = "明细打分表\n"
        t1.drop(2).eachWithIndex { def line, int i ->
            // 每个科目最多12项
            if (line[0].toString().length() < 1) {
                return
            }
            def vaID = (int) line[1]
            def cnName = line[0]
            def enName = line[2]
            def score = []
            def hash = [:]
            subjectsDims.eachWithIndex { def subjectsDim, int subjectIndex ->
                def (subject, dim) = subjectsDim.split("-")
                hash[subject] = hash[subject] ? (hash[subject] + 1) : 1
                op += ("${sem}\t${vaID}\t${subject}\t${hash[subject]}\t")
                score = line[(subjectIndex * 12 + 3)..(subjectIndex * 12 + 10 + 3)].findAll {
                    it.toString().length() > 0 //左移
                }

                def average = ""
                if (score.size() > 0) {
                    average = Report0Service.cal(String.valueOf(score.sum() / score.size()))
                }
                0.upto(11) {
                    if (null == score[it]) {
                        score[it] = ""
                    }
                }
                op += (score.join("\t"))
                op += "\t${average}\n"
            }
        }
        TextHelper.printToFile(detailSoreFilePath, op)
//        FileManager.write(op,detailSoreFilePath,"GBK")
    }

    static void importScore() {
        //生成打分表，按每个学生,科目，pk是(科目,vaNO)
        def eh = new Excelhelper(scoreExcelPath)
        eh.setSheet("2018-2019-2学期打分表")
        def t1 = eh.read()
        def subjectsDims = t1[0].findAll { it.toString().length() > 0 }
        def op = "打分表\n"
        t1.drop(2).eachWithIndex { def line, int i ->
            // 每个科目最多12项
            if (line[0].toString().length() < 1) {
                return
            }
            def vaID = (int) line[1]
            def cnName = line[0]
            def enName = line[2]
            def score = []
            subjectsDims.eachWithIndex { def subject, int subjectIndex ->
                op += ("${sem}\t${vaID}\t${subject}\t")


                score = line[(subjectIndex * 12 + 3)..(subjectIndex * 12 + 7 + 3)].findAll {
                    it.toString().length() > 0 //左移
                }
                0.upto(7) {
                    if (null == score[it]) {
                        score[it] = ""
                    }
                }
                op += (score.join("\t"))
                op += "\n"
            }
        }
        TextHelper.printToFile(scoreFilePath, op)
    }

    static void importComment() {
        // 导入评语
        def eh = new Excelhelper(scoreExcelPath)
        eh.setSheet("2018-2019-2评语")
        def t1 = eh.read()
        def subjectsDims = t1[0].drop(3).findAll { it.toString().length() > 0 }
        def op = "打分表\n"
        t1.drop(1).eachWithIndex { def line, int i ->
            if(line[1].toString().size()<1){return}
            def vaID = (int) line[1]
            def cnName = line[0]
            def enName = line[2]
            def score = []
            subjectsDims.eachWithIndex { def subject, int subjectIndex ->
                op += ("${sem}\t${vaID}\t${subject}\t")
                op += line[subjectIndex+3].toString().replaceAll("\n"," ").replaceAll("\r"," ").replaceAll("\t"," ")
                op += "\n"
            }
        }
        TextHelper.printToFile(commentFilePath, op)
    }

    static void main(String[] args) {
        importScore()
        importDetailScore()
        println("记得执行2级汇总、汇总sql")
        importComment()

    }
}
