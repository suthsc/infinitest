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
@RunWith(PowerMockRunner.class)
        @PrepareForTest
package org.infinitest.intellij.idea;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.vfs.VirtualFile;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class IdeaModuleSettingsTest {

    @Mock
    private Module module;

    @Mock
    private ModuleRootManager moduleRootManager;
    @Mock
    private CompilerModuleExtension compilerModuleExtension;

    private IdeaModuleSettings ideaModuleSettings;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        ideaModuleSettings = new IdeaModuleSettingsForTest(module);
    }

    @Test
    public void testSimpleListClasspathElements() throws Exception {
        final OrderEntry orderEntry = mock(OrderEntry.class);
        when(moduleRootManager.getOrderEntries()).thenReturn(new OrderEntry[]{orderEntry});

        final VirtualFile virtualFile = mock(VirtualFile.class);
        when(orderEntry.getFiles(eq(OrderRootType.CLASSES))).thenReturn(new VirtualFile[]{virtualFile});
        when(virtualFile.getPath()).thenReturn("FakeFile");

        final VirtualFile compilerVirtualFile = mock(VirtualFile.class);
        when(compilerModuleExtension.getOutputRoots(eq(true))).thenReturn(new VirtualFile[]{compilerVirtualFile});
        when(compilerVirtualFile.getPath()).thenReturn("FakeCompilerOutputFile");
        List<File> fileList = ideaModuleSettings.listClasspathElements();

        assertThat(fileList).hasSize(2);
        assertThat(fileList).contains(new File("FakeFile"));
        assertThat(fileList).contains(new File("FakeCompilerOutputFile"));

    }

    @Test
    public void testDuplicateListClasspathElements() throws Exception {
        final OrderEntry firstOrderEntry = mock(OrderEntry.class);
        final OrderEntry secondOrderEntry = mock(OrderEntry.class);
        when(moduleRootManager.getOrderEntries()).thenReturn(new OrderEntry[]{firstOrderEntry, secondOrderEntry});

        final VirtualFile virtualFile = mock(VirtualFile.class);
        when(firstOrderEntry.getFiles(eq(OrderRootType.CLASSES))).thenReturn(new VirtualFile[]{virtualFile});
        when(secondOrderEntry.getFiles(eq(OrderRootType.CLASSES))).thenReturn(new VirtualFile[]{virtualFile});
        when(virtualFile.getPath()).thenReturn("FakeFile");

        final VirtualFile compilerVirtualFile = mock(VirtualFile.class);
        when(compilerModuleExtension.getOutputRoots(eq(true))).thenReturn(new VirtualFile[]{compilerVirtualFile});
        when(compilerVirtualFile.getPath()).thenReturn("FakeCompilerOutputFile");
        List<File> fileList = ideaModuleSettings.listClasspathElements();

        assertThat(fileList).hasSize(2);
        assertThat(fileList).contains(new File("FakeFile"));
        assertThat(fileList).contains(new File("FakeCompilerOutputFile"));

    }

    @Test
    public void testListClasspathElementsWithModuleOrderEntryType() throws Exception {

        final ModuleOrderEntry orderEntry = mock(ModuleOrderEntry.class);
        when(moduleRootManager.getOrderEntries()).thenReturn(new OrderEntry[]{orderEntry});
        when(orderEntry.getModule()).thenReturn(module);

        List<File> fileList = ideaModuleSettings.listClasspathElements();
        assertThat(fileList).isNotNull();

    }

    private class IdeaModuleSettingsForTest extends IdeaModuleSettings {
        public IdeaModuleSettingsForTest(final Module module) {
            super(module);
        }

        @Override
        ModuleRootManager moduleRootManagerInstance() {
            return moduleRootManager;
        }

        @Override
        CompilerModuleExtension compilerModuleExtension() {
            return compilerModuleExtension;
        }
    }
}