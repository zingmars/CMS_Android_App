package me.zingmars.dankpressandroid;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {
    static Utilities utils = new Utilities();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        App.createMenu(this, 4);
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName) || AccountPreferenceFragment.class.getName().equals(fragmentName) || AppPreferencesFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment allows the user to both log in and log out.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class AccountPreferenceFragment extends PreferenceFragment {
         @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_login);
            final Activity activity = super.getActivity();

            //Check if user has logged in and disable the other button.
            //Login button
            final Preference login = findPreference("settings_login");
            final Preference logout = findPreference("settings_logout");
            if(App.loginState()) {
                login.setEnabled(false);
                logout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        logout.setTitle("Please wait!");
                        Thread logoutThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                utils.logout();
                                Looper.prepare();
                                Toast toast = Toast.makeText(App.getAppContext(), "Logout successful!", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        });
                        logoutThread.start();
                        Intent mainActivity = new Intent(getActivity(), MainActivity.class);
                        startActivity(mainActivity);
                        activity.finish();
                        return true;
                    }
                });
            } else {
                logout.setEnabled(false);
                login.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        startActivity(intent);
                        activity.finish();
                        return true;
                    }
                });
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment allows the user to modify app settings
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class AppPreferencesFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_settings);
            final Activity activity = super.getActivity();

            //Changing active domain
            final EditTextPreference domain = (EditTextPreference)findPreference("settings_domain");
            domain.setText(App.Prefs().getString("domain", ""));
            domain.setSummary(App.Prefs().getString("domain", ""));

            //Force refresh
            domain.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    App.ChangeDomain(newValue.toString());

                    domain.setSummary(App.Prefs().getString("domain", ""));
                    Toast toast = Toast.makeText(App.getAppContext(), getString(R.string.domainChanged) + newValue.toString(), Toast.LENGTH_SHORT);
                    toast.show();
                    return true;
                }
            });

            //Clear settings
            final Preference clear = findPreference("settings_clear");
            clear.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    //Log out
                    Thread logoutThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            utils.logout();
                        }
                    });
                    logoutThread.start();

                    //Wipe settings
                    final SharedPreferences.Editor editor = App.Prefs().edit();
                    editor.clear();
                    editor.commit();

                    //Pretty toast is tasty
                    Toast toast = Toast.makeText(App.getAppContext(), R.string.settingsCleared, Toast.LENGTH_SHORT);
                    toast.show();

                    //Go back to the main menu where the settings well be reset to the default value.
                    Intent mainActivity = new Intent(getActivity(), MainActivity.class);
                    startActivity(mainActivity);
                    activity.finish();
                    return true;
                }
            });
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}
