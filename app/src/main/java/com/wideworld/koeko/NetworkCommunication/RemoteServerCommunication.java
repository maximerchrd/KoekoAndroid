package com.wideworld.koeko.NetworkCommunication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wideworld.koeko.QuestionsManagement.Homework;
import com.wideworld.koeko.database_management.DbTableSettings;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class RemoteServerCommunication {
    private final String cmdReadObject = "SOBJ";
    private final String cstrOKContinue = "TROK";
    private final String cstrReceivedOK = "ROOK";
    private final String cstrTerminator = "+";
    private final String cstrByeByeDude = "BYEB";
    private final String cstrUplJSONObj = "JSOB";
    private final String cstrUplSimpStr = "STRI";

    private Socket _socket;
    private InputStream _inStream;
    private OutputStream _outStream;

    private int serverPort = 50507;

    static private RemoteServerCommunication remoteServerCommunication;

    public ArrayList<Homework> getUpdatedHomeworksForCode(String code) throws Exception {
        InitializeTransfer(getInetAddress());
        SendSimpleString(code);
        ArrayList<Homework> homeworks = ReadArrayOfHomeworks();
        EndSynchronisation();

        return homeworks;
    }

    private ArrayList<Homework> ReadArrayOfHomeworks() throws IOException {
        ArrayList<Homework> homeworkArrayList = new ArrayList<>();

        int fileSize;
        do {
            byte[] lenBuf = new byte[4];
            _inStream.read(lenBuf);
            fileSize = BytesToInt(lenBuf);
            if (fileSize != 0) {
                byte[] contents = new byte[fileSize];

                //No of bytes read in one read() call
                int bytesRead;
                int readTillNow = 0;

                while (readTillNow != fileSize) {
                    bytesRead = _inStream.read(contents, readTillNow, fileSize - readTillNow);
                    readTillNow += bytesRead;
                }

                ObjectMapper mapper = new ObjectMapper();
                Homework homework = mapper.readValue(new String(contents), Homework.class);
                homeworkArrayList.add(homework);
            }
        } while (fileSize != 0);

        return homeworkArrayList;
    }

    private InetAddress getInetAddress() throws UnknownHostException {
        return InetAddress.getByName(DbTableSettings.getInternetServer());
    }

    private void InitializeTransfer(InetAddress serverAddress) throws Exception {
        this._socket = new Socket(serverAddress, serverPort);
        // Initialize
        _inStream = _socket.getInputStream();
        _outStream = _socket.getOutputStream();
    }

    public String SendSerializableObject(Object obj) throws IOException {
        SendCommande(cstrUplJSONObj);

        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        //Get socket's output stream and send object
        this._outStream.write(jsonString.getBytes());
        this._outStream.flush();

        // Get the acknowledgment that the object has been received
        // and the result of the processing
        String muid = ObjectReceived();
        return muid;
    }

    public void SendSimpleString(String text) throws IOException {
        SendCommande(cstrUplSimpStr);

        //Get socket's output stream and send object
        byte[] allBytes = new byte[text.getBytes().length + 4];
        byte[] length = IntToBytes(text.getBytes().length);
        System.arraycopy(length, 0, allBytes, 0, length.length);
        System.arraycopy(text.getBytes(), 0, allBytes, length.length, text.getBytes().length);


        this._outStream.write(allBytes);
        this._outStream.flush();
    }

    static public RemoteServerCommunication singleton() {
        if (remoteServerCommunication == null) {
            remoteServerCommunication = new RemoteServerCommunication();
        }
        return remoteServerCommunication;
    }

    public void EndCOmmunication() throws IOException { _socket.close(); }

    // Message received indicating that the last step has been accomplished and
    // that the process can continue
    // Last step is OK, continue
    public boolean IsAcknowledged() throws IOException {
        byte[] rdBuffer = new byte[4];
        int rdb = _inStream.read(rdBuffer);
        String str = new String(rdBuffer);
        return str.equals(cstrOKContinue);
    }

    private Object GetObject() throws IOException {
        byte[] lenBuf = new byte[4];
        _inStream.read(lenBuf);
        int fileSize = BytesToInt(lenBuf);
        byte[] contents = new byte[fileSize];

        //No of bytes read in one read() call
        int bytesRead;
        int readTillNow = 0;

        while(readTillNow != fileSize) {
            bytesRead = _inStream.read(contents, readTillNow, fileSize - readTillNow);
            readTillNow += bytesRead;
        }

        ObjectMapper mapper = new ObjectMapper();
        Object obj = mapper.readValue(new String(contents), Object.class);

        return obj;
    }

    // Last object sent has been received and processed,
    // the result of that processing of the object is the muid
    // of the object in GLOBAL_COLLECT, other wise, the result is empty
    public String ObjectReceived() throws IOException {
        byte[] rdBuffer = new byte[20];
        int rdb = _inStream.read(rdBuffer);
        String str = new String(rdBuffer);
        if (str.substring(0,4).equals(cstrReceivedOK) && str.substring(19,20).equals(cstrTerminator))
            return str.substring(4,19);
        else
            return "";
    }

    // Send the command to the server
    public void SendCommande(String commande) throws IOException  {
        byte[] cmdBuf = commande.getBytes();
        if (cmdBuf.length != 4)
            return;
        _outStream.write(cmdBuf);
        _outStream.flush();
    }

    public void EndSynchronisation() {
        try {
            SendCommande(cstrByeByeDude);
            _socket.close();
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void getHomeworksWithHWCode() {

    }

    public static byte[] IntToBytes(int i) {
         return ByteBuffer.allocate(4).putInt(i).array();
    }

    public static int BytesToInt(byte[] b) {
        return ByteBuffer.wrap(b).getInt();
    }
}
