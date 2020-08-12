package com.synacor.google.calendar.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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
import com.google.api.services.calendar.model.EntryPoint;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttachment;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.Events;
import com.synacor.google.calendar.constants.PartStatConstants;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.CuType;
import net.fortuna.ical4j.model.parameter.Role;
import net.fortuna.ical4j.model.parameter.XParameter;
import net.fortuna.ical4j.model.property.Attach;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Created;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.LastModified;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.RecurrenceId;
import net.fortuna.ical4j.model.property.Sequence;
import net.fortuna.ical4j.model.property.Status;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Transp;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Url;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.model.property.XProperty;



public class CalendarQuickstart {
	private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final String TOKENS_DIRECTORY_PATH = "tokens";

	/**
	 * Global instance of the scopes required by this quickstart. If modifying these
	 * scopes, delete your previously saved tokens/ folder.
	 */
	private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_READONLY);
	private static final String CREDENTIALS_FILE_PATH = "/client_secret.json";

	/**
	 * Creates an authorized Credential object.
	 * 
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
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, SCOPES)
						.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
						.setAccessType("offline").build();
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
				.setApplicationName(APPLICATION_NAME).build();

		// Iterate through entries in calendar list
		String pageToken = null;
		do {
			CalendarList calendarList = service.calendarList().list().setPageToken(pageToken).execute();
			List<CalendarListEntry> items = calendarList.getItems();

			for (CalendarListEntry calendarListEntry : items) {
				System.out.println(calendarListEntry.getSummary() + " --- " + calendarListEntry.getId());
				// create the file per calendar basis
				createICS(calendarListEntry, service);

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
	
	//specifies who produced this ics file
	private static final String PRODID = "-//Events Calendar//iCal4j 1.0//EN"; //"-//Google Inc//Google Calendar 70.9054//EN";   

	private static final String DEFAULT_TRANSPARENCY = "OPAQUE"; 
	
	private static final String ICS_FILE_EXTENSION = ".ics"; 
	
	private static final String FOLDER_PATH = "/Users/sreejan.chowdhury/Desktop/GoogleCalendarExport";
	
	private static void createICS(final CalendarListEntry calendarListEntry, final Calendar service)
			throws IOException {

		// create calendar

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
		// add X-WR-CALNAME
//    	xprop = new XProperty("X-WR-CALNAME", "Testing COntent");
//      xprop.getParameters().add(xparam);
		calendar.getProperties().add(new XProperty("X-WR-CALNAME", calendarListEntry.getSummary()));

//    	calendar.getProperties().add(new Description("Test COntents 1234"));
//    	calendar.getProperties().add(new Location(this.location));

//    	calendar.getProperties().addAll((Collection<? extends Property>) tz);
//    	calendar.getProperties().addAll((Collection<? extends Property>) TimeZone.getTimeZone(calendarListEntry.getTimeZone()));
//    	calendar.getComponents().addAll((Collection<? extends CalendarComponent>) TimeZone.getTimeZone(calendarListEntry.getTimeZone()));
		calendar.getComponents().add(tz);

//    	

		String eventPageToken = null;
		VEvent vEvent = null;
//		if (calendarListEntry.getSummary().contains("sreejan88")) {
			do {
				Events events = service.events().list(calendarListEntry.getId()).setPageToken(eventPageToken).execute();
				List<Event> items = events.getItems();
				int i = 1;
				for (Event event : items) {
//					System.out.println(i + " ---------------- " +event.getSummary() + " ---- "+ event.getStart());
//					if( i == 10) {
						vEvent = addEvent(event);
						
						calendar.getComponents().add(vEvent);
//					}
					i++;
					
				}
				eventPageToken = events.getNextPageToken();
			} while (eventPageToken != null);
			
			calendar.validate();
			
			writeICSFile(calendarListEntry.getSummary(), calendar.toString(), FOLDER_PATH);
//			System.out.println("ICalendar "+calendar.toString());

//		}

	}

	private static VEvent addEvent(final Event event)
			throws IOException {
		
//		System.out.println("--------------NEW-------------------");
		//set to check which fields have been set
		//this is to handle the default / optional values
		Set<String> paramsSet = new HashSet<>();
//		÷if (null == vEvent)
		VEvent vEvent = new VEvent();
//    	System.out.println("calendarListEntry.toPrettyString() ::: " + event.toPrettyString());
		event.forEach((k, v) -> {
//			System.out.println("key :: " + k + " ++Value : " + v + "  Type of value ::" + v.getClass());
			// if type is of value is string...it is a property of the event
//			if (v.getClass().isInstance(String.class)) {
				try {
					addEventProps(k, vEvent, v.toString(),event);
					paramsSet.add(k);
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
//			}

		});
		
		//add the default or optional once
		if(!paramsSet.contains("transparency"))
			vEvent.getProperties().add( new Transp( DEFAULT_TRANSPARENCY ) );
		if(!paramsSet.contains("summary"))
			vEvent.getProperties().add( new Summary("") );

		return vEvent;

	}
	
	/**
	 * 
	 * @param <T>
	 * @param key
	 * @param vEvent
	 * @param value
	 * @param event
	 * @throws URISyntaxException
	 * @throws ParseException
	 * @throws IOException
	 * 
	 * 
	 * 
	 * THERE can be other params like 
	 * X-MICROSOFT-CDO-ALLDAYEVENT:FALSE
		X-MICROSOFT-CDO-APPT-SEQUENCE:2
		X-MICROSOFT-CDO-BUSYSTATUS:TENTATIVE
		X-MICROSOFT-CDO-IMPORTANCE:1
		X-MICROSOFT-CDO-INSTTYPE:0
		X-MICROSOFT-CDO-INTENDEDSTATUS:BUSY
		X-MICROSOFT-CDO-OWNERAPPTID:2118404721
		X-MICROSOFT-DISALLOW-COUNTER:FALSE
		X-MICROSOFT-DONOTFORWARDMEETING:FALSE
		X-MICROSOFT-LOCATIONDISPLAYNAME:Zoom Meeting https://zoom.us/j/6419133414
		X-MICROSOFT-LOCATIONS:[{"DisplayName":"Zoom Meeting https://zoom.us/j/12341241"\,
		"LocationAnnotation":""\,"LocationUri":""\,"LocationStreet":""\,"Lo
		 cationCity":""\,"LocationState":""\,"LocationCountry":""\,"LocationPostalCo
		 de":""\,"LocationFullAddress":""}]
		X-MICROSOFT-LOCATIONSOURCE:None
	 */
	private static <T> void addEventProps(final String key, VEvent vEvent, final T value, final Event event) throws URISyntaxException, ParseException, IOException {
		
//		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
//        LocalDate date = null;
		DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss", Locale.ENGLISH);
        LocalDateTime datetime = null;
        
        //for start and end date
        //this is for datetime pattern
        String pattern = "yyyy-MM-dd'T'HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        String outPattern = "yyyyMMdd'T'HHmmss"; //"yyyyMMdd'T'HHmmss"
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(outPattern); 
		
		TimeZone timezone = null;
		StringBuffer sb = null;
		//to get the description property object already added
		Object descProp = null;
		
		//for conversion to iCal DateTime 
		String date = null;
		//this is for date pattern
		String patternDate = "yyyy-MM-dd";
        SimpleDateFormat sdfDate = new SimpleDateFormat(patternDate);
		
		
		switch(key) {
			case "status":
				vEvent.getProperties().add(new Status( ((String) value).toUpperCase() ));
				break;
				
			case "htmlLink":
				vEvent.getProperties().add(new Url(new URI((String) value)));
				break;
				
			case "created":
				datetime = LocalDateTime.parse((String) value, inputFormatter);
//				datetime.atZone(ZoneId.of("UTC"));
//				vEvent.getProperties().add(new Created( outputFormatter.format(datetime) ) );
				vEvent.getProperties().add(new Created( new DateTime( outputFormatter.format( datetime ) ) ) );
				break;
				
			case "updated":
				datetime = LocalDateTime.parse((String) value, inputFormatter);
//				datetime.atZone(ZoneId.of("UTC"));
				vEvent.getProperties().add(new LastModified( new DateTime( outputFormatter.format( datetime ) ) ) );
				break;
			
			case "summary":
				vEvent.getProperties().add(new Summary( (String) value) );
				break;
				
			case "description":
				//ADD Conference details as
				//if description present start with \n
				//else add as is...e.g
				//-::~:~::~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~
//				 :~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~::~:~::-\nPlease do not edit this secti
//				 on of the description.\n
//				This event has a video call.\nJoin: https://meet.
//					 google.com/cgq-khtn-ini\n-::~:~::~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:~:
//					 ~:~:~:~:~:~:~:~:~:~:~:~:~:~:~::~:~::-
				sb = new StringBuffer();
				sb.append((String) value);
				
				//this is to get the conference data that has already been added, if present
				String alreadyAddedDesc = vEvent.getProperties().getProperty("DESCRIPTION") != null ?
											vEvent.getProperties().getProperty("DESCRIPTION").toString() : null;
											
				//get the object
				descProp = vEvent.getProperties().getProperty("DESCRIPTION");
				if( alreadyAddedDesc != null )
					//since it is this format
					//DESCRIPTION:\n Some text
					//removing the DESCRIPTION: BIT
					sb.append( alreadyAddedDesc.substring( alreadyAddedDesc.indexOf(':') + 1 ) );
				
				//REMOVE THE DESCRIPTION PROPERTY BEFORE ADDING THE UPDATEDD ONE
				if(descProp != null)
					vEvent.getProperties().remove(descProp);
				
				vEvent.getProperties().add(new Description( sb.toString() ) );
				
				
				break;
				
			case "conferenceData":
				
				sb = new StringBuffer();
				//this is to get the description data if it has already been added, if present
				String descIfPresent = vEvent.getProperties().getProperty("DESCRIPTION") != null ?
										vEvent.getProperties().getProperty("DESCRIPTION").toString() : null;
				
				descProp = vEvent.getProperties().getProperty("DESCRIPTION");
				
				if( descIfPresent != null )
					//since it is this format
					//DESCRIPTION:\n Some text
					//removing the DESCRIPTION: BIT
					sb.append( descIfPresent.substring( descIfPresent.indexOf(':') + 1 ) );

				if( event.getConferenceData() != null && 
					event.getConferenceData().getEntryPoints() != null && 
					event.getConferenceData().getEntryPoints().size() > 0
				  ) {
						
						for(EntryPoint ep : event.getConferenceData().getEntryPoints()) {
							
							sb.append("\n\n");
							sb.append(ep.getEntryPointType().toUpperCase());
							
							if(ep.getLabel() != null && ep.getLabel().length() > 0) {
								sb.append("\t");
								sb.append(" Join ");
								sb.append(" : ");
								sb.append(ep.getLabel());
							}
							
							sb.append(" : ");
							sb.append(ep.getUri());
							
							if(ep.getAccessCode() != null && ep.getAccessCode().length() > 0) {
								sb.append("\t");
								sb.append(" accessCode ");
								sb.append(" : ");
								sb.append(ep.getAccessCode());
							}
							
							if(ep.getMeetingCode() != null && ep.getMeetingCode().length() > 0) {
								sb.append("\t");
								sb.append(" meetingCode ");
								sb.append(" : ");
								sb.append(ep.getMeetingCode());
							}
							
							if(ep.getPasscode() != null && ep.getPasscode().length() > 0) {
								sb.append("\t");
								sb.append(" passcode ");
								sb.append(" : ");
								sb.append(ep.getPasscode());
							}
							
							if(ep.getPassword() != null && ep.getPassword().length() > 0) {
								sb.append("\t");
								sb.append(" password ");
								sb.append(" : ");
								sb.append(ep.getPassword());
							}
							
						}
							
					} 
				//REMOVE THE DESCRIPTION PROPERTY BEFORE ADDING THE UPDATEDD ONE
				if(descProp != null)
					vEvent.getProperties().remove(descProp);
				
				vEvent.getProperties().add(new Description( sb.toString() ) );
				
				break;
				
			case "location":
				vEvent.getProperties().add(new Location( (String) value));
				break;
			
				//skipped for now
//			case "colorId":
//				vEvent.getProperties().add(new Color);
//				break;
				
			case "iCalUID":
				vEvent.getProperties().add(new Uid( (String) value));
				break;
				
			case "organizer":
				
				vEvent.getProperties().add(new Organizer());
				((Property) vEvent.getProperties().getProperty(Property.ORGANIZER)).setValue("mailto:"+event.getOrganizer().getEmail());
				((Property) vEvent.getProperties().getProperty(Property.ORGANIZER)).getParameters().add(new Cn(event.getOrganizer().getDisplayName()));
				break;

			case "start":
				//it can be of date and datetime types
				//need to handle both
				if( event.getStart().getTimeZone() != null ) {
					TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
					timezone = registry.getTimeZone(event.getStart().getTimeZone());
				}
				
				
				
				if( event.getStart().getDateTime() != null)
					date = simpleDateFormat.format( sdf.parse( event.getStart().getDateTime().toString() ) );
				
				else
					date = simpleDateFormat.format( sdfDate.parse( event.getStart().getDate().toString() ) );	
						
						
				DtStart startDate = new DtStart(  date, timezone);
				vEvent.getProperties().add( startDate );
				
				break;

			case "end":

				if( event.getEnd().getTimeZone() != null ) {
					TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
					timezone = registry.getTimeZone(event.getEnd().getTimeZone());
				}
				
				
				if( event.getStart().getDateTime() != null)
					date = simpleDateFormat.format( sdf.parse( event.getStart().getDateTime().toString() ) );
				
				else
					date = simpleDateFormat.format( sdfDate.parse( event.getStart().getDate().toString() ) );	
				
				DtEnd endDate = new DtEnd( date, timezone);
				vEvent.getProperties().add( endDate );
				
				break;
//				//TODO
			case "recurrence":
				//string is in following format
				//RRULE:FREQ=WEEKLY
				//Hence removing the RRULE: part
				for( String rRule : event.getRecurrence())
					vEvent.getProperties().add( new RRule( rRule.substring( rRule.indexOf( ":" ) + 1 ) ) );
				break;	

			case "recurringEventId":
				vEvent.getProperties().add(new RecurrenceId( (String) value));
				break;
				//TODO
			case "sequence":
				vEvent.getProperties().add(new Sequence( (String) value));
				break;
				
			case "attendees":
				if( event.getAttendees() != null && 
					event.getAttendees().size() > 0 
				) {
					//TO COMPLETE
					//check this
					//https://developers.google.com/calendar/v3/reference/events#resource
					for(EventAttendee a: event.getAttendees()) {
						Attendee attendee = new Attendee(URI.create("mailto:"+a.getEmail()));
						
						//need to find and map all of the google responses values with the appropriate once
						//e.g INDIVIDUAL
						attendee.getParameters().add(CuType.INDIVIDUAL);
						
						//need to find and map all of the google responses values with the appropriate once
						//e.g REQ_PARTICIPANT
						if(a.getOptional() != null && a.getOptional())
							attendee.getParameters().add(Role.OPT_PARTICIPANT);
						else
							attendee.getParameters().add(Role.REQ_PARTICIPANT);
						
						
						//need to find and map all of the google responses values with the appropriate once
						//e.g needsAction = NEEDS_ACTION;
						//others are
//						ACCEPTED  DECLINED  TENTATIVE  DELEGATED COMPLETED IN-PROCESS
						attendee.getParameters().add( PartStatConstants.getPartStat( a.getResponseStatus() ) );
						
						attendee.getParameters().add(new Cn(a.getEmail()));
						
						attendee.getParameters().add( new XParameter("X-NUM-GUESTS", "0" ) );
						
						//add to the event
						vEvent.getProperties().add(attendee);
					}
					
				}
				
				break;
			//this is an optional field
			//by default it is Opaque
			case "transparency":
				vEvent.getProperties().add(new Transp( (String) value) );
				break;
//				//TODO
//			case "visibility":
//				vEvent.getProperties().add(new Visi( (String) value));
//				break;
			
			case "attachments":
				
				for( EventAttachment ea : event.getAttachments()) {
					Attach attachment = new Attach( URI.create( ea.getFileUrl() ) );
					attachment.getParameters().add(new XParameter("FILENAME", ea.getFileId()) );
					vEvent.getProperties().add(attachment);
				}
				
				break;
				
				
			default:
				break;

		}
		
	}
	
	private static void writeICSFile(final String fileName, final String fileContent, final String path) throws IOException {
		
		BufferedWriter bw = null;
		try {

//            File file = new File(fileName+ICS_FILE_EXTENSION);
//
//            // if file doesnt exists, then create it
//            if (!file.exists()) {
//                file.createNewFile();
//            }
//
//            FileWriter fw = new FileWriter(file.getAbsoluteFile());
//            bw = new BufferedWriter(fw);
//            
			File directory = new File(path);
		    if (! directory.exists()){
		    	// If you require it to make the entire directory path including parents,
		        // use directory.mkdirs(); here instead.
		        directory.mkdir();
		        
		    }
			
            bw = new BufferedWriter(new FileWriter(path+"/"+fileName));;
            
            bw.write(fileContent);
            bw.close();

            bw = null;
            System.out.println("Done");
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
        	
        	if(bw != null)
        		bw.close();
        }
		
	}
	
}