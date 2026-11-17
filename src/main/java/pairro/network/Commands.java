package pairro.network;

public final class Commands {
  public static final class Auth {
    public static final String OK = "AUTH_OK";
    public static final String FAIL = "AUTH_FAIL";
    public static final String CHALLENGE = "AUTH_CHALLENGE";
    public static final String INIT = "AUTH_INIT";
    public static final String PROOF = "AUTH_PROOF";
  }

  public static final class Pair {
    public static final String SUCCESS = "PAIR_SUCCESS";
    public static final String REQUEST = "PAIR_REQUEST";
    public static final String DECISION = "PAIR_DECISION";
    public static final String CHALLENGE = "PAIR_CHALLENGE";
  }

  public static final class System {
    public static final String PING = "PING";
    public static final String PONG = "PONG";
    public static final String DISCONNECT = "DISCONNECT";
  }

  public static final class Volume {
    public static final String SET = "VOLUME_SET:";
    public static final String GET = "VOLUME_GET";
    public static final String STATE = "VOLUME_STATE:";
    public static final String MUTE = "VOLUME_MUTE:";
  }

  public static final class Mouse {
    public static final String MOVE = "MOUSE_MOVE:";
    public static final String LEFT_CLICK = "MOUSE_LEFT_CLICK";
    public static final String RIGHT_CLICK = "MOUSE_RIGHT_CLICK";
    public static final String LEFT_DOWN = "MOUSE_LEFT_DOWN";
    public static final String LEFT_UP = "MOUSE_LEFT_UP";
  }

  public static final class Keyboard {
    public static final String KEY_COMBO = "KEY_COMBO:";
  }

  public static final class Clipboard {
    public static final String GET = "CLIPBOARD_GET";
    public static final String SET = "CLIPBOARD_SET:";
    public static final String APPEND = "CLIPBOARD_APPEND:";
    public static final String STATE = "CLIPBOARD_STATE:";
    public static final String ENTRY = "CLIPBOARD_ENTRY:";
    public static final String HISTORY_GET = "CLIPBOARD_HISTORY_GET";
    public static final String HISTORY_CLEAR = "CLIPBOARD_HISTORY_CLEAR";
    public static final String HISTORY_END = "CLIPBOARD_HISTORY_END";
  }
}
