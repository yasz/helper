package yjh.helper

import com.alibaba.fastjson.JSON
import com.aspose.cells.AutoFitterOptions
import com.aspose.cells.BorderType
import com.aspose.cells.Cell
import com.aspose.cells.CellBorderType
import com.aspose.cells.Cells
import com.aspose.cells.Color
import com.aspose.cells.SaveFormat
import com.aspose.cells.Style
import com.aspose.cells.StyleFlag
import com.aspose.cells.Workbook
import tool.DBHelper


class Excelhelper2 {

    Excelhelper2(InputStream fis) {

    }

    static s1(String title, String contents,OutputStream os) {
        //智能分析

        float top = 90
        float bottom = 60
        def level = "60,70,80,90"
        Workbook excel = new Workbook("${System.getProperty("user.dir")}/dat/template/tmp.xlsx");
        def ll = StringHelper.s2ll(contents)
        if(ll.sum{
            def rs = 0
            try {
                rs = Double.parseDouble(it[1])
            } catch (e) {
            }
            rs
        }/ll.size()<8){
            top=6
            bottom=4
            level="4,5,6,7"
        }

        Object[][] data = StringHelper.s2Oo(contents)
        def sheet = excel.getWorksheets().get("class")

        sheet.cells.importTwoDimensionArray(data, 2, 1, true) //写数据
        sheet.cells.importArray([title] as String[], 0, 0, true)

        //设置格式
        def rg = sheet.cells.createRange(2, 0, data.length, 5)
        Style style = excel.createStyle()
        style.setBorder(BorderType.TOP_BORDER, CellBorderType.THIN, Color.getBlack());
        style.setBorder(BorderType.BOTTOM_BORDER, CellBorderType.THIN, Color.getBlack());
        StyleFlag flag = new StyleFlag()
        flag.borders = true;
        flag.font = true;
        rg.applyStyle(style, flag)

        //对 <4以及7分标注
        style.font.bold = true
        data.eachWithIndex { es, int i ->
            def cell = sheet.cells.createRange(2 + i, 2, 1, 1);//第一行第一列单元格
            def formulaCell = sheet.cells.get(2+i,3)
            String formula = """=IF(C${3+i},LOOKUP(C${3+i},{-1,${level}},{"不及格","及格","中","良","优"}),"")"""
            println(formula)
            formulaCell.setFormula(formula)
            try {
                def f = Float.parseFloat(es[1])
                if (f > top) {
//                    println("${es[0]} is 7")
                    style.font.color = Color.deepSkyBlue
                    cell.applyStyle(style, flag);
                } else if (f < bottom) {
                    style.font.color = Color.deepPink
                    cell.applyStyle(style, flag);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        excel.calculateFormula()
        excel.save("output.xlsx");
        excel.save(os, SaveFormat.PDF)
    }

    static main(args) {
        def numbers = [10, 20, 30, 40, 50]
        assert numbers.average() == 30

        Excelhelper2.s1("test","a\t30\r\nb\t50",new FileOutputStream("test.pdf"))
        return
        Excelhelper2.s1("声现象单元测", """蔡斯帖\t3
黄宣迪\t6
胡谦铄\t5
胡一卓\t6.5
杨阳涵\t6
李百慧\t5
李其朔\t2
李诗怡\t2
李雅博\t5.5
潘刈禾\t5.5
温忻铼\t4
吴翊豪\t4.5
周晟颢\t6.5
朱王冠\t2
余初瑾\t3
郑颖\t5
傅芯雅\t3
周烜赫\t
""",new FileOutputStream("声现象单元测.pdf"))


//        def rs = DBHelper.query("""select subject,string_agg(cnname||chr(9)||VALUE1 , chr(10)) from (
//select t1.subject,t3.cnname,CASE WHEN t1.value1='0' then '' else t1.value1 end  from (select * from report where sem='211' and key1='finalexam')t1
//left join va3 t2 on t1.vano=t2.vano
//and t1.sem=t2.sem
//left join va1 t3 on t1.vano=t3.vano
//where t2.classname='9v'
//order by 1,classno
//)t4
//WHERE SUBJECT <>'班主任'
//group by 1;
//""", DBHelper.instance.conn)
//        rs.each { it ->
//            def testName = "2020-2021-1学期${it[0]}期末成绩统计"
//            Excelhelper2.s1(testName, it[1])
//        }

    }
}
