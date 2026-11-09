package pairro.update;

import java.io.IOException;
import java.util.Optional;

public interface ReleaseProvider {
  Optional<Release> fetchLatest() throws IOException, InterruptedException;
}
