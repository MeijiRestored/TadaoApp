package be.meiji.tadao;

import androidx.annotation.NonNull;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class DestinationFormatter {

  private DestinationFormatter() {
  }

  public static String formatDestination(String destination, String direction) {
    String specialFormat = handleSpecialCases(destination, direction);
    if (specialFormat != null) {
      return specialFormat;
    }

    String cityFormat = handleCityCases(destination, direction);
    if (cityFormat != null) {
      return cityFormat;
    }

    String cityDefault = destination.split(" - ")[0];
    return String.format("%s - %s", cityDefault, direction);
  }

  private static String handleSpecialCases(String destination, String direction) {
    Map<String, Function<String, String>> specialCases = getSpecialCases();

    if (destination.startsWith("LENS - Gares") && direction.contains(" / ")) {
      int x = direction.split(" / ")[0].contains("Gares - Quai") ? 0 : 1;
      String quai = direction.split(" / ")[x].split(" - ")[1];
      return String.format("LENS - Gares - %s", quai);
    }
    if (destination.startsWith("LENS - Gares")) {
      String quai = direction.split(" - ")[1];
      return String.format("BRUAY - Europe - %s", quai);
    }
    if (destination.startsWith("BRUAY-LA") && direction.startsWith("Europe")) {
      String quai = direction.substring(direction.length() - 1);
      return String.format("BRUAY - Europe - Quai %s", quai);
    }
    if (destination.startsWith("NOYELLES-LES-V") && direction.startsWith("Guadeloupe")) {
      return "NOYELLES-LES-VERMELLES - Centre-Cial.";
    }
    if (destination.startsWith("GRENAY - Guade")) {
      return "GRENAY - Guadeloupe";
    }
    if (destination.startsWith("HOUDAIN - Mar")) {
      return "HOUDAIN - Marne";
    }
    if (destination.startsWith("BARLIN - Coll")) {
      String quai = direction.split(" / ")[0].substring(direction.split(" / ")[0].length() - 1);
      return String.format("BARLIN - Collège Moulin - Quai %s", quai);
    }
    if (destination.startsWith("LILLERS - Cov")) {
      return "LILLERS - Covoiturage";
    }
    if (destination.startsWith("LILLERS - Lyc")) {
      return "LILLERS - Lycée Lavoisier";
    }
    if (destination.startsWith("LENS - Van P")) {
      return "LENS - Van Pelt";
    }
    if (destination.startsWith("LENS - Ste-I")) {
      return "LENS - Sainte-Ide";
    }
    if (destination.startsWith("AVION - Leb")) {
      return "AVION - Lebas";
    }
    if (destination.startsWith("AVION - Lyc")) {
      return "AVION - Lycée Picasso";
    }
    if (destination.startsWith("GONNEHEM - Châ")) {
      return "GONNEHEM - Château d'Eau";
    }
    if (destination.startsWith("GONNEHEM - Rue")) {
      return "GONNEHEM - Rue de Paradis";
    }
    if (destination.startsWith("BEUVRY - Lyc")) {
      return "BEUVRY - Lycée Yourcenar";
    }
    if (destination.startsWith("BARLIN - Viei")) {
      return "BARLIN - Vieil Houdain";
    }
    if (destination.startsWith("REBREUVE-") && direction.startsWith("Mairie / Vieil")) {
      return "REBREUVE-RANCHICOURT - Mairie";
    }
    if (destination.startsWith("GRENAY - Verb")) {
      return "GRENAY - Verbrugge";
    }
    if (destination.startsWith("VERMELLES - So")) {
      return "VERMELLES - Socrate";
    }
    if (destination.startsWith("LINGHEM - Ma")) {
      return "LINGHEM - Mairie";
    }
    if (destination.startsWith("NORRENT") && direction.startsWith("Mairie / Fonte")) {
      return "NORRENT-FONTES - Fontes";
    }
    if (destination.startsWith("COURRIERES - Cen")) {
      return "COURRIÈRES - Centre Culturel";
    }
    if (destination.startsWith("HENIN-BEA") && direction.startsWith("Buisse / Cen")) {
      return "HÉNIN-BEAUMONT - Buisse";
    }

    for (Map.Entry<String, Function<String, String>> entry : specialCases.entrySet()) {
      if (destination.startsWith(entry.getKey())) {
        return entry.getValue().apply(direction);
      }
    }

    return null;
  }

  private static @NonNull Map<String, Function<String, String>> getSpecialCases() {
    Map<String, Function<String, String>> specialCases = new HashMap<>();

    specialCases.put("LENS - Béhal", dir -> {
      String quai = dir.substring(dir.length() - 1);
      return String.format("LENS - Béhal-Jean Zay - Quai %s", quai);
    });

    specialCases.put("BETHUNE - Gare", dir -> {
      String quai = dir.substring(dir.length() - 1);
      return String.format("BÉTHUNE - Gare - Quai %s", quai);
    });

    specialCases.put("BETHUNE - Clem", dir -> {
      String quai = dir.substring(dir.length() - 1);
      return String.format("BÉTHUNE - Clémenceau - Quai %s", quai);
    });

    specialCases.put("BEUVRY - Hôp", dir -> {
      String quai = dir.substring(dir.length() - 1);
      return String.format("BEUVRY - Hôpital - Quai %s", quai);
    });

    specialCases.put("LIBERCOURT - Gar", dir -> {
      String quai = dir.substring(dir.length() - 1);
      return String.format("LIBERCOURT - Gare - Quai %s", quai);
    });
    return specialCases;
  }

  private static String handleCityCases(String destination, String direction) {
    List<String> cityPrefixes = Arrays.asList(
        "COURCELLES-LES-L", "BRUAY-LA-B", "NOYELLES-G", "LIEVIN",
        "NOYELLES-LES-V", "VENDIN-LE-V", "LA BASSEE", "DIEVAL",
        "ELEU-DIT-L", "CALONNE-S", "HENIN-B", "ESTREE-C",
        "BILLY-BER", "BETHUNE", "ABLAIN-S", "PONT-A-V",
        "REBREUVE-", "FRESNICOURT-", "ESTREE-BL", "NORRENT-F", "GIVENCHY-EN",
        "GIVENCHY-LES", "COURRIERES"
    );

    for (String prefix : cityPrefixes) {
      if (destination.startsWith(prefix)) {
        return String.format("%s - %s", formatCityName(prefix), direction);
      }
    }

    return null;
  }

  private static String formatCityName(String cityPrefix) {
    switch (cityPrefix) {
      case "COURCELLES-LES-L":
        return "COURCELLES-LÈS-LENS";
      case "BRUAY-LA-B":
        return "BRUAY-LA-BUISSIÈRE";
      case "NOYELLES-G":
        return "NOYELLES-GODAULT";
      case "LIEVIN":
        return "LIÉVIN";
      case "NOYELLES-LES-V":
        return "NOYELLES-LES-VERMELLES";
      case "VENDIN-LE-V":
        return "VENDIN-LE-VIEIL";
      case "LA BASSEE":
        return "LA BASSÉE";
      case "DIEVAL":
        return "DIÉVAL";
      case "ELEU-DIT-L":
        return "ÉLEU-DIT-LEAUWETTE";
      case "CALONNE-S":
        return "CALONNE-SUR-LA-LYS";
      case "HENIN-B":
        return "HÉNIN-BEAUMONT";
      case "ESTREE-C":
        return "ESTRÉE-CAUCHY";
      case "BILLY-BER":
        return "BILLY-BERCLAU";
      case "BETHUNE":
        return "BÉTHUNE";
      case "ABLAIN-S":
        return "ABLAIN-SAINT-NAZAIRE";
      case "PONT-A-V":
        return "PONT-À-VENDIN";
      case "REBREUVE-":
        return "REBREUVE-RANCHICOURT";
      case "FRESNICOURT-":
        return "FRESNICOURT-LE-DOLMEN";
      case "ESTREE-BL":
        return "ESTRÉE-BLANCHE";
      case "NORRENT-F":
        return "NORRENT-FONTES";
      case "GIVENCHY-EN":
        return "GIVENCHY-EN-GOHELLE";
      case "GIVENCHY-LES":
        return "GIVENCHY-LÈS-LA-BASSÉE";
      case "COURRIERES":
        return "COURRIÈRES";

      default:
        return cityPrefix.replace("-", " ");
    }
  }

  public static String formatLineNumber(String lineNumber) {
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
        return "18E";
      case 90:
        return "Nav Béth.";
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
}
