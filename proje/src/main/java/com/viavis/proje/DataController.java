package com.viavis.proje;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

@RestController
public class DataController {

  @Autowired
  private MongoTemplate mongoTemplate;

  @PostMapping("/data")
  @ResponseBody
  public String receiveData(@RequestBody Data data) {
    System.out.println(data.toString());
    Data savedData = mongoTemplate.save(data, "veriler");

    if (savedData != null) {
      return savedData.toString() + " Data was inserted successfully";

    } else {
      return "Error: Data was not inserted successfully";
    }
  }

  static class Data {
    private long timestamp;
    private String macId;
    private int in ;
    private int out;

    public long getTimestamp() {
      return timestamp;
    }

    public void setTimestamp(long timestamp) {
      this.timestamp = timestamp;
    }

    public String getMacId() {
      return macId;
    }

    public void setMacId(String macId) {
      this.macId = macId;
    }

    public int getIn() {
      return in;
    }

    public void setIn(int in ) {
      this.in = in;
    }

    public int getOut() {
      return out;
    }

    public void setOut(int out) {
      this.out = out;
    }

    @Override
    public String toString() {
      return "{" +
        "timestamp=" + timestamp +
        ", macId='" + macId + '\'' +
        ", in=" + in +
        ", out=" + out +
        '}';
    }
  }
}