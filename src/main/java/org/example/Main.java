package org.example;


import java.io.File;
import java.util.Scanner;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Main {
    public static void main(String[] args) {

        File file = new File(args[0]); //Argument is file location for txt recorded api pull while project is in development.
        //api pull is from https://site.api.espn.com/apis/site/v2/sports/football/nfl/scoreboard?limit=1000&dates=2024
        try{
            Scanner sc = new Scanner(file);
            String jsonString = sc.nextLine();
            JsonObject root = JsonParser.parseString(jsonString).getAsJsonObject();
            JsonArray events = root.getAsJsonArray("events");

            for(int i = 0; i < events.size(); i++){
                JsonObject Game = events.get(i).getAsJsonObject();
                JsonObject Competitions = Game.get("competitions").getAsJsonArray().get(0).getAsJsonObject();
                JsonArray Competitors = Competitions.get("competitors").getAsJsonArray();

                String HomeTeamLocation = Competitors.get(0).getAsJsonObject().get("team")
                        .getAsJsonObject().get("location").getAsString();
                String AwayTeamLocation = Competitors.get(1).getAsJsonObject().get("team")
                        .getAsJsonObject().get("location").getAsString();

                String HomeTeam = Competitors.get(0).getAsJsonObject().get("team").getAsJsonObject().get("name").getAsString();
                String AwayTeam = Competitors.get(1).getAsJsonObject().get("team").getAsJsonObject().get("name").getAsString();
                System.out.println(AwayTeamLocation + " " + AwayTeam + " @ " + HomeTeamLocation + " " + HomeTeam);
            }

        }catch(Exception e){
            System.out.println("error while accessing file: " + e.getMessage());
        }
    }
}