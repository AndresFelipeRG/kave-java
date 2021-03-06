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
package cc.kave.commons.model.groum;

import java.util.Set;

import org.junit.Test;

import cc.kave.commons.model.groum.Groum;
import cc.kave.commons.model.groum.comparator.GroumComparatorConstractTest;
import static cc.kave.commons.model.groum.GroumTestUtils.*;
import static cc.kave.commons.model.groum.PatternAssert.*;
import static cc.kave.commons.model.groum.GroumBuilder.*;

public class PattExplorerTest {

	@Test
	public void findsSingeNodePatterns() {
		Groum groum = createGroum("A", "B");

		Set<IGroum> patterns = findPatternsWithMinFrequency(1, groum);

		Set<IGroum> patternsOfSize1 = filterBySize(patterns, 1);
		assertContainsPatterns(patternsOfSize1, createGroum("A"), createGroum("B"));
	}

	@Test
	public void filtersPatternsByFrequency() {
		Groum groum1 = createGroum("A", "B");
		Groum groum2 = createGroum("A");

		Set<IGroum> patterns = findPatternsWithMinFrequency(2, groum1,
				groum2);

		assertContainsPatterns(patterns, createGroum("A"));
	}

	@Test
	public void findsTwoNodePattern() {
		Node[] nodes = createNodes("A", "B");
		Groum groum = buildGroum(nodes).withEdge(nodes[0], nodes[1]).build();

		Set<IGroum> patterns = findPatternsWithMinFrequency(1, groum);

		Set<IGroum> patternsOfSize2 = filterBySize(patterns, 2);
		assertContainsPatterns(patternsOfSize2, groum);
	}

	@Test
	public void filtersTwoNodePatternsByFrequency() {
		Node[] nodes1 = createNodes("A", "B");
		Groum groum1 = buildGroum(nodes1).withEdge(nodes1[0], nodes1[1]).build();
		Node[] nodes2 = createNodes("A", "C");
		Groum groum2 = buildGroum(nodes2).withEdge(nodes2[0], nodes2[1]).build();
		Node[] nodes3 = createNodes("A", "B");
		Groum groum3 = buildGroum(nodes3).withEdge(nodes3[0], nodes3[1]).build();

		Set<IGroum> patterns = findPatternsWithMinFrequency(2, groum1,
				groum2, groum3);

		Set<IGroum> patternsOfSize2 = filterBySize(patterns, 2);
		assertContainsPatterns(patternsOfSize2, groum1);
	}

	/**
	 * Since structure is considered, two different patterns of size 3 should
	 * be found in the following Groum:
	 * 
	 * <pre>
	 *     1          1     1
	 *   /   \        |     | \
	 *  2     3  =>   2  ,  2  3
	 *  |             |
	 *  3             3
	 * </pre>
	 */
	@Test
	public void considersEdges() {
		Node[] nodes = createNodes("1", "2", "3", "3");
		Groum groum = buildGroum(nodes)
				.withEdge(nodes[0], nodes[1])
				.withEdge(nodes[0], nodes[2])
				.withEdge(nodes[1], nodes[3]).build();

		Set<IGroum> patterns = findPatternsWithMinFrequency(1, groum);

		Node[] nodes2 = createNodes("1", "2", "3");
		Groum pattern1 = buildGroum(nodes2)
				.withEdge(nodes2[0], nodes2[1])
				.withEdge(nodes2[0], nodes2[2]).build();

		Node[] nodes3 = createNodes("1", "2", "3");
		Groum pattern2 = buildGroum(nodes3)
				.withEdge(nodes3[0], nodes3[1])
				.withEdge(nodes3[1], nodes3[2]).build();

		Set<IGroum> patternsOfSize3 = filterBySize(patterns, 3);
		assertContainsPatterns(patternsOfSize3, pattern1, pattern2);
	}

	@Test
	public void considersEdgeDirection() {
		// A -> B
		Node[] nodes1 = createNodes("A", "B");
		Groum groum1 = buildGroum(nodes1).withEdge(nodes1[0], nodes1[1]).build();

		// B -> A
		Node[] nodes2 = createNodes("A", "B");
		Groum groum2 = buildGroum(nodes2).withEdge(nodes2[1], nodes2[0]).build();

		Set<IGroum> patterns = findPatternsWithMinFrequency(2, groum1,
				groum2);

		Set<IGroum> patternsOfSize2 = filterBySize(patterns, 2);
		assertContainsPatterns(patternsOfSize2);
	}

	@Test
	public void countsOverlappingInstanesOnlyOnce1() {
		// 1 -> 2 -> 2
		Node[] nodes = createNodes("1", "2", "2");
		Groum overlappingGroum = buildGroum(nodes)
				.withEdge(nodes[0], nodes[1])
				.withEdge(nodes[1], nodes[2]).build();

		Set<IGroum> patterns = findPatternsWithMinFrequency(2,
				overlappingGroum);

		Groum pattern1 = createGroum("2");
		assertContainsPatterns(patterns, pattern1);
	}

	@Test
	public void countsOverlappingInstanesOnlyOnce2() {
		// 1 -> 2
		// |
		// 2
		Node[] nodes = createNodes("1", "2", "2");
		Groum overlappingGroum = buildGroum(nodes)
				.withEdge(nodes[0], nodes[1])
				.withEdge(nodes[0], nodes[2]).build();

		Set<IGroum> patterns = findPatternsWithMinFrequency(2,
				overlappingGroum);

		Groum pattern1 = createGroum("2");
		assertContainsPatterns(patterns, pattern1);
	}

	@Test
	public void findsMultipleInstanceInOneGraph() {
		// 1 -> 2
		// |
		// 1 -> 2
		Node[] nodes = createNodes("1", "1", "2", "2");
		Groum groum = buildGroum(nodes)
				.withEdge(nodes[0], nodes[1])
				.withEdge(nodes[0], nodes[2])
				.withEdge(nodes[1], nodes[3]).build();

		Set<IGroum> patterns = findPatternsWithMinFrequency(2, groum);

		Node[] nodes2 = createNodes("1", "2");
		Groum pattern1 = buildGroum(nodes2).withEdge(nodes2[0], nodes2[1]).build();

		patterns = filterBySize(patterns, 2);
		assertContainsPatterns(patterns, pattern1);
	}

	/**
	 * Even though both groum1 and groum2 are equal, according to our comparator
	 * (see {@link GroumComparatorConstractTest#samePathsDifferentStructure()}), we do not
	 * detect them as two occurrences of the same pattern. This is because they
	 * are generated in subsequent iterations of the algorithm (different number
	 * of nodes) and we filter by frequency after every iteration. I believe we
	 * cannot easily detect such things, as it would require us to throw the
	 * frequent-item-set assumption (every part of a frequent thing is itself
	 * frequent) overboard.
	 */
	@Test
	public void findsGraphIsomorphism() {
		// 1 -> 2
		// | /
		// 3
		Node[] nodes1 = createNodes("1", "2", "3");
		Groum groum1 = buildGroum(nodes1)
				.withEdge(nodes1[0], nodes1[1])
				.withEdge(nodes1[0], nodes1[2])
				.withEdge(nodes1[1], nodes1[2]).build();

		// 1 -> 2 -> 3
		// |
		// 3
		Node[] nodes2 = createNodes("1", "2", "3", "3");
		Groum groum2 = buildGroum(nodes2)
				.withEdge(nodes2[0], nodes2[1])
				.withEdge(nodes2[0], nodes2[2])
				.withEdge(nodes2[1], nodes2[3]).build();

		Set<IGroum> patterns = findPatternsWithMinFrequency(2, groum1,
				groum2);

		patterns = filterBySize(patterns, 3);
		assertContainsPatterns(patterns /*, none */);
	}

	@Test
	public void includesAllEdgesBetweenAllIncludedNodes() {
		Node[] nodes = createNodes("1", "2", "3");
		Groum groum = buildGroum(nodes)
				.withEdge(nodes[0], nodes[1])
				.withEdge(nodes[0], nodes[2])
				.withEdge(nodes[1], nodes[2]).build();

		Set<IGroum> patterns = findPatternsWithMinFrequency(1, groum);

		patterns = filterBySize(patterns, 3);
		assertContainsPatterns(patterns, groum);
	}
}
