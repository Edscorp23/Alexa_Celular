package com.miasistente.app;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import java.util.Locale;
public class ReminderReceiver extends BroadcastReceiver {
  static final String CH="pending_reminders";
  @Override public void onReceive(Context c,Intent i){int id=i.getIntExtra("id",-1);if(id<0)return;
    NotificationManager n=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
    if("done".equals(i.getAction())){ReminderStore.remove(c,id);ReminderScheduler.cancel(c,id);n.cancel(id);return;}
    if("snooze".equals(i.getAction())){
      ReminderScheduler.snooze(c,id);
      n.cancel(id);
      return;
    }
    ReminderStore.Item item=ReminderStore.get(c,id);if(item==null)return;
    // Ignore stale deliveries, e.g. an old alarm fired after a snooze.
    if(item.when>System.currentTimeMillis()+1500L){
      ReminderScheduler.schedule(c,id,item.when);return;
    }
    NotificationChannel channel=new NotificationChannel(CH,"Recordatorios",NotificationManager.IMPORTANCE_HIGH);n.createNotificationChannel(channel);
    PendingIntent done=ReminderScheduler.intent(c,id,"done");
    PendingIntent snooze=ReminderScheduler.intent(c,id,"snooze");
    Notification notification=new Notification.Builder(c,CH).setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
      .setContentTitle("Recordatorio pendiente").setContentText(item.text).setStyle(new Notification.BigTextStyle().bigText(item.text))
      .setOngoing(true).setAutoCancel(false)
      .addAction(new Notification.Action.Builder(android.R.drawable.checkbox_on_background,"Ya lo hice",done).build())
      .addAction(new Notification.Action.Builder(android.R.drawable.ic_lock_idle_alarm,"Recordar más tarde (5 min)",snooze).build()).build();
    try{n.notify(id,notification);}catch(SecurityException ignored){}
    // Reprogramar antes de hablar: la tarea sobrevivira si falla la sintesis de voz.
    long next=System.currentTimeMillis()+ReminderScheduler.REPEAT_MS;
    ReminderStore.updateTime(c,id,next);
    ReminderScheduler.schedule(c,id,next);
    final PendingResult pending=goAsync();
    final TextToSpeech[] voice=new TextToSpeech[1];
    final android.os.Handler handler=new android.os.Handler(android.os.Looper.getMainLooper());
    final java.util.concurrent.atomic.AtomicBoolean finished=new java.util.concurrent.atomic.AtomicBoolean(false);
    final Runnable finish=()->{if(!finished.compareAndSet(false,true))return;try{if(voice[0]!=null)voice[0].shutdown();}catch(Exception ignored){}pending.finish();};
    handler.postDelayed(finish,8000);
    voice[0]=new TextToSpeech(c,status->{if(status==TextToSpeech.SUCCESS && voice[0]!=null){voice[0].setLanguage(new Locale("es","ES"));
      voice[0].setOnUtteranceProgressListener(new UtteranceProgressListener(){public void onStart(String s){}public void onDone(String s){handler.removeCallbacks(finish);handler.post(finish);}public void onError(String s){handler.removeCallbacks(finish);handler.post(finish);}});
      voice[0].speak(item.text,TextToSpeech.QUEUE_FLUSH,null,"recordatorio-"+id);
    }else{handler.removeCallbacks(finish);handler.post(finish);}});
  }
}
