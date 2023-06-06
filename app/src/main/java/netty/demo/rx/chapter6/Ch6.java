package netty.demo.rx.chapter6;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static netty.demo.rx.chapter1.Ch1.sleep;

public class Ch6 {

    public static void main(String[] args) {

//        DateTimeFormatter f = DateTimeFormatter.ofPattern("MM:ss");
//        System.out.println(LocalDateTime.now().format(f));
//        Observable.interval(1, TimeUnit.SECONDS)
//                .map(i -> LocalDateTime.now().format(f) + " " +
//                        i + " Mississippi")
//                .subscribe(System.out::println);
//        sleep(5000);

        Observable.just("Alpha", "Beta", "Gamma")
                .subscribeOn(Schedulers.computation())
                .map(s -> intenseCalculation((s)))
                .subscribe(System.out::println);
        Observable.range(1, 3)
                .subscribeOn(Schedulers.computation())
                .map(s -> intenseCalculation((s)))
                .subscribe(System.out::println);

        Observable.range(4, 6)
                .subscribeOn(Schedulers.computation())
                .map(s -> intenseCalculation((s)))
                .subscribe(System.out::println);
        sleep(20000);
    }

    public static <T> T intenseCalculation(T value) {
        System.out.println(Thread.currentThread().getName());
        sleep(ThreadLocalRandom.current().nextInt(3000));
        return value;
    }
}
