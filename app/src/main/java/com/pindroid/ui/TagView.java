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

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.pindroid.R;
import com.pindroid.providers.TagContent.Tag;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class TagView extends TextView {

	private static final int DEFAULT_PADDING = 8;
	private static final int DEFAULT_CORNER_RADIUS = 6;
	private static final boolean DEFAULT_UPPERCASE = true;

	private int tagPadding;
	private int tagCornerRadius;
	private boolean uppercaseTags = DEFAULT_UPPERCASE;

	private OnTagClickListener listener;
	private ColorGenerator colorGenerator;

	public interface OnTagClickListener {
		void onTagClick(String tag);
	}

	@SuppressWarnings("UnusedDeclaration")
	public TagView(Context context) {
		this(context, null);
	}

	public TagView(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.tagViewStyle);
	}

	public TagView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		if (attrs != null) {
			TypedArray attributesArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TagView, defStyle, R.style.Widget_TagView);
			tagPadding = attributesArray.getDimensionPixelSize(R.styleable.TagView_tagPadding, dipToPixels(DEFAULT_PADDING));
			tagCornerRadius = attributesArray.getDimensionPixelSize(R.styleable.TagView_tagCornerRadius, dipToPixels(DEFAULT_CORNER_RADIUS));
			uppercaseTags = attributesArray.getBoolean(R.styleable.TagView_tagUppercase, DEFAULT_UPPERCASE);
			attributesArray.recycle();
		} else {
			tagPadding = dipToPixels(DEFAULT_PADDING);
			tagCornerRadius = dipToPixels(DEFAULT_CORNER_RADIUS);
		}

		this.setMovementMethod(LinkMovementMethod.getInstance());
	}

	private int dipToPixels(float dipValue) {
		DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
	}

	public void setTags(Tag[] tags, String separator) {
		setTags(Arrays.asList(tags), separator);
	}

	public void setTags(List<? extends Tag> tags, String separator) {
		if (tags.isEmpty()) {
			this.setVisibility(View.GONE);
			return;
		}

		final SpannableStringBuilder sb = new SpannableStringBuilder();
		Iterator<? extends Tag> it = tags.iterator();
		while (it.hasNext()) {
			Tag tag = it.next();
			String tagContent = uppercaseTags ? tag.getTagName().toUpperCase() : tag.getTagName();
			sb.append(tagContent);
			sb.setSpan(createSpan(tagContent, colorGenerator.getColor(tagContent)),
					sb.length() - tagContent.length(),
					sb.length(),
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			sb.setSpan(new ClickableTagSpan(tagContent, listener),
					sb.length() - tagContent.length(),
					sb.length(),
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			if (it.hasNext()) {
				sb.append(separator);
			}
		}
		sb.append(" ");  // hack to fix incorrect line spacing with orphaned span on new line
		setText(sb);
	}

	private TagSpan createSpan(String text, int color) {
		return new TagSpan(
				text,
				tagPadding,
				getTextSize(),
				getTypeface() == Typeface.DEFAULT_BOLD,
				getCurrentTextColor(),
				color,
				tagCornerRadius
		);
	}

	public int getTagPadding() {
		return tagPadding;
	}

	public void setTagPadding(int tagPadding) {
		this.tagPadding = tagPadding;
	}

	public int getTagCornerRadius() {
		return tagCornerRadius;
	}

	public void setTagCornerRadius(int tagCornerRadius) {
		this.tagCornerRadius = tagCornerRadius;
	}

	public boolean isUppercaseTags() {
		return uppercaseTags;
	}

	public void setUppercaseTags(boolean uppercaseTags) {
		this.uppercaseTags = uppercaseTags;
	}

	public OnTagClickListener getListener() {
		return listener;
	}

	public void setListener(OnTagClickListener listener) {
		this.listener = listener;
	}


	public ColorGenerator getColorGenerator() {
		return colorGenerator;
	}

	public void setColorGenerator(ColorGenerator colorGenerator) {
		this.colorGenerator = colorGenerator;
	}

	private static class TagSpan extends ImageSpan {
		public TagSpan(String text, int tagPadding, float textSize, boolean bold, int textColor, int tagColor, float roundCornersFactor) {
			super(new TagDrawable(text, tagPadding, textSize, bold, textColor, tagColor, roundCornersFactor));
		}
	}

	private static class ClickableTagSpan extends ClickableSpan {

		private final String mTag;
		private OnTagClickListener mOnTagClickListener;

		public ClickableTagSpan(String tag, OnTagClickListener listener) {
			super();
			if (tag == null) {
				throw new NullPointerException();
			}
			mTag = tag;
			mOnTagClickListener = listener;
		}

		@Override
		public void onClick(View widget) {
			if (mOnTagClickListener != null) {
				mOnTagClickListener.onTagClick(mTag);
			}
		}
	}

	private static class TagDrawable extends Drawable {
		private static final int MAGIC_PADDING_LEFT = 0;
		private static final int MAGIC_PADDING_BOTTOM = 6;
		private final String text;
		private final float roundCornersFactor;
		private final Paint textContentPain;
		private final Paint backgroundPaint;
		private final RectF fBounds;
		private final Rect backgroundPadding;

		public TagDrawable(String text, int tagPadding, float textSize, boolean bold, int textColor, int tagColor, float roundCornersFactor) {
			this.backgroundPadding = new Rect(tagPadding, tagPadding, tagPadding, tagPadding);
			this.text = text;
			this.roundCornersFactor = roundCornersFactor;
			this.textContentPain = new Paint();
			textContentPain.setColor(textColor);
			textContentPain.setTextSize(textSize);
			textContentPain.setAntiAlias(true);
			textContentPain.setFakeBoldText(bold);
			textContentPain.setStyle(Paint.Style.FILL);
			textContentPain.setTextAlign(Paint.Align.LEFT);

			this.backgroundPaint = new Paint();
			backgroundPaint.setColor(tagColor);
			backgroundPaint.setStyle(Paint.Style.FILL);
			backgroundPaint.setAntiAlias(true);

			setBounds(0, 0,
					(int) textContentPain.measureText(text) + backgroundPadding.left + backgroundPadding.right,
					(int) (textContentPain.getTextSize() + backgroundPadding.top + backgroundPadding.bottom)
			);
			fBounds = new RectF(getBounds());
		}

		@Override
		public void draw(Canvas canvas) {
			canvas.drawRoundRect(fBounds, roundCornersFactor, roundCornersFactor, backgroundPaint);
			canvas.drawText(text, backgroundPadding.left + MAGIC_PADDING_LEFT, textContentPain.getTextSize() + backgroundPadding.top - MAGIC_PADDING_BOTTOM, textContentPain);
		}

		@Override
		public void setAlpha(int alpha) {
			textContentPain.setAlpha(alpha);
			backgroundPaint.setAlpha(alpha);
		}

		@Override
		public void setColorFilter(ColorFilter cf) {
			textContentPain.setColorFilter(cf);
		}

		@Override
		public int getOpacity() {
			return PixelFormat.TRANSLUCENT;
		}
	}
}