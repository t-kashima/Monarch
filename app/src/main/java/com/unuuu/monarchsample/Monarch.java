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
import io.realm.internal.TableOrView;

/**
 * Created by kashima on 15/07/02.
 */
public class Monarch {
    /** 文字列のクラスを実態のクラスと紐づけるマップ */
    private static HashMap<String, Class> mClassMap;

    /** マイグレーションファイルのディレクトリ名 */
    private final static String MIGRATION_DIRECTORY_NAME = "migration";

    /**
     * Realmのバージョンをもらってマイグレーションを行う
     * @param context コンテキスト
     * @param realm Realm
     * @param version バージョン
     * @param schemaVersion 最新のバージョン
     * @param classMap テーブル名とクラスのマップ
     * @return マイグレーション後のバージョン
     */
    public static long migration(Context context, Realm realm, long version, int schemaVersion, HashMap classMap) {
        LogUtil.d("バージョンアップ前: " + version);

        // テーブル名とクラスの対応表
        mClassMap = classMap;
        // マイグレーションファイルを読み込む
        List<String> scriptList = getMigrationScript(context, MIGRATION_DIRECTORY_NAME);
        for (int i = 0; i < scriptList.size(); i++) {
            String script = scriptList.get(i);
            long scriptVersion = getVersion(script);
            // 実行したことがなくて、最新のバージョン以下の時は実行する
            if (version < scriptVersion && schemaVersion >= scriptVersion) {
                String jsonString = readFile(context, "migration", script);
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
                        LogUtil.e("コマンドの実行に失敗しました: " + scriptVersion);
                        return version;
                    }
                }

                // スクリプトの実行が終わったのでバージョンを更新する
                version = scriptVersion;
            }
        }
        LogUtil.d("バージョンアップ後: " + version);

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
        } else if (directive.equals("removecolumn")) {
            return execRemoveColumn(realm, command);
        } else if (directive.equals("renamecolumn")) {
            return execRenameColumn(realm, command);
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
        realm.getTable(mClassMap.get(tableName)).addColumn(columnType, columnName);

        return true;
    }

    /**
     * カラム削除をする
     * フォーマット: removecolumn tableName columnName
     * @param realm Realm
     * @param command コマンド
     * @return 実行が成功したか失敗したか
     */
    private static boolean execRemoveColumn(Realm realm, String command) {
        // スペースで区切る
        List<String> list = Arrays.asList(command.split(" "));
        // コマンドの数が少ない時は終わり
        if (2 > list.size()) {
            LogUtil.d("コマンドのカラム数が正しくありません");
            return false;
        }
        String tableName = list.get(1);
        String columnName = list.get(2);

        // カラム名からIndexに変更する
        long columnIndex = realm.getTable(mClassMap.get(tableName)).getColumnIndex(columnName);
        // カラムが存在しない時
        if (TableOrView.NO_MATCH == columnIndex) {
            return false;
        }
        // カラムを削除する
        realm.getTable(mClassMap.get(tableName)).removeColumn(columnIndex);
        return true;
    }

    /**
     * カラム名を変更する
     * フォーマット: renamecolumn tableName columnName renameColumnName
     * @param realm Realm
     * @param command コマンド
     * @return 実行が成功したか失敗したか
     */
    private static boolean execRenameColumn(Realm realm, String command) {
        // スペースで区切る
        List<String> list = Arrays.asList(command.split(" "));
        // コマンドの数が少ない時は終わり
        if (3 > list.size()) {
            LogUtil.d("コマンドのカラム数が正しくありません");
            return false;
        }
        String tableName = list.get(1);
        String columnName = list.get(2);
        String renameColumnName = list.get(3);

        // カラム名からIndexに変更する
        long columnIndex = realm.getTable(mClassMap.get(tableName)).getColumnIndex(columnName);
        // カラムが存在しない時
        if (TableOrView.NO_MATCH == columnIndex) {
            return false;
        }
        realm.getTable(mClassMap.get(tableName)).renameColumn(columnIndex, renameColumnName);

        return true;
    }

    /**
     * 文字列からカラムのタイプを取得する
     * @param typeString カラムの文字列
     * @return カラムのタイプ
     */
    private static ColumnType getType(String typeString) {
        if (typeString.equals("int")) {
            return ColumnType.INTEGER;
        } else if (typeString.equals("float")) {
            return ColumnType.FLOAT;
        } else if (typeString.equals("double")) {
            return ColumnType.DOUBLE;
        } else if (typeString.equals("boolean")) {
            return ColumnType.BOOLEAN;
        } else if (typeString.equals("date")) {
            return ColumnType.DATE;
        } else if (typeString.equals("binary")) {
            return ColumnType.BINARY;
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
    private static String readFile(Context context, String directory, String file) {
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
