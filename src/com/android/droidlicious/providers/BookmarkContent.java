package com.android.droidlicious.providers;

import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.json.JSONObject;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class BookmarkContent {

	public static class Bookmark implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.parse("content://" + 
				BookmarkContentProvider.AUTHORITY + "/bookmark");
		
		 public static final  String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.droidlicious.bookmarks";
		
		public static final String Description = "DESCRIPTION";
		public static final String Url = "URL";
		public static final String Notes = "NOTES";
		public static final String Tags = "TAGS";
		public static final String Hash = "HASH";
		public static final String Meta = "META";
		
        private String mUrl;
        private String mDescription;
        private String mNotes;
        private String mTags;
        private String mHash;
        private String mMeta;

        public String getUrl() {
            return mUrl;
        }

        public String getDescription() {
            return mDescription;
        }
        
        public String getNotes(){
        	return mNotes;
        }
        
        public String getTags(){
        	return mTags;
        }
        
        public String getHash(){
        	return mHash;
        }

        public String getMeta(){
        	return mMeta;
        }
        
        public Bookmark() {
            mUrl = "";
            mDescription = "";
            mNotes = "";
            mTags = "";
            mHash = "";
            mMeta = "";
        }
        
        public Bookmark(String url, String description) {
            mUrl = url;
            mDescription = description;
            mNotes = "";
            mTags = "";
            mHash = "";
            mMeta = "";
        }
        
        public Bookmark(String url, String description, String notes) {
            mUrl = url;
            mDescription = description;
            mNotes = notes;
            mTags = "";
            mHash = "";
            mMeta = "";
        }
        
        public Bookmark(String url, String description, String notes, String tags) {
            mUrl = url;
            mDescription = description;
            mNotes = notes;
            mTags = tags;
            mHash = "";
            mMeta = "";
        }
        
        public Bookmark(String url, String description, String notes, String tags, String hash, String meta) {
            mUrl = url;
            mDescription = description;
            mNotes = notes;
            mTags = tags;
            mHash = hash;
            mMeta = meta;
        }
        
        public static ArrayList<Bookmark> valueOf(String userBookmark){
            XPath xpath = XPathFactory.newInstance().newXPath();
            String expression = "/posts/post";
            ArrayList<Bookmark> list = new ArrayList<Bookmark>();
            
            InputSource inputSource = new InputSource(new StringReader(userBookmark));
            try {
            	
				NodeList nodes = (NodeList)xpath.evaluate(expression, inputSource, XPathConstants.NODESET);
				
				for(int i = 0; i < nodes.getLength(); i++){
					Node href = nodes.item(i).getAttributes().getNamedItem("href");
					Node title = nodes.item(i).getAttributes().getNamedItem("description");
					Node notes = nodes.item(i).getAttributes().getNamedItem("extended");
					Node tags = nodes.item(i).getAttributes().getNamedItem("tag");
					Node hash = nodes.item(i).getAttributes().getNamedItem("hash");
					Node meta = nodes.item(i).getAttributes().getNamedItem("meta");
					Node url = nodes.item(i).getAttributes().getNamedItem("url");
					String shref = "";
					String stitle = "";
					String snotes = "";
					String stags = "";
					String shash = "";
					String smeta = "";

					if(href != null)
						shref = href.getTextContent();
					if(title != null)
						stitle = title.getTextContent();
					if(notes != null)
						snotes = notes.getTextContent();
					if(tags != null)
						stags = tags.getTextContent();
					if(hash != null)
						shash = hash.getTextContent();
					if(url != null)
						shash = url.getTextContent();
					if(meta != null)
						smeta = meta.getTextContent();
					
					list.add(new Bookmark(shref, stitle, snotes, stags, shash, smeta));

				}
				
			} catch (XPathExpressionException e) {
				e.printStackTrace();
			}
			return list;
        }
        
        public static Bookmark valueOf(JSONObject userBookmark) {
            try {
                final String url = userBookmark.getString("u");
                final String description = userBookmark.getString("d");
                Log.d("bookmarkurl", url);
                Log.d("bookmarkdescription", description);

                return new Bookmark(url, description);
            } catch (final Exception ex) {
                Log.i("User.Bookmark", "Error parsing JSON user object");
            }
            return null;
        }
	}
}
