package service

import common.Const
import tool.TextHelper
import yjh.helper.Excelhelper

/**
 * Created by Peter.Yang on 2019/6/22.
 * 解析 考察项比例
 */
class ParseSubjectDim1 {
    static void parse() {

    }
    static def sem = Const.sem
    static def wordListPath = "D:\\3.ws\\2.code\\ruianva.github.io\\5.assistant\\vano\\subjectDim.txt"

    public static void main(String[] args) {
        //生成1级考察项
        def subjects = []
        def qz = []
        def subjectDim1 = []
        def className = ''
        def eh = new Excelhelper(Const.vocabularyPath)
        eh.setSheet(Const.sem)
        def op = "词汇表\n"
        eh.read().eachWithIndex { def line, int i ->
            if (i == 0) {
                subjects = line.findAll { it.toString().length() > 0 }
                return
            }
            if (i == 1) {
                return
            } else {

            }

            if (line[0].toString().contains('权重')) {
                subjects.eachWithIndex { def subject, int subjectIndex ->
                    qz += [line[ (8 * subjectIndex) + 1..8 * (subjectIndex + 1)].findAll { it.toString().length() > 0 }
                                   .collect { Float.parseFloat(it.toString()) < 1 ? it * 100 : it }]
                }
            } else if (line[0].toString().length() > 0) {
                className = line[0]
                subjects.eachWithIndex { def subject, int subjectIndex ->
                    subjectDim1 += ([line[(8 * subjectIndex) + 1 ..8 *(subjectIndex + 1)].findAll {
                        it.toString().length() > 0
                    }])
                }

                subjects.eachWithIndex { def subject, int subjectIndex ->
                    op+=("${sem}\t${className}\t${subject}\t")
                    0.upto(7) { op+=("${subjectDim1[subjectIndex][it] ? subjectDim1[subjectIndex][it] : ''}\t") }
                    0.upto(7) { op+=("${qz[subjectIndex][it] ? qz[subjectIndex][it] : ''}\t") }
                    op+="\n"
                }
                qz = []
                subjectDim1 = []
            }
        }
        TextHelper.printToFile(wordListPath,op)
    }
}
