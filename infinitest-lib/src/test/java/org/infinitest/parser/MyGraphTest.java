/*
 * Infinitest, a Continuous Test Runner.
 *
 * Copyright (C) 2010-2013
 * "Ben Rady" <benrady@gmail.com>,
 * "Rod Coffin" <rfciii@gmail.com>,
 * "Ryan Breidenbach" <ryan.breidenbach@gmail.com>
 * "David Gageot" <david@gageot.net>, et al.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.infinitest.parser;

import static com.google.common.collect.ImmutableSet.*;
import static org.assertj.core.api.Assertions.*;

import java.util.*;

import org.junit.*;

public class MyGraphTest {
	MyGraph graph = new MyGraph();

	@Test
	public void find_parent() {
		JavaClass javaClass = javaClass("Hello");
		JavaClass javaTest = javaClass("HelloTest");

		graph.addOrResetVertex(javaClass);
		graph.addOrResetVertex(javaTest);
		graph.addEdge(javaTest, javaClass);

		Set<JavaClass> parents = graph.findParents(of(javaClass));

		assertThat(parents).containsOnly(javaClass, javaTest);
	}

	@Test
	public void find_self() {
		JavaClass javaClass = javaClass("Hello");
		JavaClass javaTest = javaClass("HelloTest");

		graph.addOrResetVertex(javaClass);
		graph.addOrResetVertex(javaTest);
		graph.addEdge(javaTest, javaClass);

		Set<JavaClass> parents = graph.findParents(of(javaTest));

		assertThat(parents).containsOnly(javaTest);
	}

	@Test
	public void find_grand_parent() {
		JavaClass javaHelper = javaClass("HelloHelper");
		JavaClass javaClass = javaClass("Hello");
		JavaClass javaTest = javaClass("HelloTest");

		graph.addOrResetVertex(javaHelper);
		graph.addOrResetVertex(javaClass);
		graph.addOrResetVertex(javaTest);
		graph.addEdge(javaTest, javaClass);
		graph.addEdge(javaClass, javaHelper);

		Set<JavaClass> parents = graph.findParents(of(javaHelper));

		assertThat(parents).containsOnly(javaHelper, javaClass, javaTest);
	}

	private static JavaClass javaClass(String name) {
		return new FakeJavaClass(name);
	}
}
