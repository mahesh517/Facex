package com.app.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


import com.app.Utils.Users;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "FACEX";
    private static final String TABLE_USERS = "users";
    private static final String TABLE_SETTINGS = "settings";
    private static final String TABLE_IMAGES = "user_images";
    private static final String TABLE_TIMES = "user_times";
    private static final String TABLE_VISITOR = "visitor";
    private static final String TABLE_LIMIT = "limit";
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMP_ID = "empid";
    private static final String KEY_EMP_IMAGE_URL = "image_url";
    private static final String KEY_DIRTY = "dirty";
    private static final String KEY_VECTOR = "vector";
    private static final String KEY_PHOTO = "userphoto";
    private static final String KEY_COUNT = "search_count";
    private static final String KEY_DATE = "date";
    private static final String KEY_STATUS = "status";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "(" + KEY_ID + " INTEGER PRIMARY KEY  AUTOINCREMENT," + KEY_NAME + " TEXT," + KEY_EMP_ID + " TEXT," + KEY_EMP_IMAGE_URL + " TEXT," + KEY_DIRTY + " TEXT" + ")";
        String CREATE_SETTINGS_TABLE = "CREATE TABLE " + TABLE_SETTINGS + "(" + KEY_ID + " INTEGER PRIMARY KEY  AUTOINCREMENT," + KEY_COUNT + " TEXT" + ")";
        String CREATE_IMAGES_TABLE = "CREATE TABLE " + TABLE_IMAGES + "(" + KEY_ID + " INTEGER PRIMARY KEY  AUTOINCREMENT," + KEY_EMP_ID + " TEXT," + KEY_VECTOR + " TEXT," + KEY_PHOTO + " TEXT " + ")";
        String CREATE_USERS_STATUS_TABLE = "CREATE TABLE " + TABLE_TIMES + "(" + KEY_ID + " INTEGER PRIMARY KEY  AUTOINCREMENT," + KEY_EMP_ID + " TEXT," + KEY_STATUS + " TEXT," + KEY_DATE + " TEXT ," + KEY_PHOTO + " TEXT " + ")";
        String CREATE_VISITOR_STATUS_TABLE = "CREATE TABLE " + TABLE_VISITOR + "(" + KEY_ID + " INTEGER PRIMARY KEY  AUTOINCREMENT," + KEY_DATE + " TEXT ," + KEY_PHOTO + " TEXT " + ")";

//        Log.e("create", CREATE_SETTINGS_TABLE);
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_SETTINGS_TABLE);
        db.execSQL(CREATE_IMAGES_TABLE);
        db.execSQL(CREATE_USERS_STATUS_TABLE);
        db.execSQL(CREATE_VISITOR_STATUS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);

        // Create tables again
        onCreate(db);
    }


    public void DeleteUsers() {

        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_IMAGES);

        // Create tables again

        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "(" + KEY_ID + " INTEGER PRIMARY KEY  AUTOINCREMENT," + KEY_NAME + " TEXT," + KEY_EMP_ID + " TEXT," + KEY_EMP_IMAGE_URL + " TEXT," + KEY_DIRTY + " TEXT" + ")";
        String CREATE_IMAGES_TABLE = "CREATE TABLE " + TABLE_IMAGES + "(" + KEY_ID + " INTEGER PRIMARY KEY  AUTOINCREMENT," + KEY_EMP_ID + " TEXT," + KEY_VECTOR + " TEXT," + KEY_PHOTO + " TEXT " + ")";
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_IMAGES_TABLE);


    }

    // code to add the new contact
    public void addUser(Users users) {
        SQLiteDatabase db = this.getWritableDatabase();


        ContentValues values = new ContentValues();
        values.put(KEY_NAME, users.getName());
        values.put(KEY_EMP_ID, users.getEmpid());
        values.put(KEY_EMP_IMAGE_URL, users.getImage_url());
        values.put(KEY_DIRTY, users.getDirty());
        db.insert(TABLE_USERS, null, values);
        db.close();
    }

    public void addUserTime(Users users) {
        SQLiteDatabase db = this.getWritableDatabase();


        ContentValues values = new ContentValues();
        values.put(KEY_EMP_ID, users.getEmpid());
        values.put(KEY_STATUS, users.getStatus());
        values.put(KEY_DATE, users.getTime());
        values.put(KEY_PHOTO, users.getUserphoto());

        db.insert(TABLE_TIMES, null, values);
        db.close();
    }

    public void addVisitor(Users users) {
        SQLiteDatabase db = this.getWritableDatabase();


        ContentValues values = new ContentValues();

        values.put(KEY_DATE, users.getTime());
        values.put(KEY_PHOTO, users.getUserphoto());

        db.insert(TABLE_VISITOR, null, values);
        db.close();
    }

    public void addUserPhoto(Users users) {
        SQLiteDatabase db = this.getWritableDatabase();


        ContentValues values = new ContentValues();
        values.put(KEY_EMP_ID, users.getEmpid());
        values.put(KEY_PHOTO, users.getUserphoto());
        values.put(KEY_VECTOR, users.getVector());

        // Inserting Row
        db.insert(TABLE_IMAGES, null, values);
        db.close();
    }


    public void addSearch(String count) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_COUNT, count);
        db.insert(TABLE_SETTINGS, null, contentValues);
        db.close();
    }


    public List<Users> getAllContacts() {
        List<Users> contactList = new ArrayList<Users>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_USERS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);


        if (cursor != null && cursor.moveToFirst()) {
            do {
                Users contact = new Users();
                contact.setName(cursor.getString(1));
                contact.setEmpid(cursor.getString(2));

                // Adding contact to list
                contactList.add(contact);
            } while (cursor.moveToNext());
        }

        // return contact list
        return contactList;
    }

    public List<Users> getVisitors() {
        List<Users> contactList = new ArrayList<Users>();
        String selectQuery = "SELECT  * FROM " + TABLE_VISITOR;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);


        if (cursor != null && cursor.moveToFirst()) {
            do {
                Users contact = new Users();
                contact.setId(cursor.getString(0));
                contact.setTime(cursor.getString(1));
                contact.setUserphoto(cursor.getString(2));

                // Adding contact to list
                contactList.add(contact);
            } while (cursor.moveToNext());
        }

        // return contact list
        return contactList;
    }

    public List<Users> getAllStatus() {
        List<Users> contactList = new ArrayList<Users>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_TIMES;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);


        // looping through all rows and adding to list
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Users contact = new Users();
                contact.setEmpid(cursor.getString(1));
                contact.setStatus(cursor.getString(2));
                contact.setTime(cursor.getString(3));
                contact.setUserphoto(cursor.getString(4));

                // Adding contact to list
                contactList.add(contact);
            } while (cursor.moveToNext());
        }

        // return contact list
        return contactList;
    }

    public List<Users> getUSerVectors() {
        List<Users> contactList = new ArrayList<Users>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_IMAGES;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);


        // looping through all rows and adding to list
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Users contact = new Users();

//                Log.e("cusros0", "--" + cursor.getString(1));

                if (cursor.getString(1) != null) {
                    contact.setEmpid(String.valueOf(cursor.getString(1)));
                }
                contact.setVector(cursor.getString(2));
                contact.setUserphoto(cursor.getString(3));

                // Adding contact to list
                contactList.add(contact);
            } while (cursor.moveToNext());
        }

        // return contact list
        return contactList;
    }

    public List<Users> getUserImages(String emp_id) {
        List<Users> contactList = new ArrayList<Users>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_IMAGES + " WHERE " + KEY_EMP_ID + "='" + emp_id + "'";

        Log.e("select_query", selectQuery);

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);


        // looping through all rows and adding to list
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Users contact = new Users();

//                Log.e("cusros0", "--" + cursor.getString(1));

                contact.setId(String.valueOf(cursor.getString(0)));
                if (cursor.getString(1) != null) {
                    contact.setEmpid(String.valueOf(cursor.getString(1)));
                }
                contact.setVector(cursor.getString(2));
                contact.setUserphoto(cursor.getString(3));

                // Adding contact to list
                contactList.add(contact);
            } while (cursor.moveToNext());
        }

        // return contact list
        return contactList;
    }


    public Users getUser(String emp_id) {


        String selectQuery = "SELECT  * FROM " + TABLE_IMAGES + " WHERE " + KEY_EMP_ID + "='" + emp_id + "'";

//        Log.e("selectQuery", selectQuery);

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        Users contact = new Users();
        // looping through all rows and adding to list
        if (cursor != null && cursor.moveToFirst()) {
            do {


                contact.setVector(cursor.getString(2));
                contact.setUserphoto(cursor.getString(3));

                // Adding contact to list

            } while (cursor.moveToNext());
        }

        return contact;

    }

    public Users getLastTime(String EMP_ID, String TIME_START, String TIME_END) {


        String selectQuery = "SELECT  * FROM " + TABLE_TIMES + " WHERE " + KEY_DATE + " BETWEEN " + "'" + TIME_START + "'" + " AND " + "'" + TIME_END + "'" + " AND " + KEY_EMP_ID + "='" + EMP_ID + "'";


        Log.e("selecte", selectQuery);

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        Users contact = new Users();
        if (cursor != null && cursor.moveToFirst()) {
            do {


                contact.setStatus(cursor.getString(2));
                contact.setTime(cursor.getString(3));

            } while (cursor.moveToNext());
        }

        return contact;

    }

    public Users getUserDetails(String emp_code) {

        String selectQuery = "SELECT  * FROM " + TABLE_USERS + " WHERE " + KEY_EMP_ID + "='" + emp_code + "'";

//        Log.e("query", selectQuery);

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        Users contact = new Users();
        // looping through all rows and adding to list
        if (cursor != null && cursor.moveToFirst()) {
            do {


                contact.setName(cursor.getString(1));

            } while (cursor.moveToNext());
        }

        return contact;

    }


    public String getKeyEmpImageUrl(String emp_code) {

        String selectQuery = "SELECT  * FROM " + TABLE_USERS + " WHERE " + KEY_EMP_ID + "='" + emp_code + "'";

//        Log.e("query", selectQuery);

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        String image_url = null;

        Users contact = new Users();
        // looping through all rows and adding to list
        if (cursor != null && cursor.moveToFirst()) {
            do {


                image_url = cursor.getString(3);

            } while (cursor.moveToNext());
        }

        return image_url;

    }


    public String fetchdatabyfilter(String inputText) {

        String name = null;
        String query = "SELECT " + KEY_NAME + " FROM " + DatabaseHelper.TABLE_USERS + " WHERE " + KEY_EMP_ID + " = '" + inputText + "' ";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                if (cursor != null) {
                    name = cursor.getString(0);
                }
            } while (cursor.moveToNext());
        }
        return name;
    }


    public int fetchSearchCount() {
        int count = 0;

        String query = "SELECT " + KEY_COUNT + " FROM " + DatabaseHelper.TABLE_SETTINGS;


        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                if (cursor != null) {
                    String count_db = cursor.getString(0);
                    count = Integer.parseInt(count_db);
                }
            } while (cursor.moveToNext());
        }


        return count;
    }

    public boolean deleteTitle(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_USERS, KEY_EMP_ID + "=?", new String[]{name}) > 0;
    }

    public boolean deleteImages(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_IMAGES, KEY_EMP_ID + "=?", new String[]{name}) > 0;
    }

    public void deleteDuplicateImages(String code, String id) {
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "DELETE FROM " + TABLE_IMAGES + " WHERE " + KEY_EMP_ID + "= '" + code + "'" + " AND " + KEY_ID + "= '" + id + "'";

        Log.e("delete_query", query);

        db.execSQL("DELETE FROM " + TABLE_IMAGES + " WHERE " + KEY_EMP_ID + "= '" + code + "'" + " AND " + KEY_ID + "= '" + id + "'");

//        return db.delete(TABLE_IMAGES, KEY_EMP_ID + "=?" + " AND " + KEY_ID + "=?", new String[]{code, id}) > 0;
    }

    public boolean updatenamedetails(String name, String old_code) {
        SQLiteDatabase mDb = this.getWritableDatabase();
        ContentValues args = new ContentValues();
        args.put(KEY_NAME, name);
        return mDb.update(TABLE_USERS, args, KEY_EMP_ID + "=?", new String[]{old_code}) > 0;
    }

    public boolean updatecount(int count, String id) {
        SQLiteDatabase mDb = this.getWritableDatabase();
        ContentValues args = new ContentValues();
        args.put(KEY_COUNT, count);
        return mDb.update(TABLE_SETTINGS, args, KEY_ID + "=?", new String[]{id}) > 0;
    }

    public boolean updateempcode(String name, String old_code, String new_code) {
        SQLiteDatabase mDb = this.getWritableDatabase();
        ContentValues args = new ContentValues();
        args.put(KEY_NAME, name);
        args.put(KEY_EMP_ID, new_code);
        return mDb.update(TABLE_USERS, args, KEY_EMP_ID + "=?", new String[]{old_code}) > 0;
    }

    public boolean updateempcode(String old_code, String new_code) {
        SQLiteDatabase mDb = this.getWritableDatabase();
        ContentValues args = new ContentValues();
        args.put(KEY_EMP_ID, new_code);
        return mDb.update(TABLE_USERS, args, KEY_EMP_ID + "=?", new String[]{old_code}) > 0;
    }
    public boolean updateuserImage(String emp_code, String url) {
        SQLiteDatabase mDb = this.getWritableDatabase();
        ContentValues args = new ContentValues();
        args.put(KEY_EMP_IMAGE_URL, url);
        return mDb.update(TABLE_USERS, args, KEY_EMP_ID + "=?", new String[]{emp_code}) > 0;
    }


}