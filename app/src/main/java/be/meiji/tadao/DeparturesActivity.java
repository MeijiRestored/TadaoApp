package be.meiji.tadao;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DeparturesActivity extends AppCompatActivity {

  private static final int UPDATE_INTERVAL = 60000; // 60 seconds
  private Handler handler;
  private Runnable updateRunnable;
  private OkHttpClient client;
  private ViewGroup departuresContainer;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_departures);
    String stopName = getIntent().getStringExtra("stopName");

    if (stopName != null) {
      setTitle(stopName);
    }

    departuresContainer = findViewById(R.id.departures_container);
    client = new OkHttpClient();

    Intent intent = getIntent();
    int stopId = intent.getIntExtra("stopId", -1);

    handler = new Handler();

    // Define the task to be repeated
    updateRunnable = new Runnable() {
      @Override
      public void run() {
        fetchNextDepartures(stopId);

        handler.postDelayed(this, UPDATE_INTERVAL);
      }
    };

    handler.post(updateRunnable);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    if (handler != null) {
      handler.removeCallbacks(updateRunnable);
    }
  }

  private void fetchNextDepartures(int stopId) {
    String url = "https://api.maas-fr.cityway.fr/media/api/v1/fr/Schedules/LogicalStop/" + stopId
        + "/NextDeparture?realTime=true&userId=TWL_TADAO2_6PW21JM";

    Request request = new Request.Builder()
        .url(url)
        .build();

    client.newCall(request).enqueue(new Callback() {
      @Override
      public void onFailure(@NonNull Call call, @NonNull IOException e) {
        Log.e(MainActivity.API_ERROR, "Request Failed", e);
      }

      @Override
      public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
        if (response.isSuccessful()) {
          String jsonResponse = response.body().string();
          runOnUiThread(() -> {
            try {
              departuresContainer.removeAllViews();
              parseDeparturesData(jsonResponse);
            } catch (JSONException e) {
              Log.e("JSON_ERROR", "Failed to parse JSON", e);
            }
          });
        } else {
          Log.e(MainActivity.API_ERROR, "Response not successful: " + response.code());
        }
      }
    });
  }

  private void parseDeparturesData(String jsonResponse) throws JSONException {
    JSONArray departuresArray = new JSONArray(jsonResponse);
    List<Departure> departuresList = new ArrayList<>();

    if (jsonResponse.isEmpty() || departuresArray.length() == 0) {
      runOnUiThread(this::displayNoDeparturesMessage);
      return;
    }

    for (int i = 0; i < departuresArray.length(); i++) {
      JSONObject departureObj = departuresArray.getJSONObject(i);

      JSONArray linesArray = departureObj.getJSONArray("lines");

      for (int j = 0; j < linesArray.length(); j++) {
        JSONObject lineObj = linesArray.getJSONObject(j);

        JSONObject line = lineObj.getJSONObject("line");
        String lineNumber = line.getString("number");
        String lineColor = line.getString("color");

        JSONObject direction = lineObj.getJSONObject("direction");
        String directionName = direction.getString("name");

        JSONArray timesArray = lineObj.getJSONArray("times");
        for (int k = 0; k < timesArray.length(); k++) {
          JSONObject timeObj = timesArray.getJSONObject(k);

          String dateTime = timeObj.getString("dateTime");
          String realDateTime = timeObj.optString("realDateTime", "null");

          Departure departure = new Departure(lineNumber, directionName, lineColor, dateTime,
              realDateTime);
          departuresList.add(departure);
        }
      }
    }

    departuresList.sort(Comparator.comparing(Departure::getWaitTimeInMinutes));

    runOnUiThread(() -> displayDepartures(departuresList));
  }


  private void displayNoDeparturesMessage() {
    TextView noDeparturesTextView = new TextView(this);
    noDeparturesTextView.setText(R.string.no_next_departures);
    noDeparturesTextView.setTextSize(16);
    noDeparturesTextView.setPadding(16, 16, 16, 16);

    departuresContainer.addView(noDeparturesTextView);
  }

  private String formatLineNumber(String lineNumber) {
    int lineNum;

    try {
      lineNum = Integer.parseInt(lineNumber);
    } catch (NumberFormatException e) {
      return lineNumber;
    }

    switch (lineNum) {
      case 1:
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
      case 7:
        return "B" + lineNum;
      case 180:
        return "18 Express";
      case 90:
        return "Nav Béthune";
      case 91:
        return "Nav Lens";
      case 92:
        return "Nav Bruay";
      case 93:
        return "Nav Vimy";
      default:
        return lineNumber;
    }
  }

  private void displayDepartures(List<Departure> departuresList) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    ZoneId parisZone = ZoneId.of("Europe/Paris");
    ZonedDateTime now = ZonedDateTime.now(parisZone);

    for (Departure departure : departuresList) {
      ZonedDateTime scheduledTime = LocalDateTime
          .parse(departure.getDateTime(), formatter)
          .atZone(parisZone);

      ZonedDateTime realTime = departure.getRealDateTime() != null ?
          LocalDateTime.parse(departure.getRealDateTime(), formatter).atZone(parisZone) :
          scheduledTime;

      long waitTime = Duration.between(now, realTime).toMinutes();

      View departureView = LayoutInflater.from(this)
          .inflate(R.layout.item_departure, departuresContainer, false);

      String waitTimeText = formatWaitTime(waitTime);

      String modifiedLineNumber = formatLineNumber(departure.getLineNumber());

      TextView lineNumberView = departureView.findViewById(R.id.text_line_number);
      lineNumberView.setText(modifiedLineNumber);

      GradientDrawable background = (GradientDrawable) lineNumberView.getBackground();
      background.setColor(android.graphics.Color.parseColor("#" + departure.getLineColor()));

      TextView directionView = departureView.findViewById(R.id.text_direction);
      directionView.setText(departure.getDirectionName());

      TextView waitTimeView = departureView.findViewById(R.id.text_wait_time);
      waitTimeView.setText(waitTimeText);

      TextView delayTypeView = departureView.findViewById(R.id.text_delay_type);
      TextView delayView = departureView.findViewById(R.id.text_delay);

      long delay = Duration.between(scheduledTime, realTime).toMinutes();
      String delayText = delay != 0 ? formatWaitTime(delay) : "";
      String delayType;
      if (delay != 0) {
        if (delay > 0) {
          delayType = getString(R.string.delay);
          delayTypeView.setTextColor(ContextCompat.getColor(this, R.color.colorDelay));
          delayView.setTextColor(ContextCompat.getColor(this, R.color.colorDelay));
        } else {
          delayType = getString(R.string.early);
          delayTypeView.setTextColor(ContextCompat.getColor(this, R.color.colorEarly));
          delayView.setTextColor(ContextCompat.getColor(this, R.color.colorEarly));
        }
      } else {
        if (departure.isRealTime()) {
          delayType = getString(R.string.on_time);
          delayTypeView.setTextColor(ContextCompat.getColor(this, R.color.colorOnTime));
          delayView.setTextColor(ContextCompat.getColor(this, R.color.colorOnTime));
        } else {
          delayType = getString(R.string.planned);
          delayTypeView.setTextColor(ContextCompat.getColor(this, R.color.colorPlanned));
          delayView.setTextColor(ContextCompat.getColor(this, R.color.colorPlanned));
        }
      }

      delayTypeView.setText(delayType);
      delayView.setText(delayText);

      departuresContainer.addView(departureView);
    }
  }


  private String formatWaitTime(long minutes) {
    if (Math.abs(minutes) >= 60) {
      return String.format("%02d h %02d", Math.abs(minutes) / 60,
          minutes % 60);
    } else {
      return Math.abs(minutes) + " min";
    }
  }
}
