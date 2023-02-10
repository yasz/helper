package common;
 
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Date;


public abstract class AsposeRegister
{
    /**
     * aspose-words:jdk17:22.5 版本
     */
    public static void registerWord_v_22_5() throws Exception
    {
        Class<?> zzjXClass = Class.forName("com.aspose.words.zzjX");
        Constructor<?> constructor = zzjXClass.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        Object zzjXInstance = constructor.newInstance();
 
        // zzZ7O
        Field zzZ7O = zzjXClass.getDeclaredField("zzZ7O");
        zzZ7O.setAccessible(true);
        zzZ7O.set(zzjXInstance, new Date(Long.MAX_VALUE));
 
        // zzBf
        Field zzZfB = zzjXClass.getDeclaredField("zzZfB");
        zzZfB.setAccessible(true);
        Class<?> zzYP3Class = Class.forName("com.aspose.words.zzYP3");
        Field zzBfField = zzYP3Class.getDeclaredField("zzBf");
        zzBfField.setAccessible(true);
        zzZfB.set(zzjXInstance, zzBfField.get(null));
 
        // zzZjA
        Field zzZjA = zzjXClass.getDeclaredField("zzZjA");
        zzZjA.setAccessible(true);
        zzZjA.set(null, zzjXInstance);
 
 
        Class<?> zzCnClass = Class.forName("com.aspose.words.zzCn");
        Field zzZyx = zzCnClass.getDeclaredField("zzZyx");
        zzZyx.setAccessible(true);
        zzZyx.set(null, 128);
        Field zzZ8w = zzCnClass.getDeclaredField("zzZ8w");
        zzZ8w.setAccessible(true);
        zzZ8w.set(null, false);
    }
 
    /**
     * aspose-cells:22.6 版本有效
     */
    public static void registerExcel_v_22_6() throws Exception
    {
        String licenseExpiry = "20991231";
 
        // License
        Class<?> licenseClass = Class.forName("com.aspose.cells.License");
        Field a = licenseClass.getDeclaredField("a");
        a.setAccessible(true);
        a.set(null, licenseExpiry);
 
        // k65
        Class<?> k65Class = Class.forName("com.aspose.cells.k65");
        Field k65A = k65Class.getDeclaredField("a");
        k65A.setAccessible(true);
 
        Constructor<?> constructor = k65Class.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        Object k65Instance = constructor.newInstance();
        k65A.set(null, k65Instance);
 
        Field k56C = k65Class.getDeclaredField("c");
        k56C.setAccessible(true);
        k56C.set(k65Instance, licenseExpiry);
 
        // e0n
        Class<?> e0nClass = Class.forName("com.aspose.cells.e0n");
        Field e0nA = e0nClass.getDeclaredField("a");
        e0nA.setAccessible(true);
        e0nA.set(null, false);
 
    }
 
    /**
     * aspose-slides:21.10 版本有效
     */
    public static void registerPPT_v_21_10() throws Exception
    {
        Date licenseExpiry = new Date(Long.MAX_VALUE);
 
        Class<?> publicClass = Class.forName("com.aspose.slides.internal.of.public");
        Object publicInstance = publicClass.newInstance();
 
        Field publicTry = publicClass.getDeclaredField("try");
        publicTry.setAccessible(true);
        publicTry.set(null, publicInstance);
 
        Field publicInt = publicClass.getDeclaredField("int");
        publicInt.setAccessible(true);
        publicInt.set(publicInstance, licenseExpiry);
 
        Field publicNew = publicClass.getDeclaredField("new");
        publicNew.setAccessible(true);
        publicNew.set(publicInstance, licenseExpiry);
 
        Field publicIf = publicClass.getDeclaredField("if");
        publicIf.setAccessible(true);
        publicIf.set(publicInstance, 2);
 
        Class<?> nativeClass = Class.forName("com.aspose.slides.internal.of.native");
        Field nativeDo = nativeClass.getDeclaredField("do");
        nativeDo.setAccessible(true);
        nativeDo.set(null, publicInstance);
    }
 
    /**
     * aspose-pdf:21.7 版本有效
     */
    public static void registerPdf_v_21_7() throws Exception
    {
        Date licenseExpiry = new Date(Long.MAX_VALUE);
        Class<?> l9yClass = Class.forName("com.aspose.pdf.l9y");
        Constructor<?> constructor = l9yClass.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        Object l9yInstance = constructor.newInstance();
 
        // lc
        Field lc = l9yClass.getDeclaredField("lc");
        lc.setAccessible(true);
        lc.set(l9yInstance, licenseExpiry);
        // ly
        Field ly = l9yClass.getDeclaredField("ly");
        ly.setAccessible(true);
        ly.set(l9yInstance, licenseExpiry);
 
        // l0if
        Field l0if = l9yClass.getDeclaredField("l0if");
        l0if.setAccessible(true);
 
        Class<?> l9nClass = Class.forName("com.aspose.pdf.l9n");
        Field lfField = l9nClass.getDeclaredField("lf");
        lfField.setAccessible(true);
        Object lf = lfField.get(null); // 处理枚举
        l0if.set(l9yInstance, lf);
 
        Class<?> l9yLfClass = Class.forName("com.aspose.pdf.l9y$lf");
        Field l9y$lf = l9yLfClass.getDeclaredField("lI");
        l9y$lf.setAccessible(true);
        l9y$lf.set(null, l9yInstance);
 
 
        Class<?> l19jClass = Class.forName("com.aspose.pdf.l19j");
        Field l19jlI = l19jClass.getDeclaredField("lI");
        l19jlI.setAccessible(true);
        l19jlI.set(null, 128);
        Field l19jLf = l19jClass.getDeclaredField("lf");
        l19jLf.setAccessible(true);
        l19jLf.set(null, false);
    }
 
    public static void registerAll()
    {
        try
        {
            registerWord_v_22_5();
            registerPPT_v_21_10();
            registerExcel_v_22_6();
            registerPdf_v_21_7();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Aspose注册失败", e);
        }
 
    }
}