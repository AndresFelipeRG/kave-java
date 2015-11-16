package cc.kave.commons.model.ssts.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import cc.kave.commons.model.names.MethodName;
import cc.kave.commons.model.names.TypeName;
import cc.kave.commons.model.names.csharp.CsFieldName;
import cc.kave.commons.model.ssts.IReference;
import cc.kave.commons.model.ssts.IStatement;
import cc.kave.commons.model.ssts.blocks.ICaseBlock;
import cc.kave.commons.model.ssts.blocks.ICatchBlock;
import cc.kave.commons.model.ssts.blocks.IDoLoop;
import cc.kave.commons.model.ssts.blocks.IForEachLoop;
import cc.kave.commons.model.ssts.blocks.IForLoop;
import cc.kave.commons.model.ssts.blocks.IIfElseBlock;
import cc.kave.commons.model.ssts.blocks.ILockBlock;
import cc.kave.commons.model.ssts.blocks.ISwitchBlock;
import cc.kave.commons.model.ssts.blocks.ITryBlock;
import cc.kave.commons.model.ssts.blocks.IUncheckedBlock;
import cc.kave.commons.model.ssts.blocks.IUsingBlock;
import cc.kave.commons.model.ssts.blocks.IWhileLoop;
import cc.kave.commons.model.ssts.declarations.IFieldDeclaration;
import cc.kave.commons.model.ssts.declarations.IMethodDeclaration;
import cc.kave.commons.model.ssts.declarations.IVariableDeclaration;
import cc.kave.commons.model.ssts.expressions.IAssignableExpression;
import cc.kave.commons.model.ssts.expressions.ISimpleExpression;
import cc.kave.commons.model.ssts.expressions.assignable.IComposedExpression;
import cc.kave.commons.model.ssts.expressions.assignable.IIfElseExpression;
import cc.kave.commons.model.ssts.expressions.assignable.IInvocationExpression;
import cc.kave.commons.model.ssts.expressions.assignable.ILambdaExpression;
import cc.kave.commons.model.ssts.expressions.loopheader.ILoopHeaderBlockExpression;
import cc.kave.commons.model.ssts.expressions.simple.IConstantValueExpression;
import cc.kave.commons.model.ssts.expressions.simple.IReferenceExpression;
import cc.kave.commons.model.ssts.impl.blocks.CaseBlock;
import cc.kave.commons.model.ssts.impl.blocks.CatchBlock;
import cc.kave.commons.model.ssts.impl.blocks.DoLoop;
import cc.kave.commons.model.ssts.impl.blocks.ForEachLoop;
import cc.kave.commons.model.ssts.impl.blocks.ForLoop;
import cc.kave.commons.model.ssts.impl.blocks.IfElseBlock;
import cc.kave.commons.model.ssts.impl.blocks.LockBlock;
import cc.kave.commons.model.ssts.impl.blocks.SwitchBlock;
import cc.kave.commons.model.ssts.impl.blocks.TryBlock;
import cc.kave.commons.model.ssts.impl.blocks.UncheckedBlock;
import cc.kave.commons.model.ssts.impl.blocks.UnsafeBlock;
import cc.kave.commons.model.ssts.impl.blocks.UsingBlock;
import cc.kave.commons.model.ssts.impl.blocks.WhileLoop;
import cc.kave.commons.model.ssts.impl.declarations.FieldDeclaration;
import cc.kave.commons.model.ssts.impl.declarations.MethodDeclaration;
import cc.kave.commons.model.ssts.impl.expressions.assignable.CompletionExpression;
import cc.kave.commons.model.ssts.impl.expressions.assignable.ComposedExpression;
import cc.kave.commons.model.ssts.impl.expressions.assignable.IfElseExpression;
import cc.kave.commons.model.ssts.impl.expressions.assignable.InvocationExpression;
import cc.kave.commons.model.ssts.impl.expressions.assignable.LambdaExpression;
import cc.kave.commons.model.ssts.impl.expressions.loopheader.LoopHeaderBlockExpression;
import cc.kave.commons.model.ssts.impl.expressions.simple.ConstantValueExpression;
import cc.kave.commons.model.ssts.impl.expressions.simple.NullExpression;
import cc.kave.commons.model.ssts.impl.expressions.simple.ReferenceExpression;
import cc.kave.commons.model.ssts.impl.expressions.simple.UnknownExpression;
import cc.kave.commons.model.ssts.impl.references.EventReference;
import cc.kave.commons.model.ssts.impl.references.FieldReference;
import cc.kave.commons.model.ssts.impl.references.PropertyReference;
import cc.kave.commons.model.ssts.impl.references.UnknownReference;
import cc.kave.commons.model.ssts.impl.references.VariableReference;
import cc.kave.commons.model.ssts.impl.statements.Assignment;
import cc.kave.commons.model.ssts.impl.statements.BreakStatement;
import cc.kave.commons.model.ssts.impl.statements.ContinueStatement;
import cc.kave.commons.model.ssts.impl.statements.ExpressionStatement;
import cc.kave.commons.model.ssts.impl.statements.GotoStatement;
import cc.kave.commons.model.ssts.impl.statements.LabelledStatement;
import cc.kave.commons.model.ssts.impl.statements.ReturnStatement;
import cc.kave.commons.model.ssts.impl.statements.ThrowStatement;
import cc.kave.commons.model.ssts.impl.statements.UnknownStatement;
import cc.kave.commons.model.ssts.impl.statements.VariableDeclaration;
import cc.kave.commons.model.ssts.references.IAssignableReference;
import cc.kave.commons.model.ssts.references.IFieldReference;
import cc.kave.commons.model.ssts.references.IVariableReference;
import cc.kave.commons.model.ssts.statements.IAssignment;
import cc.kave.commons.model.ssts.statements.IExpressionStatement;

public class SSTUtil {

	public static IVariableDeclaration declare(String identifier, TypeName type) {
		VariableDeclaration variable = new VariableDeclaration();
		variable.setReference(variableReference(identifier));
		variable.setType(type);
		return variable;
	}

	public static IReferenceExpression referenceExprToVariable(String id) {
		ReferenceExpression referenceExpression = new ReferenceExpression();
		referenceExpression.setReference(variableReference(id));
		return referenceExpression;
	}

	public static IVariableReference variableReference(String id) {
		VariableReference variableReference = new VariableReference();
		variableReference.setIdentifier(id);
		return variableReference;
	}

	public static IComposedExpression composedExpression(String... strReference) {
		ComposedExpression composedExpression = new ComposedExpression();
		List<IVariableReference> varRefs = new ArrayList<IVariableReference>();
		for (int i = 0; i < strReference.length; i++)
			varRefs.add(variableReference(strReference[i]));
		composedExpression.setReferences(varRefs);
		return composedExpression;
	}

	public static IAssignment assignmentToLocal(String identifier, IAssignableExpression expr) {
		Assignment assignment = new Assignment();
		assignment.setExpression(expr);
		assignment.setReference(variableReference(identifier));
		return assignment;
	}

	public static IExpressionStatement invocationStatement(MethodName name, Iterator<ISimpleExpression> parameters) {
		ExpressionStatement expressionStatement = new ExpressionStatement();
		expressionStatement.setExpression(invocationExpression(name, parameters));
		return expressionStatement;
	}

	public static IExpressionStatement invocationStatement(String id, MethodName name) {
		ArrayList<ISimpleExpression> simpleExpr = new ArrayList<>();
		return invocationStatement(id, name, simpleExpr.iterator());
	}

	public static IExpressionStatement invocationStatement(String id, MethodName name,
			Iterator<ISimpleExpression> parameters) {
		ExpressionStatement exprStatement = new ExpressionStatement();
		exprStatement.setExpression(invocationExpression(id, name, parameters));
		return exprStatement;
	}

	public static IInvocationExpression invocationExpression(String id, MethodName name) {
		ArrayList<ISimpleExpression> parameters = new ArrayList<>();
		return invocationExpression(id, name, parameters.iterator());
	}

	public static IInvocationExpression invocationExpression(MethodName name, Iterator<ISimpleExpression> parameters) {
		// assert (name.isStatic() || name.isConstructor());
		InvocationExpression invoExpr = new InvocationExpression();
		invoExpr.setMethodName(name);
		invoExpr.setParameters(Lists.newArrayList(parameters));
		return invoExpr;
	}

	public static IInvocationExpression invocationExpression(String id, MethodName name,
			Iterator<ISimpleExpression> parameters) {
		// assert (name.isStatic() || name.isConstructor());
		InvocationExpression invocationExpression = new InvocationExpression();
		invocationExpression.setMethodName(name);
		invocationExpression.setParameters(Lists.newArrayList(parameters));
		invocationExpression.setReference(variableReference(id));
		return invocationExpression;
	}

	public static ILockBlock lockBlock(String id) {
		LockBlock lockBlock = new LockBlock();
		lockBlock.setReference(variableReference(id));
		return lockBlock;
	}

	public static IStatement returnStatement(ISimpleExpression expr) {
		ReturnStatement returnStatement = new ReturnStatement();
		returnStatement.setExpression(expr);
		return returnStatement;
	}

	public static IStatement returnVariable(String id) {
		return returnStatement(referenceExprToVariable(id));
	}

	public static IStatement label(String label, IStatement statement) {
		LabelledStatement labelled = new LabelledStatement();
		labelled.setLabel(label);
		labelled.setStatement(statement);
		return labelled;
	}

	public static IStatement invocationStatement(MethodName name, ISimpleExpression... parameters) {
		return expr(invocationExpr(name, parameters));
	}

	public static IInvocationExpression invocationExpr(MethodName name, ISimpleExpression... parameters) {
		InvocationExpression invocation = new InvocationExpression();
		invocation.setParameters(Lists.newArrayList(parameters));
		invocation.setMethodName(name);
		return invocation;
	}

	public static IStatement returnStatement(ISimpleExpression expr, boolean isVoid) {
		ReturnStatement statement = new ReturnStatement();
		statement.setIsVoid(isVoid);
		statement.setExpression(expr);
		return statement;
	}

	public static IExpressionStatement expr(IAssignableExpression expr) {
		ExpressionStatement statement = new ExpressionStatement();
		statement.setExpression(expr);
		return statement;
	}

	public static IMethodDeclaration declareMethod(MethodName name, boolean entryPoint, IStatement... statements) {
		MethodDeclaration method = new MethodDeclaration();
		method.setName(name);
		method.setEntryPoint(entryPoint);
		for (IStatement s : statements)
			method.getBody().add(s);
		return method;
	}

	public static IComposedExpression compose(IVariableReference... refs) {
		ComposedExpression composedExpr = new ComposedExpression();
		composedExpr.setReferences(Lists.newArrayList(refs));
		return composedExpr;
	}

	public static IForLoop forLoop(String var, ILoopHeaderBlockExpression condition, IStatement... body) {
		ForLoop forLoop = new ForLoop();
		forLoop.setInit(Lists.newArrayList(declareVar(var), assign(variableReference(var), constant("0"))));
		forLoop.setStep(Lists.newArrayList(assign(variableReference(var), constant("2"))));
		forLoop.setBody(Lists.newArrayList(body));
		forLoop.setCondition(condition);
		return forLoop;
	}

	public static IIfElseBlock simpleIf(List<IStatement> elseStatement, ISimpleExpression condition,
			IStatement... body) {
		IfElseBlock ifElse = new IfElseBlock();
		ifElse.setCondition(condition);
		ifElse.setThen(Lists.newArrayList(body));
		ifElse.setElse(elseStatement);
		return ifElse;
	}

	public static IDoLoop doLoop(ILoopHeaderBlockExpression condition, IStatement... body) {
		DoLoop doLoop = new DoLoop();
		doLoop.setBody(Lists.newArrayList(body));
		doLoop.setCondition(condition);
		return doLoop;
	}

	public static IForEachLoop forEachLoop(String variable, String ref, IStatement... body) {
		ForEachLoop forEachLoop = new ForEachLoop();
		forEachLoop.setBody(Lists.newArrayList(body));
		forEachLoop.setDeclaration(declareVar(variable));
		forEachLoop.setLoopedReference(variableReference(ref));
		return forEachLoop;
	}

	public static ILockBlock lockBlock(String identifier, IStatement... body) {
		LockBlock lockBlock = new LockBlock();
		lockBlock.setBody(Lists.newArrayList(body));
		lockBlock.setReference(variableReference(identifier));
		return lockBlock;
	}

	public static ISwitchBlock switchBlock(String identifier, ICaseBlock... caseBlocks) {
		SwitchBlock switchBlock = new SwitchBlock();
		switchBlock.setReference(variableReference(identifier));
		switchBlock.setSections(Lists.newArrayList(caseBlocks));
		return switchBlock;
	}

	public static ICaseBlock caseBlock(String label, IStatement... body) {
		CaseBlock caseBlock = new CaseBlock();
		caseBlock.setLabel(constant(label));
		caseBlock.setBody(Lists.newArrayList(body));
		return caseBlock;
	}

	public static ITryBlock simpleTryBlock(IStatement... body) {
		TryBlock tryBlock = new TryBlock();
		tryBlock.setBody(Lists.newArrayList(body));
		return tryBlock;
	}

	public static ITryBlock simpleTryBlock(List<IStatement> body, ICatchBlock... catchBlocks) {
		TryBlock tryBlock = new TryBlock();
		tryBlock.setBody(body);
		tryBlock.setCatchBlocks(Lists.newArrayList(catchBlocks));
		return tryBlock;
	}

	public static ITryBlock tryBlock(IStatement body, IStatement finallyB, ICatchBlock block) {
		TryBlock tryBlock = new TryBlock();
		tryBlock.setBody(Lists.newArrayList(body));
		tryBlock.setCatchBlocks(Lists.newArrayList(block));
		tryBlock.setFinally(Lists.newArrayList(finallyB));
		return tryBlock;
	}

	public static ICatchBlock catchBlock(IStatement... body) {
		CatchBlock catchBlock = new CatchBlock();
		catchBlock.setBody(Lists.newArrayList(body));
		return catchBlock;
	}

	public static IUncheckedBlock uncheckedBlock(IStatement... body) {
		UncheckedBlock uncheckedBlock = new UncheckedBlock();
		uncheckedBlock.setBody(Lists.newArrayList(body));
		return uncheckedBlock;
	}

	public static IUsingBlock usingBlock(IVariableReference ref, IStatement... body) {
		UsingBlock usingBlock = new UsingBlock();
		usingBlock.setReference(ref);
		usingBlock.setBody(Lists.newArrayList(body));
		return usingBlock;
	}

	public static ILambdaExpression lambdaExpr(IStatement... body) {
		LambdaExpression expr = new LambdaExpression();
		expr.setBody(Lists.newArrayList(body));
		return expr;
	}

	public static IWhileLoop whileLoop(ILoopHeaderBlockExpression condition, IStatement... body) {
		WhileLoop whileLoop = new WhileLoop();
		whileLoop.setBody(Lists.newArrayList(body));
		whileLoop.setCondition(condition);
		return whileLoop;
	}

	public static IIfElseExpression ifElseExpr(ISimpleExpression condition, ISimpleExpression elseExpr,
			ISimpleExpression thenExpr) {
		IfElseExpression expr = new IfElseExpression();
		expr.setCondition(condition);
		expr.setElseExpression(elseExpr);
		expr.setThenExpression(thenExpr);
		return expr;
	}

	public static ILoopHeaderBlockExpression loopHeader(IStatement... condition) {
		LoopHeaderBlockExpression loopheader = new LoopHeaderBlockExpression();
		loopheader.setBody(Lists.newArrayList(condition));
		return loopheader;
	}

	public static IAssignment assign(IAssignableReference ref, IAssignableExpression expr) {
		Assignment statement = new Assignment();
		statement.setReference(ref);
		statement.setExpression(expr);
		return statement;
	}

	public static IConstantValueExpression constant(String value) {
		ConstantValueExpression constant = new ConstantValueExpression();
		constant.setValue(value);
		return constant;
	}

	public static IReferenceExpression refExpr(String identifier) {
		ReferenceExpression refExpr = new ReferenceExpression();
		refExpr.setReference(variableReference(identifier));
		return refExpr;
	}

	public static IReferenceExpression refExpr(IReference identifier) {
		ReferenceExpression refExpr = new ReferenceExpression();
		refExpr.setReference(identifier);
		return refExpr;
	}

	public static IFieldReference fieldReference(String identifier) {
		FieldReference ref = new FieldReference();
		ref.setFieldName(CsFieldName.newFieldName(identifier));
		ref.setReference(variableReference(identifier));
		return ref;
	}

	public static IStatement breakStatement() {
		BreakStatement breakStatement = new BreakStatement();
		return breakStatement;
	}

	public static IStatement continueStatement() {
		ContinueStatement continueStatement = new ContinueStatement();
		return continueStatement;
	}

	public static IStatement gotoStatement(String label) {
		GotoStatement gotoStatement = new GotoStatement();
		gotoStatement.setLabel(label);
		return gotoStatement;
	}

	public static IStatement unknownStatement() {
		UnknownStatement statement = new UnknownStatement();
		return statement;
	}

	public static IReference refEvent(String identifier) {
		EventReference event = new EventReference();
		event.setReference(variableReference(identifier));
		return event;
	}

	public static ISimpleExpression unknownExpression() {
		UnknownExpression expr = new UnknownExpression();
		return expr;
	}

	public static ISimpleExpression nullExpr() {
		NullExpression expr = new NullExpression();
		return expr;
	}

	public static IAssignableExpression completionExpr(String ref) {
		CompletionExpression completionExpr = new CompletionExpression();
		completionExpr.setToken("token");
		completionExpr.setObjectReference(variableReference(ref));
		return completionExpr;
	}

	public static IReference unknownRef() {
		UnknownReference ref = new UnknownReference();
		return ref;
	}

	public static IReference refProperty(String identifier) {
		PropertyReference ref = new PropertyReference();
		ref.setReference(variableReference(identifier));
		return ref;
	}

	public static IStatement throwStatement() {
		ThrowStatement statement = new ThrowStatement();
		return statement;
	}

	public static IStatement unsafeBlock() {
		UnsafeBlock block = new UnsafeBlock();
		return block;
	}

	public static IVariableDeclaration declareVar(String identifier) {
		// TODO: int, bool, unknown Type methods
		VariableDeclaration variable = new VariableDeclaration();
		variable.setReference(variableReference(identifier));
		return variable;
	}

	public static IVariableDeclaration declareVar(String identifier, TypeName type) {
		VariableDeclaration variable = new VariableDeclaration();
		variable.setType(type);
		variable.setReference(variableReference(identifier));
		return variable;
	}

	public static Set<IFieldDeclaration> declareFields(String... fieldNames) {
		Set<IFieldDeclaration> fields = new HashSet<>();
		for (String name : fieldNames) {
			FieldDeclaration fieldDeclaration = new FieldDeclaration();
			fieldDeclaration.setName(CsFieldName.newFieldName(name));
			fields.add(fieldDeclaration);
		}
		return fields;
	}

}
