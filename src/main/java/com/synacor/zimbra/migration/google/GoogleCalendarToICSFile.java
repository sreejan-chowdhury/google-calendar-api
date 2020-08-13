package com.synacor.zimbra.migration.google;

import java.io.IOException;

import org.apache.http.ParseException;

import com.synacor.zimbra.migration.google.GenerateICSFIle;

import net.fortuna.ical4j.util.MapTimeZoneCache;

public class GoogleCalendarToICSFile {
	// To run are arguments as
//	 "Sreejan Chowdhury" ya29.a0AfH6SMDzM7ZUNgaCU9FPdcZsD13reAhxCZEpcyPT_ZxxNzaiOMMh1IyftUYZh780uo3wbWxUktJjkNx47WmioV-XrpRtRXxX1w9NKy-7E40oHWGqZNvag4iJF6mImxSQGzf4YoP4461ei3-Zi6ztP4TklPKm_HX_6x8 /Users/sreejan.chowdhury/Desktop/GoogleCalendarExport

	public static void main(String[] args) throws ParseException, IOException, Exception {

		// for JCache used for TimeZOneRegistry in iCal
		System.setProperty("net.fortuna.ical4j.timezone.cache.impl", MapTimeZoneCache.class.getName());
		// for iCal validate method
		System.setProperty("ical4j.validation.relaxed", "true");

		// TODO Auto-generated method stub
		String userName = args[0];
		String accessToken = args[1];
		String folderPath = args[2];

		// call the method to generate the files
		GenerateICSFIle gICS = new GenerateICSFIle(userName, accessToken, folderPath);
		gICS.generateICS();

	}

}
