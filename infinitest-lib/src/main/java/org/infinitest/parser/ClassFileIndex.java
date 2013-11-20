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

import java.io.*;
import java.util.*;

import org.infinitest.*;

import com.google.common.annotations.*;

public class ClassFileIndex {
	private final JavaClassBuilder classBuilder;
	private final MyGraph graph;

	public ClassFileIndex(ClasspathProvider classpath) {
		this(new JavaClassBuilder(classpath));
	}

	@VisibleForTesting
	ClassFileIndex(JavaClassBuilder classBuilder) {
		this.classBuilder = classBuilder;
		this.graph = new MyGraph();
	}

	public Set<JavaClass> findClasses(Collection<File> changedFiles) {
		// First update class index
		List<String> changedClassesNames = new ArrayList<String>();
		for (File changedFile : changedFiles) {
			changedClassesNames.add(classBuilder.classFileChanged(changedFile));
		}

		// Create JavaClasses
		Set<JavaClass> changedClasses = new HashSet<JavaClass>();
		for (String changedClassesName : changedClassesNames) {
			JavaClass javaClass = classBuilder.getClass(changedClassesName);
			if (!(javaClass instanceof UnparsableClass)) {
				changedClasses.add(javaClass);
			}
		}

		// Add to Index
		for (JavaClass changedClass : changedClasses) {
			addToIndex(changedClass);
		}

		return changedClasses;
	}

	public JavaClass findOrCreateJavaClass(String classname) {
		// Index by name
		JavaClass jClass = graph.findVertexByName(classname);
		if (jClass != null) {
			return jClass;
		}

		JavaClass clazz = classBuilder.getClass(classname);
		if (clazz.locatedInClassFile()) {
			addToIndex(clazz);
		}
		return clazz;
	}

	private void addToIndex(JavaClass newClass) {
		if (graph.addOrResetVertex(newClass)) {
			Log.log("ADD EDGES : " + newClass);

			for (String classname : newClass.getImports()) {
				JavaClass childClass = findOrCreateJavaClass(classname);
				graph.addEdge(newClass, childClass);
			}
		}
	}

	// Loop through all changed classes, adding their parents (and their
	// parents)
	// to another set of changed classes
	public Set<JavaClass> findChangedParents(Set<JavaClass> classes) {
		return graph.findParents(classes);
	}

	public int size() {
		return graph.size();
	}

	public void clear() {
		classBuilder.clear();
		graph.clear();
	}

	public Set<JavaClass> getIndexedClasses() {
		return graph.javaClasses();
	}
}
