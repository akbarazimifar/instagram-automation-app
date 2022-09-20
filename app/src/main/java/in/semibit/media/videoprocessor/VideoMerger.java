package in.semibit.media.videoprocessor;

import android.content.Context;

import com.semibit.ezandroidutils.EzUtils;

import org.mp4parser.Container;
import org.mp4parser.muxer.Movie;
import org.mp4parser.muxer.Track;
import org.mp4parser.muxer.builder.DefaultMp4Builder;
import org.mp4parser.muxer.container.mp4.MovieCreator;
import org.mp4parser.muxer.tracks.AppendTrack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import in.semibit.media.common.GenricDataCallback;
import in.semibit.media.common.Insta4jClient;

public class VideoMerger {

    GenricDataCallback onLog;
    Context context;

    public VideoMerger(Context context) {
        this.context = context;
    }

    public void setOnLog(GenricDataCallback onLog) {
        this.onLog = onLog;
    }


    public CompletableFuture<File> merge(String fileName, List<File> mp4Files) {
        if (onLog == null) {
            onLog = EzUtils::l;
        }
        File outputFile = new File(Insta4jClient.root, fileName);
        CompletableFuture<File> onRes = new CompletableFuture<>();
        try {

            MovieCreator mc = new MovieCreator();
            List<Movie> movies = mp4Files.stream().map(mp -> {
                try {
                    return mc.build(mp.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }).collect(Collectors.toList());

            //Fetching the video tracks from the movies and storing them into an array
            Track[] vetTrackVideo = new Track[0];
            vetTrackVideo = movies.stream()
                    .flatMap(movie -> movie.getTracks().stream())
                    .filter(movie -> movie.getHandler().equals("vide"))
                    .collect(Collectors.toList())
                    .toArray(vetTrackVideo);

            //Fetching the audio tracks from the movies and storing them into an array
            Track[] vetTrackAudio = new Track[0];
            vetTrackAudio = movies.stream()
                    .flatMap(movie -> movie.getTracks().stream())
                    .filter(movie -> movie.getHandler().equals("soun"))
                    .collect(Collectors.toList())
                    .toArray(vetTrackAudio);

            //Creating the output movie by setting a list with both video and audio tracks
            Movie movieOutput = new Movie();
            List<Track> listTracks = new ArrayList<>(List.of(new AppendTrack(vetTrackVideo), new AppendTrack(vetTrackAudio)));
            movieOutput.setTracks(listTracks);

            //Building the output movie and storing it into a Container
            DefaultMp4Builder mp4Builder = new DefaultMp4Builder();
            Container c = mp4Builder.build(movieOutput);

            //Writing the output file
            FileOutputStream fos = new FileOutputStream(outputFile);
            c.writeContainer(fos.getChannel());
            fos.close();
            onRes.complete(outputFile);
        } catch (Exception e) {
            e.printStackTrace();
            onRes.completeExceptionally(e);
        }

        return onRes;
    }

    public static void load(Context context) {

    }
}
