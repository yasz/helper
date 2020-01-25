package service

import common.Const
import tool.DBHelper
import tool.DocxHelper
import tool.TextHelper
import yjh.helper.Excelhelper

/**
 * Created by Peter.Yang on 2019/6/22.
 */
class Report3Service {
    //导出报告单

    static void main(String[] args) {
        def itemParas = [:]
        def sumParas = [:]
        def commentParas = [:]
        def scoreParas = [:]

        def outputCommonParas = [:]
        def db = new DBHelper('192.168.0.149','postgres','ruianVA123')
        db.query("select * from report0").each {it-> //考察项视图
            itemParas[it.班级号 + it.itempara ]=it
        }
        db.query("select * from reportSum").each {it-> //科目汇总分数视图
            sumParas[it.vano]=it
        }
        db.query("select * from reportComment").each {it-> //评语视图
            commentParas[it.vano+it.科目编号]=it
        }

        db.query("select * from reportScore").each {it-> //打分视图
            scoreParas[it.vano+it.科目编号]=it
        }


        db.query("SELECT DISTINCT subjectPara,科目,subjectEn from report0").each { subject->
            outputCommonParas[subject.subjectpara]= newlineToBreakHack(subject.科目 +"\n"+ subject.subjecten )
        }

        sumParas.each { sumPara->
            def paras = [:]
            def val = sumPara.value
            String vano = val.vano
            paras.vano = vano
            def classno = vano.substring(0,4)
//            if(classno != "1502"){return}
//            if(vano != "1502%"){return}

            paras.cnname = val.cnname
            paras.enname = val.enname

            1.upto(14){subjectIndex -> //14科目
                1.upto(8){ itemIndex -> //8个考察项
                    def itemPara = itemParas["${classno}item${subjectIndex}${itemIndex}".toString()]
                    paras.classname = itemPara.班名
                    paras["item${subjectIndex}${itemIndex}".toString()] = newlineToBreakHack("""${n2b(itemPara.item)}\n${n2b(itemPara
                            .itemen)}${n2b(itemPara.itemWeight).replaceAll("\\(","\n\\(")}""".toString())
                    if(subjectIndex<=3) {
                        paras["item${subjectIndex}${itemIndex}".toString()] = newlineToBreakHack("${n2b(itemPara.item)}\n${n2b(itemPara.itemen)}")
                    }

                    paras["desc${subjectIndex}${itemIndex}".toString()] = n2b(itemPara.desc1)
                    def level = scoreParas[vano+subjectIndex]["考察项${itemIndex}打分".toString()]
                    level = (level == null) ?  "":level
                    paras["level${subjectIndex}${itemIndex}".toString()] = n2b(Report0Service.cal(level.toString()))
//                    println(level)
                }
                paras["sum${subjectIndex}".toString()] = n2b(sumPara.value["val${subjectIndex}".toString()])
                def comment = commentParas["${vano}${subjectIndex}".toString()] ? n2b(commentParas["${vano}${subjectIndex}".toString()].评语):""
                paras["content${subjectIndex}1".toString()] =  comment
            }
//            println(paras)
            def outputName = "out2/${vano}_${paras.cnname}_${paras.enname}.docx"
            paras += outputCommonParas
            def classLevel = Integer.parseInt(paras.classname.toString().substring(0,1))
            def tmpPath = Const.reportSeniorTmpPath
            if(classLevel>=7){
                tmpPath = Const.reportSeniorTmpPath
            }else if(classLevel>=3){
                tmpPath = Const.reportSeniorTmpPath
            }
            new DocxHelper(tmpPath).replace(paras).saveAs(outputName)

        }
    }


    static def n2b(str){
        str==null?"":str
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
}
