package com.pindroid.application;

import android.app.Application;

public class PindroidApplication extends Application {
	
	private String username = "";
	
	public String getUsername(){
		return username;
	}

	public void setUsername(String username){
		this.username = username;
	}
}
