package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.util.*

object NotificationScheduler {

    const val CHANNEL_ID = "quran_daily_reminders"
    const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "تذكيرات مراجعة القرآن اليومية"
            val descriptionText = "إشعارات يومية لتذكير الطلاب وأولياء الأمور بموعد مراجعة الحفظ المقرر"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * التحقق مما إذا كان الوقت الحالي يقع ضمن فترة "عدم الإزعاج" (أوقات الراحة)
     */
    fun isDoNotDisturbActive(
        dndEnabled: Boolean,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int
    ): Boolean {
        if (!dndEnabled) return false

        val cal = Calendar.getInstance()
        val currentMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        val startMinutes = startHour * 60 + startMinute
        val endMinutes = endHour * 60 + endMinute

        return if (startMinutes < endMinutes) {
            // فترة نهارية (مثال: من 13:00 إلى 16:00 وقت القيلولة)
            currentMinutes in startMinutes..endMinutes
        } else {
            // فترة ليلية تمتد لليوم التالي (مثال: من 22:00 ليلاً إلى 07:00 صباحاً)
            currentMinutes >= startMinutes || currentMinutes <= endMinutes
        }
    }

    /**
     * إرسال إشعار تذكير محلي فوري بموعد مراجعة الحفظ
     * يعود بنتيجة نصية توضح ما إذا تم الإرسال أو تم الكتم بسبب وضع عدم الإزعاج
     */
    fun sendMemorizationReminder(
        context: Context,
        studentName: String,
        assignmentText: String,
        dndEnabled: Boolean,
        dndStartHour: Int,
        dndStartMinute: Int,
        dndEndHour: Int,
        dndEndMinute: Int
    ): Pair<Boolean, String> {
        // التحقق من وضع عدم الإزعاج أولاً
        if (isDoNotDisturbActive(dndEnabled, dndStartHour, dndStartMinute, dndEndHour, dndEndMinute)) {
            val quietHoursText = String.format(
                Locale("ar"),
                "من %02d:%02d إلى %02d:%02d",
                dndStartHour, dndStartMinute, endHourFormat(dndEndHour, dndEndMinute).first, endHourFormat(dndEndHour, dndEndMinute).second
            )
            return Pair(false, "🔕 تم إيقاف الإشعار مؤقتاً لوجود فترة عدم الإزعاج (أوقات الراحة $quietHoursText)")
        }

        createNotificationChannel(context)

        // التحقق من إذن الإشعارات لأندرويد 13 فما فوق
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return Pair(false, "⚠️ يرجى منح إذن الإشعارات من إعدادات الجهاز لاستقبال التنبيهات")
            }
        }

        val title = "تذكير ورد القرآن الكريم 📖"
        val message = if (assignmentText.isNotBlank()) {
            "حان موعد مراجعة الورد المقرر للطالب/ة $studentName:\n$assignmentText"
        } else {
            "حان موعد مراجعة وتثبيت حفظ القرآن الكريم للطالب/ة $studentName حسب الخطة المقررة."
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, builder.build())

        return Pair(true, "✅ تم إرسال إشعار التذكير بمراجعة الحفظ بنجاح")
    }

    private fun endHourFormat(h: Int, m: Int) = Pair(h, m)
}
