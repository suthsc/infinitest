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
package org.infinitest.changedetect;

import static java.lang.Character.*;

import java.io.*;
import java.util.*;

import org.infinitest.*;
import org.infinitest.parser.*;

public enum FileChangeDetector {
	INSTANCE;

	private final static ClassFileFilter IS_CLASS_FILE = new ClassFileFilter();

	private final Map<File, Long> timestampIndex;
	private final Map<File, Set<File>> changedSinceLastRefresh;

	private FileChangeDetector() {
		this.timestampIndex = new HashMap<File, Long>();
		this.changedSinceLastRefresh = new HashMap<File, Set<File>>();
	}

	public synchronized Set<File> findChangedFiles(List<File> roots) {
		Set<File> changedFiles = new HashSet<File>();

		for (File root : roots) {
			Set<File> changes = changedSinceLastRefresh.get(root);
			if (changes != null) {
				changedFiles.addAll(changes);
			}
		}

		return changedFiles;
	}

	public synchronized boolean refresh(Set<File> allRoots) {
		changedSinceLastRefresh.clear();

		Set<File> allChangedFiles = new HashSet<File>();

		for (File root : allRoots) {
			Set<File> changedFiles = new HashSet<File>();

			if (root.isDirectory()) {
				File[] children = root.listFiles(IS_CLASS_FILE);
				if (children != null) {
					addFiles(children, changedFiles);
				}
			} else if (ClassFileFilter.isClassFile(root)) {
				Long currentTimestamp = getModificationTimestamp(root);
				Long oldTimestamp = timestampIndex.put(root, currentTimestamp);

				if (!currentTimestamp.equals(oldTimestamp)) {
					changedFiles.add(root);
				}
			}

			changedSinceLastRefresh.put(root, changedFiles);
			allChangedFiles.addAll(changedFiles);
		}

		Iterator<File> iterator = timestampIndex.keySet().iterator();
		while (iterator.hasNext()) {
			File removed = iterator.next();
			if (!removed.exists()) {
				iterator.remove();
				allChangedFiles.add(removed);
			}
		}

		Log.log("TOTAL CHANGES " + allChangedFiles.size());

		ClassFileIndex.INSTANCE.parseAndIndex(allChangedFiles);

		return !allChangedFiles.isEmpty();
	}

	private void addFiles(File[] classesOrDirectories, Set<File> collector) {
		for (File file : classesOrDirectories) {
			if (file.isDirectory()) {
				if (isJavaIdentifierStart(file.getName().charAt(0))) {
					File[] children = file.listFiles(IS_CLASS_FILE);
					if (children != null) {
						addFiles(children, collector);
					}
				}
			} else if (ClassFileFilter.isClassFile(file)) {
				Long currentTimestamp = getModificationTimestamp(file);
				Long oldTimestamp = timestampIndex.put(file, currentTimestamp);

				if (!currentTimestamp.equals(oldTimestamp)) {
					collector.add(file);
				}
			}
		}
	}

	protected long getModificationTimestamp(File classFile) {
		return classFile.lastModified();
	}
}
