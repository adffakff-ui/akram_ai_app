package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.data.model.NotificationType
import com.example.data.model.PushNotification
import com.example.data.model.Student

object PushNotificationHelper {

    const val STUDENT_ALERTS_CHANNEL_ID = "manarat_student_alerts_channel"
    private const val CHANNEL_NAME = "تنبيهات طلاب الحلقات الفورية"
    private const val CHANNEL_DESC = "تنبيهات فورية مخصصة للطلاب من المعلم بخصوص الحفظ والمراجعة والغياب"

    fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(STUDENT_ALERTS_CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    fun sendStudentNotification(
        context: Context,
        notification: PushNotification
    ) {
        initNotificationChannels(context)

        if (!hasNotificationPermission(context)) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notification.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, STUDENT_ALERTS_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(notification.title)
            .setContentText(notification.message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notification.message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            val notificationId = (notification.id.toInt().takeIf { it != 0 } ?: (System.currentTimeMillis() % 10000).toInt())
            notificationManager.notify(notificationId, builder.build())
        } catch (_: SecurityException) {
        }
    }

    data class NotificationTemplate(
        val type: NotificationType,
        val defaultTitle: String,
        val defaultMessageTemplate: String
    )

    fun getTemplatesForStudent(student: Student): List<NotificationTemplate> {
        return listOf(
            NotificationTemplate(
                type = NotificationType.HIFZ_ENCOURAGEMENT,
                defaultTitle = "ما شاء الله يا ${student.name} 🌟",
                defaultMessageTemplate = "مبارك إتقانك لتسميع اليوم! استمر في تميزك القرآني ونفع الله بك والديك وأمتك."
            ),
            NotificationTemplate(
                type = NotificationType.HOMEWORK,
                defaultTitle = "واجب الحفظ والمراجعة غداً 📖",
                defaultMessageTemplate = "السلام عليكم ${student.name}، نود تذكيرك بمراجعة سورة ${student.currentSurah} استعداداً لحلقة الغد."
            ),
            NotificationTemplate(
                type = NotificationType.ATTENDANCE,
                defaultTitle = "تنبيه الحضور للحلقة ⏰",
                defaultMessageTemplate = "نأمل من الطالب ${student.name} الحرص على الحضور في الموعد المحدد للحلقة للاستفادة الكاملة."
            ),
            NotificationTemplate(
                type = NotificationType.EXAM_PREPARATION,
                defaultTitle = "استعداد لاختبار جزء ${student.currentJuz} 🎯",
                defaultMessageTemplate = "تم تحديد موعد تقييم إتقان الجزء ${student.currentJuz}، يرجى التثبيت والمراجعة المكثفة."
            ),
            NotificationTemplate(
                type = NotificationType.PARENT_REMINDER,
                defaultTitle = "رسالة لولي أمر ${student.name} 👨‍👧‍👦",
                defaultMessageTemplate = "المكرم ${student.parentName}، نود إحاطتكم بتميز ابنكم اليوم في الحلقة، شاكرين لكم حسن المتابعة المنزلية."
            )
        )
    }
}
