package com.naviy.uz.clean_gen.ui

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.Project

/**
 * Notification factory
 */
interface Notifier {
    companion object {
        /**
         * Creates a warning notification in the [project] context with the [content]
         */
        fun warning(project: Project?, content: String) =
            Notifications.Bus.notify(
                Notification(
                    "CleanArchitectureGenerator",
                    "Clean architecture generator Warning",
                    content,
                    NotificationType.WARNING
                ), project
            )

        /**
         * Creates an error notification in the [project] context with the [content]
         */
        fun error(project: Project?, content: String) =
            Notifications.Bus.notify(
                Notification(
                    "CleanArchitectureGenerator",
                    "Clean architecture generator Warning",
                    content,
                    NotificationType.ERROR
                ), project
            )
    }
}