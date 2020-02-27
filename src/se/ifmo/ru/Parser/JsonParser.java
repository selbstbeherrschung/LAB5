package se.ifmo.ru.Parser;

import org.json.simple.parser.*;
import org.json.simple.*;
import se.ifmo.ru.Collection.Coordinates;
import se.ifmo.ru.Collection.Ticket;
import se.ifmo.ru.Collection.TicketType;
import se.ifmo.ru.Collection.Venue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.Scanner;

import static se.ifmo.ru.Main.*;


public class JsonParser {

    private File jString;

    public File getjString() {
        return jString;
    }

    public void setjString(File jString) {
        this.jString = jString;
    }

    public JsonParser(File jsonString) {
        jString = jsonString;
    }

    public void parse(){

            try {

                Scanner scanner = new Scanner(jString);
                String line = "";
                while (scanner.hasNextLine()){
                    line = line + "\n" + scanner.nextLine();

                }

                try(BufferedReader bufferedReader = new BufferedReader(new StringReader(line))){

                    JSONObject jsonObject = (JSONObject) new JSONParser().parse(bufferedReader);
                    jsonObject = (JSONObject) jsonObject.get("file");
                    int numberOfTickets = (int) jsonObject.get("NumberOfTickets");
                    setHashCreationDate((ZonedDateTime) jsonObject.get("DateOfCreation"));
                    JSONArray TicketsArr = (JSONArray) jsonObject.get("Tickets");
                    Iterator TicketsItr = TicketsArr.iterator();
                    while (TicketsItr.hasNext()) {
                        JSONObject tick = (JSONObject) TicketsItr.next();
                        Ticket Tick = getTicket(tick);
                        TicketsHashTable.put((String) Tick.getName(),Tick);
                    }

                } catch (Exception e) {
                    System.out.println("There are mistakes in the file! While parsing.");
                }
            } catch (FileNotFoundException e) {
                System.out.println("Can't find file!");
            }


    }

    private Ticket getTicket(JSONObject jo){
         int id = (int)jo.get("id");
         String name = (String)jo.get("name");
         Coordinates coordinates = new Coordinates((JSONObject)jo.get("coordinates"));
         java.time.ZonedDateTime creationDate = java.time.ZonedDateTime.parse((String)jo.get("creationDate"));
         long price = (long)jo.get("price");
         String comment = (String)jo.get("comment");
         boolean refundable = (boolean)jo.get("refundable");
         TicketType type = TicketType.valueOf((String)jo.get("type"));
         Venue venue = new Venue((JSONObject)jo.get("venue"));
         return new Ticket(id, name, coordinates, creationDate, price, comment, refundable, type, venue);
    }

}
