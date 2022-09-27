package service

import com.alibaba.fastjson.JSON
import common.Const
import tool.DBHelper
import tool.DocxHelper

import java.sql.Connection
import java.sql.DriverManager

/**
 * Created by Peter.Yang on 2019/6/22.
 */
class N2Service {

    static void exportVocabulary(){
        //词汇表
        def unit = "'1A01','1A02'"
        def sqlStr = """SELECT japanese,chinese FROM "n1_vocabulary" where unit in ($unit) and type1 <> '*' limit 50""".toString()
        Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "ruianVA123")
        def data = DBHelper.query(sqlStr, conn)
        def time = (int) (data.size()*20/60)
        def q1 = ""
        def a1 = ""

        def list = (0..data.size()-1).toList()
        while(list.size() > 0) {
            def randomNumber = (int) (Math.random() * list.size())
            def num = list.removeAt(randomNumber)
            q1+=data[num].chinese+"\n"
            a1+=data[num].japanese+"\n"
        }
        def paras = ["q1": newlineToBreakHack(q1), "a1": newlineToBreakHack(a1),"type":unit,"time":time]
        def doc = new DocxHelper("""C:\\Users\\yangj\\Documents\\单词默写.docx""")
        doc.replace(paras).saveAs("单词默写${unit}.docx")
//        DocxHelper.toPDF("五十音图测试${type}.docx", "五十音图测试${type}.pdf")
//        println(time)
    }
    static void main(String[] args) {
        exportVocabulary()
    }
    static void gojiu() {
        //ごじゅうおんず
        def type = """'浊音','半浊音','拗音','浊拗音'"""
        def sqlStr = "SELECT no,hiragana,katakana,roman,type1 FROM n1 where type1 in (${type}) and enable is not false".toString()
        Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "ruianVA123")
        def data = DBHelper.query(sqlStr, conn)
        //,'拗音','浊拗音'
        println(data)
        def q1 = ""
        def a1 = ""
        1.upto(300) { it ->
            def randomNumber = (int) (Math.random() * data.size())
            q1 += data[randomNumber].roman

            if(data[randomNumber].roman.length()==1){
                println(data[randomNumber].roman.length())
                q1 += "  "
            }else{
                q1 += " "
            }
            a1 += data[randomNumber].hiragana+ " "
            if (it % 20 == 0) {
                q1 += "\n"
                a1 += "\n"
            }
        }
        def doc = new DocxHelper("""C:\\Users\\yangj\\Documents\\五十音图测试.docx""")
        def paras = ["q1": newlineToBreakHack(q1), "a1": newlineToBreakHack(a1),"type":type]
        doc.replace(paras).saveAs("五十音图测试${type}.docx")
        DocxHelper.toPDF("五十音图测试${type}.docx", "五十音图测试${type}.pdf")

    }
    static String newlineToBreakHack(String r) {

        StringTokenizer st = new StringTokenizer(r, "\t\n\r\f");
        // tokenize on the newline character, the carriage-return character, and the form-feed character
        StringBuilder sb = new StringBuilder()

        boolean firsttoken = true;
        while (st.hasMoreTokens()) {
            String line = (String) st.nextToken();
            if (firsttoken) {
                firsttoken = false;
            } else {
                sb.append("</w:t><w:br/><w:t>")
            }
            sb.append(line);
        }
        return sb.toString();
    }

}
