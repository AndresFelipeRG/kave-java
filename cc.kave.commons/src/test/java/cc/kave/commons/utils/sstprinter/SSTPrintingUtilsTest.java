/**
 * Copyright 2016 Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cc.kave.commons.utils.sstprinter;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import cc.kave.commons.model.names.INamespaceName;
import cc.kave.commons.model.names.csharp.NamespaceName;
import cc.kave.commons.utils.sstprinter.SSTPrintingContext;
import cc.kave.commons.utils.sstprinter.SSTPrintingUtils;

public class SSTPrintingUtilsTest {

	@Test
	public void testUsingListFormattedCorrectly() {
		Set<INamespaceName> namespaces = new HashSet<>();
		namespaces.add(NamespaceName.newNamespaceName("Z"));
		namespaces.add(NamespaceName.newNamespaceName("System"));
		namespaces.add(NamespaceName.newNamespaceName("System"));
		namespaces.add(NamespaceName.newNamespaceName("System.Collections.Generic"));
		namespaces.add(NamespaceName.newNamespaceName("A"));
		namespaces.add(NamespaceName.getGlobalNamespace());

		SSTPrintingContext context = new SSTPrintingContext();
		SSTPrintingUtils.formatAsUsingList(namespaces, context);
		String expected = String.join("\n", "using A;", "using System;", "using System.Collections.Generic;",
				"using Z;");
		Assert.assertEquals(expected, context.toString());
	}

	@Test
	public void testUnknownNameIsNotAddedToList() {
		Set<INamespaceName> namespaces = new HashSet<>();
		namespaces.add(NamespaceName.UNKNOWN_NAME);
		namespaces.add(NamespaceName.getGlobalNamespace());

		SSTPrintingContext context = new SSTPrintingContext();
		SSTPrintingUtils.formatAsUsingList(namespaces, context);
		String expected = "";
		Assert.assertEquals(expected, context.toString());
	}

}
