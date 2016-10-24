import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.Map;

/**
 * Created by latha on 10/24/2016.
 */
public class FirstBolt extends BaseRichBolt{

    OutputCollector collector;
    String message;
    String frames[];
    int framesCount;

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        collector = outputCollector;
        framesCount = 0;
    }

    @Override
    public void execute(Tuple tuple) {
        message = tuple.getString(0);
        frames = message.split(",");
        for (int i = 0; i < frames.length; i++){
            framesCount++;
        }
        collector.emit(new Values(framesCount));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("framesCount"));
    }
}
