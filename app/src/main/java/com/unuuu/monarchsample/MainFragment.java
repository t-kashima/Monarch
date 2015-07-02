package com.unuuu.monarchsample;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmMigration;
import io.realm.exceptions.RealmMigrationNeededException;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        String path = FileUtil.getExternalStorage(getActivity().getApplicationContext(), "", false) + File.separator + "monarch.realm";

        Realm realm = null;
        try {
            realm = Realm.getInstance(FileUtil.getExternalStorage(getActivity().getApplicationContext(), "", false), "monarch.realm");
        } catch (RealmMigrationNeededException e) {
            Realm.migrateRealmAtPath(path, new RealmMigration() {
                @Override
                public long execute(Realm realm, long version) {
                    return Monarch.migration(getActivity().getApplicationContext(), realm, version);
                }
            });
            realm = Realm.getInstance(FileUtil.getExternalStorage(getActivity().getApplicationContext(), "", false), "monarch.realm");
        }

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

        List<String> scriptList = Monarch.getMigrationScript(getActivity().getApplicationContext(), "migration");
        for (int i = 0; i < scriptList.size(); i++) {
            String script = scriptList.get(i);
            LogUtil.d(script);
        }

        return rootView;
    }
}
