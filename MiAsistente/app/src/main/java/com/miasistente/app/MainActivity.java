package com.miasistente.app;
import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class MainActivity extends Activity {
 private TextToSpeech tts;private LinearLayout reminderList;private EditText command;
 private final Handler timer=new Handler(Looper.getMainLooper());
 private final java.util.Map<Integer,TextView> counters=new java.util.HashMap<>();
 private final Runnable ticker=new Runnable(){@Override public void run(){updateCountdowns();timer.postDelayed(this,1000);}};
 private int dp(float x){return (int)(x*getResources().getDisplayMetrics().density);}
 private GradientDrawable bg(int color){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(dp(18));return g;}
 private TextView label(String value,int size){TextView t=new TextView(this);t.setText(value);t.setTextColor(Color.WHITE);t.setTextSize(size);t.setPadding(dp(6),dp(9),dp(6),dp(9));return t;}
 private void speak(String message){if(tts!=null)tts.speak(message,TextToSpeech.QUEUE_FLUSH,null,"answer");}
 @Override public void onCreate(Bundle b){super.onCreate(b);if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},20);
   tts=new TextToSpeech(this,s->{if(s==TextToSpeech.SUCCESS)tts.setLanguage(new Locale("es","ES"));});ui();}
 private void ui(){ScrollView scroll=new ScrollView(this);LinearLayout content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);content.setPadding(dp(19),dp(20),dp(19),dp(25));content.setBackgroundColor(Color.rgb(14,21,35));scroll.addView(content);
   TextView title=label("MI ASISTENTE  ✦",28);title.setTypeface(null,Typeface.BOLD);content.addView(title);
   TextView desc=label("Tu asistente personal en el teléfono, sin Amazon Alexa.",14);desc.setTextColor(0xFFFFD58C);content.addView(desc);
   ImageView artwork=new ImageView(this);artwork.setImageResource(R.drawable.anime_background);artwork.setScaleType(ImageView.ScaleType.CENTER_CROP);content.addView(artwork,new LinearLayout.LayoutParams(-1,dp(190)));
   TextView hint=label("Crea un recordatorio y elige su fecha y hora.",15);content.addView(hint);
   Button mic=button("🎙 Hablar con mi asistente",0xFF2763A1);content.addView(mic);mic.setOnClickListener(v->listen());
   command=new EditText(this);command.setSingleLine(false);command.setMinLines(2);command.setHint("O escribe tu comando aquí...");command.setTextColor(Color.WHITE);command.setHintTextColor(0xFFB7B7B7);command.setBackground(bg(0xFF25344A));content.addView(command);
   Button create=button("📅 Crear recordatorio: elegir fecha y hora",0xFF316B56);content.addView(create);
   create.setOnClickListener(v->{String value=command.getText().toString().trim();if(value.isEmpty()){command.setError("Escribe qué quieres recordar");command.requestFocus();return;}String low=value.toLowerCase(Locale.ROOT);if(low.startsWith("recuérdame ")||low.startsWith("recuerdame "))value=value.substring(value.indexOf(' ')+1).trim();chooseDateAndTime(value,System.currentTimeMillis()+60000);});
   Button send=button("Enviar otro comando",0xFF275276);content.addView(send);send.setOnClickListener(v->{handle(command.getText().toString());command.setText("");});
   Button test=button("🔊 Probar voz",0xFF75522E);content.addView(test);test.setOnClickListener(v->speak("Hola, soy tu asistente personal. Estoy listo para ayudarte."));
   TextView heading=label("RECORDATORIOS PENDIENTES",19);heading.setTypeface(null,Typeface.BOLD);content.addView(heading);
   TextView info=label("Elige fecha y hora al crear. Después podrás posponer cada aviso cinco minutos.",13);content.addView(info);
   reminderList=new LinearLayout(this);reminderList.setOrientation(LinearLayout.VERTICAL);content.addView(reminderList);
   refresh();setContentView(scroll);}

 private Button button(String name,int color){Button b=new Button(this);b.setText(name);b.setTextColor(Color.WHITE);b.setAllCaps(false);b.setBackground(bg(color));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(53));p.setMargins(0,dp(6),0,dp(7));b.setLayoutParams(p);return b;}
 private void listen(){Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"es-ES");i.putExtra(RecognizerIntent.EXTRA_PROMPT,"¿Qué necesitas?");try{startActivityForResult(i,101);}catch(Exception e){Toast.makeText(this,"No hay servicio de reconocimiento de voz disponible",Toast.LENGTH_LONG).show();}}
 @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request==101&&result==RESULT_OK&&data!=null){ArrayList<String> texts=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);if(texts!=null&&!texts.isEmpty())handle(texts.get(0));}}
 private void handle(String raw){String text=raw.trim();if(text.isEmpty())return;String norm=text.toLowerCase(Locale.ROOT);
   if(norm.contains("qué recordatorios")||norm.contains("que recordatorios")||norm.contains("mis recordatorios")){int count=ReminderStore.all(this).size();speak(count==0?"No tienes recordatorios pendientes":"Tienes "+count+" recordatorios pendientes");refresh();return;}
   if(norm.startsWith("recuérdame ")||norm.startsWith("recuerdame ")){
     String rest=text.substring(text.indexOf(' ')+1).trim();long suggested=System.currentTimeMillis()+60000;String what=rest;
     Matcher rel=Pattern.compile("(?i)\\s+en\\s+(\\d+)\\s+(minutos?|horas?)\\s*$").matcher(rest);
     Matcher abs=Pattern.compile("(?i)\\s+a\\s+las\\s+(\\d{1,2})(?::(\\d{2}))?\\s*$").matcher(rest);
     if(rel.find()){
       long amt=Long.parseLong(rel.group(1));
       if(amt>0&&amt<=100000){suggested=System.currentTimeMillis()+amt*(rel.group(2).toLowerCase(Locale.ROOT).startsWith("hora")?3600000L:60000L);}
       what=rest.substring(0,rel.start()).trim();
     }else if(abs.find()){
       int hour=Integer.parseInt(abs.group(1)), minute=abs.group(2)==null?0:Integer.parseInt(abs.group(2));
       if(hour<24&&minute<60){Calendar cal=Calendar.getInstance();cal.set(Calendar.HOUR_OF_DAY,hour);cal.set(Calendar.MINUTE,minute);cal.set(Calendar.SECOND,0);if(cal.getTimeInMillis()<=System.currentTimeMillis())cal.add(Calendar.DAY_OF_MONTH,1);suggested=cal.getTimeInMillis();}
       what=rest.substring(0,abs.start()).trim();
     }
     if(what.isEmpty()){Toast.makeText(this,"Indica qué quieres recordar",Toast.LENGTH_LONG).show();return;}
     chooseDateAndTime(what,suggested);return;
   }
   speak("Puedo crear recordatorios. Di recuérdame tomar agua y elegirás fecha y hora.");}
 private void chooseDateAndTime(String description,long suggested){
   Calendar selected=Calendar.getInstance();selected.setTimeInMillis(suggested);
   DatePickerDialog dateDialog=new DatePickerDialog(this,(view,year,month,day)->{
     selected.set(Calendar.YEAR,year);selected.set(Calendar.MONTH,month);selected.set(Calendar.DAY_OF_MONTH,day);
     TimePickerDialog timeDialog=new TimePickerDialog(this,(picker,hour,minute)->{
       selected.set(Calendar.HOUR_OF_DAY,hour);selected.set(Calendar.MINUTE,minute);
       selected.set(Calendar.SECOND,0);selected.set(Calendar.MILLISECOND,0);
       long selectedTime=selected.getTimeInMillis();
       if(selectedTime<=System.currentTimeMillis()){
         new AlertDialog.Builder(this).setTitle("Fecha u hora pasada")
           .setMessage("Elige una fecha y una hora futuras para tu recordatorio.")
           .setPositiveButton("Volver a elegir",(d,w)->chooseDateAndTime(description,System.currentTimeMillis()+60000))
           .setNegativeButton("Cancelar",null).show();return;
       }
       String readable=new SimpleDateFormat("dd/MM/yyyy 'a las' HH:mm",Locale.getDefault()).format(new Date(selectedTime));
       new AlertDialog.Builder(this).setTitle("Confirmar recordatorio")
         .setMessage(description+"\n"+readable)
         .setNegativeButton("Cancelar",null)
         .setPositiveButton("Guardar",(dialog,which)->saveReminder(description,selectedTime)).show();
     },selected.get(Calendar.HOUR_OF_DAY),selected.get(Calendar.MINUTE),true);
     timeDialog.setTitle("¿A qué hora te lo recuerdo?");timeDialog.show();
   },selected.get(Calendar.YEAR),selected.get(Calendar.MONTH),selected.get(Calendar.DAY_OF_MONTH));
   dateDialog.getDatePicker().setMinDate(System.currentTimeMillis()-86400000L);
   dateDialog.setTitle("¿Qué día te lo recuerdo?");dateDialog.show();
 }
 private void saveReminder(String description,long time){
   if(time<=System.currentTimeMillis()||description.trim().isEmpty())return;
   int id=(int)(System.currentTimeMillis()%1000000000);
   while(ReminderStore.get(this,id)!=null)id++;
   ReminderStore.add(this,new ReminderStore.Item(id,description,time));
   ReminderScheduler.schedule(this,id,time);
   if(command!=null)command.setText("");
   speak("Recordatorio guardado para "+new SimpleDateFormat("dd 'de' MMMM 'a las' HH:mm",new Locale("es","ES")).format(new Date(time)));
   Toast.makeText(this,"Recordatorio guardado",Toast.LENGTH_SHORT).show();refresh();
 }
 private String remaining(long time){
   long ms=time-System.currentTimeMillis();if(ms<=0)return "Aviso próximo (Android puede retrasarlo)";
   long seconds=(ms+999)/1000;long h=seconds/3600;long m=(seconds%3600)/60;long sec=seconds%60;
   return h>0?String.format(Locale.getDefault(),"%02d:%02d:%02d",h,m,sec):String.format(Locale.getDefault(),"%02d:%02d",m,sec);
 }
 private void updateCountdowns(){
   for(java.util.Map.Entry<Integer,TextView> e:counters.entrySet()){
     ReminderStore.Item item=ReminderStore.get(this,e.getKey());
     if(item!=null)e.getValue().setText("Próximo aviso en: "+remaining(item.when));
   }
 }
 private void refresh(){
   if(reminderList==null)return;
   reminderList.removeAllViews();counters.clear();
   java.util.List<ReminderStore.Item> list=ReminderStore.all(this);
   if(list.isEmpty()){reminderList.addView(label("No tienes recordatorios pendientes.",15));return;}
   for(ReminderStore.Item item:list){
     LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(13),dp(10),dp(13),dp(12));card.setBackground(bg(0xFF25344A));
     LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.setMargins(0,dp(7),0,dp(7));reminderList.addView(card,cp);
     TextView what=label(item.text,18);what.setTypeface(null,Typeface.BOLD);card.addView(what);
     TextView counter=label("Próximo aviso en: "+remaining(item.when),15);counter.setTextColor(0xFFFFD58C);card.addView(counter);counters.put(item.id,counter);
     TextView when=label("Próximo aviso estimado: "+new SimpleDateFormat("HH:mm",Locale.getDefault()).format(new Date(item.when)),12);card.addView(when);
     Button later=button("Recordar más tarde (5 min)",0xFF75522E);card.addView(later);
     later.setOnClickListener(v->{ReminderScheduler.snooze(this,item.id);((android.app.NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancel(item.id);Toast.makeText(this,"Pospuesto 5 minutos",Toast.LENGTH_SHORT).show();refresh();});
     Button done=button("Ya lo hice",0xFF316B56);card.addView(done);
     done.setOnClickListener(v->{ReminderScheduler.cancel(this,item.id);ReminderStore.remove(this,item.id);
       ((android.app.NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancel(item.id);refresh();});
   }
 }
 @Override protected void onResume(){super.onResume();refresh();timer.removeCallbacks(ticker);timer.post(ticker);}
 @Override protected void onPause(){timer.removeCallbacks(ticker);super.onPause();}
 @Override protected void onDestroy(){timer.removeCallbacks(ticker);if(tts!=null)tts.shutdown();super.onDestroy();}
}
