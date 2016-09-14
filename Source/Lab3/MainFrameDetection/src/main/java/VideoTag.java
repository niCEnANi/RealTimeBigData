import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.matcher.FastBasicKeypointMatcher;
import org.openimaj.feature.local.matcher.LocalFeatureMatcher;
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.math.geometry.transforms.estimation.RobustAffineTransformEstimator;
import org.openimaj.math.model.fit.RANSAC;
import org.openimaj.video.Video;
import org.openimaj.video.xuggle.XuggleVideo;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;


import com.clarifai.api.*;

import java.io.*;
import java.util.List;

/**
 * Created by Naga on 05-09-2016.
 */
public class VideoTag {
    static Video<MBFImage> video;
//    VideoDisplay<MBFImage> display = VideoDisplay.createVideoDisplay(video);
    static List<MBFImage> imageList = new ArrayList<MBFImage>();
    static List<Long> timeStamp = new ArrayList<Long>();
    static List<Double> mainPoints = new ArrayList<Double>();
    public static void main(String args[]) throws IOException, InterruptedException{
        String path = "data/sample.mkv";
        Frames(path);
        MainFrames();
        VRC();
    }

    public static void Frames(String path){
        video = new XuggleVideo(new File(path));
//        VideoDisplay<MBFImage> display = VideoDisplay.createVideoDisplay(video);
        int j=0;
        for (MBFImage mbfImage : video) {
            BufferedImage bufferedFrame = ImageUtilities.createBufferedImageForDisplay(mbfImage);
            j++;
            String name = "output/frames/new" + j + ".jpg";
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
    }

    public static void MainFrames(){
        for (int i=0; i<imageList.size() - 1; i++)
        {
            MBFImage image1 = imageList.get(i);
            MBFImage image2 = imageList.get(i+1);
            DoGSIFTEngine engine = new DoGSIFTEngine();
            LocalFeatureList<Keypoint> queryKeypoints = engine.findFeatures(image1.flatten());
            LocalFeatureList<Keypoint> targetKeypoints = engine.findFeatures(image2.flatten());
            RobustAffineTransformEstimator modelFitter = new RobustAffineTransformEstimator(5.0, 1500,
                    new RANSAC.PercentageInliersStoppingCondition(0.5));
            LocalFeatureMatcher<Keypoint> matcher = new ConsistentLocalFeatureMatcher2d<Keypoint>(
                    new FastBasicKeypointMatcher<Keypoint>(8), modelFitter);
            matcher.setModelFeatures(queryKeypoints);
            matcher.findMatches(targetKeypoints);
            double size = matcher.getMatches().size();
            mainPoints.add(size);
            System.out.println(size);
        }
        Double max = Collections.max(mainPoints);
        for(int i=0; i<mainPoints.size(); i++){
            if(((mainPoints.get(i))/max < 0.01) || i==0){
                Double name1 = mainPoints.get(i)/max;
                BufferedImage bufferedFrame = ImageUtilities.createBufferedImageForDisplay(imageList.get(i+1));
                String name = "output/mainframes/" + i + "_" + name1.toString() + ".jpg";
                File outputFile = new File(name);
                try {
                    ImageIO.write(bufferedFrame, "jpg", outputFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static void VRC() throws IOException, InterruptedException{
        //clarifai
        String APP_ID="sx3ak4SJ-YwLndj3Qdm6MLFIqZSZIKAtH0D52y-7";
        String APP_SECRET="l8yh7jto6BXH13KUeJPa_wjnFHQnfKdDvKO19I70";
        ClarifaiClient clarifai = new ClarifaiClient(APP_ID, APP_SECRET);

        File path = new File("output/mainframes");
        File[] files = path.listFiles();
        //request for tags
        List<RecognitionResult> results =
                clarifai.recognize(new RecognitionRequest(files));

        for(int i=0;i<results.size();i++){
            MBFImage image = ImageUtilities.readMBF(files[i]);
            int j=1;
            for (Tag tag : results.get(i).getTags()) {
                //System.out.println("result : "+i+" "+ tag.getName() + ": " + tag.getProbability());
                image.drawText(tag.getName(), (j*30)+40, (j*30)+50, HersheyFont.ASTROLOGY, 30, RGBColour.BLACK);
                if(++j>6){
                    break;
                }
                //break;
            }
            for(int k=1;k<200;k++) {
                DisplayUtilities.displayName(image, "videoFrames");
            }
        }

        /*for (Tag tag : results.get(0).getTags()) {
            System.out.println(tag.getName() + ": " + tag.getProbability());
            }*/
        /*for(VideoSegment vs : results.get(0).getVideoSegments()){
            System.out.println(vs.getTimestampSeconds()+" : "+vs.getTags());

        }*/
        /*com.google.gson.JsonObject js=results.get(0).getJsonResponse();
        System.out.println(js);*/
        /*InfoResult info = clarifai.getInfo();
        System.out.println(info.getMaxBatchSize());*/

        /*List<RecognitionResult> results =
          clarifai.recognize(new RecognitionRequest(new File("c:/users/nicen/videos/oceans.mp4")));*/

    }
}
