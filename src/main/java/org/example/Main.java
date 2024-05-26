package org.example;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;


//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        try {
            Document doc = Jsoup.connect("https://se.uit.edu.vn/").get();
            String title = doc.title();

            System.out.println(title);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}