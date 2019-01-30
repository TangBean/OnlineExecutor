package org.olexec.execute;

import java.util.Random;

public class TestClass {
    public static int val;

    public static void main(String[] args) {
        Random random = new Random();
        System.out.println("Testing...");
        System.out.println(random.nextInt());
        System.out.println("I'm running");
    }
}
