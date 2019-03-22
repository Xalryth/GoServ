package Sockets;

import Helpers.Enjoyer;
import Logging.ConsoleLogger;
import Logging.Logger;
import Helpers.Character;
import Repositories.Specifications.CharacterRepository;
import Repositories.Specifications.CharacterSpecifications.CharacterByEnjoyerIdSpecification;
import Repositories.Specifications.EnjoyerRepository;
import Repositories.Specifications.EnjoyerSpecifications.EnjoyerBySigninCredentials;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.Collection;
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
    private EnjoyerRepository enjoyerRepo;
    private CharacterRepository characterRepo;

    public WizardGoServer(int tcpPort, int udpPort, Logger logger) throws IOException {
        try{
            enjoyerRepo = new EnjoyerRepository(new ConsoleLogger());
            characterRepo = new CharacterRepository(new ConsoleLogger());
        }
        catch(SQLException e){
            logger.logError(e);
        }

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
        synchronized (clientMap){
            try{
                Socket socket = clientMap.get(connectionId).getSocket();

                //safely shut down socket connection
                socket.shutdownInput();
                socket.shutdownOutput();
                socket.close();
            }
            catch(IOException e){
                logger.logError(e);
            }
            clientMap.remove(connectionId);
        }
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
        logger.logMessage("Server started");

        //start udp thread for receiving and updating position data
        UdpReceiverThread udpThread = new UdpReceiverThread();
        udpThread.start();

        while(!this.isInterrupted()){
            try {
                logger.logMessage("Ready for new connection (" + clientMap.size() + "/" + MAX_CLIENTS + ")");
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

    private long signInClient(String email, String password){
        Enjoyer enjoyer = enjoyerRepo.query(new EnjoyerBySigninCredentials(email, password)).iterator().next();

        if(enjoyer != null){
            return enjoyer.getId();
        }

        return 0;
    }

    private void changeUserCharacter(long id, ClientConnectionObject client){
        //check if user owns the characterId
        Collection<Character> characters = characterRepo.query(new CharacterByEnjoyerIdSpecification(id, client.enjoyerId));
        if(!characters.isEmpty()){
            Character character = characters.iterator().next();
            client.setCharacterId(character.getId());

            ByteBuffer buffer = ByteBuffer.allocate(9);
            //50 = id for character change
            buffer.put((byte)50);
            buffer.putLong(client.getEnjoyerId());
            buffer.putLong(character.getId());

            buffer.flip();
            DatagramPacket packet = new DatagramPacket(buffer.array(), buffer.remaining());
            packet.setPort(udpSocket.getLocalPort());

            //send packet twice for a better chance of the message arriving successfully
            //since the message is quite important (from 75% chance of packet arriving to ~94%)
            sendBroadCast(packet);
            sendBroadCast(packet);
        }
    }

    //broadcasts udp packet to all clients who have opted in to receive player updates
    private void sendBroadCast(DatagramPacket packet) {
        //set the port of the packet
        packet.setPort(udpSocket.getLocalPort());

        synchronized (clientMap){
            if(clientMap.size() > 0){
                //loop through all connected clients
                for (ClientConnectionObject client : clientMap.values()){
                    //check if the client sends/receives data about location
                    if(client.getBroadcastLocation()){
                        packet.setAddress(client.getSocket().getInetAddress());

                    }
                }
            }
        }
    }

    //position updater thread for updating location data between clients
    protected class UdpReceiverThread extends Thread {
        //create executor for handling simple user requests without having to allocate new threads at runtime
        ExecutorService executor = Executors.newFixedThreadPool(3);

        @Override
        public void run(){
            //declare receive buffer
            ByteBuffer buffer;

            //start a thread meant for location updates based on input location data
            LocationUpdaterThread locationUpdaterThread = new LocationUpdaterThread();
            locationUpdaterThread.start();

            //udp receiver loop
            while(!this.isInterrupted()){
                buffer = ByteBuffer.allocate(BUFFER_SIZE);
                //set data packet as empty
                DatagramPacket newPacket = new DatagramPacket(buffer.array(), buffer.remaining());

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

    //thread to update location data for a client
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

                    //flip buffer to set limit and position
                    buffer.flip();

                    //create new udp packet based on the byte buffer array
                    sendBroadCast(new DatagramPacket(buffer.array(), buffer.remaining()));

                    //reset buffer
                    buffer.clear();
                    buffer.put(new byte[1024]);
                    buffer.clear();

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
            try{
                //initialize streams
                buffer = ByteBuffer.allocate(BUFFER_SIZE);
                inStream = socket.getInputStream();
                outStream = new DataOutputStream(socket.getOutputStream());

                //127 is id for connection id changes
                buffer.put((byte)127);
                //write connection id to buffer to prepare it for the client
                buffer.putInt(connectionId);
                //flip buffer to set limit and position
                buffer.flip();
                //send id to client
                outStream.write(buffer.array());
                //clear the buffer
                buffer.clear();
                buffer.put(new byte[1024]);
                buffer.clear();

                //start the main accept loop
                while(!this.isInterrupted() && socket.isConnected()){
                    //check if bytes are available
                    if(inStream.available() > 0){
                        //read bytes from stream
                        int b = inStream.read(buffer.array(), 0, inStream.available());
                        //handle request based on id
                        handleRequest(buffer.get());
                        //allocate new buffer
                        buffer.clear();
                        buffer.put(new byte[1024]);
                        buffer.clear();
                    }

                }

                closeClient(connectionId);

            }
            catch(Exception e){
                logger.logError(e);
            }
        }

        private void handleRequest(byte b) {
            /*
            * 1 - change character
            * 2 - set location visibility
            * 3 - player request
            * 4 - sign in request
            * */
            switch(b){
                case 1:
                    long characterId = buffer.getLong();
                    changeUserCharacter(characterId, this);
                    break;
                case 2:
                    broadcastLocation = buffer.get() == 1 ? true : false;
                    break;
                case 3:
                    //player requests like messaging, trading, party invites and so forth
                    break;
                case 4:
                    //fetch email string from byte buffer
                    int length = buffer.getInt();
                    byte[] email = new byte[length];
                    for (int i = 0; i < length; i++){
                        email[i] = buffer.get();
                    }

                    //fetch password string from byte buffer
                    length = buffer.getInt();
                    byte[] password = new byte[length];
                    for (int i = 0; i < length; i++){
                        password[i] = buffer.get();
                    }

                    enjoyerId = signInClient(new String(email), new String(password));
                    break;
                default:
                    //invalid request, ignore
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

    public void handleCommand(String cmd, Object param){
        switch(cmd){
            case "shutdown":
                break;
            case "close":
                try{

                }
                catch(Exception e){
                    logger.logError(e);
                }
                break;
            default:
                logger.logMessage("Invalid command");
                break;
        }
    }
}
