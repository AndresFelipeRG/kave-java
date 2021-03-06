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
package cc.kave.episodes.export;

import java.util.List;

import com.google.common.collect.Lists;

import cc.kave.commons.model.events.completionevents.Context;
import cc.kave.commons.model.naming.Names;
import cc.kave.commons.model.naming.codeelements.IMethodName;
import cc.kave.commons.model.ssts.ISST;
import cc.kave.commons.model.ssts.declarations.IMethodDeclaration;
import cc.kave.commons.model.ssts.expressions.assignable.IInvocationExpression;
import cc.kave.commons.model.ssts.expressions.assignable.ILambdaExpression;
import cc.kave.commons.model.ssts.impl.visitor.AbstractTraversingNodeVisitor;
import cc.kave.commons.model.typeshapes.IMethodHierarchy;
import cc.kave.commons.model.typeshapes.ITypeShape;
import cc.kave.commons.utils.TypeErasure;
import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.model.events.Events;

public class EventStreamGenerator {

	private List<Event> events = Lists.newLinkedList();

	public void add(Context ctx) {
		ISST sst = ctx.getSST();
		if (!sst.isPartialClass()) {
			sst.accept(new EventStreamGenerationVisitor(), ctx.getTypeShape());
			// System.out.println(ctx.getSST().getEnclosingType() + "-> " +
			// ctx.getSST().getPartialClassIdentifier());
		}
		// sst.accept(new EventStreamGenerationVisitor(), ctx.getTypeShape());
	}

	public List<Event> getEventStream() {
		return events;
	}

	private class EventStreamGenerationVisitor extends AbstractTraversingNodeVisitor<ITypeShape, Void> {

		// private IMethodName currentCtx;
		private IMethodName firstCtx;
		private IMethodName superCtx;
		private IMethodName elementCtx;

		// private IMethodName debug = MethodName.newMethodName(
		// "[System.Void, mscorlib, 4.0.0.0]
		// [ACAT.Lib.Core.PanelManagement.MenuPanelBase,
		// Core].InitializeComponent()");

		@Override
		public Void visit(IMethodDeclaration decl, ITypeShape context) {

			// if (debug.equals(decl.getName())) {
			// System.out.println();
			// }

			// currentCtx = MethodName.UNKNOWN_NAME;
			firstCtx = Names.getUnknownMethod();
			superCtx = Names.getUnknownMethod();
			elementCtx = Names.getUnknownMethod();
			IMethodName name = decl.getName();
			for (IMethodHierarchy h : context.getMethodHierarchies()) {
				if (h.getElement().equals(name)) {
					if (h.getFirst() != null) {
						firstCtx = h.getFirst();
					}
					if (h.getSuper() != null) {
						superCtx = h.getSuper();
					}
					elementCtx = h.getElement();
					// if (h.getFirst() != null) {
					// currentCtx = h.getFirst();
					// } else if (h.getSuper() != null) {
					// currentCtx = h.getSuper();
					// }
				}
			}
			return super.visit(decl, context);
		}

		@Override
		public Void visit(IInvocationExpression inv, ITypeShape context) {
			if (shouldInclude(inv.getMethodName())) {
				addEnclosingMethodIfAvailable();
				events.add(Events.newInvocation(TypeErasure.of(inv.getMethodName())));
			}
			return null;
		}

		@Override
		public Void visit(ILambdaExpression inv, ITypeShape context) {
			// stop here for now!
			return null;
		}

		private boolean shouldInclude(IMethodName name) {
			if (name.isUnknown()) {
				return false;
			}

			// enable to speed up evaluation (but do not commit!)
			// IAssemblyName mscorlib = AssemblyName.newAssemblyName("mscorlib,
			// 4.0.0.0");
			// IAssemblyName actualLib = name.getDeclaringType().getAssembly();
			// if (!mscorlib.equals(actualLib)) {
			// return false;
			// }

			return true;
		}

		private void addEnclosingMethodIfAvailable() {
			// Asserts.assertNotNull(currentCtx);
			// events.add(Events.newContext(currentCtx));
			// currentCtx = null;

			// if (currentCtx != null) {
			// events.add(Events.newContext(currentCtx));
			// currentCtx = null;
			// }
			if (firstCtx != null) {
				events.add(Events.newFirstContext(TypeErasure.of(firstCtx)));
				firstCtx = null;
			}
			if (superCtx != null) {
				Event superEvent = Events.newSuperContext(TypeErasure.of(superCtx));
				if (!superEvent.getMethod().isUnknown()) {
					events.add(superEvent);
				}
				superCtx = null;
			}
			if (elementCtx != null) {
				events.add(Events.newContext(TypeErasure.of(elementCtx)));
				elementCtx = null;
			}
		}
	}
}