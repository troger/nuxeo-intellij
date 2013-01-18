package org.nuxeo.intellij;

/**
 * Exception used when there is an issue while hot reloading Nuxeo modules.
 */
public class NuxeoHotReloadException extends RuntimeException {

    public NuxeoHotReloadException(String s) {
        super(s);
    }

    public NuxeoHotReloadException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public NuxeoHotReloadException(Throwable throwable) {
        super(throwable);
    }
}
