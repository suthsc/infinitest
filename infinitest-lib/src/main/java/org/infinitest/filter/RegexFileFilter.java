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
package org.infinitest.filter;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import org.infinitest.parser.JavaClass;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.INFO;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.infinitest.util.InfinitestUtils.log;

public class RegexFileFilter implements TestFilter {

    private final static String INCLUDE_PREFIX = "++ ";
    private final static String EXCLUDE_PREFIX = "-- ";

    private final File file;
    private final List<ClassFilter> filters = newArrayList();

    public RegexFileFilter(File file) {
        this.file = file;
        if (!file.exists()) {
            log(INFO, "Filter file " + file + " does not exist.");
        }
        updateFilterList();
    }

    public boolean match(JavaClass javaClass) {
        String className = javaClass.getName();
        for (ClassFilter filter : filters) {
            if (filter.match(className)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void updateFilterList() {
        filters.clear();

        if (file.exists()) {
            readFilterFile();
        } else {
            log(INFO, "Filtering file not in use.");
        }
    }

    private void readFilterFile() {
        try {
            for (String line : Files.readLines(file, Charsets.UTF_8)) {
                if (isValidFilter(line)) {
                    if (line.startsWith(INCLUDE_PREFIX)) {
                        filters.add(new IncludeFilter(line.substring(INCLUDE_PREFIX.length())));
                    } else if (line.startsWith(EXCLUDE_PREFIX)) {
                        filters.add(new ExcludeFilter(line.substring(EXCLUDE_PREFIX.length())));
                    } else {
                        filters.add(new DefaultFilter(line));
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Something horrible happened to the filter file", e);
        }
    }

    private boolean isValidFilter(String line) {
        return !isBlank(line) && !line.startsWith("!") && !line.startsWith("#");
    }

    private interface ClassFilter {

        boolean match(final String className);
    }


    private class DefaultFilter implements ClassFilter {

        private final Pattern pattern;

        public DefaultFilter(final String line) {
            pattern = Pattern.compile(line);
            log(FINEST, "excluding (default) tests matching " + line);
        }

        @Override
        public boolean match(final String className) {
            return pattern.matcher(className).lookingAt();
        }
    }


    private class IncludeFilter implements ClassFilter {

        private final Pattern pattern;

        public IncludeFilter(final String line) {
            pattern = Pattern.compile(line);
            log(FINEST, "including tests matching " + line);
        }

        @Override
        public boolean match(final String className) {
            return pattern.matcher(className).lookingAt();
        }
    }


    private class ExcludeFilter implements ClassFilter {

        private final Pattern pattern;

        public ExcludeFilter(final String line) {
            pattern = Pattern.compile(line);
            log(FINEST, "excluding tests matching " + line);
        }

        @Override
        public boolean match(final String className) {
            return !pattern.matcher(className).lookingAt();
        }
    }

}
