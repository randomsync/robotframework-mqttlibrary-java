package net.randomsync.robotframework.mqtt.keywords;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.robotframework.javalib.annotation.RobotKeyword;
import org.robotframework.javalib.annotation.RobotKeywordOverload;
import org.robotframework.javalib.annotation.RobotKeywords;

@RobotKeywords
public class Publish {

    private MqttClient client;

    @RobotKeyword
    public void connectToMQTTBroker(String broker, String clientId)
	    throws MqttException {
	client = new MqttClient(broker, clientId);
	System.out.println("*INFO:" + System.currentTimeMillis()
		+ "* connecting to broker");
	client.connect();
	System.out.println("*INFO:" + System.currentTimeMillis()
		+ "* connected");

    }

    @RobotKeyword
    public void publishToMQTTSynchronously(String topic, String message)
	    throws MqttException {
	publishToMQTTSynchronously(topic, message, 0, false);
    }

    @RobotKeywordOverload
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

    @RobotKeyword
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
