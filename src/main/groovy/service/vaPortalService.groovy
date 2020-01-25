package service

import com.fasterxml.jackson.databind.ObjectMapper
import common.Const
import tool.DocxHelper

/**
 * Created by Peter.Yang on 2019/6/20.
 */
class vaPortalService {

    static int exportAdmin(it) {
        def paras = [:]
        def appno = it.split("\t")[0]
        if(appno.length()<1){return}
        def jsonStr = it.split("\t")[1]
        LinkedHashMap<String, Object> hm = new ObjectMapper().readValue(jsonStr, LinkedHashMap.class)
        print("${appno}\t")
        hm.each { k, v -> print("$v\t") } println()

        paras.appno = appno
        paras.cnname = hm.学生姓名
        paras.sex = hm.学生性别
        paras.birthdate = hm.出生日期
        paras.rpr = hm.户籍地址
        paras.ca = hm.户籍地址
        paras.currentSchool = hm.现就读学校
        paras.currentGrade = hm.现就读年级
        paras.applyGrade = hm.申请就读年级
        paras.talents = hm.特长及爱好
        paras.fathername = hm.父亲姓名
        paras.mothername = hm.母亲姓名
        paras.fatherMobile = hm.父亲手机
        paras.mothermobile = hm.母亲手机
        paras.fatherWork = hm.父亲工作单位及职位
        paras.motherwork = hm.母亲工作单位及职位
        paras.hearWay = hm.了解惟校渠道
        def outputStream = new FileOutputStream("${appno} ${hm.学生姓名}.docx")
        new DocxHelper("${Const.tmplatePath}/vaAdminTmp2.docx").replace(paras).saveAsOutputStream(outputStream)
    }

    static void exportAdmin() {

        new File("D:\\1.doc\\va\\报名\\record.txt").eachWithIndex { it, index ->
            def paras = [:]
            def appno = it.split("\t")[0]
            if(appno.length()<1){return}
            def jsonStr = it.split("\t")[1]
            LinkedHashMap<String, Object> hm = new ObjectMapper().readValue(jsonStr, LinkedHashMap.class)
            print("${appno}\t")
            hm.each { k, v -> print("$v\t") } println()

            paras.appno = appno
            paras.cnname = hm.学生姓名
            paras.sex = hm.学生性别
            paras.birthdate = hm.出生日期
            paras.rpr = hm.户籍地址
            paras.ca = hm.户籍地址
            paras.currentSchool = hm.现就读学校
            paras.currentGrade = hm.现就读年级
            paras.applyGrade = hm.申请就读年级
            paras.talents = hm.特长及爱好
            paras.fathername = hm.父亲姓名
            paras.mothername = hm.母亲姓名
            paras.fatherMobile = hm.父亲手机
            paras.mothermobile = hm.母亲手机
            paras.fatherWork = hm.父亲工作单位及职位
            paras.motherwork = hm.母亲工作单位及职位
            paras.hearWay = hm.了解惟校渠道

            def outputStream = new FileOutputStream("${appno} ${hm.学生姓名}.docx")

            new DocxHelper("${Const.tmplatePath}/vaAdminTmp2.docx").replace(paras).saveAsOutputStream(outputStream)
        }


    }

    static void main(String[] args) {
        new vaPortalService().exportAdmin()
    }

}
