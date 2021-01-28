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
    //导出报告单

    static void main(String[] args) {

        new Excelhelper("C:\\Users\\peterjiahao\\Desktop\\俱乐部报告单211.xlsx").read().each{ row ->
            if(row[0]==' '){return}
            def paras = [:]
            row.eachWithIndex{ def entry, int i ->
                paras["d${i}"]= row[i]
            }
            paras.cnname= row[0]
            def doc = new DocxHelper("D:\\3.ws\\1.idea\\helper\\dat\\tmp\\tmpClub.docx")
            doc.replace(paras).saveAs("${paras.cnname}.docx")
        }

    }


}
