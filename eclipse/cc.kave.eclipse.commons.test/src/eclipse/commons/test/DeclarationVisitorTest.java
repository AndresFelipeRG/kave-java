/**
 * Copyright 2015 Waldemar Graf
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eclipse.commons.test;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import cc.kave.commons.model.names.DelegateTypeName;
import cc.kave.commons.model.names.MemberName;
import cc.kave.commons.model.names.csharp.CsDelegateTypeName;
import cc.kave.commons.model.names.csharp.CsMemberName;
import cc.kave.commons.model.names.csharp.CsMethodName;
import cc.kave.commons.model.ssts.declarations.IMethodDeclaration;
import cc.kave.commons.model.ssts.impl.SST;
import cc.kave.commons.model.ssts.impl.declarations.FieldDeclaration;
import cc.kave.commons.model.ssts.impl.statements.ReturnStatement;
import cc.kave.eclipse.commons.analysis.completiontarget.CompletionTargetMarker;
import cc.kave.eclipse.commons.analysis.transformer.BodyVisitor;
import cc.kave.eclipse.commons.analysis.transformer.DeclarationVisitor;
import cc.kave.eclipse.commons.analysis.util.UniqueVariableNameGenerator;

public class DeclarationVisitorTest extends BaseDeclarationTest {

	@BeforeClass
	public static void initialize() {
		PluginAstParser parser = new PluginAstParser("test.project.a", "example.classes;TestClass.java");
		context = parser.getContext();
	}

	@Test
	public void fieldTest() {
		FieldDeclaration expected = getFieldDeclaration("[%int, rt.jar, 1.8] [example.classes.TestClass, ?].intTest");

		assertTrue(containsField(expected));
		assertThat(context.getFields(), hasItem(expected));
	}

	@Test
	public void methodTest() {
		String methodName = "[%void, rt.jar, 1.8] [example.classes.TestClass, ?].method()";
		IMethodDeclaration expected = newMethodDeclaration(methodName);
		
		assertThat(context.getMethods(), hasItem(expected));
	}

	@Test
	public void constructorTest() {
		String methodName = "[example.classes.TestClass, ?] [example.classes.TestClass, ?]..ctor([java.lang.String, rt.jar, 1.8] a, [%int, rt.jar, 1.8] b)";

		assertTrue(containsMethod(methodName));
	}
	
	

	@Test
	public void isNestedDeclarationTest() {
		Class[] params = { DelegateTypeName.class, SST.class };
		Method method;

		try {
			method = DeclarationVisitor.class.getDeclaredMethod("isNestedDeclaration", params);
			method.setAccessible(true);
			DelegateTypeName dName = CsDelegateTypeName.newDelegateTypeName("d:[R, P] [O+D, P].()");
			System.out.println(method.invoke(null, dName, context));
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			e.printStackTrace();
		}

		try {
			params[0] = MemberName.class;
			method = DeclarationVisitor.class.getDeclaredMethod("isNestedDeclaration", params);
			method.setAccessible(true);

			MemberName mName = CsMethodName.newMethodName("[System.Void, mscore, 4.0.0.0] [T, P, 1.2.3.4].MethodName()");
			System.out.println(method.invoke(null, mName, context));
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			e.printStackTrace();
		}
	}
}
