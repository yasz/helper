package yjh.helper


import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFParagraph
import org.apache.poi.xwpf.usermodel.XWPFRun

import java.nio.file.Files
import java.nio.file.Paths
import java.util.regex.Matcher

/**
 * Created by Peter.Yang on 7/4/2017.
 */
class DocHelper {

    /**
     * 用一个docx文档作为模板，然后替换其中的内容，再写入目标文档中。
     * @throws Exception
     */

    //英文 中文 数学 科学 社会 体育 美术 音乐

    static String dataPath = "${System.getProperty("user.dir")}/data"

    public static void main(String[] args) throws IOException {
//        va1()
    }



    static public void testTemplateWrite() throws Exception {


        def name = "杨家昊"
        def params = [
                "DATE"  : "2017-07-04",
                "NAME"  : name,
                "CITY"  : "杭州",
                "SKILL1": "土豆烧鸡块",
                "SKILL2": "钢琴即兴伴奏",
                "SKILL3": "perl"
        ]

        String filePath = "D:\\kuaipan\\2.doc\\career\\0.cv\\cv.docx"
        XWPFDocument doc = new XWPFDocument(new FileInputStream(filePath))

        doc.getParagraphs().each {
            this.replaceInPara(it, params)
        }
        this.replaceInTable(doc, params);
        OutputStream os = new FileOutputStream("D:\\${name}.docx");
        doc.write(os);
        this.close(os);
    }


    /**
     * 替换段落里面的变量
     * @param para 要替换的文档
     * @param params 参数
     */
    static private void replaceInPara(XWPFParagraph para, LinkedHashMap params) {
        def runs = para.getRuns()
        if (runs.size() <= 0) return
        for (i in 0..runs.size() - 1) {
            def run = runs.get(i)
            def runtext = run.text()
            Matcher m = runtext =~ /(\S+)/
            if (m.count > 0) {
                m.findAll {
                    def key = it[1]
                    def value = params.get(key)
                    if (value) {
                        def newText = runtext.replaceAll(it[0], value)
                        XWPFRun repRun = para.insertNewRun(i)
                        repRun.setText(newText)
                        repRun.setFontFamily(run.getFontFamily())
                        repRun.setBold(run.isBold())
                        repRun.setItalic(run.isItalic())
                        repRun.setUnderline(run.getUnderline())
                        repRun.setColor(run.getColor())
                        repRun.setTextPosition(run.getTextPosition())
                        para.removeRun(i + 1)
                    }

                }
            }
        }
    }

    static private void replaceInTable(XWPFDocument doc, LinkedHashMap params) {
        doc.getTables().each {
            it.getRows().each {
                it.getTableCells().each {
                    it.getParagraphs().each {
                        this.replaceInPara(it, params)
                    }
                }
            }
        }
    }

    static private void close(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static private void close(OutputStream os) {
        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}