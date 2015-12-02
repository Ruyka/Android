package hhp.pdfreader;

/**
 * Created by hhphat on 7/27/2015.
 */
public class OnlineCloudAccount {
    private String Account, passWord;

    public OnlineCloudAccount(String Account, String passWord){
        setAccount(Account);
        setPassWord(passWord);
    }

    public String getAccount() {
        return Account;
    }

    public void setAccount(String account) {
        Account = account;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }
}
