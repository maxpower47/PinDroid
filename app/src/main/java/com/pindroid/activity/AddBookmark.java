package com.pindroid.activity;

import android.content.Intent;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;

import com.pindroid.R;
import com.pindroid.fragment.AddBookmarkFragment;
import com.pindroid.platform.BookmarkManager;
import com.pindroid.providers.BookmarkContent.Bookmark;
import com.pindroid.util.SettingsHelper;
import com.pindroid.util.StringUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.FragmentById;

@EActivity(R.layout.activity_add_bookmark)
public class AddBookmark extends AppCompatActivity implements AddBookmarkFragment.OnBookmarkSaveListener {

    @FragmentById(R.id.addbookmark_fragment) AddBookmarkFragment addBookmarkFragment;
	@Extra("username") String username;
	@Extra("bookmark") Bookmark bookmark;
	@Extra("oldBookmark") Bookmark oldBookmark;

    @AfterViews
    protected void init() {
        processIntent(getIntent());
        addBookmarkFragment.setUsername(username);
        addBookmarkFragment.loadBookmark(bookmark, oldBookmark);
    }

    private void processIntent(Intent intent){
        if(Intent.ACTION_SEND.equals(intent.getAction())){
            bookmark = findExistingBookmark(loadBookmarkFromShareIntent());
        }
    }

    private Bookmark loadBookmarkFromShareIntent() {
        Bookmark bookmark = new Bookmark();
        ShareCompat.IntentReader reader = ShareCompat.IntentReader.from(this);

        if(reader.getText() != null){
            String url = StringUtils.getUrl(reader.getText().toString());
            bookmark.setUrl(url);
        }

        if(reader.getSubject() != null) {
            bookmark.setDescription(reader.getSubject());
        }

        bookmark.setToRead(SettingsHelper.getToReadDefault(this));
        bookmark.setShared(!SettingsHelper.getPrivateDefault(this));

        return bookmark;
    }

    private Bookmark findExistingBookmark(Bookmark bookmark) {

        try{
            Bookmark old = BookmarkManager.GetByUrl(bookmark.getUrl(), username, this);
            bookmark = old.copy();
        } catch(Exception e) {
        }

        return bookmark;
    }

    @Override
    public void onBookmarkSave(Bookmark b) {
        finish();
    }

    @Override
    public void onBookmarkCancel(Bookmark b) {
        finish();
    }
}
