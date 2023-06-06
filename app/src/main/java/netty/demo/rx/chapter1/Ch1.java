package netty.demo.rx.chapter1;

import io.reactivex.rxjava3.core.Observable;

import java.util.concurrent.TimeUnit;

public class Ch1 {

    public static void main(String[] args) {
        Observable<String> myStrings = Observable.just("Alpha", "Beta", "Gamma");

        myStrings.subscribe(s -> System.out.println(Thread.currentThread().getName() + ": "+ s));

        Observable<Long> secondIntervals =
                Observable.interval(1, TimeUnit.SECONDS);
        secondIntervals.subscribe(s -> System.out.println(Thread.currentThread().getName() + ": "+ s));
                /* Hold main thread for 5 seconds
                   so Observable above has chance to fire */
        sleep(20000);
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
