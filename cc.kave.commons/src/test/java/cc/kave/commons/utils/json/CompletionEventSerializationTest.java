/**
 * Copyright 2014 Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cc.kave.commons.utils.json;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;

import cc.kave.commons.model.events.Trigger;
import cc.kave.commons.model.events.completionevents.CompletionEvent;
import cc.kave.commons.model.events.completionevents.Context;
import cc.kave.commons.model.events.completionevents.ICompletionEvent;
import cc.kave.commons.model.events.completionevents.IProposal;
import cc.kave.commons.model.events.completionevents.Proposal;
import cc.kave.commons.model.events.completionevents.ProposalSelection;
import cc.kave.commons.model.events.completionevents.TerminationState;
import cc.kave.commons.model.names.csharp.MethodName;
import cc.kave.commons.model.names.csharp.TypeName;
import cc.kave.commons.model.ssts.impl.SST;

public class CompletionEventSerializationTest {

	@Test
	public void verifyToJson() {
		CompletionEvent getExample = GetExample();
		String actual = JsonUtils.toJson(getExample, ICompletionEvent.class);
		String expected = GetExampleJson_Current();
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void verifyFromJson() {
		ICompletionEvent actual = JsonUtils.fromJson(GetExampleJson_Current(), ICompletionEvent.class);
		ICompletionEvent expected = GetExample();
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void verifyObjToObjEquality() {
		String json = JsonUtils.toJson(GetExample(), ICompletionEvent.class);
		CompletionEvent actual = JsonUtils.fromJson(json, ICompletionEvent.class);
		CompletionEvent expected = GetExample();
		Assert.assertEquals(expected, actual);
	}

	private static CompletionEvent GetExample() {
		CompletionEvent e = new CompletionEvent();

		// IDEEvent

		e.IDESessionUUID = "0xDEADBEEF";
		e.KaVEVersion = "1.0";
		e.TriggeredAt = getDate("2012-02-23T18:54:59.549");
		e.TriggeredBy = Trigger.Unknown;
		e.Duration = "00:00:02";
		e.ActiveWindow = "VisualStudio.WindowName:vsWindowTypeDocument File.cs";
		e.ActiveDocument = "VisualStudio.DocumentName:\\Path\\To\\File.cs";

		// CompletionEvent

		SST sst = new SST();
		sst.setEnclosingType(TypeName.newTypeName("T,P"));
		e.context = new Context();
		e.context.setSST(sst);

		e.proposalCollection = Lists.newLinkedList();
		e.proposalCollection.add(createProposal("[T1,P1] [T1,P2].M1()"));
		e.proposalCollection.add(createProposal("[T1,P1] [T1,P2].M2()"));

		e.selections = Lists.newLinkedList();
		e.selections.add(createProposalSelection("[T1,P1] [T1,P2].M1()", "18:54:59.6720000"));
		e.selections.add(createProposalSelection("[T1,P1] [T1,P2].M2()", "18:54:59.7830000"));
		e.selections.add(createProposalSelection("[T1,P1] [T1,P2].M1()", "18:54:59.8940000"));

		e.terminatedBy = Trigger.Typing;
		e.terminatedState = TerminationState.Applied;

		return e;
	}

	private static LocalDateTime getDate(String str) {
		return LocalDateTime.parse(str, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
	}

	private static ProposalSelection createProposalSelection(String methodName, String selectedAfter) {
		IProposal p = createProposal(methodName);
		ProposalSelection ps = new ProposalSelection(p);
		ps.SelectedAfter = selectedAfter;
		return ps;
	}

	private static IProposal createProposal(String methodName) {
		Proposal p = new Proposal();
		p.Name = MethodName.newMethodName(methodName);
		p.Relevance = 42;
		return p;
	}

	private static String GetExampleJson_Current() {
		// should reflect current serialization format!
		return "{\"$type\":\"KaVE.Commons.Model.Events.CompletionEvents.CompletionEvent, KaVE.Commons\",\"Context2\":{\"$type\":\"KaVE.Commons.Model.Events.CompletionEvents.Context, KaVE.Commons\",\"TypeShape\":{\"$type\":\"KaVE.Commons.Model.TypeShapes.TypeShape, KaVE.Commons\",\"TypeHierarchy\":{\"$type\":\"KaVE.Commons.Model.TypeShapes.TypeHierarchy, KaVE.Commons\",\"Element\":\"CSharp.UnknownTypeName:?\",\"Implements\":[]},\"MethodHierarchies\":[]},\"SST\":{\"$type\":\"[SST:SST]\",\"EnclosingType\":\"CSharp.TypeName:T,P\",\"PartialClassIdentifier\":\"\",\"Fields\":[],\"Properties\":[],\"Methods\":[],\"Events\":[],\"Delegates\":[]}},\"ProposalCollection\":[{\"$type\":\"KaVE.Commons.Model.Events.CompletionEvents.Proposal, KaVE.Commons\",\"Name\":\"CSharp.MethodName:[T1,P1] [T1,P2].M1()\",\"Relevance\":42},{\"$type\":\"KaVE.Commons.Model.Events.CompletionEvents.Proposal, KaVE.Commons\",\"Name\":\"CSharp.MethodName:[T1,P1] [T1,P2].M2()\",\"Relevance\":42}],\"Selections\":[{\"$type\":\"KaVE.Commons.Model.Events.CompletionEvents.ProposalSelection, KaVE.Commons\",\"Proposal\":{\"$type\":\"KaVE.Commons.Model.Events.CompletionEvents.Proposal, KaVE.Commons\",\"Name\":\"CSharp.MethodName:[T1,P1] [T1,P2].M1()\",\"Relevance\":42},\"SelectedAfter\":\"18:54:59.6720000\",\"Index\":-1},{\"$type\":\"KaVE.Commons.Model.Events.CompletionEvents.ProposalSelection, KaVE.Commons\",\"Proposal\":{\"$type\":\"KaVE.Commons.Model.Events.CompletionEvents.Proposal, KaVE.Commons\",\"Name\":\"CSharp.MethodName:[T1,P1] [T1,P2].M2()\",\"Relevance\":42},\"SelectedAfter\":\"18:54:59.7830000\",\"Index\":-1},{\"$type\":\"KaVE.Commons.Model.Events.CompletionEvents.ProposalSelection, KaVE.Commons\",\"Proposal\":{\"$type\":\"KaVE.Commons.Model.Events.CompletionEvents.Proposal, KaVE.Commons\",\"Name\":\"CSharp.MethodName:[T1,P1] [T1,P2].M1()\",\"Relevance\":42},\"SelectedAfter\":\"18:54:59.8940000\",\"Index\":-1}],\"TerminatedBy\":3,\"TerminatedState\":0,\"ProposalCount\":0,\"IDESessionUUID\":\"0xDEADBEEF\",\"KaVEVersion\":\"1.0\",\"TriggeredAt\":\"2012-02-23T18:54:59.549\",\"TriggeredBy\":0,\"Duration\":\"00:00:02\",\"ActiveWindow\":\"VisualStudio.WindowName:vsWindowTypeDocument File.cs\",\"ActiveDocument\":\"VisualStudio.DocumentName:\\\\Path\\\\To\\\\File.cs\"}";
	}

	private static String GetExampleJson_Old() {
		// should reflect current serialization format!
		return "{\"$type\":\"KaVE.Commons.Model.Events.CompletionEvents.CompletionEvent, KaVE.Commons\",\"Context2\":{\"$type\":\"KaVE.Commons.Model.Events.CompletionEvents.Context, KaVE.Commons\",\"TypeShape\":{\"$type\":\"KaVE.Commons.Model.TypeShapes.TypeShape, KaVE.Commons\",\"TypeHierarchy\":{\"$type\":\"KaVE.Commons.Model.TypeShapes.TypeHierarchy, KaVE.Commons\",\"Element\":\"CSharp.UnknownTypeName:?\",\"Implements\":[]},\"MethodHierarchies\":[]},\"SST\":{\"$type\":\"[SST:SST]\",\"EnclosingType\":\"CSharp.TypeName:T,P\",\"PartialClassIdentifier\":\"\",\"Fields\":[],\"Properties\":[],\"Methods\":[],\"Events\":[],\"Delegates\":[]}},\"ProposalCollection\":[{\"$type\":\"KaVE.Commons.Model.Events.CompletionEvents.Proposal, KaVE.Commons\",\"Name\":\"CSharp.MethodName:[T1,P1] [T1,P2].M1()\",\"Relevance\":42},{\"$type\":\"KaVE.Commons.Model.Events.CompletionEvents.Proposal, KaVE.Commons\",\"Name\":\"CSharp.MethodName:[T1,P1] [T1,P2].M2()\",\"Relevance\":42}],\"Selections\":[{\"$type\":\"KaVE.Commons.Model.Events.CompletionEvents.ProposalSelection, KaVE.Commons\",\"Proposal\":{\"$type\":\"KaVE.Commons.Model.Events.CompletionEvents.Proposal, KaVE.Commons\",\"Name\":\"CSharp.MethodName:[T1,P1] [T1,P2].M1()\",\"Relevance\":42},\"SelectedAfter\":\"18:54:59.6720000\",\"Index\":-1},{\"$type\":\"KaVE.Commons.Model.Events.CompletionEvents.ProposalSelection, KaVE.Commons\",\"Proposal\":{\"$type\":\"KaVE.Commons.Model.Events.CompletionEvents.Proposal, KaVE.Commons\",\"Name\":\"CSharp.MethodName:[T1,P1] [T1,P2].M2()\",\"Relevance\":42},\"SelectedAfter\":\"18:54:59.7830000\",\"Index\":-1},{\"$type\":\"KaVE.Commons.Model.Events.CompletionEvents.ProposalSelection, KaVE.Commons\",\"Proposal\":{\"$type\":\"KaVE.Commons.Model.Events.CompletionEvents.Proposal, KaVE.Commons\",\"Name\":\"CSharp.MethodName:[T1,P1] [T1,P2].M1()\",\"Relevance\":42},\"SelectedAfter\":\"18:54:59.8940000\",\"Index\":-1}],\"TerminatedBy\":3,\"TerminatedState\":0,\"ProposalCount\":0,\"IDESessionUUID\":\"0xDEADBEEF\",\"KaVEVersion\":\"1.0\",\"TriggeredAt\":\"2012-02-23T18:54:59.549\",\"TriggeredBy\":0,\"Duration\":\"00:00:02\",\"ActiveWindow\":\"VisualStudio.WindowName:vsWindowTypeDocument File.cs\",\"ActiveDocument\":\"VisualStudio.DocumentName:\\\\Path\\\\To\\\\File.cs\"}";
	}
}