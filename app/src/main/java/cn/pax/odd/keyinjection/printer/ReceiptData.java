package cn.pax.odd.keyinjection.printer;

public class ReceiptData {
    private String mSN;
    private String mTmkKCV;
    private String mDate;
    private String mTime;
    private String mOperator1;
    private String mOperator2;

    public ReceiptData() {
    }

    public String getSN() {
        return mSN;
    }

    public void setSN(String SN) {
        mSN = SN;
    }

    public String getTmkKCV() {
        return mTmkKCV;
    }

    public void setTmkKCV(String tmkKCV) {
        mTmkKCV = tmkKCV;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public String getTime() {
        return mTime;
    }

    public void setTime(String time) {
        mTime = time;
    }

    public String getOperator1() {
        return mOperator1;
    }

    public void setOperator1(String operator1) {
        mOperator1 = operator1;
    }

    public String getOperator2() {
        return mOperator2;
    }

    public void setOperator2(String operator2) {
        mOperator2 = operator2;
    }
}
