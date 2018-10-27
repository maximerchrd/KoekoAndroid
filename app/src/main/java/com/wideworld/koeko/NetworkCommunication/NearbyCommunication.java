package com.wideworld.koeko.NetworkCommunication;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.wideworld.koeko.Koeko;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class NearbyCommunication {
    private Context mNearbyContext;
    private final SimpleArrayMap<Long, Payload> incomingPayloads = new SimpleArrayMap<>();
    private final SimpleArrayMap<Long, String> incomingSmallData = new SimpleArrayMap<>();
    private final SimpleArrayMap<Long, Payload> outgoingPayloads = new SimpleArrayMap<>();
    private final SimpleArrayMap<Long, String> outgoingSmallData = new SimpleArrayMap<>();
    private String singleConnectionEndpointId;
    private Boolean isDiscovering = false;
    private Boolean isAdvertising = false;

    static private int NO_NEARBY_ROLE = 0;
    static private int ADVERTISER = 1;
    static private int DISCOVERER = 2;
    private int deviceRole = NO_NEARBY_ROLE;
    static private int nbOfTransfers= 0;
    static private Long start = 0L;

    private Boolean sender = false;



    private String TAG = "NearbyCommunication";

    NearbyCommunication(Context context) {
        mNearbyContext = context;
    }

    public void startAdvertising() {
        deviceRole = ADVERTISER;
        sender = true;
        AdvertisingOptions.Builder advertisingOptionsBuilder = new AdvertisingOptions.Builder();
        advertisingOptionsBuilder.setStrategy(Strategy.P2P_CLUSTER);
        Nearby.getConnectionsClient(mNearbyContext).startAdvertising(
                NetworkCommunication.deviceIdentifier,
                NetworkCommunication.nearbyServiceID,
                mConnectionLifecycleCallback,
                advertisingOptionsBuilder.build()
        )
                .addOnSuccessListener(
                        unusedResult -> {
                            isAdvertising = true;
                            Log.v(TAG, "Started advertising");
                        })
                .addOnFailureListener(
                        e -> e.printStackTrace());
    }

    private final EndpointDiscoveryCallback mEndpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(
                        String endpointId, DiscoveredEndpointInfo discoveredEndpointInfo) {
                    Nearby.getConnectionsClient(mNearbyContext).requestConnection(
                            NetworkCommunication.deviceIdentifier,
                            endpointId,
                            mConnectionLifecycleCallback)
                            .addOnSuccessListener(
                                    new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unusedResult) {
                                            Log.v(TAG, "Successfull Connection");
                                        }
                                    })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            e.printStackTrace();
                                        }
                                    });
                }


                @Override
                public void onEndpointLost(String endpointId) {
                    Log.v(TAG, "Endpoint Lost");
                }
            };

    public void startDiscovery() {
        deviceRole = DISCOVERER;
        DiscoveryOptions.Builder discoveryOptionsBuilder = new DiscoveryOptions.Builder();
        discoveryOptionsBuilder.setStrategy(Strategy.P2P_CLUSTER);
        Nearby.getConnectionsClient(mNearbyContext).startDiscovery(
                NetworkCommunication.nearbyServiceID,
                mEndpointDiscoveryCallback,
                discoveryOptionsBuilder.build())
                .addOnSuccessListener(
                        unusedResult -> {
                            isDiscovering = true;
                            Log.v(TAG, "Started discovering");
                        })
                .addOnFailureListener(
                        e -> e.printStackTrace());
    }



    private final ConnectionLifecycleCallback mConnectionLifecycleCallback =
            new ConnectionLifecycleCallback() {

                @Override
                public void onConnectionInitiated(
                        String endpointId, ConnectionInfo connectionInfo) {
                    // Automatically accept the connection on both sides.
                    Nearby.getConnectionsClient(mNearbyContext).acceptConnection(endpointId, mPayloadCallback);
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    switch (result.getStatus().getStatusCode()) {
                        case ConnectionsStatusCodes.STATUS_OK:
                            Log.v(TAG, "STATUS_OK");
                            Log.v(TAG, "time start: " + System.currentTimeMillis() / 1000);
                            singleConnectionEndpointId = endpointId;
                            if (sender) {
                                Nearby.getConnectionsClient(mNearbyContext).stopAdvertising();
                                isAdvertising = false;
                                String pay = "What the fuck Dude is that all you want from me!?";
                                for (int i = 0; i < 7; i++) {
                                    pay += pay;
                                }
                                FileOutputStream out = null;
                                File file = new File(mNearbyContext.getFilesDir(), "tmp.txt");

                                try {
                                    file.createNewFile();
                                    if(file.exists())
                                    {
                                        out = new FileOutputStream(file);
                                        out.write(pay.getBytes());
                                        out.close();
                                    } else {
                                        System.out.println("FUCK!!!");
                                    }

                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                Log.v(TAG, "file size: " + pay.getBytes().length);

                                while (true) {
                                    try {
                                        sendPayload(Payload.fromFile(file));
                                        Thread.sleep(5000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                }

                            } else {
                                if (isDiscovering) {
                                    Nearby.getConnectionsClient(mNearbyContext).stopDiscovery();
                                    isDiscovering = false;
                                }
                            }
                            break;
                        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                            Log.v(TAG, "STATUS_CONNECTION_REJECTED");
                            break;
                        case ConnectionsStatusCodes.STATUS_ERROR:
                            Log.v(TAG, "STATUS_ERROR");
                            break;
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {
                    Log.v(TAG, "DISCONNECTED");
                    if (deviceRole == ADVERTISER) {
                        startAdvertising();
                    } else if (deviceRole == DISCOVERER) {
                        startDiscovery();
                    }
                }
            };

    public void stopNearbyDiscoveryAndAdvertising() {
        if (isDiscovering) {
            Nearby.getConnectionsClient(mNearbyContext).stopDiscovery();
            isDiscovering = false;
        }
        if (isAdvertising) {
            Nearby.getConnectionsClient(mNearbyContext).stopAdvertising();
            isAdvertising = false;
        }
    }

    public void sendPayload(Payload payload) {
        //Payload payload = Payload.fromBytes(bytesData);

        outgoingPayloads.put(payload.getId(), payload);



        Nearby.getConnectionsClient(mNearbyContext).sendPayload(singleConnectionEndpointId,payload);
    }


    /**
     * Someone connected to us has sent us data. Override this method to act on the event.
     *
     * @param endpointID The sender.
     * @param payload The data.
     */
    protected void onReceive(String endpointID, Payload payload) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            //Payload.File file = payload.asFile();

        }
    }


    private final PayloadCallback mPayloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(String endpointId, Payload payload) {
                    Log.v(TAG, "onPayloadReceived");
                    start = System.currentTimeMillis() / 1000;
                    onReceive(endpointId, payload);
                }

                @Override
                public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
                    long payloadId = update.getPayloadId();
                    if (incomingPayloads.containsKey(payloadId)) {
                        Log.v(TAG, "onPayloadTransferUpdate");
                        if (update.getStatus() != PayloadTransferUpdate.Status.IN_PROGRESS) {
                            // This is the last update, so we no longer need to keep track of this notification.
                            incomingPayloads.remove(payloadId);
                        }
                    } else if (outgoingPayloads.containsKey(payloadId)) {
                        Log.v(TAG, "onPayloadTransferUpdate");
                        if (update.getStatus() != PayloadTransferUpdate.Status.IN_PROGRESS) {
                            // This is the last update, so we no longer need to keep track of this notification.
                            outgoingPayloads.remove(payloadId);
                        }
                    }

                    switch(update.getStatus()) {
                        case PayloadTransferUpdate.Status.IN_PROGRESS:
                            long size = update.getBytesTransferred();
                            if (size == -1) {
                                // This is a stream payload, so we don't need to update anything at this point.
                                return;
                            }
                            Log.v(TAG, "onPayloadTransferUpdate: " + size);
                            break;
                        case PayloadTransferUpdate.Status.SUCCESS:
                            // SUCCESS always means that we transferred 100%.
                            Log.v(TAG, "onPayloadTransferUpdate: SUCCESS: ");
                            nbOfTransfers++;
                            Long end = System.currentTimeMillis() / 1000;
                            Log.v(TAG, "time end: " + System.currentTimeMillis() / 1000);
                            String message = "nb of transfers: " + nbOfTransfers;
                            message += "\n lasted at least: " + String.valueOf(end - start) + " seconds";

                            Koeko.wifiCommunicationSingleton.mNetworkCommunication.mInteractiveModeActivity.showMessage(message);
                            break;
                        case PayloadTransferUpdate.Status.FAILURE:
                            Log.v(TAG, "onPayloadTransferUpdate: FAILURE");
                            break;
                    }
            }

    };

}
