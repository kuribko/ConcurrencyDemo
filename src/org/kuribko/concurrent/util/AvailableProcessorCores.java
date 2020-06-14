package org.kuribko.concurrent.util;

public class AvailableProcessorCores {

    public static void main(String[] args) {
        System.out.println("Available processor cores: " + Runtime.getRuntime().availableProcessors());
    }

}
