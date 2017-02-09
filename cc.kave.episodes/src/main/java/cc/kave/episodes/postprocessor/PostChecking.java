package cc.kave.episodes.postprocessor;

import static cc.recommenders.assertions.Asserts.assertTrue;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import cc.kave.commons.model.naming.Names;
import cc.kave.commons.model.naming.codeelements.IMethodName;
import cc.kave.commons.model.naming.types.ITypeName;
import cc.kave.episodes.io.EventStreamIo;
import cc.kave.episodes.io.ValidationDataIO;
import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.model.events.EventKind;
import cc.kave.episodes.model.events.Fact;
import cc.recommenders.datastructures.Tuple;
import cc.recommenders.io.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class PostChecking {

	private EventStreamIo trainStreamIo;
	private ValidationDataIO valStreamIo;

	@Inject
	public PostChecking(EventStreamIo streamIo, ValidationDataIO validation) {
		this.trainStreamIo = streamIo;
		this.valStreamIo = validation;
	}

	private static final int FOLDNUM = 0;
	private static final int METHODSIZE = 500;

	public void numEvents(int frequency) {
		List<Event> events = trainStreamIo.readMapping(frequency, FOLDNUM);

		Logger.log("Number of events in event stream: %d", events.size());
	}

	public void trainValOverlaps(int frequency) {
		List<Tuple<Event, List<Fact>>> trainStream = trainStreamIo.parseStream(
				frequency, FOLDNUM);
		List<Event> valStream = valStreamIo.read(frequency, FOLDNUM);

		Set<ITypeName> trainTypes = getTrainCtxs(trainStream);
		Set<ITypeName> valTypes = getValCtxs(valStream);

		Set<ITypeName> overlaps = getSetOverlaps(trainTypes, valTypes);
		Logger.log(
				"Number of overlaping types for training and validation stream: %d",
				overlaps.size());
		Logger.log("Number of types in training stream: %d", trainTypes.size());
		Logger.log("Number of types in validation stream: %d", valTypes.size());

		if (overlaps.size() > 0) {
			for (ITypeName type : overlaps) {
				Logger.log("Type name: %s", type.getFullName());
				Logger.log("Is it unknown type? %s",
						type.equals(Names.getUnknownType()));
			}
		}
	}

	public void methodSize(int frequency) {
		List<Tuple<Event, List<Fact>>> trainStream = trainStreamIo.parseStream(
				frequency, FOLDNUM);
		List<Event> longMethodsCtxs = Lists.newLinkedList();

		for (Tuple<Event, List<Fact>> tuple : trainStream) {

			if ((tuple.getSecond().size() > METHODSIZE)
					&& !(tuple.getFirst().getMethod().equals(Names
							.getUnknownMethod()))) {
				longMethodsCtxs.add(tuple.getFirst());
			}
		}
		Logger.log("Number of methods with more than 500 events: %d", longMethodsCtxs.size());
		for (Tuple<Event, List<Fact>> tuple : trainStream) {
			
			if (longMethodsCtxs.contains(tuple.getFirst())) {
				IMethodName method = tuple.getFirst().getMethod();
				String typeName = method.getDeclaringType().getFullName();
				String methodName = method.getName();
				String fullName = typeName + "." + methodName;
				Logger.log("%s: %d events", fullName, tuple.getSecond().size());
			} 
		}
	}

	public void methodContent(int frequency) {
		List<Tuple<Event, List<Fact>>> trainStream = trainStreamIo.parseStream(
				frequency, FOLDNUM);
		List<Event> events = trainStreamIo.readMapping(frequency, FOLDNUM);

		for (Tuple<Event, List<Fact>> method : trainStream) {
			assertTrue(
					method.getFirst().getKind() == EventKind.METHOD_DECLARATION,
					"Method contexts is not the element!");
			List<Event> invocations = getInvocations(method.getSecond(), events);

			for (Event event : invocations) {
				assertTrue(event.getKind() != EventKind.METHOD_DECLARATION,
						"There is method element in method body!");
			}
		}
		Logger.log("Method content is correct in training stream!");
	}

	private List<Event> getInvocations(List<Fact> method, List<Event> events) {
		List<Event> invocations = Lists.newLinkedList();

		for (Fact fact : method) {
			invocations.add(events.get(fact.getFactID()));
		}
		assertTrue(method.size() == invocations.size(),
				"Method length changes when converting from facts to events!");
		return invocations;
	}

	private Set<ITypeName> getValCtxs(List<Event> valStream) {
		Set<ITypeName> types = Sets.newLinkedHashSet();

		for (Event event : valStream) {
			if (event.getKind() == EventKind.METHOD_DECLARATION) {
				try {
					ITypeName typeName = event.getMethod().getDeclaringType();
					types.add(typeName);
				} catch (Exception e) {
					continue;
				}
			}
		}
		return types;
	}

	private Set<ITypeName> getTrainCtxs(
			List<Tuple<Event, List<Fact>>> trainStream) {
		Set<ITypeName> types = Sets.newLinkedHashSet();

		for (Tuple<Event, List<Fact>> tuple : trainStream) {
			try {
				types.add(tuple.getFirst().getMethod().getDeclaringType());
			} catch (Exception e) {
				continue;
			}
		}
		return types;
	}

	private Set<ITypeName> getSetOverlaps(Set<ITypeName> set1,
			Set<ITypeName> set2) {
		Set<ITypeName> results = Sets.newLinkedHashSet();

		for (ITypeName type : set2) {
			if (set1.contains(type)) {
				results.add(type);
			}
		}
		return results;
	}
}
