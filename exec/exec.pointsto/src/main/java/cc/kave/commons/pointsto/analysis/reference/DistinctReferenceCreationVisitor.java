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
package cc.kave.commons.pointsto.analysis.reference;

import cc.kave.commons.model.ssts.references.IFieldReference;
import cc.kave.commons.model.ssts.references.IIndexAccessReference;
import cc.kave.commons.model.ssts.references.IPropertyReference;
import cc.kave.commons.model.ssts.references.IVariableReference;
import cc.kave.commons.pointsto.ScopedMap;
import cc.kave.commons.pointsto.analysis.FailSafeNodeVisitor;
import cc.kave.commons.pointsto.analysis.exceptions.MissingBaseVariableException;

public class DistinctReferenceCreationVisitor
		extends FailSafeNodeVisitor<ScopedMap<String, DistinctReference>, DistinctReference> {

	@Override
	public DistinctReference visit(IFieldReference fieldRef, ScopedMap<String, DistinctReference> context) {
		IVariableReference baseRef = fieldRef.getReference();
		if (baseRef.isMissing() && !fieldRef.getFieldName().isStatic()) {
			throw new MissingBaseVariableException(fieldRef);
		}

		return new DistinctFieldReference(fieldRef, context.get(baseRef.getIdentifier()));
	}

	@Override
	public DistinctReference visit(IPropertyReference propertyRef, ScopedMap<String, DistinctReference> context) {
		IVariableReference baseRef = propertyRef.getReference();
		if (baseRef.isMissing() && !propertyRef.getPropertyName().isStatic()) {
			throw new MissingBaseVariableException(propertyRef);
		}

		return new DistinctPropertyReference(propertyRef, context.get(baseRef.getIdentifier()));
	}

	@Override
	public DistinctReference visit(IVariableReference varRef, ScopedMap<String, DistinctReference> context) {
		return context.get(varRef.getIdentifier());
	}

	@Override
	public DistinctReference visit(IIndexAccessReference indexAccessRef, ScopedMap<String, DistinctReference> context) {
		IVariableReference baseRef = indexAccessRef.getExpression().getReference();
		if (baseRef.isMissing()) {
			throw new MissingBaseVariableException(indexAccessRef);
		}

		return new DistinctIndexAccessReference(indexAccessRef, context.get(baseRef.getIdentifier()));
	}

}