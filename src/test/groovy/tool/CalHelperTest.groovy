package tool

import spock.lang.Specification

class CalHelperTest extends Specification {
    def "Dse1002va100"() {
        expect :"测试百分制转换"
        println(CalHelper.dse1002va100(78,[90,89,70,60,40,20,1]))
        CalHelper.dse1002va100(78,[90,89,70,60,40,20,1])==74.21052631
    }
}
