package com.pindroid.ui;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pindroid.R;
import com.pindroid.event.AccountChangedEvent;
import com.pindroid.util.AccountHelper;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

@EViewGroup(R.layout.account_spinner)
public class AccountSpinner extends LinearLayout {
    @ViewById(R.id.account_button) ImageView accountSpinnerButton;
    @ViewById(R.id.account_list) LinearLayout accountList;
    @ViewById(R.id.account_selected) TextView accountSelected;

    boolean accountSpinnerOpen;

    public AccountSpinner(Context context) {
        super(context);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(sticky = true)
    public void onAccountChanged(AccountChangedEvent event) {
        accountSelected.setText(event.getNewAccount());
        accountList.removeAllViews();

        final List<String> accounts = AccountHelper.getAccountNames(getContext());
        accounts.remove(event.getNewAccount());

        if(accounts.size() > 0) {

            accountSpinnerButton.setVisibility(View.VISIBLE);

            for (String account : accounts) {
                View accountView = ((Activity)getContext()).getLayoutInflater().inflate(R.layout.account_list_view, null);
                ((TextView) accountView.findViewById(R.id.account_title)).setText(account);

                accountView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EventBus.getDefault().postSticky(new AccountChangedEvent(((TextView) v.findViewById(R.id.account_title)).getText().toString()));
                    }
                });

                accountList.addView(accountView);
            }

            setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        toggle(true);
                    }
                });
        }

        close(false);
    }

    public void close(boolean animate) {
        if(accountSpinnerOpen) {
            toggle(animate);
        }
    }

    public void toggle(boolean animate) {
        accountSpinnerButton.setImageResource(accountSpinnerOpen ? R.drawable.ic_arrow_drop_down : R.drawable.ic_arrow_drop_up);

        if(animate) {
            ResizeAnimation animation;

            if (accountSpinnerOpen) {
                animation = new ResizeAnimation(accountList, accountList.getChildCount() * (int) getResources().getDimension(R.dimen.account_list_height), 0, true, getResources().getDisplayMetrics());
            } else {
                animation = new ResizeAnimation(accountList, 0, accountList.getChildCount() * (int) getResources().getDimension(R.dimen.account_list_height), true, getResources().getDisplayMetrics());
            }

            animation.setDuration(200);
            accountList.startAnimation(animation);
        } else {
            accountList.getLayoutParams().height = accountSpinnerOpen ? 0 : accountList.getChildCount() * (int) getResources().getDimension(R.dimen.account_list_height);
        }

        accountSpinnerOpen = !accountSpinnerOpen;
    }
}
