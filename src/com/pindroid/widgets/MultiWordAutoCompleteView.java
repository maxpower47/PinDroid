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

package com.pindroid.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

public class MultiWordAutoCompleteView extends AutoCompleteTextView {
	private static final String DEFAULT_SEPARATOR = " ";
    private String mSeparator = DEFAULT_SEPARATOR;

    public MultiWordAutoCompleteView(Context context) {
    	super(context);
    }

    public MultiWordAutoCompleteView(Context context, AttributeSet attrs) {
    	super(context, attrs);
    }

    public MultiWordAutoCompleteView(Context context, AttributeSet attrs, int defStyle) {
    	super(context, attrs, defStyle);
    }

    /**
     * Gets the separator used to delimit multiple words. Defaults to " " if
     * never specified.
     */
    public String getSeparator() {
    	return mSeparator;
    }

    /**
     * Sets the separator used to delimit multiple words. Defaults to " " if
     * never specified.
     *
     * @param separator
     */
    public void setSeparator(String separator) {
    	mSeparator = separator;
    }

    @Override
    protected void performFiltering(CharSequence text, int keyCode) {
    	String newText = text.toString();
    	if (newText.indexOf(mSeparator) != -1) {
    		int lastIndex = newText.lastIndexOf(mSeparator);
    		if (lastIndex != newText.length() - 1) {
    			newText = newText.substring(lastIndex + 1).trim();
    			if (newText.length() >= getThreshold()) {
    				text = newText;
    			}
    		}
    	}
    	super.performFiltering(text, keyCode);
    }

    @Override
    protected void replaceText(CharSequence text) {
    	String newText = getText().toString();
    	if (newText.indexOf(mSeparator) != -1) {
    		int lastIndex = newText.lastIndexOf(mSeparator);
    		newText = newText.substring(0, lastIndex + 1) + text.toString();
    	}
    	else {
    		newText = text.toString();
    	}
    	super.replaceText(newText);
    } 
}
