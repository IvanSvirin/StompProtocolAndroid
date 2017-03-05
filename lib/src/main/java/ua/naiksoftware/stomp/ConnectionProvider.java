package ua.naiksoftware.stomp;

import rx.Observable;
import ua.naiksoftware.stomp.client.StompMessage;

public interface ConnectionProvider {

    /**
     * Subscribe this for receive stomp messages
     */
    Observable<String> messages();

    /**
     * Sending stomp messages via you ConnectionProvider.
     * onError if not connected or error detected will be called, or onCompleted id sending started
     * TODO: send messages with ACK
     */
    Observable<Void> send(StompMessage stompMessage);

    /**
     * Subscribe this for receive #LifecycleEvent events
     */
    Observable<LifecycleEvent> getLifecycleReceiver();
}
