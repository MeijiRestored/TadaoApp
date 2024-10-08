package be.meiji.tadao;

public class BusStop {

  private int id;
  private String name;
  private String postalCode;
  private String cityName;

  public BusStop(String id, String name, String postalCode, String cityName) {
    int nid;
    try {
      nid = Integer.parseInt(id);
    } catch (NumberFormatException e) {
      if (id.startsWith("18 e")) {
        nid = 180;
      } else {
        nid = -1;
      }
    }
    this.id = nid;
    this.name = name;
    this.postalCode = postalCode;
    this.cityName = cityName;
  }

  public String getName() {
    return name;
  }

  public String getPostalCode() {
    return postalCode;
  }

  public String getCityName() {
    return cityName;
  }

  public int getId() {
    return id;
  }
}
