import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by latha on 10/24/2016.
 */
public class SecondBolt extends BaseRichBolt {
    Map<String, Integer> wordsCount = new HashMap<String, Integer>();
    OutputCollector Collector;
    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        Collector = outputCollector;
    }

    @Override
    public void execute(Tuple tuple) {
        String message = tuple.getString(0);
        Integer wordCount = wordsCount.get(message);
        if (wordCount == null){
            wordCount = 0;
        }
        wordCount++;
        wordsCount.put(message,wordCount);
        Collector.emit(new Values(message, wordCount));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("word", "count"));
    }
}
