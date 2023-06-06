package netty.demo.rx.chapter6;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.net.URL;
import java.util.Scanner;

import static netty.demo.rx.chapter1.Ch1.sleep;

public class Ch6_10 {

    public static void main(String[] args) {
        String href = "https://api.github.com/users/thomasnield/starred";
        Observable.fromSupplier(() -> getResponse(href))
                .subscribeOn(Schedulers.io())
                .subscribe(System.out::println);

        sleep(10000);
    }
    private static String getResponse(String path) {
        try {
            return new Scanner(new URL(path).openStream(), "UTF-8")
                    .useDelimiter("\\A").next();
        } catch (Exception e) {
            return e.getMessage();
        } }
}
