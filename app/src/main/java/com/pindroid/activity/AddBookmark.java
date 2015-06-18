package com.pindroid.activity;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.pindroid.Constants;
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

        if(username == null || "".equals(username)) {
            requestAccount();
        } else {
            getSupportActionBar().setSubtitle(username);
            addBookmarkFragment.setUsername(username);
            addBookmarkFragment.loadBookmark(bookmark, oldBookmark);
        }
    }

    private void handleIntent(){
        if(Intent.ACTION_SEND.equals(getIntent().getAction())){
            bookmark = findExistingBookmark(loadBookmarkFromShareIntent());
            addBookmarkFragment.loadBookmark(bookmark, oldBookmark);
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

    protected void requestAccount() {
        Intent i = AccountManager.newChooseAccountIntent(null, null, new String[]{Constants.ACCOUNT_TYPE}, false, null, null, null, null);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(i, Constants.REQUEST_CODE_ACCOUNT_CHANGE);
    }

    @Override
    public void onBookmarkSave(Bookmark b) {
        finish();
    }

    @Override
    public void onBookmarkCancel(Bookmark b) {
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode != Constants.REQUEST_CODE_ACCOUNT_CHANGE) {
            finish();
        } else {
            if (resultCode == Activity.RESULT_CANCELED) {
                finish();
            } else if (resultCode == Activity.RESULT_OK) {
                username = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                getSupportActionBar().setSubtitle(username);
                handleIntent();
            }
        }
    }
}
