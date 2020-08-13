package com.synacor.google.calendar.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.EntryPoint;
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

public class GenerateICSFiles {

	public static void main(String[] args) throws IOException, InterruptedException {

		String jwtToken = "Bearer ya29.a0AfH6SMCp3oDU1g_apqaORCpeCWJ7seKua9w6dtVuWskDjcZMym00-fzcfIhvvCHjezTzJlOt7y-GxlIDC7l6d1w3O4-J1Kj5OfCy346UMkfMuAm7qK5sPwbdUvViAuXsMUoV72h4adq_hflCWJiVduA-KWU9QMeOdmg";

		GenerateICSFiles c = new GenerateICSFiles();
		c.generateICS(jwtToken);
	}

	private static final String ALL_CALENDAR_URL = "https://www.googleapis.com/calendar/v3/users/me/calendarList";
	
	// specifies who produced this ics file
	private static final String PRODID = "-//Events Calendar//iCal4j 1.0//EN"; // "-//Google Inc//Google Calendar// 70.9054//EN";

	private static final String DEFAULT_TRANSPARENCY = "OPAQUE";

	private static final String ICS_FILE_EXTENSION = ".ics";

	private static final String FOLDER_PATH = "/Users/sreejan.chowdhury/Desktop/GoogleCalendarExport";
	
	//https://www.googleapis.com/calendar/v3/calendars/{calendarId}/events
	private static final String EVENTS_URL = "https://www.googleapis.com/calendar/v3/calendars/";
	private static final String EVENTS = "/events";

	
	//special character to hexadecimal for URL
	//refer to the link : https://secure.n-able.com/webhelp/NC_9-1-0_SO_en/Content/SA_docs/API_Level_Integration/API_Integration_URLEncoding.html
	//for # -> 23
	private static final String POUND_SUB = "%23";

	private void generateICS(final String jwtToken) throws ParseException, IOException {

		ObjectMapper mapper = new ObjectMapper();
		
		String pageToken = null;
		CalendarList allCals = null;
//		JsonNode allCals = null;
		do {

			// get all Calendars
			String allCalsJsonString = sendRequest(ALL_CALENDAR_URL, jwtToken, pageToken);
			
//			allCals = new ObjectMapper().readValue(allCalsJsonString, JsonNode.class);
			
//			System.out.println(allCals.);

			allCals = new ObjectMapper().readValue(allCalsJsonString, CalendarList.class);
			
			//this had to be done as the items list was not being converted to it corresponding class
			//i.e Arraylist of CalendarListEntry
			String jsonStr = mapper.writeValueAsString(allCals.getItems());
//			System.out.println(jsonStr);
			
			JsonNode calendarListEntryNode = new ObjectMapper().readValue(jsonStr, JsonNode.class);
			
			//en.indian#holiday@group.v.calendar.google.com
			int i =0;
			while(calendarListEntryNode.has(i)) {
//				System.out.println(calendarListEntryNode.get(i).get("id") +" ----- "+calendarListEntryNode.get(i).get("summary") 
//						+"  -------  "+calendarListEntryNode.get(i).get("timeZone").toString());
				
//				if(calendarListEntryNode.get(i).get("summary").asText().contains("sreejan"))
				//get the events for this calendar ID 
				createICS(calendarListEntryNode.get(i),jwtToken);
				i++;
			}
			
			pageToken = allCals.getNextPageToken();
		} while (pageToken != null);

	}

	
	private static void createICS(final JsonNode calendarListEntry, final String jwtToken) throws IOException {

		// create calendar
//		System.out.println("Inside createICS :: "+ calendarListEntry.get("timeZone").asText());

		TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
		TimeZone timezone = registry.getTimeZone("Asia/Kolkata");
		VTimeZone tz = timezone.getVTimeZone();


		net.fortuna.ical4j.model.Calendar calendar = new net.fortuna.ical4j.model.Calendar();
		calendar.getProperties().add(new ProdId(PRODID));
		calendar.getProperties().add(Version.VERSION_2_0);
		calendar.getProperties().add(CalScale.GREGORIAN);
		calendar.getProperties().add(Method.PUBLISH);

		calendar.getProperties().add(new XProperty("X-WR-TIMEZONE", calendarListEntry.get("timeZone").asText() ));
		// add X-WR-CALNAME
		calendar.getProperties().add(new XProperty("X-WR-CALNAME", calendarListEntry.get("timeZone").asText() ));

		calendar.getComponents().add(tz);

		String eventPageToken = null;
		VEvent vEvent = null;
		Events events = null;
		do {
			// get all Events
			String allEventsJsonString = sendRequest(EVENTS_URL + calendarListEntry.get("id").asText().replace("#", POUND_SUB)+EVENTS, jwtToken, eventPageToken);

//			events = new ObjectMapper().readValue(allCalsJsonString, Events.class);
			JsonNode eventsNode = new ObjectMapper().readValue(allEventsJsonString, JsonNode.class);
//			System.out.println(eventsNode);
			
			int i = 0;
			while(eventsNode.get("items").has(i)) {
//				System.out.println("----EVENT "+eventsNode.get("items").get(i));
				
				vEvent = addEvent(eventsNode.get("items").get(i));
				
				calendar.getComponents().add(vEvent);
				
				i++;
			}
			
			eventPageToken = eventsNode.get("nextPageToken") != null ? eventsNode.get("nextPageToken").asText() : null;
			
		} while (eventPageToken != null);

		calendar.validate();

		writeICSFile(calendarListEntry.get("summary").asText(), calendar.toString(), FOLDER_PATH);
		System.out.println("Done");
//				System.out.println("ICalendar "+calendar.toString());

//			}

	}
	
	private static VEvent addEvent(final JsonNode event)
			throws IOException {
		
//		System.out.println("--------------NEW-------------------");
		
		//set to check which fields have been set
		//this is to handle the default / optional values
		Set<String> paramsSet = new HashSet<>();
		VEvent vEvent = new VEvent();
		
		Iterator<Entry<String, JsonNode>> fields = event.fields();
	    boolean isFirst = true;
	    while (fields.hasNext()) {
	        Entry<String, JsonNode> jsonField = fields.next();
//	        System.out.println(jsonField.getKey()+ " --- "+ jsonField.getValue());
	        try {
				addEventProps(jsonField.getKey(), vEvent, jsonField.getValue().asText(),event);
				paramsSet.add(jsonField.getKey());
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
	    }

		
//		Consumer<JsonNode> data = (JsonNode node) -> System.out.println(node.toString());
//		event.forEach(data);
		
		//add the default or optional once
		if(!paramsSet.contains("transparency"))
			vEvent.getProperties().add( new Transp( DEFAULT_TRANSPARENCY ) );
		if(!paramsSet.contains("summary"))
			vEvent.getProperties().add( new Summary("") );

		return vEvent;

	}
	
	
//	 THERE can be other params like 
//	  X-MICROSOFT-CDO-ALLDAYEVENT:FALSE
//		X-MICROSOFT-CDO-APPT-SEQUENCE:2
//		X-MICROSOFT-CDO-BUSYSTATUS:TENTATIVE
//		X-MICROSOFT-CDO-IMPORTANCE:1
//		X-MICROSOFT-CDO-INSTTYPE:0
//		X-MICROSOFT-CDO-INTENDEDSTATUS:BUSY
//		X-MICROSOFT-CDO-OWNERAPPTID:2118404721
//		X-MICROSOFT-DISALLOW-COUNTER:FALSE
//		X-MICROSOFT-DONOTFORWARDMEETING:FALSE
//		X-MICROSOFT-LOCATIONDISPLAYNAME:Zoom Meeting https://zoom.us/j/6419133414
//		X-MICROSOFT-LOCATIONS:[{"DisplayName":"Zoom Meeting https://zoom.us/j/12341241"\,
//		"LocationAnnotation":""\,"LocationUri":""\,"LocationStreet":""\,"Lo
//		 cationCity":""\,"LocationState":""\,"LocationCountry":""\,"LocationPostalCo
//		 de":""\,"LocationFullAddress":""}]
//		X-MICROSOFT-LOCATIONSOURCE:None
	 
	private static <T> void addEventProps(final String key, VEvent vEvent, final T value, final JsonNode event) throws URISyntaxException, ParseException, IOException, java.text.ParseException {
		
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
        
        ObjectMapper mapper = new ObjectMapper();

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

				if( event.get("conferenceData") != null && 
						event.get("conferenceData").get("entryPoints") != null 
//						&& event.get("conferenceData").get("entryPoints").as.size() > 0
				  ) {
					
					List<EntryPoint> epList = mapper.convertValue(
							event.get("conferenceData").get("entryPoints"), 
						    new TypeReference<List<EntryPoint>>(){}
						);
					
						for(EntryPoint ep : epList) {
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
				((Property) vEvent.getProperties().getProperty(Property.ORGANIZER)).setValue("mailto:"+event.get("organizer").get("email").asText() );
				
				String cn = event.get("organizer").get("displayName") != null ?
							event.get("organizer").get("displayName").asText() : null;
				if(cn != null)
					((Property) vEvent.getProperties().getProperty(Property.ORGANIZER)).getParameters().add(new Cn( cn ) );
				break;

			case "start":
				//it can be of date and datetime types
				//need to handle both
				if( event.get("start").get("timeZone") != null ) {
					TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
					timezone = registry.getTimeZone(event.get("start").get("timeZone").asText());
				}
				
				
				
				if( event.get("start").get("dateTime") != null)
					date = simpleDateFormat.format( sdf.parse( event.get("start").get("dateTime").asText() ) );
				
				else
					date = simpleDateFormat.format( sdfDate.parse( event.get("start").get("date").asText() ) );	
						
						
				DtStart startDate = new DtStart(  date, timezone);
				vEvent.getProperties().add( startDate );
				
				break;

			case "end":

				if( event.get("end").get("timeZone") != null ) {
					TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
					timezone = registry.getTimeZone(event.get("end").get("timeZone").asText());
				}
				
				
				if( event.get("end").get("dateTime") != null)
					date = simpleDateFormat.format( sdf.parse( event.get("end").get("dateTime").asText() ) );
				
				else
					date = simpleDateFormat.format( sdfDate.parse( event.get("end").get("date").asText() ) );	
				
				DtEnd endDate = new DtEnd( date, timezone);
				vEvent.getProperties().add( endDate );
				
				break;

			case "recurrence":
				
				List<String> rrList = mapper.convertValue(
						event.get("recurrence"), 
					    new TypeReference<List<String>>(){}
					);
				
//				System.out.println("*****  rrList ***** "+rrList.toString());
				//string is in following format
				//RRULE:FREQ=WEEKLY
				//Hence removing the RRULE: part
				for( String rRule : rrList)
					vEvent.getProperties().add( new RRule( rRule.substring( rRule.indexOf( ":" ) + 1 ) ) );
				break;	

			case "recurringEventId":
				vEvent.getProperties().add(new RecurrenceId( (String) value));
				break;

			case "sequence":
				vEvent.getProperties().add(new Sequence( (String) value));
				break;
				
			case "attendees":
				if( event.get("attendees") != null	) {
					
					List<EventAttendee> attendeeList = mapper.convertValue(
							event.get("attendees"), 
						    new TypeReference<List<EventAttendee>>(){}
						);
					
//					System.out.println("*****  rrList ***** "+attendeeList.toString());
					//TO COMPLETE
					//check this
					//https://developers.google.com/calendar/v3/reference/events#resource
					for(EventAttendee a: attendeeList) {
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
//			
			case "attachments":
				
				List<EventAttachment> attachmenList = mapper.convertValue(
						event.get("attachments"), 
					    new TypeReference<List<EventAttachment>>(){}
					);
				
				for( EventAttachment ea : attachmenList) {
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
//            System.out.println("Done");
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
        	
        	if(bw != null)
        		bw.close();
        }
		
	}

	private static String sendRequest(final String url, final String jwtToken, final String nextPageToken)
			throws ParseException, IOException {

		CloseableHttpClient httpClient = HttpClients.createDefault();
		String result = null;

		try {

			HttpGet request = new HttpGet(url);
			System.out.println("Calling end poitn ::: "+url);

			// add request headers
			request.addHeader(HttpHeaders.AUTHORIZATION, jwtToken);

			CloseableHttpResponse response = httpClient.execute(request);

			try {

				// Get HttpResponse Status
//                System.out.println(response.getProtocolVersion());              // HTTP/1.1
//                System.out.println(response.getStatusLine().getStatusCode());   // 200
//                System.out.println(response.getStatusLine().getReasonPhrase()); // OK
//                System.out.println(response.getStatusLine().toString());        // HTTP/1.1 200 OK

				HttpEntity entity = response.getEntity();
				if (entity != null) {
					// return it as a String
					result = EntityUtils.toString(entity);
//                    System.out.println(result);
				}

			}catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			
			finally {
				response.close();
			}
		} finally {
			httpClient.close();
		}

		return result;
	}

}
