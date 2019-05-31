import java.io.IOException;
import java.net.*;
import java.util.Map;
import java.util.TreeMap;

public class Z2Receiver
{
    static final int datagramSize=50;
    InetAddress localHost;
    int destinationPort;
    DatagramSocket socket;
    ReceiverThread receiver;
    TreeMap<Integer,Character> received;

    public Z2Receiver(int myPort, int destPort)
            throws Exception
    {
        localHost=InetAddress.getByName("127.0.0.1");
        destinationPort=destPort;
        socket=new DatagramSocket(myPort);
        received = new TreeMap<>();
        receiver=new ReceiverThread();
    }

    class ReceiverThread extends Thread
    {

        public void run()
        {
            int current = 0;
            int code;
            char character;
            try
            {
                while(true)
                {
                    byte[] data=new byte[datagramSize];
                    DatagramPacket packet=
                            new DatagramPacket(data, datagramSize);
                    socket.receive(packet);
                    Z2Packet p = new Z2Packet(packet.getData());
                    code = p.getIntAt(0);
                    character = (char) p.data[4];

                    if(code == current){
                        current++;
                        System.out.println("R:"+code +": "+character);

                        while (!received.isEmpty() && received.firstKey() == current) {
                            Map.Entry<Integer, Character> entry = received.pollFirstEntry();
                            System.out.println("R:" + entry.getKey() + ": " + entry.getValue());
                            current++;
                        }


                    }else if (code > current){
                        received.put(code,character);
                    }else{
                        continue;
                    }

                    packet.setPort(destinationPort);
                    socket.send(packet);
                }
            }
            catch(IOException e)
            {
                System.out.println("Z2Receiver.ReceiverThread.run: "+e);
            }
        }

    }

    public static void main(String[] args)
            throws Exception
    {
        Z2Receiver receiver=new Z2Receiver( Integer.parseInt(args[0]),
                Integer.parseInt(args[1]));
        receiver.receiver.start();
    }


}