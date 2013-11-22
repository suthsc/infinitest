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

import javassist.*;

import org.infinitest.*;

import com.google.common.io.*;

public enum ClassFileIndex {
	INSTANCE;

	private ClassPool classPool;
	private MyGraph graph;
	private Map<File, JavaClass> classes;

	private ClassFileIndex() {
		clear();
	}

	public void clear() {
		classPool = new ClassPool();
		graph = new MyGraph();
		classes = new HashMap<File, JavaClass>();
	}

	public void parseAndIndex(Collection<File> changedFiles) {
		Set<JavaClass> changedClasses = new HashSet<JavaClass>();
		for (File changedFile : changedFiles) {
			Log.log("FileChanged : " + changedFile);

			if (!changedFile.exists()) {
				JavaClass old = classes.remove(changedFile);
				if (old != null) {
					graph.remove(old);
				}
			} else {
				FileInputStream inputStream = null;
				try {
					inputStream = new FileInputStream(changedFile);

					CtClass ctClass = classPool.makeClass(inputStream);
					String className = ctClass.getName();

					if (!unparsable(ctClass)) {
						JavaAssistClass clazz = new JavaAssistClass(ctClass, changedFile);
						changedClasses.add(clazz);
						classes.put(changedFile, clazz);
					}
				} catch (IOException e) {
					Log.log("ERROR " + e);
					// Ignore
				} finally {
					if (inputStream != null) {
						Closeables.closeQuietly(inputStream);
					}
				}
			}
		}

		Log.log("TOTAL CHANGES " + changedClasses.size());

		Set<JavaClass> toLink = new HashSet<JavaClass>();
		for (JavaClass changedClass : changedClasses) {
			if (graph.addOrResetVertex(changedClass)) {
				toLink.add(changedClass);
			}
		}

		for (JavaClass changedClass : toLink) {
			String[] imports = changedClass.getImports();
			for (String className : imports) {
				JavaClass childClass = graph.findVertexByName(className);
				if (childClass != null) {
					graph.addEdge(changedClass, childClass);
				}
			}
		}
	}

	public Set<JavaClass> findClasses(Collection<File> changedFiles) {
		Set<JavaClass> changedClasses = new HashSet<JavaClass>();
		for (File changedFile : changedFiles) {
			JavaClass javaClass = classes.get(changedFile);
			if (javaClass != null) {
				changedClasses.add(javaClass);
			}
		}

		return changedClasses;
	}

	public Set<JavaClass> findChangedParents(Set<JavaClass> classes) {
		return graph.findParents(classes);
	}

	public int size() {
		return graph.size();
	}

	public Set<JavaClass> getIndexedClasses() {
		return graph.javaClasses();
	}

	private static boolean unparsable(CtClass cachedClass) {
		return cachedClass.getClassFile2() == null;
	}
}
