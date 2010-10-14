package com.android.droidlicious.providers;

import android.net.Uri;
import android.provider.BaseColumns;

public class TagContent {

	public static final class Tag implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.parse("content://" + 
				BookmarkContentProvider.AUTHORITY + "/tag");
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.droidlicious.tags";
		
		public static final String Name = "NAME";
		public static final String Count = "COUNT";
	}
}
