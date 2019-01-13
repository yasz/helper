package yjh.helper;


/**
 * Created by Peter.Yang on 6/16/2017.
 */


public class Test {
    static main(args) {
//        If ins1=new Class1()
////        If ins2=new Class2()
////        ins1.show()
////        ins2.show()
//        println 110;
//        println a(ins1)

        //测试批量文件输出
        FileReader fr =null
        for (i in 0 .. 9)  {
            fr = new FileWriter("$i.txt",)
            char a = '$i'
            fr.read(a)

        }
        fr.close()

//        char [] a = new char[50];
//        fr.read(a); // 从数组中读取内容
//        for(char c : a)
//            System.out.print(c); // 一个个打印字符
//        fr.close();
//    }

    }
    static int a(If a){
        return 1
    }
}

public class Class1 implements If {
    void show() {
        println("i am Class1");
    }

}
public class Class2 implements If {
    void show() {
        println("i am Class2");
    }

}

interface If {
    int i = 10;
    void show();
}
