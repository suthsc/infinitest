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
package org.infinitest.eclipse.workspace;

import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.launching.*;

import com.google.common.cache.*;

public class EclipseFacade {
	private static final LoadingCache<IJavaProject, String[]> CACHE = CacheBuilder.newBuilder().build(new CacheLoader<IJavaProject, String[]>() {
		@Override
		public String[] load(IJavaProject project) throws CoreException {
			String[] computeDefaultRuntimeClassPath = JavaRuntime.computeDefaultRuntimeClassPath(project);
			return computeDefaultRuntimeClassPath;
		}
	});

	public String[] computeDefaultRuntimeClassPath(IJavaProject project) throws CoreException {
		long date1 = System.currentTimeMillis();
		try {
			return CACHE.getUnchecked(project);
		} finally {
			long date2 = System.currentTimeMillis();
		}
	}

	public IRuntimeClasspathEntry[] computeUnresolvedRuntimeClasspath(IJavaProject project) throws CoreException {
		return JavaRuntime.computeUnresolvedRuntimeClasspath(project);
	}

	public IClasspathContainer getClasspathContainer(IPath path, IJavaProject project) throws CoreException {
		return JavaCore.getClasspathContainer(path, project);
	}
}
