/*
 * Copyright � 2013 dvbviewer-controller Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.dvbviewer.controller.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.dvbviewer.controller.data.DbConsts.ChannelTbl;
import org.dvbviewer.controller.data.DbConsts.EpgTbl;
import org.dvbviewer.controller.data.DbConsts.NowTbl;
import org.dvbviewer.controller.entities.Channel;
import org.dvbviewer.controller.entities.Channel.Fav;
import org.dvbviewer.controller.entities.EpgEntry;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

/**
 * The Class DbHelper.
 * 
 * @author RayBa
 * @date 26.04.2012
 */
public class DbHelper extends SQLiteOpenHelper {

	private static final String	DATABASE_NAME		= "dvbviewer.db";

	private static final int	DATABASE_VERSION	= 2;

	CursorFactory				mCursorFactory;

	public static String		SMALLER_OR_EQUALS	= " <= ";
	
	public static String		BIGGER_OR_EQUALS	= " >= ";
	
	public static String		EQUALS				= " = ";
	
	public static String		NOT_EQUALS			= " != ";
	
	public static String		AND					= " AND ";
	
	public static String		BETWEEN				= " BETWEEN ";
	
	public static String		WHERE				= " WHERE ";
	
	public static String		ORDER_BY			= " ORDER BY ";
	
	public static String		BIT_AND				= " & ";

	/**
	 * Instantiates a new dB helper.
	 * 
	 * @param context
	 *            the context
	 * @author RayBa
	 * @date 26.04.2012
	 */
	public DbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite
	 * .SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS " + ChannelTbl.TABLE_NAME + "(" + ChannelTbl._ID + " INTEGER PRIMARY KEY," + ChannelTbl.NAME + " TEXT," + ChannelTbl.POSITION + " INTEGER, " + ChannelTbl.FAV_POSITION + " INTEGER, " + ChannelTbl.FAV_ID + " INTEGER,"  + ChannelTbl.EPG_ID + " INTEGER," + ChannelTbl.LOGO_URL + " TEXT," + ChannelTbl.FLAGS + " INTEGER);");
		db.execSQL("CREATE TABLE IF NOT EXISTS " + EpgTbl.TABLE_NAME + "(" + EpgTbl._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + EpgTbl.EPG_ID + " INTEGER," + EpgTbl.START + " INTEGER, " + EpgTbl.END + " INTEGER," + EpgTbl.TITLE + " TEXT," + EpgTbl.SUBTITLE + " TEXT," + EpgTbl.DESC + " TEXT);");
		db.execSQL("CREATE TABLE IF NOT EXISTS " + NowTbl.TABLE_NAME + "(" + NowTbl._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + NowTbl.EPG_ID + " INTEGER," + NowTbl.START + " INTEGER, " + NowTbl.END + " INTEGER," + NowTbl.TITLE + " TEXT," + NowTbl.SUBTITLE + " TEXT," + NowTbl.DESC + " TEXT);");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite
	 * .SQLiteDatabase, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		switch (newVersion) {
		case 2:
			db.execSQL("ALTER TABLE " + ChannelTbl.TABLE_NAME + " ADD COLUMN "+ ChannelTbl.LOGO_URL + " TEXT");
			break;

		default:
			Log.w(this.getClass().getSimpleName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ", doing nothing");
//			db.execSQL("DROP TABLE IF EXISTS " + ChannelTbl.TABLE_NAME);
//			db.execSQL("DROP TABLE IF EXISTS " + EpgTbl.TABLE_NAME);
//			break;
		}
	}

	/**
	 * Gets the channellist.
	 * 
	 * @return the channellist
	 * 
	 * @author RayBa
	 * @date 20.06.2010
	 * @description Gets the channellist.
	 */
	public List<Channel> loadChannellist() {
		SQLiteDatabase db = getWritableDatabase();
		List<Channel> result = new ArrayList<Channel>();
		Cursor c = db.rawQuery("SELECT * FROM " + ChannelTbl.TABLE_NAME + " ORDER BY " + ChannelTbl.POSITION, null);
		while (c.moveToNext()) {
			Channel channel = new Channel();
			channel.setId(c.getLong(c.getColumnIndex(ChannelTbl._ID)));
			channel.setEpgID(c.getLong(c.getColumnIndex(ChannelTbl.EPG_ID)));
			String name = c.getString(c.getColumnIndex(ChannelTbl.NAME));
			name = name.replaceAll("\\([^\\(]*\\)", "").trim();
			channel.setName(name);
			channel.setPosition(c.getInt(c.getColumnIndex(ChannelTbl.POSITION)));
			result.add(channel);
		}
		c.close();
		db.close();
		if (result.size() <= 0) {
			result = null;
		}
		return result;
	}

	/**
	 * Gets the channellist.
	 * 
	 * @return the channellist
	 * 
	 * @author RayBa
	 * @date 20.06.2010
	 * @description Gets the channellist.
	 */
	public List<Channel> loadPendingUpdateChannellist() {
		SQLiteDatabase db = getWritableDatabase();
		List<Channel> result = new ArrayList<Channel>();
		Cursor c = db.rawQuery("SELECT * FROM " + ChannelTbl.TABLE_NAME + WHERE + ChannelTbl.FLAGS + BIT_AND + Channel.FLAG_PENDING_UPDATE + NOT_EQUALS + "0" + ORDER_BY + ChannelTbl.POSITION, null);
		while (c.moveToNext()) {
			Channel channel = new Channel();
			channel.setId(c.getLong(c.getColumnIndex(ChannelTbl._ID)));
			channel.setEpgID(c.getLong(c.getColumnIndex(ChannelTbl.EPG_ID)));
			String name = c.getString(c.getColumnIndex(ChannelTbl.NAME));
			name = name.replaceAll("\\([^\\(]*\\)", "").trim();
			channel.setName(name);
			channel.setPosition(c.getInt(c.getColumnIndex(ChannelTbl.POSITION)));
			result.add(channel);
		}
		c.close();
		db.close();
		if (result.size() <= 0) {
			result = null;
		}
		return result;
	}

	/**
	 * Gets the channel cursor.
	 *
	 * @return the channel cursor
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public Cursor getChannelCursor() {
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.rawQuery("SELECT * FROM " + ChannelTbl.TABLE_NAME + " ORDER BY " + ChannelTbl.POSITION, null);
		return c;
	}

	/**
	 * Gets the fav cursor.
	 *
	 * @return the fav cursor
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public Cursor getFavCursor() {
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.rawQuery("SELECT * FROM " + ChannelTbl.TABLE_NAME + " WHERE " + ChannelTbl.FLAGS + " & " + Channel.FLAG_FAV + "!= 0 ORDER BY " + ChannelTbl.FAV_POSITION, null);
		return c;
	}

	/**
	 * Gets the channellist.
	 *
	 * @param epgId the epg id
	 * @param start the start
	 * @param stop the stop
	 * @return the channellist
	 * @author RayBa
	 * @date 20.06.2010
	 * @description Gets the channellist.
	 */
	public List<EpgEntry> loadChannelEPG(long epgId, long start, long stop) {
		SQLiteDatabase db = getWritableDatabase();
		List<EpgEntry> result = new ArrayList<EpgEntry>();
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(EpgTbl.TABLE_NAME);
		Cursor c = qb.query(db, null, EpgTbl.EPG_ID + EQUALS + epgId + AND + EpgTbl.END + BETWEEN + start + AND + stop, null, null, null, EpgTbl.END);
		while (c.moveToNext()) {
			EpgEntry epg = new EpgEntry();
			epg.setId(c.getLong(c.getColumnIndex(EpgTbl._ID)));
			epg.setEpgID(c.getLong(c.getColumnIndex(EpgTbl.EPG_ID)));
			epg.setStart(new Date((c.getLong(c.getColumnIndex(EpgTbl.START)))));
			epg.setEnd(new Date((c.getLong(c.getColumnIndex(EpgTbl.END)))));
			epg.setTitle(c.getString(c.getColumnIndex(EpgTbl.TITLE)));
			epg.setSubTitle(c.getString(c.getColumnIndex(EpgTbl.SUBTITLE)));
			epg.setDescription(c.getString(c.getColumnIndex(EpgTbl.DESC)));
			result.add(epg);
		}
		c.close();
		db.close();
		if (result.size() <= 0) {
			result = null;
		}
		return result;
	}

	/**
	 * Gets the channellist.
	 * 
	 * @return the channellist
	 * 
	 * @author RayBa
	 * @date 20.06.2010
	 * @description Gets the channellist.
	 */
	public List<Channel> loadFavourites() {
		SQLiteDatabase db = getWritableDatabase();
		List<Channel> result = new ArrayList<Channel>();
		Cursor c = db.rawQuery("SELECT * FROM " + ChannelTbl.TABLE_NAME + " WHERE " + ChannelTbl.FLAGS + " & " + Channel.FLAG_FAV + "!= 0 ORDER BY " + ChannelTbl.FAV_POSITION, null);
		while (c.moveToNext()) {
			Channel channel = new Channel();
			channel.setId(c.getLong(c.getColumnIndex(ChannelTbl._ID)));
			channel.setEpgID(c.getLong(c.getColumnIndex(ChannelTbl.EPG_ID)));
			String name = c.getString(c.getColumnIndex(ChannelTbl.NAME));
			name = name.replaceAll("\\([^\\(]*\\)", "").trim();
			channel.setName(name);
			channel.setPosition(c.getInt(c.getColumnIndex(ChannelTbl.FAV_POSITION)));
			result.add(channel);
		}
		c.close();
		db.close();
		if (result.size() <= 0) {
			result = null;
		}
		return result;
	}

	/**
	 * Save epg entries.
	 * 
	 * @param channels
	 *            the channels
	 * @author RayBa
	 * @date 26.04.2012
	 */
	public void saveChannels(List<Channel> channels) {
		if (channels == null || channels.size() <= 0) {
			return;
		}
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		db.execSQL("DELETE FROM " + ChannelTbl.TABLE_NAME);
		try {
			for (Channel channel : channels) {
				db.insert(ChannelTbl.TABLE_NAME, null, channel.toContentValues());
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
			db.close();
		}
	}

	/**
	 * Save epg.
	 *
	 * @param epgEntries the epg entries
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public void saveEPG(List<EpgEntry> epgEntries) {
		if (epgEntries == null || epgEntries.size() <= 0) {
			return;
		}
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		// db.execSQL("DELETE FROM " + ChannelTbl.TABLE_NAME);
		try {
			for (EpgEntry epgEntrie : epgEntries) {
				db.insert(EpgTbl.TABLE_NAME, null, epgEntrie.toContentValues());
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
			db.close();
		}
	}
	
	/**
	 * Save now playing.
	 *
	 * @param epgEntries the epg entries
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public void saveNowPlaying(List<EpgEntry> epgEntries) {
		if (epgEntries == null || epgEntries.size() <= 0) {
			return;
		}
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		 db.execSQL("DELETE FROM " + NowTbl.TABLE_NAME);
		try {
			for (EpgEntry epgEntrie : epgEntries) {
				db.insert(NowTbl.TABLE_NAME, null, epgEntrie.toContentValues());
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
			db.close();
		}
	}

	/**
	 * Delete epg.
	 *
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public void deleteEPG() {
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL("DELETE FROM " + EpgTbl.TABLE_NAME);
		db.close();
	}

	/**
	 * Delete epg for channel.
	 *
	 * @param epgId the epg id
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public void deleteEpgForChannel(long epgId) {
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL("DELETE FROM " + EpgTbl.TABLE_NAME + WHERE + EpgTbl.EPG_ID + EQUALS + epgId);
		db.close();
	}

	/**
	 * Mark channels for update.
	 *
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public void markChannelsForUpdate() {
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL("update " + ChannelTbl.TABLE_NAME + " set " + ChannelTbl.FLAGS + " = " + ChannelTbl.FLAGS + " | " + Channel.FLAG_PENDING_UPDATE + ";");
		db.close();
	}

	/**
	 * Mark channel updated.
	 *
	 * @param channelId the channel id
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public void markChannelUpdated(long channelId) {
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL("update " + ChannelTbl.TABLE_NAME + " set " + ChannelTbl.FLAGS + " = " + ChannelTbl.FLAGS + " &~ " + Channel.FLAG_PENDING_UPDATE + WHERE + ChannelTbl._ID + EQUALS + channelId + ";");
		db.close();
	}

	/**
	 * Save favs.
	 *
	 * @param favs the favs
	 * @author RayBa
	 * @date 26.04.2012
	 */
	public void saveFavs(List<Fav> favs) {
		if (favs == null || favs.size() <= 0) {
			return;
		}
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		try {
			for (Fav fav : favs) {
				db.execSQL("update " + ChannelTbl.TABLE_NAME + " set " + ChannelTbl.FAV_POSITION + " = " + fav.position + ", " + ChannelTbl.FLAGS + " = " + ChannelTbl.FLAGS + " | " + Channel.FLAG_FAV + " where " + ChannelTbl._ID + " = '" + fav.id + "';");
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
			db.close();
		}
	}
}
