import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

/**
 * Created by Peter.Yang on 2021/1/30.
 */
public class Assis {
    public static void main(String[] args) throws Exception {
        ClassPool.getDefault().insertClassPath("D:\\3.ws\\1.idea\\helper\\lib\\aspose-words-21.1-jdk16.jar");
        CtClass zzZJJClass = ClassPool.getDefault().getCtClass("com.aspose.words.zzZE0");
        CtMethod zzv = zzZJJClass.getDeclaredMethod("zzZ4h");
        zzv.setBody("{return 1;}");
//
         zzv = zzZJJClass.getDeclaredMethod("zzZ4g");
        zzv.setBody("{return 1;}");
//
//
//        for (CtMethod c : zzZJJClass.getDeclaredMethods()) {
//            if (c.getName().equals("zzZ") && c.getReturnType().getName().contains("boolean")) {
//                System.out.println("got zzZ");
//                c.setBody("{return true;}");
//            }
//            if (c.getName().equals("zzV") ) {
//                System.out.println("got zzV");
//                c.setBody("{return;}");
//            }
//
//
//        }


                zzZJJClass.writeFile("D:\\3.ws\\1.idea\\class");
            }

        }
