package tool;

import org.apache.poi.xwpf.usermodel.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class CheckDocumentFonts {
    public static void main(String[] args) {
        String filePath = "path/to/your/document.docx";

        try (FileInputStream fis = new FileInputStream(filePath);
             XWPFDocument document = new XWPFDocument(fis)) {

            // 获取文档中的所有段落
            List<XWPFParagraph> paragraphs = document.getParagraphs();

            // 遍历段落
            for (XWPFParagraph paragraph : paragraphs) {
                // 获取段落中的所有运行元素
                List<XWPFRun> runs = paragraph.getRuns();

                // 遍历运行元素
                for (XWPFRun run : runs) {
                    // 获取运行元素的字体名称
                    String fontName = run.getFontFamily();

                    // 检查字体是否在本地系统中可用
                    if (!isFontInstalled(fontName)) {
                        System.out.println("字体 '" + fontName + "' 在本地系统中未安装。");
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 检查字体是否在本地系统中可用
    private static boolean isFontInstalled(String fontName) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fontNames = ge.getAvailableFontFamilyNames();
        for (String name : fontNames) {
            if (name.equalsIgnoreCase(fontName)) {
                return true;
            }
        }
        return false;
    }
}
