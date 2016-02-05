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
package cc.kave.commons.utils.sstprinter.visitortestsuite;

import org.junit.Ignore;
import org.junit.Test;

import cc.kave.commons.model.names.csharp.EventName;
import cc.kave.commons.model.names.csharp.FieldName;
import cc.kave.commons.model.names.csharp.MethodName;
import cc.kave.commons.model.names.csharp.PropertyName;
import cc.kave.commons.model.ssts.impl.SSTUtil;
import cc.kave.commons.model.ssts.impl.references.EventReference;
import cc.kave.commons.model.ssts.impl.references.FieldReference;
import cc.kave.commons.model.ssts.impl.references.IndexAccessReference;
import cc.kave.commons.model.ssts.impl.references.MethodReference;
import cc.kave.commons.model.ssts.impl.references.PropertyReference;
import cc.kave.commons.model.ssts.impl.references.UnknownReference;
import cc.kave.commons.model.ssts.references.IVariableReference;

public class ReferencePrinterTest extends SSTPrintingVisitorBaseTest {
	@Test
	public void testVariableReference() {
		IVariableReference sst = SSTUtil.variableReference("variable");
		assertPrint(sst, "variable");
	}

	@Test
	public void testEventReference() {
		EventReference sst = new EventReference();
		sst.setEventName(EventName.newEventName("[EventType,P] [DeclaringType,P].E"));
		sst.setReference(SSTUtil.variableReference("o"));

		assertPrint(sst, "o.E");
	}

	@Test
	public void testFieldReference() {
		FieldReference sst = new FieldReference();
		sst.setFieldName(FieldName.newFieldName("[FieldType,P] [DeclaringType,P].F"));
		sst.setReference(SSTUtil.variableReference("o"));

		assertPrint(sst, "o.F");
	}

	@Test
	public void testMethodReference() {
		MethodReference sst = new MethodReference();
		sst.setMethodName(MethodName.newMethodName("[ReturnType,P] [DeclaringType,P].M([ParameterType,P] p)"));
		sst.setReference(SSTUtil.variableReference("o"));

		assertPrint(sst, "o.M");
	}

	@Test
	public void testPropertyReference() {
		PropertyReference sst = new PropertyReference();
		sst.setPropertyName(PropertyName.newPropertyName("get set [PropertyType,P] [DeclaringType,P].P"));
		sst.setReference(SSTUtil.variableReference("o"));

		assertPrint(sst, "o.P");
	}

	@Test
	public void testUnknownReference() {
		UnknownReference sst = new UnknownReference();
		assertPrint(sst, "???");
	}
	
	@Test
	public void testIndexAccessReference() {
		IndexAccessReference sst = new IndexAccessReference();
		assertPrint(sst, "???");
	}
	
	
	
	
}
