package edu.ucla.cens.audiosens.sqlite;

import java.util.ArrayList;
import java.util.List;

import edu.ucla.cens.audiosens.helper.Logger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper 
{
	private static final int DATABASE_VERSION = 2;
	private static final String DATABASE_NAME = "audioSens.db";
	private static final String TABLE_INFERENCE = "inference";

	// Contacts Table Columns names
	private static final String KEY_ID = "id";
	private static final String KEY_VERSION = "version";
	private static final String KEY_DATA = "data";
	private static final String KEY_INFERENCE = "inference";
	private static final String KEY_PERIOD = "period";
	private static final String KEY_DURATION = "duration";

	public DatabaseHelper(Context context) 
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_INFERENCE_TABLE = "CREATE TABLE " + TABLE_INFERENCE 
				+ "("
				+ KEY_ID + " INTEGER PRIMARY KEY," 
				+ KEY_VERSION + " TEXT,"
				+ KEY_DATA + " TEXT,"
				+ KEY_INFERENCE + " INTEGER,"
				+ KEY_PERIOD + " INTEGER,"
				+ KEY_DURATION + " INTEGER" + ")";
		db.execSQL(CREATE_INFERENCE_TABLE);
	}

	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_INFERENCE);

		// Create tables again
		onCreate(db);
	}

	/**
	 * All CRUD(Create, Read, Update, Delete) Operations
	 */

	// Adding new contact
	public void addInference(SpeechInferenceObject obj) 
	{
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_ID, obj.getId());
		values.put(KEY_VERSION, obj.getVersion());
		values.put(KEY_DATA, obj.getData());
		values.put(KEY_INFERENCE, obj.getInference());
		values.put(KEY_PERIOD, obj.getPeriod());
		values.put(KEY_DURATION, obj.getDuration());

		Logger.w("Inserting:"+values.toString());
		
		// Inserting Row
		db.insert(TABLE_INFERENCE, null, values);
		db.close(); // Closing database connection
	}

	public List<SpeechInferenceObject> getAllInferences() 
	{
		List<SpeechInferenceObject> inferenceList = new ArrayList<SpeechInferenceObject>();
		String selectQuery = "SELECT  * FROM " + TABLE_INFERENCE;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) 
		{
			do 
			{
				SpeechInferenceObject inferenceObject = new SpeechInferenceObject();
				inferenceObject.setId(Long.parseLong(cursor.getString(0)));
				inferenceObject.setVersion(cursor.getString(1));
				inferenceObject.setData(cursor.getString(2));
				inferenceObject.setInference(Integer.parseInt(cursor.getString(3)));
				inferenceObject.setPeriod(Integer.parseInt(cursor.getString(4)));
				inferenceObject.setDuration(Integer.parseInt(cursor.getString(5)));
				inferenceList.add(inferenceObject);
			} while (cursor.moveToNext());
		}

		cursor.close();
		return inferenceList;
	}
	
	public List<SpeechInferenceObject> getIntervalInferences(long start, long end) 
	{
		List<SpeechInferenceObject> inferenceList = new ArrayList<SpeechInferenceObject>();
		String selectQuery = "SELECT  * FROM " + TABLE_INFERENCE 
							+ " WHERE (" + KEY_ID + " > " + start + " AND "
							+ KEY_ID + " < " + end + ")";

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) 
		{
			do 
			{
				SpeechInferenceObject inferenceObject = new SpeechInferenceObject();
				inferenceObject.setId(Long.parseLong(cursor.getString(0)));
				inferenceObject.setVersion(cursor.getString(1));
				inferenceObject.setData(cursor.getString(2));
				inferenceObject.setInference(Integer.parseInt(cursor.getString(3)));
				inferenceObject.setPeriod(Integer.parseInt(cursor.getString(4)));
				inferenceObject.setDuration(Integer.parseInt(cursor.getString(5)));
				inferenceList.add(inferenceObject);
			} while (cursor.moveToNext());
		}

		return inferenceList;
	}


	// Getting Inference Count
	public int getInferenceCount() 
	{
		String countQuery = "SELECT  * FROM " + TABLE_INFERENCE;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		cursor.close();

		// return count
		return cursor.getCount();
	}

}