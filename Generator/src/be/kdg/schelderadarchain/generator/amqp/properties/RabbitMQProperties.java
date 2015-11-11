package be.kdg.schelderadarchain.generator.amqp.properties;

/**
 * Created by Cas on 11/11/2015.
 */
public class RabbitMQProperties extends AMQPProperties {
    private static final String HOST;
    private static final String SENDER_INCIDENT_QUEUE;
    private static final String SENDER_POSITION_MESSAGE_QUEUE;
    private static final String RECEIVER_INCIDENT_ACTION_REPORT_QUEUE;

    static {
        HOST = "rabbitmq.receiver.host";
        SENDER_INCIDENT_QUEUE = "rabbitmq.sender.incident.queue";
        SENDER_POSITION_MESSAGE_QUEUE = "rabbitmq.sender.position.queue";
        RECEIVER_INCIDENT_ACTION_REPORT_QUEUE = "rabbitmq.receiver.report.queue";
    }

    public static String getHost() { return PROPERTIES.getProperty(HOST); }
    public static String getReceiverIncidentQueue() { return PROPERTIES.getProperty(SENDER_INCIDENT_QUEUE); }
    public static String getReceiverPositionQueue() { return PROPERTIES.getProperty(SENDER_POSITION_MESSAGE_QUEUE); }
    public static String getSenderReportQueue() { return PROPERTIES.getProperty(RECEIVER_INCIDENT_ACTION_REPORT_QUEUE); }
}
