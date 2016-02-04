/**
 * Copyright 2016 Simon Reuß
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
package cc.kave.commons.pointsto.analysis.unification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cc.kave.commons.model.ssts.references.IEventReference;
import cc.kave.commons.model.ssts.references.IFieldReference;
import cc.kave.commons.model.ssts.references.IMethodReference;
import cc.kave.commons.model.ssts.references.IPropertyReference;
import cc.kave.commons.model.ssts.references.IUnknownReference;
import cc.kave.commons.model.ssts.references.IVariableReference;
import cc.kave.commons.pointsto.analysis.visitors.FailSafeNodeVisitor;

class MethodParameterVisitor extends FailSafeNodeVisitor<ContextLocationPair, Void> {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodParameterVisitor.class);

	@Override
	public Void visit(IVariableReference varRef, ContextLocationPair context) {
		context.getAnalysisContext().registerParameterReference(context.getLocation(), varRef);
		return null;
	}

	@Override
	public Void visit(IFieldReference fieldRef, ContextLocationPair context) {
		context.getAnalysisContext().registerParameterReference(context.getLocation(), fieldRef);
		return null;
	}

	@Override
	public Void visit(IPropertyReference propertyRef, ContextLocationPair context) {
		context.getAnalysisContext().registerParameterReference(context.getLocation(), propertyRef);
		return null;
	}

	@Override
	public Void visit(IMethodReference methodRef, ContextLocationPair context) {
		context.getAnalysisContext().registerParameterReference(context.getLocation(), methodRef);
		return null;
	}

	@Override
	public Void visit(IEventReference eventRef, ContextLocationPair context) {
		LOGGER.info("Ignoring event reference");
		return null;
	}

	@Override
	public Void visit(IUnknownReference unknownRef, ContextLocationPair context) {
		LOGGER.error("Ignoring an unknown reference");
		return null;
	}
}
