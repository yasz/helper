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

        def tmpPath = "C:\\Users\\peterjiahao\\Desktop\\姓名9v.docx"
        def paras = [:]
        def op = ""
        def tab = new Excelhelper("C:\\Users\\peterjiahao\\Desktop\\tmplate.xlsx").setSheet(1).read()
        def tab2 = new Excelhelper("C:\\Users\\peterjiahao\\Desktop\\tmplate.xlsx").setSheet(2).read()

        def subjects = tab[2]
        tab.eachWithIndex { def line, int i ->
            paras = [:]
            def outputName = line[1] + "_" + line[2]
            if (i >= 280 && i < 302) {
                paras["cnname"] = line[2]
                int count = 0
                line.eachWithIndex { def unit, int j ->
                    if (j < 7) return
                    def countS = 0
                    if (unit.getClass().toString().contains("String")) {
                        if(unit.toString().length()<1){return}

                        unit = Double.parseDouble(unit.toString().replaceAll("[\\xC2\\xA0]",""))
                    }
                    if (unit.getClass().toString().contains("Double")) {
                        countS = String.format("%02d", ++count)
                        if (unit > 7) {
                            unit = tool.CalHelper.vascore2(unit)
                        } else {
                            unit = tool.CalHelper.vascore(unit)
                        }
                        paras["sub${countS}"] = subjects[j]
                        paras["t${countS}"] = unit
                    }
                    unit = tab2[i][j]
                    if (unit.getClass().toString().contains("Double")) {
//                        def countS = String.format("%02d", ++count)
                        if (unit > 7) {
                            unit = tool.CalHelper.vascore2(unit)
                        } else {
                            unit = tool.CalHelper.vascore(unit)
                        }

                        paras["s${countS}"] = unit
                    }
                }
                println(paras)
                new DocxHelper(tmpPath).replace(paras).saveAs(outputName.toString() + ".docx")
            }
        }
    }

}
