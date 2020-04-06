package ch.zhaw.prog2.account;

class AccountTransferThread extends Thread {

    private Account fromAccount;
    private Account toAccount;
    private int amount;
    private int maxIter = 10000;

    public AccountTransferThread(String name, Account fromAccount, Account toAccount, int amount) {
        super(name);
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
    }

    /*  Transfer amount from fromAccount to toAccount */
    public void accountTransfer() {
        // Account must not be overdrawn
        if (fromAccount.getSaldo() >= amount) {
            fromAccount.changeSaldo(-amount);
            toAccount.changeSaldo(amount);
        }
    }

    @Override
    public void run() {
        for (int i = 0; i < maxIter; i++) {
            accountTransfer();
            try { // simulation of work time
                Thread.sleep((int) (Math.random() * 10));
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
        System.out.println("DONE! " + getName());
    }
}
