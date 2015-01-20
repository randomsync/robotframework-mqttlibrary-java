package net.randomsync.robotframework.mqtt;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.robotframework.javalib.annotation.ArgumentNames;
import org.robotframework.javalib.annotation.RobotKeyword;
import org.robotframework.javalib.annotation.RobotKeywordOverload;
import org.robotframework.javalib.annotation.RobotKeywords;
import org.robotframework.javalib.library.AnnotationLibrary;

/**
 * A keyword library for Robot Framework. It provides keywords for performing
 * various operations on an MQTT broker. See <a href="http://mqtt.org/">MQTT</a>
 * for more details on MQTT specification.
 *
 */
@RobotKeywords
public class MQTTLibrary extends AnnotationLibrary {

    public static final String KEYWORD_PATTERN = "net/randomsync/robotframework/mqtt/**/*.class";

    public static final String ROBOT_LIBRARY_SCOPE = "GLOBAL";

    public static final String ROBOT_LIBRARY_VERSION = "1.0.0";

    public static final String ROBOT_LIBRARY_DOC_FORMAT = "HTML";

    private MqttClient client;

    public MQTTLibrary() {
        super(KEYWORD_PATTERN);
    }

    /**
     * Connect to an MQTT broker.
     * 
     * @param broker
     *            Uri of the broker to connect to
     * @param clientId
     *            Client Id
     * @throws MqttException
     *             if there is an issue connecting to the broker
     */
    @RobotKeyword("Connect to MQTT Broker")
    @ArgumentNames({ "broker", "clientId" })
    public void connectToMQTTBroker(String broker, String clientId)
            throws MqttException {
        client = new MqttClient(broker, clientId);
        System.out.println("*INFO:" + System.currentTimeMillis()
                + "* connecting to broker");
        client.connect();
        System.out.println("*INFO:" + System.currentTimeMillis()
                + "* connected");
    }

    /**
     * Publish a message to a topic
     * 
     * @param topic
     *            topic to which the message will be published
     * @param message
     *            message payload to publish
     * @throws MqttException
     *             if there is an issue publishing to the broker
     */
    @RobotKeywordOverload
    @ArgumentNames({ "topic", "message" })
    public void publishToMQTTSynchronously(String topic, Object message)
            throws MqttException {
        publishToMQTTSynchronously(topic, message, 0, false);
    }

    /**
     * Publish a message to a topic with specified qos and retained flag
     * 
     * @param topic
     *            topic to which the message will be published
     * @param message
     *            message payload to publish
     * @param qos
     *            qos of the message
     * @param retained
     *            retained flag
     * @throws MqttException
     *             if there is an issue publishing to the broker
     */
    @RobotKeyword("Publish to MQTT Synchronously")
    @ArgumentNames({ "topic", "message", "qos=0", "retained=false" })
    public void publishToMQTTSynchronously(String topic, Object message,
            int qos, boolean retained) throws MqttException {
        MqttMessage msg;
        if (message instanceof String) {
            msg = new MqttMessage(message.toString().getBytes());
        } else {
            msg = new MqttMessage((byte[]) message);
        }
        msg.setQos(qos);
        msg.setRetained(retained);
        System.out.println("*INFO:" + System.currentTimeMillis()
                + "* publishing message");
        client.publish(topic, msg);
        System.out.println("*INFO:" + System.currentTimeMillis()
                + "* published");
    }

    /**
     * Disconnect from MQTT Broker
     */
    @RobotKeyword("Disconnect from MQTT Broker")
    public void disconnectFromMQTTBroker() {
        if (client != null) {
            try {
                client.disconnect();
            } catch (MqttException e) {
                throw new RuntimeException(e.getLocalizedMessage());
            }
        }
    }

    /**
     * Subscribe to an MQTT broker and validate that a message with specified
     * payload is received
     * 
     * @param broker
     *            Uri of broker to subscribe to
     * @param clientId
     *            Client Id
     * @param topic
     *            topic to subscribe to
     * @param expectedPayload
     *            payload to validate
     * @param timeout
     *            timeout for the payload to be received
     */
    @RobotKeyword("Subscribe to MQTT Broker and validate that it received a specific message")
    @ArgumentNames({ "broker", "clientId", "topic", "expectedPayload",
            "timeout" })
    public void subscribeToMQTTAndValidate(String broker, String clientId,
            String topic, String expectedPayload, long timeout) {
        MqttClient client = null;
        try {
            MqttClientPersistence persistence = new MemoryPersistence();
            client = new MqttClient(broker, clientId, persistence);

            // set clean session to false so the state is remembered across
            // sessions
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(false);

            // set callback before connecting so prior messages are delivered as
            // soon as we connect
            MQTTResponseHandler handler = new MQTTResponseHandler();
            client.setCallback(handler);

            System.out.println("*INFO:" + System.currentTimeMillis()
                    + "* Connecting to broker: " + broker);
            client.connect(connOpts);

            System.out.println("*INFO:" + System.currentTimeMillis()
                    + "* Subscribing to topic: " + topic);
            client.subscribe(topic);
            System.out.println("*INFO:" + System.currentTimeMillis()
                    + "* Subscribed to topic: " + topic);

            // now loop until either we receive the message in the topic or
            // timeout
            System.out.println("*INFO:" + System.currentTimeMillis()
                    + "* Waiting for message to arrive");
            boolean validated = false;
            byte[] payload;
            MqttMessage message;
            long endTime = System.currentTimeMillis() + timeout;
            while (true) {
                /*
                 * If expected payload is empty, all we need to validate is
                 * receiving the message in the topic. If expected payload is
                 * not empty, then we need to validate that it is contained in
                 * the actual payload
                 */
                message = handler.getNextMessage(timeout);
                if (message != null) { // received a message in the topic
                    payload = message.getPayload();
                    String payloadStr = new String(payload);
                    if (expectedPayload.isEmpty()
                            || (payloadStr.matches(expectedPayload))) {
                        validated = true;
                        break;
                    }
                }
                // update timeout to remaining time and check
                if ((timeout = endTime - System.currentTimeMillis()) <= 0) {
                    System.out.println("*DEBUG:" + System.currentTimeMillis()
                            + "* timeout: " + timeout);
                    break;
                }
            }
            if (!validated) {
                throw new RuntimeException(
                        "The expected payload didn't arrive in the topic");
            }
        } catch (MqttException e) {
            throw new RuntimeException(e.getLocalizedMessage());
        } finally {
            try {
                client.disconnect();
            } catch (MqttException e) {
                // empty
            }
        }
    }

    class MQTTResponseHandler implements MqttCallback {

        List<MqttMessage> messages = new ArrayList<MqttMessage>();

        public MqttMessage getNextMessage(long timeout) {
            synchronized (messages) {
                if (messages.size() == 0) {
                    try {
                        messages.wait(timeout);
                    } catch (InterruptedException e) {
                        System.out.println("*ERROR:"
                                + System.currentTimeMillis() + "* "
                                + e.getLocalizedMessage());
                    }
                }
            }
            if (messages.size() == 0) {
                return null;
            }
            return messages.remove(0);
        }

        @Override
        public void connectionLost(Throwable cause) {
        }

        @Override
        public void messageArrived(String topic, MqttMessage message)
                throws Exception {
            System.out.println("*INFO:" + System.currentTimeMillis()
                    + "* Message arrived");
            synchronized (messages) {
                messages.add(message);
                messages.notifyAll();
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
        }
    }

    @Override
    public String getKeywordDocumentation(String keywordName) {
        if (keywordName.equals("__intro__"))
            return "Keyword Library for MQTT";
        if (keywordName.equals("__init__"))
            return "MQTT Library can be imported directly";
        return super.getKeywordDocumentation(keywordName);
    }
}
