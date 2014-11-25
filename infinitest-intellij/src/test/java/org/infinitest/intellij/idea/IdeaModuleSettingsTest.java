package org.infinitest.intellij.idea;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.vfs.VirtualFile;
import org.junit.Before;
import org.junit.Test;
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