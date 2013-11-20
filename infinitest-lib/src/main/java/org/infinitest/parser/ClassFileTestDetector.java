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

import static com.google.common.collect.Sets.*;
import static org.infinitest.util.InfinitestUtils.*;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import org.infinitest.*;
import org.infinitest.filter.*;

/**
 * @author <a href="mailto:benrady@gmail.com"Ben Rady</a>
 */
public class ClassFileTestDetector implements TestDetector {
	private final TestFilter filters;
	private ClassFileIndex index;
	private ClasspathProvider classpath;

	public ClassFileTestDetector(TestFilter testFilterList) {
		filters = testFilterList;
	}

	@Override
	public void clear() {
		index.clear();
	}

	/**
	 * Runs through the classpath looking for changed files and returns the set
	 * of tests that need to be run.
	 */
	@Override
	public synchronized Set<JavaClass> findTestsToRun(Collection<File> changedFiles) {
		filters.updateFilterList();
		if (filters.acceptsNone()) {
			return new HashSet<JavaClass>(); // No need to do anything for
												// projects with
												// catch all filter
		}

		// Find changed classes
		Set<JavaClass> changedClasses = index.findClasses(changedFiles);
		Set<JavaClass> changedParents = index.findChangedParents(changedClasses);

		// combine two sets
		changedClasses.addAll(changedParents);

		// run through total set, and pick out tests to run
		log(Level.FINE, "Total changeset: " + changedParents);
		Log.log("Total changeset: " + changedParents);
		return filterTests(changedClasses);
	}

	private Set<JavaClass> filterTests(Set<JavaClass> changedClasses) {
		Set<JavaClass> testsToRun = new HashSet<JavaClass>();
		for (JavaClass javaClass : changedClasses) {
			if (javaClass.isATest() && !filters.match(javaClass) && inCurrentProject(javaClass)) {
				testsToRun.add(javaClass);
			} else {
				log(Level.FINE, "Filtered test: " + javaClass);
			}
		}
		return testsToRun;
	}

	private boolean inCurrentProject(JavaClass jclass) {
		// I can't find a scenario where a non-classfile could get in here, but
		// I think I'm missing
		// it, so I still want to guard against it
		if (jclass.locatedInClassFile()) {
			File classFile = jclass.getClassFile();
			for (File parentDir : classpath.getClassOutputDirs()) {
				if (classFile.getAbsolutePath().startsWith(parentDir.getAbsolutePath())) {
					return true;
				}
			}
		}
		return false;
	}

	public int size() {
		return index.size();
	}

	public JavaClass findOrCreateJavaClass(String name) {
		return index.findOrCreateJavaClass(name);
	}

	@Override
	public void setClasspathProvider(ClasspathProvider classpath) {
		this.classpath = classpath;
		index = new ClassFileIndex(classpath);
	}

	@Override
	public Set<String> getCurrentTests() {
		Set<String> tests = newHashSet();
		for (JavaClass javaClass : index.getIndexedClasses()) {
			if (javaClass.isATest() && !filters.match(javaClass) && inCurrentProject(javaClass)) {
				tests.add(javaClass.getName());
			}
		}
		return tests;
	}
}
