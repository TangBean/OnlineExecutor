package org.olexec.execute;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 执行外部传来的一个代表Java类的byte数组
 * 执行过程：
 * 1. 清空HackSystem中的缓存
 * 2. new ClassModifier，并传入需要被修改的字节数组
 * 3. 调用ClassModifier#modifyUTF8Constant修改：
 *      java/lang/System -> com/jvm/ch9/remoteinvoke/HackSystem
 * 4. new一个类加载器，把字节数组加载为Class对象
 * 5. 通过反射调用Class对象的main方法
 * 6. 从HackSystem中获取返回结果
 */
public class JavaClassExecutor {
    /* 程序中正在运行的客户端代码个数 */
//    private static volatile AtomicInteger runningCount = new AtomicInteger(0);

    public static String execute(byte[] classByte, String systemIn) {
        // 2. new ClassModifier，并传入需要被修改的字节数组
        ClassModifier cm = new ClassModifier(classByte);

        // 3. 调用ClassModifier#modifyUTF8Constant修改
        byte[] modifyBytes = cm.modifyUTF8Constant("java/lang/System","org/olexec/execute/HackSystem");
        modifyBytes = cm.modifyUTF8Constant("java/util/Scanner", "org/olexec/execute/HackScanner");

        // 设置用户传入的标准输入
        ((HackInputStream) HackSystem.in).set(systemIn);

        // 4. new一个类加载器，把字节数组加载为Class对象
        HotSwapClassLoader classLoader = new HotSwapClassLoader();
        Class clazz = classLoader.loadByte(modifyBytes);

        // 5. 通过反射调用Class对象的main方法
        try {
            Method mainMethod = clazz.getMethod("main", new Class[] { String[].class });
            mainMethod.invoke(null, new String[] { null });
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            /*
            被调用的方法的内部抛出了异常而没有被捕获时，将由此异常接收，
            由于这部分异常是远程执行代码的异常，我们要把异常栈反馈给客户端，
            所以不能使用默认的无参 printStackTrace() 把信息 print 到 System.err 中，
            而是要把异常信息 print 到 HackSystem.err 以反馈给客户端
            */
            e.getCause().printStackTrace(HackSystem.err);
        }

        // 6. 从HackSystem中获取返回结果
        String res = HackSystem.getBufferString();
        HackSystem.closeBuffer();
        return res;
    }
}
