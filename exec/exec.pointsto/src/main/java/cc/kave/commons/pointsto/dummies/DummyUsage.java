/**
 * Copyright 2015 Simon Reuß
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package cc.kave.commons.pointsto.dummies;

import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

import cc.kave.commons.model.names.MethodName;
import cc.kave.commons.model.names.TypeName;
//import cc.recommenders.usages.CallSiteKind;

/**
 * Dummy class to act as temporary Usage implementation.
 */
public class DummyUsage {

	private TypeName type;
	private TypeName classContext;
	private MethodName methodContext;
	private DummyDefinitionSite definitionSite;
	private Set<DummyCallsite> callsites = new HashSet<>();

	public void setType(TypeName typeName) {
		this.type = typeName;
	}

	public TypeName getType() {
		return type;
	}

	public void setClassContext(TypeName typeName) {
		this.classContext = typeName;
	}

	public TypeName getClassContext() {
		return classContext;
	}

	public void setMethodContext(MethodName methodName) {
		this.methodContext = methodName;
	}

	public MethodName getMethodContext() {

		return methodContext;
	}

	public void setDefinitionSite(DummyDefinitionSite defsite) {
		this.definitionSite = defsite;
	}

	public DummyDefinitionSite getDefinitionSite() {
		return definitionSite;
	}

	public Set<DummyCallsite> getAllCallsites() {
		return callsites;
	}

	public void addCallsite(DummyCallsite callsite) {
		callsites.add(callsite);
	}

//	public Set<DummyCallsite> getReceiverCallsites() {
//		return Sets.filter(callsites, new Predicate<DummyCallsite>() {
//
//			@Override
//			public boolean apply(DummyCallsite input) {
//				return input.getKind() == CallSiteKind.RECEIVER;
//			}
//		});
//	}
//
//	public Set<DummyCallsite> getParameterCallsites() {
//		return Sets.filter(callsites, new Predicate<DummyCallsite>() {
//
//			@Override
//			public boolean apply(DummyCallsite input) {
//				return input.getKind() == CallSiteKind.PARAMETER;
//			}
//		});
//	}

}