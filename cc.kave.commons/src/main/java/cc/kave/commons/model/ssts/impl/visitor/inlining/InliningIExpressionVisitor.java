package cc.kave.commons.model.ssts.impl.visitor.inlining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.kave.commons.model.names.IMethodName;
import cc.kave.commons.model.names.IParameterName;
import cc.kave.commons.model.ssts.IExpression;
import cc.kave.commons.model.ssts.IStatement;
import cc.kave.commons.model.ssts.declarations.IMethodDeclaration;
import cc.kave.commons.model.ssts.expressions.ISimpleExpression;
import cc.kave.commons.model.ssts.expressions.assignable.ICompletionExpression;
import cc.kave.commons.model.ssts.expressions.assignable.IComposedExpression;
import cc.kave.commons.model.ssts.expressions.assignable.IIfElseExpression;
import cc.kave.commons.model.ssts.expressions.assignable.IInvocationExpression;
import cc.kave.commons.model.ssts.expressions.assignable.ILambdaExpression;
import cc.kave.commons.model.ssts.expressions.loopheader.ILoopHeaderBlockExpression;
import cc.kave.commons.model.ssts.expressions.simple.IConstantValueExpression;
import cc.kave.commons.model.ssts.expressions.simple.INullExpression;
import cc.kave.commons.model.ssts.expressions.simple.IReferenceExpression;
import cc.kave.commons.model.ssts.expressions.simple.IUnknownExpression;
import cc.kave.commons.model.ssts.impl.SSTUtil;
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
import cc.kave.commons.model.ssts.impl.references.VariableReference;
import cc.kave.commons.model.ssts.impl.visitor.AbstractThrowingNodeVisitor;
import cc.kave.commons.model.ssts.impl.visitor.inlining.util.CountReturnContext;
import cc.kave.commons.model.ssts.impl.visitor.inlining.util.CountReturnsVisitor;
import cc.kave.commons.model.ssts.references.IMemberReference;
import cc.kave.commons.model.ssts.references.IVariableReference;
import cc.kave.commons.model.ssts.statements.IReturnStatement;
import cc.kave.commons.model.ssts.statements.IVariableDeclaration;

public class InliningIExpressionVisitor extends AbstractThrowingNodeVisitor<InliningContext, IExpression> {

	public IExpression visit(IInvocationExpression expr, InliningContext context) {
		IMethodDeclaration method = context.getNonEntryPoint(expr.getMethodName());
		if (method != null) {
			context.addInlinedMethod(method);
			List<IStatement> body = new ArrayList<>();
			Map<IVariableReference, IVariableReference> preChangedNames = new HashMap<>();

			if (method.getName().hasParameters()) {
				createParameterVariables(body, method.getName().getParameters(), expr.getParameters(), preChangedNames);
			}
			// Checking if guards statements are needed and setting them up if
			// so
			CountReturnContext countReturnContext = new CountReturnContext();
			method.accept(new CountReturnsVisitor(), countReturnContext);
			int returnStatementCount = countReturnContext.returnCount;
			context.setVoid(countReturnContext.isVoid);
			boolean guardsNeeded = returnStatementCount > 1 ? true : false;
			if (returnStatementCount == 1 && method.getBody().size() > 0) {
				guardsNeeded = !(method.getBody().get(method.getBody().size() - 1) instanceof IReturnStatement);
			}
			if (guardsNeeded) {
				setupGuardVariables(method.getName(), body, context);
			}
			// Add all Statements from the method body
			body.addAll(method.getBody());
			// enter a new Scope
			context.enterScope(body, preChangedNames);
			context.setInline(true);
			context.setGuardVariableNames(method.getName().getIdentifier());
			if (!guardsNeeded) {
				for (IStatement statement : body) {
					if (statement instanceof IReturnStatement) {
						IReturnStatement stmt = (IReturnStatement) statement;
						context.leaveScope();
						context.setInline(false);
						return stmt.getExpression();
					}
					statement.accept(context.getStatementVisitor(), context);
				}
			} else {
				context.visitBlock(body, context.getBody());
				context.leaveScope();
				context.setInline(false);
				if (!context.isVoid())
					return SSTUtil.referenceExprToVariable(context.getResultName());
				else
					return null;
			}
			context.leaveScope();
			context.setInline(false);
			return null;
		} else {
			InvocationExpression expression = new InvocationExpression();
			expression.setMethodName(expr.getMethodName());
			for (ISimpleExpression e : expr.getParameters()) {
				expression.getParameters().add(e);
			}
			expression.setReference(
					(IVariableReference) expr.getReference().accept(context.getReferenceVisitor(), context));

			return expression;
		}
	}

	private void setupGuardVariables(IMethodName methodName, List<IStatement> body, InliningContext context) {
		context.setGuardVariableNames(methodName.getIdentifier());
		if (!context.isVoid()) {
			IVariableDeclaration variable = SSTUtil.declare(context.getResultName(), methodName.getReturnType());
			body.add(variable);
		}
		body.add(SSTUtil.declare(context.getGotResultName(), context.GOT_RESULT_TYPE));
		ConstantValueExpression constant = new ConstantValueExpression();
		constant.setValue("true");
		body.add(SSTUtil.assignmentToLocal(context.getGotResultName(), constant));

	}

	private void createParameterVariables(List<IStatement> body, List<IParameterName> parameters,
			List<ISimpleExpression> expressions, Map<IVariableReference, IVariableReference> preChangedNames) {
		for (int i = 0; i < parameters.size(); i++) {
			IParameterName parameter = parameters.get(i);
			// TODO what to do when parameter has out/ref keyWord but is
			// no ReferenceExpression ?
			if (!parameter.isOptional() || expressions.size() == parameters.size()) {

				/*
				 * if (parameter.isPassedByReference() &&
				 * !parameter.isParameterArray() && i < expressions.size() &&
				 * expressions.get(i) instanceof ReferenceExpression) {
				 * ReferenceExpression refExpr = (ReferenceExpression)
				 * expressions.get(i); if (refExpr.getReference() instanceof
				 * VariableReference) {
				 * preChangedNames.put(SSTUtil.variableReference(parameter.
				 * getName()), (IVariableReference) refExpr.getReference()); }
				 * else if (refExpr.getReference() instanceof IMemberReference)
				 * { preChangedNames.put(SSTUtil.variableReference(parameter.
				 * getName()), ((IMemberReference)
				 * refExpr.getReference()).getReference()); } continue; } else
				 * if (parameter.isParameterArray()) {
				 * body.add(SSTUtil.declare(parameter.getName(),
				 * parameter.getValueType()));
				 * body.add(SSTUtil.assigmentToLocal(parameter.getName(), new
				 * UnknownExpression())); break; } else {
				 * body.add(SSTUtil.declare(parameter.getName(),
				 * parameter.getValueType()));
				 * body.add(SSTUtil.assigmentToLocal(parameter.getName(),
				 * expressions.get(i))); }
				 */
				if (parameter.isParameterArray() && !parameter.isPassedByReference()) {
					body.add(SSTUtil.declare(parameter.getName(), parameter.getValueType()));
					body.add(SSTUtil.assignmentToLocal(parameter.getName(), new UnknownExpression()));
					break;
				} else if (i < expressions.size() && expressions.get(i) instanceof ReferenceExpression) {
					ReferenceExpression refExpr = (ReferenceExpression) expressions.get(i);
					if (refExpr.getReference() instanceof VariableReference) {
						preChangedNames.put(SSTUtil.variableReference(parameter.getName()),
								(IVariableReference) refExpr.getReference());
					} else if (refExpr.getReference() instanceof IMemberReference) {
						preChangedNames.put(SSTUtil.variableReference(parameter.getName()),
								((IMemberReference) refExpr.getReference()).getReference());
					}
				} else if (i < expressions.size()) {
					body.add(SSTUtil.declare(parameter.getName(), parameter.getValueType()));
					body.add(SSTUtil.assignmentToLocal(parameter.getName(), expressions.get(i)));
				}
			}
		}

	}

	public IExpression visit(IReferenceExpression expr, InliningContext context) {
		ReferenceExpression refExpr = new ReferenceExpression();
		refExpr.setReference(expr.getReference().accept(context.getReferenceVisitor(), context));
		return refExpr;
	}

	@Override
	public IExpression visit(IConstantValueExpression expr, InliningContext context) {
		ConstantValueExpression constant = new ConstantValueExpression();
		constant.setValue(expr.getValue());
		return constant;
	}

	@Override
	public IExpression visit(IComposedExpression expr, InliningContext context) {
		ComposedExpression composed = new ComposedExpression();
		for (IVariableReference ref : expr.getReferences())
			composed.getReferences().add((IVariableReference) ref.accept(context.getReferenceVisitor(), context));
		return composed;
	}

	@Override
	public IExpression visit(ILoopHeaderBlockExpression expr, InliningContext context) {
		LoopHeaderBlockExpression loopHeader = new LoopHeaderBlockExpression();
		context.visitBlock(expr.getBody(), loopHeader.getBody());
		return loopHeader;
	}

	@Override
	public IExpression visit(IIfElseExpression expr, InliningContext context) {
		IfElseExpression expression = new IfElseExpression();
		expression
				.setCondition((ISimpleExpression) expr.getCondition().accept(context.getExpressionVisitor(), context));
		expression.setElseExpression(
				(ISimpleExpression) expr.getElseExpression().accept(context.getExpressionVisitor(), context));
		expression.setThenExpression(
				(ISimpleExpression) expr.getThenExpression().accept(context.getExpressionVisitor(), context));
		return expression;
	}

	@Override
	public IExpression visit(INullExpression expr, InliningContext context) {
		NullExpression expression = new NullExpression();
		return expression;
	}

	@Override
	public IExpression visit(IUnknownExpression unknownExpr, InliningContext context) {
		UnknownExpression expression = new UnknownExpression();
		return expression;
	}

	@Override
	public IExpression visit(ILambdaExpression expr, InliningContext context) {
		LambdaExpression expression = new LambdaExpression();
		expression.setName(expr.getName());
		context.visitBlock(expr.getBody(), expression.getBody());
		return expression;
	}

	@Override
	public IExpression visit(ICompletionExpression entity, InliningContext context) {
		CompletionExpression expression = new CompletionExpression();
		IVariableReference objectReference = entity.getVariableReference();
		if (objectReference != null)
			expression.setObjectReference(
					(IVariableReference) objectReference.accept(context.getReferenceVisitor(), context));
		expression.setTypeReference(entity.getTypeReference());
		expression.setToken(entity.getToken());
		return expression;
		// TODO tests
	}
}
