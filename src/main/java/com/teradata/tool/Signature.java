package com.teradata.tool;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by Peter.Yang on 2018/3/21.
 */
public class Signature {
    public static void main(String[] args) {
        new Signature().printName();
    }

    private void printName() {
        System.out.println(myname_ + mystudent_id_ + myclass_ + " " + new SimpleDateFormat("yyyyMMdd").format(mydate_));
    }

    private String myname_;
    private String mystudent_id_;
    private String myclass_;
    private Date mydate_;

    public Signature() {
    }

    public String getMyname_() {
        return myname_;
    }

    public void setMyname_(String myname_) {
        this.myname_ = myname_;
    }

    public String getMystudent_id_() {
        return mystudent_id_;
    }

    public void setMystudent_id_(String mystudent_id_) {
        this.mystudent_id_ = mystudent_id_;
    }

    public String getMyclass_() {
        return myclass_;
    }

    public void setMyclass_(String myclass_) {
        this.myclass_ = myclass_;
    }

    public Date getMydate_() {
        return mydate_;
    }

    public void setMydate_(Date mydate_) {
        this.mydate_ = mydate_;
    }
}