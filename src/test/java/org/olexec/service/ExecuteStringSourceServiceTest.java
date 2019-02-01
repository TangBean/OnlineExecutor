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

        String res = executeStringSourceService.execute(source);
        System.out.println("---------- Begin ----------");
        System.out.print(res);
        System.out.println("----------- End -----------");
    }
}