package org.example;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;


//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        FirebaseInit.initialize();
        Firestore db = FirebaseInit.getFirestore();

        try {
            // community seed
            DocumentReference seRef = db.collection("Community").document("SE-UIT");
            ApiFuture<DocumentSnapshot> future = seRef.get();

            DocumentSnapshot document = future.get();
            if (!document.exists()) {
                // write seed
                Map<String, Object> seed = new HashMap<>();
                seed.put("name", "Cổng thông tin khoa Công Nghệ Phần Mềm");
                seed.put("description", "Cổng thông tin chính thức của Khoa Công Nghệ Phần Mềm - Trường Đại học Công Nghệ Thông Tin, ĐHQG-HCM. Cung cấp thông tin về các hoạt động của Khoa, thông báo, tuyển dụng, học bổng, sự kiện, tin tức, ...");
                seed.put("department", "Công nghệ Phần mềm");
                seed.put("timeCreated", FieldValue.serverTimestamp());
                seed.put("visibility", "all");
                seed.put("totalPost", 0);
                seed.put("adminList", new ArrayList<>());
                seed.put("userList", new ArrayList<>());
                WriteBatch batch = db.batch();
                // add seed to database
//                seRef.set(seed);
                batch.set(seRef, seed);
                // category seed
                List<Map<String, Object>> categories = List.of(
                        Map.of("title", "Sự kiện nổi bật", "isAnnouncement", false),
                        Map.of("title", "Thông báo học vụ", "isAnnouncement", false),
                        Map.of("title", "Học bổng - Tuyển dụng", "isAnnouncement", false),
                        Map.of("title", "Khoa học - Công nghệ", "isAnnouncement", false)
                );
                DocumentReference catRef1 = db.collection("Community").document("SE-UIT").collection("Category").document("SuKienNoiBat");
//                catRef1.set(categories.get(0));
                batch.set(catRef1, categories.get(0));

                DocumentReference catRef2 = db.collection("Community").document("SE-UIT").collection("Category").document("ThongBaoHocVu");
//                catRef2.set(categories.get(1));
                batch.set(catRef2, categories.get(1));

                DocumentReference catRef3 = db.collection("Community").document("SE-UIT").collection("Category").document("HocBongTuyenDung");
//                catRef3.set(categories.get(2));
                batch.set(catRef3, categories.get(2));

                DocumentReference catRef4 = db.collection("Community").document("SE-UIT").collection("Category").document("KhoaHocCongNghe");
//                catRef4.set(categories.get(3));
                batch.set(catRef4, categories.get(3));

                batch.commit().get();
            }


            // fetch su kien
            Document doc = Jsoup.connect("https://se.uit.edu.vn/tin-tuc/su-kien-noi-bat.html").get();
            WriteBatch batch = db.batch();
            Map<String, Object> cat = Map.of("categoryID", "SuKienNoiBat", "title", "Sự kiện nổi bật");
            fetchArticles(db, doc, batch, cat);

            // fetch hoc bong tuyen dung
            doc = Jsoup.connect("https://se.uit.edu.vn/tin-tuc/hoc-bong-tuyen-dung.html").get();
            cat = Map.of("categoryID", "HocBongTuyenDung", "title", "Học bổng - Tuyển dụng");
            fetchArticles(db, doc, batch, cat);

            // fetch thong bao hoc vu
            doc = Jsoup.connect("https://se.uit.edu.vn/tin-tuc/thong-bao-hoc-vu.html").get();
            cat = Map.of("categoryID", "ThongBaoHocVu", "title", "Thông báo học vụ");
            fetchArticles(db, doc, batch, cat);

            // fetch khoa hoc cong nghe
            doc = Jsoup.connect("https://se.uit.edu.vn/tin-tuc/khoa-hoc-cong-nghe.html").get();
            cat = Map.of("categoryID", "KhoaHocCongNghe", "title", "Khoa học - Công nghệ");
            fetchArticles(db, doc, batch, cat);

            // commit changes
            batch.commit().get();

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    private static void fetchArticles(Firestore db, Document doc, WriteBatch batch, Map<String, Object> articleCategory) throws IOException, InterruptedException, ExecutionException {
        Elements articleLinks;
        List<Map<String, Object>> fetchedPosts;
        ApiFuture<QuerySnapshot> query;
        boolean isEmpty;
        DocumentSnapshot documentSnapshot;
        Timestamp lastTime;
        articleLinks = doc.select(".g-content-array.g-joomla-articles h3 a");
        fetchedPosts = new ArrayList<>();
        for (Element articleLink : articleLinks) {
            String link = articleLink.attr("abs:href");
            System.out.println(link);
            Document article = Jsoup.connect(link).get();
            String title = article.select("h1[itemprop=\"headline\"]").first().text();
            String content = article.select("div[itemprop=\"articleBody\"]").first().text();
            Timestamp timeCreated = Timestamp.parseTimestamp(article.select("time[datetime]").first().attr("datetime"));
            Map<String, Object> newPost = new HashMap<>();
            newPost.put("title", title);
            newPost.put("content", content);
            newPost.put("timeCreated", timeCreated);
            List<Map<String, Object>> categories = List.of(
                    articleCategory
            );
            newPost.put("category", categories);
            newPost.put("communityID", "SE-UIT");
            newPost.put("creator", null);
            newPost.put("isAnonymous", false);
            newPost.put("totalUpvote", 0);
            newPost.put("totalDownvote", 0);
            newPost.put("voteDifference", 0);
            newPost.put("totalComment", 0);
            newPost.put("lastModified", FieldValue.serverTimestamp());
            fetchedPosts.add(newPost);
        }

        fetchedPosts.sort((o1, o2) -> {
            Timestamp t1 = (Timestamp) o1.get("timeCreated");
            Timestamp t2 = (Timestamp) o2.get("timeCreated");
            return t2.compareTo(t1);
        });

        query = db.collection("Community")
                .document("SE-UIT")
                .collection("Post")
                .whereArrayContains("category", articleCategory)
                .orderBy("timeCreated", Query.Direction.DESCENDING)
                .limit(1)
                .get();
        isEmpty = true;
        if (query.get().isEmpty()) {
            for (Map<String, Object> fetchedPost : fetchedPosts) {
                DocumentReference ref = db.collection("Community")
                        .document("SE-UIT")
                        .collection("Post")
                        .document();
                batch.set(ref, fetchedPost);
            }
            isEmpty = false;
        }
        if (isEmpty) {
            documentSnapshot = query.get().getDocuments().getFirst();
            lastTime = (Timestamp) documentSnapshot.get("timeCreated");
            for (Map<String, Object> fetchedPost : fetchedPosts) {
                Timestamp timeCreated = (Timestamp) fetchedPost.get("timeCreated");
                if (timeCreated.compareTo(lastTime) <= 0) {
                    break;
                }
                DocumentReference ref = db.collection("Community").document("SE-UIT").collection("Post").document();
                batch.set(ref, fetchedPost);
            }
        }
    }
}