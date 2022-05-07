package service

import com.alibaba.fastjson.JSON
import common.Const
import tool.DBHelper
import tool.DocxHelper
import yjh.helper.Excelhelper

/**
 * Created by Peter.Yang on 2021/1/22.
 */
class ClubReportViewService {
    //导出俱乐部报告单

    static void main(String[] args) {

        def fileList = []
        new Excelhelper("C:\\Users\\yangj\\Downloads\\俱乐部评估表2.xlsx").read().eachWithIndex { row, i ->
            if (i < 3) {
                return
            }
            def paras = [:]
            row.eachWithIndex { def entry, int j ->
                paras["d${j}"] = row[j + 2]
            }
            paras.cnname = row[1]
            paras.enname = row[2]
            def doc = new DocxHelper(Const.tmpClub)
            doc.replace(paras).saveAs("${paras.cnname}.docx")
            fileList += "${paras.cnname}.docx"

        }
        DocxHelper.merge(fileList, "RC.docx")
        DocxHelper.toPDF("RC.docx", "RC.pdf")
    }


}
