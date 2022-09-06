package in.semibit.instadp.common;

import android.os.Environment;

import com.github.instagram4j.instagram4j.IGClient;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Insta4jClient {

    private static IGClient client;
    public static File root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "instadp");

    public static IGClient getClient(String username, String passwd,GenricDataCallback callback) {
        if(callback == null){
            callback =  (s)->{};
        }
        if(client == null){

            try {
                if (!root.exists()) {
                    root.mkdir();
                }
                File file = new File(root.getAbsoluteFile(), "instagram.txt");
                if (file.exists()) {
                    String split = Files.readAllBytes(Paths.get(file.getPath())).toString();
                    username = split.split(",")[0].trim();
                    passwd = split.split(",")[1].trim();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                File sessionFile = new File(root, "instagram_session.json");
                File fileClient = new File(root, "instagram_client.json");

                try {
                    if (fileClient.exists() && sessionFile.exists()) {
                        client = IGClient.deserialize(fileClient, sessionFile);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (client == null) {
                    client = IGClient.builder()
                            .username(username)
                            .password(passwd)
                            .login();

                    client.serialize(fileClient, sessionFile);

                }
                callback.onStart("Logged In");
            } catch (Exception e) {
                e.printStackTrace();
                callback.onStart(e.getMessage());
            }
        }
        return client;
    }


}
