/**
 * Copyright 2016 Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package cc.kave.commons.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

import cc.kave.commons.model.naming.Names;
import cc.kave.commons.model.naming.codeelements.IMethodName;
import cc.kave.commons.model.naming.types.ITypeName;

public class TypeErasureTest {

	@Test
	public void noGenerics() {
		String inp = "T,P";
		String out = "T,P";
		assertRepl(inp, out);
	}

	@Test
	public void generic_unbound() {
		String inp = "T`1[[G]],P";
		String out = "T`1[[G]],P";
		assertRepl(inp, out);
	}

	@Test
	public void generic_bound() {
		String inp = "T`1[[G -> T2,P]],P";
		String out = "T`1[[G]],P";
		assertRepl(inp, out);
	}

	@Test
	public void generic_multiple_unbound() {
		String inp = "T`2[[G],[G2]],P";
		String out = "T`2[[G],[G2]],P";
		assertRepl(inp, out);
	}

	@Test
	public void generic_multiple_bound() {
		String inp = "T`2[[G -> T2,P],[G2 -> T3,P]],P";
		String out = "T`2[[G],[G2]],P";
		assertRepl(inp, out);
	}

	@Test
	public void nestedGenercis() {
		String inp = "T`1[[G1 -> T`1[[G2-> T2]],P]],P";
		String out = "T`1[[G1]],P";
		assertRepl(inp, out);
	}

	@Test
	public void strangeNestingSyntax_unbound() {
		String inp = "T`1+U`1[[G1],[G2],P";
		String out = "T`1+U`1[[G1],[G2],P";
		assertRepl(inp, out);
	}

	@Test
	@Ignore
	public void strangeNestingSyntax_bound() {
		String inp = "T`1+U`1[[G1 -> T2,P],[G2 -> T3,P]],P";
		String out = "T`1+U`1[[G1],[G2]],P";
		assertRepl(inp, out);
	}

	@Test
	public void method_happyPjgfath() {
		String inp = "[T,P] [T,P].M`2[[G1],[G2 -> T,P]]()";
		String out = "[T,P] [T,P].M`2[[G1],[G2]]()";
		assertRepl(inp, out);
	}

	@Test
	public void repeatingTheSameReplacement() {
		String inp = "[T`1[[G->T,P]],P] [T`1[[G->T,P]],P].M()";
		String out = "[T`1[[G]],P] [T`1[[G]],P].M()";
		assertRepl(inp, out);
	}

	@Test
	public void type_happyPath() {
		ITypeName inp = Names.newType("T`1[[G->T2,P]],P");
		ITypeName exp = Names.newType("T`1[[G]],P");
		ITypeName act = TypeErasure.of(inp);
		assertEquals(exp, act);
	}

	@Test
	public void method_happyPath() {
		IMethodName inp = Names.newMethod("[T,P] [T,P].M`1[[G1 -> T,P]]()");
		IMethodName exp = Names.newMethod("[T,P] [T,P].M`1[[G1]]()");
		IMethodName act = TypeErasure.of(inp);
		assertEquals(exp, act);
	}

	private void assertRepl(String in, String expected) {
		String actual = TypeErasure.of(in);
		assertEquals(expected, actual);
	}
}