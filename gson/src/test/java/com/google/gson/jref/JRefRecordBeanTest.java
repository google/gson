package com.google.gson.jref;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.gson.Gson;

public class JRefRecordBeanTest extends JRefAbstractTest {

	static record IntBean(Integer j) {
	}

	@Test
	public void testIntBeanArray() throws Exception {
		Gson mapper = buildGsonJRef();
		IntBean i1 = new IntBean(10);
		IntBean i2 = new IntBean(20);
		IntBean[] beans = new IntBean[] { i1, i2, i1, i2 };
		String out = mapper.toJson(beans);
		trace("testIntBeanArray", out);
		IntBean[] result = mapper.fromJson(out, IntBean[].class);
		assertEquals(result[0], result[2]);
		assertEquals(result[1], result[3]);
	}

	/*
	 * @Test public void testIntBeanList() throws Exception { Gson mapper =
	 * buildGsonJRef(); IntBean i1 = new IntBean(10); IntBean i2 = new IntBean(20);
	 * List<IntBean> beans = List.of(i1, i2, i1, i2); String out =
	 * mapper.toJson(beans); trace("testIntBeanList", out);
	 * 
	 * @SuppressWarnings("unchecked") List<IntBean> result = (List<IntBean>)
	 * mapper.fromJson(out, List.class); assertEquals(result.get(0), result.get(2));
	 * assertEquals(result.get(1), result.get(3)); }
	 */
	record StringKeyBeanMap(Map<String, IntBean> items) {
	}

	@Test
	public void testStringKeyMap() throws Exception {
		Gson mapper = buildGsonJRef();
		IntBean i1 = new IntBean(10);
		IntBean i2 = new IntBean(20);
		StringKeyBeanMap beanMap = new StringKeyBeanMap(Map.of("one", i1, "two", i2, "three", i1, "four", i2));
		String out = mapper.toJson(beanMap);
		trace("testIntBeanStringKeyMap", out);
		StringKeyBeanMap result = mapper.fromJson(out, StringKeyBeanMap.class);
		assertEquals(result.items().get("one"), result.items().get("three"));
		assertEquals(result.items().get("two"), result.items().get("four"));
	}

	record Node(Node parent, String name) {
	}

	record NodeList(List<Node> nodes) {

	}

	@Test
	public void testNodeTree() throws Exception {
		Gson mapper = buildGsonJRef();
		Node root = new Node(null, "root");
		String[] nodeNames = new String[] { "child1", "child2", "child3" };
		List<Node> nodeList = new ArrayList<>();
		for (int i = 0; i < nodeNames.length; i++) {
			nodeList.add(new Node(root, nodeNames[i]));
		}
		// Add references to previously added nodes in reverse order
		nodeList.add(nodeList.get(2));
		nodeList.add(nodeList.get(1));
		nodeList.add(nodeList.get(0));
		String out = mapper.toJson(new NodeList(nodeList));
		trace("testNodeTree", out);
		NodeList result = mapper.fromJson(out, NodeList.class);
		List<Node> nodes = result.nodes();
		// assert nodes list same as input
		assertEquals(nodeList.size(), nodes.size());
		for (int firstIndex = 0; firstIndex < nodeList.size() / 2; firstIndex++) {
			int secondIndex = (nodeList.size() - 1) - firstIndex;
			Node n1 = result.nodes().get(firstIndex);
			assertEquals(root.name(), n1.parent().name());
			Node n2 = result.nodes().get(secondIndex);
			assertEquals(root.name(), n2.parent().name());
			assertEquals(n1, n2);

		}
	}

}
