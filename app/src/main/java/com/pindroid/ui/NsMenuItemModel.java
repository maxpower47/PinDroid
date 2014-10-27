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

/**
 * 
 * Model per item menu
 * 
 * @author gabriele
 *
 */
public class NsMenuItemModel {

	public int title;
    public String stringTitle;
	public int iconRes;
	public int counter;
	public boolean isHeader;

	public NsMenuItemModel(int title, int iconRes, boolean header, int counter) {
		this.title = title;
		this.iconRes = iconRes;
		this.isHeader = header;
		this.counter = counter;
	}

    public NsMenuItemModel(String title, int iconRes, boolean header, int counter) {
        this.stringTitle = title;
        this.iconRes = iconRes;
        this.isHeader = header;
        this.counter = counter;
    }
	
	public NsMenuItemModel(int title, int iconRes, boolean header){
		this(title, iconRes, header, 0);
	}

    public NsMenuItemModel(String title, int iconRes, boolean header){
        this(title, iconRes, header, 0);
    }
	
	public NsMenuItemModel(int title, int iconRes) {
		this(title, iconRes, false);
	}

    public NsMenuItemModel(String title, int iconRes) {
        this(title, iconRes, false);
    }
	
	public NsMenuItemModel(int title) {
		this(title, 0, false);
	}

    public NsMenuItemModel(String title) {
        this(title, 0, false);
    }
	
	public void setCounter(int counter){
		this.counter = counter;
	}
	
}