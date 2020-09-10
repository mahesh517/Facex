package com.app.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import org.json.JSONObject;

import java.util.Set;


public class LoginPrefManager {

    private final Context _context;

    private static android.app.AlertDialog.Builder alertDialogBuilder = null;
    private static android.app.AlertDialog networkAlertDialog = null;

    Set<String> fav_list;
    private SharedPreferences pref;
    private Editor editor;

    private static final String PREF_NAME = "AndroidCustomerPracleoPref";
    private static final String IS_LOGIN = "IsLoggedIn";
    private static final String Check_login = "0";
    private static JSONObject jsonObject;

    private String pickupaddress_latitude;

    public LoginPrefManager(Context context) {
        this._context = context;
        int PRIVATE_MODE = 0;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
        editor.apply();
    }

    public void setLoginPrefData(String text, String data) {
        editor.putString(text, data).commit();
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(Check_login, "1").commit();
        editor.commit();
    }

    public void setPrefData(String text, String data) {
        editor.putString(text, data).commit();
        editor.commit();
    }

    public String getPrefData(String text) {
        return pref.getString(text, "");
    }

    public Editor getEditor() {
        return editor;
    }

    public SharedPreferences getShaPref() {
        return pref;
    }

    public void setEditor(Editor editor) {
        this.editor = editor;
    }

    public void setMyPref(SharedPreferences pref) {
        this.pref = pref;
    }

    public void setIntValue(String keyName, int value) {
        pref.edit().putInt(keyName, value).apply();
    }

    public int getIntValue(String keyName) {
        return pref.getInt(keyName, 0);
    }

    public void setStringValue(String keyName, String value) {
        pref.edit().putString(keyName, value).apply();

    }

    public String getStringValue(String keyName) {

        return pref.getString(keyName, "");

    }


    public void setLimittime(Long userId) {
        pref.edit().putLong("limit_end", userId).apply();
    }

    public Long getLastTime() {
        return pref.getLong("limit_end", 0);
    }

}
