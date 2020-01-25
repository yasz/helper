package yjh.helper

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.formula.eval.FunctionEval
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.ss.usermodel.FormulaEvaluator
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook

class Excelhelper {

    def EXTENSION_XLS = "xls"
    def EXTENSION_XLSX = "xlsx"
    Workbook workbook
    Sheet sheet

    Excelhelper(InputStream fis){
        init()
        workbook = new XSSFWorkbook(fis)
    }
    Excelhelper(String fileName) {
        init()
        if (fileName.endsWith(EXTENSION_XLS)) {
            new File(fileName).withInputStream { is -> workbook = new HSSFWorkbook(is) }
        } else if (fileName.endsWith(EXTENSION_XLSX)) {
            new File(fileName).withInputStream { is -> workbook = new XSSFWorkbook(is)
            }
        }
        setSheet(0)
    }
    def init(){
        Row.metaClass.getAt = { int idx ->
            Cell cell = delegate.getCell(idx as short)
            def cel_Type
            try {
                cel_Type = cell.getCellTypeEnum()
            } catch (Exception e) {
                cel_Type = 3
            }
            if (cell == null) return ""
            def value

            switch (cel_Type) {
                case CellType.NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) value = cell.dateCellValue
                    else value = cell.numericCellValue
                    break
                case CellType.BOOLEAN:
                    value = cell.booleanCellValue
                    break
                case CellType.ERROR:
                    value = ""
                    break
                case CellType.FORMULA:
                    cell.setCellFormula(cell.getCellFormula().replaceAll("_xlfn.",""))
                    value = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator().evaluate(cell)
                            .getNumberValue()
                    break

                default:

                    value = cell.stringCellValue
                    break
            }
            return value
        }


        Row.metaClass.toList = {
            def l = []

            if(delegate.getLastCellNum() >= 0){
                for (i in 0..delegate.getLastCellNum() - 1) {
                    l.add(i, delegate.getAt(i))
                }
            }
            l
        }
        List.metaClass.toLL = {
            def ll = []
            delegate.each {
                ll.add(it.toList())
            }
            ll
        }
        List.metaClass.getUnitArrange = {
            //List<List> list
            def start = 0
            List rl = []
            for (i in 0..delegate.size() - 1) {
                if (i == delegate.size() - 1) {
                    rl.push([start, i])
                    break
                }
                def value = delegate.get(i).get(0)//默认第一列是排序值
                def value2 = delegate.get(i + 1).getAt(0)//默认第一列是排序值
                if (value == value2) {
                    continue
                } else {
                    rl.push([start, i])
                    start = i + 1
                }
            }
            return rl
        }
        List.metaClass.getRowUnitArrange = {
            //List<Row> list
            List list2 = []
            delegate.each {
                list2.push(it.toList())
            }
            def start = 0
            List rl = []
            for (i in 0..list2.size() - 1) {
                if (i == list2.size() - 1) {
                    rl.push([start, i])
                    break
                }
                def value = list2.get(i).get(0)//默认第一列是排序值
                def value2 = list2.get(i + 1).getAt(0)//默认第一列是排序值
                if (value == value2) {
                    continue
                } else {
                    rl.push([start, i])
                    start = i + 1
                }
            }
            return rl
        }

    }
//    读取SHEET的数据到List数组中
    List<Row> readRow(int sn, int en) {
        en = en ? en : sheet.getLastRowNum()
        en = en > sheet.getLastRowNum() ? sheet.getLastRowNum() : en
        sn = sn ? sn : 0
        List<Row> rowList = []
        (sn..en).each { rowList.add(sheet.getRow(it)) }
        return rowList
    }

    List<Row> readRow() {
        return readRow(0, sheet.getLastRowNum())
    }


    List<Row> readRow(int sn, int en, String fw, List kw) {
        //READ CH1
        en = en ? en : sheet.getLastRowNum()
        sn = sn ? sn : 0
        def nm //待过滤TITLE的NUM
        sheet.getRow(0).toList().eachWithIndex {
            n, i ->//VALUE,SEQ
                if (n.equals(fw))
                    nm = nm ? nm : i//仅返回第一列
        }
        def rl
        if (kw) {
            rl = readRow(sn, en).findAll {
                def cv = it.getAt(nm)//取该列
                if (cv.getClass().toString().contains("Double")) {
                    cv = String.format("%.0f", cv);
                }
                if (kw) {
                    kw.findAll {
                        it.equalsIgnoreCase(cv)
                    }
                }
            }
        } else {
            rl = readRow(sn, en)
        }
        return rl
    }

    List<Row> readRow(String fw, List kw) {
        readRow(1, sheet.getLastRowNum(), fw, kw)
    }

    List read(int sn, int en, def fws) {

        List cindex = []
        List result = []
        fws.each { fw ->
            sheet.getRow(0).toList().eachWithIndex {
                value, seq ->//VALUE,SEQ
                    if (fw.equals(value)) {
//                        println("$fw,$seq")
                        cindex.add(seq)
                    }
            }
        }

        def rl = readRow(sn, en).each { row ->
            List irow = []
            cindex.each { index ->
                irow.add(row.getAt(index))
            }
            result.add(irow)
        }
        return result
    }

    List read() {
        read(0, sheet.getLastRowNum())
    }

    List read(def sn) {
        read(sn, sheet.getLastRowNum())
    }
    List read(int sn, int en) {
        List result = []

        readRow(sn, en).each { row ->
            result.add(row.toList())
        }
        return result
    }

    List read(int startrow, int endrow,int startCol,int colNum) {
        List result = []
        readRow(startrow, endrow).each { row ->
            result.add(row.toList()[startCol..colNum])
        }
        return result
    }
    Sheet setSheet(idx) {
        sheet = getSheet(idx)
    }

    void export(def path) {
        FileOutputStream outputStream = new FileOutputStream(path)
        workbook.write(outputStream)
        outputStream.flush()
        outputStream.close()
    }


    void write(List<List> rl, def x, def y) {
        //二维数组
//        println("write list...")
        rl.eachWithIndex { row, i ->
            row.eachWithIndex { it, j ->
//                println(it+" "+(x+j)+" "+(y+i))
                write(it, x + j, y + i)
            }
        }

    }

    void write(def rl, int x, int y) {
        //对当前Sheet指定单元格位置写入一个内容
        def row = sheet.getRow(y)
        if (row) {
            def cell = row.getCell(x)
            if (cell) {
                cell.setCellValue(rl)
            } else {
                row.createCell(x).setCellValue(rl)
            }
        } else {
            sheet.createRow(y).createCell(x).setCellValue(rl)
        }
    }

    void writeRow(List<Row> rl, def x, def y) {
        /**
         * created by yang on 14:48 2018/1/26.
         * describtion:
         * @param rl :
         * @param x :
         * @param y :

         */

        rl.eachWithIndex { row, i ->
            writeRow(row, x, y + i)
        }
    }

    void writeRow(Row rl, def x, def y) {
        for (i in 0..rl.getLastCellNum() - 1) {
            def cell
            Row row = sheet.getRow(y)
            if (row) {
                cell = row.getCell(x + i)
                if (!cell) {
                    cell = row.createCell(x + i)
                }
            } else {
                cell = sheet.createRow(y).createCell(x + i)
            }
//            if(!rl.getCell(i))//源row也可能是空
//            {
//                rl.createCell(i)
//            }
//            cell.getCellStyle().cloneStyleFrom(rl.getCell(i).getCellStyle())
//            println rl.getAt(i)
            cell.setCellValue(rl.getAt(i))
        }
    }

    List mergeUnitByRegxp(List items) {
        List rl = [] //总的队列
        List tl = [] //临时队列
        for (i in 0..items.size() - 1) {
            if (i == items.size() - 1) {
                if (tl.size() == 0) {
                    rl.push([items[i]])
                } else {
                    tl += items[i]
                    rl.push(tl)
                }
                break
            }
            String value = items[i]
            String value2 = items[i + 1]
            tl += value
            if (value.replaceAll(/\d+$/, "") != value2.replaceAll(/\d+$/, "")) {
                rl.push(tl)
                tl = []
            }
        }
        return rl
    }

    List getUnitArrange(int sn, int en) {
        def start = sn
        List rl = []
        for (i in sn..en) {
            if (i == en) {
                rl.push([start, i])
                break
            }
            def value = sheet.getRow(i).getAt(0)//默认第一列是排序值
            def value2 = sheet.getRow(i + 1).getAt(0)//默认第一列是排序值
            if (value == value2) {
                continue
            } else {
                rl.push([start, i])
                start = i + 1
            }
        }
        return rl
    }

    List getUnitArrange() {
        getUnitArrange(1, sheet.getLastRowNum())
    }

    Sheet getSheet(idx) {
//        返回指定sheet,同时将类成员指向该sheet
        if (idx ==~ /^\d+$/) sheet = workbook.getSheetAt(Integer.valueOf(idx))
        else sheet = workbook.getSheet(idx)
        return sheet
    }

    void appendSheet(idx, String name) {
        //基于既有的Sheet，增加
        if (name.size() >= 31) {
            name = name.substring(0, 31)
            println "sheetname [$name] size is greater than 31 we cast it to [$name]"
        }
        def index = workbook.getSheetIndex(getSheet(idx))
        workbook.cloneSheet(index)
        workbook.setSheetName(workbook.getNumberOfSheets() - 1, name)
        sheet = getSheet(name)
    }


    static void writeRowCh1() {
        Excelhelper metaExcel = new Excelhelper("D:\\toshiba\\4.~TD~\\p.cmcczj\\zmccsvn\\9.TD迁移\\3.前端\\前端bdi元数据.xlsx")
        metaExcel.setSheet("TASK_JOB_LIST")
        List bdiUnit = metaExcel.readRow(1, 2)
        metaExcel.writeRow(bdiUnit, 1, 3)

//        metaExcel.write(yjh.helper.StringHelper.s2ll(s),1,3)
//        metaExcel.export("D:\\1.xlsx")
    }

    static def batchExportTmp() {
        Excelhelper srcExcel = new Excelhelper("D:\\0.doc\\src.xlsx")
        Excelhelper tmpExcel = new Excelhelper("D:\\0.doc\\SXX_XX统数据字典v1.0.xls")
        for (i in 1..srcExcel.workbook.getNumberOfSheets() - 1) {
            def sheet = srcExcel.setSheet(i)
            def name = srcExcel.getSheet(i).getSheetName()
            List cols = srcExcel.readRow().toLL()
            println("start process:$name")
            List writeCols = []
            for (j in 2..cols.size() - 1) {
                def col = cols.get(j)
            }
            tmpExcel.appendSheet("template", "$name")
            tmpExcel.write(writeCols, 0, 1)
        }
        tmpExcel.export("D:\\instance.xls")
    }

    static def s34() {
        //FROM STANDARD COLS TO BOWZ COLS
        Excelhelper tmpExcel = new Excelhelper("D:\\0.doc\\p.bowz\\2017入仓\\SDK入仓\\SXX_XX统数据字典v1.0.xls")
        List cols = StringHelper.f2ll("D:\\0.doc\\p.bowz\\2017入仓\\LXD入仓\\导出2.dsv")
        List ar = StringHelper.getUnitArrange(cols, 0)
        ar.remove(0)
        ar.each {
            def (st, ed) = [it[0], it[1]]
            def name = cols[st][0]
            def cnName = cols[st][1]
            def writeCols = []
            def seq = 0
            for (i in st..ed) {
                seq++
                def colName = cols[i][2]
                def colCnName = cols[i][3]
                def colType = cols[i][4]
                def colLength = ""

                def colLengthMat = (colType =~ /\((.*?)\)/)
                if (colLengthMat.find()) {
                    colLength = colLengthMat.group(1)
                }
                colType = colType.replaceAll(/\(.*?\)/, "")

                def isPK = cols[i].size() > 5 ? cols[i][5] : ""
                def isNull = 'Y'

                writeCols.add([name, seq, colName, colCnName, colType, colLength, isPK, isNull])

            }
            tmpExcel.appendSheet("template", "$name")
            tmpExcel.write(writeCols, 0, 1)

        }
        tmpExcel.export("D:\\2.xls")
    }

    static def s33() {
        Excelhelper srcExcel = new Excelhelper("D:\\0.doc\\p.bowz\\2017入仓\\FSD入仓\\账务核心数据出仓FSD接口-V1.6.xlsx")
        Excelhelper tmpExcel = new Excelhelper("D:\\0.doc\\p.bowz\\2017入仓\\FSD入仓\\SXX_XX统数据字典v1.0.xls")
        for (i in 1..srcExcel.workbook.getNumberOfSheets() - 1) { // 遍历SHEET
            def sheet = srcExcel.setSheet(i)
            List cols = srcExcel.readRow().toLL()
            def name = cols[0][0]
            def cnName = srcExcel.getSheet(i).getSheetName()

            println("$name  $cnName")
            List writeCols = []
            for (j in 2..cols.size() - 1) {//构造WRITESHEET
                def col = cols.get(j)
                def seq = j - 1
                def colName = col[0]
                def colCnName = col[1]
                def colType = "UNKOWN"
                if (col[2].equals("S")) {
                    colType = 'VARCHAR'
                } else if (col[2].equals("B")) {
                    colType = 'DECIMAL'
                } else {
                    println("unknows type:" + col[2])
                }
                def colLength = col[3]
                def pk = ""
                if (col[4].contains("*")) {
                    pk = "PK"
                }
                def isNull = "Y"
                if (col[5].contains("*")) {
                    isNull = "N"
                }
                writeCols.add([name, seq, colName, colCnName, colType, colLength, pk, isNull])
            }
            tmpExcel.appendSheet("template", "$name")
            tmpExcel.write(writeCols, 0, 1)
        }
        tmpExcel.export("D:\\1.xls")
    }

    static void sample() {
        def a = [['A', '您好', 3], [4, 5, 6]]
        Excelhelper colsExcel = new Excelhelper("D:\\1.xlsx")
        colsExcel.write(a, 1, 1);
        colsExcel.export("D:\\2.xlsx");
    }

    static void bdi() {
        //S1 init data
        Excelhelper colsExcel = new Excelhelper("D:\\0.doc\\p.cmcczj\\zmccsvn\\9.TD迁移\\3.前端\\迁移columns.xls")
        Excelhelper metaExcel = new Excelhelper("D:\\0.doc\\p.cmcczj\\zmccsvn\\9.TD迁移\\3.前端\\前端bdi元数据.xlsx")

        Excelhelper bdiExcel = new Excelhelper("D:\\0.doc\\p.cmcczj\\zmccsvn\\9.TD迁移\\bdi\\bdiGroovy.xlsx")
        bdiExcel.setSheet("需求清单")
        metaExcel.setSheet("META")

        def bdiUnitRow = bdiExcel.readRow(3, 4)//read 2-3 line as template
        def colSheetName = "tdfin_hive_db"
        def metalist = metaExcel.readRow(84, 117) //从第二行开始遍历

//        def bdiUnitRow = bdiExcel.readRow(1,2)//read 2-3 line as template
//        def colSheetName = "PD_CSI_PORTAL"
//        def metalist = metaExcel.readRow(71,82) //从第二行开始遍历
//        def bdiUnitRow = bdiExcel.readRow(1,2)//read 2-3 line as template

//        def colSheetName = "CSI"
//        def metalist = metaExcel.readRow(119,120) //从第二行开始遍历
//        def bdiUnitRow = bdiExcel.readRow(5,6)//read 2-3 line as template


        colsExcel.setSheet(colSheetName)
        def columnlist = colsExcel.readRow("表英文名", null)
//        println columnlist.toLL()
        def metash = [:]
        metalist.eachWithIndex { it, i ->
            if (it == null || it.getAt(1).equals("")) {
                println "line $i is not demmand metaline"
                return
            }

            def srctable = it.getAt(4).toLowerCase()
            def metavalue = [it.getAt(1), it.getAt(3), it.getAt(5), it.getAt(7), it.getAt(8)] //(cnName,math,descTable,deleteKey,keyType)
            metash.put(srctable, metavalue)
        }
        //END OF S1

        //S2 make excel

        def template = StringHelper.ll2s(bdiUnitRow.toLL())
        def num = 0
        columnlist.getRowUnitArrange().each { it -> //遍历COLUMNSLIST
//BODY
            def count = 0 //字段计数器
            List bdilist = []
            def (sn, en) = [it[0], it[1]]
            def srctable = columnlist[sn][0].toLowerCase()
            if (!metash.get(srctable)) {
                println "columns.xls table [$srctable] is not find in meta.xls" //如果没有发现则跳过
                return
            }
            def (cnName, math, descTable, deleteKey, keyType) = metash.get(srctable)
            for (i in sn..en) {
                count++
                def (col, colcn, hivetype, oracletype, tdtype) = [columnlist[i][2], columnlist[i][3], columnlist[i][6], columnlist[i][4], columnlist[i][5]]
                //4:oracle, 5:td , 6:hive
                bdilist.push([count, col, colcn, oracletype, col, colcn, hivetype])
            }
            bdiExcel.appendSheet(2, srctable) //报错的原因是sheet太长
            bdiExcel.write(srctable, 2, 1)
            bdiExcel.write(descTable, 5, 1)
            bdiExcel.write(bdilist, 0, 3)
//HEAD
            //读取unit单元后先转成 String 进行批量替换，再写入格式和文本:
            def where = ""
            //如果没有DELETE_KEY或KEY_TYPE，
            def mode = "全量"
            if (math.equals("F0")) {
                if (deleteKey.equals("BILLMON") || deleteKey.equals("bill_mon") || deleteKey.equals("p_mon")) {
                    where = "where $deleteKey = YYYYMM"
                } else if (deleteKey.equals("DATA_DATE") || deleteKey.equals("DATA_DT")) {
                    where = "where $deleteKey ='FmtTime(#flow.startDataTime#,'yyyy-MM-dd 00:00:00')'"
                } else if (deleteKey.equals("p_day")) {
                    where = "where $deleteKey = 'YYYYMMDD'"
                }
                mode = "增量"
            }
            num++
            def instance = template
            instance = instance.replaceAll("\\\$num", "" + (num))
            instance = instance.replaceAll("\\\$cnName", cnName)
            instance = instance.replaceAll("\\\$where", where)
            instance = instance.replaceAll("\\\$descTable", descTable)
            instance = instance.replaceAll("\\\$srctable", srctable)
            instance = instance.replaceAll("\\\$mode", mode)
            bdiExcel.setSheet("需求清单")
            bdiExcel.write(StringHelper.s2ll(instance), 0, 2 * (num - 1) + 1)
        }

        bdiExcel.export("D:\\${colSheetName}.xlsx")
    }

//    private static Excelhelper colsExcel = new Excelhelper("D:\\toshiba\\4.~TD~\\p.cmcczj\\zmccsvn\\9.TD迁移\\3.前端\\迁移columns.xls")
//    public static Excelhelper getColsExcelInstance() {
//        return colsExcel;
//    }
////
//    public static Excelhelper reloadInstance() {
//        colsExcel = new Excelhelper("D:\\0.doc\\p.cmcczj\\zmccsvn\\9.TD迁移\\3.前端\\迁移columns.xls")
//    }
    static def readDbtab(def dbtab) {
        def (db, tab) = dbtab.split("\\.")
        Excelhelper colsExcel = getColsExcelInstance()
        colsExcel.setSheet(db)
        colsExcel.readRow("表英文名", [tab]).toLL()
    }

    static main(args) {
//        s34()
        sample()
    }
}
