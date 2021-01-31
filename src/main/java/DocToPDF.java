import org.docx4j.Docx4J;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

public class DocToPDF {

    public static void main(String[] args) {
        
        try {
            InputStream templateInputStream = new FileInputStream("D:\\3.ws\\1.idea\\helper\\out\\1v.docx");
            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(templateInputStream);
            MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();

            String outputfilepath = "D:\\3.ws\\1.idea\\helper\\out\\1v.docx.PDF";
            FileOutputStream os = new FileOutputStream(outputfilepath);
            Docx4J.toPDF(wordMLPackage,os);
            os.flush();
            os.close();
        } catch (Throwable e) {

            e.printStackTrace();
        } 
    }

}