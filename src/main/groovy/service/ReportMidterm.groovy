package service

import tool.DocxHelper
import tool.TextHelper
import yjh.helper.Excelhelper

/**
 * Created by Peter.Yang on 2019/6/22.
 * 解析 考察项比例
 */
class ReportMidterm {

    static void main(String[] args) {

        def tmpPath = "C:\\Users\\peterjiahao\\Desktop\\姓名.docx"
        def paras = [:]
        def op = ""
        def tab = new Excelhelper("C:\\Users\\peterjiahao\\Desktop\\tmplate.xlsx").setSheet(1).read()
        def subjects = tab[2]
         tab.eachWithIndex { def line, int i ->

            def outputName = line[6]
            if (i > 253 && i < 280) {
                line.eachWithIndex { def unit, int j ->
                    if (j<7) return
                    if (unit.getClass().toString().contains("Double")) {
                        print()
                        print(unit)
                        paras["subject${}"] = subjects[j]
                        paras["subject${}"] = subjects[j]
                        paras["t${}"] = subjects[j]

                    }
                }
//                op += "\n"
                println()
                new DocxHelper(tmpPath).replace(paras).saveAs(outputName.toString() + ".docx")
            }
        }
//        TextHelper.printToFile("a.txt", op)

    }
}
