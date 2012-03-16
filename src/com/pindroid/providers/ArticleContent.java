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

public class ArticleContent {

	public static class Article {

		
        private String url;
        private String responseUrl;
        private String content;
        private String title;


        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
        	this.url = url;
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
	}
}
