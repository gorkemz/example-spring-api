package com.viavis.proje;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Criteria.*;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.LimitOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.limit;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.annotation.Id;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import org.bson.Document;
@RestController
public class DataChecker {
  @Autowired
  private MongoTemplate mongoTemplate;

  @GetMapping("/DataChecker")
  public String DataChecker(Integer startTimestamp, Integer finishTimestamp, String groupBy) {

    String group = null;
    String datee = null;

    if (startTimestamp == null || finishTimestamp == null || groupBy == null) {
      return "Error: Missing parameters";
    } else {

      if (groupBy.equals("day")) {
        group = "day";

      } else if (groupBy.equals("hour")) {
        group = "hour";
      } else {
        return "Error: Invalid groupBy parameter";
      }

      ZonedDateTime start = Instant.ofEpochMilli(Long.valueOf(startTimestamp) * 1000).atZone(ZoneId.of("GMT+03:00"));
      ZonedDateTime end = Instant.ofEpochMilli(Long.valueOf(finishTimestamp) * 1000).atZone(ZoneId.of("GMT+03:00"));
      List < ZonedDateTime > dates = new ArrayList < > ();
      while (!start.isAfter(end)) {
        dates.add(start);
        if (group == "hour") {
          start = start.plusHours(1).withMinute(0).withSecond(0).withNano(0);
        } else if (group == "day") {
          start = start.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        }
      }
      dates.add(end);
      List < Long > timestamps = new ArrayList < > ();
      for (ZonedDateTime date: dates) {
        Instant instant = date.toInstant();
        long timestamp = instant.getEpochSecond();
        timestamps.add(timestamp);
      }

      System.out.println(timestamps);
      JsonArray array = new JsonArray();
      for (int i = 0; i < timestamps.size() - 1; i++) {

        Aggregation agg = newAggregation(
          match(where("timestamp").gt(Long.valueOf(timestamps.get(i))).lt(Long.valueOf(timestamps.get(i + 1)))),
          group().sum("in").as("totalIn").sum("out").as("totalOut")
        );
        AggregationResults < Document > results = mongoTemplate.aggregate(
          agg, "veriler", Document.class
        );

        long timestamp = Long.valueOf(timestamps.get(i));
        Instant instant = Instant.ofEpochSecond(timestamp);
        if (group == "day") {
          datee = instant.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } else if (group == "hour") {
          datee = instant.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH"));
        }

        List < Document > mappedResults = results.getMappedResults();
        for (Document result: mappedResults) {
          result.put("group", datee);
        }

        if (mappedResults.toString() != "[]") {
          Gson gson = new Gson();
          String json = gson.toJson(mappedResults);
          JsonArray array2 = gson.fromJson(json, JsonArray.class);
          JsonArray newArray = new JsonArray();
          for (int a = 0; a < array2.size(); a++) {
            JsonObject obj = array2.get(a).getAsJsonObject();
            JsonObject newObj = new JsonObject();
            newObj.addProperty("group", obj.get("group").getAsString());
            newObj.addProperty("totalIn", obj.get("totalIn").getAsInt());
            newObj.addProperty("totalOut", obj.get("totalOut").getAsInt());
            newArray.add(newObj);
          }
          array.addAll(newArray);
        }

      }
      System.out.println(array);
      return array.toString();
    }
  }
}