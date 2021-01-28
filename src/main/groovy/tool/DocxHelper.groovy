package tool

import groovy.io.FileType
import org.docx4j.Docx4J
import org.docx4j.convert.out.Output
import org.docx4j.model.datastorage.migration.VariablePrepare;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart
import org.docx4j.wml.ContentAccessor
import org.docx4j.wml.Tbl
import org.docx4j.wml.Tc
import org.docx4j.wml.Text
import org.docx4j.wml.Tr

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

    def deleteCol(String keyword){
        String textNodesXPath = "//w:t";
        List<Object> textNodes = documentPart.getJAXBNodesViaXPath(textNodesXPath, false);
        for (Object o1 : textNodes) {
            org.docx4j.wml.Text o2= o1.value
            println(o2.getValue())
            if(((org.docx4j.wml.Text) o2).getValue().contains(keyword)) {
                // if your text contains "WhatYouWant" then...
                Object o4 =((org.docx4j.wml.Text)o2).getParent();
                //gets R
                Object o5 = ((org.docx4j.wml.R) o4).getParent();
                // gets P
                Tc o6 = ((org.docx4j.wml.P) o5).getParent();
                Tr o7 = ((org.docx4j.wml.Tc) o6).getParent();
                Tbl o8 = ((org.docx4j.wml.Tr) o7).getParent();
                // gets SdtElement
                //判断自己是第几个位置
                int elementIndex = o7.content.findIndexOf{JAXBElement it->o6==it.value}
                o8.content.each {
                    println(it.content.size() )
                    println(o7.content.size())
                    if(it.content.size() >= o7.content.size()){
                        it.content.remove(elementIndex)
                    }
                }
//                o7.content.remove(o6)

            }
        }
        return this
    }

    def deleteRow(String keyword){
        String textNodesXPath = "//w:t";
        List<Object> textNodes = documentPart.getJAXBNodesViaXPath(textNodesXPath, false);
        for (Object o1 : textNodes) {
            org.docx4j.wml.Text o2= o1.value
            println(o2.getValue())
            if(((org.docx4j.wml.Text) o2).getValue().contains(keyword)) {
                println("got!!!"+ keyword)
                // if your text contains "WhatYouWant" then...
                Object o4 =((org.docx4j.wml.Text)o2).getParent();
                //gets R
                Object o5 = ((org.docx4j.wml.R) o4).getParent();
                // gets P
                Object o6 = ((org.docx4j.wml.P) o5).getParent();
                Object o7 = ((org.docx4j.wml.Tc) o6).getParent();
                Object o8 = ((org.docx4j.wml.Tr) o7).getParent();
                // gets SdtElement
                o8.content.remove(o7)
                // now you remove your P (paragraph)
            }
        }
        return this
    }
    static void main(String[] args) throws Exception {

        def dir = "D:\\3.ws\\1.idea\\helper\\dat\\tmp\\tmpg9-3.docx"
        def doc = new DocxHelper(dir)
        doc.deleteCol("日常").deleteCol("考试").deleteCol("年度总评")

//        doc.deleteCol("日常")

        doc.saveAs("a.docx")


//        HashMap<String, String> mappings = new HashMap<String, String>("s05");




    }


}

