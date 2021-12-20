package fr.isep.musictagger;
/*
 * Original Code shared by "DarwinLouis" at https://stackoverflow.com/a/33298265
 * under the [CC BY-SA 3.0](https://creativecommons.org/licenses/by-sa/3.0/) license
 *
 * Original Code has been changed to adapt to the project
 */

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.function.Predicate;

public class PathUtils {

    public static final Predicate<Uri> isExternalStorageDocument = uri -> uri.getAuthority().equals("com.android.externalstorage.documents");
    public static final Predicate<Uri> isDownloadsDocument = uri -> uri.getAuthority().equals("com.android.providers.downloads.documents");
    public static final Predicate<Uri> isMediaDocument = uri -> uri.getAuthority().equals("com.android.providers.media.documents");

    @Nullable
    public static String getDataColumn(@NonNull final Context context, final Uri uri, final String selection, final String[] selectionArgs) {
        final String[] projection = {MediaStore.MediaColumns.DATA};
        try (Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                return cursor.getString(column_index);
            }
        }
        return null;
    }

    @Nullable
    public static String getPath(final Context context, final Uri uri) {
        if (DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument.test(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if (type.equalsIgnoreCase("primary")) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                } else {
                    return "/storage/" + split[0] + "/" + split[1];
                }
            } else if (isDownloadsDocument.test(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.parseLong(id));

                return getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument.test(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");

                final Uri contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                final String selection = "_id=?";
                final String[] selectionArgs = {split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if (uri.getScheme().equalsIgnoreCase("content")) {
            return getDataColumn(context, uri, null, null);
        } else if (uri.getScheme().equalsIgnoreCase("file")) {
            return uri.getPath();
        }

        return null;
    }
}
