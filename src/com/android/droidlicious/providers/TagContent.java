package com.android.droidlicious.providers;

import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.android.droidlicious.client.User;

import android.net.Uri;
import android.provider.BaseColumns;

public class TagContent {

	public static class Tag implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.parse("content://" + 
				BookmarkContentProvider.AUTHORITY + "/tag");
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.droidlicious.tags";
		
		public static final String Name = "NAME";
		public static final String Count = "COUNT";
		
        private final String mTagName;
        private final int mCount;

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
            XPath xpath = XPathFactory.newInstance().newXPath();
            String expression = "/tags/tag";
            ArrayList<Tag> list = new ArrayList<Tag>();
            
            InputSource inputSource = new InputSource(new StringReader(userTag));
            try {
            	
				NodeList nodes = (NodeList)xpath.evaluate(expression, inputSource, XPathConstants.NODESET);
				
				for(int i = 0; i < nodes.getLength(); i++){
					Node count = nodes.item(i).getAttributes().getNamedItem("count");
					Node name = nodes.item(i).getAttributes().getNamedItem("tag");

					String scount = "";
					String sname = "";


					if(count != null)
						scount = count.getTextContent();
					if(name != null)
						sname = name.getTextContent();

					
					list.add(new Tag(sname, Integer.parseInt(scount)));

				}
				
			} catch (XPathExpressionException e) {
				e.printStackTrace();
			}
			return list;
        }
	}
}
