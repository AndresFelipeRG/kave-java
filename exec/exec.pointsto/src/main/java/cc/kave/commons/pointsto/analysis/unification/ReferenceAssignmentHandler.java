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

import cc.kave.commons.model.ssts.IReference;
import cc.kave.commons.model.ssts.references.IFieldReference;
import cc.kave.commons.model.ssts.references.IPropertyReference;
import cc.kave.commons.model.ssts.references.IVariableReference;

public class ReferenceAssignmentHandler extends AssignmentHandler<IReference> {

	private UnificationAnalysisVisitorContext context;

	public ReferenceAssignmentHandler() {
		this(null);
	}

	public ReferenceAssignmentHandler(UnificationAnalysisVisitorContext context) {
		this.context = context;
	}

	public void setContext(UnificationAnalysisVisitorContext context) {
		this.context = context;
	}

	@Override
	protected IReference getReference(IReference entry) {
		return entry;
	}

	@Override
	protected void assignVarToVar(IReference dest, IReference src) {
		context.copy((IVariableReference) dest, (IVariableReference) src);
	}

	@Override
	protected void assignFieldToVar(IReference dest, IReference src) {
		context.readField((IVariableReference) dest, (IFieldReference) src);
	}

	@Override
	protected void assignPropToVar(IReference dest, IReference src) {
		context.readProperty((IVariableReference) dest, (IPropertyReference) src);
	}

	@Override
	protected void assignVarToField(IReference dest, IReference src) {
		context.writeField((IFieldReference) dest, (IVariableReference) src);
	}

	@Override
	protected void assignFieldToField(IReference dest, IReference src) {
		context.assign((IFieldReference) dest, (IFieldReference) src);
	}

	@Override
	protected void assignPropToField(IReference dest, IReference src) {
		context.assign((IFieldReference) dest, (IPropertyReference) src);
	}

	@Override
	protected void assignVarToProp(IReference dest, IReference src) {
		context.writeProperty((IPropertyReference) dest, (IVariableReference) src);
	}

	@Override
	protected void assignFieldToProp(IReference dest, IReference src) {
		context.assign((IPropertyReference) dest, (IFieldReference) src);
	}

	@Override
	protected void assignPropToProp(IReference dest, IReference src) {
		context.assign((IPropertyReference) dest, (IPropertyReference) src);
	}

	/* (non-Javadoc)
	 * @see cc.kave.commons.pointsto.analysis.unification.AssignmentHandler#assignArrayToVar(java.lang.Object, java.lang.Object)
	 */
	@Override
	protected void assignArrayToVar(IReference dest, IReference src) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see cc.kave.commons.pointsto.analysis.unification.AssignmentHandler#assignArrayToField(java.lang.Object, java.lang.Object)
	 */
	@Override
	protected void assignArrayToField(IReference dest, IReference src) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see cc.kave.commons.pointsto.analysis.unification.AssignmentHandler#assignArrayToProp(java.lang.Object, java.lang.Object)
	 */
	@Override
	protected void assignArrayToProp(IReference dest, IReference src) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see cc.kave.commons.pointsto.analysis.unification.AssignmentHandler#assignVarToArray(java.lang.Object, java.lang.Object)
	 */
	@Override
	protected void assignVarToArray(IReference dest, IReference src) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see cc.kave.commons.pointsto.analysis.unification.AssignmentHandler#assignFieldToArray(java.lang.Object, java.lang.Object)
	 */
	@Override
	protected void assignFieldToArray(IReference dest, IReference src) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see cc.kave.commons.pointsto.analysis.unification.AssignmentHandler#assignPropToArray(java.lang.Object, java.lang.Object)
	 */
	@Override
	protected void assignPropToArray(IReference dest, IReference src) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see cc.kave.commons.pointsto.analysis.unification.AssignmentHandler#assignArrayToArray(java.lang.Object, java.lang.Object)
	 */
	@Override
	protected void assignArrayToArray(IReference dest, IReference src) {
		// TODO Auto-generated method stub
		
	}

}
