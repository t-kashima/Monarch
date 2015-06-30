package com.unuuu.monarchsample;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * ファイル操作系のUtilityクラス
 * Created by t-kashima on 15/06/10.
 */
public class FileUtil {

    public static String getExternalStoragePath(Context context, String dir, boolean useCacheDir) {
        File storage = Environment.getExternalStorageDirectory();
        if (storage == null) {
            if (useCacheDir) {
                return getCachePath(context, dir);
            }
            return null;
        }

        String path = storage.getPath();

        path += File.separator + context.getPackageName();
        if (dir != null) {
            path += File.separator + dir;
        }
        File cacheDir = new File(path);
        if (!cacheDir.exists() && !cacheDir.isDirectory() && !cacheDir.mkdirs()) {
            LogUtil.d("not mkdirs!");
            if (useCacheDir) {
                return getCachePath(context, dir);
            }
            return null;
        }
        return cacheDir.getPath();
    }

    public static File getExternalStorage(Context context, String dir, boolean useCacheDir) {
        return new File(getExternalStoragePath(context, dir, useCacheDir));
    }

    public static String getCachePath(Context context, String dir) {
        File storage = context.getCacheDir();

        if (storage == null) {
            return null;
        }
        LogUtil.d("storage = " + storage);

        if (dir != null) {
            String path = storage.getAbsolutePath() + File.separator + dir;
            storage = new File(path);
        }

        if (!storage.exists() && !storage.isDirectory() && !storage.mkdirs()) {
            LogUtil.d("not mkdirs!");
            return null;
        }
        return storage.getAbsolutePath();
    }

    public static boolean deleteContentFile(Context context, String filePath) {
        try {
            ContentResolver cr = context.getContentResolver();
            String where = MediaStore.Images.Media.DATA + " = ?";
            String[] selectionArgs = { filePath };
            return cr.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, where, selectionArgs) == 1;
        } catch (Exception ignore) {
            LogUtil.e(ignore);
        }
        return false;
    }

    public static boolean deleteMediaFile(Context context, String filePath) {
        if (filePath == null) {
            return false;
        }
        File file = new File(filePath);
        return !file.exists() || !deleteContentFile(context, filePath) && file.delete();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String contentUriToFilePath(Context context, Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        LogUtil.d("uri = " + uri);

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                LogUtil.d("split[0] = " + split[0] + ", split[1] = " + split[1]);
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                LogUtil.d("contentUri = " + contentUri);
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                LogUtil.d("split[0] = " + split[0] + ", split[1] = " + split[1] + ", contentUri = " + contentUri);
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if ("content".equals(uri.getScheme())) {

            if (isGooglePhotosUri(uri)) {
                return uri.getLastPathSegment();
            }
            return getDataColumn(context, uri, null, null);

        } else if ("file".equals(uri.getScheme())) {
            return uri.getPath();
        } else {
            return uri.getPath();
        }
        return null;
    }

    public static String getExternalFilePath(
            Context context,
            String dir,
            String extension,
            boolean useCache) {
        return getExternalFilePath(context,
                dir,
                getTimestampFileName(),
                extension,
                useCache);
    }

    public static String getExternalFilePath(
            Context context,
            String dir,
            String filename,
            String extension,
            boolean useCache) {
        String path = getExternalStoragePath(context, dir, useCache);
        if (path == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(path).append(File.separator);
        builder.append(filename);
        if (extension != null) {
            builder.append(".").append(extension);
        }
        return builder.toString();
    }

    public static String getImageFilePath(Context context, String filename) {
        return getExternalFilePath(
                context,
                "images",
                filename,
                "jpg",
                false);
    }

    public static String getImageFilePath(Context context) {
        return getImageFilePath(
                context,
                getTimestampFileName());
    }

    public static String getTimestampFileName() {
        return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    }

    public static void writeBitmap(Bitmap bitmap, int quality, String path) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File(path));
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos);
        } catch (Exception ignore) {
            LogUtil.e(ignore);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception ignore) {
                }
            }
        }
    }


    public static boolean writeObject(Context context, String fileName, Object object) {
        FileOutputStream fileOutputStream = null;
        ObjectOutputStream objectOutputStream = null;
        try {
            fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(object);
            return true;
        } catch (Exception ignore) {
            LogUtil.e(ignore);
        } finally {
            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close();
                } catch (Exception ignore) {
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (Exception ignore) {
                }
            }
        }
        return false;
    }

    public static boolean writeCacheObject(Context context, String dir, String fileName, Object object) {
        String path = getCachePath(context, dir);
        if (path != null) {
            path = path + File.separator + fileName;
            LogUtil.d("path = " + path);
            File file = new File(path);
            FileOutputStream fileOutputStream = null;
            ObjectOutputStream objectOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(file);
                objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(object);
                return true;
            } catch (Exception ignore) {
                LogUtil.e(ignore);
            } finally {
                if (objectOutputStream != null) {
                    try {
                        objectOutputStream.close();
                    } catch (Exception ignore) {
                    }
                }
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (Exception ignore) {
                    }
                }
            }
        }
        return false;
    }

    public static Object readObject(Context context, String fileName) {
        FileInputStream fileInputStream = null;
        ObjectInputStream objectInputStream = null;
        try {
            fileInputStream = context.openFileInput(fileName);
            objectInputStream = new ObjectInputStream(fileInputStream);
            return objectInputStream.readObject();
        } catch (FileNotFoundException ignore) {
        } catch (NullPointerException ignore) {
        } catch (Exception ignore) {
            LogUtil.e(ignore);
        } finally {
            if (objectInputStream != null) {
                try {
                    objectInputStream.close();
                } catch (Exception ignore) {
                }
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (Exception ignore) {
                }
            }
        }
        return null;
    }

    public static Object readCacheObject(Context context, String dirName, String fileName) {
        String path = getCachePath(context, dirName);
        if (path != null) {
            path = path + File.separator + fileName;
            LogUtil.d("path = " + path);
            File file = new File(path);
            if (file.exists()) {
                return readObject(file);
            }
        }
        return null;
    }

    public static Object readObject(File file) {
        if (file != null) {
            LogUtil.d("file.getPath() = " + file.getPath());
            FileInputStream fileInputStream = null;
            ObjectInputStream objectInputStream = null;
            try {
                fileInputStream = new FileInputStream(file);
                objectInputStream = new ObjectInputStream(fileInputStream);
                return objectInputStream.readObject();
            } catch (Exception ignore) {
            } finally {
                if (objectInputStream != null) {
                    try {
                        objectInputStream.close();
                    } catch (Exception ignore) {
                    }
                }
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (Exception ignore) {
                    }
                }
            }
        }
        return null;
    }

    public static boolean writeBytes(Context context, String fileName, byte[] bytes) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            fileOutputStream.write(bytes);
            return true;
        } catch (Exception ignore) {
            LogUtil.e(ignore);
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (Exception ignore) {
                }
            }
        }
        return false;
    }

    public static byte[] readBytes(Context context, String fileName) {
        FileInputStream fileInputStream = null;

        try {
            fileInputStream = context.openFileInput(fileName);
            byte[] buffer = new byte[1024];
            int count;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            while ((count = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, count);
            }
            return outputStream.toByteArray();
        } catch (FileNotFoundException ignore) {
        } catch (Exception ignore) {
            LogUtil.e(ignore);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (Exception ignore) {
                }
            }
        }
        return null;
    }

    public static boolean deleteFile(Context context, String path) {
        try {
            LogUtil.d("delete: " + path);
            return context.deleteFile(path);
        } catch (Exception ignore) {
            LogUtil.e(ignore);
        }
        return false;
    }

    public static boolean deleteCacheFile(Context context, String dirName, String fileName) {
        String path = getCachePath(context, dirName);
        if (path != null) {
            path = path + File.separator + fileName;
            return deleteFile(context, path);
        }
        return false;
    }

    public static boolean deleteFile(File file, boolean deleteDir) {
        try {
            if (deleteDir && file.isDirectory()) {
                File[] files = file.listFiles();
                boolean result = true;
                if (files != null) {
                    for (File innerFile : files) {
                        result = deleteFile(innerFile, true);
                    }
                }
                return result && file.delete();
            } else {
                LogUtil.d("delete: " + file.getAbsolutePath());
                return file.delete();
            }
        } catch (Exception ignore) {
            LogUtil.e(ignore);
        }
        return false;
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = MediaStore.MediaColumns.DATA;
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                LogUtil.d("uri = " + uri + ", c.getColumnCount = " + cursor.getColumnCount() + ", c.getColumnIndex = " + index + " : c.getString = " + cursor.getString(index));
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}
