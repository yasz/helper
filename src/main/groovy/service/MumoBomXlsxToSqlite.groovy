package service

import com.alibaba.fastjson.JSON
import common.AsposeRegister
import groovy.io.FileType
import utils.DBHelper
import yjh.helper.Excelhelper

import java.text.SimpleDateFormat

class MumoBomXlsxToSqlite {

    static String parse(originalData) {
//        def excel = new Excelhelper("C:\\Users\\yangj\\Documents\\双层床料单.xlsx")
//        def originalData = excel.read()

        //s1
        def startIndex = originalData.findIndexOf { it.contains('序号') } + 1
        def endIndex = originalData.findIndexOf(startIndex, { it[0] == null || !(it[0] instanceof Double) }) - 1
        def res = originalData[startIndex..endIndex]
        res = res.findAll { row ->
            !(row[3] == null || row[3].toString().trim().isEmpty()) || !(row[4] == null || row[4].toString().trim().isEmpty())
        }
        //s2
        def remarkIndex = originalData[startIndex - 1].indexOf('备注')
        res = res.collect { it[0..<(remarkIndex + 1)] }


        //s3 处理行
        def titleData = originalData[startIndex - 1].take(remarkIndex + 1)
        if (titleData[1] == '名称') {
            titleData[1] = '板件名称'
        }
        if (titleData[1] == '部件') {
            titleData[1] = '板件部位'
        }
        int nameIndex = titleData.indexOf('板件名称')
        if (nameIndex != -1 && (nameIndex + 1) < titleData.size() && (titleData[nameIndex + 1].trim().isEmpty() || titleData[nameIndex + 1] == null)) {
            titleData[nameIndex] = '板件部位'
            titleData[nameIndex + 1] = '板件名称'
        }
        if ((titleData[1] == null || titleData[1].trim().isEmpty())) {
            titleData[1] = '板件名称'
        }
        //s4 数据填充
        def lastPart = null
        def lastRemark = null

        res.collect { row ->
            if (row[1] == null || row[1].trim().isEmpty()) {
                row[1] = lastPart
            } else {
                lastPart = row[1]
            }

            if (row[2] instanceof String && (row[2] == null || row[2].trim().isEmpty())) {
                row[2] = row[1]
                row[1] = ""
            }
            if (row[remarkIndex].trim().isEmpty() || row[remarkIndex] == null) {
                row[remarkIndex] = lastRemark
            } else {
                lastRemark = row[remarkIndex]
            }
            row
        }
        println(res)

        //s5 填充json
        def jsonData = []

        res.each { row ->
            def rowMap = [:]
            titleData.eachWithIndex { title, index ->
                // 使用标题和当前行的值填充映射
                rowMap[title] = index < row.size() ? row[index] ?: "" : "" // 用空字符串替换null值
            }
            jsonData << rowMap
        }
        return JSON.toJSONString(jsonData, true)
    }
    static void main(String[] args) {

        def dbhelper = new DBHelper("sqlite2")
        def res = dbhelper.query("select * from bom_current")
        Map map = res.groupBy { it.file_path }.collectEntries { filePath, entries ->
            [(filePath): entries.groupBy { it.sheet_name }]
        }
        def workDir = "C:\\Users\\yangj\\Desktop\\mumo_bom"
        def excludeDirectory = "C:\\Users\\yangj\\Desktop\\mumo_bom\\▲技术部资料"

        new File(workDir).eachFileRecurse(FileType.FILES) { file ->
            def filePath = file.getPath().replace(workDir, '')
            def currentFileBom = map[filePath]
            if (!(file.name.endsWith(".xlsx") && file.name.contains("料单"))) return
            if (file.name.startsWith("~")) return
            if (file.getParent().contains("sync")||file.name.contains('模板') ){
                return
            }
            if (file.getParent().contains(excludeDirectory) ){
                return
            }

            if (currentFileBom && new SimpleDateFormat('yyyy-MM-dd HH:mm:ss').format(file.lastModified()).equals(currentFileBom.entrySet().iterator().next().value[0]["start_date"])) return
            if (currentFileBom) {
                println(file.getName() + "更新的文件bom:${file.getPath()}")
                dbhelper.executeUpdate("""  update bom_history set end_date = '${new SimpleDateFormat('yyyy-MM-dd HH:mm:ss').format(new Date())}' 
                                                   where bom_id in ( 
                                                       select bom_id
                                                       FROM bom_main
                                                       where file_path ='${filePath}')  """);
                //将历史的拉链全部关上，并注意要提取bom_id后面使用
            } else {
                println(file.getName() + "全新的文件bom:${file.getPath()}")
                //插入bom_id后获取bom_od；
            }
            //在excel里分别读取sheet进行加载
            def category = file.getPath().minus(workDir).minus(file.name).replace("\\", "/")
            def excel = new Excelhelper(file.getPath())
            for (def i = 0; i < excel.workbook.getNumberOfSheets(); i++) {
                def sheet = excel.getSheet(i)
                if (["调整内容", "修改记录", "Sheet1", "Sheet2", "WpsReserved_CellImgList"].contains(sheet.getSheetName())) continue
                excel.setSheet(i)
                def bomName = "${file.name.replaceAll(/\.[^\.]*$/, '').replaceAll(/料单/, '')}"
                println("sheet:" +  sheet.getSheetName())
                def bomData = parse(excel.read())
                //判断 currentFileBom[sheet] 是否存在，若不存在则需要插入bom_id,并获取bom_id，若已存在，则直接使用已有的bom_id；
                def bom_id = (currentFileBom && currentFileBom[sheet.getSheetName()]) ? currentFileBom[sheet.getSheetName()][0]."bom_id" :
                        dbhelper.executeInsertAndGetId("insert into bom_main(bom_name,bom_category,file_path,sheet_name) values(" +
                                "'${bomName}','${category}','${filePath}','${sheet.getSheetName()}' )");

                def sql = "insert into bom_history(bom_id ,start_date,bom_data) " +
                        "values(${bom_id},'${new SimpleDateFormat('yyyy-MM-dd HH:mm:ss').format(file.lastModified())}','${bomData}')"
                println(sql)
                dbhelper.executeUpdate(sql)
            }
        }


        System.out.println(System.getProperty("file.encoding"));

    }


}
