package net.bnfour.phone2web2tg_forwarder;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

public class Notifier {
    public static void showNotification(Context context, String message) {

        Intent intent = new Intent(context, MainPreferencesActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        // channel id is ignore on api 25 and lower, and i'm writing with 19 in mind
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "whatever")
                // all notifications indicate that something went wrong and so use
                // the exclamation sign icon
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setContentTitle(context.getResources().getString(R.string.app_name))
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        // updates and/or removal are not needed, hence static id
        notificationManager.notify(0, builder.build());
    }
}
