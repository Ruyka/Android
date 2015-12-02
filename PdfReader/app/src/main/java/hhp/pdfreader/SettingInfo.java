package hhp.pdfreader;

/**
 * Created by hhphat on 7/27/2015.
 */
public class SettingInfo {
    private String owner, version;
    private int skin;
    private boolean isRememberLastPage, isAutoTurnPageBySpeech;
    private OnlineCloudAccount account;

    public void saveInfo(){

    }
    public void loadInfo(){

    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getVersion() {
        return version;
    }

    public int getSkin() {
        return skin;
    }

    public boolean isRememberLastPage() {
        return isRememberLastPage;
    }

    public boolean isAutoTurnPageBySpeech() {
        return isAutoTurnPageBySpeech;
    }

    public OnlineCloudAccount getAccount() {
        return account;
    }
}
