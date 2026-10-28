
package com.example.lostandfoundlist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "LostFound.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "items";
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_LOCATION = "location";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_STATUS = "status";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT NOT NULL, " +
                COL_LOCATION + " TEXT, " +
                COL_DESCRIPTION + " TEXT, " +
                COL_STATUS + " TEXT DEFAULT 'Lost')";

        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,
                          int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public long addItem(String name, String location,
                        String description) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_LOCATION, location);
        values.put(COL_DESCRIPTION, description);
        values.put(COL_STATUS, "Lost");

        return db.insert(TABLE_NAME, null, values);
    }

    public ArrayList<Item> getItems(String searchText) {
        ArrayList<Item> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        String selection = null;
        String[] selectionArgs = null;

        if (searchText != null && !searchText.trim().isEmpty()) {
            selection = COL_NAME + " LIKE ? OR " +
                    COL_LOCATION + " LIKE ?";
            String search = "%" + searchText.trim() + "%";
            selectionArgs = new String[]{search, search};
        }

        Cursor cursor = db.query(
                TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                COL_ID + " DESC"
        );

        try {
            while (cursor.moveToNext()) {
                Item item = new Item(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_LOCATION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS))
                );

                items.add(item);
            }
        } finally {
            cursor.close();
        }

        return items;
    }

    public boolean markAsFound(int id) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_STATUS, "Found");

        return db.update(
                TABLE_NAME,
                values,
                COL_ID + " = ?",
                new String[]{String.valueOf(id)}
        ) > 0;
    }

    public boolean deleteItem(int id) {
        SQLiteDatabase db = getWritableDatabase();

        return db.delete(
                TABLE_NAME,
                COL_ID + " = ?",
                new String[]{String.valueOf(id)}
        ) > 0;
    }

    public static class Item {
        public int id;
        public String name;
        public String location;
        public String description;
        public String status;

        public Item(int id, String name, String location,
                    String description, String status) {
            this.id = id;
            this.name = name;
            this.location = location;
            this.description = description;
            this.status = status;
        }
    }
}
