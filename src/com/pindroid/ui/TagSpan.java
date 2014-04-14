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

package com.pindroid.ui;

import android.text.style.ClickableSpan;
import android.view.View;

public class TagSpan extends ClickableSpan {
	public interface OnTagClickListener {
		public void onTagClick(String tag);
	}

	private final String mTag;
	private OnTagClickListener mOnTagClickListener;

	public TagSpan(String tag) {
		super();
		if (tag == null) {
			throw new NullPointerException();
		}
		mTag = tag;
	}

	@Override
	public void onClick(View widget) {
		if (mOnTagClickListener != null) {
			mOnTagClickListener.onTagClick(mTag);
		}
	}

	public OnTagClickListener getOnTagClickListener() {
		return mOnTagClickListener;
	}

	public void setOnTagClickListener(OnTagClickListener onTagClickListener) {
		mOnTagClickListener = onTagClickListener;
	}
}