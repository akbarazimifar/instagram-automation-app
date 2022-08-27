package in.semibit.instadp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.androidnetworking.AndroidNetworking;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.common.util.Strings;
import com.google.firebase.FirebaseApp;

import java.io.File;

import in.semibit.instadp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private Button watchAdButton;
    private TextView mLevelTextView;

    private ActivityMainBinding binding;
    private AdService adService;
    private DownloadState curDownloadState = DownloadState.WAITING_FOR_INPUT;
    private boolean rewardsLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidNetworking.initialize(getApplicationContext());

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        adService = new AdService(this);
        adService.initAds();

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        watchAdButton = binding.watchAd;

        binding.searchButton.setOnClickListener(v -> {
            if (binding.urlOrUsername.getText() == null || Strings.isEmptyOrWhitespace(binding.urlOrUsername.getText().toString())) {
                binding.conturlOrUsername.setError("Please enter a username or profile url");
                return;
            }
            binding.conturlOrUsername.setError(null);
            searchUserOrLink(binding.urlOrUsername.getText().toString());
        });
        adService.onRewardLoaded = new GenricCallback() {
            @Override
            public void onStart() {
                rewardsLoaded = true;
            }
        };
    }

    public void searchUserOrLink(String userOrLink) {
        curDownloadState = DownloadState.SEARCHING;

        // download to lcoal
        binding.contBottom.setVisibility(View.VISIBLE);
        processScrapedData(new ScrappedData());

    }

    public void processScrapedData(ScrappedData processedData){

       binding.watchAd.setOnClickListener(v->{
           if (adService.isRewardedAdReady()) {
               curDownloadState = DownloadState.READY_TO_SHOW_AD;
               adService.showRewardedAd(() -> showDownloadAndShare(processedData));
           } else {
               curDownloadState = DownloadState.WAITING_FOR_AD;
               adService.onRewardLoaded = () -> adService.showRewardedAd(() -> showDownloadAndShare(processedData));
               if (!adService.init) {
                   showDownloadAndShare(processedData);
                   return;
               }
               adService.initializeRewardedAd();
           }
       });
    }

    File downloadedFile;

    public void showDownloadAndShare(ScrappedData processedData) {
        curDownloadState = DownloadState.READY;
        binding.watchAd.setText("Save to Gallery");
        binding.share.setVisibility(View.VISIBLE);



        binding.share.setOnClickListener(c -> {
            shareImage(downloadedFile);
        });
        binding.watchAd.setOnClickListener(v -> {
            checkPermission();
        });

    }

    public void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            saveToGallery(downloadedFile);

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 12312);
            }
        }
    }

    private void shareImage(File file) {

    }

    private void saveToGallery(File file) {
        File picsDir = (getExternalFilesDir(Environment.DIRECTORY_PICTURES));
        Toast.makeText(this, "Saved to " + picsDir.getAbsolutePath(), Toast.LENGTH_LONG).show();
        binding.watchAd.setText("VIEW");
        binding.watchAd.setOnClickListener(v->{

        });

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 12312) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveToGallery(downloadedFile);
            } else {
                Toast.makeText(MainActivity.this, "Permission denied to read your External storage. Please retry again.", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }
}