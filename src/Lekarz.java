

import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Lekarz {

    private Channel sendingQueue;
    private Channel receivingQueue;
    private final String doctorName;


    public Lekarz() throws Exception {
        System.out.println("Nazwisko lekarza:");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        this.doctorName = br.readLine();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        //TODO
        Admin.plug(connection);
        sendingQueue = connection.createChannel();
        sendingQueue.queueDeclare();
        sendingQueue.exchangeDeclare(Domain.Exchange.Name, BuiltinExchangeType.TOPIC);


        receivingQueue = connection.createChannel();
        String qName = receivingQueue.queueDeclare(Domain.TopicNames.doctorAnyResponse(doctorName), false, false, false, null).getQueue();
        receivingQueue.exchangeDeclare(Domain.Exchange.Name, BuiltinExchangeType.TOPIC);

        receivingQueue.queueBind(qName, Domain.Exchange.Name, qName);
        System.out.println("created queue: " + qName + " na " + qName);

        Consumer consumer = new DefaultConsumer(receivingQueue) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received: " + message);
            }
        };

        System.out.println("Waiting for messages...");
        receivingQueue.basicConsume(qName, true, consumer);
    }

    public static void main(String[] args) throws Exception {
        new Lekarz().sendRequest();
    }

    private void sendRequest() throws Exception {

        while (true) {

            // read msg
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter examination type");
            Domain.ExaminationType examinationType = Domain.ExaminationType.valueOf(br.readLine().toUpperCase());
            System.out.println("Enter name");
            String pacjent = br.readLine();

            String message = "Tutaj " + doctorName + ". Wybadaj " + examinationType + "pacjent : " + pacjent;
            sendingQueue.basicPublish(Domain.Exchange.Name, Domain.TopicNames.createRequestTopic(examinationType, doctorName, pacjent), null, (message).getBytes("UTF-8"));
            System.out.println("Sent: " + message);
        }
    }
}
