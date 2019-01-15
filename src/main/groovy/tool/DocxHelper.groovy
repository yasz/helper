package tool


import org.docx4j.Docx4J;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;


/**

 * 收入证明模板示例

 */
class DocxHelper {

    WordprocessingMLPackage wordMLPackage
    MainDocumentPart documentPart

    DocxHelper(def path) {
        wordMLPackage = WordprocessingMLPackage.load(new java.io.File(path))
        documentPart = wordMLPackage.getMainDocumentPart()
    }

    def saveAs(def outputPath) {
        Docx4J.save(wordMLPackage, new File(outputPath))
        return this
    }

    def replace(def mappings) {
        documentPart.variableReplace(mappings)
        return this
    }

    static void main(String[] args) throws Exception {

        HashMap<String, String> mappings = new HashMap<String, String>();

        mappings.put("cnname", "马参军");
        mappings.put("name", "马参军");

        mappings.put("years", "5");

        mappings.put("post", "攻城狮");

        mappings.put("money", "25,000.00");

        mappings.put("moneyChinese", "二万五年里");

        mappings.put("address", "天宫一号天宫一号天宫一号天宫一号");

        mappings.put("telephone", "188188188188");

        mappings.put("year", "2018");

        mappings.put("month", "09");

        mappings.put("date", "11");

        new DocxHelper("${System.getProperty("user.dir")}/data/收入证明_template.docx")
                .replace(mappings)
                .saveAs("${System.getProperty("user.dir")}/data/out/收入证明.docx")
    }


}

