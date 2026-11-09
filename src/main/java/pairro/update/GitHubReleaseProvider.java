package pairro.update;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

public final class GitHubReleaseProvider implements ReleaseProvider {
  private final HttpClient http;
  private final URI endpoint;
  private final String userAgent;
  private final GitHubReleaseJsonParser parser = new GitHubReleaseJsonParser();

  private String etag;
  private Release cached;

  public GitHubReleaseProvider(HttpClient http, String owner, String repo) {
    this.http = http;
    this.endpoint = URI.create("https://api.github.com/repos/" + owner + "/" + repo + "/releases/latest");
    this.userAgent = repo + "-update-checker";
  }

  public static HttpClient defaultHttpClient() {
    return HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
  }

  @Override
  public synchronized Optional<Release> fetchLatest() throws IOException, InterruptedException {
    HttpRequest.Builder request = HttpRequest.newBuilder(endpoint)
        .timeout(Duration.ofSeconds(15))
        .header("Accept", "application/vnd.github+json")
        .header("User-Agent", userAgent);

    if (etag != null) {
      request.header("If-None-Match", etag);
    }

    HttpResponse<String> response = http.send(request.build(), HttpResponse.BodyHandlers.ofString());

    return switch (response.statusCode()) {
      case 200 -> {
        cached = parser.parse(response.body());
        etag = response.headers().firstValue("ETag").orElse(null);
        yield Optional.of(cached);
      }
      case 304 -> Optional.ofNullable(cached);
      case 404 -> Optional.empty();
      default -> throw new IOException("GitHub API returned HTTP " + response.statusCode());
    };
  }
}
