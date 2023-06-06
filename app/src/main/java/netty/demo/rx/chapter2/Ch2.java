package netty.demo.rx.chapter2;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.observables.ConnectableObservable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static netty.demo.rx.chapter1.Ch1.sleep;

public class Ch2 {

    public static void main(String[] args) {
        Observable<String> source = Observable.create(emitter -> {
            emitter.onNext("Alpha");
            emitter.onNext("Beta");
            emitter.onNext("Gamma");
            emitter.onComplete();
        });
        source.subscribe(s -> System.out.println("RECEIVED: " + s));

        Observable<String> sourceJust = Observable.just("Alpha", "Beta", "Gamma");
        sourceJust.map(String::length).filter(i -> i >= 5)
                .subscribe(s -> System.out.println("RECEIVED: " + s));

        ConnectableObservable<String> sourceConnectable = Observable.just("Alpha", "Beta", "Gamma").publish();
        //Set up observer 1
        sourceConnectable.subscribe(s -> System.out.println("Observer 1: " + s));
        //Set up observer 2
        sourceConnectable
                .map(String::length)
                .subscribe(i -> System.out.println("Observer 2: " + i));
        //Fire!
        sourceConnectable.connect();


        Observable.range(5, 10)
                .subscribe(s -> System.out.println("RECEIVED: " + s));

        List<Integer> integers = new ArrayList<>();
        integers.add(23);
        integers.add(13);
        integers.add(54);
        integers.add(453);

        Single<@NonNull List<Integer>> listSingle = Observable.fromIterable(integers)
                .map(integer -> integer / 5)
                .toList();

        listSingle.subscribe(s -> System.out.println(s));

        Observable<Long> seconds =
                Observable.interval(1, TimeUnit.SECONDS);
        Disposable disposable = seconds
                .subscribe(l -> System.out.println("Received: " + l));

        //sleep 5 seconds
        sleep(5000);

        //dispose and stop emissions
        disposable.dispose();
        //sleep 5 seconds to prove
        //there are no more emissions
        sleep(5000);
    }
}
