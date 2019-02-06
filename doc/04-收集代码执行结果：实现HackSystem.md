# 收集代码执行结果：实现 HackSystem

<!-- TOC -->

- [收集代码执行结果：实现 HackSystem](#%E6%94%B6%E9%9B%86%E4%BB%A3%E7%A0%81%E6%89%A7%E8%A1%8C%E7%BB%93%E6%9E%9C%E5%AE%9E%E7%8E%B0-hacksystem)
  - [System 类详细解析](#system-%E7%B1%BB%E8%AF%A6%E7%BB%86%E8%A7%A3%E6%9E%90)
  - [HackSystem](#hacksystem)
  - [HackPrintStream](#hackprintstream)
    - [ensureOpen 方法](#ensureopen-%E6%96%B9%E6%B3%95)
    - [close 方法](#close-%E6%96%B9%E6%B3%95)
    - [write 方法](#write-%E6%96%B9%E6%B3%95)

<!-- /TOC -->

客户端程序主要通过将程序中的运行结果通过标准输出打印至控制台进行观察，正如我们前面说过的，标准输出是虚拟机全局共享的资源，我们不可能让客户端传来的程序和服务器本身抢夺 System 资源。所以我们通过模仿 System 重写了一个 HackSystem 替换掉对 System 的调用，从而将客户端程序的标准输出和我们服务器的标准输出隔离开来。

但这将引出另一个问题：尽管客户端发来的程序将对 System 的方法调用的调用都替换为了 HackSystem 的方法的调用，从而避免了与服务器本身发生资源冲突，可是在同一时刻，可能有多个待运行的程序从客户端发来（假设为程序 A，B，C），对于 A，B，C 三个程序，它们是共享 HackSystem 的，即它们会在 HackSystem 发生资源争夺。最简单的处理方法就是将客户端发来的运行程序的请求完全变成串行的，也就是运行完一个客户端发来的程序再运行另一个，这种方法是完全不可取的，因为可能有一个程序执行了一个超长循环要跑好久，而其他执行的很快的程序只能等着它执行完。

为了解决这个并发问题，我们需要将 HackSystem 变成一个线程安全的类，本项目的问题十分适合通过线程封闭的方式来解决，详细的解决方法我们将在后面进行说明。

本篇文章中的重点是模仿一个 System 类来替换原有的 ，要做到知己知彼，我们首先需要先了解一下 System 类。



## System 类详细解析

System 类，正如其名“系统”，是在 Java 程序中作为一个标准的系统类，与 Class 类一样的直接注册进虚拟机，也就是说，是一个直接与虚拟机打交道的类，它实现了：

- 控制台与程序之间的输入输出流的控制；
- 系统的初始化；
- 获取系统环境变量；
- 数组的复制；
- 返回一个精准的时间；
- 一些简单的对虚拟机的操作等。

System 在 java.lang 包中，作为 Java 语言的核心特性，它是一个不可被实例化的类，只有一个什么都没写的私有空参构造函数来禁止别人创建 System 实例：

```java
private System() {
}
```

System 中公有的属性只有 3 个，即标准输入流，标准输出流和标准错误流：

```java
public final static InputStream in = null; // 源码里final static反着写的，看起来有点不爽...
public final static PrintStream out = null;
public final static PrintStream err = null;
```

这 3 个字段都是 `static final` 的，并且 `out` 和 `err` 都是 PrintStream，它们都是 PrintStream，这很重要，因为 PrintStream 这个流有点特别， **它是用来装饰其它输出流的，能为其他输出流添加了功能，使它们能够方便地打印各种数据值表示形式** 。所以它所有的构造方法都会要求我们传入一个流或者一个可以变成流的东西（如文件名等）。与其他输出流不同， **PrintStream 永远不会抛出 IOException** ，它一旦产生的 IOException，不会再次把它抛出去，而是将它的 trouble 字段设置为 true，这样用户就可以通过 `checkError()` 返回错误标记，从而查看 PrintStream 内部是否产生 IOException 了。

PrintStream 中有许多 print 方法，这些 print 的方法会将想要打印进它所装饰的输出流的内容写入，这些方法一般都是通过调用 PrintStream 中的各种 write 方法实现的。因为 PrintStream 只装饰了一个输出流，但同时可能有多个线程要向这个输出流写入内容，所以我们发现，PrintStream 中所有需要向输出流中写入内容的地方都进行了同步，比如：

```java
private void write(String s) {
    try {
        synchronized (this) {
            ensureOpen();
            textOut.write(s);
            textOut.flushBuffer();
            charOut.flushBuffer();
            if (autoFlush && (s.indexOf('\n') >= 0))
                out.flush();
        }
    }
    catch (InterruptedIOException x) {
        Thread.currentThread().interrupt();
    }
    catch (IOException x) {
        trouble = true;
    }
}
```

如此详细的介绍 PrintStream 就是为了说明，System 类中本来的 PrintStream 本质上并不符合本项目的要求，因为它的作用是将多个输出格式化后并写入到一个流中，而在本项目中，我们要能同时运行多个客户端程序， **并且将它们的标准输出打印到不同的流中** 。也就是说，除了要将 System 类重写为 HackSystem 外，我们的 HackSystem 类中的 `out` 和 `err` 属性需要一种特殊的装饰，首先它本质上还要是一个 PrintStream，这样才能让我们的 HackSystem 好好的伪装 System，其次，它内部装饰的不是一个流，而是多个流，即每一个调用 HackSystem 中方法的线程都会给自己创建一个新的流用于存储输出结果。即我们需要进行以下 2 个替换操作：

- 将 System 替换为 HackSystem；
- 将 HackSystem 的 `PrintStream out` 和 `PrintStream err` 的本质替换为我们自己写的 HackPrintStream 实例。



## HackSystem

HackSystem 基本只要仿造 System 的写法即可，但需要做一些修改，相比于 System 类，我们首先需要对 `out` 和 `err` 两个字段的实际类型进行修改，修改为我们自己写的 HackPrintStream 对象：

```java
public final static PrintStream out = new HackPrintStream();
public final static PrintStream err = out;
```

然后新加两个方法，用来获取当前线程的输出流中的内容和关闭当前线程的输出流：

```java
public static String getBufferString() {
    return out.toString();
}

public static void closeBuffer() {
    out.close();
}
```

其次，对于一些比较危险的方法，我们要禁止客户端调用，客户端一旦调用类这些方法，直接抛出异常。例如：

```java
public static void exit(int status) {
    throw new SecurityException("Use hazardous method: System.exit().");
}
```

最后，对于一些不涉及系统的工具方法，可以按原样保留，直接在方法内部调用 System 的方法即可。例如：

```java
public static void arraycopy(Object src,  int srcPos, Object dest, int destPos, int length) {
    System.arraycopy(src, srcPos, dest, destPos, length);
}
```

HackSystem 这样就已经可以了，详细的实现可见 [HackSystem.java](../src/main/java/org/olexec/execute/HackSystem.java)

接下来我们将对 HackPrintStream 类的实现进行解说，这个类的实现是解除并发问题的关键。



## HackPrintStream

首先，HackPrintStream 要继承 PrintStream 类并重写 PrintStream 的所有公有方法，这是因为通过观察上一节的第一个代码片段，可以得知，在 HackSystem 中，我们要通过一个 PrintStream 型的引用来引用 HackPrintStream 的实例，所以 HackPrintStream 的实例需要能伪装成一个 PrintStream。

接下来，就是 HackPrintStream 的实现重点了，我们需要 HackPrintStream 能实现支持多个线程调用，并且可以将不同线程通过 PrintStream 打印到流中的内容打印到不同的流中，这样多个线程的标准输出的操作才不会互相影响，也就不存在并发问题了。这就需要我们为每个线程创建一个 OutputStream 来保存运行结果，并且将这个 OutputStream 封闭到线程中（这里我们采用了 ByteArrayOutputStream 类）。既然要实现线程封闭，那么最合适的工具就是 ThreadLocal 了，所以在 HackPrintStream 中，我们加入了如下字段，用来保存每个线程的标准输出流和每个线程的标准输出写入过程是否抛出 IOException。

```java
private ThreadLocal<ByteArrayOutputStream> out;
private ThreadLocal<Boolean> trouble;
```

> **ThreadLocal 实现原理：**
>
> - 每一个 ThreadLocal 都有一个唯一的的 ThreadLocalHashCode；
> - 每一个线程中有一个专门保存这个 HashCode 的 `Map<ThreadLocalHashCode, 对应变量的值>`；
> - 当 `ThreadLocal#get()` 时，实际上是当前线程先拿到这个 ThreadLocal 对象的 ThreadLocalHashCode，然后通过这个 ThreadLocalHashCode 去自己内部的 Map 中去取值。
> 	- 即每个线程对应的变量不是存储在 ThreadLocal 对象中的，而是存在当前线程对象中的，线程自己保管封存在自己内部的变量，达到线程封闭的目的。
> 	- 也就是说，ThreadLocal 对象并不负责保存数据，它只是一个访问入口。

在进行了以上的修改之后，我们还需要将 HackPrintStream 的父类 PrintStream 中所有对流进行操作的方法进行重写。我们下面将举几个例子，对如何重写父类的方法进行说明。

### ensureOpen 方法

PrintStream 中的实现：

```java
private void ensureOpen() throws IOException {
    if (out == null)
        throw new IOException("Stream closed");
}
```

重写为：

```java
private void ensureOpen() throws IOException {
    if (out.get() == null) { // 不是判断out是否为空，而是判断out.get()是否为空
        out.set(new ByteArrayOutputStream()); // 如果为空不再抛出异常，而是新建一个流给调用这个方法的线程
    }
}
```

### close 方法

PrintStream 中的实现：

```java
private boolean closing = false; /* To avoid recursive closing */

public void close() {
    synchronized (this) {
        if (!closing) {
            closing = true;
            try {
                textOut.close();
                out.close();
            }
            catch (IOException x) {
                trouble = true;
            }
            textOut = null;
            charOut = null;
            out = null;
        }
    }
}
```

重写为：

```java
public void close() {
    try {
        out.get().close(); // 关闭当前线程的OutputStream
    }
    catch (IOException x) {
        trouble.set(true);
    }
    out.remove(); // 将当前线程的OutputStream移除
}
```

### write 方法

PrintStream 中的实现：

```java
public void write(byte buf[], int off, int len) {
    try {
        synchronized (this) {
            ensureOpen();
            out.write(buf, off, len);
            if (autoFlush)
                out.flush();
        }
    }
    catch (InterruptedIOException x) {
        Thread.currentThread().interrupt();
    }
    catch (IOException x) {
        trouble = true;
    }
}
```

重写为：

```java
public void write(byte buf[], int off, int len) {
    try {
        ensureOpen();
        out.get().write(buf, off, len); // out.get()才是当前线程的OutputStream
    }
    catch (InterruptedIOException x) {
        Thread.currentThread().interrupt();
    }
    catch (IOException x) {
        trouble.set(true);
    }
}
```

按照以上方式对 PrintStream 中需要重写的方法进行重写，详细的实现可见 [HackPrintStream.java](../src/main/java/org/olexec/execute/HackPrintStream.java)