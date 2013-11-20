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

import java.util.*;

import com.google.common.collect.*;

public class MyGraph {
	private final Map<String, Vertex> verticesByName;

	private static class Vertex {
		final JavaClass javaClass;
		final Set<Vertex> parents;
		final Set<Vertex> children;

		public Vertex(JavaClass javaClass) {
			this.javaClass = javaClass;
			this.parents = new HashSet<MyGraph.Vertex>();
			this.children = new HashSet<MyGraph.Vertex>();
		}
	}

	public MyGraph() {
		verticesByName = new HashMap<String, Vertex>();
		clear();
	}

	public int size() {
		return verticesByName.size();
	}

	public void clear() {
		verticesByName.clear();
	}

	public Set<JavaClass> javaClasses() {
		Set<JavaClass> javaClasses = new HashSet<JavaClass>();
		for (Vertex vertex : verticesByName.values()) {
			javaClasses.add(vertex.javaClass);
		}
		return javaClasses;
	}

	public boolean addOrResetVertex(JavaClass javaClass) {
		String className = javaClass.getName();
		Vertex existing = verticesByName.get(className);
		if (existing == null) {
			// Add
			verticesByName.put(className, new Vertex(javaClass));
			return true;
		} else {
			if (existing.javaClass == javaClass) {
				// Unchanged
				return false;
			}
			// Reset
			for (Vertex child : existing.children) {
				child.parents.remove(existing);
			}
			existing.children.clear();
			return true;
		}
	}

	public JavaClass findVertexByName(String name) {
		Vertex vertex = verticesByName.get(name);
		if (vertex == null) {
			return null;
		}
		return vertex.javaClass;
	}

	public boolean containsVertex(JavaClass javaClass) {
		return verticesByName.containsKey(javaClass.getName());
	}

	public void addEdge(JavaClass from, JavaClass to) {
		Vertex fromVertex = verticesByName.get(from.getName());
		Vertex toVertex = verticesByName.get(to.getName());
		if ((fromVertex != null) && (toVertex != null)) {
			if (fromVertex != toVertex) {
				toVertex.parents.add(fromVertex);
				fromVertex.children.add(toVertex);
			}
		}
	}

	public Set<JavaClass> findParents(Set<JavaClass> classes) {
		Set<JavaClass> changedParents = Sets.newHashSet();
		for (JavaClass jclass : classes) {
			Vertex vertex = verticesByName.get(jclass.getName());
			if (vertex != null) {
				findParents(vertex, changedParents);
			}
		}
		return changedParents;
	}

	private void findParents(Vertex vertex, Set<JavaClass> changedParents) {
		if (changedParents.add(vertex.javaClass)) {
			for (Vertex parent : vertex.parents) {
				findParents(parent, changedParents);
			}
		}
	}
}
