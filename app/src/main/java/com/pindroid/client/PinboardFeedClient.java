package com.pindroid.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

public class PinboardFeedClient {
	private static PinboardFeedService REST_CLIENT;
	private static final String ROOT = "https://feeds.pinboard.in";

	private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

	static {
		setupRestClient();
	}

	private PinboardFeedClient() {}

	public static PinboardFeedService get() {
		return REST_CLIENT;
	}

	private static void setupRestClient() {


		Gson gson = new GsonBuilder()
				.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
					@Override
					public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
						try {
							return new SimpleDateFormat(DATE_FORMAT, Locale.US).parse(json.getAsString());
						} catch (ParseException e) {
						}
						throw new JsonParseException("Unparseable date: \"" + json.getAsString()
								+ "\". Supported format: " + DATE_FORMAT);
					}
				})
				.create();

		RestAdapter prodAdapter = new RestAdapter.Builder()
				.setEndpoint(ROOT)
				.setConverter(new GsonConverter(gson))
				.setRequestInterceptor(new RequestInterceptor() {
					@Override
					public void intercept(RequestFacade request) {
						request.addHeader("User-Agent", "PinDroid");
					}
				})
				.setLogLevel(RestAdapter.LogLevel.FULL)
				.build();

		REST_CLIENT = prodAdapter.create(PinboardFeedService.class);
	}
}
