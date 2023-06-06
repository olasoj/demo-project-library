package netty.demo.rx.chapter4;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.observables.GroupedObservable;

import java.util.ArrayList;
import java.util.List;

public class Ch4_01 {

    public static void main(String[] args) {
        Observable<String> src1 = Observable.just("Alpha", "Beta");
        Observable<String> src2 = Observable.just("Zeta", "Eta");
        Observable.merge(src1, src2)
                .subscribe(i -> System.out.println("RECEIVED: " + i));

        List<Integer> integers = new ArrayList<>();
        integers.add(23);
        integers.add(13);
        integers.add(54);
        integers.add(453);

        Observable<Integer> integerObservable = Observable.fromIterable(integers);
        Observable<Integer> integerObservable2 = Observable.fromIterable(integers).map(i -> i * 2);

        integerObservable.mergeWith(integerObservable2)
                .subscribe(i -> System.out.println(i));


        Observable<String> src1Zip = Observable.just("Alpha", "Beta", "Gamma");
        Observable<Integer> src2Zip = Observable.range(1, 6);
        Observable.zip(src1Zip, src2Zip, (s, i) -> {
                    System.out.print(Thread.currentThread().getName());
                    return s + "-" + i;
                })
                .subscribe(System.out::println);

        Observable<String> source = Observable.just("Alpha", "Beta",
                "Gamma", "Delta", "Epsilon");
        Observable<GroupedObservable<Integer, String>> byLengths =
                source.groupBy(String::length);
        byLengths.flatMapSingle(Observable::toList)
                .subscribe(System.out::println);
    }
}
