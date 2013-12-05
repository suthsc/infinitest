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
package org.infinitest.testrunner;

import java.io.*;
import java.util.*;

import org.infinitest.*;
import org.infinitest.testrunner.process.*;
import org.infinitest.testrunner.queue.*;

public class MultiProcessRunner extends AbstractTestRunner {
	private final ProcessConnectionFactory remoteProcessManager;
	private QueueConsumer queueConsumer;

	public MultiProcessRunner() {
		this(new NativeConnectionFactory(JUnit4Runner.class), null);
	}

	public MultiProcessRunner(final ProcessConnectionFactory remoteProcessManager, RuntimeEnvironment environment) {
		this.remoteProcessManager = remoteProcessManager;
		setRuntimeEnvironment(environment);
	}

	private QueueConsumer createQueueConsumer(final ProcessConnectionFactory remoteProcessManager) {
		return new QueueConsumer(getEventSupport(), new TestQueue(getTestPriority())) {
			@Override
			protected QueueProcessor createQueueProcessor() throws IOException {
				return new TestQueueProcessor(getEventSupport(), remoteProcessManager, getRuntimeEnvironment());
			}
		};
	}

	@Override
	public void setConcurrencyController(ConcurrencyController semaphore) {
		super.setConcurrencyController(semaphore);
	}

	@Override
	public void runTests(List<String> testNames) {
		if (!testNames.isEmpty()) {
			if (queueConsumer == null) {
				queueConsumer = createQueueConsumer(remoteProcessManager);
			}
			queueConsumer.setConcurrencySemaphore(getConcurrencySemaphore());
			queueConsumer.push(testNames);
		}
	}
}
