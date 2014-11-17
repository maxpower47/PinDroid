/*
 * PinDroid - http://code.google.com/p/PinDroid/
 *
 * Copyright (C) 2010 Matt Schmidt
 *
 * PinDroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * PinDroid is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PinDroid; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */

package com.pindroid.providers;

import org.json.JSONObject;

import android.text.Spanned;
import android.util.Log;

public class ArticleContent {

	public static class Article {

		
        private String url;
        private String responseUrl;
        private String content;
        private String title;
        private Spanned span;


        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
        	this.url = url;
        }
        
        public Spanned getSpan() {
            return span;
        }

        public void setSpan(Spanned span) {
        	this.span = span;
        }
        
        public String getResponseUrl() {
            return responseUrl;
        }

        public void setResponseUrl(String responseUrl) {
        	this.responseUrl = responseUrl;
        }
        
        public String getContent() {
            return content;
        }

        public void setContent(String content) {
        	this.content = content;
        }
        
        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
        	this.title = title;
        }
        

        
        public Article() {
        }
        
        public Article(String content, String title, String url, String responseUrl) {
        	this.content = content;
        	this.title = title;
        	this.url = url;
        	this.responseUrl = responseUrl;
        }
        
        
        public static Article valueOf(JSONObject userBookmark) {
            try {
                final String content = userBookmark.getString("content");
                final String title = userBookmark.getString("title");
                final String url = userBookmark.getString("url");
                final String responseUrl = userBookmark.getString("responseUrl");

                return new Article(content, title, url, responseUrl);
            } catch (final Exception ex) {
            	Log.i("User.Bookmark", "Error parsing JSON user object");
            }
            return null;
        }
	}
}
