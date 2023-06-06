package netty.demo.rx.chapter6;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static netty.demo.rx.chapter1.Ch1.sleep;

public class Ch6_14 {

    public static void main(String[] args) {
        Observable.just("WHISKEY/27653/TANGO",
                        "6555/BRAVO", "232352/5675675/FOXTROT")
                .subscribeOn(Schedulers.io()) //Starts on I/O scheduler
                .flatMap(s -> Observable.fromArray(s.split("/")))
                .observeOn(Schedulers.computation()) //Switches to computation scheduler
                .filter(s -> s.matches("[0-9]+"))
                .map(Integer::valueOf)
                .reduce((total, next) -> total + next)
                .subscribe(i -> System.out.println("Received " + i +
                        " on thread " + Thread.currentThread().getName()));
        sleep(1000);

    }
}
