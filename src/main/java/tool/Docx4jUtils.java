package tool;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.model.structure.SectionWrapper;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Document;
import org.docx4j.wml.Ftr;
import org.docx4j.wml.Hdr;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * 关于文件操作的工具类
 *
 * @author kaizen
 * @date 2018-10-23 17:21:36
 */
public final class Docx4jUtils {



    public static void replace(Map<String, String> map,MainDocumentPart mainDocumentPart,WordprocessingMLPackage wordMLPackage) {
        try {

            if (null != map && !map.isEmpty()) {
                // 将${}里的内容结构层次替换为一层
                Docx4jUtils.cleanDocumentPart(mainDocumentPart);
                // 替换文本内容
                mainDocumentPart.variableReplace(map);
            }

            for (SectionWrapper section : wordMLPackage.getDocumentModel().getSections()) {
                if (section.getHeaderFooterPolicy() != null) {
                    Docx4jUtils.cleanSectionPart(section);
                    section.getHeaderFooterPolicy().getDefaultHeader().variableReplace(map);
                    section.getHeaderFooterPolicy().getDefaultFooter().variableReplace(map);
                    section.getHeaderFooterPolicy().getHeader(2).variableReplace(map);
                    section.getHeaderFooterPolicy().getFooter(2).variableReplace(map);

                }
            }

        } catch (Exception e) {
            System.out.println(e.getMessage().toString());
        }
    }



    public static boolean cleanSectionPart(SectionWrapper section) throws Exception {
        if (section == null) {
            return false;
        }
        HeaderPart hp= section.getHeaderFooterPolicy().getDefaultHeader();
        FooterPart fp= section.getHeaderFooterPolicy().getDefaultFooter();
        String headerWmlTemplate =
                XmlUtils.marshaltoString(hp.getContents(), true, false, Context.jc);
        Hdr headerDocument = (Hdr) XmlUtils.unwrap(DocxVariableClearUtils.doCleanDocumentPart(headerWmlTemplate, Context.jc));

        String footerWmlTemplate =
                XmlUtils.marshaltoString(fp.getContents(), true, false, Context.jc);
        Ftr footerDocument = (Ftr) XmlUtils.unwrap(DocxVariableClearUtils.doCleanDocumentPart(footerWmlTemplate, Context.jc));

        hp.setContents(headerDocument);
        fp.setContents(footerDocument);

        HeaderPart hp2= section.getHeaderFooterPolicy().getHeader(2);
        FooterPart fp2= section.getHeaderFooterPolicy().getFooter(2);
        String header2WmlTemplate =
                XmlUtils.marshaltoString(hp2.getContents(), true, false, Context.jc);
        Hdr header2Document = (Hdr) XmlUtils.unwrap(DocxVariableClearUtils.doCleanDocumentPart(header2WmlTemplate, Context.jc));

        String footer2WmlTemplate =
                XmlUtils.marshaltoString(fp2.getContents(), true, false, Context.jc);
        Ftr footer2Document = (Ftr) XmlUtils.unwrap(DocxVariableClearUtils.doCleanDocumentPart(footer2WmlTemplate, Context.jc));

        hp2.setContents(header2Document);
        fp2.setContents(footer2Document);

        return true;
    }
    public static boolean cleanDocumentPart(MainDocumentPart documentPart) throws Exception {
        if (documentPart == null) {
            return false;
        }
        Document document = documentPart.getContents();
        String wmlTemplate =
                XmlUtils.marshaltoString(document, true, false, Context.jc);
        System.out.println("before clean:"+wmlTemplate);
        Object afterClean=DocxVariableClearUtils.doCleanDocumentPart(wmlTemplate, Context.jc);
        document = (Document) XmlUtils.unwrap(afterClean);

        documentPart.setContents(document);

        return true;
    }

    /**
     * 清扫 docx4j 模板变量字符,通常以${variable}形式
     * <p>
     * XXX: 主要在上传模板时处理一下, 后续
     *
     * @author liliang
     * @since 2018-11-07
     */
    private static class DocxVariableClearUtils {


        /**
         * 去任意XML标签
         */
        private static final Pattern XML_PATTERN = Pattern.compile("<.*?>");



        /**
         * start符号
         */
        private static final char PREFIX = '$';

        /**
         * 中包含
         */
        private static final char LEFT_BRACE = '{';

        /**
         * 结尾
         */
        private static final char RIGHT_BRACE = '}';

        /**
         * 未开始
         */
        private static final int NONE_START = -1;

        /**
         * 未开始
         */
        private static final int NONE_START_INDEX = -1;

        /**
         * 开始
         */
        private static final int PREFIX_STATUS = 1;

        /**
         * 左括号
         */
        private static final int LEFT_BRACE_STATUS = 2;

        /**
         * 右括号
         */
        private static final int RIGHT_BRACE_STATUS = 3;


        /**
         * doCleanDocumentPart
         *
         * @param wmlTemplate
         * @param jc
         * @return
         * @throws JAXBException
         */
        private static Object doCleanDocumentPart(String wmlTemplate, JAXBContext jc) throws JAXBException, JAXBException {
            // 进入变量块位置
            int curStatus = NONE_START;
            // 开始位置
            int keyStartIndex = NONE_START_INDEX;
            // 当前位置
            int curIndex = 0;
            char[] textCharacters = wmlTemplate.toCharArray();
            StringBuilder documentBuilder = new StringBuilder(textCharacters.length);
            documentBuilder.append(textCharacters);
            // 新文档
            StringBuilder newDocumentBuilder = new StringBuilder(textCharacters.length);
            // 最后一次写位置
            int lastWriteIndex = 0;
            for (char c : textCharacters) {
                switch (c) {
                    case PREFIX:
                        // TODO 不管其何状态直接修改指针,这也意味着变量名称里面不能有PREFIX
                        keyStartIndex = curIndex;
                        curStatus = PREFIX_STATUS;
                        break;
                    case LEFT_BRACE:
                        if (curStatus == PREFIX_STATUS) {
                            curStatus = LEFT_BRACE_STATUS;
                        }
                        break;
                    case RIGHT_BRACE:
                        if (curStatus == LEFT_BRACE_STATUS) {
                            // 接上之前的字符
                            newDocumentBuilder.append(documentBuilder.substring(lastWriteIndex, keyStartIndex));
                            // 结束位置
                            int keyEndIndex = curIndex + 1;
                            // 替换
                            String rawKey = documentBuilder.substring(keyStartIndex, keyEndIndex);
                            // 干掉多余标签
                            String mappingKey = XML_PATTERN.matcher(rawKey).replaceAll("");
//                            System.out.println(mappingKey);
                            newDocumentBuilder.append(mappingKey);
                            lastWriteIndex = keyEndIndex;
                            curStatus = NONE_START;
                            keyStartIndex = NONE_START_INDEX;
                        }
                    default:
                        break;
                }
                curIndex++;
            }
            // 余部
            if (lastWriteIndex < documentBuilder.length()) {
                newDocumentBuilder.append(documentBuilder.substring(lastWriteIndex));
            }
//            System.out.println("after clean:"+newDocumentBuilder.toString());
            return XmlUtils.unmarshalString(newDocumentBuilder.toString());
        }
    }
}