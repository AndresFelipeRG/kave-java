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
package cc.kave.commons.model.naming.impl.v0.codeelements;

import java.util.List;

import cc.kave.commons.model.naming.Names;
import cc.kave.commons.model.naming.codeelements.ILambdaName;
import cc.kave.commons.model.naming.codeelements.IParameterName;
import cc.kave.commons.model.naming.impl.csharp.CsNameUtils;
import cc.kave.commons.model.naming.impl.v0.BaseName;
import cc.kave.commons.model.naming.types.ITypeName;

public class LambdaName extends BaseName implements ILambdaName {

	private LambdaName() {
		this(UNKNOWN_NAME_IDENTIFIER);
	}

	private LambdaName(String identifier) {
		super(identifier);
	}

	@Override
	public List<IParameterName> getParameters() {
		return CsNameUtils.getParameterNames(identifier);
	}

	@Override
	public boolean hasParameters() {
		return CsNameUtils.hasParameters(identifier);
	}

	@Override
	public ITypeName getReturnType() {
		int startIndexOfValueTypeIdentifier = identifier.indexOf('[') + 1;
		int lastIndexOfValueTypeIdentifer = identifier.indexOf("]") + 1;
		int lengthOfValueTypeIdentifier = lastIndexOfValueTypeIdentifer - startIndexOfValueTypeIdentifier;
		return Names.newType(identifier.substring(startIndexOfValueTypeIdentifier, lengthOfValueTypeIdentifier));
	}
}