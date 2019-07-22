package org.mcservice;

import java.lang.reflect.Method;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class MockedApplicationExtension implements BeforeTestExecutionCallback, AfterEachCallback{
	
	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		if(context.getTestInstance().isPresent()) {
			Object testObject=context.getTestInstance().get();
			if (testObject instanceof MockedApplicationTest) {
				((MockedApplicationTest) testObject).tearDownTestStage();
			}
		}
	}

	@Override
	public void beforeTestExecution(ExtensionContext context) throws Exception {
		if(context.getTestInstance().isPresent()) {
			Object testObject=context.getTestInstance().get();
			if (testObject instanceof MockedApplicationTest) {
				((MockedApplicationTest) testObject).setUpTestStage();
			}
			if(context.getTestClass().isPresent()) {
				for(Method method : context.getTestClass().get().getMethods()) {
					if(method.getAnnotation(AfterFXInitBeforeEach.class)!=null) {
						@SuppressWarnings("deprecation")
						boolean oldAccess=method.isAccessible();
						method.setAccessible(true);
						method.invoke(testObject);
						method.setAccessible(oldAccess);
					}
				}
			}
		}
	}

}
