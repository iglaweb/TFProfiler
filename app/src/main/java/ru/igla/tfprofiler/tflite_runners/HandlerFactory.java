package ru.igla.tfprofiler.tflite_runners;

import java.util.Date;

public class HandlerFactory {

    enum ValidHandler {

        String {
            @Override
            Handler<String> make() {
                return new StringHandler();
            }
        },
        Date {
            @Override
            Handler<Date> make() {
                return new DateHandler();
            }
        };

        abstract <T> Handler<T> make();
    }

    public <T> Handler<T> getHandler(Class<T> clazz) {
        if (clazz == String.class) {
            return ValidHandler.String.make();
        }
        if (clazz == Date.class) {
            return ValidHandler.Date.make();
        }
        return null;
    }

    public static class DateHandler implements Handler<Date> {

        @Override
        public void handle(Date date) {
            System.out.println(date);
        }
    }

    public static class StringHandler implements Handler<String> {

        @Override
        public void handle(String str) {
            System.out.println(str);
        }
    }

    public interface Handler<T> {

        void handle(T obj);
    }
}