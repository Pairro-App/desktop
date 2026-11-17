package pairro.update;

import java.net.URI;

public record Release(
  Version version,
  URI  pageUrl
) {}
