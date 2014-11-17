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

package com.pindroid.xml;

import java.io.InputStream;
import java.text.ParseException;

import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.util.Xml;

import com.pindroid.providers.ArticleContent.Article;

public class SaxArticleParser {

	private InputStream is;
	
    public SaxArticleParser(InputStream stream) {
    	is = stream;
    }

    public Article parse() throws ParseException {
        final Article article = new Article();
        final RootElement root = new RootElement("Article");
        
        root.getChild("ResponseUrl").setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
            	article.setResponseUrl(body);
            }
        });
        root.getChild("URL").setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
            	article.setUrl(body);
            }
        });
        root.getChild("Content").setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
            	article.setContent(body);
            }
        });
        root.getChild("Title").setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
            	article.setTitle(body);
            }
        });

        try {
            Xml.parse(is, Xml.Encoding.UTF_8, root.getContentHandler());
        } catch (Exception e) {
            throw new ParseException(e.getMessage(), 0);
        }
        return article;
    }
}