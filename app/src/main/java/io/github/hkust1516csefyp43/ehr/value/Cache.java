package io.github.hkust1516csefyp43.ehr.value;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.util.List;

import io.github.hkust1516csefyp43.ehr.pojo.server_response.v1.Patient;
import io.github.hkust1516csefyp43.ehr.pojo.server_response.v1.User;
import io.github.hkust1516csefyp43.ehr.pojo.server_response.v2.BloodType;
import io.github.hkust1516csefyp43.ehr.pojo.server_response.v2.Clinic;

/**
 * Created by Louis on 5/11/15.
 */

public class Cache {

    //==============================<Currently>==================================

    public static void setUser(Context context, User user) {
        Gson gson = new GsonBuilder().create();
        String jsonString = gson.toJson(user);
        SharedPreferences prefs = context.getSharedPreferences(Const.KEY_SHARE_PREFERENCES, Context.MODE_PRIVATE);
        prefs.edit().putString(Const.KEY_CURRENT_USER, jsonString).apply();
    }

    public static User getUser(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Const.KEY_SHARE_PREFERENCES, Context.MODE_PRIVATE);
        String value = prefs.getString(Const.KEY_CURRENT_USER, null);
        if (value != null) {
            return new Gson().fromJson(value, User.class);
        } else {
            return null;
        }
    }

    public static void clearUser(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Const.KEY_SHARE_PREFERENCES, Context.MODE_PRIVATE);
        prefs.edit().remove(Const.KEY_CURRENT_USER).apply();
    }

    public static void setCurrentClinicId(Context context) {
        //TODO save id only (to put in back to each api)
    }

    //==============================</Currently>==================================

    //The basic functions: get, set and delete
    /**
     * @param context can be fragment, activity, application, etc
     * @param key     is the string on how & where to find
     * @return
     */
    private static List<Patient> getPatients(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences(Const.KEY_SHARE_PREFERENCES, Context.MODE_PRIVATE);
        String value = prefs.getString(key, null);
        if (value != null) {
            try {
                List<Patient> lp = new Gson().fromJson(value, new TypeToken<List<Patient>>() {
                }.getType());
                return lp;
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                Log.d("qqq131", value);
                //TODO for some reason it often got incorrect syntax stuff -_-
            }
        }
        return null;
    }

    private static void setPatients(Context context, String key, List<Patient> patients) {
        Gson gson = new GsonBuilder().create();
        String jsonString = gson.toJson(patients);
        SharedPreferences prefs = context.getSharedPreferences(Const.KEY_SHARE_PREFERENCES, Context.MODE_PRIVATE);
        prefs.edit().putString(key, jsonString).apply();
    }

    private static void clearPatients(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences(Const.KEY_SHARE_PREFERENCES, Context.MODE_PRIVATE);
        prefs.edit().remove(key).apply();
    }

    //------------------------- separation line ----------------------------------

    /**
     * @param context
     * @return
     */
    public static List<Patient> getPostTriagePatients(Context context) {
        return getPatients(context, Const.KEY_POST_TRIAGE_PATIENTS);
    }

    public static void setPostTriagePatients(Context context, List<Patient> postTriagePatients) {
        setPatients(context, Const.KEY_POST_TRIAGE_PATIENTS, postTriagePatients);
    }

    public static void clearPostTriagePatients(Context context) {
        clearPatients(context, Const.KEY_POST_TRIAGE_PATIENTS);
    }

    public static List<Patient> getPostPharmacyPatients(Context context) {
        return getPatients(context, Const.KEY_POST_PHARMACY_PATIENTS);
    }

    public static void setPostPharmacyPatients(Context context, List<Patient> postPharmacyPatients) {
        setPatients(context, Const.KEY_POST_PHARMACY_PATIENTS, postPharmacyPatients);
    }

    public static void clearPostPharmacyPatients(Context context) {
        clearPatients(context, Const.KEY_POST_PHARMACY_PATIENTS);
    }

    //==============================<Current patient>==================================
    //TODO use onSaveInstanceState and onActivityCreated to save data


    //==============================</Current patient>==================================

    //==============================<Connection config>==================================

    public static void setConfig(Context context, JSONObject json) {
        SharedPreferences prefs = context.getSharedPreferences(Const.KEY_SHARE_PREFERENCES, Context.MODE_PRIVATE);
        prefs.edit().putString(Const.KEY_VIRGIN, json.toString()).apply();
    }

    public static String getConfig(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Const.KEY_SHARE_PREFERENCES, Context.MODE_PRIVATE);
        return prefs.getString(Const.KEY_VIRGIN, null);
    }

    public static void clearConfig(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Const.KEY_SHARE_PREFERENCES, Context.MODE_PRIVATE);
        prefs.edit().remove(Const.KEY_VIRGIN).apply();
    }

    //==============================</Connection config>==================================

    public static void setEmergencyFix(Context context, int i) {
        SharedPreferences prefs = context.getSharedPreferences(Const.KEY_SHARE_PREFERENCES, Context.MODE_PRIVATE);
        prefs.edit().putInt(Const.KEY_EMERGENCY_FIX, i).apply();
    }

    public static int getEmergencyFix(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Const.KEY_SHARE_PREFERENCES, Context.MODE_PRIVATE);
        return prefs.getInt(Const.KEY_EMERGENCY_FIX, 0);
    }

    //==============================<Static stuff>==================================

    public static void setClinics(Context context, List<Clinic> clinics) {
        Gson gson = new GsonBuilder().create();
        String jsonString = gson.toJson(clinics);
        SharedPreferences prefs = context.getSharedPreferences(Const.KEY_SHARE_PREFERENCES, Context.MODE_PRIVATE);
        prefs.edit().putString(Const.KEY_CLINICS, jsonString).apply();
    }

    private static List<Clinic> getClinics(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Const.KEY_SHARE_PREFERENCES, Context.MODE_PRIVATE);
        String value = prefs.getString(Const.KEY_CLINICS, null);
        if (value != null) {
            try {
                List<Clinic> lp = new Gson().fromJson(value, new TypeToken<List<Clinic>>() {
                }.getType());
                return lp;
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                Log.d("qqq131", value);
                //TODO for some reason it occasionally got incorrect syntax stuff -_-
            }
        }
        return null;
    }

    public static void setBloodTypes(Context context, List<BloodType> bt) {
        Gson gson = new GsonBuilder().create();
        String jsonString = gson.toJson(bt);
        SharedPreferences prefs = context.getSharedPreferences(Const.KEY_SHARE_PREFERENCES, Context.MODE_PRIVATE);
        prefs.edit().putString(Const.KEY_BLOOD_TYPES, jsonString).apply();
    }

    private static List<BloodType> getBloodTypes(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Const.KEY_SHARE_PREFERENCES, Context.MODE_PRIVATE);
        String value = prefs.getString(Const.KEY_BLOOD_TYPES, null);
        if (value != null) {
            try {
                List<BloodType> lp = new Gson().fromJson(value, new TypeToken<List<BloodType>>() {
                }.getType());
                return lp;
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                Log.d("qqq131", value);
                //TODO for some reason it occasionally got incorrect syntax stuff -_-
            }
        }
        return null;
    }

    //==============================</Static stuff>==================================

}
