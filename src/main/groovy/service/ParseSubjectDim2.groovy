package service

import common.Const
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

    static def eh = new Excelhelper(Const.subjectDimPath)
    static def op = "评价项\n" //标题行

    static void parse(gradeNum){
        subjects = []
        h = [:]
        subjectDims = []
        eh.read().eachWithIndex { def line, int i ->
            if (i % (gradeNum+2) == 0) { //初始化科目
                line.findAll { it.toString().length() > 0 && it.toString() != '科目' }.each {
                    h[it.toString()] = [:]
                    subjects += it
                }
            }
            else if (i % (gradeNum+2) == 1) { //初始化指标项
                line.findAll { it.toString().length() > 0 && it.toString() != '年级' }.eachWithIndex { it, j ->
                    subjectDims += it //科目下的指标  or 科目下的年级
                }
            } else {
                line.findAll {  it.toString().length() > 0 && !it.toString().startsWith("g") }.eachWithIndex{ it, j ->
                    def subjectNo = i.intdiv((gradeNum+2))*4 + j.intdiv(8)
                    def rate = Float.parseFloat(it.toString())?Float.parseFloat(it.toString()):0
                    def hs = [(subjectDims[subjectNo*8 + j%8]):rate] // 指标 : 指标比例
                    if(h[subjects[i.intdiv((gradeNum+2))*4 + j.intdiv(8)]]."${line[0]}" ==null){h[subjects[i.intdiv((gradeNum+2))*4 + j.intdiv(8)]]."${line[0]}" = [:]}
                    h[subjects[i.intdiv((gradeNum+2))*4 + j.intdiv(8)]]."${line[0]}" += hs
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



        //开始解析初中小学部
        eh.setSheet("评价比例(1-9)")
        parse(9)
        eh.setSheet("评价比例(10-12)")
        parse(3)
//        println(h)
        TextHelper.printToFile(wordListPath, op)
    }
}
