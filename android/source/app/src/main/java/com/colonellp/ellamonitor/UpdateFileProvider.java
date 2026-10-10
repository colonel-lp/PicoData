package com.colonellp.ellamonitor;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileNotFoundException;

/** Read-only provider exposing only the verified staged update APK. */
public final class UpdateFileProvider extends ContentProvider {
    static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".updates";
    private static final String APK_PATH = "apk";
    private static final String APK_NAME = "Ella-monitoring-update.apk";

    static File updateFile(Context context) {
        if (context == null) throw new IllegalStateException("Update provider context is unavailable");
        File base = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (base == null) base = new File(context.getCacheDir(), "updates");
        try { UpdateFiles.ensureDirectory(base); }
        catch (java.io.IOException error) { throw new IllegalStateException(error.getMessage(), error); }
        return new File(base, APK_NAME);
    }

    static Uri updateUri() {
        return new Uri.Builder().scheme("content").authority(AUTHORITY)
                .appendPath(APK_PATH).build();
    }

    private boolean isUpdateUri(Uri uri) {
        return uri != null && "content".equals(uri.getScheme())
                && AUTHORITY.equals(uri.getAuthority())
                && uri.getPathSegments().size() == 1
                && APK_PATH.equals(uri.getPathSegments().get(0));
    }

    @Override public boolean onCreate() { return true; }

    @Override public String getType(Uri uri) {
        return isUpdateUri(uri) ? "application/vnd.android.package-archive" : null;
    }

    @Override public ParcelFileDescriptor openFile(Uri uri, String mode)
            throws FileNotFoundException {
        if (!isUpdateUri(uri) || !"r".equals(mode)) throw new FileNotFoundException();
        File file;
        try { file = updateFile(getContext()); }
        catch (IllegalStateException error) { throw new FileNotFoundException(error.getMessage()); }
        if (!file.isFile()) throw new FileNotFoundException();
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
    }

    @Override public Cursor query(Uri uri, String[] projection, String selection,
                                  String[] selectionArgs, String sortOrder) {
        if (!isUpdateUri(uri)) return null;
        String[] columns = projection == null
                ? new String[]{OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE}
                : projection;
        MatrixCursor cursor = new MatrixCursor(columns, 1);
        MatrixCursor.RowBuilder row = cursor.newRow();
        File file = updateFile(getContext());
        for (String column : columns) {
            if (OpenableColumns.DISPLAY_NAME.equals(column)) row.add(APK_NAME);
            else if (OpenableColumns.SIZE.equals(column)) row.add(file.length());
            else row.add(null);
        }
        return cursor;
    }

    @Override public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("Read only");
    }

    @Override public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Read only");
    }

    @Override public int update(Uri uri, ContentValues values, String selection,
                                String[] selectionArgs) {
        throw new UnsupportedOperationException("Read only");
    }
}

