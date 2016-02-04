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
package cc.kave.commons.pointsto;

import cc.kave.commons.model.names.IMethodName;
import cc.kave.commons.model.names.ITypeName;
import cc.kave.commons.model.names.Name;
import cc.kave.commons.model.typeshapes.ITypeHierarchy;

public abstract class LanguageOptions {

	private static final LanguageOptions instance = new CSharpLanguageOptions();

	public static LanguageOptions getInstance() {
		return instance;
	}

	protected static final String LAMBDA_KEYWORD = "$Lambda";

	public abstract String getThisName();

	public abstract String getSuperName();

	public abstract ITypeName getSuperType(ITypeHierarchy typeHierarchy);

	public abstract ITypeName getTopClass();

	public abstract String getPropertyParameterName();

	public abstract IMethodName addLambda(IMethodName method);

	public abstract IMethodName removeLambda(IMethodName method);

	public abstract ITypeName addLambda(ITypeName type);

	public boolean isLambdaName(Name name) {
		return name.getIdentifier().contains(LAMBDA_KEYWORD);
	}

}
