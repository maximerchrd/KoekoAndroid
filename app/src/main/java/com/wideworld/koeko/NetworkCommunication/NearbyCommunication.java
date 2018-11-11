package com.wideworld.koeko.NetworkCommunication;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
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
import com.wideworld.koeko.QuestionsManagement.QuestionMultipleChoice;
import com.wideworld.koeko.QuestionsManagement.QuestionShortAnswer;
import com.wideworld.koeko.Tools.FileHandler;
import com.wideworld.koeko.Tools.IOUtils;
import com.wideworld.koeko.database_management.DbTableQuestionMultipleChoice;
import com.wideworld.koeko.database_management.DbTableQuestionShortAnswer;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class NearbyCommunication {
    private Context mNearbyContext;
    private final SimpleArrayMap<Long, Payload> incomingPayloads = new SimpleArrayMap<>();
    private final SimpleArrayMap<Long, Payload> outgoingPayloads = new SimpleArrayMap<>();
    private String singleConnectionEndpointId;
    private Boolean isDiscovering = false;
    private Boolean isAdvertising = false;
    private NearbyReceptionProtocol nearbyReceptionProtocol;

    static public int NO_NEARBY_ROLE = 0;
    static public int ADVERTISER_ROLE = 1;
    static public int DISCOVERER_ROLE = 2;
    static public int deviceRole = NO_NEARBY_ROLE;
    static private int nbOfTransfers= 0;


    private String TAG = "NearbyCommunication";

    NearbyCommunication(Context context) {
        mNearbyContext = context;
        nearbyReceptionProtocol = new NearbyReceptionProtocol(mNearbyContext, this);
    }

    public void startAdvertising() {
        deviceRole = ADVERTISER_ROLE;
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
                                            if (deviceRole == ADVERTISER_ROLE) {
                                                syncWithClients();
                                            }
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
                    Log.e(TAG, "Endpoint Lost");
                }
            };

    public void startDiscovery() {
        deviceRole = DISCOVERER_ROLE;
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
                            singleConnectionEndpointId = endpointId;
                            if (isAdvertising) {
                                Nearby.getConnectionsClient(mNearbyContext).stopAdvertising();
                                isAdvertising = false;
                            } else if (isDiscovering) {
                                Nearby.getConnectionsClient(mNearbyContext).stopDiscovery();
                                isDiscovering = false;
                                if (Koeko.networkCommunicationSingleton.getServerHotspot() != null) {
                                    if (!Koeko.networkCommunicationSingleton.getServerHotspot().configApState()) {
                                        Log.d(TAG, "onConnectionResult: unable to start Hotspot");
                                        Koeko.networkCommunicationSingleton.sendStringToServer("HOTSPOTFAIL///");
                                    }
                                } else {
                                    System.err.println("Server hotspot is null when trying to start");
                                }
                            }
                            break;
                        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                            Log.v(TAG, "STATUS_CONNECTION_REJECTED");
                            break;
                        case ConnectionsStatusCodes.STATUS_ALREADY_CONNECTED_TO_ENDPOINT:
                            Log.d(TAG, "onConnectionResult: STATUS_ALREADY_CONNECTED_TO_ENDPOINT");
                            Nearby.getConnectionsClient(mNearbyContext).stopDiscovery();
                            isDiscovering = false;
                            if (Koeko.networkCommunicationSingleton.getServerHotspot() != null) {
                                if (!Koeko.networkCommunicationSingleton.getServerHotspot().configApState()) {
                                    Log.d(TAG, "onConnectionResult: unable to start Hotspot");
                                    Koeko.networkCommunicationSingleton.sendStringToServer("HOTSPOTFAIL///");
                                }
                            } else {
                                System.err.println("Server hotspot is null when trying to start");
                            }
                        case ConnectionsStatusCodes.STATUS_ERROR:
                            Log.v(TAG, "STATUS_ERROR");
                            break;
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {
                    Log.v(TAG, "DISCONNECTED");
                    if (deviceRole == ADVERTISER_ROLE) {
                        startAdvertising();
                    } else if (deviceRole == DISCOVERER_ROLE) {
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

    public void sendBytes(byte[] bytesData) {
        Log.v(TAG, "Sending: " + bytesData.length + " bytes");
        try {
            Log.v(TAG, new String(bytesData, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Payload payload;
        if (bytesData.length < ConnectionsClient.MAX_BYTES_DATA_SIZE) {
            payload = Payload.fromBytes(bytesData);
            Nearby.getConnectionsClient(mNearbyContext).sendPayload(singleConnectionEndpointId,payload);
        } else {
            File directory = new File(mNearbyContext.getFilesDir(), FileHandler.mediaDirectoryNoSlash);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            File tmpFile = new File(directory, "nearby_tmp_file.out");
            if (tmpFile.exists()) {
                tmpFile.delete();
            }
            try (FileOutputStream fos = new FileOutputStream(tmpFile)) {
                fos.write(bytesData);
                System.out.println("Data bigger than: " + ConnectionsClient.MAX_BYTES_DATA_SIZE);
                // Add it to the tracking list so we can update it.
                payload = Payload.fromFile(tmpFile);
                outgoingPayloads.put(payload.getId(), payload);
                Nearby.getConnectionsClient(mNearbyContext).sendPayload(singleConnectionEndpointId, payload);
                tmpFile.delete();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    public void syncWithClients() {
        // Lambda Runnable
        Runnable syncThread = () -> {
            for (String id : Koeko.networkCommunicationSingleton.idsToSync) {
                try {
                    if (Long.valueOf(id) < 0) {

                    } else {
                        QuestionMultipleChoice questionMultipleChoice = DbTableQuestionMultipleChoice.getQuestionWithId(id);
                        if (questionMultipleChoice.getQuestion().length() > 0 && !questionMultipleChoice.getQuestion().contentEquals("none")) {

                        } else {
                            QuestionShortAnswer questionShortAnswer = DbTableQuestionShortAnswer.getShortAnswerQuestionWithId(id);
                            if (questionShortAnswer.getQuestion().length() > 0 && !questionShortAnswer.getQuestion().contentEquals("none")) {

                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(syncThread).start();
    }

    public void sendPayload(Payload payload) {
        //Payload payload = Payload.fromBytes(bytesData);

        outgoingPayloads.put(payload.getId(), payload);



        Nearby.getConnectionsClient(mNearbyContext).sendPayload(singleConnectionEndpointId,payload);
    }


    private final PayloadCallback mPayloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(String endpointId, Payload payload) {
                    Log.v(TAG, "onPayloadReceived");
                    if (payload.getType() == Payload.Type.BYTES) {
                        nearbyReceptionProtocol.receivedData(payload.asBytes());
                    } else if (payload.getType() == Payload.Type.FILE) {
                        // Add this to our tracking map, so that we can retrieve the payload later.
                        Log.d(TAG, "onPayloadReceived: FILE");
                        incomingPayloads.put(payload.getId(), payload);
                    }
                }

                @Override
                public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
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
                            if (deviceRole == DISCOVERER_ROLE) {
                                Payload payload = incomingPayloads.remove(update.getPayloadId());
                                if (payload != null && payload.getType() == Payload.Type.FILE) {
                                    try {
                                        byte[] allData = IOUtils.readFile(payload.asFile().asJavaFile());
                                        nearbyReceptionProtocol.receivedData(allData);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            break;
                        case PayloadTransferUpdate.Status.FAILURE:
                            Log.v(TAG, "onPayloadTransferUpdate: FAILURE");
                            break;
                    }
            }

    };

    public void closeConnection() {
        if(isAdvertising) {
            sendBytes( "Shutting down host".getBytes());
            Nearby.getConnectionsClient(mNearbyContext).stopAllEndpoints();
            isAdvertising = false;
        } else {
            sendBytes( "Shutting down client".getBytes());
            Nearby.getConnectionsClient(mNearbyContext).stopAllEndpoints();
            isDiscovering = false;
        }
    }
}
