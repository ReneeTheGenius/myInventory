package android.example.com.myInventory.data;

import android.content.ContentUris;
import android.net.Uri;

import androidx.annotation.NonNull;

public class myContentUris extends ContentUris {
    public static String parseName(@NonNull Uri contentUri){
        String last = contentUri.getLastPathSegment();
        return last.trim();
    }
    public static Uri.Builder appendName(Uri.Builder builder, String name) {
        return builder.appendEncodedPath(name);

    }
    public static Uri withAppendedName(Uri contentUri, String name) {
        return appendName(contentUri.buildUpon(), name).build();
    }
}
