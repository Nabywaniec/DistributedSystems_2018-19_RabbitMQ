import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

public class Admin {
    private Channel sniffingChannel;
    private Channel infoChannel;
    String sniffingQueueName;

    public static void main(String[] args) throws Exception {
        new Admin().sendToAll();
    }

    public Admin() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();

        sniffingChannel = connection.createChannel();
        this.sniffingQueueName = sniffingChannel.queueDeclare().getQueue();
        sniffingChannel.exchangeDeclare(Domain.Exchange.Name, BuiltinExchangeType.TOPIC);

        sniffingChannel.queueBind(sniffingQueueName, Domain.Exchange.Name, "#");
        Logger.getGlobal().info("Created sniffing channel");
        infoChannel = connection.createChannel();
        infoChannel.queueDeclare();
        infoChannel.exchangeDeclare(Domain.Exchange.Name, BuiltinExchangeType.TOPIC);
        sendInfo("ADMIN WCHODZI");
        startListening();
    }

    private void sendToAll() throws Exception {
        while (true) {

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter message: ");
            String message = br.readLine();


            if ("exit".equals(message)) {
                break;
            }
            sendInfo(message);
        }
    }

    public void sendInfo(String msg) throws Exception {
        infoChannel.basicPublish(Domain.Exchange.Name, Domain.TopicNames.Info, null, msg.getBytes("UTF-8"));

    }

    public void startListening() throws Exception {
        Consumer consumer = new DefaultConsumer(sniffingChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("wiadomość z klucza " + envelope.getRoutingKey() + ": " + message);
            }
        };

        System.out.println("Waiting for messages...");
        sniffingChannel.basicConsume(sniffingQueueName, true, consumer);
    }


    public static void plug(Connection connection) throws Exception {

        Channel channel = connection.createChannel();
        String channelName = channel.queueDeclare().getQueue();
        channel.exchangeDeclare(Domain.Exchange.Name, BuiltinExchangeType.TOPIC);

        channel.queueBind(channelName, Domain.Exchange.Name, Domain.TopicNames.Info);
        System.out.println("created queue: " + channelName);


        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("INFO OD ADMINA: " + message);
            }
        };

        System.out.println("Waiting for messages...");
        channel.basicConsume(channelName, true, consumer);
    }
}
