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

package com.pindroid.util;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.widget.MultiAutoCompleteTextView.Tokenizer;

public class SpaceTokenizer implements Tokenizer {

	public int findTokenStart(CharSequence text, int cursor) {
		int i = cursor;

		while (i > 0 && text.charAt(i - 1) != ' ') {
			i--;
		}
		while (i < cursor && text.charAt(i) == ' ') {
			i++;
		}

		return i;
	}

	public int findTokenEnd(CharSequence text, int cursor) {
		int i = cursor;
		int len = text.length();

		while (i < len) {
			if (text.charAt(i) == ' ') {
				return i;
			} else {
				i++;
			}
		}

		return len;
	}

	public CharSequence terminateToken(CharSequence text) {
		int i = text.length();

		while (i > 0 && text.charAt(i - 1) == ' ') {
			i--;
		}

		if (i > 0 && text.charAt(i - 1) == ' ') {
			return text;
		} else {
			if (text instanceof Spanned) {
				SpannableString sp = new SpannableString(text + " ");
				TextUtils.copySpansFrom((Spanned) text, 0, text.length(),
						Object.class, sp, 0);
				return sp;
			} else {
				return text + " ";
			}
		}
	}
}