package util.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

public class KafkaSubscriber {
	static public KafkaSubscriber createSubscriber(String brokers, List<String> topics, String mode) {

		Properties props = new Properties();

		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);

		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, mode);

		props.put(ConsumerConfig.GROUP_ID_CONFIG, "grp" + System.nanoTime());

		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

		return new KafkaSubscriber(new KafkaConsumer<>(props), topics);
	}

	private static final long POLL_TIMEOUT = 1L;

	final KafkaConsumer<String, String> consumer;

	public KafkaSubscriber(KafkaConsumer<String, String> consumer, List<String> topics) {
		this.consumer = consumer;
		this.consumer.subscribe(topics);
	}

	public void start(boolean block, RecordProcessor processor) {
		if (block) {
			consume(processor);
		} else {
			new Thread(() -> consume(processor)).start();
		}
	}

	private void consume(RecordProcessor processor) {
		for (; ; ) {
			consumer.poll(Duration.ofSeconds(POLL_TIMEOUT)).forEach(processor::onReceive);
		}
	}
}
