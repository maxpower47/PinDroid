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

package com.pindroid.fragment;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.pindroid.Constants;
import com.pindroid.Constants.BookmarkViewType;
import com.pindroid.R;
import com.pindroid.action.GetArticleTextTask;
import com.pindroid.action.GetWebpageTitleTask;
import com.pindroid.activity.FragmentBaseActivity;
import com.pindroid.client.NetworkUtilities;
import com.pindroid.fragment.AddBookmarkFragment.GetTitleTask;
import com.pindroid.fragment.BrowseBookmarksFragment.OnBookmarkSelectedListener;
import com.pindroid.platform.BookmarkManager;
import com.pindroid.providers.ContentNotFoundException;
import com.pindroid.providers.ArticleContent.Article;
import com.pindroid.providers.BookmarkContent.Bookmark;
import com.pindroid.providers.TagContent.Tag;
import com.pindroid.ui.AccountSpan;
import com.pindroid.ui.TagSpan;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

public class ViewBookmarkFragment extends Fragment {

	private FragmentBaseActivity base;
	
	private ScrollView mBookmarkView;
	private TextView mTitle;
	private TextView mUrl;
	private TextView mNotes;
	private TextView mTags;
	private TextView mTime;
	private TextView mUsername;
	private ImageView mIcon;
	private WebView mWebContent;
	private Bookmark bookmark;
	private BookmarkViewType viewType;
	private View readSection;
	private TextView readTitle;
	private TextView readView;
	
	private AsyncTask<String, Integer, Article> articleTask;
	
	private OnBookmarkActionListener bookmarkActionListener;
	private OnBookmarkSelectedListener bookmarkSelectedListener;
	
	public interface OnBookmarkActionListener {
		public void onViewTagSelected(String tag);
		public void onUserTagSelected(String tag, String user);
		public void onAccountSelected(String account);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		
		base = (FragmentBaseActivity)getActivity();
		
		mBookmarkView = (ScrollView) getView().findViewById(R.id.bookmark_scroll_view);
		mTitle = (TextView) getView().findViewById(R.id.view_bookmark_title);
		mUrl = (TextView) getView().findViewById(R.id.view_bookmark_url);
		mNotes = (TextView) getView().findViewById(R.id.view_bookmark_notes);
		mTags = (TextView) getView().findViewById(R.id.view_bookmark_tags);
		mTime = (TextView) getView().findViewById(R.id.view_bookmark_time);
		mUsername = (TextView) getView().findViewById(R.id.view_bookmark_account);
		mIcon = (ImageView) getView().findViewById(R.id.view_bookmark_icon);
		mWebContent = (WebView) getView().findViewById(R.id.web_view);
		readSection = getView().findViewById(R.id.read_bookmark_section);
		readTitle = (TextView) getView().findViewById(R.id.read_bookmark_title);
		readView = (TextView) getView().findViewById(R.id.read_view);
		
		mWebContent.getSettings().setJavaScriptEnabled(true);
		
		setHasOptionsMenu(true);
		setRetainInstance(true);
	}
	
    TagSpan.OnTagClickListener tagOnClickListener = new TagSpan.OnTagClickListener() {
        public void onTagClick(String tag) {
    		bookmarkActionListener.onViewTagSelected(tag);
        }
    };
    
    TagSpan.OnTagClickListener userTagOnClickListener = new TagSpan.OnTagClickListener() {
        public void onTagClick(String tag) {
        	bookmarkActionListener.onUserTagSelected(tag, bookmark.getAccount());
        }
    };
    
    AccountSpan.OnAccountClickListener accountOnClickListener = new AccountSpan.OnAccountClickListener() {
        public void onAccountClick(String account) {
        	bookmarkActionListener.onAccountSelected(account);
        }
    };
    
	public void setBookmark(Bookmark b, BookmarkViewType viewType) {
		this.viewType = viewType;
		bookmark = b;
		
		if(android.os.Build.VERSION.SDK_INT >= 11) {
			this.getActivity().invalidateOptionsMenu();
		}
	}
	
	private void addTag(SpannableStringBuilder builder, Tag t, TagSpan.OnTagClickListener listener) {
		int flags = 0;
		
		if (builder.length() != 0) {
			builder.append("  ");
		}
		
		int start = builder.length();
		builder.append(t.getTagName());
		int end = builder.length();
		
		TagSpan span = new TagSpan(t.getTagName());
		span.setOnTagClickListener(listener);

		builder.setSpan(span, start, end, flags);
	}
    
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
	    inflater.inflate(R.menu.view_menu, menu);
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		if(bookmark == null || !isMyself()){
			menu.removeItem(R.id.menu_view_editbookmark);
			menu.removeItem(R.id.menu_view_deletebookmark);
		}
		if(bookmark == null){
			menu.removeItem(R.id.menu_view_sendbookmark);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
		    case R.id.menu_view_details:
				bookmarkSelectedListener.onBookmarkView(bookmark);
				return true;
		    case R.id.menu_view_read:
		    	if(isMyself() && bookmark.getToRead() && base.markAsRead)
		    		bookmarkSelectedListener.onBookmarkMark(bookmark);
				bookmarkSelectedListener.onBookmarkRead(bookmark);
				return true;
		    case R.id.menu_view_openbookmark:
		    	bookmarkSelectedListener.onBookmarkOpen(bookmark);
				return true;
		    case R.id.menu_view_editbookmark:
		    	bookmarkSelectedListener.onBookmarkEdit(bookmark);
		    	return true;
		    case R.id.menu_view_deletebookmark:
		    	bookmarkSelectedListener.onBookmarkDelete(bookmark);
				return true;
		    case R.id.menu_view_sendbookmark:
		    	bookmarkSelectedListener.onBookmarkShare(bookmark);
		    	return true;
		    default:
		        return super.onOptionsItemSelected(item);
	    }
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.view_bookmark_fragment, container, false);
    }
    
    private boolean isMyself() {
    	return bookmark != null && bookmark.getId() != 0;
    }
    
    @Override
    public void onStart(){
    	super.onStart();
    	
    	loadBookmark();
    }

    
    public void loadBookmark(){
    	if(bookmark != null){
    		
    		if(isMyself() && bookmark.getId() != 0){
				try{		
					int id = bookmark.getId();
					bookmark = BookmarkManager.GetById(id, base);
				}
				catch(ContentNotFoundException e){}
    		}
    		
    		if(viewType == BookmarkViewType.VIEW){
				mBookmarkView.setVisibility(View.VISIBLE);
				readSection.setVisibility(View.GONE);
				if(isMyself()){
					Date d = new Date(bookmark.getTime());
					
					mTitle.setText(bookmark.getDescription());
					mUrl.setText(bookmark.getUrl());
					mNotes.setText(bookmark.getNotes());
					mTime.setText(d.toString());
					mUsername.setText(bookmark.getAccount());
					
					if(mIcon != null){
						if(!bookmark.getShared()) {
							mIcon.setImageResource(R.drawable.padlock);
						} else if(bookmark.getToRead()) {
							mIcon.setImageResource(R.drawable.book_open);
						}
					}
					
	        		SpannableStringBuilder tagBuilder = new SpannableStringBuilder();
	
	        		for(Tag t : bookmark.getTags()) {
	        			addTag(tagBuilder, t, tagOnClickListener);
	        		}
	        		
	        		mTags.setText(tagBuilder);
	        		mTags.setMovementMethod(LinkMovementMethod.getInstance());
				} else {
					
					Date d = new Date(bookmark.getTime());
					
					if(!bookmark.getDescription().equals("null"))
						mTitle.setText(bookmark.getDescription());
					
					mUrl.setText(bookmark.getUrl());
					
					if(!bookmark.getNotes().equals("null"))
							mNotes.setText(bookmark.getNotes());
					
					mTime.setText(d.toString());
					
		    		SpannableStringBuilder tagBuilder = new SpannableStringBuilder();
		
		    		for(Tag t : bookmark.getTags()) {
		    			addTag(tagBuilder, t, userTagOnClickListener);
		    		}
		    		
		    		mTags.setText(tagBuilder);
		    		mTags.setMovementMethod(LinkMovementMethod.getInstance());
		
					SpannableStringBuilder builder = new SpannableStringBuilder();
					int start = builder.length();
					builder.append(bookmark.getAccount());
					int end = builder.length();
					
					AccountSpan span = new AccountSpan(bookmark.getAccount());
					span.setOnAccountClickListener(accountOnClickListener);
		
					builder.setSpan(span, start, end, 0);
					
					mUsername.setText(builder);
					mUsername.setMovementMethod(LinkMovementMethod.getInstance());
				}
			} else if(viewType == BookmarkViewType.READ){
				
				//String readUrl = Constants.TEXT_EXTRACTOR_URL + URLEncoder.encode(bookmark.getUrl());
				//mWebContent.loadUrl(readUrl);
				//mWebContent.setWebViewClient(new WebClient());
				

				
				articleTask = new GetArticleTask().execute(bookmark.getUrl());
				

				

			} else if(viewType == BookmarkViewType.WEB){
				mWebContent.clearView();
				mWebContent.clearCache(true);
				mBookmarkView.setVisibility(View.GONE);
				mWebContent.setVisibility(View.VISIBLE);
				mWebContent.loadUrl(bookmark.getUrl());
			}
    	}
    }
    
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			bookmarkActionListener = (OnBookmarkActionListener) activity;
			bookmarkSelectedListener = (OnBookmarkSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnBookmarkActionListener and OnBookmarkSelectedListener");
		}
	}
	
	@Override
	public void onResume(){
		super.onResume();
		mWebContent.getSettings().setDefaultFontSize(Integer.parseInt(base.readingFontSize));
	}
	
    public class GetArticleTask extends GetArticleTextTask{
    	protected void onPreExecute(){
			mWebContent.clearView();
			mWebContent.clearCache(true);
			mWebContent.setBackgroundColor(Color.BLACK);
			mWebContent.getSettings().setDefaultFontSize(Integer.parseInt(base.readingFontSize));

			//mBookmarkView.setVisibility(View.VISIBLE);
			//mWebContent.setVisibility(View.GONE);
			//readSection.setVisibility(View.VISIBLE);
    	}
    	
        protected void onPostExecute(Article result) {
        	//readTitle.setText(result.getTitle());
        	//readView.setText(Html.fromHtml(result.getContent()));
        	mBookmarkView.setVisibility(View.GONE);
			readSection.setVisibility(View.VISIBLE);
			readTitle.setText(result.getTitle());
			
			String html = "Hello " +
			"<img src='http://www.gravatar.com/avatar/" + 
			"f9dd8b16d54f483f22c0b7a7e3d840f9?s=32&d=identicon&r=PG'/>" +
			" This is a test " +
			"<img src='http://www.gravatar.com/avatar/a9317e7f0a78bb10a980cadd9dd035c9?s=32&d=identicon&r=PG'/>";
			
			URLImageParser p = new URLImageParser(readView, base);
			Spanned htmlSpan = Html.fromHtml(result.getContent(), p, null);
			
			
			
			
			readView.setText(htmlSpan);
        	//mWebContent.loadDataWithBaseURL("file:///android_asset/", "<html><head><link rel='stylesheet' type='text/css' href='style.css' /></head><body>" + result.getContent() + "</body></html>", "text/html", "utf-8", null);
        }
    }
    


	
	
	
	
	
	public class URLDrawable extends BitmapDrawable {
	    // the drawable that you need to set, you could set the initial drawing
	    // with the loading image if you need to
	    protected Drawable drawable;

	    @Override
	    public void draw(Canvas canvas) {
	        // override the draw to facilitate refresh function later
	        if(drawable != null) {
	            drawable.draw(canvas);
	        }
	    }
	}
	
	
	public class URLImageParser implements ImageGetter {
	    Context c;
	    View container;

	    /***
	     * Construct the URLImageParser which will execute AsyncTask and refresh the container
	     * @param t
	     * @param c
	     */
	    public URLImageParser(View t, Context c) {
	        this.c = c;
	        this.container = t;
	    }

	    public Drawable getDrawable(String source) {
	        URLDrawable urlDrawable = new URLDrawable();

	        // get the actual source
	        ImageGetterAsyncTask asyncTask = new ImageGetterAsyncTask(urlDrawable);

	        asyncTask.execute(source);

	        // return reference to URLDrawable where I will change with actual image from
	        // the src tag
	        return urlDrawable;
	    }

	    public class ImageGetterAsyncTask extends AsyncTask<String, Void, Drawable>  {
	        URLDrawable urlDrawable;

	        public ImageGetterAsyncTask(URLDrawable d) {
	            this.urlDrawable = d;
	        }

	        @Override
	        protected Drawable doInBackground(String... params) {
	            String source = params[0];
	            return fetchDrawable(source);
	        }

	        @Override
	        protected void onPostExecute(Drawable result) {
	            // set the correct bound according to the result from HTTP call
	            urlDrawable.setBounds(0, 0, 0 + result.getIntrinsicWidth(), 0 
	                    + result.getIntrinsicHeight()); 

	            // change the reference of the current drawable to the result
	            // from the HTTP call
	            urlDrawable.drawable = result;

	            // redraw the image by invalidating the container
	            URLImageParser.this.container.invalidate();
	        }

	        /***
	         * Get the Drawable from URL
	         * @param urlString
	         * @return
	         */
	        public Drawable fetchDrawable(String urlString) {
	            try {
	                InputStream is = fetch(urlString);
	                Drawable drawable = Drawable.createFromStream(is, "src");
	                drawable.setBounds(0, 0, 0 + drawable.getIntrinsicWidth(), 0 
	                        + drawable.getIntrinsicHeight()); 
	                return drawable;
	            } catch (Exception e) {
	                return null;
	            } 
	        }

	        private InputStream fetch(String urlString) throws MalformedURLException, IOException {
	            DefaultHttpClient httpClient = new DefaultHttpClient();
	            HttpGet request = new HttpGet(urlString);
	            HttpResponse response = httpClient.execute(request);
	            return response.getEntity().getContent();
	        }
	    }
	}
}