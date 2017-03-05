package ua.naiksoftware.stompclientexample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.java_websocket.WebSocket;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.client.StompClient;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private List<String> mDataSet = new ArrayList<>();
    private StompClient mStompClient;
    private final SimpleDateFormat mTimeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    private Gson mGson = new GsonBuilder().create();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        SimpleAdapter mAdapter = new SimpleAdapter(mDataSet);
        mAdapter.setHasStableIds(true);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true));

        connectStomp();
    }

    private void connectStomp() {
        mStompClient = Stomp.over(WebSocket.class, "ws://echo.websocket.org");

        mStompClient.lifecycle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(lifecycleEvent -> {
                    switch (lifecycleEvent.getType()) {
                        case OPENED:
                            toast("Stomp connection opened");
                            break;
                        case ERROR:
                            Log.e(TAG, "Stomp connection error", lifecycleEvent.getException());
                            toast("Stomp connection error");
                            break;
                        case CLOSED:
                            toast("Stomp connection closed");
                        case MESSAGE:
                            toast(lifecycleEvent.getMessage());
                    }
                });

        // Receive greetings
//        mStompClient.topic("/topic/greetings")
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(topicMessage -> {
//                    Log.d(TAG, "Received " + topicMessage.getPayload());
//                    addItem(mGson.fromJson(topicMessage.getPayload(), EchoModel.class));
//                });

        mStompClient.connect();
    }

    public void sendEchoViaStomp(View v) {
        mStompClient.send("/topic/hello-msg-mapping", "Echo STOMP " + mTimeFormat.format(new Date()))
                .compose(applySchedulers())
                .subscribe(aVoid -> {
                    Log.d(TAG, "STOMP echo send successfully");
                }, throwable -> {
                    Log.e(TAG, "Error send STOMP echo", throwable);
                    toast(throwable.getMessage());
                });
    }

//    private void addItem(EchoModel echoModel) {
//        mDataSet.add(echoModel.getEcho() + " - " + mTimeFormat.format(new Date()));
//        mAdapter.notifyDataSetChanged();
//        mRecyclerView.smoothScrollToPosition(mDataSet.size() - 1);
//    }

    private void toast(String text) {
        Log.i(TAG, text);
        if (text != null) {
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        }
    }

    protected <T> Observable.Transformer<T, T> applySchedulers() {
        return rObservable -> rObservable
                .unsubscribeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    protected void onDestroy() {
        mStompClient.disconnect();
        super.onDestroy();
    }
}
