package wellsaid.it.racingcalendar.settings;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import wellsaid.it.racingcalendar.R;

public class RCPreferenceFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);
    }
}
