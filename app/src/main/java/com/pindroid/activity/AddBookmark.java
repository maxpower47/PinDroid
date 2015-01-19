package com.pindroid.activity;

import android.support.v7.app.ActionBarActivity;

import com.pindroid.R;
import com.pindroid.fragment.AddBookmarkFragment;
import com.pindroid.providers.BookmarkContent.Bookmark;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.FragmentById;

@EActivity(R.layout.activity_add_bookmark)
public class AddBookmark extends ActionBarActivity implements AddBookmarkFragment.OnBookmarkSaveListener {

    @FragmentById(R.id.addbookmark_fragment) AddBookmarkFragment addBookmarkFragment;
	@Extra("username") String username;
	@Extra("bookmark") Bookmark bookmark;
	@Extra("oldBookmark") Bookmark oldBookmark;

    @AfterViews
    protected void init() {
        addBookmarkFragment.setUsername(username);
        addBookmarkFragment.loadBookmark(bookmark, oldBookmark);
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
