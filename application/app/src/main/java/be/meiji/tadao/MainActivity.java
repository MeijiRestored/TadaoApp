package be.meiji.tadao;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements
    BusStopAdapter.OnBusStopClickListener {

  public static final String API_ERROR = "API_ERROR";
  private EditText searchInput;
  private Button searchButton;
  private RecyclerView recyclerView;
  private BusStopAdapter adapter;
  private List<BusStop> busStopList = new ArrayList<>();
  private OkHttpClient client;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    searchInput = findViewById(R.id.search_input);
    searchButton = findViewById(R.id.search_button);
    recyclerView = findViewById(R.id.recycler_view);

    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    adapter = new BusStopAdapter(busStopList, this); // Pass `this` to handle clicks
    recyclerView.setAdapter(adapter);

    client = new OkHttpClient();

    searchButton.setOnClickListener(v -> {
      String input = searchInput.getText().toString();
      if (!input.isEmpty()) {
        performSearch(input);
      }
    });
  }

  private void performSearch(String input) {
    String url = "https://api.maas-fr.cityway.fr/search/all?keywords=" + input
        + "&maxitems=10&objectTypes=2";

    Request request = new Request.Builder()
        .url(url)
        .build();

    client.newCall(request).enqueue(new Callback() {
      @Override
      public void onFailure(@NonNull Call call, @NonNull IOException e) {
        Log.e(API_ERROR, "Request Failed", e);
        runOnUiThread(() -> {
          Snackbar.make(findViewById(android.R.id.content), "Une erreur de connexion est survenue",
                  BaseTransientBottomBar.LENGTH_LONG)
              .setAction("Réessayer", v -> performSearch(searchInput.getText().toString()))
              .show();
        });
      }

      @Override
      public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
        if (response.isSuccessful()) {
          String jsonResponse = response.body().string();
          parseBusStopData(jsonResponse);
        } else {
          Log.e(API_ERROR, String.format("Response not successful: %d", response.code()));
          runOnUiThread(() -> {
            Snackbar.make(findViewById(android.R.id.content),
                    String.format("Une erreur est survenue (HTTP %d)", response.code()),
                    BaseTransientBottomBar.LENGTH_LONG)
                .show();
          });
        }
      }
    });
  }

  private void parseBusStopData(String jsonResponse) {
    try {
      JSONArray jsonArray = new JSONArray(jsonResponse);
      busStopList.clear();

      for (int i = 0; i < jsonArray.length(); i++) {
        JSONObject jsonObject = jsonArray.getJSONObject(i);

        String id = jsonObject.getString("Id");
        String name = jsonObject.getString("Name");
        String postalCode = jsonObject.getString("PostalCode");
        String cityName = jsonObject.getString("CityName");

        if (postalCode.startsWith("62") || postalCode.startsWith("59")) {
          BusStop busStop = new BusStop(id, name, postalCode, cityName);
          busStopList.add(busStop);
        }
      }

      // Notify the RecyclerView to update the list
      runOnUiThread(() -> adapter.notifyDataSetChanged());

    } catch (JSONException e) {
      Log.e("JSON_ERROR", "Failed to parse JSON", e);
      runOnUiThread(() -> {
        Snackbar.make(findViewById(android.R.id.content), "Erreur de lecture des données",
                BaseTransientBottomBar.LENGTH_LONG)
            .show();
      });
    }
  }

  // This method is triggered when a bus stop is clicked
  @Override
  public void onBusStopClick(int stopId, String stopName, String cityName) {
    Intent intent = new Intent(MainActivity.this, DeparturesActivity.class);
    intent.putExtra("stopId", stopId);
    intent.putExtra("stopName", stopName);
    intent.putExtra("cityName", cityName);
    startActivity(intent);
  }
}
