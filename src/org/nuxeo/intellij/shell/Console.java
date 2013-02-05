package org.nuxeo.intellij.shell;

import java.io.IOException;
import java.io.Writer;

import jline.ConsoleReader;

import org.nuxeo.shell.swing.SwingTerminal;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;

/**
 * Wraps the original Nuxeo Shell Console class to have a safer out stream.
 * <p>
 * All the writing calls to any Swing components are now done in the AWT event
 * dispatch thread.
 */
public class Console extends org.nuxeo.shell.swing.Console {

    private static final Logger logger = Logger.getInstance(Console.class.getName());

    protected SafeOut safeOut;

    public Console() throws Exception {
        super();
        safeOut = new SafeOut(this);
        reader = new ConsoleReader(in, safeOut, null, new SwingTerminal(this));
    }

    class SafeOut extends Writer {

        protected final Console console;

        public SafeOut(Console console) {
            this.console = console;
        }

        protected void _write(char[] cbuf, int off, int len) throws IOException {
            _write(new String(cbuf, off, len));
        }

        protected void _write(final String str) throws IOException {
            ApplicationManager.getApplication().invokeLater(new Runnable() {
                @Override
                public void run() {
                    console.append(str);
                    setCaretPosition(getDocument().getLength());
                }
            });
        }

        protected boolean handleOutputChar(char c) {
            try {
                if (c == 7) { // beep
                    beep();
                } else if (c < 32 && c != '\n' && c != '\t') {
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                logger.error(e);
            }
            return true;
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            if (len == 1) {
                char c = cbuf[off];
                if (!handleOutputChar(c)) {
                    _write(cbuf, off, len);
                }
            } else {
                StringBuilder buf = new StringBuilder();
                for (int i = off, end = off + len; i < end; i++) {
                    char c = cbuf[i];
                    if (!handleOutputChar(c)) {
                        buf.append(c);
                    }
                }
                if (buf.length() > 0) {
                    _write(buf.toString());
                }
            }
        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public void close() throws IOException {
            flush();
        }
    }

}
