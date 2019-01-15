package service


import yjh.helper.Excelhelper
import tool.DocxHelper
/**
 * Created by Peter.Yang on 2019/1/15.
 */
class OfficeAutoService {
    public static void main(String[] args) {
        va1()

    }
    static dataPath = "${System.getProperty("user.dir")}/data"
    static def reportExcelPath = "${dataPath}/9v.xlsx"
    static def reportDocTmpPath = "${dataPath}/tmp.docx"

    static void va1() {

        def subjectItemParas = [:] //科目考察项，参数列表
        def level
        def levelsParas = [:]
        def eh = new Excelhelper(reportExcelPath)
        def tab = eh.read()


        def subjects = tab[0].findAll { it.length() > 0 && !it.equals("科目") }
        def subjectItems = tab[2].takeRight(tab[2].size() - 6).findAll { // 科目考察项,二维数组 每8个一组
            !(it ==~ /评语段.*/) && !(it.equals("总评"))
        }

        subjects.eachWithIndex { def entry, int i ->
            def key = "subject${i}".toString()
            subjectItemParas.put(key, entry)
        }
        tab = tab.takeRight(tab.size()-3).findAll {!it[0].equals("")}

        tab.each { row ->

//            def paras = [:]
//            paras.cnname = row[2]
//            paras.enname = row[3]
            HashMap<String, String> paras = new HashMap<String, String>();

            paras.put("cnname", "马参军");
            paras.put("enname", "abc");


            def doc = new DocxHelper(reportDocTmpPath)
            .replace(paras).saveAs("${dataPath}/out/${paras.cnname}.docx")
            System.exit(0)
        }


    }
}
