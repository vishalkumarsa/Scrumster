/**
 * scrumster.java handles meeting bookings via integrations with Amazon Alexa and Google Calendar API.
 * Implementation provides room for further functionality.
 *
 * @author Sachin Saligram; Raghavendra Nayak Muddur
 * @version 1.0
 * @since 11/13/2017
 **/

// Import packages and libraries

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.Calendar.Freebusy;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.*;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Date;


public class scrumster {

    /*
    * @param service Google calendar service
    * @param min_time Set the calendar start time
    * @param max_time Set the calendar end time
    * @param zone_val Current time zone
    * @param items List of users/rooms
    * @param epoch_val Epoch values for every 30 minutes
    * @param user Identify if user or not (room)
    * @param epoch_now Current Epoch value
    * @return unique_user_slot List of available slots
    */
    private static List<Integer> slot(Calendar service, DateTime min_time, DateTime max_time, String zone_val,
                                      List<String> items, List<Long> epoch_val, boolean user, long epoch_now, List<Integer> time_slots) throws Exception {

        // HashMap to store all available slots for each user
        Map<String, List<Integer>> all_item_slots = new HashMap<>();

        // Iterate over each item to store list of free slots
        for (int i = 0; i < items.size(); i++) {

            // Identify slots when item is busy
            List<Integer> item_slots = event_check(service, min_time, max_time, zone_val, items.get(i), epoch_val, epoch_now);
            // All possible time slots that can exist
//            List<Integer> time_slots = new ArrayList<>((Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)));
            // Find all time slots when the item is free and add to the HashMap
            Set<Integer> hash_slots = new HashSet<>();
            Set<Integer> hash_time_slots = new HashSet<>();
            hash_slots.addAll(item_slots);
            hash_time_slots.addAll(time_slots);
            hash_time_slots.removeAll(hash_slots);
            item_slots.clear();
            item_slots.addAll(hash_time_slots);
            all_item_slots.put(items.get(i), item_slots);
            //System.out.println(items.get(i)+" "+item_slots);
            //System.out.println("Time slots "+time_slots);
        }

        // Initialize to uniquely identify slots when all items are available
        Set<Integer> first_item = new HashSet<>();
        Set<Integer> second_item = new HashSet<>();
        List<Integer> unique_item_slot = new ArrayList<>();

        // Identify the first item. Iteratively check the intersection of each item for unique set of free slots
        first_item.addAll(all_item_slots.get(all_item_slots.keySet().toArray()[0]));
        if (user) {
            for (String key : all_item_slots.keySet()) {
                second_item.clear();
                second_item.addAll(all_item_slots.get(key));
                first_item.retainAll(second_item);
            }
        }

        // Return the slots when all items are available
        unique_item_slot.addAll(first_item);
        return unique_item_slot;
    }

    /*
    * @param service Google calendar service
    * @param min_time Set the calendar start time
    * @param max_time Set the calendar end time
    * @param zone Current time zone
    * @param val User being checked
    * @param epoch_values Epoch values for every 30 minutes
    * @param epoch_current Current Epoch value
    * @return unique_user_slot List of busy slots
    */
    private static List<Integer> event_check(Calendar service, DateTime min_time, DateTime max_time,
                                                  String zone, String val, List<Long> epoch_values, long epoch_current) throws IOException {

        FreeBusyRequestItem ids = new FreeBusyRequestItem();
        ids.setId(val);
        List<FreeBusyRequestItem> temp = new ArrayList<>();
        temp.add(ids);

        FreeBusyRequest req = new FreeBusyRequest();
        req.setTimeMin(min_time);
        req.setTimeMax(max_time);
        req.setTimeZone(zone);
        req.setItems(temp);
        Freebusy.Query fbq = service.freebusy().query(req);
        FreeBusyResponse response = fbq.execute();

        JSONObject obj = new JSONObject(response);
        JSONArray arr = obj.getJSONObject("calendars").getJSONObject(val).getJSONArray("busy");

        //List of busy slots per user
        ArrayList<Integer> busy_slots = new ArrayList<>();

        for (int j = 0; j < arr.length(); j++) {
            JSONObject start_objects = arr.getJSONObject(j).getJSONObject("start");
            JSONObject end_objects = arr.getJSONObject(j).getJSONObject("end");

            long start_val = (long) start_objects.get("value") / (long) 1000;
            long end_val = (long) end_objects.get("value") / (long) 1000;

            //Start slot
            int start_slot = 0;

            boolean start_flag = false;
            //End slot
            int end_slot = 0;

            for (int i = 0; i < epoch_values.size(); i++) {
                if (start_val > epoch_current) {
                    if (start_val == epoch_values.get(i)) {
                        start_slot = i + 1;

                    }
                }
                if (end_val > epoch_current) {
                    if (end_val == epoch_values.get(i)) {
                        end_slot = i;
                    }
                }
            }

            for (int i = start_slot; i <= end_slot; i++) {
                busy_slots.add(i);
            }
        }

        //System.out.println("val"+val);
        //System.out.println("busy slots"+busy_slots);

        return busy_slots;
    }


    private static void create_event(com.google.api.services.calendar.Calendar service, List<String> users, String zone, DateTime startDateTime,
                                     DateTime endDateTime) throws IOException, ParseException {
        Event event = new Event()
                .setSummary("HackNC")
                .setLocation("UNC Chapel Hill")
                .setDescription("Daily Scrum Meeting");

        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone(zone);
        event.setStart(start);

        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone(zone);
        event.setEnd(end);

        EventAttendee[] attendees = new EventAttendee[users.size()];

        for (int i = 0; i < users.size(); i++) {
            attendees[i] = new EventAttendee();
            attendees[i].setEmail(users.get(i));
        }

        //System.out.println("Start " + startDateTime);
        //System.out.println("End " + endDateTime);
        event.setAttendees(Arrays.asList(attendees));
        service.events().insert("primary", event).execute();
    }


    @NotNull
    private static DateTime GetFormat(String date, int slot, boolean isStart) throws ParseException {
        String start[] = {"09:00", "09:30", "10:00", "10:30", "11:00", "11:30", "12:00", "12:30", "13:00", "13:30", "14:00", "14:30", "15:00", "15:30", "16:00", "16:30"};
        String end[] = {"09:30", "10:00", "10:30", "11:00", "11:30", "12:00", "12:30", "13:00", "13:30", "14:00", "14:30", "15:00", "15:30", "16:00", "16:30", "17:00"};

        if (isStart) {
            String datetime = date + "T" + start[slot - 1] + ":00";
            return timeStamp(datetime);
        } else {
            String datetime = date + "T" + end[slot - 1] + ":00";
            return timeStamp(datetime);
        }
    }


    @NotNull
    private static DateTime timeStamp(String datetime) throws ParseException {
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        isoFormat.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        Date datetime_new = isoFormat.parse(datetime);
        return new DateTime(datetime_new);
    }


    public static void main(String[] args) throws Exception {

        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        GoogleCredential credential = new GoogleCredential.Builder().setTransport(httpTransport)
                .setJsonFactory(jsonFactory).setServiceAccountId("calender@ourcal-185819.iam.gserviceaccount.com")
                .setServiceAccountPrivateKeyFromP12File(new File("/Users/sachin/eclipse-workspace/hackNC/src/main/resources/client_secret.p12"))
                .setServiceAccountScopes(Collections.singleton(CalendarScopes.CALENDAR))
                .setServiceAccountUser("scrum@ouruse.com")
                .build();

        // Build a new authorized API client service.
        // Note: Do not confuse this class with the
        // com.google.api.services.calendar.model.Calendar class.
        com.google.api.services.calendar.Calendar service = new com.google.api.services.calendar.Calendar.Builder(httpTransport, jsonFactory, credential).setApplicationName("OurCal").build();

        //Obtain date input from Alexa
        String start_time = "09:00:00";
        String end_time = "17:00:00";
        String date = "2017-11-14";
        String zone = "America/New_York";

        DateTime min_time = timeStamp(date + "T" + start_time);
        DateTime max_time = timeStamp(date + "T" + end_time);
        //System.out.println("min_time " + min_time);
        //System.out.println("max_time " + max_time);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.now();
        //System.out.println(dtf.format(localDate)); //2016/11/16

        Date current = new Date();
        DateTime current1 = new DateTime(current);
        //System.out.println(min_time.getValue());
        //System.out.println(current1.getValue());

        if (current1.getValue() > max_time.getValue()) {
            System.out.println("Cannot book for " + date + ". Please book for a later date.");
            return;
        }

        //Current Epoch value
        long now1 = (Instant.now().toEpochMilli()) / 1000;

        //Create list for storing 30 minute EPOCH values
        List<Long> epoch_values = new ArrayList<>();
        List<Integer> time_slots = new ArrayList<>();

        ArrayList<String> users = new ArrayList<>();
        users.add("raghav@ouruse.com");
        users.add("sachin@ouruse.com");
        users.add("kshitija@ouruse.com");
        users.add("vishal@ouruse.com");


        //Obtain epoch times for all time slots
        LocalDate ldt = LocalDate.from(DateTimeFormatter.ISO_DATE.parse(date));
        LocalTime localTime = LocalTime.of(9, 0);
        ZonedDateTime zdt = ZonedDateTime.of(ldt, localTime, ZoneId.of("America/New_York"));
        for (int i = 0; i < 18; i++) {
            if ((zdt.toEpochSecond() > now1) && (i<16)){
                time_slots.add(i+1);
            }
            epoch_values.add(zdt.toEpochSecond());
            zdt = zdt.plusMinutes(30);
        }

        List<Integer> user_slot = slot(service, min_time, max_time, zone, users, epoch_values, true, now1, time_slots);
        //System.out.println("book-slot1 = " + user_slot);
        Collections.sort(user_slot);
        if (!user_slot.isEmpty()) {
            //System.out.println(user_slot.get(0));
            DateTime start_date1 = GetFormat(date, user_slot.get(0), true);
            //System.out.println("Format output " + start_date1);
            DateTime end_date1 = GetFormat(date, user_slot.get(0), false);
            //System.out.println("Format output " + end_date1);
            create_event(service, users, zone, start_date1, end_date1);
            return;
        }

        else{
            String output = "Team is not free on " + date + ". Please try another date.";
            System.out.println(output);
            return;
        }

    }
}