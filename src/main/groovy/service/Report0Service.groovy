package service

import common.Const
import org.apache.commons.compress.archivers.zip.Zip64Mode
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import yjh.helper.Excelhelper
import tool.DocxHelper

/**
 * Created by Peter.Yang on 2019/1/15.
 */
class Report0Service {

    static void main(String[] args) {

        """8V
7V""".split(/\s+/).each {
            println("******parse ${it}.xlsx...")
            def s1 = new Report0Service("${dataPath}/${it}.xlsx")
            s1.va2(new FileOutputStream("1.zip"))
        }
        Report0Service.checkWords()
    }


    Excelhelper eh

    Report0Service(String reportExcelPath) {
        eh = new Excelhelper(reportExcelPath)
    }

//    Report0Service(InputStream fis) {
//        eh = new Excelhelper(fis)
//    }


    static def dataPath = Const.dataPath
    static def reportDocTmpPath = dataPath + "/tmp.docx"
//    static def metasExcelPath = dataPath + "/词汇表.xlsx"
    static def metasExcelPath = Const.vocabularyPath2

    static int SUBJECT_DIM1_NUM = 8 // 1级考察项
    static int SUBJECT_DIM2_NUM = 14 // 2级考察项
    static int SUBJECT_DIM1_SUM_NUM = 4 // 总评 评语
    static int SUBJECT_DIM1_ALL_NUM = SUBJECT_DIM1_NUM + SUBJECT_DIM1_SUM_NUM // 总评 评语
    static int META_COL_NUM = 6 // 姓名等其他元数据项
    static def metas = [:]
    static def qualitySubjects = "技能 品格与习惯 作业".split(/\s/)

    def paras = [:]

    String metaMain(String alias) {
        //获取主词汇
        if (metas[alias] != null && metas[alias]["main"] != null) {
            return metas[alias]["main"]
        } else {
            return alias
        }
    }


    void va2(OutputStream op) {
//        Report0Service.initMetas()
        def subjectParas = [:]
        def subjectItemParas = [:]
        def commonParas = [:]

        eh.setSheet(0)// 0:科目考察表 1: 明细打分表
        def tab = eh.read() //获取非学科考察科目


        def subjects = []
        tab[0].eachWithIndex { def subject, int i ->
            if (i % SUBJECT_DIM1_ALL_NUM == META_COL_NUM) {
                subjects += subject
                if (subject.equals("")) {
                    println("【科目单元为空】：可能错误的科目:")
                    println(subjects[subjects.size() - 2])
//                    System.exit(1)
                }
            }

        }
        println("*******s1.get all subjects over ${subjects.toString()}*********")
        def mergeSubjects = eh.mergeUnitByRegxp(subjects) // 一维数组 变 2维数组
        def unitCount = 0


        mergeSubjects.eachWithIndex { List entry, i -> //对每个科目 的1级考察项进行统计

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
                1.upto(SUBJECT_DIM1_NUM) { j ->
                    def startNum = unitCount * SUBJECT_DIM1_ALL_NUM + META_COL_NUM
                    def subjectItem = tab[2][startNum + j - 1]
                    String weight = tab[1][startNum + j - 1]
                    subjectItem = metaMain(subjectItem)
                    String subjectEnItem = metas[subjectItem] ? (metas[subjectItem]["en"] ? metas[subjectItem]["en"] : "") : ""
                    if (eh.getWorkbook().getNumberOfSheets() > 1) {
                        weight = ""
                    } else {
                        weight = weight.length() > 0 ? "(${(int) Double.parseDouble(weight)}%)" : ""
                    }
                    if (subjectEnItem.equals("") && !subjectItem.equals("")) println "[${subject}]${subjectItem}${weight}"
                    String desc = metas[subjectItem] ? (metas[subjectItem]["desc"] ? metas[subjectItem]["desc"] : "") : ""

                    String key1 = "item${i + 1}${j + k * SUBJECT_DIM1_NUM}" //SUBJECT_DIM1_NUM个科目考察项
                    String key2 = "desc${i + 1}${j + k * SUBJECT_DIM1_NUM}" //SUBJECT_DIM1_NUM个科目考察项
                    subjectItemParas.put(key1, newlineToBreakHack("${subjectItem}\r\n${subjectEnItem}\r\n${weight}"))
                    subjectItemParas.put(key2, newlineToBreakHack("${desc}"))
                }
                unitCount++
            }
        }
        commonParas = subjectItemParas + subjectParas
        println("*******s2.parse dim1 over.*********")


        //s3.parse dim2(option)
        def dim2LevelTab = []
        def qualitySubjectItemHash = [:]

        def dim1LevelTab = tab.takeRight(tab.size() - 3).findAll { !it[0].equals("") } //去除空行以及前3行
        if (eh.getWorkbook().getNumberOfSheets() > 1) { //构造非学科类明细考察项hash表
            reportDocTmpPath = "${dataPath}/tmp2.docx"
            eh.setSheet(1)
            def tab2 = eh.read()
            dim2LevelTab = tab2.takeRight(tab2.size() - 3).findAll { !it[0].equals("") } //去除空行以及前3行
            def qualitySubjectItemCount = 0
            tab2[0].eachWithIndex { def item, int i ->
                if (i % SUBJECT_DIM2_NUM == META_COL_NUM) {
                    if (item.equals("")) { //留空栏不报错
                        item = qualitySubjectItemCount
                    } else {
                        item = metaMain(item)
                        qualitySubjectItemHash[item] = qualitySubjectItemCount
                    }
                    qualitySubjectItemCount++
                }
            }
            println("*******s3.parse dim2(option) over.*********")
        } else {
            reportDocTmpPath = "${dataPath}/tmp.docx"
        }

        ZipArchiveOutputStream zous = new ZipArchiveOutputStream(op);        //web端时直接用response流
        zous.setUseZip64(Zip64Mode.AsNeeded)

        //s4.parse dim2
        dim1LevelTab.eachWithIndex { row, num -> //每一行是一个学生
            paras = [:]
            paras.classname = row[0]
            paras.studentno = row[1]
            paras.cnname = row[2]
            paras.enname = row[3]
            paras.studentcontent = row[SUBJECT_DIM1_NUM + SUBJECT_DIM1_SUM_NUM + META_COL_NUM - 1]

            unitCount = 0
            mergeSubjects.eachWithIndex { List entry, i ->
                entry.eachWithIndex { String subject, int k ->
                    //对于小学非学科科目需要特殊处理；
                    //由于包含了合并科目需要整合此部分

                    def startNum = unitCount * SUBJECT_DIM1_ALL_NUM + META_COL_NUM

                    1.upto(SUBJECT_DIM1_NUM) { j ->
                        String levelKey = "level${i + 1}${k * SUBJECT_DIM1_NUM + j}"
                        paras[levelKey] = row[startNum + j - 1]

                        if (qualitySubjects.any { it -> subject.contains(it) }) { //对于非学科类(小学)部分，需要level将按照明细项平均值 覆盖 总评
                            def qualitySubjectItem = metaMain(tab[2][startNum + j - 1])
                            def qualitySubjectItemIndex = qualitySubjectItemHash[qualitySubjectItem]
                            if (qualitySubjectItemIndex != null) {
                                int nonStartNum = qualitySubjectItemIndex * this.SUBJECT_DIM2_NUM + this.META_COL_NUM
                                List detailItem = dim2LevelTab[num]
                                def nonEndNum = (nonStartNum + SUBJECT_DIM2_NUM - 1 > detailItem.size() - 1) ? detailItem.size() - 1 : nonStartNum + SUBJECT_DIM2_NUM - 1
                                // end项可能表格不齐
                                List detailItemScore = dim2LevelTab[num][nonStartNum..nonEndNum]
                                detailItemScore.removeAll([null, ""])
                                println("${paras.cnname}\t${entry[0]}\t${tab[2][startNum + j - 1]}\t${detailItemScore}")
                                if (detailItemScore.size() > 0) {
                                    Double score = (detailItemScore.sum() / detailItemScore.size())
                                    paras[levelKey] = cal(score.toString())
                                }
                            } else {
                                if (qualitySubjectItem.length() > 0) {
                                    println("【error】【${qualitySubjectItem}】在sheet1中无对应项!")//表格前后不一致
//                                    System.exit(1)
                                }
                            }

                        }

                    }

                    if (k == entry.size() - 1) { //merge ，到统计一下平均分
                        def score = row[startNum + SUBJECT_DIM1_NUM].toString() //总分
                        if (!score.equals("")) {
                            paras["sum" + (i + 1)] = cal(score) //按0.5一个登级
                        }
                        println("${paras.cnname}\t${entry[0]}\t${paras["sum" + (i + 1)]}")
                        paras["content" + (i + 1) + "1"] = newlineToBreakHack(row[startNum + SUBJECT_DIM1_NUM + 1] + "\n\n")
                        paras["content" + (i + 1) + "2"] = row[startNum + SUBJECT_DIM1_NUM + 2]
                    }
                    unitCount++
                }

            }

            if (paras.get("item29") == null) {
                paras["item29"] = ""
                paras["item210"] = ""
                paras["desc29"] = ""
                paras["desc210"] = ""
                paras["level29"] = ""
                paras["level210"] = ""

            }
            paras = commonParas + paras
//            println(paras)

            def outputName = "${paras.classname}_${paras.studentno}_${paras.cnname}__${paras.enname}.docx"
            def outputPath = "${dataPath}/out/${outputName}"

//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
//            new DocxHelper(reportDocTmpPath).replace(paras).saveAsOutputStream(outputStream)
//            zous.putArchiveEntry(new ZipArchiveEntry(outputName))
//            zous.write(outputStream.toByteArray())
//            zous.closeArchiveEntry()
        }
//        zous.close()
    }

//    void exportToLocalPath(outputPath) {
//        def doc = new DocxHelper(reportDocTmpPath).replace(paras).saveAs(outputPath)
//    }

    static Excelhelper initMetas() {
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
        return eh
    }

    static checkWords() {
        def eh = initMetas()
        if (metas.size() < 1) {
            initMetas()
            throw new Exception("vocabulary is null.")
        }
        eh.setSheet("2019-1学期考察项")
        def tabs = eh.read()

        //check每个科目是否属于标准词汇
        def subejct = []
        def subjectNum = 11+3
        for (def i in 0..subjectNum) {
            subejct[i] = tabs[0][SUBJECT_DIM1_NUM * i + 1]
//            println(tabs[0][SUBJECT_DIM1_NUM * i + 1])
        }



        for (def i in 0..6) { //小学7个班
            def row = tabs[i * 2 + 2]
            def vaClass = row[0]

            println (vaClass + ": ")


            for (def j in 0..11) { // 3态度类+ 9个学科 科目
                def output = ""
                def allDimName = ""
                for (def k in 1..SUBJECT_DIM1_NUM) { //8个考察项
                    def dim1Name = row[j * SUBJECT_DIM1_NUM + k]
                    if (dim1Name == null) {
                        dim1Name = ""
                    }
                    allDimName += dim1Name
                    if(metas[dim1Name] == null){
                        output += ("考察项${k}无对应词汇: `${dim1Name}`;")
                    }
                }
                if (allDimName == "") {
                    output += ("请补充考察项.")
                }
                if(output != "") {
                    print(subejct[j] + ': ' + output)
                    println()
                }
            }
            println()
        }

        for (def i in 0..2) { //小学7个班
            def row = tabs[i * 2 + 20]
            def vaClass = row[0]
            println(vaClass + ": ")

            for (def j in 3..10) { // 8个学科 科目
                def output = ""
                def allDimName = ""
                for (def k in 1..SUBJECT_DIM1_NUM) { //8个考察项
                    def dim1Name = row[j * SUBJECT_DIM1_NUM + k]
                    if (dim1Name == null) {
                        dim1Name = ""
                    }
                    allDimName += dim1Name
                    if (metas[dim1Name] == null) {
                        output += ("考察项${k}无对应词汇: `${dim1Name}`;")
                    }
                }
                if (allDimName == "") {
                    output += ("请补充考察项.")
                }
                if (output != "") {
                    print(subejct[j] + ': ' + output)
                    println()
                }
            }
            println()
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

    static Double cal(String num) {
        if(num == ""){return null}
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

}
