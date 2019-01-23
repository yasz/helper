package service

import org.docx4j.jaxb.Context
import yjh.helper.Excelhelper
import tool.DocxHelper

/**
 * Created by Peter.Yang on 2019/1/15.
 */
class OfficeAutoService {

    static void main(String[] args) {
        OfficeAutoService.initMetas()
        """3V
""".split(/\s+/).each {
            println("******parse ${it}.xlsx...")
            def s1 = new OfficeAutoService("${dataPath}/${it}.xlsx")
            s1.va2()
        }
    }

    OfficeAutoService(String reportExcelPath) {
        this.reportExcelPath = reportExcelPath
        this.reportDocTmpPath = "${dataPath}/tmp.docx"
    }

    static def dataPath = "${System.getProperty("user.dir")}/data"
    def reportExcelPath
    def reportDocTmpPath = "${dataPath}/tmp.docx"
    static def metasExcelPath = "${dataPath}/词汇表.xlsx"

    static int SUBJECT_ITEM_NUM = 8 // 基本考察项
    static int NONSUBJECT_ITEM_NUM = 14 // 基本考察项
    static int SUBJECT_SUM_NUM = 4 // 总评 评语
    static int SUBJECT_NUM = SUBJECT_ITEM_NUM + SUBJECT_SUM_NUM // 总评 评语
    static int META_COL_NUM = 6 // 元数据项
    static def metas = [:]
    static def nonSubjects = "技能 品格与习惯 作业".split(/\s/)

    String metaMain(String alias){
        if (metas[alias]!=null && metas[alias]["main"] != null) {
            return metas[alias]["main"]
        }else{
            return alias
        }
    }
    void va2() {

        def subjectParas = [:]
        def subjectItemParas = [:]
        def commonParas = [:]

        def eh = new Excelhelper(reportExcelPath)
        eh.setSheet(0)// 0:科目考察表 1: 明细打分表
        def tab = eh.read() //获取非学科考察科目

        def subjects = []
        tab[0].eachWithIndex { def subject, int i ->
            if (i % SUBJECT_NUM == META_COL_NUM) {
                subjects += subject
                if(subject.equals("")){
                    println("【科目单元为空】：可能错误的科目:")
                    println(subjects[subjects.size()-2])
                    System.exit(1)
                }
            }

        }
        def mergeSubjects = eh.mergeUnitByRegxp(subjects)
        def unitCount = 0

        mergeSubjects.eachWithIndex { List entry, i -> //init 考察项 , 注意这里要对相同科目做一下合并

            // 构造subject para
            String key0 = "subject${i + 1}"
            String subject = entry[0]
            if (entry.size() > 1) {
                subject = subject.replaceAll(/\d$/, "")
            }

            subject = metaMain(subject)
            String subjectEn = metas[subject] ? metas[subject]["en"] : "null"
            subjectParas.put(key0, newlineToBreakHack("${subject}\r\n${subjectEn}"))

            // 构造subjectItem  subjectDesc para
            entry.eachWithIndex { tmpSubject, k ->
                1.upto(SUBJECT_ITEM_NUM) { j ->
                    def startNum = unitCount * SUBJECT_NUM + META_COL_NUM
                    def subjectItem = tab[2][startNum + j - 1]
                    String weight = tab[1][startNum + j - 1]
                    subjectItem = metaMain(subjectItem)
                    String subjectEnItem = metas[subjectItem] ? (metas[subjectItem]["en"] ? metas[subjectItem]["en"] : "") : ""
                    if (eh.getWorkbook().getNumberOfSheets() > 1){weight = ""}else{
                        weight = weight.length() > 0 ? "(${(int) Double.parseDouble(weight)}%)" : ""
                    }
                    if (subjectEnItem.equals("") && !subjectItem.equals("")) println "[${subject}]${subjectItem}${weight}"
                    String desc = metas[subjectItem] ? (metas[subjectItem]["desc"] ? metas[subjectItem]["desc"] : "") : ""

                    String key1 = "item${i + 1}${j + k * SUBJECT_ITEM_NUM}" //SUBJECT_ITEM_NUM个科目考察项
                    String key2 = "desc${i + 1}${j + k * SUBJECT_ITEM_NUM}" //SUBJECT_ITEM_NUM个科目考察项

                    subjectItemParas.put(key1, newlineToBreakHack("${subjectItem}\r\n${subjectEnItem}\r\n${weight}"))
                    subjectItemParas.put(key2, newlineToBreakHack("${desc}"))
                }
                unitCount++
            }

        }

//        return
        commonParas = subjectItemParas + subjectParas
        def detailTab2 = []

        def nonSubjectItemHash = [:]

        def detailTab = tab.takeRight(tab.size() - 3).findAll { !it[0].equals("") } //去除空行以及前3行
        if (eh.getWorkbook().getNumberOfSheets() > 1) { //构造非学科类明细考察项hash表
            reportDocTmpPath = "${dataPath}/tmp2.docx"
            eh.setSheet(1)
            def tab2 = eh.read()
            detailTab2 = tab2.takeRight(tab2.size() - 3).findAll { !it[0].equals("") } //去除空行以及前3行
            def nonSubjectItemCount = 0
            tab2[0].eachWithIndex { def item, int i ->
                if (i % NONSUBJECT_ITEM_NUM == META_COL_NUM) {
                    if(item.equals("")){ //留空栏不报错
                        item = nonSubjectItemCount
                    }else{
                        item = metaMain(item)
                    }
                    nonSubjectItemCount++
                    nonSubjectItemHash[item] = nonSubjectItemCount
                }
            }
        }

        detailTab.eachWithIndex { row, num -> //每一行是一个学生
            def paras = [:]
            paras.classname = row[0]
            paras.studentno = row[1]
            paras.cnname = row[2]
            paras.enname = row[3]
            paras.studentcontent = row[SUBJECT_ITEM_NUM + SUBJECT_SUM_NUM + META_COL_NUM - 1]

            unitCount = 0
            mergeSubjects.eachWithIndex { List entry, i ->
                entry.eachWithIndex { String subject, int k ->
                    //对于小学非学科科目需要特殊处理；
                    //由于包含了合并科目需要整合此部分

                    def startNum = unitCount * SUBJECT_NUM + META_COL_NUM

                    1.upto(SUBJECT_ITEM_NUM) { j ->
                        String levelKey = "level${i + 1}${k * SUBJECT_ITEM_NUM + j}"
                        paras[levelKey] = row[startNum + j - 1]

                        if (nonSubjects.any { it -> subject.contains(it) }) { //对于非学科类(小学)部分，需要level将按照明细项平均值 覆盖 总评
                            def nonSubjectItem = metaMain(tab[2][startNum + j - 1])
                            def nonSubjectItemIndex = nonSubjectItemHash[nonSubjectItem]
                            if (nonSubjectItemIndex != null  ) {
                                int nonStartNum = nonSubjectItemIndex * this.NONSUBJECT_ITEM_NUM + this.META_COL_NUM
                                List detailItem = detailTab2[num]
                                def nonEndNum = (nonStartNum + NONSUBJECT_ITEM_NUM - 1 > detailItem.size() - 1) ?
                                        detailItem.size() - 1 : nonStartNum + NONSUBJECT_ITEM_NUM - 1 // end项可能表格不齐
                                List detailItemScore = detailTab2[num][nonStartNum..nonEndNum]
                                detailItemScore.removeAll([null, ""])
                                println(this.reportExcelPath + paras.cnname + tab[2][startNum + j - 1] +detailItemScore)
                                if(detailItemScore.size()>0){
                                    Double score = (detailItemScore.sum() / detailItemScore.size())
                                    paras[levelKey] = cal(score.toString())
                                }
                            }else{
                                if(nonSubjectItem.length()>0){
                                    println("【error】【${nonSubjectItem}】在sheet1中无对应项!")//表格前后不一致
                                    System.exit(1)
                                }
                            }

                        }

                    }

                    if (k == entry.size() - 1) {
                        def score = row[startNum + SUBJECT_ITEM_NUM].toString() //总分
                        if (!score.equals("")) {
                            paras["sum" + (i + 1)] = cal(score) //按0.5一个登级
                        }
                        paras["content" + (i + 1) + "1"] = newlineToBreakHack(row[startNum + SUBJECT_ITEM_NUM + 1] + "\n\n")
                        paras["content" + (i + 1) + "2"] = row[startNum + SUBJECT_ITEM_NUM + 2]
                    }
                    unitCount++
                }

            }

            if(paras.get("item29") == null){
                paras["item29"]=""
                paras["item210"]=""
                paras["desc29"]=""
                paras["desc210"]=""
                paras["level29"]=""
                paras["level210"]=""

            }
            paras = commonParas + paras
            def doc = new DocxHelper(reportDocTmpPath)
                    .replace(paras).saveAs("${dataPath}/out/${paras.classname}_${paras.studentno}_${paras.cnname}__${paras.enname}" +
                    ".docx")
//            System.exit(0)
        }


    }


    static void initMetas() {
        //构造词汇映射hash
        def eh = new Excelhelper(metasExcelPath)
        def metasTab = eh.read()
        metasTab.takeRight(metasTab.size() - 1).each { row -> //去除首行
            String en = row[2]
            String desc = row[4]

            def value = [en: en, desc: desc]
            metas.put(row[1], value)

            row.takeRight(row.size() - 5).each { String word -> //alias
                if (word.length() > 0) {
                    value["main"] = row[1]
                    metas.put(word, value)
                }
            }
        }
    }


    private String newlineToBreakHack(String r) {

        StringTokenizer st = new StringTokenizer(r, "\n\r\f");
        // tokenize on the newline character, the carriage-return character, and the form-feed character
        StringBuilder sb = new StringBuilder();

        boolean firsttoken = true;
        while (st.hasMoreTokens()) {
            String line = (String) st.nextToken();
            if (firsttoken) {
                firsttoken = false;
            } else {
                sb.append("</w:t><w:br/><w:t>");
            }
            sb.append(line);
        }
        return sb.toString();
    }

    Double cal(String num) {
        Double f = Double.parseDouble(num)
        int roundNum = (int) (f * 100) % 100
        if (roundNum < 25) {
            f = Math.floor(f)
        } else if (roundNum < 75) {
            f = Math.floor(f) + 0.5
        } else {
            f = Math.floor(f) + 1
        }
        return f
    }


//     void va1() {
//        def eh = new Excelhelper(reportExcelPath)
//        def tab = eh.read()
//
//
//        def subjects = []
//        tab[0].eachWithIndex { def subject, int i ->
//            if (i % SUBJECT_NUM == META_COL_NUM) {
//                subjects += subject
//            }
//        }
//
//        subjects.eachWithIndex { def entry, int n, int i = n + 1 ->
//            def key = "subject${i}".toString()
//            String en = metas[entry] ? metas[entry]["en"] : "null"
//            if(metas[entry]["main"] !=null){entry = metas[entry]["main"]}
//            subjectParas.put(key, newlineToBreakHack(entry + "\r\n" + en))
//        }
//
//        def subjectItems = []
//        subjects.eachWithIndex { def entry, i -> //init 考察项
//            1.upto(SUBJECT_ITEM_NUM) { j ->
//                def startNum = i * (SUBJECT_ITEM_NUM + SUBJECT_SUM_NUM) + META_COL_NUM
//                def subjectItem = tab[2][startNum + j - 1]
//                String weight = tab[1][startNum + j - 1]
//                if(metas[subjectItem]["main"] !=null){subjectItem = metas[subjectItem]["main"]}
//
//                String subjectEnItem = metas[subjectItem] ? (metas[subjectItem]["en"] ? metas[subjectItem]["en"] : "") : ""
//                if (subjectEnItem.equals("") && !subjectItem.equals("")) println "[${entry}]${subjectItem}"
//                weight = weight.length() > 0 ? "(${weight}%)" : ""
//                subjectItems += subjectItem
//                String key = "item${i + 1}${j}" //SUBJECT_ITEM_NUM个科目考察项
//                subjectItemParas.put(key, newlineToBreakHack("${subjectItem}\r\n${subjectEnItem} ${weight}"))
//            }
//        }
//
//        commonParas = subjectItemParas + subjectParas
//
//        tab = tab.takeRight(tab.size() - 3).findAll { !it[0].equals("") } //去除空行以及前3行
//        tab.each { row -> //每一行是一个学生
//            def paras = [:]
//            paras.classname = row[0]
//            paras.studentno = row[1]
//            paras.cnname = row[2]
//            paras.enname = row[3]
//            paras.studentcontent = row[SUBJECT_ITEM_NUM + SUBJECT_SUM_NUM + META_COL_NUM - 1]
//            subjectParas.eachWithIndex { subject, i ->
//                def startNum = i * (SUBJECT_ITEM_NUM + SUBJECT_SUM_NUM) + META_COL_NUM
//                1.upto(SUBJECT_ITEM_NUM) { j ->
//                    String levelKey = "level${i + 1}${j}"
//                    paras[levelKey] = row[startNum + j - 1]
//                }
//
//                //分数部分
//                def score = row[startNum + SUBJECT_ITEM_NUM].toString()
//                if (!score.equals("")) {
//                    paras["sum" + (i + 1)] = cal(score) //按0.5一个登级
//                }
//                paras["content" + (i + 1) + "1"] = newlineToBreakHack(row[startNum + SUBJECT_ITEM_NUM + 1] + "\n\n")
//                paras["content" + (i + 1) + "2"] = row[startNum + SUBJECT_ITEM_NUM + 2]
//            }
//            paras += commonParas
//            def doc = new DocxHelper(reportDocTmpPath)
//                    .replace(paras).saveAs("${dataPath}/out/${paras.classname}${paras.cnname}.docx")
//            System.exit(0)
//        }
//
//
//    }
}
