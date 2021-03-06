package com.wideworld.koeko.NetworkCommunication;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;
import android.widget.Toast;

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
import com.wideworld.koeko.Koeko;
import com.wideworld.koeko.NetworkCommunication.HotspotServer.HotspotServer;
import com.wideworld.koeko.QuestionsManagement.QuestionMultipleChoice;
import com.wideworld.koeko.QuestionsManagement.QuestionShortAnswer;
import com.wideworld.koeko.R;
import com.wideworld.koeko.Tools.FileHandler;
import com.wideworld.koeko.database_management.DbTableQuestionMultipleChoice;
import com.wideworld.koeko.database_management.DbTableQuestionShortAnswer;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.CopyOnWriteArrayList;

public class NearbyCommunication {
    private Context mNearbyContext;
    private final SimpleArrayMap<Long, Payload> incomingPayloads = new SimpleArrayMap<>();
    private CopyOnWriteArrayList<String> connectionEndpointIds = new CopyOnWriteArrayList<>();
    public Boolean isDiscovering = false;
    public Boolean isAdvertising = false;
    private NearbyReceptionProtocol nearbyReceptionProtocol;

    static public int NO_NEARBY_ROLE = 0;
    static public int ADVERTISER_ROLE = 1;
    static public int DISCOVERER_ROLE = 2;
    static public int deviceRole = NO_NEARBY_ROLE;
    static private int nbOfTransfers= 0;

    static public int NEARBY_TESTING = 0;

    private long testStart = 0L;

    private String TAG = "NearbyCommunication";

    public NearbyCommunication(Context context) {
        mNearbyContext = context;
        nearbyReceptionProtocol = new NearbyReceptionProtocol(mNearbyContext, this);
    }

    public void startAdvertising() {
        stopNearbyDiscoveryAndAdvertising();
        //closeNearbyConnection();
        NetworkCommunication.network_solution = 1;
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
                            Koeko.networkCommunicationSingleton.mInteractiveModeActivity.changeNearbyAdvertiseButtonText("Advertising...");
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
                                    unusedResult -> {
                                        Log.v(TAG, "Successfull Connection");
                                        if (deviceRole == ADVERTISER_ROLE) {
                                            syncWithClients();
                                        }
                                    })
                            .addOnFailureListener(
                                    e -> e.printStackTrace());
                }


                @Override
                public void onEndpointLost(String endpointId) {
                    Log.e(TAG, "Endpoint Lost");
                }
            };

    public void startDiscovery() {
        stopNearbyDiscoveryAndAdvertising();
        closeNearbyConnection();
        NetworkCommunication.network_solution = 1;
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
                            if (NearbyCommunication.NEARBY_TESTING == 1) {
                                HotspotServer hotspotServer = new HotspotServer("koeko", "12345678", mNearbyContext);
                                Koeko.networkCommunicationSingleton.setHotspotServerHotspot(hotspotServer);
                            }
                            Log.v(TAG, "Started discovering");
                            Koeko.networkCommunicationSingleton.mInteractiveModeActivity.changeNearbyDiscoverButtonText("Discovering...");
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
                    Log.d(TAG, "onConnectionInitiated");
                    Nearby.getConnectionsClient(mNearbyContext).acceptConnection(endpointId, mPayloadCallback);
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    switch (result.getStatus().getStatusCode()) {
                        case ConnectionsStatusCodes.STATUS_OK:
                            Log.v(TAG, "STATUS_OK");
                            connectionEndpointIds.add(endpointId);
                            Koeko.networkCommunicationSingleton.mInteractiveModeActivity.setBridgesNumber(connectionEndpointIds.size());
                            if (isAdvertising) {
                                //TODO: check if the device is the right one (could be another device that failed to stop discovering correctly)
                                Nearby.getConnectionsClient(mNearbyContext).stopAdvertising();
                                isAdvertising = false;
                                Koeko.networkCommunicationSingleton.mInteractiveModeActivity
                                        .changeNearbyAdvertiseButtonText(mNearbyContext.getString(R.string.advertise));
//                                if (connectionEndpointIds.size() == 1) {
//                                    new Thread(() -> {
//                                        if (DbTableSettings.getHotspotConfiguration() == 2) {
//                                            File imgFile = new File(Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/Camera/20190312_210053.jpg");
//                                            int size = (int) imgFile.length();
//                                            byte[] bytes = new byte[size];
//                                            try {
//                                                BufferedInputStream buf = new BufferedInputStream(new FileInputStream(imgFile));
//                                                buf.read(bytes, 0, bytes.length);
//                                                buf.close();
//                                            } catch (FileNotFoundException e) {
//                                                e.printStackTrace();
//                                            } catch (IOException e) {
//                                                e.printStackTrace();
//                                            }
//                                            if (imgFile.exists()) {
//                                                for (int i = 0; i < 700; i++) {
//                                                    if (!isAdvertising) {
//                                                        System.out.println("sending  TEST FILE");
//                                                        sendBytes(bytes);
//                                                    }
//                                                    try {
//                                                        Thread.sleep(10000);
//                                                    } catch (InterruptedException e) {
//                                                        e.printStackTrace();
//                                                    }
//                                                }
//                                            } else {
//                                                Log.d(TAG, "onConnectionResult: Unable to load file");
//                                            }
//                                        } else if (DbTableSettings.getHotspotConfiguration() == 1) {
//                                            for (int i = 0; i < 700; i++) {
//                                                System.out.println("sending PING");
//                                                sendBytes("PING///".getBytes());
//                                                try {
//                                                    Thread.sleep(10000);
//                                                } catch (InterruptedException e) {
//                                                    e.printStackTrace();
//                                                }
//                                            }
//                                        }
//                                    }).start();
//                                }
                            } else if (isDiscovering) {
                                Koeko.networkCommunicationSingleton.mInteractiveModeActivity
                                        .changeNearbyDiscoverButtonText(mNearbyContext.getString(R.string.discover));
                                NetworkCommunication.connected = 1;

                                testStart = System.currentTimeMillis();
                                Koeko.networkCommunicationSingleton.mInteractiveModeActivity.showConnected();
                                Nearby.getConnectionsClient(mNearbyContext).stopDiscovery();
                                Koeko.networkCommunicationSingleton.closeOnlyWifiConnection();
                                /*ClientToServerTransferable transferable = new ClientToServerTransferable(CtoSPrefix.successPrefix);
                                transferable.setOptionalArgument1(NetworkCommunication.deviceIdentifier);
                                System.out.println("sending SUCCESS");
                                sendBytes(transferable.getTransferableBytes());*/
                                isDiscovering = false;
                                /*if (Koeko.networkCommunicationSingleton.getHotspotServerHotspot() != null) {
                                    if (!Koeko.networkCommunicationSingleton.getHotspotServerHotspot().configHotspotState()) {
                                        Log.d(TAG, "onConnectionResult: unable to start Hotspot");
                                        ClientToServerTransferable transferable2 = new ClientToServerTransferable(CtoSPrefix.failPrefix);
                                        transferable2.setOptionalArgument1(NetworkCommunication.deviceIdentifier);
                                        Koeko.networkCommunicationSingleton.sendBytesToServer(transferable2.getTransferableBytes());
                                    } else if (!HotspotServer.serverON) {
                                        Koeko.networkCommunicationSingleton.getHotspotServerHotspot().startHotspotServer();
                                    }
                                } else {
                                    System.err.println("HotspotServer hotspot is null when trying to start");
                                }*/
                            }
                            break;
                        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                            Log.v(TAG, "STATUS_CONNECTION_REJECTED");
                            break;
                        case ConnectionsStatusCodes.STATUS_ALREADY_CONNECTED_TO_ENDPOINT:
                            Log.w(TAG, "onConnectionResult: STATUS_ALREADY_CONNECTED_TO_ENDPOINT");
                            break;
                        case ConnectionsStatusCodes.STATUS_ERROR:
                            Log.v(TAG, "STATUS_ERROR");
                            if (deviceRole == ADVERTISER_ROLE) {
                                startAdvertising();
                            } else if (deviceRole == DISCOVERER_ROLE) {
                                startDiscovery();
                            }
                            break;
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {
                    Log.v(TAG, endpointId + " DISCONNECTED");
                    NetworkCommunication.connected = 0;
                    if (deviceRole == ADVERTISER_ROLE) {
                        startAdvertising();
                    } else if (deviceRole == DISCOVERER_ROLE) {
                        startDiscovery();
                    }

                    connectionEndpointIds.remove(endpointId);
                    Koeko.networkCommunicationSingleton.mInteractiveModeActivity.setBridgesNumber(connectionEndpointIds.size());

                    ((Activity)mNearbyContext).runOnUiThread(() -> Koeko.networkCommunicationSingleton.mInteractiveModeActivity.hotspotButtonDiscover.setTextColor(Color.BLUE));
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
        for (String toEndpointId : connectionEndpointIds) {
            Log.v(TAG, "Sending: " + bytesData.length + " bytes");
            try {
                Log.v(TAG, new String(bytesData, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            Payload payload;
            if (bytesData.length < ConnectionsClient.MAX_BYTES_DATA_SIZE) {
                payload = Payload.fromBytes(bytesData);
                Nearby.getConnectionsClient(mNearbyContext).sendPayload(toEndpointId, payload);
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
                    Log.d(TAG, "Data bigger than: " + ConnectionsClient.MAX_BYTES_DATA_SIZE);
                    Log.d(TAG, "Number of clients: " + connectionEndpointIds.size());
                    // Add it to the tracking list so we can update it.
                    payload = Payload.fromFile(tmpFile);
                    Log.d(TAG, "sendBytes: to " + toEndpointId);
                    Nearby.getConnectionsClient(mNearbyContext).sendPayload(toEndpointId, payload);
                    tmpFile.delete();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
    }

    public void syncWithClients() {
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


    private final PayloadCallback mPayloadCallback =
            new PayloadCallback() {
                private long transferStart = 0;
                @Override
                public void onPayloadReceived(String endpointId, Payload payload) {
                    Log.v(TAG, "onPayloadReceived from: " + endpointId);
                    if (payload.getType() == Payload.Type.BYTES) {
                        nearbyReceptionProtocol.receivedData(payload.asBytes());
                        if (NearbyCommunication.deviceRole == NearbyCommunication.DISCOVERER_ROLE
                                && Koeko.networkCommunicationSingleton.getHotspotServerHotspot() != null) {
                            Koeko.networkCommunicationSingleton.getHotspotServerHotspot().sendDataToClients(null, payload.asBytes());
                        }
                    } else if (payload.getType() == Payload.Type.FILE) {
                        // Add this to our tracking map, so that we can retrieve the payload later.
                        Log.d(TAG, "onPayloadReceived: FILE");
                        transferStart = System.currentTimeMillis();
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
                            Log.v(TAG, "onPayloadTransferUpdate IN_PROGRESS from " + endpointId + ": " + size);
                            break;
                        case PayloadTransferUpdate.Status.SUCCESS:
                            // SUCCESS always means that we transferred 100%.
                            Log.v(TAG, "onPayloadTransferUpdate: SUCCESS: " + endpointId + " for: " + update.getTotalBytes() + " bytes");
                            long timeInMillis = System.currentTimeMillis() - transferStart;
                            if (deviceRole == DISCOVERER_ROLE) {
                                Payload payload = incomingPayloads.remove(update.getPayloadId());
                                if (payload != null && payload.getType() == Payload.Type.FILE) {
                                    //try {
                                        //byte[] allData = IOUtils.readFile(payload.asFile().asJavaFile());

                                        //for testing
                                        Log.d(TAG, "onPayloadTransferUpdate: transfer rate = " + update.getBytesTransferred() / timeInMillis + "kB/s");
                                        Toast.makeText(mNearbyContext, "Seconds since test start: " + (System.currentTimeMillis() - testStart) / 1000 +"\nTransfer rate = " + update.getBytesTransferred() / timeInMillis + " kB/s", Toast.LENGTH_LONG)
                                                .show();

                                        /*nearbyReceptionProtocol.receivedData(allData);
                                        Koeko.networkCommunicationSingleton.getHotspotServerHotspot().sendDataToClients(null, allData);*/
                                    /*} catch (IOException e) {
                                        e.printStackTrace();
                                    }*/
                                }
                            }
                            break;
                        case PayloadTransferUpdate.Status.FAILURE:
                            Log.v(TAG, "onPayloadTransferUpdate: FAILURE from: " + endpointId);
                            break;
                    }
            }

    };

    public void closeNearbyConnection() {
        if(isAdvertising) {
            sendBytes("Shutting down host".getBytes());
            Nearby.getConnectionsClient(mNearbyContext).stopAllEndpoints();
            isAdvertising = false;
            Log.d(TAG, "closeNearbyConnection: was advertiser");
        } else {
            sendBytes("Shutting down client".getBytes());
            Nearby.getConnectionsClient(mNearbyContext).stopAllEndpoints();
            isDiscovering = false;
            Log.d(TAG, "closeNearbyConnection: was discoverer");
        }
    }
}
