package com.google.gson.jref;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.gson.Gson;

public class JRefNestedTypeTest extends JRefAbstractTest {

	public record TreeNode(TreeNode parent, String name, String data) {
	}

	private static final String ADDRESS = "Four score and seven years ago our fathers brought forth on this continent, a new nation, conceived in Liberty, and dedicated to the proposition that all men are created equal.\r\n"
			+ "\r\n"
			+ "Now we are engaged in a great civil war, testing whether that nation, or any nation so conceived and so dedicated, can long endure. We are met on a great battle-field of that war. We have come to dedicate a portion of that field, as a final resting place for those who here gave their lives that that nation might live. It is altogether fitting and proper that we should do this.\r\n"
			+ "\r\n"
			+ "But, in a larger sense, we can not dedicate -- we can not consecrate -- we can not hallow -- this ground. The brave men, living and dead, who struggled here, have consecrated it, far above our poor power to add or detract. The world will little note, nor long remember what we say here, but it can never forget what they did here. It is for us the living, rather, to be dedicated here to the unfinished work which they who fought here have thus far so nobly advanced. It is rather for us to be here dedicated to the great task remaining before us -- that from these honored dead we take increased devotion to that cause for which they gave the last full measure of devotion -- that we here highly resolve that these dead shall not have died in vain -- that this nation, under God, shall have a new birth of freedom -- and that government of the people, by the people, for the people, shall not perish from the earth.";

	TreeNode[] buildTwoLevelTopArray() {
		TreeNode topNode = new TreeNode(null, "top", ADDRESS);
		// Create three children
		TreeNode firstChild = new TreeNode(topNode, "child1", "data1");
		TreeNode secondChild = new TreeNode(topNode, "child2", "data2");
		TreeNode thirdChild = new TreeNode(topNode, "child3", "data3");
		// Put top and all nodes in array
		return new TreeNode[] { topNode, firstChild, secondChild, thirdChild };
	}

	TreeNode[] buildTwoLevelNoTopArray() {
		TreeNode topNode = new TreeNode(null, "top", ADDRESS);
		// Create three children
		TreeNode firstChild = new TreeNode(topNode, "child1", "data1");
		TreeNode secondChild = new TreeNode(topNode, "child2", "data2");
		TreeNode thirdChild = new TreeNode(topNode, "child3", "data3");
		// Put child nodes in array
		return new TreeNode[] { firstChild, secondChild, thirdChild };
	}

	static String JREF_JSON = "[ {\r\n" + "  \"parent\" : null,\r\n" + "  \"name\" : \"top\",\r\n"
			+ "  \"data\" : \"Four score and seven years ago our fathers brought forth on this continent, a new nation, conceived in Liberty, and dedicated to the proposition that all men are created equal.\\r\\n\\r\\nNow we are engaged in a great civil war, testing whether that nation, or any nation so conceived and so dedicated, can long endure. We are met on a great battle-field of that war. We have come to dedicate a portion of that field, as a final resting place for those who here gave their lives that that nation might live. It is altogether fitting and proper that we should do this.\\r\\n\\r\\nBut, in a larger sense, we can not dedicate -- we can not consecrate -- we can not hallow -- this ground. The brave men, living and dead, who struggled here, have consecrated it, far above our poor power to add or detract. The world will little note, nor long remember what we say here, but it can never forget what they did here. It is for us the living, rather, to be dedicated here to the unfinished work which they who fought here have thus far so nobly advanced. It is rather for us to be here dedicated to the great task remaining before us -- that from these honored dead we take increased devotion to that cause for which they gave the last full measure of devotion -- that we here highly resolve that these dead shall not have died in vain -- that this nation, under God, shall have a new birth of freedom -- and that government of the people, by the people, for the people, shall not perish from the earth.\"\r\n"
			+ "}, {\r\n" + "  \"parent\" : {\r\n" + "    \"$ref\" : \"#/0\"\r\n" + "  },\r\n"
			+ "  \"name\" : \"child1\",\r\n" + "  \"data\" : \"data1\"\r\n" + "}, {\r\n" + "  \"parent\" : {\r\n"
			+ "    \"$ref\" : \"#/0\"\r\n" + "  },\r\n" + "  \"name\" : \"child2\",\r\n" + "  \"data\" : \"data2\"\r\n"
			+ "}, {\r\n" + "  \"parent\" : {\r\n" + "    \"$ref\" : \"#/0\"\r\n" + "  },\r\n"
			+ "  \"name\" : \"child3\",\r\n" + "  \"data\" : \"data3\"\r\n" + "} ]";

	@Test
	public void testDeerializeTwoLevelTreeThreeChildrenJRef() {
		Gson mapper = buildGsonJRef();
		TreeNode[] nodes = mapper.fromJson(JREF_JSON, TreeNode[].class);
		assertEquals(nodes[0], nodes[1].parent);
		assertEquals(nodes[0], nodes[2].parent);
		assertEquals(nodes[0], nodes[3].parent);
	}

	@Test
	public void testTwoLevelTreeNoJRefNoTop() {
		TreeNode[] nodes = buildTwoLevelNoTopArray();
		Gson mapper = buildGsonNoJRef();
		String json = mapper.toJson(nodes);
		// two jrefs
		assertJRefCount(json, 0);
		trace("testTwoLevelTreeNoJRefNoTop json=", json);
		TreeNode[] out = mapper.fromJson(json, TreeNode[].class);
		assertTrue(out.length == 3);
		assertEquals(out[0].parent, out[1].parent);
		assertEquals(out[0].parent, out[2].parent);
	}

	@Test
	public void testTwoLevelTreeNoJRefWithTop() {
		TreeNode[] nodes = buildTwoLevelTopArray();
		// Object mapper without JRefModule
		Gson mapper = buildGsonNoJRef();
		String json = mapper.toJson(nodes);
		// No jrefs
		assertJRefCount(json, 0);
		trace("testTwoLevelTreeNoJRef json=", json);
		TreeNode[] out = mapper.fromJson(json, TreeNode[].class);
		assertEquals(out.length, 4);
		assertEquals(out[0], out[1].parent);
		assertEquals(out[0], out[2].parent);
		assertEquals(out[0], out[3].parent);
	}

	@Test
	public void testTwoLevelTreeJRefNoTop() {
		TreeNode[] nodes = buildTwoLevelNoTopArray();
		Gson mapper = buildGsonJRef();
		String json = mapper.toJson(nodes);
		// two jrefs
		assertJRefCount(json, 2);
		trace("testTwoLevelTreeNoJRefNoTop json=", json);
		TreeNode[] out = mapper.fromJson(json, TreeNode[].class);
		assertTrue(out.length == 3);
		assertEquals(out[0].parent, out[1].parent);
		assertEquals(out[0].parent, out[2].parent);
	}

	@Test
	public void testTwoLevelTreeJRefWithTop() {
		TreeNode[] nodes = buildTwoLevelTopArray();
		// Object mapper without JRefModule
		Gson mapper = buildGsonJRef();
		String json = mapper.toJson(nodes);
		// No jrefs
		assertJRefCount(json, 3);
		trace("testTwoLevelTreeNoJRef json=", json);
		TreeNode[] out = mapper.fromJson(json, TreeNode[].class);
		assertEquals(out.length, 4);
		assertEquals(out[0], out[1].parent);
		assertEquals(out[0], out[2].parent);
		assertEquals(out[0], out[3].parent);
	}

	TreeNode[] buildThreeLevelArray() {
		TreeNode topNode = new TreeNode(null, "top", ADDRESS);
		// Create three children
		TreeNode firstChild = new TreeNode(topNode, "child1", "data1");
		TreeNode firstGChild = new TreeNode(firstChild, "gchild1", "data1g");
		TreeNode secondChild = new TreeNode(topNode, "child2", "data2");
		TreeNode secondGChild = new TreeNode(secondChild, "gchild2", "data2g");
		TreeNode thirdChild = new TreeNode(topNode, "child3", "data3");
		TreeNode thirdGChild = new TreeNode(thirdChild, "child", "data3g");

		// Put grandchildren nodes in array
		return new TreeNode[] { firstGChild, secondGChild, thirdGChild };
	}

	@Test
	public void testThreeLevelTree() {
		TreeNode[] nodes = buildThreeLevelArray();
		Gson mapper = buildGsonJRef();
		String json = mapper.toJson(nodes);
		// two jrefs
		assertJRefCount(json, 2);
		trace("testThreeLevelTree json=", json);
		TreeNode[] out = mapper.fromJson(json, TreeNode[].class);
		assertTrue(out.length == 3);
		assertEquals(out[0].parent.parent, out[1].parent.parent);
		assertEquals(out[0].parent.parent, out[2].parent.parent);
	}

	List<TreeNode> buildThreeLevelArrayAsList() {
		TreeNode topNode = new TreeNode(null, "top", ADDRESS);
		// Create three children
		TreeNode firstChild = new TreeNode(topNode, "child1", "data1");
		TreeNode firstGChild = new TreeNode(firstChild, "gchild1", "data1g");
		TreeNode secondChild = new TreeNode(topNode, "child2", "data2");
		TreeNode secondGChild = new TreeNode(secondChild, "gchild2", "data2g");
		TreeNode thirdChild = new TreeNode(topNode, "child3", "data3");
		TreeNode thirdGChild = new TreeNode(thirdChild, "child", "data3g");

		// Put grandchildren nodes in List
		return List.of(firstGChild, secondGChild, thirdGChild);
	}

	public record TreeNodeList(List<TreeNode> nodes) {
	}

	@Test
	public void testThreeLevelTreeNodeListRecord() {
		List<TreeNode> nodes = buildThreeLevelArrayAsList();
		TreeNodeList tnl = new TreeNodeList(nodes);
		Gson mapper = buildGsonJRef();
		String json = mapper.toJson(tnl);
		// two jrefs
		assertJRefCount(json, 2);
		trace("testThreeLevelTreeNodeListRecord json=", json);
		TreeNodeList out = mapper.fromJson(json, TreeNodeList.class);
		assertTrue(out.nodes().size() == 3);
		assertEquals(out.nodes().get(0).parent.parent, out.nodes().get(1).parent.parent);
		assertEquals(out.nodes().get(0).parent.parent, out.nodes().get(2).parent.parent);
	}

	static class TreeNodeListClass {

		public List<TreeNode> nodes;

	}

	@Test
	public void testThreeLevelTreeNodeListClass() {
		TreeNodeListClass tnlc = new TreeNodeListClass();
		tnlc.nodes = buildThreeLevelArrayAsList();
		Gson mapper = buildGsonJRef();
		String json = mapper.toJson(tnlc);
		// two jrefs
		assertJRefCount(json, 2);
		trace("testThreeLevelTreeNodeListClass json=", json);
		TreeNodeListClass out = mapper.fromJson(json, TreeNodeListClass.class);
		assertTrue(out.nodes.size() == 3);
		assertEquals(out.nodes.get(0).parent.parent, out.nodes.get(1).parent.parent);
		assertEquals(out.nodes.get(0).parent.parent, out.nodes.get(2).parent.parent);
	}

	Map<String, TreeNode> buildThreeLevelArrayAsMap() {
		TreeNode topNode = new TreeNode(null, "top", ADDRESS);
		// Create three children
		TreeNode firstChild = new TreeNode(topNode, "child1", "data1");
		TreeNode firstGChild = new TreeNode(firstChild, "gchild1", "data1g");
		TreeNode secondChild = new TreeNode(topNode, "child2", "data2");
		TreeNode secondGChild = new TreeNode(secondChild, "gchild2", "data2g");
		TreeNode thirdChild = new TreeNode(topNode, "child3", "data3");
		TreeNode thirdGChild = new TreeNode(thirdChild, "child", "data3g");

		// Put grandchildren nodes in Map
		return Map.of(firstGChild.name, firstGChild, secondGChild.name, secondGChild, thirdGChild.name, thirdGChild);
	}

	public record TreeNodeMapRecord(Map<String, TreeNode> nodes) {
	}

	@Test
	public void testThreeLevelTreeNodeMapRecord() {
		TreeNodeMapRecord tnmr = new TreeNodeMapRecord(buildThreeLevelArrayAsMap());
		Gson mapper = buildGsonJRef();
		String json = mapper.toJson(tnmr);
		// two jrefs
		assertJRefCount(json, 2);
		trace("testThreeLevelTreeNodeMapRecord json=", json);
		TreeNodeMapRecord out = mapper.fromJson(json, TreeNodeMapRecord.class);
		assertTrue(out.nodes.size() == 3);
		out.nodes().forEach((k, v) -> {
			assertEquals(k, v.name);
		});
		TreeNode first = null;
		for (Map.Entry<String, TreeNode> entry : out.nodes().entrySet()) {
			if (first == null) {
				first = entry.getValue();
			} else {
				assertEquals(first.parent.parent, entry.getValue().parent.parent);
			}
		}
	}

}
