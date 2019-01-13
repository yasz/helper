package yjh.poi;

import java.net.URI;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.PresentationML.MainPresentationPart;
import org.docx4j.openpackaging.parts.PresentationML.SlideLayoutPart;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.pptx4j.jaxb.Context;
import org.pptx4j.pml.Shape;
import org.pptx4j.pml.Sld;
import org.pptx4j.pml.SldLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;


/**
 * @author jharrop
 *
 */
public class CreateHelloWorld  {
	
	protected static Logger log = LoggerFactory.getLogger(CreateHelloWorld.class);
	
	private static boolean MACRO_ENABLE = true;
		
	public static void main(String[] args) throws Exception {

		String outputfilepath = "C:\\5.pptx";
		PresentationMLPackage presentationMLPackage =(PresentationMLPackage) OpcPackage.load(new java.io.File("C:\\tmp1.pptx"));
		MainPresentationPart pp = (MainPresentationPart)presentationMLPackage.getParts().getParts().get(new PartName("/ppt/presentation.xml"));
		SlideLayoutPart layoutPart = (SlideLayoutPart)presentationMLPackage.getParts().getParts().get(new PartName("/ppt/slideLayouts/slideLayout1.xml"));//基于哪个master页

		for (i in 1..2){
			SlidePart slidePart = new SlidePart(new PartName("/ppt/slides/slide1.xml"));
//			slidePart.setContents( SlidePart.createSld() );
			pp.addSlide(slidePart);
			slidePart.addTargetPart(layoutPart);
			SlidePart slide = pp.getSlide(1);
			String content = slide.getXML();
			content = content.replaceAll(/#title/, "歌词${i}").replaceAll(/#lyric/, "歌词${i}").replaceAll (/#order/,
					"${i}")
			slidePart.setJaxbElement((Sld)XmlUtils.unmarshalString(content,Context.jcPML));
		}

		presentationMLPackage.save(new java.io.File(outputfilepath));
		System.out.println("\n\n done .. saved " + outputfilepath);
		
	}

}