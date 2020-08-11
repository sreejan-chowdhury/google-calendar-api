package com.synacor.google.calendar.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.parameter.XParameter;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.model.property.XProperty;

public class CalendarQuickstart {
    private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/client_secret.json";

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = CalendarQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        
    }

    public static void main(String... args) throws IOException, GeneralSecurityException {
    	
    	
//    	CacheManager cacheManager = Caching.getCachingProvider().getCacheManager();
//    	MutableConfiguration<String, Date> config = new MutableConfiguration<String, Date>();
//
//    	config.setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(Duration.ONE_MINUTE)).setStatisticsEnabled(true);
//    	cacheManager.createCache("simpleCache",config);
//    	Cache<String, Date> cache= cacheManager.getCache("simpleCache");
    	
    	
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        
        
     // Iterate through entries in calendar list
        String pageToken = null;
        do {
          CalendarList calendarList = service.calendarList().list().setPageToken(pageToken).execute();
          List<CalendarListEntry> items = calendarList.getItems();

          for (CalendarListEntry calendarListEntry : items) {
            System.out.println(calendarListEntry.getSummary() +" --- " +calendarListEntry.getId());
            //create the file per calendar basis
            createICS(calendarListEntry,service);
            
          }
          pageToken = calendarList.getNextPageToken();
        } while (pageToken != null);

        // List the next 10 events from the primary calendar.
//        DateTime now = new DateTime(0);//System.currentTimeMillis()
//        Events events = service.events().list("primary")
//                .setMaxResults(10)
//                .setTimeMin(now)
//                .setOrderBy("startTime")
//                .setSingleEvents(true)
//                .execute();
//        List<Event> items = events.getItems();
//        if (items.isEmpty()) {
//            System.out.println("No upcoming events found.");
//        } else {
//            System.out.println("Upcoming events");
//            for (Event event : items) {
//                DateTime start = event.getStart().getDateTime();
//                if (start == null) {
//                    start = event.getStart().getDate();
//                }
//                System.out.printf("%s (%s)\n", event.getSummary(), start);
//            }
//        }
    }
    
    
//    BEGIN:VCALENDAR
//    PRODID:-//Google Inc//Google Calendar 70.9054//EN
//    VERSION:2.0
//    CALSCALE:GREGORIAN
//    METHOD:PUBLISH
//    BEGIN:VTIMEZONE
//    TZID:Asia/Kolkata
//    TZURL:http://tzurl.org/zoneinfo/Asia/Kolkata
//    X-LIC-LOCATION:Asia/Kolkata
//    BEGIN:STANDARD
//    TZOFFSETFROM:+055328
//    TZOFFSETTO:+055320
//    TZNAME:HMT
//    DTSTART:18540628T000000
//    RDATE:18540628T000000
//    END:STANDARD
//    BEGIN:STANDARD
//    TZOFFSETFROM:+055320
//    TZOFFSETTO:+052110
//    TZNAME:MMT
//    DTSTART:18700101T000000
//    RDATE:18700101T000000
//    END:STANDARD
//    BEGIN:STANDARD
//    TZOFFSETFROM:+052110
//    TZOFFSETTO:+0530
//    TZNAME:IST
//    DTSTART:19060101T000850
//    RDATE:19060101T000850
//    END:STANDARD
//    BEGIN:DAYLIGHT
//    TZOFFSETFROM:+0530
//    TZOFFSETTO:+0630
//    TZNAME:+0630
//    DTSTART:19411001T000000
//    RDATE:19411001T000000
//    RDATE:19420901T000000
//    END:DAYLIGHT
//    BEGIN:STANDARD
//    TZOFFSETFROM:+0630
//    TZOFFSETTO:+0530
//    TZNAME:IST
//    DTSTART:19420515T000000
//    RDATE:19420515T000000
//    RDATE:19451015T000000
//    END:STANDARD
//    END:VTIMEZONE
//    END:VCALENDAR
    
    
    private static final String PRODID= "-//Google Inc//Google Calendar 70.9054//EN";
    
    private static void createICS(final CalendarListEntry calendarListEntry,final Calendar service ) throws IOException {
    	
    	//create calendar 
    	
//    	System.out.println("calendarListEntry.toPrettyString() ::: " +calendarListEntry.toPrettyString());
    	
    	
    	
    	TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
        TimeZone timezone = registry.getTimeZone(calendarListEntry.getTimeZone());
        VTimeZone tz = timezone.getVTimeZone();
    	
//    	VTimeZone tz = TimeZone.getTimeZone(calendarListEntry.getTimeZone()); //TIMEZONE.getVTimeZone();
        
    	
    	
    	net.fortuna.ical4j.model.Calendar calendar = new net.fortuna.ical4j.model.Calendar();
    	calendar.getProperties().add(new ProdId(PRODID));
    	calendar.getProperties().add(Version.VERSION_2_0);
    	calendar.getProperties().add(CalScale.GREGORIAN);
    	calendar.getProperties().add(Method.PUBLISH);
    	
//    	XParameter xparam = new XParameter("X-WR-CALNAME", "Testing COntent");
//    	XProperty xprop = new XProperty("X-WR-TIMEZONE", calendarListEntry.getTimeZone());
//        xprop.getParameters().add(xparam);
    	calendar.getProperties().add(new XProperty("X-WR-TIMEZONE", calendarListEntry.getTimeZone()));
    	//add X-WR-CALNAME
//    	xprop = new XProperty("X-WR-CALNAME", "Testing COntent");
//      xprop.getParameters().add(xparam);
    	calendar.getProperties().add(new XProperty("X-WR-CALNAME", "Testing COntent"));
    	
    	
//    	calendar.getProperties().add(new Description("Test COntents 1234"));
//    	calendar.getProperties().add(new Location(this.location));

//    	calendar.getProperties().addAll((Collection<? extends Property>) tz);
//    	calendar.getProperties().addAll((Collection<? extends Property>) TimeZone.getTimeZone(calendarListEntry.getTimeZone()));
//    	calendar.getComponents().addAll((Collection<? extends CalendarComponent>) TimeZone.getTimeZone(calendarListEntry.getTimeZone()));
    	calendar.getComponents().add(tz);
    	
//    	System.out.println("ICalendar "+calendar.toString());
    	
    	String eventPageToken = null;
        do {
          Events events = service.events().list(calendarListEntry.getId()).setPageToken(eventPageToken).execute();
          List<Event> items = events.getItems();
          for (Event event : items) {
//            System.out.println(event.  getSummary());
        	  addEvent(event, calendar);
          }
          eventPageToken = events.getNextPageToken();
        } while (eventPageToken != null);


    	
    }
    
    private static void addEvent(final Event event,net.fortuna.ical4j.model.Calendar calendar) throws IOException {
    	
    	System.out.println("calendarListEntry.toPrettyString() ::: " + event.toPrettyString());
    }
}