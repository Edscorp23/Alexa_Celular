package com.miasistente.app;
import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
public final class ReminderStore {
  static final String PREF="reminders"; private ReminderStore(){}
  static final class Item {final int id; final String text; final long when; Item(int i,String t,long w){id=i;text=t;when=w;}}
  static synchronized List<Item> all(Context c){ List<Item> out=new ArrayList<>(); String s=c.getSharedPreferences(PREF,0).getString("items","[]");
    try {JSONArray a=new JSONArray(s);for(int i=0;i<a.length();i++){JSONObject o=a.getJSONObject(i);out.add(new Item(o.getInt("id"),o.getString("text"),o.getLong("when")));}}catch(Exception ignored){}return out;}
  static synchronized void add(Context c,Item item){List<Item> a=all(c);a.add(item);save(c,a);}
  static synchronized void updateTime(Context c,int id,long when){
    List<Item> a=all(c);
    for(int k=0;k<a.size();k++){
      Item item=a.get(k);
      if(item.id==id){a.set(k,new Item(item.id,item.text,when));break;}
    }
    save(c,a);
  }
  static synchronized Item get(Context c,int id){for(Item i:all(c))if(i.id==id)return i;return null;}
  static synchronized void remove(Context c,int id){List<Item> a=all(c);a.removeIf(i->i.id==id);save(c,a);}
  static synchronized void save(Context c,List<Item> a){JSONArray j=new JSONArray();for(Item i:a){try{JSONObject o=new JSONObject();o.put("id",i.id);o.put("text",i.text);o.put("when",i.when);j.put(o);}catch(Exception ignored){}}
    c.getSharedPreferences(PREF,0).edit().putString("items",j.toString()).apply();}
}
