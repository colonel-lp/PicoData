package com.colonellp.ellamonitor;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.DocumentsContract.Document;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/** The system-granted subtree for Ella settings shared across app installations. */
final class DownloadsFolder {
    static final String NAME = "ella-monitoring";
    private static final String TREE = "tree";
    private static final String OFFERED = "offered";
    private static final int ACCESS = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
    private final ContentResolver resolver;
    private final Uri tree;

    private DownloadsFolder(ContentResolver resolver, Uri tree) {
        this.resolver = resolver;
        this.tree = tree;
    }

    private static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences("downloads-folder", Context.MODE_PRIVATE);
    }

    static boolean offerAccess(Context context) {
        SharedPreferences prefs = preferences(context);
        if (prefs.contains(TREE) || prefs.getBoolean(OFFERED, false)) return false;
        prefs.edit().putBoolean(OFFERED, true).apply();
        return true;
    }

    static Intent picker() {
        Uri initial = DocumentsContract.buildDocumentUri(
                "com.android.externalstorage.documents", "primary:Download/" + NAME);
        return new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                .addFlags(ACCESS | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
                .putExtra(DocumentsContract.EXTRA_INITIAL_URI, initial);
    }

    static void remember(Context context, Intent selection) throws Exception {
        Uri uri = selection.getData();
        if (uri == null || !DocumentsContract.isTreeUri(uri)) throw new IOException("Choose Downloads/ella-monitoring.");
        ContentResolver resolver = context.getContentResolver();
        DownloadsFolder folder = new DownloadsFolder(resolver, uri);
        if (!NAME.equals(folder.displayName(folder.root()))) throw new IOException("Choose Downloads/ella-monitoring.");
        if ((selection.getFlags() & ACCESS) != ACCESS) throw new IOException("Allow read and write access to the folder.");

        SharedPreferences prefs = preferences(context);
        String previous = prefs.getString(TREE, null);
        boolean changed = !uri.toString().equals(previous);
        resolver.takePersistableUriPermission(uri, ACCESS);
        if (!prefs.edit().putString(TREE, uri.toString()).commit()) {
            if (changed) resolver.releasePersistableUriPermission(uri, ACCESS);
            throw new IOException("Could not remember the saved folder.");
        }
        if (previous != null && changed) {
            try { resolver.releasePersistableUriPermission(Uri.parse(previous), ACCESS); }
            catch (SecurityException ignored) {}
        }
    }

    static DownloadsFolder selected(Context context) {
        String value = preferences(context).getString(TREE, null);
        return value == null ? null : new DownloadsFolder(context.getContentResolver(), Uri.parse(value));
    }

    private Uri root() {
        return DocumentsContract.buildDocumentUriUsingTree(tree, DocumentsContract.getTreeDocumentId(tree));
    }

    private String displayName(Uri document) throws IOException {
        try (Cursor cursor = resolver.query(document, new String[]{Document.COLUMN_DISPLAY_NAME}, null, null, null)) {
            if (cursor == null || !cursor.moveToFirst()) throw new IOException("Could not open the saved folder.");
            return cursor.getString(0);
        }
    }

    private Map<String, Uri> children(Uri parent, boolean directories) throws IOException {
        Uri children = DocumentsContract.buildChildDocumentsUriUsingTree(tree, DocumentsContract.getDocumentId(parent));
        String[] columns = {Document.COLUMN_DOCUMENT_ID, Document.COLUMN_DISPLAY_NAME, Document.COLUMN_MIME_TYPE};
        Map<String, Uri> found = new LinkedHashMap<>();
        try (Cursor cursor = resolver.query(children, columns, null, null, null)) {
            if (cursor == null) throw new IOException("Could not read the saved folder.");
            while (cursor.moveToNext()) {
                if (directories != Document.MIME_TYPE_DIR.equals(cursor.getString(2))) continue;
                Uri uri = DocumentsContract.buildDocumentUriUsingTree(tree, cursor.getString(0));
                if (found.put(cursor.getString(1), uri) != null) throw new IOException("Duplicate saved filenames.");
            }
        }
        return found;
    }

    private Uri directory(String name) throws IOException {
        return name.isEmpty() ? root() : children(root(), true).get(name);
    }

    Map<String, Uri> files(String name) throws IOException {
        Uri directory = directory(name);
        return directory == null ? new LinkedHashMap<>() : children(directory, false);
    }

    Uri create(String name, String mime, String filename) throws IOException {
        Uri parent = directory(name);
        if (parent == null) parent = DocumentsContract.createDocument(resolver, root(), Document.MIME_TYPE_DIR, name);
        if (parent == null) throw new IOException("Could not create the settings subfolder.");
        Uri document = DocumentsContract.createDocument(resolver, parent, mime, filename);
        if (document == null) throw new IOException("Could not create saved file.");
        try {
            if (!filename.equals(displayName(document))) throw new IOException("Saved filename is already in use.");
        } catch (IOException | RuntimeException error) {
            try { DocumentsContract.deleteDocument(resolver, document); }
            catch (IOException | RuntimeException cleanup) { error.addSuppressed(cleanup); }
            throw error;
        }
        return document;
    }
}
