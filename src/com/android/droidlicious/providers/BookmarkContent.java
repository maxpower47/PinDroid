package com.android.droidlicious.providers;

import android.net.Uri;
import android.provider.BaseColumns;

public class BookmarkContent {

	public static final class Bookmark implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.parse("content://" + 
				BookmarkContentProvider.AUTHORITY + "/bookmark");
		
		 public static final  String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.droidlicious.bookmarks";
		
		public static final String Description = "DESCRIPTION";
		public static final String Url = "URL";
		public static final String Notes = "NOTES";
		public static final String Tags = "TAGS";
		public static final String Hash = "HASH";
		public static final String Meta = "META";
	}
}
