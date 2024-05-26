package org.example;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.common.util.concurrent.MoreExecutors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        FirebaseInit.initialize();
        Firestore db = FirebaseInit.getFirestore();
        Map<String, Object> testData = new HashMap<>();
        testData.put("test", "hahaha");
        ApiFuture<DocumentReference> addedDocRef = db.collection("TestThoiNha").add(testData);
        ApiFutures.addCallback(addedDocRef, new ApiFutureCallback<DocumentReference>() {
            @Override
            public void onFailure(Throwable t) {
                // Handle failure
                System.out.println("Failed to add document " + t.getMessage());
            }

            @Override
            public void onSuccess(DocumentReference result) {
                System.out.println("Added document with ID: " + result.getId());
            }
        }, MoreExecutors.directExecutor());

        try {
            Document doc = Jsoup.connect("https://se.uit.edu.vn/").get();
            String title = doc.title();


            System.out.println(title);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}