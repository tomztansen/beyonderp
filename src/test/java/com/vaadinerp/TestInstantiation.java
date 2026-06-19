package com.vaadinerp;

import com.vaadinerp.views.GenericFormView;

public class TestInstantiation {
    public static void main(String[] args) {
        try {
            new GenericFormView(null, null);
            System.out.println("Success");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
