package ch.zhaw.prog2.account;

import java.util.concurrent.atomic.AtomicInteger;

public class Account {
    private int id;
    private AtomicInteger saldo;

    public Account(int id, int initialAmount) {
        this.id = id;
        this.saldo = new AtomicInteger(initialAmount);
    }

    public int getId() {
        return id;
    }

    public int getSaldo() {
        return saldo.get();
    }

    public void changeSaldo(int delta) {
        this.saldo.addAndGet(delta);
    }
}
