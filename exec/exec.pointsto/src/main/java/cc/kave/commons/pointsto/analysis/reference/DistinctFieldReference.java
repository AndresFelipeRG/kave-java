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

import cc.kave.commons.model.names.TypeName;
import cc.kave.commons.model.ssts.references.IFieldReference;

public class DistinctFieldReference extends DistinctMemberReference {

	public DistinctFieldReference(IFieldReference fieldRef, DistinctReference baseReference) {
		super(fieldRef, baseReference);
	}

	@Override
	public boolean isStaticMember() {
		return getReference().getFieldName().isStatic();
	}

	@Override
	public IFieldReference getReference() {
		return (IFieldReference) super.getReference();
	}

	@Override
	public TypeName getType() {
		return ((IFieldReference) memberReference).getFieldName().getValueType();
	}

	@Override
	public <TReturn, TContext> TReturn accept(DistinctReferenceVisitor<TReturn, TContext> visitor, TContext context) {
		return visitor.visit(this, context);
	}

}
