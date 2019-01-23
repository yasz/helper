import yjh.helper.Excelhelper

/**
 * Created by Peter.Yang on 2019/1/13.
 */
class Test {
    public static void main(String[] args) {

        def eh = new Excelhelper("D:\\3.ws\\1.idea\\helper\\data\\3V.xlsx")
        eh.setSheet(1)
        def tab2 = eh.read()

        eh.setSheet(0)// 0:科目考察表 1: 明细打分表
        def tab = eh.read() //获取非学科考察科目
        tab2 = tab2.takeRight(tab2.size() - 3).findAll { !it[0].equals("") }


        //通过item找对应的
        println(tab2)



    }

    static Double getNonSubjectScore(def tab,def item){

    }

}
