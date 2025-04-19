package org.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.io.File;
import java.util.Scanner;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Main {

    static final String ESPNSCOREBOARDAPIADDRESS = "https://site.api.espn.com/apis/site/v2/sports/football/nfl/scoreboard?limit=1000&dates=";

    public static void main(String[] args) {
        String jsonString = "";

        if (args.length == 1) { //in the case of an argument to process an api pull segment as a file

            try{
                File file = new File(args[0]);
                Scanner sc = new Scanner(file);
                jsonString = sc.nextLine();
            }catch(Exception e){
                System.out.println("Could not read API Segment");
                return;
            }
        } else if(args.length == 0){//make api pull from current time
            //Access Date and time for api pulls
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            String formattedDateTime = now.format(formatter);

            String apiloc = ESPNSCOREBOARDAPIADDRESS + formattedDateTime;

            try{
                URI uri = new URI(apiloc);

                HttpURLConnection scConnection = (HttpURLConnection) uri.toURL().openConnection();
                int responseCode = scConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(scConnection.getInputStream()));
                    String line;
                    StringBuilder response = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    System.out.println(response);
                    jsonString = line;
                } else {
                    System.out.println("Request failed: " + responseCode);
                    return;
                }

                scConnection.disconnect();
            } catch(Exception e) {
                System.out.println("Unable to reach the api.");
                return;
            }

        }

        JsonObject root = JsonParser.parseString(jsonString).getAsJsonObject();
        JsonArray events = root.getAsJsonArray("events");

        for(int i = 0; i < events.size(); i++) {
            JsonObject Game = events.get(i).getAsJsonObject();
            JsonObject Competitions = Game.get("competitions").getAsJsonArray().get(0).getAsJsonObject();
            JsonArray Competitors = Competitions.get("competitors").getAsJsonArray();

            String HomeTeamLocation = Competitors.get(0).getAsJsonObject().get("team")
                    .getAsJsonObject().get("location").getAsString();
            String AwayTeamLocation = Competitors.get(1).getAsJsonObject().get("team")
                    .getAsJsonObject().get("location").getAsString();

            //events . status holds game clock information

            JsonObject Status = events.get(i).getAsJsonObject().get("status").getAsJsonObject();

            //type -> id gives a number that shows the state the game is in.

            int StatusTypeID = Status.get("type").getAsJsonObject().get("id").getAsInt();

            //type -> detail gives a nice little game status, ie: "11:37 4TH QUARTER"

            String StatusDetail = Status.get("type").getAsJsonObject().get("detail").getAsString();

            //competitors(array) -> get (i) -> score (String) for total / linescores (array) for all quarters.

            String HomeScore = Competitors.get(0).getAsJsonObject().get("score").getAsString();
            String AwayScore = Competitors.get(1).getAsJsonObject().get("score").getAsString();

            String HomeTeam = Competitors.get(0).getAsJsonObject().get("team").getAsJsonObject().get("name").getAsString();
            String AwayTeam = Competitors.get(1).getAsJsonObject().get("team").getAsJsonObject().get("name").getAsString();

            System.out.println(AwayTeamLocation + " " + AwayTeam + " @ " + HomeTeamLocation + " " + HomeTeam + ": " + StatusDetail);
            if (StatusTypeID > 1) {//operates on the understanding that any in progress or finished game has a status id of more than 1
                JsonArray HomeLineScore = Competitors.get(0).getAsJsonObject().get("linescores").getAsJsonArray();
                JsonArray AwayLineScore = Competitors.get(1).getAsJsonObject().get("linescores").getAsJsonArray();

                System.out.print(String.format("%10s", HomeTeam) + ": ");
                for (int j = 0; j < 4; j++) { //always show 4 quarters of scoring at any moment during game.
                    //api will only have one line score for quarter of game played.
                    if (j < HomeLineScore.size()) {
                        System.out.print(String.format("%02d", Integer.parseInt(HomeLineScore.get(j).getAsJsonObject().get("value").getAsString())) + " ");
                    } else {
                        System.out.print("00 ");
                    }
                }
                System.out.println(": " + HomeScore);

                System.out.print(String.format("%10s", AwayTeam) + ": ");
                for (int j = 0; j < 4; j++) {
                    if (j < AwayLineScore.size()) {
                        System.out.print(String.format("%02d", Integer.parseInt(AwayLineScore.get(j).getAsJsonObject().get("value").getAsString())) + " ");
                    } else {
                        System.out.print("00 ");
                    }
                }
                System.out.println(": " + AwayScore);
            }
            System.out.println();
        }
    }
}