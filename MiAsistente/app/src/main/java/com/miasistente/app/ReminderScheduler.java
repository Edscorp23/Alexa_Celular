package com.miasistente.app;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
public final class ReminderScheduler {
  private ReminderScheduler(){}
  public static final long REPEAT_MS=2*60*1000L;
  public static final long SNOOZE_MS=5*60*1000L;
  static PendingIntent intent(Context c,int id,String action){
    Intent i=new Intent(c,ReminderReceiver.class).setAction(action).putExtra("id",id);
    // Unique request codes per action so 'done' and 'snooze' cannot overwrite a ringing alarm.
    int code=id*3 + ("done".equals(action)?1:("snooze".equals(action)?2:0));
    return PendingIntent.getBroadcast(c,code,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  }
  static void schedule(Context c,int id,long time){
    AlarmManager a=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
    if(a!=null)a.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,Math.max(System.currentTimeMillis()+1000,time),intent(c,id,"ring"));
  }
  static void cancel(Context c,int id){
    AlarmManager a=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
    if(a!=null)a.cancel(intent(c,id,"ring"));
  }
  static void snooze(Context c,int id){
    ReminderStore.Item item=ReminderStore.get(c,id);
    if(item==null)return;
    long next=System.currentTimeMillis()+SNOOZE_MS;
    ReminderStore.updateTime(c,id,next);
    schedule(c,id,next);
  }
}
