package tool

import groovy.io.FileType
import org.docx4j.Docx4J
import org.docx4j.convert.out.Output
import org.docx4j.model.datastorage.migration.VariablePrepare;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart
import org.docx4j.wml.ContentAccessor
import org.docx4j.wml.Text

import javax.xml.bind.JAXBElement;


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

    def saveAsOutputStream(OutputStream op) {
        Docx4J.save(wordMLPackage,op)
        return op
    }

    def replace(def mappings) {
        documentPart.variableReplace(mappings)
        return this
    }


    def read() {
        String textNodesXPath = "//w:t";
        List<Object> textNodes = documentPart.getJAXBNodesViaXPath(textNodesXPath, true);
        for (Object obj : textNodes) {
            Text text = (Text) ((JAXBElement) obj).getValue();
            String textValue = text.getValue();
            System.out.println(textValue);
        }
    }

    static void main(String[] args) throws Exception {

        def dir = "D:\\3.ws\\2.code\\ruianva.github.io\\5.assistant\\report\\初中毕业生综合素质评价报告"
        new File(dir).eachFileRecurse(FileType.FILES) { file ->
            if (file.toString() ==~ /.*docx$/) {
                new DocxHelper(file.toString()).read()
                println("分割线")

            }
        }

//        new DocxHelper("${dir}/陈可悦.docx").read()

        HashMap<String, String> mappings = new HashMap<String, String>();

        for (i in 1..8) {
            mappings["item${i}"] = "测试${i}"
        }
        mappings["cnname"] = "楼主"
        mappings["enname"] = "louzhu"

//        new DocxHelper("${System.getProperty("user.dir")}/data/卢佳俊-降1级.docx")
//                .replace(mappings)
//                .saveAs("${System.getProperty("user.dir")}/data/out/0115.docx")


    }


}

