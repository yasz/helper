package service

import common.Const
import tool.DBHelper
import tool.DocxHelper

/**
 * Created by Peter.Yang on 2019/6/22.
 */
class CorvService {
    //导出报告单

    static void main(String[] args) {

        detail1()
    }

    static void detail1(){
        def commentParas = [:]
        def scoreParas = [:]
        def h = [:]
        def h2 = [:]
        def db = new DBHelper('192.168.0.149', 'postgres', 'ruianVA123')

        db.query("""SELECT classname AS grade,cnname as name,idno,case sex when 'M' then '男' else '女' end as sex FROM "va3" t1 
left join VA1 t2 ON  t1.vano = t2.vano
where classname  in ('7v')
order by 1,2 desc
""").each { it -> //科目汇总分数视图
            h[it.name] = h[it.name] ? h[it.name] : [:]
            it.each{key,value->
                h[it.name][key] = value
            }
//            scoreParas[it.vano][it.subjectno] = it
        }
        h.each { s ->

            def outputName = "out2\\${s.value["grade"]}_${s.key}_3.docx"
            def tmpPath = "C:\\Users\\peterjiahao\\Desktop\\VA\\附件1-3.docx"
            new DocxHelper(tmpPath).replace(s.value).saveAs(outputName)
        }
    }

    static def n2b(str) {
        str == null ? "" : str
    }

    static String newlineToBreakHack(String r) {

        StringTokenizer st = new StringTokenizer(r, "\n\r\f");
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
