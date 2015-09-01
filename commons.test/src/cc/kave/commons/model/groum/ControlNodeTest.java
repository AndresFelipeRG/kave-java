package cc.kave.commons.model.groum;

import static org.junit.Assert.*;

import org.junit.Test;

import cc.kave.commons.model.groum.nodes.ControlNode;

public class ControlNodeTest extends NodeContractTest {

	@Override
	protected Node createNode(String id) {
		return new ControlNode(id);
	}
	
	@Test
	public void serializes() {
		Node first = createNode("WHILE");

		assertEquals("WHILE", first.toString());
	}
}
