package com.google.gson.jref;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.google.gson.Gson;

public class JRefCircularReferenceTests extends JRefAbstractTest {

	public static class Node {
		public String name;
		public Node child;
		public Node sibling;
		public List<Node> neighbors = new ArrayList<>();

		public Node() {
		}

		public Node(String name) {
			this.name = name;
		}
	}

	public static class Graph {
		public Node root;
		public List<Node> allNodes = new ArrayList<>();
	}

	@Test
	public void testNestedCircularDeserialization() throws Exception {
		Gson mapper = buildGsonJRef();

		// JSON representing a circular structure: root -> child -> child (ref to root)
		String json = "{" + "  \"root\": {" + "    \"name\": \"parent\"," + "    \"child\": {"
				+ "      \"name\": \"child\"," + "      \"child\": { \"$ref\": \"#/root\" }" + "    }" + "  }" + "}";

		try {
			mapper.fromJson(json, Graph.class);
			fail();
		} catch (Exception e) {
			// this should be thrown by deserialization
			// so we pass
		}

	}

	@Test
	public void testSiblingAndNeighborDeserialization() throws Exception {
		Gson mapper = buildGsonJRef();

		// JSON representing nodes where neighbors refer back to previous nodes in a
		// list
		String json = "{" + "  \"allNodes\": [" + "    { \"name\": \"node0\" },"
				+ "    { \"name\": \"node1\", \"sibling\": { \"$ref\": \"#/allNodes/0\" } },"
				+ "    { \"name\": \"node2\", \"neighbors\": [ { \"$ref\": \"#/allNodes/0\" }, { \"$ref\": \"#/allNodes/1\" } ] }"
				+ "  ]" + "}";

		Graph graph = mapper.fromJson(json, Graph.class);

		assertEquals(3, graph.allNodes.size());
		Node n0 = graph.allNodes.get(0);
		Node n1 = graph.allNodes.get(1);
		Node n2 = graph.allNodes.get(2);

		assertEquals("node0", n0.name);
		assertEquals("node1", n1.name);
		assertEquals("node2", n2.name);

		assertSame(n0, n1.sibling);
		assertEquals(2, n2.neighbors.size());
		assertSame(n0, n2.neighbors.get(0));
		assertSame(n1, n2.neighbors.get(1));
	}

	/*
	 * @Test public void testCircularStructureSerialization() throws Exception {
	 * Gson mapper = buildGsonJRef();
	 * 
	 * Node root = new Node("root"); Node child = new Node("child"); root.child =
	 * child; child.child = root; // Circular
	 * 
	 * Graph graph = new Graph(); graph.root = root; graph.allNodes.add(root);
	 * graph.allNodes.add(child);
	 * 
	 * try { mapper.toJson(graph); fail(); } catch (Exception e) { // this should be
	 * thrown by serialization // so we pass } }
	 */
	@Test
	public void testNestedPathDeserialization() throws Exception {
		Gson mapper = buildGsonJRef();

		// Test resolving a path that goes through multiple levels of objects and arrays
		String json = "{" + "  \"root\": {" + "    \"neighbors\": ["
				+ "      { \"name\": \"neighbor0\", \"child\": { \"name\": \"inner\" } }" + "    ]" + "  },"
				+ "  \"allNodes\": [" + "    { \"$ref\": \"#/root/neighbors/name/child/name\" }" + "  ]" + "}";

		try {
			mapper.fromJson(json, Graph.class);
			fail();
		} catch (Exception e) {
			// this should be thrown by deserialization
			// so we pass
			System.out.println(e);
		}
	}
}
