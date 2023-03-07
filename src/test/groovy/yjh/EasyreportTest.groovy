package yjh

import spock.lang.Specification
import tool.DocxHelper

class EasyreportTest extends Specification {

    def "InvokeMain"() {
        expect :"执行模板替换器"
        Easyreport.replace()==true
    }
}
