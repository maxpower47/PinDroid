package com.pindroid.client;

import android.accounts.AuthenticatorException;
import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.pindroid.Constants;
import com.pindroid.event.AuthenticationEvent;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.utils.URLEncodedUtils;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import de.greenrobot.event.EventBus;
import retrofit.ErrorHandler;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

public class PinboardClient {
	private static PinboardService REST_CLIENT;
	private static String ROOT = "https://api.pinboard.in";

	private static final String[] DATE_FORMATS = new String[] {
			"yyyy-MM-dd'T'HH:mm:ss'Z'",
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
								return new SimpleDateFormat(format, Locale.US).parse(json.getAsString());
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
						if (HttpStatus.SC_UNAUTHORIZED == r.getStatus()) {

							for(NameValuePair n : URLEncodedUtils.parse(URI.create(r.getUrl()), Charset.defaultCharset().name())) {
								if("auth_token".equals(n.getName())) {
									EventBus.getDefault().post(new AuthenticationEvent(n.getValue()));
								}
							}

							return new AuthenticationException();
						} else if (Constants.HTTP_STATUS_TOO_MANY_REQUESTS == r.getStatus()) {
							return new TooManyRequestsException(300);
						} else if (HttpStatus.SC_REQUEST_URI_TOO_LONG == r.getStatus()) {
							return new PinboardException();
						}

						return cause;
					}
				})
				.setLogLevel(RestAdapter.LogLevel.FULL)
				.build();

		REST_CLIENT = prodAdapter.create(PinboardService.class);
	}
}
