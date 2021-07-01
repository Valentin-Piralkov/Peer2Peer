import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {
    private String username;
    private ArrayList<DatagramSocket> sockets;
    private ArrayList<String> names;
    public Client() {
        this.username = this.login();
        this.sockets = new ArrayList<>();
        this.names = new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }

    private class clientInThread implements Runnable{
        Scanner userInput = null;
        @Override
        public void run() {
            try {
                //read user's input from the terminal
                this.userInput = new Scanner(System.in);
                //add your first peer
                addNewReceiver();
                while (true) {
                    //red the message
                    String userInputString = userInput.nextLine();
                    if(userInputString.equals(".")){
                        String s = "%exit%";
                        byte[] a = s.getBytes(StandardCharsets.UTF_8);
                        for(DatagramSocket ds : sockets){
                            DatagramPacket packet = new DatagramPacket(a, a.length,
                                    ds.getInetAddress(), ds.getPort());
                            //send the packet to the receiver
                            sockets.forEach(sockets -> {
                                try {
                                    sockets.send(packet);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                        exit();
                    }
                    if(userInput.equals("look")){
                        look();
                        continue;
                    }
                    Scanner scanner = new Scanner(userInputString);
                    int index = 0;
                    try{
                       index = scanner.nextInt();
                    }catch (Exception e){
                        continue;
                    }
                    userInputString = username + userInputString;
                    //create a byte array to store the message
                    byte[] arr = userInputString.getBytes(StandardCharsets.UTF_8);
                    if(sockets.size() - 1 >= index){ //check if there exist a peer at this index
                        //create a packet to send
                        DatagramPacket packet = new DatagramPacket(arr, arr.length,
                                sockets.get(index).getInetAddress(), sockets.get(index).getPort());
                        //send the packet to the receiver
                        sockets.get(index).send(packet);
                    }
                    else {
                        addNewReceiver();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        private synchronized void addNewReceiver(){ //creates a new peer socket and appends it  to the list
            System.out.println("Enter port number:");
            int p = userInput.nextInt();
            System.out.println("Enter name:");
            String s = userInput.next();
            InetAddress ip = null;
            try {
                ip = InetAddress.getLocalHost();
                DatagramSocket upd = new DatagramSocket();
                upd.connect(ip, p);
                sockets.add(upd);
                names.add(s);
                System.out.println(s + " is at index: " + sockets.indexOf(upd));
            } catch (UnknownHostException | SocketException e) {
                e.printStackTrace();
            }
        }
    }
    //thread to read messages from server
    private class clientOutThread implements Runnable{
        private DatagramSocket datagramSocket;
        public clientOutThread(int port) {
            try {
                InetAddress ip = InetAddress.getLocalHost();
                SocketAddress sa = new InetSocketAddress(ip, port);
                this.datagramSocket = new DatagramSocket(sa);
            } catch (SocketException | UnknownHostException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            byte[] arr = new byte[1024];
            DatagramPacket packet = new DatagramPacket(arr, arr.length);
            while (true){
                try {
                    datagramSocket.receive(packet);
                    String message = new String(packet.getData(), 0, packet.getLength());
                    if(message.contains("%exit%")){
                        removeDSocket(packet.getPort());
                    }
                    System.out.println(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private synchronized void removeDSocket(int port){
        for(int i = 0; i < sockets.size(); i++){
            if(sockets.get(i).getPort() == port){
                sockets.remove(i);
                names.remove(i);
            }
        }
    }
    private synchronized void look(){
        for(int i = 0; i < names.size(); i++){
            System.out.println(i + ": " + names.get(i));
        }
    }
    public void go(int port) {
        //start threads to communicate with the server
        Thread clientInthread = new Thread(new clientInThread());
        Thread clientOutThread = new Thread(new clientOutThread(port));
        clientInthread.start();
        clientOutThread.start();
    }

    private synchronized String login(){
        String username = "[";
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter username");
        username += sc.next();
        username += "]: ";
        return username;
    }
    public static void main(String[] args) throws UnknownHostException {
        Client client2 = new Client();
        client2.go(1234);
        Client client1 = new Client();
        client1.go(12345);
    }
    private void exit(){
        System.exit(0);
    }
}
