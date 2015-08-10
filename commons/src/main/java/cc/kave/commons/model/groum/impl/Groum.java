package cc.kave.commons.model.groum.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import cc.kave.commons.model.groum.IGroum;
import cc.kave.commons.model.groum.INode;
import cc.kave.commons.model.groum.ISubGroum;
import cc.kave.commons.model.groum.ISubgraphStrategy;

public class Groum implements IGroum {
	DirectedGraph<INode, DefaultEdge> groum;
	INode root;
	ISubgraphStrategy subgraphStrategy;

	public Groum() {
		groum = new DefaultDirectedGraph<INode, DefaultEdge>(DefaultEdge.class);
	}

	/*
	 * TODO: Implement dynamic solution (non-Javadoc) e.g. only Node without
	 * incoming Edges
	 * 
	 * @see cc.kave.commons.model.groum.IGroum#getRoot()
	 */
	public INode getRoot() {
		return root;
	}

	public void setSubgraphStrategy(ISubgraphStrategy strategy) {
		this.subgraphStrategy = strategy;
	}

	// @Override
	// public boolean equals(Object anotherGroum) {
	// if (!(anotherGroum instanceof Groum))
	// return false;
	// else
	// return (toString().equals(anotherGroum.toString()));
	// }

	@Override
	public boolean equals(Object anotherGroum) {
		if (!(anotherGroum instanceof Groum))
			return false;
		else {
			Groum groum = (Groum) anotherGroum;

			if (getAllNodes().size() != groum.getAllNodes().size())
				return false;

			List<INode> myNodes = new LinkedList<>();
			myNodes.addAll(getAllNodes());
			List<INode> otherNodes = new LinkedList<>();
			otherNodes.addAll(groum.getAllNodes());

			Collections.sort(myNodes);
			Collections.sort(otherNodes);

			for (int i = 0; i < myNodes.size(); i++) {
				if (!(myNodes.get(i).equals(otherNodes.get(i))))
					return false;
			}
			return true;
		}
	}

	public Set<DefaultEdge> getAllEdges() {
		return groum.edgeSet();
	}

	@Override
	public INode getNode(INode node) {
		if (groum.containsVertex(node)) {
			for (INode aNode : groum.vertexSet()) {
				if (aNode == node)
					return node;
			}
		} else {
			return null;
		}
		return null;
	}

	@Override
	public boolean containsEqualNode(INode node) {
		for (INode aNode : groum.vertexSet()) {
			if (node.equals(aNode))
				return true;
		}
		return false;
	}

	@Override
	public boolean containsNode(INode node) {
		return groum.containsVertex(node);
	}

	@Override
	public boolean containsEdge(INode source, INode target) {
		return groum.containsEdge(source, target);
	}

	@Override
	public void addEdge(INode source, INode target) {
		groum.addEdge(source, target);

	}

	@Override
	public void addVertex(INode node) {
		groum.addVertex(node);
		if (root == null)
			root = node;
	}

	@Override
	public Set<INode> getSuccessors(INode node) {
		Set<INode> successors = new HashSet<>();
		Set<DefaultEdge> outgoingEdges = groum.outgoingEdgesOf(node);

		for (DefaultEdge edge : outgoingEdges) {
			successors.add(groum.getEdgeTarget(edge));
		}
		return successors;
	}

	@Override
	public String toString() {
		return groum.toString();
	}

	@Override
	public int getVertexCount() {
		return groum.vertexSet().size();
	}

	@Override
	public int getEdgeCount() {
		return groum.edgeSet().size();
	}

	@Override
	public Set<INode> getAllNodes() {
		return groum.vertexSet();
	}

	@Override
	public Set<INode> getEqualNodes(INode reference) {

		Set<INode> equalNodes = new HashSet<>();
		for (INode node : groum.vertexSet()) {
			if (node.equals(reference))
				equalNodes.add(node);
		}
		if (equalNodes.size() == 0)
			return null;
		return equalNodes;
	}

	@Override
	public List<ISubGroum> getSubgraphs(IGroum subgraph) {
		if (subgraphStrategy == null) {
			throw new UnsupportedOperationException("Subgraphstrategy has not been set");
		}

		return subgraphStrategy.getIsomorphSubgraphs(this, subgraph);
	}

	@Override
	public int compareTo(IGroum o) {
		if (o == null)
			return 1;
		else if (this.equals(o))
			return 0;
		else
			return toString().compareTo(o.toString());
	}

	@Override
	public INode getLeaf() {
		for (INode node : getAllNodes()) {
			if (groum.outDegreeOf(node) == 0)
				return node;
		}
		return null;
	}
}
