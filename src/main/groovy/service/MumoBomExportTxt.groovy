package service

import com.alibaba.fastjson.JSON
import com.aspose.cells.Workbook
import com.aspose.cells.Worksheet
import com.aspose.cells.WorksheetCollection
import common.AsposeRegister
import groovy.io.FileType

import utils.DBHelper
import yjh.helper.Excelhelper

import java.text.SimpleDateFormat

class MumoBomExportTxt {


    public static void main(String[] args) throws Exception {
        def dbhelper = new DBHelper("sqlite2")
        def res = dbhelper.query("select * from bom_current")
        res.each { row ->
            def bomId = row.bom_id
            def bomName = row.bom_name
            def bomCategory = row.bom_category
            def bomDataJson = row.bom_data
            // 遍历 JSON 数组
            JSON.parse(bomDataJson).each { jsonObj ->

                println("${bomId}\t${bomName}\t${bomCategory}\t${jsonObj.板件名称}")
            }
        }

    }

    public static void main3(String[] args) throws Exception {
        // 加载 Excel 文件
        AsposeRegister.registerExcel_v_22_6()


        def workDir = "C:\\Users\\yangj\\Desktop\\mumo_bom"
        def excludeDirectory = "C:\\Users\\yangj\\Desktop\\mumo_bom\\▲技术部资料"
        def excludeDirectory2 = "C:\\Users\\yangj\\Desktop\\mumo_bom\\.sync"

        new File(workDir).eachFileRecurse(FileType.FILES) { file ->
            def filePath = file.getPath().replace(workDir, '')
            if (!(file.name.endsWith(".xlsx") && file.name.contains("料单"))) return
            if (!(file.name.endsWith(".xlsx") && file.name.contains("料单"))) return

            if (file.getParent().contains(excludeDirectory) ){
                return
            }
            if (file.getParent().contains("sync") ){
                return
            }


            //先拆分，然偶

            if (file.name.startsWith("~")) return
            println("cp -r --parent \""+file.getPath()+"")
//            def sourceWorkbook  = new Workbook(file.getPath())
//            // 获取原始工作簿中的所有工作表
//
//            WorksheetCollection sourceSheets = sourceWorkbook .getWorksheets();
//
//            for (int i = 0; i < sourceSheets.getCount(); i++) {
//                def sourceSheet = sourceSheets.get(i)
//                if (["调整内容", "修改记录", "Sheet1", "Sheet2", "WpsReserved_CellImgList"].contains(sourceSheet.getName())) continue
//                Workbook targetWorkbook = new Workbook();
//                Worksheet targetSheet = targetWorkbook.getWorksheets().get(targetWorkbook.getWorksheets().add());
//                targetSheet.copy(sourceSheet);
//                targetSheet.name = "料单"
//                targetWorkbook.getWorksheets().removeAt("Sheet1");
//                Worksheet newSheet = targetWorkbook.getWorksheets().add("调整内容");
//                // 从原始工作簿中复制工作表到新工作簿
//                def bomName = "${file.name.replaceAll(/\.[^\.]*$/, '').replaceAll(/料单/, '')}" + (sourceSheet.getName().toLowerCase().equals("sheet") ? "" : "(${sourceSheet.getName()})")
//                def dir = "./生产料单${file.getParent().minus(workDir)}"
//                new File(dir).mkdirs();
//                targetWorkbook.save("${dir}/${bomName}料单.xlsx");
//            }
        }

    }


}
