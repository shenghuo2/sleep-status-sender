package cn.shenghuo2.ctf.sleep_status_sender;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        private static final String TAG = "SettingsFragment";
        private RequestQueue requestQueue;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);

            requestQueue = Volley.newRequestQueue(getActivity());

            EditTextPreference baseUrlPreference = findPreference("base_url");
            if (baseUrlPreference != null) {
                baseUrlPreference.setSummary(baseUrlPreference.getText());
                baseUrlPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        String url = newValue.toString();
                        if (!url.startsWith("https://")) {
                            Toast.makeText(getActivity(), "For security reasons, please use HTTPS.", Toast.LENGTH_LONG).show();
                            return false;
                        }
                        if (!url.endsWith("/")) {
                            url += "/";
                        }
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                        editor.putString("base_url", url);
                        editor.apply();
                        preference.setSummary(url);
                        return true;
                    }
                });
            }

            Preference testButton = findPreference("test_connection");
            if (testButton != null) {
                testButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        String baseUrl = sharedPreferences.getString("base_url", "https://example.com/");
                        String url = baseUrl + "status";

                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                                    @Override
                                    public void onResponse(JSONObject response) {
                                        Log.d(TAG, "Response: " + response.toString());
                                        Toast.makeText(getActivity(), "Connection successful: " + response.toString(), Toast.LENGTH_LONG).show();
                                    }
                                }, new Response.ErrorListener() {

                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.e(TAG, "Request error: " + error.getMessage());
                                        Toast.makeText(getActivity(), "Connection failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });

                        requestQueue.add(jsonObjectRequest);
                        return true;
                    }
                });
            }
        }
    }
}
