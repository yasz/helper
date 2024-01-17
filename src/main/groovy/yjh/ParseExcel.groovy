package yjh


import yjh.helper.Excelhelper
import yjh.helper.StringHelper

import java.text.SimpleDateFormat

class ParseExcel {
    static void main(String[] args) {
        def t = new Excelhelper("C:\\Users\\yangj\\Desktop\\1216\\desktop\\朗文.xlsx")
        0.upto(t.workbook.getNumberOfSheets() - 1) {i->
            t.setSheet(i)
            def grade = t.sheet.getSheetName()
            def data = t.read()
            String chapter = ""
            def chapterFileSeq =0

            data.eachWithIndex{ def row, int count ->
                {
                    if(row[0].toString().length()<3){
                        chapter = row[0];
                        chapterFileSeq = 0;
                        return
                    }

                    println("${row[6].toString().replaceAll(" HTTP/1.1","").replaceAll("GET ","http://vod.eltmax.net")}\t${grade}_${chapter}_${sprintf("%02d", ++chapterFileSeq)}.mp3")
                }
            }

        }
    }

}
