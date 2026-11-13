package pairro.notification;

public interface NotificationPresenter {
  public void present(
    NotificationType type,
    String deviceName
  );
}
