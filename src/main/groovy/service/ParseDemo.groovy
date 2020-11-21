package service

import common.Const
import tool.TextHelper
import yjh.helper.Excelhelper

/**
 * Created by Peter.Yang on 2019/6/22.
 * 解析 考察项比例
 */
class ParseDemo {

    static void main(String[] args) {
        //生成1级考察项 91.德文-IB  05.社会-IB
        def op = ""
        new Excelhelper("D:\\迅雷下载\\2019-2020-2\\2019-2020-2\\91.德文-IB.xlsx").read().eachWithIndex { def line, int i ->
            line.each {
                op += "\t"+it
            }
            op += "\n"
        }
        TextHelper.printToFile("a.txt", op)

    }
}
