package com.synacor.google.calendar.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets.Details;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar.Events;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;


@RestController
public class GoogleCalendarAPIController {
	

	private final static Log logger = LogFactory.getLog(GoogleCalendarAPIController.class);
	private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";
	private static HttpTransport httpTransport;
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static com.google.api.services.calendar.Calendar client;

	GoogleClientSecrets clientSecrets;
	GoogleAuthorizationCodeFlow flow;
	Credential credential;

	@Value("${google.client.client-id}")
	private String clientId;
	@Value("${google.client.client-secret}")
	private String clientSecret;
	@Value("${google.client.redirectUri}")
	private String redirectURI;

	private Set<Event> events = new HashSet<>();

	final DateTime date1 = new DateTime("2017-05-05T16:30:00.000+05:30");
	final DateTime date2 = new DateTime(new Date());

	public void setEvents(Set<Event> events) {
		this.events = events;
	}
	
	@GetMapping(value = "/test")
	public String test() {
		System.out.println("Indside");
		return "Works!!!!";
	}

	@RequestMapping(value = "/login/google", method = RequestMethod.GET)
	public RedirectView googleConnectionStatus(HttpServletRequest request) throws Exception {
		return new RedirectView(authorize());
	}

	@RequestMapping(value = "/Callback", method = RequestMethod.GET, params = "code")
	public ResponseEntity<String> oauth2Callback(@RequestParam(value = "code") String code) {
		System.out.println("Inside");
		com.google.api.services.calendar.model.Events eventList;
		String message;
		try {
			TokenResponse response = flow.newTokenRequest(code).setRedirectUri(redirectURI).execute();
			credential = flow.createAndStoreCredential(response, "userID");
			client = new com.google.api.services.calendar.Calendar.Builder(httpTransport, JSON_FACTORY, credential)
					.setApplicationName(APPLICATION_NAME).build();
			Events events = client.events();
			eventList = events.list("primary").setTimeMin(date1).setTimeMax(date2).execute();
			message = eventList.getItems().toString();
			System.out.println("My:" + eventList.getItems());
		} catch (Exception e) {
			logger.warn("Exception while handling OAuth2 callback (" + e.getMessage() + ")."
					+ " Redirecting to google connection status page.");
			message = "Exception while handling OAuth2 callback (" + e.getMessage() + ")."
					+ " Redirecting to google connection status page.";
		}

		System.out.println("cal message:" + message);
		return new ResponseEntity<>(message, HttpStatus.OK);
	}

	public Set<Event> getEvents() throws IOException {
		return this.events;
	}

	private String authorize() throws Exception {
		AuthorizationCodeRequestUrl authorizationUrl;
		if (flow == null) {
			Details web = new Details();
			web.setClientId(clientId);
			web.setClientSecret(clientSecret);
			//
			web.setAuthUri("https://accounts.google.com/o/oauth2/auth");
			List<String> redirectURIs = new ArrayList<>();
			redirectURIs.add("http://localhost:8888/Callback");
			web.setRedirectUris(redirectURIs);
			web.setTokenUri("https://oauth2.googleapis.com/token");
			web.set("project_id", "gogolecalapi-1597062063645");
			web.set("auth_provider_x509_cert_url", "https://www.googleapis.com/oauth2/v1/certs");
			clientSecrets = new GoogleClientSecrets().setWeb(web);
			httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets,
					Collections.singleton(CalendarScopes.CALENDAR)).build();
		}
		authorizationUrl = flow.newAuthorizationUrl().setRedirectUri(redirectURI);
		System.out.println("cal authorizationUrl->" + authorizationUrl);
		return authorizationUrl.build();
		
//		Credential authorize = new GoogleCredential.Builder().setTransport(GoogleNetHttpTransport.newTrustedTransport())
//				.setJsonFactory(JSON_FACTORY)
//				.setClientSecrets(clientSecrets.getDetails().getClientId().toString(),
//									clientSecrets.getDetails().getClientSecret().toString())
//				.build()
//				.setAccessToken("ya29.a0AfH6SMAmN-IVz19_Q_SK7vLRWgfVV1nsO1mTN9siySr-2ayp4cMdUwU5MM74U59VTem79MgTjMUeTP24MSmEjc1cvdeeTl8rJpm7_XPkEDSQldEu5km7b4TT2A-kQAPjs5CWLh2BN_GpVtbkaKYxTtDEnFdULRt1_nSf")
//				.setRefreshToken("1//0g59kdOYVEsprCgYIARAAGBASNwF-L9Ir5GNzoIxv6sV6s7_0gZq_9i5_MdTSCA-NbM91GcqEt9d6wEJB8w-qvHyHg423DFwuEjQ");
		
//		{"web":{
//		"auth_uri":"https://accounts.google.com/o/oauth2/auth",
//		"client_id":"172655689908-rlrvcionqhcuukaoh3lqkh7veldg0naa.apps.googleusercontent.com",
//		"client_secret":"UAPPKXUIVb8SG5fshkpUWrbK","redirect_uris":["http://localhost:8888/Callback"],
//		"token_uri":"https://oauth2.googleapis.com/token",
//		"project_id":"gogolecalapi-1597062063645",
//		"auth_provider_x509_cert_url":"https://www.googleapis.com/oauth2/v1/certs"
//			}
//	}
//				
	}

}
