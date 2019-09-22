/*******************************************************************************
 * Copyright (C) 2019 Sebastian Müller <sebastian.mueller@mcservice.de>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
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
