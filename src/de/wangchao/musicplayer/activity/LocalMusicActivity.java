package de.wangchao.musicplayer.activity;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.wangchao.musicplayer.R;
import de.wangchao.musicplayer.db.DataBase;
import de.wangchao.musicplayer.service.MusicService;
import de.wangchao.musicplayer.service.MusicService.MusicBinder;
import de.wangchao.musicplayer.type.Music;
import de.wangchao.musicplayer.util.Tools;

import de.wangchao.musicplayer.widget.GridViewAdapter;
import de.wangchao.musicplayer.widget.MiniPlayPannelWrapper;

import de.wangchao.musicplayer.widget.MiniPlayPannelWrapper.OnStatusChangedListener;
import android.app.Activity;
import android.content.ComponentName;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;

import android.os.Bundle;
import android.os.IBinder;

import android.provider.MediaStore;

import android.util.DisplayMetrics;

import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;



public class LocalMusicActivity extends Activity{
   private boolean mBound=false;
   private MusicService mService;
   private MiniPlayPannelWrapper miniPlayPannelWrapper;
   private static DataBase db;
   private ArrayList<Music> mMusicList = new ArrayList<Music>();
   public static ArrayList<Map<String,Object>> allMusicMap=new ArrayList<Map<String,Object>>();
   public static ArrayList<Map<String,Object>> singerMusicMap=new ArrayList<Map<String,Object>>();
   public static ArrayList<Map<String,Object>> albumMusicMap=new ArrayList<Map<String,Object>>();
   public static ArrayList<Map<String,Object>> fileMusicMap=new ArrayList<Map<String,Object>>();
   public static ArrayList<Map<String,Object>> playListMusicMap=new ArrayList<Map<String,Object>>();
   public static final String ALL_MUSIC="allmusic";
   public static final String SINGER_MUSIC="singermusic";
   public static final String ALBUM_MUSIC="albummusic";
   public static final String FILE_MUSIC="filemusic";
   public static final String PLAYLIST_MUSIC="playlistmusic";
   
   private ServiceConnection mConnection = new ServiceConnection() {
	        @Override
	        public void onServiceConnected(ComponentName name, IBinder service) {

	            MusicBinder binder = (MusicBinder) service;
	            mService = binder.getService();
	            mBound = true;

	            miniPlayPannelWrapper.bindService(mService);
	            miniPlayPannelWrapper.registerBroadcastReceiver(LocalMusicActivity.this,
	                    new OnStatusChangedListener() {
	                        @Override
	                        public void onStatusChanged() {
	                            miniPlayPannelWrapper.bindService(mService);
	                        }
	                    });
	        }
	        @Override
	        public void onServiceDisconnected(ComponentName name) {
	            mBound = false;
	        }
	 };
   @Override
   protected void onCreate(Bundle savedInstanceState){
	   super.onCreate(savedInstanceState);
	   setContentView(R.layout.local_home);
	   View panel=(View)findViewById(R.id.panel);
	   miniPlayPannelWrapper=new MiniPlayPannelWrapper(panel);
	   db=new DataBase(this);
	   //lv_music=(ListView)findViewById(R.id.listView1);
	   DisplayMetrics outMetrics = new   DisplayMetrics();
	   this.getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
	   int width=outMetrics.widthPixels ;
	   
	   GridView grid=(GridView)findViewById(R.id.gridView1);
	   grid.setAdapter(new GridViewAdapter(this,(width-4*5)/3));
	   grid.setOnItemClickListener(new OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3) {
			// TODO Auto-generated method stub
				Intent intent=new Intent(LocalMusicActivity.this,LocalMusicListActivity.class);
				switch(position){
				case 0:
					intent=new Intent(LocalMusicActivity.this,SongsListActivity.class);
					intent.putExtra("id", ALL_MUSIC);
					break;
				case 1:
					intent.putExtra("id", SINGER_MUSIC);
					break;
				case 2:
					intent.putExtra("id", ALBUM_MUSIC);
					break;
				case 3:
					intent.putExtra("id", FILE_MUSIC);
					break;
				case 4:
					intent.putExtra("id", PLAYLIST_MUSIC);
					break;
				case 5:
					//intent=new Intent(LocalMusicActivity.this,SettingsActivity.class);
					Toast.makeText(getApplicationContext(), "unfinished", Toast.LENGTH_SHORT).show();
					break;
				default:
					break;
				}
				if(position!=5)
				startActivity(intent);
				return;
		}
		   
	   });
	   
	   initMediaData();
   }
   
   @Override
   protected void onStart() {
	        super.onStart();
	        // Bind to music service
	        Intent intent = new Intent(LocalMusicActivity.this, MusicService.class);
	        getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}
   
   @Override
   protected void onDestroy() {

       super.onDestroy();
       if (mBound) {
           getApplicationContext().unbindService(mConnection);
           mBound = false;
       }
      
       miniPlayPannelWrapper.unregister();
   }
   @Override
   public boolean onKeyDown(int keyCode, KeyEvent event) {

       if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
           Tools.exitDialog(this);
           return true;
       }

       return super.onKeyDown(keyCode, event);
   }
   
   private void initMediaData(){
	   mMusicList.clear();
	   allMusicMap.clear();
	   singerMusicMap.clear();
	   albumMusicMap.clear();
	   fileMusicMap.clear();
	   
	   ContentResolver contentResolver = LocalMusicActivity.this.getContentResolver(); 
	   
	   //query all music
		Cursor cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
				 MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		if(cursor==null)
			return;
		while(cursor.moveToNext()){
			Music music=new Music();
			music.setSongId(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)));
			music.setSongName(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)));
			music.setSingerName(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)));
			music.setAlbum(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)));
			music.setWebFile(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));
			music.setFormNet(false);
			music.setLength(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)));
			//list.add(music);
			mMusicList.add(music);
			
			Map<String,Object> map=new HashMap<String,Object>();
			map.put("txt1", music.getSongName());
			map.put("txt2", music.getSingerName());
			map.put("music", music);
			allMusicMap.add(map);
		}
		cursor.close();
		
		//query artist
		cursor = contentResolver.query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, null, null, null,
				 MediaStore.Audio.Artists.DEFAULT_SORT_ORDER);
		while(cursor.moveToNext()){
			Map<String,Object> map=new HashMap<String,Object>();
			String singer=cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST));
			String songcount=cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_TRACKS));			
			ArrayList<Music> musiclist=new ArrayList<Music>();
			for(int i=0;i<mMusicList.size();i++){
				if(mMusicList.get(i).getSingerName().equals(singer)){
					musiclist.add(mMusicList.get(i));
				}
			}
			map.put("txt1", singer);
			map.put("txt2", songcount+"首歌曲");
			map.put("list", musiclist);
			singerMusicMap.add(map);
		}
		cursor.close();
		
		//query album
		cursor = contentResolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null, null, null,
				 MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);
		while(cursor.moveToNext()){
			Map<String,Object> map=new HashMap<String,Object>();
			String albumname=cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM));
			String albumart=cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART));
			String singer=cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST));
			
			ArrayList<Music> musiclist=new ArrayList<Music>();
			for(int i=0;i<mMusicList.size();i++){
				if(mMusicList.get(i).getAlbum().equals(albumname)){
					musiclist.add(mMusicList.get(i));
					mMusicList.get(i).setPic(albumart);//add pic in song
				}
			}
			map.put("txt1", albumname);
			map.put("txt2", singer);
			map.put("art", albumart);
			map.put("list", musiclist);
			albumMusicMap.add(map);
		}
		cursor.close();
		
		//file
		ArrayList<String> fileList=new ArrayList<String>();//list to store file url which contains music
		for(Music music : mMusicList){
		    String fileurl=Tools.getFileUrlString(music.getWebFile());
			if(!fileList.contains(fileurl))
				fileList.add(fileurl);
		}
		for(String url : fileList){
			ArrayList<Music> fileMusic=new ArrayList<Music>();
			for(Music music : mMusicList){
				if(url.equals(Tools.getFileUrlString(music.getWebFile())))
					fileMusic.add(music);
			}
			Map<String,Object> map=new HashMap<String,Object>();
		    map.put("txt1", Tools.getFileNameString(url));
		    map.put("txt2", url);
		    map.put("list", fileMusic);
		    fileMusicMap.add(map);
		}
		
		//myplaylist 
		refreshPlayList();
   }
   
   public static void refreshPlayList(){
	   playListMusicMap.clear();
	   ArrayList<String> playlistList=db.getMyPlayList();
		if(playlistList!=null){
			for(String playlist:playlistList){
				ArrayList<Music> musicInPlayList=db.getMusicInPlayList(playlist);
				Map<String,Object> map=new HashMap<String,Object>();
				map.put("txt1", playlist);
				map.put("txt2", musicInPlayList.size()+"首歌曲");
				map.put("list", musicInPlayList);
				playListMusicMap.add(map);
			}
		}
   }

}
