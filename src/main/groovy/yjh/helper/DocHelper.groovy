package yjh.helper

import common.Const
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFParagraph
import org.apache.poi.xwpf.usermodel.XWPFRun
import tool.DocxHelper

import java.nio.file.Files
import java.nio.file.Paths
import java.util.regex.Matcher

/**
 * Created by Peter.Yang on 7/4/2017.
 */
class DocHelper {

    /**
     * 用一个docx文档作为模板，然后替换其中的内容，再写入目标文档中。
     * @throws Exception
     */

    //英文 中文 数学 科学 社会 体育 美术 音乐

    static String dataPath = "${System.getProperty("user.dir")}/data"

    public static void main(String[] args) throws IOException {

//        通过标准excel导出标准数据

        def list = new Excelhelper("dat\\template\\标准表格.xlsx").read()
        def ha = StringHelper.array2hash(list[0],list[1..list.size()-1])
        ha.each { it->
            println(it)
            new DocxHelper("dat\\template\\标准化台账--二级标题封面-模板.docx").replace(it).saveAs("${it.filename}.docx")
        }

    }

}