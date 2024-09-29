package be.meiji.tadao;

public class BusStop {

  private int id;
  private String name;
  private String postalCode;
  private String cityName;

  public BusStop(int id, String name, String postalCode, String cityName) {
    this.id = id;
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
