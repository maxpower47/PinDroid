package com.pindroid.listadapter;

import android.content.Context;
import android.database.Cursor;
import android.view.ContextMenu;
import android.view.View;
import android.view.ViewGroup;

import com.pindroid.event.BookmarkSelectedEvent;
import com.pindroid.platform.BookmarkManager;
import com.pindroid.providers.BookmarkContent;
import com.pindroid.ui.BookmarkView;
import com.pindroid.ui.BookmarkView_;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import de.greenrobot.event.EventBus;

@EBean
public class BookmarkAdapter extends RecyclerCursorAdapter<BookmarkContent.Bookmark, BookmarkView> {

    @RootContext Context context;

    public BookmarkAdapter(Context context) {
        super(context, null);
    }

    @Override
    protected BookmarkView onCreateItemView(ViewGroup parent, int viewType) {
        return BookmarkView_.build(context);
    }

    @Override
    public void onBindViewHolder(BookmarkView view, Cursor c) {
        final BookmarkContent.Bookmark bookmark = BookmarkManager.CursorToBookmark(c);
        view.bind(bookmark);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new BookmarkSelectedEvent(bookmark));
            }
        });


        view.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
                contextMenu.setHeaderTitle("Actions");
                //MenuInflater inflater = context.getMenuInflater();

                //inflater.inflate(R.menu.browse_bookmark_context_menu_self, contextMenu);
            }
        });
    }
}