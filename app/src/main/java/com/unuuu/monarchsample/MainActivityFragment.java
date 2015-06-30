package com.unuuu.monarchsample;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import io.realm.Realm;
import io.realm.internal.Table;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        String realmFile = "monarch.realm";
        Realm realm =  Realm.getInstance(FileUtil.getExternalStorage(getActivity().getApplicationContext(), "", false), realmFile);

        List<String> scriptList = this.getMigrationScript("migration");
        for (int i = 0; i < scriptList.size(); i++) {
            String script = scriptList.get(i);
            String jsonString = this.getString("migration", script);
            Gson gson = new Gson();
            MonarchScript monarchScript = gson.fromJson(jsonString, MonarchScript.class);
            List<String> upList = monarchScript.getUp();
            for (int j = 0; j < upList.size(); j++) {
                String upScript = upList.get(j);
                Table userTable = realm.getTable(User.class);

                Log.d("MONARCH: ", upScript);
            }
        }

        return rootView;
    }

    /**
     * Migrationのスクリプト名の一覧を取得する
     * @param directory ディレクトリ
     * @return スクリプト名の一覧
     */
    private List<String> getMigrationScript(String directory) {
        AssetManager assetManager = getActivity().getAssets();
        List<String> scriptList = null;
        try {
             scriptList = Arrays.asList(assetManager.list(directory));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return scriptList;
    }

    /**
     * ファイルを読み込んで文字列を返す
     * @param directory ディレクトリ
     * @param file ファイル名
     * @return 文字列
     */
    private String getString(String directory, String file) {
        String json = null;
        try {
            InputStream inputStream = getActivity().getAssets().open(directory + File.separator + file);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }
}
