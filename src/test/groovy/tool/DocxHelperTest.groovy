package tool

import spock.lang.Specification

class DocxHelperTest extends Specification {

    def "Merge"() {
        expect :"合并測試"
        DocxHelper.merge(["data/test/a.docx","data/test/b.docx"],"data/test/c.docx")==true
    }
}
