package netty.demo.rx.chapter8;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static netty.demo.rx.chapter1.Ch1.sleep;

public class Ch8_03 {

    public static void main(String[] args) {
        Flowable.range(1, 999_999_999)
                .map(Ch8_02.MyItem::new)
                .observeOn(Schedulers.io())
                .subscribe(myItem -> {
                    sleep(50);
                    System.out.println("Received MyItem " + myItem.id);
                });
        sleep(Long.MAX_VALUE);
    }
}
