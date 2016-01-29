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
package cc.kave.commons.pointsto.analysis;

import cc.kave.commons.model.names.EventName;
import cc.kave.commons.model.names.FieldName;
import cc.kave.commons.model.names.PropertyName;
import cc.kave.commons.model.names.csharp.CsMethodName;
import cc.kave.commons.model.ssts.IReference;
import cc.kave.commons.model.ssts.references.IEventReference;
import cc.kave.commons.model.ssts.references.IFieldReference;
import cc.kave.commons.model.ssts.references.IIndexAccessReference;
import cc.kave.commons.model.ssts.references.IMethodReference;
import cc.kave.commons.model.ssts.references.IPropertyReference;
import cc.kave.commons.model.ssts.references.IUnknownReference;
import cc.kave.commons.model.ssts.references.IVariableReference;

public class ReferenceNormalizationVisitor extends FailSafeNodeVisitor<Void, IReference> {

	@Override
	public IReference visit(IUnknownReference unknownRef, Void context) {
		return null;
	}

	@Override
	public IReference visit(IFieldReference fieldRef, Void context) {
		FieldName field = fieldRef.getFieldName();
		if (field.isUnknown()) {
			return null;
		} else if (!field.isStatic() && fieldRef.getReference().isMissing()) {
			return null;
		} else {
			return fieldRef;
		}
	}

	@Override
	public IReference visit(IVariableReference varRef, Void context) {
		return varRef.isMissing() ? null : varRef;
	}

	@Override
	public IReference visit(IPropertyReference propertyRef, Void context) {
		PropertyName property = propertyRef.getPropertyName();
		if (property.isUnknown()) {
			return null;
		} else if (!property.isStatic() && propertyRef.getReference().isMissing()) {
			return null;
		} else {
			return propertyRef;
		}
	}

	@Override
	public IReference visit(IIndexAccessReference indexAccessRef, Void context) {
		// map array accesses to the base variable
		return indexAccessRef.getExpression().getReference().accept(this, context);
	}

	@Override
	public IReference visit(IMethodReference methodRef, Void context) {
		// TODO replace with isUnknown once fixed
		if (CsMethodName.UNKNOWN_NAME.equals(methodRef.getMethodName())) {
			return null;
		} else {
			return methodRef;
		}
	}

	@Override
	public IReference visit(IEventReference eventRef, Void context) {
		EventName event = eventRef.getEventName();
		if (event.isUnknown()) {
			return null;
		} else if (!event.isStatic() && eventRef.getReference().isMissing()) {
			return null;
		} else {
			return eventRef;
		}
	}
}
