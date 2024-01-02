package service

import com.alibaba.fastjson.JSON
import common.AsposeRegister
import common.Const

import tool.DocxHelper
import utils.DBHelper

import java.sql.Connection
import java.sql.DriverManager
import java.text.SimpleDateFormat

/**
 * Created by Peter.Yang on 2019/6/22.
 */
class N2Service {

    static void exportVocabulary(String[] ids,String outputFileName){
        //词汇表
        def unit = ids.join(",")
        def sqlStr = """SELECT japanese,chinese FROM "JapaneseVocabulary" where unit_id in ($unit) and type1 <> '*' limit 55""".toString()
        println(sqlStr)
        def dbhelper = new DBHelper("sqlite1")
        def data = dbhelper.query(sqlStr)
        def time = (int) (data.size()*20/60)
        def q1 = ""
        def a1 = ""
        def b1 = ""
        def list = (0..data.size()-1).toList()
        while(list.size() > 0) {
            def randomNumber = (int) (Math.random() * list.size())
            def num = list.removeAt(randomNumber)
            q1+=data[num].chinese+"\n"
            a1+=data[num].japanese+"\n"
            b1+="_______________________\n"
        }
        def paras = ["b1":newlineToBreakHack(b1),"q1": newlineToBreakHack(q1), "a1": newlineToBreakHack(a1),"unit":unit,"time":time]
        def doc = new DocxHelper("""data/单词默写.docx""")
        doc.replace(paras).saveAsPDF("${outputFileName}.pdf")

    }
    static Map<String, String[]> parseArguments(String[] args) {
        Map<String, List<String>> parsedArgs = [:] // Groovy的字面量语法创建Map
        String currentKey = ""

        args.each { arg ->
            if (arg.startsWith('-')) {
                currentKey = arg[1..-1] // 使用Groovy的范围语法获取子字符串
                parsedArgs[currentKey] = [] // 自动推断为ArrayList
            } else if (currentKey) {
                parsedArgs[currentKey] << arg // 使用左移操作符添加元素到列表
            }
        }

        // 转换列表为数组
        Map<String, String[]> result = parsedArgs.collectEntries { key, value ->
            [(key): value as String[]] // 使用'as'关键字转型
        }

        result
    }

    static void main(String[] args) {
        AsposeRegister.registerWord_v_22_5()

        Map<String, String[]> parsedArgs = parseArguments(args)

        if (parsedArgs.containsKey('i') && parsedArgs['i'] && parsedArgs.containsKey('o') && parsedArgs['o']) {
            String[] inputArgs = parsedArgs['i']
            String outputFileName = parsedArgs['o'][0]

            if (inputArgs.length > 0) {
                switch (inputArgs[0]) {
                    case "exportVocabulary":
                        exportVocabulary(inputArgs.tail(), outputFileName)
                        break
                    case "exportKata":
                        // 确保为exportKata提供足够的参数
                        exportKata(inputArgs.length > 1 ? inputArgs[1] : null, inputArgs.length > 2 ? inputArgs[2] : null, outputFileName)
                        break
                    default:
                        println "Unknown method: ${inputArgs[0]}"
                }
            } else {
                println "No method specified"
            }
        } else {
            println "Input IDs or output file name not specified"
        }
    }
    static void exportKata(type,type2, String outputFileName) {
        println("${type} ${type2} ${outputFileName}")
        //type： 1 生成清音， 2.只生成 浊音/拗音 3.都生成
        //type2:  1 平仮名 hiragana  2.片仮名 katakana カタカナ
        def typeConditions = [
                "1": "'清音'",
                "2": "'浊音', '半浊音', '拗音', '浊拗音'",
                "3": "'清音', '浊音', '半浊音', '拗音', '浊拗音'"
        ]
        if(type == null) type =1
        if(type2 == null) type2 =2

        def sqlStr = "SELECT id,hiragana,katakana,roman,type1 FROM JapaneseKana where type1 in (${typeConditions[type]}) and status is null".toString()
        println(sqlStr)
        def dbhelper = new DBHelper("sqlite1")
        def data = dbhelper.query(sqlStr)

        println(data)
        def q1 = ""
        def a1 = ""
        1.upto(300) { it ->
            def randomNumber = (int) (Math.random() * data.size())
            q1 += data[randomNumber].roman

            if(data[randomNumber].roman.length()==1){
//                println(data[randomNumber].roman.length())
                q1 += "  "
            }else{
                q1 += " "
            }
            a1 += (type2=="2"?data[randomNumber].katakana:data[randomNumber].hiragana)+ " "
            if (it % 20 == 0) {
                q1 += "\n"
                a1 += "\n"
            }
        }
        def doc = new DocxHelper("""data/五十音图测试.docx""")
        def paras = ["q1": newlineToBreakHack(q1), "a1": newlineToBreakHack(a1)]
        doc.replace(paras).saveAsPDF("${outputFileName}.pdf")
//        DocxHelper.toPDF("五十音图测试${type}.docx", "五十音图测试${type}.pdf")

    }
    static String newlineToBreakHack(String r) {

        StringTokenizer st = new StringTokenizer(r, "\t\n\r\f");
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
