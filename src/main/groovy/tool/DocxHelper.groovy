package tool

import com.aspose.words.Document
import com.aspose.words.SaveFormat
import com.aspose.words.SaveOptions
import com.plutext.merge.BlockRange
import com.plutext.merge.DocumentBuilder
import common.Const
import groovy.io.FileType
import org.docx4j.Docx4J
import org.docx4j.Docx4jProperties
import org.docx4j.convert.out.Output
import org.docx4j.model.datastorage.migration.VariablePrepare;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart
import org.docx4j.toc.TocGenerator
import org.docx4j.wml.ContentAccessor
import org.docx4j.wml.Tbl
import org.docx4j.wml.Tc
import org.docx4j.wml.Text
import org.docx4j.wml.Tr

import javax.xml.bind.JAXBElement


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

    static void toPDF(String inPath, String outPath) throws Exception {
        new Document(inPath).save(outPath);
    }

    static String newlineToBreakHack(String r) {

        StringTokenizer st = new StringTokenizer(r, "\n\r\f");
        // tokenize on the newline character, the carriage-return character, and the form-feed character
        StringBuilder sb = new StringBuilder()

        boolean firsttoken = true;
        while (st.hasMoreTokens()) {
            String line = (String) st.nextToken();
            if (firsttoken) {
                firsttoken = false;
            } else {
                sb.append("</w:t><w:br/><w:t>")
            }
            sb.append(line);
        }
        return sb.toString();
    }
    static merge(List<String> files, String outPath) {

        List<BlockRange> blockRanges = new ArrayList<BlockRange>();
        files.eachWithIndex { File file, i ->

            BlockRange block = new BlockRange(WordprocessingMLPackage.load(
                    file));
            blockRanges.add(block);
            block.setStyleHandler(BlockRange.StyleHandler.RENAME_RETAIN);
            block.setNumberingHandler(BlockRange.NumberingHandler.ADD_NEW_LIST);
            block.setRestartPageNumbering(false);
            block.setHeaderBehaviour(BlockRange.HfBehaviour.DEFAULT);
            block.setFooterBehaviour(BlockRange.HfBehaviour.DEFAULT);
            block.setSectionBreakBefore(BlockRange.SectionBreakBefore.NEXT_PAGE);

        }

        // Perform the actual merge
        DocumentBuilder documentBuilder = new DocumentBuilder();
        WordprocessingMLPackage wordMLPackage = documentBuilder.buildOpenDocument(blockRanges);
        Docx4J.save(wordMLPackage, new File(outPath));
    }

    def saveAs(String outputPath) {
        Docx4J.save(wordMLPackage, new File(outputPath), Docx4J.FLAG_NONE)
        return this
    }

    def saveAsPDF(String outputPath){
        OutputStream os = new ByteArrayOutputStream()
        Docx4J.save(wordMLPackage, os)
        new Document(new ByteArrayInputStream(os.toByteArray())).save(outputPath)
        return
    }
    def saveAsPDFOutputStream(OutputStream pdfOs){
        OutputStream docOs = new ByteArrayOutputStream()
        Docx4J.save(wordMLPackage, docOs)
        new Document(new ByteArrayInputStream(docOs.toByteArray())).save(pdfOs, SaveFormat.PDF)
        return
    }
    def saveAsOutputStream(OutputStream os) {
        Docx4J.save(wordMLPackage, os)
        return os
    }


    int getPages() {

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

        }
    }

    def deleteCol(String keyword) {
        String textNodesXPath = "//w:t";
        List<Object> textNodes = documentPart.getJAXBNodesViaXPath(textNodesXPath, false);
        for (Object o1 : textNodes) {
            org.docx4j.wml.Text o2 = o1.value
            if (((org.docx4j.wml.Text) o2).getValue().contains(keyword)) {
                // if your text contains "WhatYouWant" then...
                Object o4 = ((org.docx4j.wml.Text) o2).getParent();
                //gets R
                Object o5 = ((org.docx4j.wml.R) o4).getParent();
                // gets P
                Tc o6 = ((org.docx4j.wml.P) o5).getParent();
                Tr o7 = ((org.docx4j.wml.Tc) o6).getParent();
                Tbl o8 = ((org.docx4j.wml.Tr) o7).getParent();
                // gets SdtElement
                //判断自己是第几个位置
                int elementIndex = o7.content.findIndexOf { JAXBElement it -> o6 == it.value }
                o8.content.each {

                    if (it.content.size() >= o7.content.size()) {
                        it.content.remove(elementIndex)
                    }
                }
//                o7.content.remove(o6)

            }
        }
        return this
    }

    def deleteRow(String keyword) {
        String textNodesXPath = "//w:t";
        List<Object> textNodes = documentPart.getJAXBNodesViaXPath(textNodesXPath, false);
        for (Object o1 : textNodes) {
            org.docx4j.wml.Text o2 = o1.value

            if (((org.docx4j.wml.Text) o2).getValue().contains(keyword)) {

                // if your text contains "WhatYouWant" then...
                Object o4 = ((org.docx4j.wml.Text) o2).getParent();
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


    }


}

