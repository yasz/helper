package yjh.helper

import groovy.sql.Sql

class StringHelper {

    static printL(String[] l) {

    }

    static String printLL(def ll, String sep) {
        def o = ""
        ll.eachWithIndex { l, i ->
            if (i == 4) {
                o += "\n"
            }
            o += (l.collect { '"' + it.replaceAll("\n", "\n") + '"' }.join(sep)) + "\n"
        }
        println(o)
        return o
    }

    String colclear(String src) {
        def desc = src
        desc.replaceAll(" ", "")

    }

    static List<List> s2ll(String s) {
        def rs = []
        s = s.replaceAll("\r", "")
        s.split("\n").toList().each {
            rs.add(it.split("\t"))
        }
        return rs
    }

    static Object[][] s2Oo(String s) {
        s = s.replaceAll("\r", "")
        def rs = [] as Object[]
        s.split("\n").toList().each {
            def os = it.split("\t") as Object[]
            if (os.size() == 1) {
                os = [os[0], null] as Object[]
            }
            rs += [os]
        }
        return rs
    }

    static String u2g(String src) {
        return new String(src.getByte("unicode"), "GBK")
    }

    static String ll2s(List list) {
        def s = ""
        list.each { l ->
            def unit = ""
            l.each {
                unit = "$unit\t$it"
            }
            s = s + unit.substring(1) + "!\n"
        }
        s
    }

    static List<List> f2ll(String filePath) {
        StringBuffer fileData = new StringBuffer();
        //
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        //缓冲区使用完必须关掉
        reader.close();
        return s2ll(fileData.toString())
    }

    static List getUnitArrange(List<List> list) {
        return getUnitArrange(list, 0)
    }

    static List getUnitArrange(List<String[]> list, def pid) {
        def start = 0
        List rl = []
        for (i in 0..list.size() - 1) {
            if (i == list.size() - 1) {
                rl.push([start, i])
                break
            }
            def value = list.get(i)[pid]//默认第一列是排序值
            def value2 = list.get(i + 1)[pid]//默认第一列是排序值
            if (value == value2) {
                continue
            } else {
                rl.push([start, i])
                start = i + 1
            }
        }
        return rl
    }

    static List array2hash(List titles, List datas) {
//        if (titles.size() != datas.size()) throw new Exception("titles与datas必须一致")
        if (titles == null) {
            titles = ['a', 'b']
        }
        def ha = []
        datas.each { col ->
            def h = [:]
            titles.eachWithIndex {
                title, j ->
                    {
                        h.put(title, col[j])
                    }

            }
            ha.add(h)
//        标准化应对将['a','b'],['1','2']['3','4']导出{a:1,b:2},{a:3,b:4}
        }
        ha
    }

    static String excel1(String str) {
//专门的功能
        List<String> strs = str.split(/\t|\n/)
        strs = strs.reverse()
        def out = ""
        while (strs) {
            def left = strs.pop()
            def right = strs.pop()
            out += "$left  $right\n"
            if (right.contains("\"")) {
                right = strs.pop()
                while (!right.contains("\"")) {
                    out += "$left  $right\n"
                    right = strs.pop()
                }
                out += "$left  $right\n"
            }
        }
        println out.replaceAll("\"", "")
    }

    static main(args) {
//        printLL([["c1", "c2"], ["v1", "v2"]], "\t")
        print(array2hash(["c1", "c2"], [["1", "2"], ["3", "4"]]))
    }
}
