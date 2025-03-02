package com.assessment.speernotes.setup;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.support.AbstractTestExecutionListener;

public class TestSetup extends AbstractTestExecutionListener implements TestExecutionListener {
    @Override
    public void beforeTestClass(TestContext testContext) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMalformed().ignoreIfMissing().load();
        dotenv.entries().forEach(entry -> {
            if (System.getProperty(entry.getKey()) == null) { // Only set if not already defined
                System.setProperty(entry.getKey(), entry.getValue());
            }
        });
        System.out.println("✅ Environment variables loaded from .env file");
    }
}
