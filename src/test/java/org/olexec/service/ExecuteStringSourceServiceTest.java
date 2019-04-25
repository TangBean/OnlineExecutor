package org.olexec.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ExecuteStringSourceServiceTest {
    @Autowired
    private ExecuteStringSourceService executeStringSourceService;

    @Test
    public void execute() {
        String source = "public class Man {\n" +
                "\tpublic static void main(String[] args) {\n" +
                "\t\tSystem.out.println(\"hello world 0\");\n" +
                "\t\tSystem.out.println(\"hello world 1\");\n" +
                "\t\tSystem.out.println(\"hello world 2\");\n" +
                "\t\tSystem.out.println(\"hello world 3\");\n" +
                "\t\tSystem.out.println(\"hello world 4\");\n" +
                "\t}\n" +
                "}";
        String source1 = "public class Man {\n" +
                "\tpublic static void main(String[] args) throws InterruptedException {\n" +
                "\t\tSystem.out.println(\"hello world 1\");\n" +
                "\t\tThread.sleep(5000);\n" +
                "\t\tSystem.out.println(\"hello world 1\");\n" +
                "\t}\n" +
                "}";
        String source2 = "public class Man {\n" +
                "\tpublic static void main(String[] args) {\n" +
                "\t\tSystem.out.println(\"hello world 2\");\n" +
                "\t\tSystem.out.println(\"hello world 2\");\n" +
                "\t}\n" +
                "}";

        // 测试 Scanner in = new Scanner(System.in);
        String sourceTestSystemIn = "import java.util.Scanner;\n" +
                "public class Run {\n" +
                "\tpublic static void main(String[] args) {\n" +
                "\t\tScanner in = new Scanner(System.in);\n" +
                "\t\tSystem.out.println(in.nextInt());\n" +
                "\t\tSystem.out.println(in.nextDouble());\n" +
                "\t\tSystem.out.println(in.next());\n" +
                "\t}\n" +
                "}";

        String systemIn = "1 1.5 \n fsdfasdfasdf";

        // Test2
        String sourceTestSystemIn1 = "import java.util.*;\n" +
                "public class Run {\n" +
                "\tpublic static void main(String[] args) {\n" +
                "\t\tScanner in = new Scanner(System.in);\n" +
                "\t}\n" +
                "}";

        String systemIn1 = "";

////        new Thread() {
////            @Override
////            public void run() {
////                System.out.println(executeStringSourceService.execute(source1));
////            }
////        }.start();
//
////        System.out.println(executeStringSourceService.execute(source1));
//
//        new Thread() {
//            @Override
//            public void run() {
//                System.out.println("begin");
//                String res = executeStringSourceService.execute(source2);
//                System.out.println(res);
//                System.out.println("end");
//            }
//        }.start();

        String res = executeStringSourceService.execute(sourceTestSystemIn1, systemIn1);
        System.out.println("---------- Begin ----------");
        System.out.print(res);
        System.out.println("----------- End -----------");
    }
}