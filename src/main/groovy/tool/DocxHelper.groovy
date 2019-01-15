package tool


import org.docx4j.Docx4J
import org.docx4j.model.datastorage.migration.VariablePrepare;
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
        VariablePrepare.prepare(wordMLPackage);
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

        for(i in 1..8){
            mappings["item2${i}"] = "测试${i}"
        }

        mappings += [cnname  : "中国人"
                ,name2  : "中国人"
                    , enname: "english"


        ]

        new DocxHelper("${System.getProperty("user.dir")}/data/tmp2.docx")
                .replace(mappings)
                .saveAs("${System.getProperty("user.dir")}/data/out/0115.docx")
    }


}

