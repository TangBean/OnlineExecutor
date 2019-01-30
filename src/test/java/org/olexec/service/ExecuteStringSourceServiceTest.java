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
                "\t\tSystem.out.println(\"hello world\");\n" +
                "\t}\n" +
                "}";

        String res = executeStringSourceService.execute(source);
        System.out.println("---------- Begin ----------");
        System.out.print(res);
        System.out.println("----------- End -----------");
    }
}