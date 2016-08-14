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
package cc.kave.commons.utils.json;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cc.kave.commons.model.episodes.EventKind;
import cc.kave.commons.model.events.ActivityEvent;
import cc.kave.commons.model.events.CommandEvent;
import cc.kave.commons.model.events.ErrorEvent;
import cc.kave.commons.model.events.InfoEvent;
import cc.kave.commons.model.events.NavigationEvent;
import cc.kave.commons.model.events.NavigationType;
import cc.kave.commons.model.events.SystemEvent;
import cc.kave.commons.model.events.SystemEventType;
import cc.kave.commons.model.events.Trigger;
import cc.kave.commons.model.events.completionevents.CompletionEvent;
import cc.kave.commons.model.events.completionevents.Context;
import cc.kave.commons.model.events.completionevents.Proposal;
import cc.kave.commons.model.events.completionevents.ProposalSelection;
import cc.kave.commons.model.events.completionevents.TerminationState;
import cc.kave.commons.model.events.testrunevents.TestCaseResult;
import cc.kave.commons.model.events.testrunevents.TestResult;
import cc.kave.commons.model.events.testrunevents.TestRunEvent;
import cc.kave.commons.model.events.userprofiles.Educations;
import cc.kave.commons.model.events.userprofiles.Likert7Point;
import cc.kave.commons.model.events.userprofiles.Positions;
import cc.kave.commons.model.events.userprofiles.UserProfileEvent;
import cc.kave.commons.model.events.userprofiles.YesNoUnknown;
import cc.kave.commons.model.events.versioncontrolevents.VersionControlAction;
import cc.kave.commons.model.events.versioncontrolevents.VersionControlActionType;
import cc.kave.commons.model.events.versioncontrolevents.VersionControlEvent;
import cc.kave.commons.model.events.visualstudio.BuildEvent;
import cc.kave.commons.model.events.visualstudio.BuildTarget;
import cc.kave.commons.model.events.visualstudio.DebuggerEvent;
import cc.kave.commons.model.events.visualstudio.DebuggerMode;
import cc.kave.commons.model.events.visualstudio.DocumentAction;
import cc.kave.commons.model.events.visualstudio.DocumentEvent;
import cc.kave.commons.model.events.visualstudio.EditEvent;
import cc.kave.commons.model.events.visualstudio.FindEvent;
import cc.kave.commons.model.events.visualstudio.IDEStateEvent;
import cc.kave.commons.model.events.visualstudio.InstallEvent;
import cc.kave.commons.model.events.visualstudio.LifecyclePhase;
import cc.kave.commons.model.events.visualstudio.SolutionAction;
import cc.kave.commons.model.events.visualstudio.SolutionEvent;
import cc.kave.commons.model.events.visualstudio.UpdateEvent;
import cc.kave.commons.model.events.visualstudio.WindowAction;
import cc.kave.commons.model.events.visualstudio.WindowEvent;
import cc.kave.commons.model.naming.impl.v0.GeneralName;
import cc.kave.commons.model.naming.impl.v0.codeelements.AliasName;
import cc.kave.commons.model.naming.impl.v0.codeelements.EventName;
import cc.kave.commons.model.naming.impl.v0.codeelements.FieldName;
import cc.kave.commons.model.naming.impl.v0.codeelements.LambdaName;
import cc.kave.commons.model.naming.impl.v0.codeelements.LocalVariableName;
import cc.kave.commons.model.naming.impl.v0.codeelements.MethodName;
import cc.kave.commons.model.naming.impl.v0.codeelements.ParameterName;
import cc.kave.commons.model.naming.impl.v0.codeelements.PropertyName;
import cc.kave.commons.model.naming.impl.v0.idecomponents.CommandBarControlName;
import cc.kave.commons.model.naming.impl.v0.idecomponents.CommandName;
import cc.kave.commons.model.naming.impl.v0.idecomponents.DocumentName;
import cc.kave.commons.model.naming.impl.v0.idecomponents.ProjectItemName;
import cc.kave.commons.model.naming.impl.v0.idecomponents.ProjectName;
import cc.kave.commons.model.naming.impl.v0.idecomponents.SolutionName;
import cc.kave.commons.model.naming.impl.v0.idecomponents.WindowName;
import cc.kave.commons.model.naming.impl.v0.others.ReSharperLiveTemplateName;
import cc.kave.commons.model.naming.impl.v0.types.ArrayTypeName;
import cc.kave.commons.model.naming.impl.v0.types.DelegateTypeName;
import cc.kave.commons.model.naming.impl.v0.types.PredefinedTypeName;
import cc.kave.commons.model.naming.impl.v0.types.TypeName;
import cc.kave.commons.model.naming.impl.v0.types.TypeParameterName;
import cc.kave.commons.model.naming.impl.v0.types.organization.AssemblyName;
import cc.kave.commons.model.naming.impl.v0.types.organization.AssemblyVersion;
import cc.kave.commons.model.naming.impl.v0.types.organization.NamespaceName;
import cc.kave.commons.model.ssts.IMemberDeclaration;
import cc.kave.commons.model.ssts.IReference;
import cc.kave.commons.model.ssts.ISST;
import cc.kave.commons.model.ssts.IStatement;
import cc.kave.commons.model.ssts.blocks.CatchBlockKind;
import cc.kave.commons.model.ssts.blocks.ICaseBlock;
import cc.kave.commons.model.ssts.blocks.ICatchBlock;
import cc.kave.commons.model.ssts.declarations.IDelegateDeclaration;
import cc.kave.commons.model.ssts.declarations.IEventDeclaration;
import cc.kave.commons.model.ssts.declarations.IFieldDeclaration;
import cc.kave.commons.model.ssts.declarations.IMethodDeclaration;
import cc.kave.commons.model.ssts.declarations.IPropertyDeclaration;
import cc.kave.commons.model.ssts.expressions.IAssignableExpression;
import cc.kave.commons.model.ssts.expressions.ILoopHeaderExpression;
import cc.kave.commons.model.ssts.expressions.ISimpleExpression;
import cc.kave.commons.model.ssts.expressions.assignable.BinaryOperator;
import cc.kave.commons.model.ssts.expressions.assignable.CastOperator;
import cc.kave.commons.model.ssts.expressions.assignable.IIndexAccessExpression;
import cc.kave.commons.model.ssts.expressions.assignable.UnaryOperator;
import cc.kave.commons.model.ssts.impl.SST;
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
import cc.kave.commons.model.ssts.impl.declarations.DelegateDeclaration;
import cc.kave.commons.model.ssts.impl.declarations.EventDeclaration;
import cc.kave.commons.model.ssts.impl.declarations.FieldDeclaration;
import cc.kave.commons.model.ssts.impl.declarations.MethodDeclaration;
import cc.kave.commons.model.ssts.impl.declarations.PropertyDeclaration;
import cc.kave.commons.model.ssts.impl.expressions.assignable.BinaryExpression;
import cc.kave.commons.model.ssts.impl.expressions.assignable.CastExpression;
import cc.kave.commons.model.ssts.impl.expressions.assignable.CompletionExpression;
import cc.kave.commons.model.ssts.impl.expressions.assignable.ComposedExpression;
import cc.kave.commons.model.ssts.impl.expressions.assignable.IfElseExpression;
import cc.kave.commons.model.ssts.impl.expressions.assignable.IndexAccessExpression;
import cc.kave.commons.model.ssts.impl.expressions.assignable.InvocationExpression;
import cc.kave.commons.model.ssts.impl.expressions.assignable.LambdaExpression;
import cc.kave.commons.model.ssts.impl.expressions.assignable.TypeCheckExpression;
import cc.kave.commons.model.ssts.impl.expressions.assignable.UnaryExpression;
import cc.kave.commons.model.ssts.impl.expressions.loopheader.LoopHeaderBlockExpression;
import cc.kave.commons.model.ssts.impl.expressions.simple.ConstantValueExpression;
import cc.kave.commons.model.ssts.impl.expressions.simple.NullExpression;
import cc.kave.commons.model.ssts.impl.expressions.simple.ReferenceExpression;
import cc.kave.commons.model.ssts.impl.expressions.simple.UnknownExpression;
import cc.kave.commons.model.ssts.impl.references.EventReference;
import cc.kave.commons.model.ssts.impl.references.FieldReference;
import cc.kave.commons.model.ssts.impl.references.IndexAccessReference;
import cc.kave.commons.model.ssts.impl.references.MethodReference;
import cc.kave.commons.model.ssts.impl.references.PropertyReference;
import cc.kave.commons.model.ssts.impl.references.UnknownReference;
import cc.kave.commons.model.ssts.impl.references.VariableReference;
import cc.kave.commons.model.ssts.impl.statements.Assignment;
import cc.kave.commons.model.ssts.impl.statements.BreakStatement;
import cc.kave.commons.model.ssts.impl.statements.ContinueStatement;
import cc.kave.commons.model.ssts.impl.statements.EventSubscriptionStatement;
import cc.kave.commons.model.ssts.impl.statements.ExpressionStatement;
import cc.kave.commons.model.ssts.impl.statements.GotoStatement;
import cc.kave.commons.model.ssts.impl.statements.LabelledStatement;
import cc.kave.commons.model.ssts.impl.statements.ReturnStatement;
import cc.kave.commons.model.ssts.impl.statements.ThrowStatement;
import cc.kave.commons.model.ssts.impl.statements.UnknownStatement;
import cc.kave.commons.model.ssts.impl.statements.VariableDeclaration;
import cc.kave.commons.model.ssts.references.IAssignableReference;
import cc.kave.commons.model.ssts.references.IVariableReference;
import cc.kave.commons.model.ssts.statements.EventSubscriptionOperation;
import cc.kave.commons.model.ssts.statements.IVariableDeclaration;
import cc.kave.commons.model.typeshapes.MethodHierarchy;
import cc.kave.commons.model.typeshapes.TypeHierarchy;
import cc.kave.commons.model.typeshapes.TypeShape;
import cc.kave.commons.utils.json.legacy.GsonUtil;
import cc.recommenders.assertions.Asserts;
import cc.recommenders.assertions.Throws;

public abstract class JsonUtils {

	private static Gson gson;
	private static Gson gsonPretty;

	static {
		gson = createBuilder().create();
		gsonPretty = createBuilder().setPrettyPrinting().create();
	}

	private static GsonBuilder createBuilder() {
		GsonBuilder gb = new GsonBuilder();

		// add support for new Java 8 date/time framework
		Converters.registerAll(gb);
		gb.registerTypeHierarchyAdapter(LocalDateTime.class, new LocalDateTimeConverter());

		GsonUtil.addTypeAdapters(gb);

		registerNames(gb);
		registerSST(gb);
		registerEvents(gb);

		// enums
		gb.registerTypeAdapter(EventKind.class, EnumDeSerializer.create(EventKind.values()));
		gb.registerTypeAdapter(CatchBlockKind.class, EnumDeSerializer.create(CatchBlockKind.values()));
		gb.registerTypeAdapter(EventSubscriptionOperation.class,
				EnumDeSerializer.create(EventSubscriptionOperation.values()));
		gb.registerTypeAdapter(CastOperator.class, EnumDeSerializer.create(CastOperator.values()));
		gb.registerTypeAdapter(BinaryOperator.class, EnumDeSerializer.create(BinaryOperator.values()));
		gb.registerTypeAdapter(UnaryOperator.class, EnumDeSerializer.create(UnaryOperator.values()));

		gb.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE);
		gb.excludeFieldsWithModifiers(java.lang.reflect.Modifier.TRANSIENT);

		return gb;
	}

	private static void registerSST(GsonBuilder gb) {
		// SST Model
		registerHierarchy(gb, ISST.class, SST.class);
		// Declarations
		registerHierarchy(gb, IMemberDeclaration.class, IDelegateDeclaration.class, IEventDeclaration.class,
				IFieldDeclaration.class, IMethodDeclaration.class, IPropertyDeclaration.class, IReference.class);
		registerHierarchy(gb, IFieldDeclaration.class, FieldDeclaration.class);
		registerHierarchy(gb, IDelegateDeclaration.class, DelegateDeclaration.class);
		registerHierarchy(gb, IEventDeclaration.class, EventDeclaration.class);
		registerHierarchy(gb, IPropertyDeclaration.class, PropertyDeclaration.class);
		registerHierarchy(gb, IMethodDeclaration.class, MethodDeclaration.class);
		registerHierarchy(gb, IVariableDeclaration.class, VariableDeclaration.class);
		// References
		registerHierarchy(gb, IReference.class, UnknownReference.class, EventReference.class, FieldReference.class,
				IndexAccessReference.class, PropertyReference.class, MethodReference.class, VariableReference.class);
		registerHierarchy(gb, IAssignableReference.class, EventReference.class, FieldReference.class,
				IndexAccessReference.class, PropertyReference.class, UnknownReference.class, VariableReference.class);
		registerHierarchy(gb, IVariableReference.class, VariableReference.class);
		registerHierarchy(gb, IIndexAccessExpression.class, IndexAccessExpression.class);

		// Expressions
		registerHierarchy(gb, IAssignableExpression.class,
				// assignable
				BinaryExpression.class, CastExpression.class, CompletionExpression.class, ComposedExpression.class,
				IfElseExpression.class, IndexAccessExpression.class, InvocationExpression.class, LambdaExpression.class,
				TypeCheckExpression.class, UnaryExpression.class,
				// simple
				ConstantValueExpression.class, NullExpression.class, ReferenceExpression.class,
				UnknownExpression.class);

		registerHierarchy(gb, ISimpleExpression.class, ConstantValueExpression.class, NullExpression.class,
				ReferenceExpression.class, UnknownExpression.class);

		registerHierarchy(gb, ILoopHeaderExpression.class,
				// loop header
				LoopHeaderBlockExpression.class,
				// simple
				ConstantValueExpression.class, NullExpression.class, ReferenceExpression.class,
				UnknownExpression.class);

		// Statements
		registerHierarchy(gb, IStatement.class, Assignment.class, BreakStatement.class, ContinueStatement.class,
				DoLoop.class, ExpressionStatement.class, ForEachLoop.class, ForLoop.class, GotoStatement.class,
				IfElseBlock.class, LabelledStatement.class, LockBlock.class, ReturnStatement.class, SwitchBlock.class,
				ThrowStatement.class, TryBlock.class, UncheckedBlock.class, UnknownStatement.class, UnsafeBlock.class,
				UsingBlock.class, EventSubscriptionStatement.class, VariableDeclaration.class, WhileLoop.class);

		registerHierarchy(gb, ICatchBlock.class, CatchBlock.class);
		registerHierarchy(gb, ICaseBlock.class, CaseBlock.class);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void registerEvents(GsonBuilder gb) {

		Class<?>[] eventsAndRelatedTypes = new Class<?>[] {
				// completion events
				CompletionEvent.class, TerminationState.class, Proposal.class, ProposalSelection.class, Context.class,
				TypeShape.class, MethodHierarchy.class, TypeHierarchy.class,
				// test run events
				TestRunEvent.class, TestResult.class, TestCaseResult.class,
				// user profile event
				UserProfileEvent.class, Educations.class, Likert7Point.class, Positions.class, YesNoUnknown.class,
				// version control
				VersionControlEvent.class, VersionControlAction.class, VersionControlActionType.class,
				// visual studio
				BuildEvent.class, BuildTarget.class, DebuggerEvent.class, DebuggerMode.class, DocumentEvent.class,
				DocumentAction.class, EditEvent.class, FindEvent.class, IDEStateEvent.class, InstallEvent.class,
				LifecyclePhase.class, SolutionAction.class, SolutionEvent.class, UpdateEvent.class, WindowAction.class,
				WindowEvent.class,
				// general
				ActivityEvent.class, CommandEvent.class, ErrorEvent.class, InfoEvent.class, NavigationEvent.class,
				NavigationType.class, SystemEvent.class, SystemEventType.class, Trigger.class };

		Map<Class, Set<Class>> subclasses = Maps.newHashMap();

		for (Class<?> elem : eventsAndRelatedTypes) {
			if (elem.isEnum()) {
				registerEnum(gb, (Class<Enum>) elem);
			} else {
				for (Class<?> c : getAllTypesFromHierarchyExceptObject(elem, false)) {
					Set<Class> set = subclasses.get(c);
					if (set == null) {
						set = Sets.newHashSet();
						subclasses.put(c, set);
					}
					set.add(elem);
				}
				registerHierarchy(gb, elem, new Class[] { elem });
			}
		}

		for (Class base : subclasses.keySet()) {
			Set<Class> subs = subclasses.get(base);
			Class[] arr = subs.toArray(new Class[0]);
			registerHierarchy(gb, base, arr);
		}
	}

	private static void registerNames(GsonBuilder gb) {

		Class<?>[] names = new Class<?>[] {
				/* ----- v0 ----- */
				GeneralName.class,
				// code elements
				AliasName.class, EventName.class, FieldName.class, LambdaName.class, LocalVariableName.class,
				MethodName.class, ParameterName.class, PropertyName.class,
				// ide components
				CommandBarControlName.class, CommandName.class, DocumentName.class, ProjectItemName.class,
				ProjectName.class, SolutionName.class, WindowName.class,
				// others
				ReSharperLiveTemplateName.class,
				// types
				AssemblyName.class, AssemblyVersion.class, NamespaceName.class,
				//
				ArrayTypeName.class, DelegateTypeName.class, PredefinedTypeName.class, TypeName.class,
				TypeParameterName.class
				/* ----- v1 ----- */
				// yet to come...
		};

		GsonNameDeserializer nameAdapter = new GsonNameDeserializer();

		for (Class<?> concreteName : names) {
			for (Class<?> nameType : getAllTypesFromHierarchyExceptObject(concreteName, false)) {
				gb.registerTypeAdapter(nameType, nameAdapter);
			}
		}
	}

	private static <T extends Enum<T>> void registerEnum(GsonBuilder gb, Class<T> e) {
		T[] constants = e.getEnumConstants();
		gb.registerTypeAdapter(e, EnumDeSerializer.create(constants));
	}

	private static Set<Class<?>> getAllTypesFromHierarchyExceptObject(Class<?> elem, boolean includeElem) {
		Set<Class<?>> hierarchy = Sets.newHashSet();
		if (elem == null || elem.equals(Object.class)) {
			return hierarchy;
		}

		if (includeElem) {
			hierarchy.add(elem);
		}

		for (Class<?> i : elem.getInterfaces()) {
			hierarchy.addAll(getAllTypesFromHierarchyExceptObject(i, true));
		}
		hierarchy.addAll(getAllTypesFromHierarchyExceptObject(elem.getSuperclass(), true));

		return hierarchy;
	}

	@SafeVarargs
	private static <T> void registerHierarchy(GsonBuilder gsonBuilder, Class<T> type, Class<? extends T>... subtypes) {

		System.out.printf("<< %s >>\n", type.getSimpleName());
		for (Class c : subtypes) {
			System.out.printf("- %s\n", c.getSimpleName());
		}
		System.out.println();

		Asserts.assertTrue(subtypes.length > 0);

		RuntimeTypeAdapterFactory<T> factory = RuntimeTypeAdapterFactory.of(type, "$type");
		for (int i = 0; i < subtypes.length; i++) {
			factory = factory.registerSubtype(subtypes[i]);
		}

		gsonBuilder.registerTypeAdapterFactory(factory);
	}

	public static <T> T fromJson(String json, Type targetType) {
		json = TypeUtil.toJavaTypeNames(json);
		return gson.fromJson(json, targetType);
	}

	public static <T> String toJson(Object obj, Type targetType) {
		String json = gson.toJsonTree(obj, targetType).toString();
		return TypeUtil.toCSharpTypeNames(json);
	}

	public static <T> String toJson(Object obj) {
		String json = gson.toJson(obj);
		return TypeUtil.toCSharpTypeNames(json);
	}

	public static <T> String toJsonFormatted(Object obj) {
		String json = gsonPretty.toJson(obj).replace("  ", "    ");
		return TypeUtil.toCSharpTypeNames(json);
	}

	public static <T> T fromJson(File file, Type classOfT) {
		try {
			String json = FileUtils.readFileToString(file);
			return fromJson(json, classOfT);
		} catch (IOException e) {
			throw Throws.throwUnhandledException(e);
		}
	}

	public static <T> T fromJson(InputStream in, Type classOfT) {
		try {
			String json = IOUtils.toString(in, Charset.defaultCharset().toString());
			return fromJson(json, classOfT);
		} catch (IOException e) {
			throw Throws.throwUnhandledException(e);
		}
	}

	public static <T> void toJson(T obj, File file) {
		try {
			String json = toJson(obj);
			FileUtils.writeStringToFile(file, json);
		} catch (IOException e) {
			throw Throws.throwUnhandledException(e);
		}
	}
}