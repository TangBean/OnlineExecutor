package org.olexec.service;

import org.olexec.compile.StringSourceCompiler;
import org.olexec.execute.JavaClassExecutor;
import org.springframework.stereotype.Service;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import java.util.List;
import java.util.concurrent.*;

@Service
public class ExecuteStringSourceService {
    private static final int RUN_TIME_LIMITED = 15;

    public String execute(String source) {
        DiagnosticCollector<JavaFileObject> compileCollector = new DiagnosticCollector<>(); // 编译结果收集器

        // 编译源代码
        byte[] classBytes = StringSourceCompiler.compile(source, compileCollector);

        // 编译不通过，获取并返回编译错误信息
        if (classBytes == null) {
            // 获取编译错误信息
            List<Diagnostic<? extends JavaFileObject>> compileError = compileCollector.getDiagnostics();
            StringBuilder compileErrorRes = new StringBuilder();
            for (Diagnostic diagnostic : compileError) {
                compileErrorRes.append("Compilation error at ");
                compileErrorRes.append(diagnostic.getLineNumber());
                compileErrorRes.append(".");
                compileErrorRes.append(System.lineSeparator());
            }
            return compileErrorRes.toString();
        }

        // 运行字节码的main方法
        ExecutorService pool = Executors.newSingleThreadExecutor();
        Callable<String> runTask = new Callable<String>() {
            @Override
            public String call() throws Exception {
                return JavaClassExecutor.execute(classBytes);
            }
        };
        Future<String> res = pool.submit(runTask);

        // 获取运行结果，处理非客户端代码错误
        String runResult;
        try {
            runResult = res.get(RUN_TIME_LIMITED, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            runResult = "Program interrupted.";
        } catch (ExecutionException e) {
            runResult = e.getCause().getMessage();
        } catch (TimeoutException e) {
            runResult = "Time Limit Exceeded.";
        } finally {
            pool.shutdown();
        }
        return runResult;
    }
}
