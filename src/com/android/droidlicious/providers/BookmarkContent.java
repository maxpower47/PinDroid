package com.android.droidlicious.providers;

import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.android.droidlicious.util.DateParser;

import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class BookmarkContent {

	public static class Bookmark implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.parse("content://" + 
				BookmarkContentProvider.AUTHORITY + "/bookmark");
		
		public static final  String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.droidlicious.bookmarks";
		
		public static final String Account = "ACCOUNT";
		public static final String Description = "DESCRIPTION";
		public static final String Url = "URL";
		public static final String Notes = "NOTES";
		public static final String Tags = "TAGS";
		public static final String Hash = "HASH";
		public static final String Meta = "META";
		public static final String Time = "TIME";
		public static final String LastUpdate = "LASTUPDATE";
		
		private int mId = 0;
		private String mAccount = null;
        private String mUrl = null;
        private String mDescription = null;
        private String mNotes = null;
        private String mTags = null;
        private String mHash = null;
        private String mMeta = null;
        private Boolean mPrivate = false;
        private long mTime = 0;
        private long mLastUpdate = 0;

        public int getId(){
        	return mId;
        }
        
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
        
        public long getTime(){
        	return mTime;
        }
        
        public long getLastUpdate(){
        	return mLastUpdate;
        }
        
        public Boolean getPrivate(){
        	return mPrivate;
        }
        
        public Bookmark() {
        }
        
        public Bookmark(String url, String description) {
            mUrl = url;
            mDescription = description;
        }
        
        public Bookmark(String url, String description, String notes) {
            mUrl = url;
            mDescription = description;
            mNotes = notes;
        }
        
        public Bookmark(String url, String description, String notes, String tags) {
            mUrl = url;
            mDescription = description;
            mNotes = notes;
            mTags = tags;
        }
        
        public Bookmark(String url, String description, String notes, String tags, Boolean priv) {
            mUrl = url;
            mDescription = description;
            mNotes = notes;
            mTags = tags;
            mPrivate = priv;
        }
        
        public Bookmark(String url, String description, String notes, String tags, String hash, String meta, long time) {
            mUrl = url;
            mDescription = description;
            mNotes = notes;
            mTags = tags;
            mHash = hash;
            mMeta = meta;
            mTime = time;
        }
        
        public Bookmark(int id, String url, String description, String notes, String tags, String hash, String meta, long time) {
            mId = id;
        	mUrl = url;
            mDescription = description;
            mNotes = notes;
            mTags = tags;
            mHash = hash;
            mMeta = meta;
            mTime = time;
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
					Node time = nodes.item(i).getAttributes().getNamedItem("time");
					String shref = "";
					String stitle = "";
					String snotes = "";
					String stags = "";
					String shash = "";
					String smeta = "";
					String stime = "";

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
					if(time != null)
						stime = time.getTextContent();
					
					Date d = new Date(0);
					if(stime != null && stime != ""){
						try {
							d = DateParser.parse(stime);
						} catch (ParseException e) {
							Log.d("Parse error", stime);
							e.printStackTrace();
						}
					}
					
					list.add(new Bookmark(shref, stitle, snotes, stags, shash, smeta, d.getTime()));

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
                final JSONArray tags = userBookmark.getJSONArray("t");
                Log.d("bookmarkurl", url);
                Log.d("bookmarkdescription", description);
                Log.d("bookmarktags", tags.join(" ").replace("\"", ""));

                return new Bookmark(url, description, "", tags.join(" ").replace("\"", ""));
            } catch (final Exception ex) {
                Log.i("User.Bookmark", "Error parsing JSON user object");
            }
            return null;
        }
	}
}
