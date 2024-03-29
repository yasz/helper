package yjh

import common.AsposeRegister
import tool.DocxHelper
import tool.YamlHelper
import yjh.helper.Excelhelper
import yjh.helper.StringHelper

import java.text.SimpleDateFormat

class Easyreport {
    static{
        AsposeRegister.registerAll()
    }
    static String yamlPath = "./data/config.yml"
    static boolean replace(){
        HashMap hs=YamlHelper.file2hash(this.yamlPath)
        String dataPath = hs.dataXlsx
        String tmpPath = hs.tmpDocx
        //读取2维数组
        List dataList = new Excelhelper(dataPath).read()
        List title=dataList.remove(0)
        ArrayList<HashMap> dataHashList = StringHelper.array2hash(title,dataList)
        def dir = "output"+new SimpleDateFormat("MMdd-HHmmss").format(new Date())
        new File("./${dir}").mkdirs();
        dataHashList.each { hash->
            new DocxHelper(tmpPath).replace(hash).saveAs("${dir}/${hash.文件名}.docx")
            println("导出${hash.文件名}.docx完成")
        }
        return true
    }
    static void main(String[] args) {
        if(!new File(this.yamlPath).exists()){
            print("当前路径下无法找到config.yml配置文件")
            return
        }
        while(1){
            replace()
            print('继续操作?(按回车继续，任意键退出)')
            Scanner sc=new Scanner(System.in);
            String flag=sc.nextLine()

            if(flag.length()>0){break}
        }
        return
    }
}
