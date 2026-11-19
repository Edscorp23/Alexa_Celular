package com.miasistente.app;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
public class BootReceiver extends BroadcastReceiver {
  @Override public void onReceive(Context c,Intent intent){if(!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()))return;
    for(ReminderStore.Item item:ReminderStore.all(c))ReminderScheduler.schedule(c,item.id,Math.max(System.currentTimeMillis()+15000,item.when));
  }
}
