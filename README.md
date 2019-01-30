# 在线执行 Java 代码实现原理

在线执行 Java 代码的实现流程如下图所示：

![在线执行Java代码实现流程.jpg](./doc/pic/在线执行Java代码实现流程.jpg)

通过观察上图可以发现，我们的重点在于实现 `StringSourceCompiler` 和 `JavaClassExecuter` 两个类。它们的作用分别为：

- `StringSourceCompiler`：将字符串形式的源代码 String source 编译成字节码 byte[] classBytes；
- `JavaClassExecuter`：将字节码 byte[] classBytes 加载进 JVM，执行其入口方法，并收集运行输出结果字符串返回。

> **Note：** 我们只收集 `System.out` 和 `System.err` 输出的内容返回给客户端。



## `StringSourceCompiler` 的实现

通过 JDK 1.6 后新加的动态编译实现 `StringSourceCompiler`，使用动态编译，可以直接在内存中将源代码字符串编译为字节码的字节数组，这样既不会污染环境，又不会额外的引入 IO 操作，一举两得。

具体实现以及原理说明详见：[动态编译](./doc/01-动态编译.md)

## `JavaClassExecuter` 的实现

`JavaClassExecuter` 的实现分为以下两步：

- [执行字节码的入口方法（main 方法）](./doc/02-执行字节码的入口方法.md)
- [收集代码执行结果](./doc/03-收集代码执行结果.md)