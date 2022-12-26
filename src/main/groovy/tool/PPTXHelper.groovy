package tool

import com.aspose.slides.Presentation
import com.aspose.slides.SaveFormat
import org.docx4j.XmlUtils
import org.docx4j.openpackaging.packages.OpcPackage
import org.docx4j.openpackaging.packages.PresentationMLPackage
import org.docx4j.openpackaging.parts.PartName
import org.docx4j.openpackaging.parts.PresentationML.MainPresentationPart
import org.docx4j.openpackaging.parts.PresentationML.SlideLayoutPart
import org.docx4j.openpackaging.parts.PresentationML.SlidePart
import org.pptx4j.jaxb.Context
import org.pptx4j.pml.Sld

/**
 * Created by Peter.Yang on 7/27/2017.
 */

class PPTXHelper {
    PresentationMLPackage presentationMLPackage
    MainPresentationPart pp
    SlideLayoutPart layoutPart
    String defaultTmpPath = System.getProperty("user.dir")+"/dat/template/child2.pptx"
    PPTXHelper(){
        /**
         * created by yang on 11:22 2018/1/16.
         * describtion:
         * @param :默认基于child2.pptx

         */
        init(defaultTmpPath)
    }
    def saveAsPDFOutputStream(OutputStream pdfOs){
        OutputStream docOs = new ByteArrayOutputStream()
        presentationMLPackage.save(docOs)
        new Presentation(new ByteArrayInputStream(docOs.toByteArray())).save(pdfOs, SaveFormat.Pdf)
        return
    }

    def saveAsOutputStream(OutputStream os){
//        OutputStream docOs = new ByteArrayOutputStream()
//        presentationMLPackage.save(docOs)
//        new Presentation(new ByteArrayInputStream(docOs.toByteArray())).save(os, SaveFormat.Pptx)
        presentationMLPackage.save(os)

        return
    }

    PPTXHelper(String filePath) {
        /**
         * created by yang on 11:21 2018/1/16.
         * describtion:
         * @param filePath:基于何种模板路径

         */
        init(filePath)

    }

    void init(String filePath){
        if (filePath.endsWith(".ppt")) {
            throw new Exception("不支持此格式，请转换为pptx格式");
        } else if (filePath.endsWith(".pptx")) {
            presentationMLPackage=(PresentationMLPackage) OpcPackage.load(new java.io.File(filePath))
            pp = (MainPresentationPart)presentationMLPackage.getParts().getParts().get(new PartName("/ppt/presentation.xml"))
            layoutPart = (SlideLayoutPart)presentationMLPackage.getParts().getParts().get(new PartName("/ppt/slideLayouts/slideLayout1.xml"));//基于哪个master页
        }
    }
    void copySlide(int num) {
        /**
         * created by yang on 17:06 2017/12/29.
         * describtion:拷贝一页SLIDE默认到最后一页
         * @param num:拷贝SLIDE页码，从0开始
         */

        SlidePart newSlide = new SlidePart();//new PartName("/ppt/slides/slide1.xml")
        pp.addSlide(newSlide)
        SlidePart copySlide = pp.getSlide(num);
        

        newSlide.addTargetPart(copySlide.getSlideLayoutPart())//模板
        String content = copySlide.getXML()
        newSlide.addTargetPart(copySlide.getSlideLayoutPart());
        newSlide.setJaxbElement((Sld)XmlUtils.unmarshalString(content,copySlide.getJAXBContext()))

    }
    void removeSlide(int num){
        /**
         * created by yang on 11:04 2017/12/30.
         * describtion:删除一页PPT
         * @param num:remove slide start with 0, default is the final page 页码，默认删除最后一页

         */
        pp.removeSlide(num)
    }

    void removeSlide(){
        removeSlide(pp.getSlideCount()-1)
    }

    void writeTmplate(String title, String lyric, String order, int num) {
        /**
         * created by yang on 22:28 2017/12/28.
         * describtion:基于已经copy好的模板,对固定页替换title lyric[也是控制的]
         * @param title :
         * @param lyrics :one page
         * @param num :rewrite slide start with 0, default is the final page
         */
        num = num ? num : pp.getSlideCount()-1
        SlidePart slide = pp.getSlide(num)
        String content = slide.getXML()
        content = content.replaceAll(/#title/, title).replaceAll(/#lyric/, lyric).replaceAll (/#order/, order)
        slide.setJaxbElement((Sld)XmlUtils.unmarshalString(content,Context.jcPML))
    }

    void writeTmplate(String title, String lyric, String order){
        writeTmplate(title,lyric,order,pp.getSlideCount()-1)
    }
    boolean readSlideText() {
        /**
         * created by yang on 17:37 2017/12/28.
         * describtion:读取一份文档，并将每个模块的文本读取出来
         * @param slideShow :
         */
        return true
    }

    String checkTmplate() {
        /**
         * created by yang on 17:21 2017/12/28.
         * describtion:检查用户上传PPT模板是否合规,合规的规则是
         *
         */
        return true
    }
    void export(String path) {
        /**
         * created by yang on 21:59 2017/12/28.
         * describtion:导出文件
         * @param path :文件路径
         */
        presentationMLPackage.save(new java.io.File(path))

    }

    void parseUnit1(String title,String lyrics){
        /**
         * created by yang on 17:28 2017/12/29.
         * describtion: 基于 single slide ppt 导出一首
         * @param title:
         * @param lyrics: lyric
         */
        def lyricList = lyrics.split(/\r\n\r\n/)
        if (lyricList.size()==1){
            lyricList = lyrics.split(/\n\n/)
        }
        def max = lyricList.size()
        lyricList.eachWithIndex{ String lyric, int i ->
            copySlide(0)
            writeTmplate(title,lyric,"${i+1}/${max}")
        }
    }

    void parseUnits1(List<String> titles,List<String> lyrics){
        /**
         * created by yang on 22:03 2017/12/31.
         * describtion:基于 single slide ppt 导出多首
         * @param titles:
         * @param lyrics:

         */
        titles.eachWithIndex{ String title, int i -> parseUnit1(title,lyrics.get(i))}
        removeSlide(0)
    }
    void parseUnit2(String title,String lyrics){
        /**
         * created by yang on 17:28 2017/12/29.
         * describtion: 基于 二页的模板处理1个单元
         * @param title:
         * @param lyrics:whoe lyric
         */

        copySlide(0)
        writeTmplate(title,"","")
        def lyricList = lyrics.split(/\r\n\r\n/)
        if (lyricList.size()==1){
            lyricList = lyrics.split(/\n\n/)
        }
        def max = lyricList.size()
        lyricList.eachWithIndex{ String lyric, int i ->
            copySlide(1)
            writeTmplate(title,lyric,"${i+1}/${max}")
        }
    }

    void parseUnits2(List<String> titles,List<String> lyrics){
        /**
         * created by yang on 22:03 2017/12/31.
         * describtion:基于 二页的模板处理多个个单元
         * @param titles:
         * @param lyrics:

         */
        titles.eachWithIndex{ String title, int i -> parseUnit2(title,lyrics.get(i))}
        removeSlide(0)
        removeSlide(0)
    }
    static void main(String[] args) {
        def ppt = new PPTXHelper()
        ppt.parseUnits2(["街は光の中に","街は光の中に2"], ["""我的与你的
""","""hahaha
hahaha

hahaha
"""])
        ppt.export("2.pptx")
//        new PPTXHelper("D:\\3.ws\\1.idea\\ps_bg\\1.pptx").saveAsPDFOutputStream(new FileOutputStream("c:\\1.pdf"))

    }

}
