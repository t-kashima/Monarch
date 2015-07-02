package com.unuuu.monarchsample;

import android.content.Context;
import android.content.res.AssetManager;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import io.realm.Realm;
import io.realm.internal.ColumnType;

/**
 * Created by kashima on 15/07/02.
 */
public class Monarch {
    /** 文字列のクラスを実態のクラスと紐づけるマップ */
    private static HashMap<String, Class> classMap = new HashMap<String, Class>() {
        {put("User", User.class);}
    };

    /**
     * Realmのバージョンをもらってマイグレーションを行う
     * @param context コンテキスト
     * @param realm Realm
     * @param version バージョン
     * @return マイグレーション後のバージョン
     */
    public static long migration(Context context, Realm realm, long version) {
        List<String> scriptList = getMigrationScript(context, "migration");
        for (int i = 0; i < scriptList.size(); i++) {
            String script = scriptList.get(i);
            long scriptVersion = getVersion(script);
            if (scriptVersion > version) {
                String jsonString = getString(context, "migration", script);
                Gson gson = new Gson();
                MonarchScript monarchScript = gson.fromJson(jsonString, MonarchScript.class);
                List<String> upList = monarchScript.getUp();
                for (int j = 0; j < upList.size(); j++) {
                    String command = upList.get(j);
                    // コマンドが空の時は何もしない
                    if (command.equals("")) {
                        continue;
                    }
                    // 実行に失敗した時
                    if (!execCommand(realm, command)) {
                        // 終了する
                        LogUtil.e("コマンドの実行に失敗しました");
                        return version;
                    }
                }

                // スクリプトの実行が終わったのでバージョンを更新する
                version = scriptVersion;
            }
        }
        return version;
    }

    /**
     * ファイル名からバージョンを取得する
     * @param fileName ファイル名
     * @return バージョン
     */
    public static long getVersion(String fileName) {
        // ファイル名からUnixTime(バージョン)を取得する
        List<String> list = Arrays.asList(fileName.split("_"));
        String versionString = list.get(0);
        long version = Long.parseLong(versionString);
        return version;
    }

    /**
     * コマンドを実行する
     * @param realm Realm
     * @param command コマンド
     * @return 実行が成功したか失敗したか
     */
    private static boolean execCommand(Realm realm, String command) {
        // スペースで区切る
        List<String> list = Arrays.asList(command.split(" "));
        String directive = list.get(0);
        LogUtil.d("コマンド: " + command);
        // カラム追加の時
        if (directive.equals("addcolumn")) {
            return execAddColumn(realm, command);
        }
        return false;
    }

    /**
     * カラム追加をする
     * フォーマット: addcolumn tableName columnName:type
     * @param realm Realm
     * @param command コマンド
     * @return 実行が成功したか失敗したか
     */
    private static boolean execAddColumn(Realm realm, String command) {
        // スペースで区切る
        List<String> list = Arrays.asList(command.split(" "));
        // コマンドの数が少ない時は終わり
        if (3 > list.size()) {
            LogUtil.d("コマンドのカラム数が正しくありません");
            return false;
        }
        String tableName = list.get(1);
        String columnNameAndType = list.get(2);
        List<String> columnList = Arrays.asList(columnNameAndType.split(":"));
        String columnName = columnList.get(0);
        ColumnType columnType = getType(columnList.get(1));

        realm.getTable(classMap.get(tableName)).addColumn(columnType, columnName);

        return true;
    }

    private static ColumnType getType(String typeString) {
        if (typeString.equals("int")) {
            return ColumnType.INTEGER;
        }
        return ColumnType.STRING;
    }

    /**
     * Migrationのスクリプト名の一覧を取得する
     * @param context コンテキスト
     * @param directory ディレクトリ
     * @return スクリプト名の一覧
     */
    public static List<String> getMigrationScript(Context context, String directory) {
        AssetManager assetManager = context.getAssets();
        List<String> scriptList = null;
        try {
            scriptList = Arrays.asList(assetManager.list(directory));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        // 名前順に並び替える (日付けが古いものが一番前になる)
        Collections.sort(scriptList, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.compareTo(rhs);
            }
        });

        return scriptList;
    }

    /**
     * ファイルを読み込んで文字列を返す
     * @param context コンテキスト
     * @param directory ディレクトリ
     * @param file ファイル名
     * @return 文字列
     */
    private static String getString(Context context, String directory, String file) {
        String json = null;
        try {
            InputStream inputStream = context.getAssets().open(directory + File.separator + file);
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
