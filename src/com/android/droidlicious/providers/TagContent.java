package com.android.droidlicious.providers;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

import android.net.Uri;
import android.provider.BaseColumns;

public class TagContent {

	public static class Tag implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.parse("content://" + 
				BookmarkContentProvider.AUTHORITY + "/tag");
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.droidlicious.tags";
		
		public static final String Name = "NAME";
		public static final String Count = "COUNT";
		public static final String Account = "ACCOUNT";
		
        private final String mTagName;
        private final int mCount;
        private final String mAccount = null;
        private int mId = 0;

        public int getId(){
        	return mId;
        }
        
        public String getTagName() {
            return mTagName;
        }

        public int getCount() {
            return mCount;
        }

        public Tag(String tagName, int count) {
            mTagName = tagName;
            mCount = count;
        }
        
        public static ArrayList<Tag> valueOf(String userTag){
        	      	
        	SAXReader reader = new SAXReader();
        	InputSource inputSource = new InputSource(new StringReader(userTag));
        	Document document = null;
			try {
				document = reader.read(inputSource);
			} catch (DocumentException e1) {
				e1.printStackTrace();
			}   	
        	
			String expression = "/tags/tag";
			ArrayList<Tag> list = new ArrayList<Tag>();
           
        	List<Element> nodes = document.selectNodes(expression);
			
			for(int i = 0; i < nodes.size(); i++){
				String scount = nodes.get(i).attributeValue("count");
				String sname = nodes.get(i).attributeValue("tag");
				
				list.add(new Tag(sname, Integer.parseInt(scount)));
			}

			return list;
        }
	}
}
