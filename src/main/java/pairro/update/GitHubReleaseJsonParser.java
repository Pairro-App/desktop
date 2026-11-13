package pairro.update;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;

final class GitHubReleaseJsonParser {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private static final String RELEASE_URL_PREFIX = "https://github.com/";
  private static final String RELEASE_URL_PATH = "/releases/tag/";

  Release parse(String json) throws IOException {
    JsonNode root = MAPPER.readTree(json);

    String tag = root.path("tag_name").textValue();
    String url = root.path("html_url").textValue();

    if (tag == null || url == null) {
      throw new IOException("Unexpected GitHub response: missing tag_name or html_url");
    }

    if (!url.startsWith(RELEASE_URL_PREFIX) || !url.contains(RELEASE_URL_PATH)) {
      throw new IOException("Unexpected release URL: " + url);
    }

    try {
      return new Release(Version.parse(tag), URI.create(url));
    } catch (IllegalArgumentException exception) {
      throw new IOException("Unusable release tag: " + tag, exception);
    }
  }
}
