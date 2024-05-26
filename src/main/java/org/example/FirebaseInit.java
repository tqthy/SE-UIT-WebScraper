package org.example;

import com.google.cloud.firestore.FirestoreOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FirebaseInit {
    private static Firestore db;

    public static void initialize() {
        try {
            File configs = new File("/Users/justixz/IdeaProjects/SE-UIT-WebScraper/src/eduforum-d2e3d-firebase-adminsdk-y4uig-4d6fce68e2.json");
            FileInputStream serviceAccount = new FileInputStream(configs);
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .setProjectId("eduforum-d2e3d")
                    .build();

            if (FirebaseApp.getApps().isEmpty()) { // Check if FirebaseApp has already been initialized
                FirebaseApp.initializeApp(options);
            }

            // Point to the Firestore emulator
            FirestoreOptions firestoreOptions = FirestoreOptions.newBuilder()
                    .setCredentials(credentials)
                    .setProjectId("eduforum-d2e3d") // Ensure this matches your FirebaseOptions
                    .setHost("localhost:8080") // Default Firestore emulator port
                    .build();
            db = firestoreOptions.getService();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Firestore getFirestore() {
        return db;
    }
}