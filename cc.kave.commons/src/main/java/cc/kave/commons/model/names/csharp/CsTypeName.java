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
package cc.kave.commons.model.names.csharp;

import java.util.List;

import com.google.common.collect.Lists;

import cc.kave.commons.model.names.IArrayTypeName;
import cc.kave.commons.model.names.IBundleName;
import cc.kave.commons.model.names.IDelegateTypeName;
import cc.kave.commons.model.names.INamespaceName;
import cc.kave.commons.model.names.IParameterName;
import cc.kave.commons.model.names.ITypeName;
import cc.kave.commons.model.names.csharp.parser.TypeNameParseUtil;
import cc.kave.commons.model.names.csharp.parser.TypeNamingParser.NestedTypeNameContext;
import cc.kave.commons.model.names.csharp.parser.TypeNamingParser.ResolvedTypeContext;
import cc.kave.commons.model.names.csharp.parser.TypeNamingParser.TypeContext;
import cc.kave.commons.model.names.csharp.parser.TypeNamingParser.TypeNameContext;

public class CsTypeName implements ITypeName, IDelegateTypeName, IArrayTypeName {

	private static final String UNKNOWN_IDENTIFIER = "???";
	protected TypeContext ctx;

	public CsTypeName(String type) {
		TypeContext ctx = TypeNameParseUtil.validateTypeName(type);
		this.ctx = ctx;
	}

	public CsTypeName(TypeContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ctx == null) ? 0 : ctx.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CsTypeName other = (CsTypeName) obj;
		if (ctx == null) {
			if (other.ctx != null)
				return false;
		} else if (!ctx.getText().equals(other.ctx.getText()))
			return false;
		return true;
	}

	@Override
	public boolean isGenericEntity() {
		return (ctx.regularType() != null);
	}

	@Override
	public boolean hasTypeParameters() {
		return isGenericEntity();
	}

	@Override
	public List<ITypeName> getTypeParameters() {
		// TODO: first typename ?
		if (hasTypeParameters()) {
			TypeNameContext typeNameCtx = getFirstGenericTypeName();
			if (typeNameCtx != null) {
			}
		}
		return Lists.newArrayList();
	}

	private TypeNameContext getFirstGenericTypeName() {
		return null;
	}

	@Override
	public String getIdentifier() {
		return ctx.getText();
	}

	@Override
	public boolean isUnknown() {
		return ctx.UNKNOWN() != null;
	}

	@Override
	public IBundleName getAssembly() {
		String identifier = UNKNOWN_IDENTIFIER;
		if (ctx.regularType() != null) {
			identifier = ctx.regularType().assembly().getText();
		} else if (ctx.arrayType() != null) {
			return new CsTypeName(ctx.arrayType().type()).getAssembly();
		} else if (ctx.delegateType() != null) {
			return new CsMethodName(ctx.delegateType().method()).getDeclaringType().getAssembly();
		}
		return AssemblyName.newAssemblyName(identifier);
	}

	@Override
	public INamespaceName getNamespace() {
		String identifier = UNKNOWN_IDENTIFIER;
		if (ctx.regularType() != null && ctx.regularType().resolvedType() != null
				&& ctx.regularType().resolvedType().namespace() != null) {
			identifier = ctx.regularType().resolvedType().namespace().getText();
		} else if (ctx.regularType() != null && ctx.regularType().nestedType() != null) {
			identifier = recursiveNested(ctx.regularType().nestedType().nestedTypeName());
		} else if (ctx.delegateType() != null) {
			return new CsMethodName(ctx.delegateType().method()).getDeclaringType().getNamespace();
		}
		return NamespaceName.newNamespaceName(identifier);
	}

	private String recursiveNested(NestedTypeNameContext nestedTypeName) {
		if (nestedTypeName.resolvedType() != null)
			return nestedTypeName.resolvedType().namespace() != null
					? nestedTypeName.resolvedType().namespace().getText() : UNKNOWN_IDENTIFIER;
		return recursiveNested(nestedTypeName.nestedType().nestedTypeName());
	}

	@Override
	public ITypeName getDeclaringType() {
		if (ctx.regularType() != null && this.isNestedType()) {
			String identifier = getWithoutLastTypeName(ctx.regularType().resolvedType()) + ","
					+ (ctx.regularType().WS() != null ? ctx.regularType().WS().getText() : "")
					+ ctx.regularType().assembly().getText();
			return new CsTypeName(identifier);
		}
		return null;
	}

	private String getWithoutLastTypeName(ResolvedTypeContext resolvedType) {
		String typeName = resolvedType.namespace() != null
				? resolvedType.namespace().getText() + resolvedType.typeName().getText()
				: resolvedType.typeName().getText();
		List<TypeNameContext> typeNames = Lists.newArrayList();
		for (int i = 1; i < typeNames.size() - 1; i++) {
			typeName += "+" + typeNames.get(i).getText();
		}
		return typeName;
	}

	@Override
	public String getFullName() {
		String fullName = "";
		if (ctx.regularType() != null) {
			fullName = ctx.regularType().resolvedType().getText();
		} else if (ctx.UNKNOWN() != null) {
			fullName = ctx.UNKNOWN().getText();
		}

		if (fullName.startsWith("e:") || fullName.startsWith("i:") || fullName.startsWith("s:"))
			fullName = fullName.substring(2);

		return fullName;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public boolean isUnknownType() {
		return ctx.UNKNOWN() != null || ctx.typeParameter() != null;
	}

	@Override
	public boolean isVoidType() {
		return ctx.regularType() != null && ctx.regularType().resolvedType().getText().startsWith("System.Void");
	}

	@Override
	public boolean isValueType() {
		return isStructType() || isEnumType() || isVoidType();
	}

	@Override
	public boolean isSimpleType() {
		if (ctx.regularType() != null) {
			return isSimpleTypeIdentifier(ctx.regularType().getText());
		}
		return false;
	}

	private static boolean isSimpleTypeIdentifier(String identifier) {
		return isNumericTypeName(identifier) || identifier.startsWith("System.Boolean,");
	}

	private static boolean isNumericTypeName(String identifier) {
		return isIntegralTypeName(identifier) || isFloatingPointTypeName(identifier)
				|| identifier.startsWith("System.Decimal,");
	}

	private static final String[] IntegralTypeNames = { "System.SByte,", "System.Byte,", "System.Int16,",
			"System.UInt16,", "System.Int32,", "System.UInt32,", "System.Int64,", "System.UInt64,", "System.Char," };

	private static boolean isIntegralTypeName(String identifier) {
		for (int i = 0; i < IntegralTypeNames.length; i++) {
			if (identifier.startsWith(IntegralTypeNames[i])) {
				return true;
			}
		}
		return false;
	}

	private static final String[] FloatingPointTypeNames = { "System.Single,", "System.Double," };

	private static boolean isFloatingPointTypeName(String identifier) {
		for (int i = 0; i < FloatingPointTypeNames.length; i++) {
			if (identifier.startsWith(FloatingPointTypeNames[i])) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isEnumType() {
		return ctx.regularType() != null && ctx.regularType().resolvedType().getText().startsWith("e:");
	}

	@Override
	public boolean isStructType() {
		return isSimpleType() || isVoidType() || isNullableType()
				|| (ctx.regularType() != null && ctx.regularType().resolvedType().getText().startsWith("s:"));
	}

	@Override
	public boolean isNullableType() {
		return ctx.regularType() != null
				&& ctx.regularType().resolvedType().getText().startsWith("System.Nullable'1[[");
	}

	@Override
	public boolean isReferenceType() {
		return isClassType() || isInterfaceType() || isArrayType() || isDelegateType();
	}

	@Override
	public boolean isClassType() {
		return !isValueType() && !isInterfaceType() && !isArrayType() && !isDelegateType() && !isUnknownType();
	}

	@Override
	public boolean isInterfaceType() {
		return ctx.regularType() != null && ctx.regularType().resolvedType().getText().startsWith("i:");
	}

	@Override
	public boolean isDelegateType() {
		return ctx.delegateType() != null;
	}

	@Override
	public boolean isNestedType() {
		if (ctx.regularType() != null) {
			return ctx.regularType().nestedType() != null;
		}
		return false;
	}

	private boolean containsGenericParts(List<TypeNameContext> typeName) {
		return false;
	}

	@Override
	public boolean isArrayType() {
		return ctx.arrayType() != null;
	}

	@Override
	public ITypeName getArrayBaseType() {
		ITypeName type = new CsTypeName(ctx.arrayType().type());
		if (type.isArrayType()) {
			return type.getArrayBaseType();
		}
		return type;
	}

	@Override
	public ITypeName DeriveArrayTypeName(int rank) {
		return null;
	}

	@Override
	public boolean isTypeParameter() {
		return ctx.typeParameter() != null;
	}

	@Override
	public String getTypeParameterShortName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ITypeName getTypeParameterType() {
		return null;
	}

	@Override
	public String getSignature() {
		return new CsMethodName(ctx.delegateType().method()).getSignature();
	}

	@Override
	public List<IParameterName> getParameters() {
		return new CsMethodName(ctx.delegateType().method()).getParameters();
	}

	@Override
	public boolean hasParameters() {
		return new CsMethodName(ctx.delegateType().method()).hasParameters();
	}

	@Override
	public ITypeName getReturnType() {
		return new CsMethodName(ctx.delegateType().method()).getReturnType();
	}

	@Override
	public int getRank() {
		if (isArrayType()) {
			return 1 + new CsTypeName(ctx.arrayType().type()).getRank();
		}
		return 0;
	}

	public IDelegateTypeName toDelegateType() {
		if (isDelegateType()) {
			return this;
		} else {
			throw new ClassCastException();
		}
	}

	public IArrayTypeName toArrayType() {
		if (isArrayType()) {
			return this;
		} else {
			throw new ClassCastException();
		}
	}

}