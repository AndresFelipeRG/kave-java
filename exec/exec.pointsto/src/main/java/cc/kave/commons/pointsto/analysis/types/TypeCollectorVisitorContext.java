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
package cc.kave.commons.pointsto.analysis.types;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cc.kave.commons.model.events.completionevents.Context;
import cc.kave.commons.model.naming.Names;
import cc.kave.commons.model.naming.codeelements.IFieldName;
import cc.kave.commons.model.naming.codeelements.IParameterName;
import cc.kave.commons.model.naming.codeelements.IPropertyName;
import cc.kave.commons.model.naming.types.ITypeName;
import cc.kave.commons.model.ssts.IReference;
import cc.kave.commons.model.ssts.declarations.IFieldDeclaration;
import cc.kave.commons.model.ssts.declarations.IMethodDeclaration;
import cc.kave.commons.model.ssts.declarations.IPropertyDeclaration;
import cc.kave.commons.model.ssts.references.IFieldReference;
import cc.kave.commons.model.ssts.references.IIndexAccessReference;
import cc.kave.commons.model.ssts.references.IPropertyReference;
import cc.kave.commons.model.ssts.references.IVariableReference;
import cc.kave.commons.model.ssts.statements.IVariableDeclaration;
import cc.kave.commons.model.typeshapes.ITypeHierarchy;
import cc.kave.commons.pointsto.analysis.utils.LanguageOptions;
import cc.kave.commons.pointsto.analysis.utils.ScopedMap;

public class TypeCollectorVisitorContext {

	private static final Logger LOGGER = LoggerFactory.getLogger(TypeCollectorVisitorContext.class);

	private ScopedMap<String, ITypeName> symbolTable = new ScopedMap<>();

	private IdentityHashMap<IReference, ITypeName> referenceTypes = new IdentityHashMap<>();
	private Set<ITypeName> allTypes = new HashSet<>();

	public TypeCollectorVisitorContext(Context context) {
		initializeSymbolTable(context);
	}

	public IdentityHashMap<IReference, ITypeName> getReferenceTypes() {
		return referenceTypes;
	}

	public Set<ITypeName> getTypes() {
		return allTypes;
	}

	private void initializeSymbolTable(Context context) {
		symbolTable.enter();

		// add implicitly available variables
		ITypeHierarchy typeHierarchy = context.getTypeShape().getTypeHierarchy();
		LanguageOptions languageOptions = LanguageOptions.getInstance();
		declare(languageOptions.getThisName(), typeHierarchy.getElement());
		declare(languageOptions.getSuperName(), languageOptions.getSuperType(typeHierarchy));

		// collect the types of fields and properties
		// note that these do not have to be added to the symbol table as
		// FieldReference and PropertyReference contain
		// the associated type
		for (IFieldDeclaration fieldDecl : context.getSST().getFields()) {
			IFieldName field = fieldDecl.getName();
			collectType(field.getValueType());
		}

		for (IPropertyDeclaration propertyDecl : context.getSST().getProperties()) {
			IPropertyName property = propertyDecl.getName();
			collectType(property.getValueType());
		}
	}

	private void declare(String identifier, ITypeName type) {
		declare(identifier, type, false);
	}

	private void declare(String identifier, ITypeName type, boolean allowUpdate) {
		if (allowUpdate) {
			if (symbolTable.createOrUpdate(identifier, type)) {
				LOGGER.debug("Redeclared variable {}", identifier);
			}
		} else {
			symbolTable.create(identifier, type);
		}
		allTypes.add(type);
	}

	public void enterMethod(IMethodDeclaration method) {
		collectType(method.getName().getReturnType());
		symbolTable.enter();
		for (IParameterName parameter : method.getName().getParameters()) {
			declareParameter(parameter);
		}
	}

	public void leaveMethod() {
		symbolTable.leave();
	}

	public void enterScope() {
		symbolTable.enter();
	}

	public void leaveScope() {
		symbolTable.leave();
	}

	public void declareVariable(IVariableDeclaration varDecl) {
		if (varDecl.isMissing()) {
			LOGGER.error("Cannot declare a missing variable");
		} else {
			ITypeName type = varDecl.getType();
			// SST lack a compound statement to handle scoping brackets -> allow
			// a variable to be declared multiple
			// times
			declare(varDecl.getReference().getIdentifier(), type, true);

			referenceTypes.put(varDecl.getReference(), type);
		}
	}

	public void declareParameter(IParameterName parameter) {
		if (parameter.isUnknown()) {
			LOGGER.error("Cannot declare an unknown parameter");
		} else {
			// allow a method parameter to be declared multiple times in order
			// to guard against faulty user input
			declare(parameter.getName(), parameter.getValueType(), true);
		}
	}

	public void collectType(ITypeName type) {
		if (!type.isTypeParameter() && !type.isVoidType()) {
			allTypes.add(type);
		}
	}

	public void useVariableReference(IVariableReference reference) {
		if (reference.isMissing()) {
			LOGGER.error("Skipping a reference to a missing variable");
			return;
		}

		ITypeName type = symbolTable.get(reference.getIdentifier());
		if (type == null) {
			LOGGER.error("Skipping a reference to an undeclared variable");
		} else {
			referenceTypes.put(reference, type);
		}
	}

	public void useFieldReference(IFieldReference reference) {
		IFieldName field = reference.getFieldName();
		if (field.isUnknown()) {
			LOGGER.error("Skipping a reference to an unknown field");
		} else {
			ITypeName type = field.getValueType();
			referenceTypes.put(reference, type);
			allTypes.add(type);
		}
	}

	public void usePropertyReference(IPropertyReference reference) {
		IPropertyName property = reference.getPropertyName();
		if (property.isUnknown()) {
			LOGGER.error("Skipping a reference to an unknown property");
		} else {
			ITypeName type = property.getValueType();
			referenceTypes.put(reference, type);
			allTypes.add(type);
		}
	}

	public void useIndexAccessReference(IIndexAccessReference reference) {
		IVariableReference baseRef = reference.getExpression().getReference();
		if (baseRef.isMissing()) {
			LOGGER.error("Skipping an index access reference to a missing variable");
			return;
		}

		ITypeName type = symbolTable.get(baseRef.getIdentifier());
		if (type == null) {
			LOGGER.error("Skipping an index access reference due to an undeclared base variable");
		} else {
			ITypeName baseType = Names.getUnknownType();
			if (type.isArray()) {
				baseType = type.asArrayTypeName().getArrayBaseType();
			}

			referenceTypes.put(reference, baseType);
			allTypes.add(baseType);
		}
	}
}