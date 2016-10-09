import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.migcomponents.migbase64.Base64;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.video.Video;
import org.openimaj.video.xuggle.XuggleVideo;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.netlib.arpack.Dsaitr.j;

public class SimpleProducer {
    private static Producer<Integer, String> producer;
    private final Properties properties = new Properties();
    static Video<MBFImage> video;
    static List<MBFImage> imageList = new ArrayList<MBFImage>();
    static List<Long> timeStamp = new ArrayList<Long>();

    public SimpleProducer() {
        properties.put("metadata.broker.list", "localhost:9092");
        properties.put("serializer.class", "kafka.serializer.StringEncoder");
        properties.put("request.required.acks", "1");
        properties.put("message.max.bytes", "10000000");
        producer = new Producer<Integer, String>(new ProducerConfig(properties));
    }


    public static String EncodeVideo(String file){
        String encodedString = null;
        InputStream inputStream = null, inputStream1 = null;
        int j = 0, a = 1;
        byte[] bytes;
        byte[] buffer = new byte[8192];
        int bytesRead;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            //inputStream = new FileInputStream(file);
            video = new XuggleVideo(new File(file));

            for (MBFImage mbfImage : video) {
                BufferedImage bufferedFrame = ImageUtilities.createBufferedImageForDisplay(mbfImage);
                j++;
                String name = "out/frames" + j + ".jpg";
                File outputFile = new File(name);
                try {

                    ImageIO.write(bufferedFrame, "jpg", outputFile);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                MBFImage b = mbfImage.clone();
                imageList.add(b);
                timeStamp.add(video.getTimeStamp());
            }

            for (int k = 0; k < 10; k++) {
                String imgName = "out/" + "frames" + a + ".jpg";
                System.out.println(imgName);
                BufferedImage bufferedFrame = ImageUtilities.createBufferedImageForDisplay(imageList.get(k + 1));
                a++;
                inputStream1 = new FileInputStream(imgName);
                while ((bytesRead = inputStream1.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
            }
            }catch(Exception e){
                // TODO: handle exception
            }
        bytes = output.toByteArray();
        encodedString = Base64.encodeToString(bytes, true);
        return encodedString;
    }

    public static void main(String[] args) {
        new SimpleProducer(); //Setting properties for kafka producer
        String topic = args[0];  //Topic Name
        String msg = EncodeVideo(args[1]); //Encoding the Video
        System.out.println("Message Length" + msg.length());
        Iterable<String> result = Splitter.fixedLength(msg.length()).split(msg); //Splitting the video file
        String[] parts = Iterables.toArray(result, String.class); //Parts of video file
        System.out.println("length" + parts.length);
        System.out.println("========================================================================");
        System.out.println(msg);
        System.out.println("========================================================================");
        //for(int i=0; i<parts.length; i++){
            KeyedMessage<Integer, String> data = new KeyedMessage<Integer, String>(topic, msg);
            System.out.println(msg);
            producer.send(data);
        //}
        System.out.println("Images Sent");
        producer.close();
    }
}


