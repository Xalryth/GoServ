package Sockets;

import Logging.Logger;
import Helpers.Character;
import Repositories.Specifications.CharacterRepository;
import Repositories.Specifications.EnjoyerRepository;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WizardGoServer extends Thread implements MultiClientServer, ClientIdGenerator
{
    //variable for holding the max amount of client connections that are allowed at any point in time
    final public static int MAX_CLIENTS = 2000;
    //global buffer size
    final public static int BUFFER_SIZE = 2048;
    //Location update interval
    final public static int LOCATION_UPDATE_INTERVAL = 500;

    //the tcp socket for the server to handle new connections as well as keeping track of/managing currently connected clients
    final private ServerSocket tcpSocket;
    //the udp server socket for receiving udp packets after an initial connection has been established
    final private DatagramSocket udpSocket;
    //logger implementation
    final private Logger logger;
    //mapping of connection objects and their respective connection id which is used for udp communication
    private static ConcurrentHashMap<Integer, ClientConnectionObject> clientMap;

    //declare repositories to draw data from
    final private EnjoyerRepository enjoyerRepo = new EnjoyerRepository();
    final private CharacterRepository characterRepo = new CharacterRepository();

    public WizardGoServer(int tcpPort, int udpPort, Logger logger) throws IOException {
        //initialize the tcp socket
        tcpSocket = new ServerSocket(tcpPort);
        //initialize the udp socket
        udpSocket = new DatagramSocket(udpPort);
        //initialize client map
        clientMap = new ConcurrentHashMap<>();

        //define logging solution
        this.logger = logger;

        //start udp listener thread
        UdpReceiverThread udpThread = new UdpReceiverThread();
        udpThread.start();

        //start tcp listener thread and begin accepting clients
        start();
    }

    @Override
    //Assign a new socket to a connection object with information,
    //about the individual connection as well as the id that they will be communicating with
    public void assignClient(Socket connection) {
        //fetch new id
        Integer id = generateClientId();
        //create new connection object based on the socket
        ClientConnectionObject client = new ClientConnectionObject(connection, id);
        //add connection to client map
        synchronized (clientMap){
            clientMap.put(id, client);
        }

        //print client information through the logger implementation
        logger.logMessage("(" + client.getSocket().getInetAddress().getHostAddress() + ") connected with id: " + id.toString());

        //start client object thread
        client.start();
    }

    @Override
    //closes a client connection based on the connection id
    public void closeClient(int connectionId) {
        //TODO: this
    }

    @Override
    //Generates a new client id which is not present in the current client mapping
    public Integer generateClientId() {
        Integer newId;

        //Generate id until one that is not present in the client map is encountered
        do{
            newId = random.nextInt();
        }while(clientMap.containsKey(newId));

        //return the new id
        return newId;
    }

    @Override
    public void run(){
        //start udp thread for receiving and sending position data
        //Thread udpThread

        while(!this.isInterrupted()){
            try {
                //listen for new connections via tcp
                Socket newConnection = tcpSocket.accept();
                //assign a client id to the client
                assignClient(newConnection);
            } catch (IOException e) {
                //log error
                logger.logError(e);
            }
        }
    }


    private void changeUserCharacter(long id, ClientConnectionObject client){
        //check if user owns the characterId

        //change characterId on connection object
        //broadcast characterId change to other clients
        //TODO: this too
    }

    //position updater thread for updating location data between clients
    protected class UdpReceiverThread extends Thread {
        //create executor for handling simple user requests without having to allocate new threads at runtime
        ExecutorService executor = Executors.newFixedThreadPool(3);

        @Override
        public void run(){
            //declare receive buffer
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

            //start a thread meant for location updates based on input location data
            LocationUpdaterThread locationUpdaterThread = new LocationUpdaterThread();
            locationUpdaterThread.start();

            //udp receiver loop
            while(!this.isInterrupted()){
                //set data packet as empty
                DatagramPacket newPacket = new DatagramPacket(buffer.array(), buffer.array().length);

                try {
                    //fill packet with new data
                    udpSocket.receive(newPacket);

                    try{
                        //get client id
                        Integer clientId = buffer.getInt();
                        //get position coordinates from request
                        float lng = buffer.getFloat();
                        float lat = buffer.getFloat();

                        //start new thread to handle player position update
                        executor.execute(new SetPlayerPositionThread(clientId, lng, lat));
                    }
                    catch(BufferUnderflowException ue){
                        logger.logError(ue);
                    }
                } catch (IOException e) {
                    logger.logError(e);
                }
            }
        }

        private class SetPlayerPositionThread extends Thread{
            Integer id;
            float longitude;
            float latitude;

            public SetPlayerPositionThread(Integer id, float lng, float lat){
                this.id = id;
                this.longitude = lng;
                this.latitude = lat;
            }

            @Override
            public void run(){
                //make sure there's no collision when accessing
                synchronized (clientMap){
                    //make sure the client is within the map
                    if(clientMap.contains(id)){
                        //update our client coordinates
                        clientMap.get(id).setLocation(longitude, latitude);
                    }
                }
            }
        }
    }

    protected class LocationUpdaterThread extends Thread{
        @Override
        public void run(){
            //create a buffer for keeping client location data
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

            try{
                //keep updating as long as the thread lives
                while(!this.isInterrupted()){
                    //only run loop if the client map is populated
                    if(clientMap.size() > 0){
                        //loop through all clients within the map
                        for (ClientConnectionObject client : clientMap.values()) {
                            //check if the client wants their location to be broadcast
                            if(client.getBroadcastLocation()){
                                //check if client is fully connected before attempting tp fetch data from it
                                if(!(client.getEnjoyerId() == 0 || client.getLongitude() == 0 || client.getLatitude() == 0)){
                                    //write values to buffer
                                    buffer.putLong(client.getEnjoyerId());
                                    buffer.putFloat(client.getLongitude());
                                    buffer.putFloat(client.getLatitude());
                                }
                            }
                        }
                    }

                    //create new udp packet based on the byte buffer array
                    DatagramPacket packet = new DatagramPacket(buffer.array(), buffer.array().length);
                    //set the port of the packet
                    packet.setPort(udpSocket.getPort());

                    if(clientMap.size() > 0){
                        //loop through all connected clients
                        for (ClientConnectionObject client : clientMap.values()){
                            //check if the client sends/receives data about location
                            if(client.getBroadcastLocation()){
                                packet.setAddress(client.getSocket().getInetAddress());

                            }
                        }
                    }

                    //sleep thread to fit our update interval
                    Thread.sleep(LOCATION_UPDATE_INTERVAL);
                }
            }
            catch(InterruptedException ie){
                logger.logError(ie);
            }
        }
    }

    //connection object class for storing session data as well as basic game data,
    //like last location and their currently equipped characterId
    protected class ClientConnectionObject extends Thread{
        private Socket socket;
        private long enjoyerId;
        private int connectionId;
        private long characterId;

        private float longitude;
        private float latitude;

        private boolean broadcastLocation = false;
        private ByteBuffer buffer;

        public ClientConnectionObject(Socket socket, int connectionId) {
            this.socket = socket;
            this.enjoyerId = 0;
            this.connectionId = connectionId;
        }

        public ClientConnectionObject(Socket socket, long enjoyerId, int connectionId) {
            this.socket = socket;
            this.enjoyerId = enjoyerId;
            this.connectionId = connectionId;
        }

        @Override
        public void run(){
            //declare streams
            InputStream inStream = null;
            DataOutputStream outStream = null;
            //BufferedReader reader = null;
            try{
                //initialize streams
                buffer = ByteBuffer.allocate(BUFFER_SIZE);
                inStream = socket.getInputStream();
                outStream = new DataOutputStream(socket.getOutputStream());

                //write connection id to buffer to prepare it for the client
                buffer.putInt(connectionId);
                //send id to client
                outStream.write(buffer.array());
                //clear the buffer
                buffer.clear();

                //start the main accept loop
                while(!this.isInterrupted()){
                    //check if bytes are available
                    if(inStream.available() > 0){
                        //read bytes from stream
                        int b = inStream.read(buffer.array(), 0, inStream.available());
                    }

                    //handle request based on id
                    handleRequest(buffer.get());
                }

            }
            catch(Exception e){}
        }

        private void handleRequest(byte b) {
            /*
            * 1 - change characterId
            * 2 - set location visibility
            * 3 - player request
            * */
            switch(b){
                case 1:
                    long characterId = buffer.getLong();
                    changeUserCharacter(characterId, this);
                    buffer.clear();
                    break;
                case 2:
                    broadcastLocation = buffer.get() == 1 ? true : false;
                    buffer.clear();
                    break;
                case 3:
                    //player requests like messaging, trading, party invites and so forth
                    buffer.clear();
                    break;
                default:
                    //invalid request, ignore
                    buffer.clear();
                    break;
            }
        }

        public boolean getBroadcastLocation() {
            return broadcastLocation;
        }

        public void setBroadcastLocation(boolean broadcastLocation) {
            this.broadcastLocation = broadcastLocation;
        }

        public float getLongitude() {
            return longitude;
        }

        public void setLongitude(float longitude) {
            this.longitude = longitude;
        }

        public float getLatitude() {
            return latitude;
        }

        public void setLatitude(float latitude) {
            this.latitude = latitude;
        }

        public void setLocation(float longitude, float latitude){
            this.longitude = longitude;
            this.latitude = latitude;
        }

        public Socket getSocket() {
            return socket;
        }

        public long getEnjoyerId() {
            return enjoyerId;
        }

        public void setEnjoyerId(int enjoyerId) {
            this.enjoyerId = enjoyerId;
        }

        public long getCharacterId() {
            return characterId;
        }

        public void setCharacterId(long characterId) {
            this.characterId = characterId;
        }

        public int getConnectionId() {
            return connectionId;
        }

        public void setConnectionId(int connectionId) {
            this.connectionId = connectionId;
        }
    }
}
