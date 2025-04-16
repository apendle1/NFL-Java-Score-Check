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
                if(StatusTypeID > 1){//operates on the understanding that any in progress or finished game has a status id of more than 1
                    JsonArray HomeLineScore = Competitors.get(0).getAsJsonObject().get("linescores").getAsJsonArray();
                    JsonArray AwayLineScore = Competitors.get(1).getAsJsonObject().get("linescores").getAsJsonArray();

                    System.out.print(String.format("%10s", HomeTeam) + ": ");
                    for(int j = 0; j < 4; j++){ //always show 4 quarters of scoring at any moment during game.
                        //api will only have one line score for quarter of game played.
                        if(j < HomeLineScore.size()){
                            System.out.print(String.format("%02d" , Integer.parseInt(HomeLineScore.get(j).getAsJsonObject().get("value").getAsString())) + " ");
                        } else {
                            System.out.print("00 ");
                        }
                    }
                    System.out.println(": " + HomeScore);

                    System.out.print(String.format("%10s", AwayTeam) + ": ");
                    for(int j = 0; j < 4; j++){
                        if(j < AwayLineScore.size()){
                            System.out.print(String.format("%02d" , Integer.parseInt(AwayLineScore.get(j).getAsJsonObject().get("value").getAsString())) + " ");
                        } else {
                            System.out.print("00 ");
                        }
                    }
                    System.out.println(": " + AwayScore);
                }
                System.out.println();
            }

        }catch(Exception e){
            System.out.println("error while accessing file: " + e.getMessage());
        }
    }
}