package com.unuuu.monarchsample;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.unuuu.monarch.Monarch;

import java.util.HashMap;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        final int schemaVersion = 1436076736;
        RealmConfiguration config = new RealmConfiguration.Builder(FileUtil.getExternalStorage(getActivity().getApplicationContext(), "", false))
                .name("monarch.realm")
                .schemaVersion(schemaVersion)
                .migration(new RealmMigration() {
                    @Override
                    public long execute(Realm realm, long version) {
                        HashMap<String, Class> classMap = new HashMap<String, Class>() {
                            {
                                put("User", User.class);
                            }
                        };
                        return Monarch.migration(getActivity().getApplicationContext(), realm, version, schemaVersion, classMap);
                    }
                })
                .build();
        Realm realm = Realm.getInstance(config);

        LogUtil.d(realm.getPath());

        {
            realm.beginTransaction();
            User user = realm.createObject(User.class);
            user.setUserId(123);
            realm.commitTransaction();
        }

        {
            User user = realm.where(User.class).equalTo("userId", 123).findFirst();
            if (user != null) {
                LogUtil.d("ユーザが見つかったよ: " + user.getUserId());
            }
        }

        return rootView;
    }
}
