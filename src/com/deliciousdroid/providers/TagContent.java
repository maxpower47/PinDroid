/*
 * DeliciousDroid - http://code.google.com/p/DeliciousDroid/
 *
 * Copyright (C) 2010 Matt Schmidt
 *
 * DeliciousDroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * DeliciousDroid is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DeliciousDroid; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */

package com.deliciousdroid.providers;

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
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.deliciousdroid.tags";
		
		public static final String Name = "NAME";
		public static final String Count = "COUNT";
		public static final String Account = "ACCOUNT";
		
        private final String mTagName;
        private int mCount;
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
        
        public void setCount(int count) {
        	mCount = count;
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
