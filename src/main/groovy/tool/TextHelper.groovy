package tool

import yjh.helper.StringHelper

/**
 * Created by Peter.Yang on 2019/5/17.
 * 基于已导出的纯文本进行二次加工
 */
class TextHelper {

    static String splitLine = """******************"""
    static void printToFile(path,String str){
        File file = new File(path);
        file.delete();
        file.createNewFile();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file), "utf-8"));
        writer.write(str);
        writer.close();

    }
    static String unit1(text) {
//        String text =""""""

        String subject = "英文 中文 数学 科学 社会 体育 美术 音乐"
        String split = "教师评语|附录"

        String comment = "中文|英文|语言|数学|科学|社会|体育|美术|音乐|班主任"
        def para = text.split(/教师评语|附录/)
//        println para.size()
        if (para.size() != 3) {
//            throw new Exception("para number error")
            return
        }

        def para1 = para[0]
        def para2 = para[1]

        def name = (para1 =~ /姓名\r\n：\r\n(.*?)\r\n/)[0][1]
        print(name+"\t")

        (para1 =~ /[\d\.%]+/).each {
            if (!(it =~ /%/))
                if (!(it =~ /2016|2017/))
                    print("$it\t")
        }

        comment = comment.split("\\|").join("：|")
//        println(comment)
        (para2.split(/${comment}/)).each { it ->
            def output = it.toString()
            output = output.replaceAll(/：/, " ")
            output = output.replaceAll(/\r\n/, " ")
            output = output.replaceAll(/\t/, " ")
            print("$output\t")
        }
        println()

//        println(para2)

    }

    static void main(String[] args) {
        print(CalHelper.vascore2(79.5))
        print(CalHelper.vascore2(80))


    }
}
