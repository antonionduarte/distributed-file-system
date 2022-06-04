package util.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class KafkaPublisher {

	static public KafkaPublisher createPublisher(String brokers) {
		Properties props = new Properties();

		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);

		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

		return new KafkaPublisher(new KafkaProducer<String, String>(props));
	}

	private final KafkaProducer<String, String> producer;

	private KafkaPublisher(KafkaProducer<String, String> producer) {
		this.producer = producer;
	}

	public void close() {
		this.producer.close();
	}

	public long publish(String topic, String key, String value) {
		try {
			return producer.send(new ProducerRecord<>(topic, key, value)).get().offset();
		} catch (ExecutionException | InterruptedException x) {
			x.printStackTrace();
		}
		return -1;
	}

	public long publish(String topic, String value) {
		try {
			return producer.send(new ProducerRecord<>(topic, value)).get().offset();
		} catch (ExecutionException | InterruptedException x) {
			x.printStackTrace();
		}
		return -1;
	}


	public static void main(String[] args) throws Exception {


	}
}
