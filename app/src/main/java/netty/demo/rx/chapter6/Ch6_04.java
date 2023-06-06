package netty.demo.rx.chapter6;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.util.List;

import static netty.demo.rx.chapter1.Ch1.sleep;
import static netty.demo.rx.chapter6.Ch6.intenseCalculation;

public class Ch6_04 {

    public static void main(String[] args) {

        Observable<String> source1 =
                Observable.just("Alpha", "Beta", "Gamma")
                        .subscribeOn(Schedulers.computation())
                        .map(s -> intenseCalculation((s)));
        Observable<Integer> source2 =
                Observable.range(1, 3)
                        .subscribeOn(Schedulers.computation())
                        .map(s -> intenseCalculation((s)));
        Observable<String> zip = Observable.zip(source1, source2, (s, i) -> s + "-" + i);

        List<String> blockingSingle = zip
                .doOnNext(s -> System.out.println(Thread.currentThread().getName()+": "+s))
                .toList()
                .blockingGet();

        System.out.println(Thread.currentThread().getName()+": "+blockingSingle);
//                .subscribe(System.out::println);
        sleep(20000);
    }
}
