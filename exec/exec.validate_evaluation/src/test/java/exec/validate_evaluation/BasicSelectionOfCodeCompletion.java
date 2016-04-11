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
package exec.validate_evaluation;

import java.util.List;

import org.junit.Before;

import com.google.common.collect.Lists;

import cc.kave.commons.model.events.completionevents.CompletionEvent;
import cc.kave.commons.model.events.completionevents.ICompletionEvent;
import cc.kave.commons.model.ssts.expressions.IAssignableExpression;
import cc.kave.commons.model.ssts.impl.declarations.MethodDeclaration;
import cc.kave.commons.model.ssts.impl.expressions.simple.ConstantValueExpression;
import cc.kave.commons.model.ssts.impl.statements.Assignment;
import cc.kave.commons.model.ssts.statements.IAssignment;
import cc.recommenders.names.ICoReMethodName;
import cc.recommenders.usages.Usage;
import exec.validate_evaluation.microcommits.MicroCommit;

public class BasicSelectionOfCodeCompletion extends DataTransformationIntegrationBaseTest {

	@Before
	public void setup() {
	}

	@Override
	protected List<ICompletionEvent> getEvents() {

		MethodDeclaration md = newMethodDeclaration(10, 1, //
				varDecl(t(1), "o"), //
				assign("o", new ConstantValueExpression()), //
				invStmt("o", m(1, 1)), //
				complStmt("o"));
		
		/*
		 * Der resultierende SST sollte folgendem Code entsprechen:
		 * 
		 * class T10 {
		 *   void m1() { // T10.m1()
		 *      T1 o;    
		 *      o = -constant-
		 *      o.m1(); // T1.m1()
		 *      o.$
		 *   }
		 * }
		 */

		CompletionEvent ce = completionEvent(10, md, m(1, 2));

		return Lists.newArrayList(ce);
	}

	private IAssignment assign(String id, IAssignableExpression expr) {
		Assignment assignment = new Assignment();
		assignment.setReference(ref(id));
		assignment.setExpression(expr);
		return assignment;
	}

	@Override
	protected List<MicroCommit> getExpectedMicroCommits() {
		ICoReMethodName ctx = _m(10, 1);
		Usage a = _q(ctx, _t(1), _m(1, 1));
		Usage b = _q(ctx, _t(1), _m(1, 1), _m(1, 2));
		return Lists.newArrayList(MicroCommit.create(a, b));
	}
}