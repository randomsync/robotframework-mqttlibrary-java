package net.randomsync.robotframework.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MQTTLibrary {

    private MqttClient client;

    public static final String ROBOT_LIBRARY_SCOPE = "GLOBAL";

    public static final String ROBOT_LIBRARY_VERSION = "1.0.0";

    public static final String ROBOT_LIBRARY_DOC_FORMAT = "HTML";

    public void connectToMQTTBroker(String broker, String clientId)
            throws MqttException {
        client = new MqttClient(broker, clientId);
        System.out.println("*INFO:" + System.currentTimeMillis()
                + "* connecting to broker");
        client.connect();
        System.out.println("*INFO:" + System.currentTimeMillis()
                + "* connected");

    }

    public void publishToMQTTSynchronously(String topic, String message)
            throws MqttException {
        publishToMQTTSynchronously(topic, message, 0, false);
    }

    public void publishToMQTTSynchronously(String topic, String message,
            int qos, boolean retained) throws MqttException {
        System.out.println("*INFO:" + System.currentTimeMillis()
                + "* publishing message");
        MqttMessage msg = new MqttMessage(message.getBytes());
        msg.setQos(qos);
        msg.setRetained(retained);
        client.publish(topic, new MqttMessage(message.getBytes()));
        System.out.println("*INFO:" + System.currentTimeMillis()
                + "* published");
    }

    public void subscribeToMQTTandValidate(String topic, String message,
            int qos, boolean retained) {
        // not implemented yet
        throw new RuntimeException("Not Implemented Yet");
    }

    public void disconnectFromMQTTBroker() {
        if (client != null) {
            try {
                client.disconnect();
            } catch (MqttException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
