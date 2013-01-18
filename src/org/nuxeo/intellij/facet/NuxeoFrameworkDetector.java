package org.nuxeo.intellij.facet;

import org.jetbrains.annotations.NotNull;

import com.intellij.facet.FacetType;
import com.intellij.framework.detection.FacetBasedFrameworkDetector;
import com.intellij.framework.detection.FileContentPattern;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PatternCondition;
import com.intellij.util.ProcessingContext;
import com.intellij.util.indexing.FileContent;

/**
 * Framework detector to automatically detect if a module could be a Nuxeo
 * module.
 * <p>
 * Detection is triggered if the module contains a MANIFEST.MF file containing
 * the string "Nuxeo" or "nuxeo".
 */
public class NuxeoFrameworkDetector extends
        FacetBasedFrameworkDetector<NuxeoFacet, NuxeoFacetConfiguration> {

    public NuxeoFrameworkDetector() {
        super("nuxeo");
    }

    @Override
    public FacetType<NuxeoFacet, NuxeoFacetConfiguration> getFacetType() {
        return NuxeoFacet.getFacetType();
    }

    @NotNull
    public FileType getFileType() {
        return FileTypeManager.getInstance().getFileTypeByExtension("MF");
    }

    @NotNull
    public ElementPattern<FileContent> createSuitableFilePattern() {
        return FileContentPattern.fileContent().withName("MANIFEST.MF").with(
                new PatternCondition<FileContent>("withNuxeoContent") {
                    @Override
                    public boolean accepts(@NotNull
                    FileContent fileContent, ProcessingContext context) {
                        return isNuxeoManifest(fileContent);
                    }
                });
    }

    private boolean isNuxeoManifest(FileContent fileContent) {
        String content = fileContent.getContentAsText().toString();
        return content.contains("Nuxeo")
                || content.contains("nuxeo");
    }

}
