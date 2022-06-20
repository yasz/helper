package yjh.helper

import groovy.sql.Sql

class StringHelper {

    static printL(String[] l) {

    }
    /**
     * 中文数字
     */
    private static final String[] CN_NUM = ["零", "一", "二", "三", "四", "五", "六", "七", "八", "九"];

    /**
     * 中文数字单位
     */
    private static final String[] CN_UNIT = ["", "十", "百", "千", "万", "十", "百", "千", "亿", "十", "百", "千"];

    /**
     * 特殊字符：负
     */
    private static final String CN_NEGATIVE = "负";

    /**
     * 特殊字符：点
     */
    private static final String CN_POINT = "点";
    /**
     * int 转 中文数字
     * 支持到int最大值
     *
     * @param intNum 要转换的整型数
     * @return 中文数字
     */
    public static String int2chineseNum(int intNum) {
        StringBuffer sb = new StringBuffer();
        boolean isNegative = false;
        if (intNum < 0) {
            isNegative = true;
            intNum *= -1;
        }
        int count = 0;
        while (intNum > 0) {
            sb.insert(0, CN_NUM[intNum % 10] + CN_UNIT[count]);
            intNum = intNum / 10;
            count++;
        }
        if (isNegative) {
            sb.insert(0, CN_NEGATIVE);
        }
        return sb.toString().replaceAll("零[千百十]", "零").replaceAll("零+万", "万")
                .replaceAll("零+亿", "亿").replaceAll("亿万", "亿零")
                .replaceAll("零+", "零").replaceAll("零\$", "");
    }

    /**
     * bigDecimal 转 中文数字
     * 整数部分只支持到int的最大值
     *
     * @param bigDecimalNum 要转换的BigDecimal数
     * @return 中文数字
     */
    public static String bigDecimal2chineseNum(BigDecimal bigDecimalNum) {
        if (bigDecimalNum == null) {
            return CN_NUM[0];
        }
        StringBuffer sb = new StringBuffer();

        //将小数点后面的零给去除
        String numStr = bigDecimalNum.abs().stripTrailingZeros().toPlainString();
        String[] split = numStr.split("\\.");
        String integerStr = int2chineseNum(Integer.parseInt(split[0]));
        sb.append(integerStr);
        //如果传入的数有小数，则进行切割，将整数与小数部分分离
        if (split.length == 2) {
            //有小数部分
            sb.append(CN_POINT);
            String decimalStr = split[1];
            char[] chars = decimalStr.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                int index = Integer.parseInt(String.valueOf(chars[i]));
                sb.append(CN_NUM[index]);
            }
        }
        //判断传入数字为正数还是负数
        int signum = bigDecimalNum.signum();
        if (signum == -1) {
            sb.insert(0, CN_NEGATIVE);
        }
        return sb.toString();
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
