import kafka.javaapi.producer.Producer;
import kafka.producer.ProducerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;
import kafka.producer.KeyedMessage;

import java.util.Map;
import java.util.Properties;

/**
 * Created by latha on 10/24/2016.
 */
public class ThirdBolt extends BaseRichBolt {
    public static final String TOPIC = "topic";
    public static final String KAFKA_BROKER_PROPERTIES = "kafka.broker.properties";

    public static final String BOLT_KEY = "key";
    public static final String BOLT_MESSAGE = "message";

    private Producer<String, String> producer;
    private OutputCollector collector;
    private String topic;

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        Map configMap = (Map) map.get(KAFKA_BROKER_PROPERTIES);
        Properties properties = new Properties();
        properties.putAll(configMap);
        ProducerConfig config = new ProducerConfig(properties);
        producer = new Producer<String, String>(config);
        this.topic = (String) map.get(TOPIC);
        this.collector = collector;
    }

    @Override
    public void execute(Tuple tuple) {
        String key = null;
        //if (tuple.contains(BOLT_KEY)) {
            key = (String) tuple.getStringByField("framescount");

        String message = (String) tuple.getStringByField("word");
        try {
            producer.send(new KeyedMessage<String, String>(topic, key, message));
        } catch (Exception ex) {
            //LOG.error("Could not send message with key '" + key + "' and value '" + message + "'", ex);
        } finally {
            collector.ack(tuple);
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }
}
