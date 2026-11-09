package pairro.update;

import java.io.IOException;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class GitHubReleaseJsonParser {
  private static final Pattern TAG = Pattern.compile("\"tag_name\"\\s*:\\s*\"([^\"]+)\"");
  private static final Pattern URL = Pattern.compile(
      "\"html_url\"\\s*:\\s*\"(https://github\\.com/[^\"]+/releases/tag/[^\"]+)\"");

  Release parse(String json) throws IOException {
    String tag = firstGroup(TAG, json);
    String url = firstGroup(URL, json);

    if (tag == null || url == null) {
      throw new IOException("Unexpected GitHub response: missing tag_name or html_url");
    }

    try {
      return new Release(Version.parse(tag), URI.create(url));
    } catch (IllegalArgumentException exception) {
      throw new IOException("Unusable release tag: " + tag, exception);
    }
  }

  private static String firstGroup(Pattern pattern, String text) {
    Matcher matcher = pattern.matcher(text);

    return matcher.find() ? matcher.group(1) : null;
  }
}
