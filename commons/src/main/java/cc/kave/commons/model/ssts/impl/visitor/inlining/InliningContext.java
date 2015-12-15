package cc.kave.commons.model.ssts.impl.visitor.inlining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import cc.kave.commons.model.names.MethodName;
import cc.kave.commons.model.names.TypeName;
import cc.kave.commons.model.names.csharp.CsTypeName;
import cc.kave.commons.model.ssts.IReference;
import cc.kave.commons.model.ssts.ISST;
import cc.kave.commons.model.ssts.IStatement;
import cc.kave.commons.model.ssts.declarations.IMethodDeclaration;
import cc.kave.commons.model.ssts.impl.SST;
import cc.kave.commons.model.ssts.impl.SSTUtil;
import cc.kave.commons.model.ssts.impl.blocks.IfElseBlock;
import cc.kave.commons.model.ssts.impl.references.VariableReference;
import cc.kave.commons.model.ssts.impl.visitor.inlining.util.InvocationMethodNameVisitor;
import cc.kave.commons.model.ssts.impl.visitor.inlining.util.NameScopeVisitor;
import cc.kave.commons.model.ssts.references.IVariableReference;

public class InliningContext {

	public static final TypeName GOT_RESULT_TYPE = CsTypeName.newTypeName("Boolean");
	public static final String RESULT_NAME = "$result_";
	public static final String RESULT_FLAG = "$gotNoResult_";

	private int counter;
	private SST sst;
	private NameScopeVisitor visitor;
	private Scope scope;
	private Set<IMethodDeclaration> nonEntryPoints;
	private boolean inline = false;
	private boolean guardNeeded = false;
	private boolean globalGuardNeed = false;
	private final InliningIStatementVisitor statementVisitor;
	private final InliningIReferenceVisitor referenceVisitor;
	private final InliningIExpressionVisitor expressionVisitor;

	private Set<IMethodDeclaration> inlinedMethods;
	private boolean isVoid;

	public InliningContext() {
		this.nonEntryPoints = new HashSet<>();
		this.sst = new SST();
		this.inline = false;
		this.scope = null;
		this.visitor = new NameScopeVisitor();
		this.counter = 0;
		this.statementVisitor = new InliningIStatementVisitor();
		this.referenceVisitor = new InliningIReferenceVisitor();
		this.expressionVisitor = new InliningIExpressionVisitor();
		this.inlinedMethods = new HashSet<>();
	}

	public void addInlinedMethod(IMethodDeclaration method) {
		inlinedMethods.add(method);
	}

	public void addMethod(IMethodDeclaration method) {
		sst.getMethods().add(method);
	}

	public void addStatement(IStatement stmt) {
		scope.body.add(stmt);
	}

	private boolean checkCallTree(Set<MethodName> invocations, Map<MethodName, Set<MethodName>> calls,
			HashSet<MethodName> met, IMethodDeclaration method) {
		if (met.size() == calls.size() || invocations == null) {
			return false;
		} else {
			for (MethodName call : invocations) {
				if (met.contains(call)) {
					continue;
				}
				met.add(call);
				if (call.equals(method.getName()) || checkCallTree(calls.get(call), calls, met, method)) {
					return true;
				}
			}
		}
		return false;

	}

	public void createSST(ISST oldSST) {
		sst.getDelegates().addAll(oldSST.getDelegates());
		sst.setEnclosingType(oldSST.getEnclosingType());
		sst.getEvents().addAll(oldSST.getEvents());
		sst.getFields().addAll(oldSST.getFields());
		sst.getProperties().addAll(oldSST.getProperties());
	}

	public void enterBlock() {
		Scope newScope = new Scope();
		newScope.parent = scope;
		if (scope != null) {
			newScope.existingIds.addAll(scope.existingIds);
			newScope.changedNames = scope.changedNames;
			newScope.resultName = scope.resultName;
			newScope.gotResultName = scope.gotResultName;
		}
		scope = newScope;
	}

	public void leaveBlock(List<IStatement> body) {
		body.addAll(scope.body);
		scope.parent.resultName = scope.resultName;
		scope.parent.gotResultName = scope.gotResultName;
		setGuardNeeded(isGlobalGuardNeeded());
		scope = scope.parent;
	}

	public void enterScope(List<IStatement> body, Map<IVariableReference, IVariableReference> preChangedNames) {
		Set<IVariableReference> newNames = collectNames(body);
		Scope newScope = new Scope();
		if (scope != null) {
			newScope.existingIds.addAll(scope.existingIds);
			newScope.parent = scope;
			for (IVariableReference ref : newNames) {
				if (newScope.existingIds.contains(ref)) {
					IVariableReference newRef = generateNewRef(ref);
					newScope.changedNames.put(ref, newRef);
					newScope.existingIds.add(newRef);
				} else {
					newScope.existingIds.add(ref);
				}
			}
		} else {
			newScope.existingIds.addAll(newNames);
		}
		if (preChangedNames != null) {
			newScope.changedNames.putAll(preChangedNames);
		}
		scope = newScope;
	}

	public void leaveScope() {
		if (scope.parent != null) {
			scope.parent.existingIds.addAll(scope.existingIds);
			scope.parent.body.addAll(scope.body);
			scope = scope.parent;
		}
		setGlobalGuardNeeded(false);
		setGuardNeeded(false);
	}

	public void resetScope() {
		scope = null;
		counter = 0;
	}

	private Set<IVariableReference> collectNames(List<IStatement> body) {
		Set<IVariableReference> newNames = new LinkedHashSet<>();
		for (IStatement statement : body) {
			statement.accept(visitor, newNames);
		}
		return newNames;
	}

	private IVariableReference generateNewRef(IVariableReference reference) {
		VariableReference ref = new VariableReference();
		ref.setIdentifier("$" + counter + "_" + reference.getIdentifier());
		counter++;
		return ref;
	}

	private Set<IMethodDeclaration> testForRecursiveCalls(Set<IMethodDeclaration> nonEntryPoints) {
		Set<IMethodDeclaration> outputNonEntryPoints = new HashSet<>();
		Map<MethodName, Set<MethodName>> calls = getMethodNamesOfInvocations(nonEntryPoints);
		for (IMethodDeclaration method : nonEntryPoints) {
			Set<MethodName> invocations = calls.get(method.getName());
			if (invocations == null || invocations.contains(method.getName())
					|| (!invocations.isEmpty() && checkCallTree(invocations, calls, Sets.newHashSet(), method))) {
				addMethod(method);
			} else {
				outputNonEntryPoints.add(method);
			}
		}
		return outputNonEntryPoints;
	}

	private Map<MethodName, Set<MethodName>> getMethodNamesOfInvocations(Set<IMethodDeclaration> nonEntryPoints) {
		Map<MethodName, Set<MethodName>> calls = new HashMap<>();
		Set<MethodName> removeList = new HashSet<>();
		for (IMethodDeclaration method : nonEntryPoints) {
			Set<MethodName> context = new HashSet<>();
			method.accept(new InvocationMethodNameVisitor(), context);
			if (calls.containsKey(method.getName())) {
				// Remove MethodDeclarations with duplicated names
				removeList.add(method.getName());
			}
			calls.put(method.getName(), context);
		}
		for (MethodName name : removeList) {
			calls.remove(name);
		}
		return calls;
	}

	public IStatement visit(IStatement statement, InliningContext context) {
		List<IStatement> body = new ArrayList<>();
		visitBlock(Lists.newArrayList(statement), body);
		return body.get(0);
	}

	public void visitBlock(List<IStatement> body, List<IStatement> blockBody) {
		enterBlock();
		int index = 0;
		for (IStatement statement : body) {
			statement.accept(getStatementVisitor(), this);
			if ((isGuardNeeded()) && index < body.size() - 1 && body.subList(index + 1, body.size()).size() > 0) {
				IfElseBlock block = new IfElseBlock();
				block.setCondition(SSTUtil.referenceExprToVariable(getGotResultName()));
				setGuardNeeded(false);
				visitBlock(body.subList(index + 1, body.size()), block.getThen());
				addStatement(block);
				break;
			}
			index++;

		}
		leaveBlock(blockBody);
	}

	public void visitScope(List<IStatement> body) {
		visitScope(body, null);
	}

	public void visitScope(List<IStatement> body, Map<IVariableReference, IVariableReference> preChangedNames) {
		enterScope(body, preChangedNames);
		int index = 0;
		for (IStatement statement : body) {
			statement.accept(getStatementVisitor(), this);
			if ((isGuardNeeded() || isGlobalGuardNeeded()) && body.subList(index + 1, body.size()).size() > 0) {
				IfElseBlock block = new IfElseBlock();
				block.setCondition(SSTUtil.referenceExprToVariable(getGotResultName()));
				setGuardNeeded(false);
				setGlobalGuardNeeded(false);
				visitBlock(body.subList(index + 1, body.size()), block.getThen());
				addStatement(block);
				break;
			}
			index++;
		}
		// newBody.addAll(context.getBody());
		leaveScope();
	}

	public void setGlobalGuardNeeded(boolean guardNeeded) {
		this.globalGuardNeed = guardNeeded;
	}

	public void setGotResultName(String name) {
		scope.gotResultName = name;
	}

	public void setGuardNeeded(boolean guardNeeded) {
		this.guardNeeded = guardNeeded;
	}

	public void setGuardVariableNames(String name) {
		scope.resultName = RESULT_NAME + name;
		scope.gotResultName = RESULT_FLAG + name;
	}

	public void setInline(boolean inline) {
		this.inline = inline;
	}

	public void setNonEntryPoints(Set<IMethodDeclaration> nonEntryPoints) {
		this.nonEntryPoints = testForRecursiveCalls(nonEntryPoints);
	}

	public void setResultName(String name) {
		scope.resultName = name;
	}

	public void setVoid(boolean isVoid) {
		this.isVoid = isVoid;
	}

	public List<IStatement> getBody() {
		return scope.body;
	}

	public InliningIExpressionVisitor getExpressionVisitor() {
		return expressionVisitor;
	}

	public String getGotResultName() {
		return scope.gotResultName;
	}

	public Set<IMethodDeclaration> getInlinedMethods() {
		return inlinedMethods;
	}

	public IMethodDeclaration getNonEntryPoint(MethodName methodName) {
		for (IMethodDeclaration method : nonEntryPoints)
			if (method.getName().equals(methodName))
				return method;
		return null;
	}

	public Set<IMethodDeclaration> getNonEntryPoints() {
		return nonEntryPoints;
	}

	public InliningIReferenceVisitor getReferenceVisitor() {
		return referenceVisitor;
	}

	public String getResultName() {
		return scope.resultName;
	}

	public ISST getSST() {
		return sst;
	}

	public InliningIStatementVisitor getStatementVisitor() {
		return statementVisitor;
	}

	public boolean isGlobalGuardNeeded() {
		return globalGuardNeed;
	}

	public boolean isGuardNeeded() {
		return guardNeeded;
	}

	public boolean isInline() {
		return inline;
	}

	public boolean isVoid() {
		return isVoid;
	}

	public IReference resolve(IVariableReference ref) {
		return scope.resolve(ref);
	}
}