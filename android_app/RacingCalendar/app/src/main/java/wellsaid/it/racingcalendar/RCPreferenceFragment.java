package wellsaid.it.racingcalendar;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

public class RCPreferenceFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);
    }
}
