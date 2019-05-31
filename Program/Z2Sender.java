import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class Z2Sender
{
    static final int datagramSize=50;
    static final int sleepTime=500;
    static final int maxPacket=50;
    InetAddress localHost;
    int destinationPort;
    DatagramSocket socket;
    SenderThread sender;
    ReceiverThread receiver;
    ResenderThread resender;
    Map<Integer, DatagramPacket> sent;


    public Z2Sender(int myPort, int destPort)
            throws Exception
    {
        localHost=InetAddress.getByName("127.0.0.1");
        destinationPort=destPort;
        socket=new DatagramSocket(myPort);
        sent = Collections.synchronizedMap( new HashMap<>());
        sender=new SenderThread();
        receiver=new ReceiverThread();
        resender = new ResenderThread();
    }

    class SenderThread extends Thread
    {
        public void run()
        {
            int i, x;
            try
            {
                for(i=0; (x=System.in.read()) >= 0 ; i++)
                {
                    Z2Packet p=new Z2Packet(4+1);
                    p.setIntAt(i,0);
                    p.data[4]= (byte) x;
                    DatagramPacket packet =
                            new DatagramPacket(p.data, p.data.length,
                                    localHost, destinationPort);
                    sent.put(i,packet);
                    socket.send(packet);
                    sleep(sleepTime);
                }
            }
            catch(Exception e)
            {
                System.out.println("Z2Sender.SenderThread.run: "+e);
            }
        }

    }



    class ReceiverThread extends Thread
    {

        public void run()
        {
            try
            {
                while(true)
                {
                    byte[] data=new byte[datagramSize];
                    DatagramPacket packet=
                            new DatagramPacket(data, datagramSize);
                    socket.receive(packet);
                    Z2Packet p=new Z2Packet(packet.getData());
                    sent.remove(p.getIntAt(0));
                    System.out.println("S:"+p.getIntAt(0)+
                            ": "+(char) p.data[4]);
                }
            }
            catch(Exception e)
            {
                System.out.println("Z2Sender.ReceiverThread.run: "+e);
            }
        }

    }


    class ResenderThread extends Thread
    {
        public void run()
        {
            HashMap<Integer, DatagramPacket> copy = new HashMap();
            try
            {
                while(true) {
                    if(sent.size() != 0) {
                        if(copy.size() != 0) {
                            for (HashMap.Entry<Integer, DatagramPacket> entry : copy.entrySet()) {
                                int key = entry.getKey();
                                DatagramPacket packet = sent.get(key);
                                if (packet != null) {
                                    socket.send(packet);
                                }
                            }
                        }

                        copy.clear();
                        synchronized (sent) {
                            for (HashMap.Entry<Integer, DatagramPacket> entry : sent.entrySet()) {
                                copy.put(entry.getKey(),entry.getValue());
                            }
                        }
                    }
                    sleep(sleepTime*6);
                }
            }
            catch(Exception e)
            {
                System.out.println("Z2Sender.ReceiverThread.run: "+e);
            }
        }

    }


    public static void main(String[] args)
            throws Exception
    {
        Z2Sender sender=new Z2Sender( Integer.parseInt(args[0]),
                Integer.parseInt(args[1]));
        sender.sender.start();
        sender.receiver.start();
        sender.resender.start();
    }



}