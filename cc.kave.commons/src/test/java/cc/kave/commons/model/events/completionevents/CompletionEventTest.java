/**
 * Copyright 2016 Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package cc.kave.commons.model.events.completionevents;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import cc.kave.commons.model.names.IName;
import cc.kave.commons.model.names.csharp.Name;

public class CompletionEventTest {

	private CompletionEvent sut;

	@Before
	public void setup() {
		sut = new CompletionEvent();
	}

	@Test
	public void GetLastSelectedProposal() {
		IProposal pX = p(Name.newName("3"));
		IProposal pY = p(Name.newName("4"));

		IProposal p1 = p(Name.newName("1"));
		IProposal p2 = p(Name.newName("2"));
		ProposalSelection s1 = new ProposalSelection();
		s1.Proposal = p1;
		ProposalSelection s2 = new ProposalSelection();
		s2.Proposal = p2;

		CompletionEvent sut = new CompletionEvent();

		// null by default
		assertNull(sut.getLastSelectedProposal());

		sut.proposalCollection.add(pX);
		sut.proposalCollection.add(pY);

		// sometime there is no selection, when the first proposal is directly
		// applied
		assertEquals(pX, sut.getLastSelectedProposal());

		sut.selections.add(s1);
		assertEquals(p1, sut.getLastSelectedProposal());

		sut.selections.add(s2);
		assertEquals(p2, sut.getLastSelectedProposal());
	}

	private static IProposal p(IName n) {
		Proposal p = new Proposal();
		p.Name = n;
		return p;
	}
}