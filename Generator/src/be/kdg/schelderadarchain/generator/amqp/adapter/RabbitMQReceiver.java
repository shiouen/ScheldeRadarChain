package be.kdg.schelderadarchain.generator.amqp.adapter;

import be.kdg.schelderadarchain.generator.amqp.exceptions.AMQPException;
import be.kdg.schelderadarchain.generator.backend.dom.ActionReport;
import be.kdg.schelderadarchain.generator.backend.generator.ActionReportListener;
import be.kdg.schelderadarchain.generator.backend.utility.XmlConverter;
import com.rabbitmq.client.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * This class acts as an adapter between AMQP-based RabbitMQ functionality and
 * the AMQPCommunicator abstract interface class, separating both codebases.
 *
 * This class also acts as a strategy to receive AMQP messages based on RabbitMQs implementation.
 *
 * @author Cas Decelle
 */

public class RabbitMQReceiver implements AMQPCommunicator {

    private String host;
    private String queue;

    private final Channel channel;
    private final Connection connection;
    private final Logger logger = Logger.getLogger(this.getClass());

    private ActionReportListener actionReportListener;

    public RabbitMQReceiver(String host, String queue, ActionReportListener actionReportListener) {
        this.host = host;
        this.queue = queue;
        this.actionReportListener = actionReportListener;

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(this.host);

        try {
            this.connection = factory.newConnection();
        } catch (IOException | TimeoutException e) {
            String message = "Error trying to setup the connection with RabbitMQ";
            this.logger.error(message);
            throw new AMQPException(message, e);
        }

        try {
            this.channel = this.connection.createChannel();
        } catch (IOException e) {
            String message = "Error trying to setup the channel with RabbitMQ";
            this.logger.error(message);
            throw new AMQPException(message, e);
        }
    }

    @Override
    public void close() throws AMQPException {
        try {
            this.channel.close();
        } catch (IOException e) {
            String message = "Error trying to close the channel with RabbitMQ";
            this.logger.error(message);
            throw new AMQPException(message, e);
        } catch (TimeoutException e) {
            String message = "Timeout trying to close the channel with RabbitMQ";
            this.logger.error(message);
            throw new AMQPException(message, e);
        }
        try {
            this.connection.close();
        } catch (IOException e) {
            String message = "Error trying to close the connection with RabbitMQ";
            this.logger.error(message);
            throw new AMQPException(message, e);
        }
    }

    @Override
    public void open() throws AMQPException {
        try {
            // make sure the queue exists
            this.channel.queueDeclare(this.queue, false, false, false, null);
        } catch (IOException e) {
            String message = "Error trying to declare the queue with RabbitMQ";
            this.logger.error(message);
            throw new AMQPException(message, e);
        }

        // will buffer the asynchronously delivered calls until we're ready to use them
        Consumer consumer = new DefaultConsumer(this.channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                ActionReport actionReport = (ActionReport)XmlConverter.fromXml(new String(body, "UTF-8"));
                actionReportListener.onActionReportReceived(actionReport);
            }
        };

        try {
            this.channel.basicConsume(this.queue, true, consumer);
        } catch (IOException e) {
            String message = "Error trying to poll a message from the queue";
            this.logger.error(message);
            throw new AMQPException(message, e);
        }

    }
}
