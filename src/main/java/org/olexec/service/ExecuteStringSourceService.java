package org.olexec.service;

import org.olexec.compile.StringSourceCompiler;
import org.olexec.execute.JavaClassExecuter;
import org.springframework.stereotype.Service;

@Service
public class ExecuteStringSourceService {
    public String execute(String source) {
        byte[] classBytes = StringSourceCompiler.compile(source);
        return JavaClassExecuter.execute(classBytes);
    }
}
