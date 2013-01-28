package org.nuxeo.intellij;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;

/**
 * Helper class to easily show {@link Notification}s related to the Nuxeo
 * plugin.
 */
public class NuxeoNotification {

    public static final String NUXEO_GROUP = "Nuxeo";

    private NuxeoNotification() {
    }

    public static void show(final Project project, String title,
            String content, NotificationType type, NotificationListener listener) {
        final Notification notification = new Notification(NUXEO_GROUP, title,
                content, type, listener);
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                Notifications.Bus.notify(notification, project);
            }
        });
    }

    public static void show(final Project project, String title,
                            String content, NotificationType type) {
        show(project, title, content, type, null);
    }

    public static void show(final Project project, String content,
            NotificationType type, NotificationListener listener) {
        show(project, NUXEO_GROUP, content, type, listener);
    }

    public static void show(final Project project, String content,
            NotificationType type) {
        show(project, NUXEO_GROUP, content, type, null);
    }

}
