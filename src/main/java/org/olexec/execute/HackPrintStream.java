package org.olexec.execute;

import java.io.*;

public class HackPrintStream extends PrintStream {
    private ThreadLocal<ByteArrayOutputStream> out; // 每个线程的标准输出流
    private ThreadLocal<Boolean> trouble; // 每个线程的标准输出写入过程是否抛出IOException

    public HackPrintStream() {
        super(new ByteArrayOutputStream());
        out = new ThreadLocal<>();
        trouble = new ThreadLocal<>();
    }

    @Override
    public String toString() {
        return out.get().toString();
    }

    /**
     * Check to make sure that there is a stream for the current thread.
     * If do not have one, create it.
     */
    private void ensureOpen() throws IOException {
        if (out.get() == null) {
            out.set(new ByteArrayOutputStream());
        }
    }

    /**
     * Flushes the stream.  This is done by writing any buffered output bytes to
     * the underlying output stream and then flushing that stream.
     *
     * @see        java.io.OutputStream#flush()
     */
    public void flush() {
        try {
            ensureOpen();
            out.get().flush();
        }
        catch (IOException x) {
            trouble.set(true);
        }
    }

    /**
     * Closes the stream.  This is done by flushing the stream and then closing
     * the underlying output stream.
     *
     * @see        java.io.OutputStream#close()
     */
    public void close() {
        try {
            out.get().close();
        }
        catch (IOException x) {
            trouble.set(true);
        }
        out.remove();
    }

    /**
     * Flushes the stream and checks its error state. The internal error state
     * is set to <code>true</code> when the underlying output stream throws an
     * <code>IOException</code> other than <code>InterruptedIOException</code>,
     * and when the <code>setError</code> method is invoked.  If an operation
     * on the underlying output stream throws an
     * <code>InterruptedIOException</code>, then the <code>PrintStream</code>
     * converts the exception back into an interrupt by doing:
     * <pre>
     *     Thread.currentThread().interrupt();
     * </pre>
     * or the equivalent.
     *
     * @return <code>true</code> if and only if this stream has encountered an
     *         <code>IOException</code> other than
     *         <code>InterruptedIOException</code>, or the
     *         <code>setError</code> method has been invoked
     */
    public boolean checkError() {
        if (out.get() != null)
            flush();
        return trouble.get() != null ? trouble.get() : false;
    }

    /**
     * Sets the error state of the stream to <code>true</code>.
     *
     * <p> This method will cause subsequent invocations of {@link
     * #checkError()} to return <tt>true</tt> until {@link
     * #clearError()} is invoked.
     *
     * @since JDK1.1
     */
    protected void setError() {
        trouble.set(true);
    }

    /**
     * Clears the internal error state of this stream.
     *
     * <p> This method will cause subsequent invocations of {@link
     * #checkError()} to return <tt>false</tt> until another write
     * operation fails and invokes {@link #setError()}.
     *
     * @since 1.6
     */
    protected void clearError() {
        trouble.remove();
    }

    /*
     * Exception-catching, synchronized output operations,
     * which also implement the write() methods of OutputStream
     */

    /**
     * Writes the specified byte to this stream.  If the byte is a newline and
     * automatic flushing is enabled then the <code>flush</code> method will be
     * invoked.
     *
     * <p> Note that the byte is written as given; to write a character that
     * will be translated according to the platform's default character
     * encoding, use the <code>print(char)</code> or <code>println(char)</code>
     * methods.
     *
     * @param  b  The byte to be written
     * @see #print(char)
     * @see #println(char)
     */
    public void write(int b) {
        try {
            ensureOpen();
            out.get().write(b);
            if ((b == '\n'))
                out.get().flush();
        }
        catch (InterruptedIOException x) {
            Thread.currentThread().interrupt();
        }
        catch (IOException x) {
            trouble.set(true);
        }
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array starting at
     * offset <code>off</code> to this stream.  If automatic flushing is
     * enabled then the <code>flush</code> method will be invoked.
     *
     * <p> Note that the bytes will be written as given; to write characters
     * that will be translated according to the platform's default character
     * encoding, use the <code>print(char)</code> or <code>println(char)</code>
     * methods.
     *
     * @param  buf   A byte array
     * @param  off   Offset from which to start taking bytes
     * @param  len   Number of bytes to write
     */
    public void write(byte buf[], int off, int len) {
        try {
            ensureOpen();
            out.get().write(buf, off, len);
        }
        catch (InterruptedIOException x) {
            Thread.currentThread().interrupt();
        }
        catch (IOException x) {
            trouble.set(true);
        }
    }

    /*
     * The following private methods on the text- and character-output streams
     * always flush the stream buffers, so that writes to the underlying byte
     * stream occur as promptly as with the original PrintStream.
     */

    private void write(char buf[]) {
        try {
            ensureOpen();
            out.get().write(new String(buf).getBytes());
        }
        catch (InterruptedIOException x) {
            Thread.currentThread().interrupt();
        }
        catch (IOException x) {
            trouble.set(true);
        }
    }

    private void write(String s) {
        try {
            ensureOpen();
            out.get().write(s.getBytes());
        }
        catch (InterruptedIOException x) {
            Thread.currentThread().interrupt();
        }
        catch (IOException x) {
            trouble.set(true);
        }
    }

    private void newLine() {
        try {
            ensureOpen();
            out.get().write(System.lineSeparator().getBytes());
        }
        catch (InterruptedIOException x) {
            Thread.currentThread().interrupt();
        }
        catch (IOException x) {
            trouble.set(true);
        }
    }

    /* Methods that do not terminate lines */

    /**
     * Prints a boolean value.  The string produced by <code>{@link
     * java.lang.String#valueOf(boolean)}</code> is translated into bytes
     * according to the platform's default character encoding, and these bytes
     * are written in exactly the manner of the
     * <code>{@link #write(int)}</code> method.
     *
     * @param      b   The <code>boolean</code> to be printed
     */
    public void print(boolean b) {
        write(b ? "true" : "false");
    }

    /**
     * Prints a character.  The character is translated into one or more bytes
     * according to the platform's default character encoding, and these bytes
     * are written in exactly the manner of the
     * <code>{@link #write(int)}</code> method.
     *
     * @param      c   The <code>char</code> to be printed
     */
    public void print(char c) {
        write(String.valueOf(c));
    }

    /**
     * Prints an integer.  The string produced by <code>{@link
     * java.lang.String#valueOf(int)}</code> is translated into bytes
     * according to the platform's default character encoding, and these bytes
     * are written in exactly the manner of the
     * <code>{@link #write(int)}</code> method.
     *
     * @param      i   The <code>int</code> to be printed
     * @see        java.lang.Integer#toString(int)
     */
    public void print(int i) {
        write(String.valueOf(i));
    }

    /**
     * Prints a long integer.  The string produced by <code>{@link
     * java.lang.String#valueOf(long)}</code> is translated into bytes
     * according to the platform's default character encoding, and these bytes
     * are written in exactly the manner of the
     * <code>{@link #write(int)}</code> method.
     *
     * @param      l   The <code>long</code> to be printed
     * @see        java.lang.Long#toString(long)
     */
    public void print(long l) {
        write(String.valueOf(l));
    }

    /**
     * Prints a floating-point number.  The string produced by <code>{@link
     * java.lang.String#valueOf(float)}</code> is translated into bytes
     * according to the platform's default character encoding, and these bytes
     * are written in exactly the manner of the
     * <code>{@link #write(int)}</code> method.
     *
     * @param      f   The <code>float</code> to be printed
     * @see        java.lang.Float#toString(float)
     */
    public void print(float f) {
        write(String.valueOf(f));
    }

    /**
     * Prints a double-precision floating-point number.  The string produced by
     * <code>{@link java.lang.String#valueOf(double)}</code> is translated into
     * bytes according to the platform's default character encoding, and these
     * bytes are written in exactly the manner of the <code>{@link
     * #write(int)}</code> method.
     *
     * @param      d   The <code>double</code> to be printed
     * @see        java.lang.Double#toString(double)
     */
    public void print(double d) {
        write(String.valueOf(d));
    }

    /**
     * Prints an array of characters.  The characters are converted into bytes
     * according to the platform's default character encoding, and these bytes
     * are written in exactly the manner of the
     * <code>{@link #write(int)}</code> method.
     *
     * @param      s   The array of chars to be printed
     *
     * @throws  NullPointerException  If <code>s</code> is <code>null</code>
     */
    public void print(char s[]) {
        write(s);
    }

    /**
     * Prints a string.  If the argument is <code>null</code> then the string
     * <code>"null"</code> is printed.  Otherwise, the string's characters are
     * converted into bytes according to the platform's default character
     * encoding, and these bytes are written in exactly the manner of the
     * <code>{@link #write(int)}</code> method.
     *
     * @param      s   The <code>String</code> to be printed
     */
    public void print(String s) {
        if (s == null) {
            s = "null";
        }
        write(s);
    }

    /**
     * Prints an object.  The string produced by the <code>{@link
     * java.lang.String#valueOf(Object)}</code> method is translated into bytes
     * according to the platform's default character encoding, and these bytes
     * are written in exactly the manner of the
     * <code>{@link #write(int)}</code> method.
     *
     * @param      obj   The <code>Object</code> to be printed
     * @see        java.lang.Object#toString()
     */
    public void print(Object obj) {
        write(String.valueOf(obj));
    }


    /* Methods that do terminate lines */

    /**
     * Terminates the current line by writing the line separator string.  The
     * line separator string is defined by the system property
     * <code>line.separator</code>, and is not necessarily a single newline
     * character (<code>'\n'</code>).
     */
    public void println() {
        newLine();
    }

    /**
     * Prints a boolean and then terminate the line.  This method behaves as
     * though it invokes <code>{@link #print(boolean)}</code> and then
     * <code>{@link #println()}</code>.
     *
     * @param x  The <code>boolean</code> to be printed
     */
    public void println(boolean x) {
        print(x);
        newLine();
    }

    /**
     * Prints a character and then terminate the line.  This method behaves as
     * though it invokes <code>{@link #print(char)}</code> and then
     * <code>{@link #println()}</code>.
     *
     * @param x  The <code>char</code> to be printed.
     */
    public void println(char x) {
        print(x);
        newLine();
    }

    /**
     * Prints an integer and then terminate the line.  This method behaves as
     * though it invokes <code>{@link #print(int)}</code> and then
     * <code>{@link #println()}</code>.
     *
     * @param x  The <code>int</code> to be printed.
     */
    public void println(int x) {
        print(x);
        newLine();
    }

    /**
     * Prints a long and then terminate the line.  This method behaves as
     * though it invokes <code>{@link #print(long)}</code> and then
     * <code>{@link #println()}</code>.
     *
     * @param x  a The <code>long</code> to be printed.
     */
    public void println(long x) {
        print(x);
        newLine();
    }

    /**
     * Prints a float and then terminate the line.  This method behaves as
     * though it invokes <code>{@link #print(float)}</code> and then
     * <code>{@link #println()}</code>.
     *
     * @param x  The <code>float</code> to be printed.
     */
    public void println(float x) {
        print(x);
        newLine();
    }

    /**
     * Prints a double and then terminate the line.  This method behaves as
     * though it invokes <code>{@link #print(double)}</code> and then
     * <code>{@link #println()}</code>.
     *
     * @param x  The <code>double</code> to be printed.
     */
    public void println(double x) {
        print(x);
        newLine();
    }

    /**
     * Prints an array of characters and then terminate the line.  This method
     * behaves as though it invokes <code>{@link #print(char[])}</code> and
     * then <code>{@link #println()}</code>.
     *
     * @param x  an array of chars to print.
     */
    public void println(char x[]) {
        print(x);
        newLine();
    }

    /**
     * Prints a String and then terminate the line.  This method behaves as
     * though it invokes <code>{@link #print(String)}</code> and then
     * <code>{@link #println()}</code>.
     *
     * @param x  The <code>String</code> to be printed.
     */
    public void println(String x) {
        print(x);
        newLine();
    }

    /**
     * Prints an Object and then terminate the line.  This method calls
     * at first String.valueOf(x) to get the printed object's string value,
     * then behaves as
     * though it invokes <code>{@link #print(String)}</code> and then
     * <code>{@link #println()}</code>.
     *
     * @param x  The <code>Object</code> to be printed.
     */
    public void println(Object x) {
        String s = String.valueOf(x);
        print(s);
        newLine();
    }
}
