package com.chalkdigital.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.lang.reflect.Type;

import static android.content.Context.MODE_PRIVATE;

public final class SharedPreferencesHelper {
    public static final String DEFAULT_PREFERENCE_NAME = "cdadsSettings";

    private SharedPreferencesHelper() {}
    
    public static SharedPreferences getSharedPreferences(@NonNull final Context context) {
        Preconditions.checkNotNull(context);

        return context.getSharedPreferences(DEFAULT_PREFERENCE_NAME, MODE_PRIVATE);
    }

    public static SharedPreferences getSharedPreferences(
            @NonNull final Context context, @NonNull final String preferenceName) {
        Preconditions.checkNotNull(context);
        Preconditions.checkNotNull(preferenceName);

        return context.getSharedPreferences(preferenceName, MODE_PRIVATE);
    }

    public static SharedPreferences.Editor getSharedPreferencesEditor(Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        return preferences.edit();
    }

    public static void putStringToSharedPreferences(String key, String value, Context context) {
        SharedPreferences.Editor editor = getSharedPreferencesEditor(context);
        editor.putString(key, value);
        editor.commit();
    }

    public static void removeFromSharedPreferences(String key, Context context) {
        SharedPreferences.Editor editor = getSharedPreferencesEditor(context);
        editor.remove(key);
        editor.commit();
    }

    public static void putObjectToSharedPreferences(String key, Object object, Context context) {
        if (object instanceof Location){
            object = new CDAdLocation((Location) object);
        }
        SharedPreferences.Editor editor = getSharedPreferencesEditor(context);
        editor.putString(key, new Gson().toJson(object));
        editor.commit();
    }

    public static void putBooleanToSharedPreferences(String key, Boolean value, Context context) {
        SharedPreferences.Editor editor = getSharedPreferencesEditor(context);
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static void putIntegerToSharedPreferences(String key, Integer value, Context context) {
        SharedPreferences.Editor editor = getSharedPreferencesEditor(context);
        editor.putInt(key, value);
        editor.commit();
    }


    public static void putLongToSharedPreferences(String key, long value, Context context) {
        SharedPreferences.Editor editor = getSharedPreferencesEditor(context);
        editor.putLong(key, value);
        editor.commit();
    }

    public static void putFloatToSharedPreferences(String key, float value, Context context) {
        SharedPreferences.Editor editor = getSharedPreferencesEditor(context);
        editor.putFloat(key, value);
        editor.commit();
    }


    public static String getStringFromSharedPreferences(String key, String defaultValue, Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        return preferences.getString(key, defaultValue);
    }

    public static <T> T getObjectFromSharedPreferences(String key, Type typeOfT, Context context) {
        if (isKeyExistsInSharedPreferences(key, context)){
            SharedPreferences preferences = getSharedPreferences(context);
            if (typeOfT == Location.class) {
                typeOfT = CDAdLocation.class;
                return (T)((CDAdLocation) new Gson().fromJson(preferences.getString(key, ""), typeOfT)).toLocation();
            }
            return new Gson().fromJson(preferences.getString(key, ""), typeOfT);
        }
        else return null;
    }

    public static Boolean isKeyExistsInSharedPreferences(String key, Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        return preferences.contains(key);
    }

    public static Boolean getBooleanFromSharedPreferences(String key, Boolean defaultValue, Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        return preferences.getBoolean(key, defaultValue);
    }

    public static Integer getIntegerFromSharedPreferences(String key, Integer defaultValue, Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        return preferences.getInt(key, defaultValue);
    }

    public static Long getLongFromSharedPreferences(String key, long defaultValue, Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        return preferences.getLong(key, defaultValue);
    }

    public static float getFloatFromSharedPreferences(String key, float defaultValue, Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        return preferences.getFloat(key, defaultValue);
    }
}
