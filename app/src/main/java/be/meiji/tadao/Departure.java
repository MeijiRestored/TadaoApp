package be.meiji.tadao;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Departure {

  // Formatter for parsing the date and time
  private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
      "yyyy-MM-dd'T'HH:mm:ss");
  private String lineNumber;
  private String directionName;
  private String destinationName;
  private String lineColor;
  private String dateTime;
  private String realDateTime;
  private boolean isRealTime;

  public Departure(String lineNumber, String directionName, String destinationName, String lineColor, String dateTime,
      String realDateTime) {
    this.lineNumber = lineNumber;
    this.directionName = directionName;
    this.destinationName = destinationName;
    this.lineColor = lineColor;
    this.dateTime = dateTime;
    this.isRealTime = !realDateTime.equals("null");
    this.realDateTime =
        !realDateTime.equals("null") ? realDateTime : dateTime;
  }

  public String getLineNumber() {
    return lineNumber;
  }

  public String getDirectionName() {
    return directionName;
  }

  public String getDestinationName() {
    return destinationName;
  }

  public String getLineColor() {
    return lineColor;
  }

  public String getDateTime() {
    return dateTime;
  }

  public String getRealDateTime() {
    return realDateTime;
  }

  public boolean isRealTime() {
    return isRealTime;
  }

  public long getWaitTimeInMinutes() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime realTime = LocalDateTime.parse(realDateTime, formatter);
    return Duration.between(now, realTime).toMinutes();
  }
}
