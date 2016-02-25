package com.pindroid.client;

import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.pindroid.BuildConfig;
import com.pindroid.Constants;
import com.pindroid.event.AuthenticationEvent;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import org.greenrobot.eventbus.EventBus;
import retrofit.ErrorHandler;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

public class PinboardClient {
	private static PinboardService REST_CLIENT;
	private static final String ROOT = "https://api.pinboard.in";

	private static final String[] DATE_FORMATS = new String[] {
			"yyyy-MM-dd'T'HH:mm:ssZ",
			"yyyy-MM-dd HH:mm:ss"
	};

	static {
		setupRestClient();
	}

	private PinboardClient() {}

	public static PinboardService get() {
		return REST_CLIENT;
	}

	private static void setupRestClient() {


		Gson gson = new GsonBuilder()
				.registerTypeAdapter(Boolean.class, new JsonDeserializer<Boolean>() {
					@Override
					public Boolean deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
						try {
							String value = json.getAsJsonPrimitive().getAsString();
							return value.toLowerCase().equals("yes");
						} catch (ClassCastException e) {
							throw new JsonParseException("Cannot parse json boolean '" + json.toString() + "'", e);
						}
					}
				})
				.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
					@Override
					public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
						for (String format : DATE_FORMATS) {
							try {
								return new SimpleDateFormat(format, Locale.US).parse(json.getAsString().replace("Z", "+0000"));
							} catch (ParseException e) {
							}
						}
						throw new JsonParseException("Unparseable date: \"" + json.getAsString()
								+ "\". Supported formats: " + Arrays.toString(DATE_FORMATS));
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
						request.addQueryParam("format", "json");
					}
				})
				.setErrorHandler(new ErrorHandler() {
					@Override
					public Throwable handleError(RetrofitError cause) {
						Response r = cause.getResponse();
						if (Constants.HTTP_STATUS_UNAUTHORIZED == r.getStatus()) {

                            Uri uri = Uri.parse(r.getUrl());
                            EventBus.getDefault().post(new AuthenticationEvent(uri.getQueryParameter("auth_token")));

							return new AuthenticationException();
						} else if (Constants.HTTP_STATUS_TOO_MANY_REQUESTS == r.getStatus()) {
							return new TooManyRequestsException(300);
						} else if (Constants.HTTP_STATUS_REQUEST_URI_TOO_LONG == r.getStatus()) {
							return new PinboardException();
						}

						return cause;
					}
				})
				.setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.BASIC)
				.build();

		REST_CLIENT = prodAdapter.create(PinboardService.class);
	}
}
