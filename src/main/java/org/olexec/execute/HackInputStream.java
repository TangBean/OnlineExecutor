package org.olexec.execute;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 这个类本质上和 HackPrintStream 差不多，不过它不需要格式化输出的功能，
 * 唯一的功能就是为每一个线程都保持一个标准输入流
 */
public class HackInputStream extends InputStream {
    public final static ThreadLocal<InputStream> holdInputStream = new ThreadLocal<>();

    @Override
    public int read() throws IOException {
        return 0;
    }

    public InputStream get() {
        return holdInputStream.get();
    }

    public void set(String systemIn) {
        holdInputStream.set(new ByteArrayInputStream(systemIn.getBytes()));
    }

    @Override
    public void close() {
        holdInputStream.remove();
    }
}
