package yjh.helper

import com.teradata.tool.CloneUtils
import org.apache.poi.hslf.usermodel.HSLFSlide
import org.apache.poi.hslf.usermodel.HSLFSlideShow

import org.apache.poi.sl.usermodel.Slide
import org.apache.poi.sl.usermodel.SlideShow
import org.apache.poi.xslf.usermodel.SlideLayout
import org.apache.poi.xslf.usermodel.XMLSlideShow
import org.apache.poi.xslf.usermodel.XSLFSlide
import org.apache.poi.xslf.usermodel.XSLFSlideShow

import org.apache.poi.hslf.usermodel.HSLFTextBox
import org.apache.poi.xslf.usermodel.XSLFTextBox


/**
 * Created by Peter.Yang on 7/27/2017.
 */

class PPTHelper {
    SlideShow ss

    PPTHelper(String fileName) {
        if (fileName.endsWith(".ppt")) {
            new File(fileName).withDataInputStream { ss = new HSLFSlideShow(it) }
        } else if (fileName.endsWith(".pptx")) {
            new File(fileName).withInputStream { ss = new XMLSlideShow(it) }
        }
    }

    boolean readSlideText() {
/**
 * created by yang on 17:37 2017/12/28.
 * describtion:读取一份文档，并将每个模块的文本读取出来
 * @param slideShow :
 */
        Slide[] slides = ss.getSlides()
        slides.each { slide ->
            slide.getShapes().each { shape ->
                if (shape.getClass() == XSLFTextBox.class) {
                    XSLFTextBox box = (XSLFTextBox) shape
                    println box.getText()
                } else if (shape.getClass() == HSLFTextBox.class) {
                    HSLFTextBox box = (HSLFTextBox) shape
                    println box.getText()
                }
            }

        }

    }

    String checkTmplate() {
        /**
         * created by yang on 17:21 2017/12/28.
         * describtion:检查用户上传PPT模板是否合规,合规的规则是
         *
         */
        return true
    }
    void copySlide(int num) {
        Slide srcSlide = ss.getSlides()[num]
        Slide descSlide = ss.createSlide()

        srcSlide.getShapes().each { shape ->
            if (shape.getClass() == XSLFTextBox.class) {
                XSLFTextBox box = XSLFTextBox(shape)

            } else {
                    descSlide.addShape(shape)
            }
        }
    }

    void copyMaster(int num) {

        XMLSlideShow ppt =(XMLSlideShow)ss
        Slide descSlide = ppt.createSlide(ppt.getSlideMasters()[num].getLayout(SlideLayout.TITLE_AND_CONTENT))

    }
    void writeTmplate(String title, String lyric, String order, int slideNum) {
        /**
         * created by yang on 22:28 2017/12/28.
         * describtion:基于已经copy好的模板,对固定页替换title lyric[也是控制的]
         * @param title : title
         * @param lyrics :歌词
         * @param slideNum :页码序列 0开始
         */

        Slide slide = ss.getSlides()[slideNum]
        slide.getShapes().each { shape ->
            if (shape.getClass() == XSLFTextBox.class) {//ppt
                XSLFTextBox box = (XSLFTextBox) shape
                String repStr= box.getText().replaceAll(/#title/, title).replaceAll(/#lyric/, lyric).replaceAll (/#order/, order)
                println "replace from ${box.getText()} TO ${repStr}"
                box.setText(repStr)
            }
        }
    }


    void export(String path) {
        /**
         * created by yang on 21:59 2017/12/28.
         * describtion:导出文件
         * @param path :文件路径
         */
        FileOutputStream outputStream = new FileOutputStream(path)
        ss.write(outputStream)
//        outputStream.flush()
        outputStream.close()
    }


    static void main(String[] args) {
        String fileName = "C:\\tmp1.pptx"
        def ppt = new PPTHelper(fileName)
//
        ppt.writeTmplate("啊1", "歌词内容1", "1", 0)
        ppt.writeTmplate("啊2", "歌词内容2", "2", 1)
//        ppt.writeTmplate("啊3", "歌词内容3", "3", 2)
//        ppt.readSlideText()
        ppt.export("c:\\2.pptx")

    }

}
