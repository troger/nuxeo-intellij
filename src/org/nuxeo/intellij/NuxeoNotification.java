package org.nuxeo.intellij;

import com.intellij.notification.Notification;
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
            String content, NotificationType type) {
        final Notification notification = new Notification(NUXEO_GROUP, title,
                content, type);
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                Notifications.Bus.notify(notification, project);
            }
        });
    }

}
