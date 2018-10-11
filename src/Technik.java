

import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Technik {
    private List<Domain.ExaminationType> examinationTypes = new ArrayList<>();
    private Connection connection;
    private Channel responseQueue;

    public static void main(String[] argv) throws Exception {
        new Technik().setUpChannels();
    }

    public Technik() throws Exception {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Specjalizacja 1:");
        examinationTypes.add(Domain.ExaminationType.valueOf(br.readLine().toUpperCase()));
        System.out.println("Specjalizacja 2:");
        examinationTypes.add(Domain.ExaminationType.valueOf(br.readLine().toUpperCase()));
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        this.connection = factory.newConnection();
        //TODO
        Admin.plug(connection);
        responseQueue = connection.createChannel();
        responseQueue.queueDeclare();
        responseQueue.exchangeDeclare(Domain.Exchange.Name, BuiltinExchangeType.TOPIC);

    }

    public void setUpChannels() throws Exception {
        for (Domain.ExaminationType knownExamination : examinationTypes) {
            handle(knownExamination);
        }
    }

    private void handle(Domain.ExaminationType examinationType) throws Exception {
        Channel receiveChannel = connection.createChannel();
        String queueName = receiveChannel.queueDeclare(Domain.TopicNames.createRequestTopic(examinationType, "*", "*"), false, false, false, null).getQueue();
        System.out.println(queueName);
        System.out.println("///////////");
        receiveChannel.exchangeDeclare(Domain.Exchange.Name, BuiltinExchangeType.TOPIC);

        receiveChannel.queueBind(queueName, Domain.Exchange.Name, queueName);
        System.out.println("created queue: " + queueName + " na " + queueName);

        Consumer consumer = new DefaultConsumer(receiveChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println("///////////");
                System.out.println(envelope.getRoutingKey());
                System.out.println("///////////");
                String message = new String(body, "UTF-8");
                System.out.println("Dostałem wiadomość: " + message);
                int i1 = message.indexOf(":");
                String m1 = message.substring(i1 +2 );
                String t ="";
                if(message.contains("KNEE")) t = "KNEE";
                else if (message.contains("ELBOW")) t= "ELBOW";
                else if(message.contains("HIP")) t = "HIP";
                message = "type : " + t + " pacjent :" + m1;
                System.out.println("Tutaj......." + envelope.getRoutingKey());
                responseQueue.basicPublish(Domain.Exchange.Name, Domain.TopicNames.doctorExaminationResponse(envelope.getRoutingKey()), null, ("WYBADAŁEM: " + message).getBytes("UTF-8"));
            }
        };

        System.out.println("Waiting for messages...");
        receiveChannel.basicConsume(queueName, true, consumer);
    }

}
