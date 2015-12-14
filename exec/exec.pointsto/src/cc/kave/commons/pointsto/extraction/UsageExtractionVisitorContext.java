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
package cc.kave.commons.pointsto.extraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import cc.kave.commons.model.events.completionevents.Context;
import cc.kave.commons.model.names.FieldName;
import cc.kave.commons.model.names.MethodName;
import cc.kave.commons.model.names.ParameterName;
import cc.kave.commons.model.names.PropertyName;
import cc.kave.commons.model.names.TypeName;
import cc.kave.commons.model.names.csharp.CsFieldName;
import cc.kave.commons.model.ssts.IReference;
import cc.kave.commons.model.ssts.IStatement;
import cc.kave.commons.model.ssts.declarations.IFieldDeclaration;
import cc.kave.commons.model.ssts.declarations.IPropertyDeclaration;
import cc.kave.commons.model.ssts.expressions.simple.IConstantValueExpression;
import cc.kave.commons.model.ssts.impl.references.FieldReference;
import cc.kave.commons.model.ssts.impl.references.PropertyReference;
import cc.kave.commons.model.ssts.impl.references.VariableReference;
import cc.kave.commons.model.ssts.references.IFieldReference;
import cc.kave.commons.model.ssts.references.IPropertyReference;
import cc.kave.commons.model.ssts.references.IVariableReference;
import cc.kave.commons.model.ssts.statements.IAssignment;
import cc.kave.commons.model.ssts.statements.IExpressionStatement;
import cc.kave.commons.pointsto.LanguageOptions;
import cc.kave.commons.pointsto.analysis.AbstractLocation;
import cc.kave.commons.pointsto.analysis.Callpath;
import cc.kave.commons.pointsto.analysis.PointerAnalysis;
import cc.kave.commons.pointsto.analysis.PointsToContext;
import cc.kave.commons.pointsto.analysis.QueryContextKey;
import cc.kave.commons.pointsto.analysis.types.TypeCollector;
import cc.kave.commons.pointsto.dummies.DummyCallsite;
import cc.kave.commons.pointsto.dummies.DummyDefinitionSite;
import cc.kave.commons.pointsto.dummies.DummyUsage;

public class UsageExtractionVisitorContext {

	private static final Logger LOGGER = Logger.getLogger(UsageExtractionVisitorContext.class.getName());

	private DefinitionSitePriorityComparator definitionSiteComparator = new DefinitionSitePriorityComparator();

	private PointerAnalysis pointerAnalysis;
	private TypeCollector typeCollector;
	private TypeName enclosingClass;

	private Map<AbstractLocation, DummyUsage> locationUsages = new HashMap<>();

	private Map<AbstractLocation, DummyDefinitionSite> implicitDefinitions = new HashMap<>();

	private IStatement currentStatement;
	private Callpath currentCallpath;

	public UsageExtractionVisitorContext(PointsToContext context) {
		this.pointerAnalysis = context.getPointerAnalysis();
		this.enclosingClass = context.getSST().getEnclosingType();
		this.typeCollector = new TypeCollector(context);

		createImplicitDefinitions(context);
	}

	public List<DummyUsage> getUsages() {
		return new ArrayList<>(locationUsages.values());
	}

	private IVariableReference buildVariableReference(String name) {
		VariableReference varRef = new VariableReference();
		varRef.setIdentifier(name);
		return varRef;
	}

	private IFieldReference buildFieldReference(FieldName field) {
		FieldReference fieldRef = new FieldReference();
		fieldRef.setReference(buildVariableReference(LanguageOptions.getInstance().getThisName()));
		fieldRef.setFieldName(field);
		return fieldRef;
	}

	private IPropertyReference buildPropertyReference(PropertyName property) {
		PropertyReference propertyRef = new PropertyReference();
		propertyRef.setReference(buildVariableReference(LanguageOptions.getInstance().getThisName()));
		propertyRef.setPropertyName(property);
		return propertyRef;
	}

	public void setEntryPoint(MethodName method) {
		currentCallpath = new Callpath(method);
		currentStatement = null;

		// reset usages
		locationUsages.clear();
	}

	public void setCurrentStatement(IStatement stmt) {
		this.currentStatement = stmt;
	}

	public void enterNonEntryPoint(MethodName method) {
		currentCallpath.enterMethod(method);
	}

	public void leaveNonEntryPoint() {
		currentCallpath.leaveMethod();
	}

	private Set<AbstractLocation> queryPointsTo(IReference reference, IStatement stmt, TypeName type) {
		QueryContextKey query = new QueryContextKey(reference, stmt, type, currentCallpath);
		return pointerAnalysis.query(query);
	}

	private void createImplicitDefinitions(Context context) {
		LanguageOptions languageOptions = LanguageOptions.getInstance();

		// this
		DummyDefinitionSite thisDefinition = DummyDefinitionSite.byThis();
		for (AbstractLocation location : queryPointsTo(buildVariableReference(languageOptions.getThisName()), null,
				enclosingClass)) {
			implicitDefinitions.put(location, thisDefinition);
		}

		// super
		DummyDefinitionSite superDefinition = DummyDefinitionSite.byThis();
		for (AbstractLocation location : queryPointsTo(buildVariableReference(languageOptions.getSuperName()), null,
				languageOptions.getSuperType(context.getTypeShape().getTypeHierarchy()))) {
			implicitDefinitions.put(location, superDefinition);
		}

		for (IFieldDeclaration fieldDecl : context.getSST().getFields()) {
			FieldName field = fieldDecl.getName();
			DummyDefinitionSite fieldDefinition = DummyDefinitionSite.byField(field);
			for (AbstractLocation location : queryPointsTo(buildFieldReference(field), null, field.getValueType())) {
				// TODO we might overwrite definitions here if two fields share one location
				implicitDefinitions.put(location, fieldDefinition);
			}
		}

		// treat properties as fields if they have no custom get code
		for (IPropertyDeclaration propertyDecl : context.getSST().getProperties()) {
			if (!propertyDecl.getGet().isEmpty()) {
				continue;
			}

			PropertyName property = propertyDecl.getName();
			DummyDefinitionSite propertyDefinition = DummyDefinitionSite.byField(propertyToField(property));
			for (AbstractLocation location : queryPointsTo(buildPropertyReference(property), null,
					property.getValueType())) {
				// do not overwrite an existing definition by a real field
				if (!implicitDefinitions.containsKey(location)) {
					implicitDefinitions.put(location, propertyDefinition);
				}
			}
		}

	}

	private FieldName propertyToField(PropertyName property) {
		FieldName field = CsFieldName.newFieldName(property.getIdentifier());
		return field;
	}

	private DummyUsage initializeUsage(TypeName type, AbstractLocation location) {
		DummyUsage usage = new DummyUsage();

		usage.setType(type);
		usage.setClassContext(enclosingClass);
		usage.setMethodContext(currentCallpath.getFirst());

		if (location == null || !implicitDefinitions.containsKey(location)) {
			usage.setDefinitionSite(DummyDefinitionSite.unknown());
		} else {
			usage.setDefinitionSite(implicitDefinitions.get(location));
		}

		return usage;
	}

	private DummyUsage getOrCreateUsage(AbstractLocation location, TypeName type) {
		DummyUsage usage = locationUsages.get(location);
		if (usage == null) {
			usage = initializeUsage(type, location);
			locationUsages.put(location, usage);
		}

		return usage;
	}

	private void updateDefinitions(QueryContextKey query, DummyDefinitionSite newDefinition) {
		Set<AbstractLocation> locations = pointerAnalysis.query(query);

		for (AbstractLocation location : locations) {
			DummyUsage usage = getOrCreateUsage(location, query.getType());

			DummyDefinitionSite currentDefinition = usage.getDefinitionSite();
			if (definitionSiteComparator.compare(currentDefinition, newDefinition) < 0) {
				usage.setDefinitionSite(newDefinition);
			}
		}
	}

	private void updateCallsites(QueryContextKey query, DummyCallsite callsite) {
		Set<AbstractLocation> locations = pointerAnalysis.query(query);

		for (AbstractLocation location : locations) {
			DummyUsage usage = getOrCreateUsage(location, query.getType());

			usage.addCallsite(callsite);
		}
	}

	public void declareParameter(MethodName method, ParameterName parameter, int argIndex) {
		QueryContextKey query = new QueryContextKey(buildVariableReference(parameter.getName()), null,
				parameter.getValueType(), currentCallpath);
		DummyDefinitionSite newDefinition = DummyDefinitionSite.byParam(method, argIndex);

		updateDefinitions(query, newDefinition);
	}

	public void registerConstant(IConstantValueExpression constExpr) {
		if (!(currentStatement instanceof IAssignment)) {
			LOGGER.log(Level.SEVERE, "Cannot register constant definition site: target is no assignment");
			return;
		}

		IAssignment assignStmt = (IAssignment) currentStatement;
		TypeName type = typeCollector.getType(assignStmt.getReference());
		QueryContextKey query = new QueryContextKey(assignStmt.getReference(), currentStatement, type, currentCallpath);
		DummyDefinitionSite newDefinition = DummyDefinitionSite.byConstant();

		updateDefinitions(query, newDefinition);
	}

	public void registerConstructor(MethodName method) {
		if (!(currentStatement instanceof IAssignment)) {
			LOGGER.log(Level.SEVERE, "Cannot register constructor definition site: target is no assignment");
			return;
		}

		IAssignment assignStmt = (IAssignment) currentStatement;
		TypeName type = typeCollector.getType(assignStmt.getReference());
		QueryContextKey query = new QueryContextKey(assignStmt.getReference(), currentStatement, type, currentCallpath);
		DummyDefinitionSite newDefinition = DummyDefinitionSite.byConstructor(method);

		updateDefinitions(query, newDefinition);
	}

	public void registerPotentialReturnDefinitionSite(MethodName method) {
		if (currentStatement instanceof IExpressionStatement) {
			// method called without saving returned value
			return;
		} else if (!(currentStatement instanceof IAssignment)) {
			LOGGER.log(Level.SEVERE, "Cannot register return definition site: target is no assignment");
			return;
		}

		IAssignment assignStmt = (IAssignment) currentStatement;
		TypeName type = typeCollector.getType(assignStmt.getReference());
		QueryContextKey query = new QueryContextKey(assignStmt.getReference(), currentStatement, type, currentCallpath);
		DummyDefinitionSite newDefinition = DummyDefinitionSite.byReturn(method);

		updateDefinitions(query, newDefinition);
	}

	public void registerParameterCallsite(MethodName method, IReference parameterExpr, int argIndex) {
		TypeName type = typeCollector.getType(parameterExpr);
		QueryContextKey query = new QueryContextKey(parameterExpr, currentStatement, type, currentCallpath);
		DummyCallsite callsite = DummyCallsite.parameterCallsite(method, argIndex);

		updateCallsites(query, callsite);
	}

	public void registerReceiverCallsite(MethodName method, IReference receiver) {
		TypeName type = typeCollector.getType(receiver);
		QueryContextKey query = new QueryContextKey(receiver, currentStatement, type, currentCallpath);
		DummyCallsite callsite = DummyCallsite.receiverCallsite(method);

		updateCallsites(query, callsite);
	}

}