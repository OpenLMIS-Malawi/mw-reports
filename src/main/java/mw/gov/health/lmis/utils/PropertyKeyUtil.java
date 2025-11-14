package mw.gov.health.lmis.utils;

import java.util.regex.Pattern;

public class PropertyKeyUtil {

  private static final Pattern INVALID_CHARACTERS_PATTERN = Pattern.compile("[^a-zA-Z0-9_]");

  /**
   * Transforms a string to a valid property key format.
   * - Trims whitespace
   * - Converts to uppercase
   * - Replaces spaces with underscores
   * - Removes invalid characters
   * - Ensures the key does not start with a number
   *
   * @param nameToTransform input string
   * @return the valid name which can be used as property key
   */
  public static String transformToPropertyKey(String nameToTransform) {
    if (nameToTransform == null || nameToTransform.trim().isEmpty()) {
      return null;
    }

    String transformed = nameToTransform.trim().toUpperCase().replace(" ", "_");

    transformed = INVALID_CHARACTERS_PATTERN.matcher(transformed).replaceAll("");

    if (Character.isDigit(transformed.charAt(0))) {
      transformed = "_" + transformed;
    }

    return transformed + "_RIGHT";
  }
}
