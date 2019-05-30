package me.herrlestrate.yandexintro;

import me.herrlestrate.yandexintro.Parser.CustomParser;

public class Main {
    public static void main(String[] args){

        CustomParser parser = new CustomParser("input.txt");
        parser.parseTo("output.json");
    }
}
