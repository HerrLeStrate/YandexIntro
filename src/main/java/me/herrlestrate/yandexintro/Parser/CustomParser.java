package me.herrlestrate.yandexintro.Parser;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Pattern;

public class CustomParser {

    private String mInput;
    private static Logger LOGGER = LoggerFactory.getLogger(CustomParser.class);

    public CustomParser(String input){
        LOGGER.info("CustomParser initialized with input: {}", input);
        mInput = input;
    }

    public void parseTo(String output){

        Scanner scanner = init(output);
        if(scanner == null){
            LOGGER.debug("Stopped!");
            return;
        }

        JSONObject root = new JSONObject();

        while(scanner.hasNextLine()){
            String line = scanner.nextLine().toLowerCase();
            LOGGER.debug("Scanner gives us line: {}", line);

            if(!isValid(line)){
                LOGGER.error("String {} not valid!", line);
                LOGGER.debug("Stopped!");
                return;
            }

            String[] args = line.split("=");
            String[] keys = args[0].split(Pattern.quote("."));
            int value;

            try{
                value = Integer.parseInt(args[1]);
            }catch(NumberFormatException exception){
                LOGGER.error("Error while convert {} to Integer: {} ", args[1], exception.getMessage());
                LOGGER.debug("Stopped");
                return;
            }

            if(!addToJson(root, keys, value)){
                LOGGER.debug("Stopped!");
                return;
            } else {
                LOGGER.debug("Successful parsed line");
            }


        }

        scanner.close();
        LOGGER.debug("Scanner closed");
        print(output, root);
        LOGGER.debug("Printed to output!");

    }

    private boolean addToJson(JSONObject root, String[] keys, int value){
        int step = 0;
        JSONObject entity = root;
        while(step < keys.length && entity.has(keys[step])){
            if(!(entity.get(keys[step]) instanceof JSONObject)){
                LOGGER.error("{} exists and not JSONObject!", getFullPath(keys, step));

                return false;
            }
            entity = entity.getJSONObject(keys[step]);
            step++;
        }

        for(; step < keys.length; step++){
            entity.put(keys[step], new JSONObject());
            entity = entity.getJSONObject(keys[step]);
        }

        if(entity.has("value") && (entity.get("value") instanceof JSONObject)){
            LOGGER.error("value at {} is already JSONObject", getFullPath(keys,step));

            return false;
        }

        entity.put("value", value);
        return true;
    }

    private String getFullPath(String[] keys, int limit){
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i <= limit; i++){
            stringBuilder.append(keys[i]);
            if(i < limit)
                stringBuilder.append('.');
        }
        return stringBuilder.toString();
    }

    private Scanner init(String output){
        if(new File(output).exists()){
            LOGGER.error("Output file {} already exists", output);
            return null;
        }

        File fileInput = new File(mInput);

        if(!fileInput.exists()){
            LOGGER.error("File {} not founded!", mInput);
            return null;
        }

        Scanner scanner;

        try {
            scanner = new Scanner(fileInput);
        } catch(FileNotFoundException exception){
            LOGGER.error(exception.getMessage(), exception);
            return null;
        }

        return scanner;
    }

    private void print(String output, JSONObject result){
        File fileOutput = new File(output);
        FileWriter fw;

        try {
            if(!fileOutput.createNewFile()){
                LOGGER.error("File for output not created!");
                return;
            }

            fw = new FileWriter(fileOutput);

            fw.write(toJsonFormat(result));

            fw.close();
            LOGGER.info("Done!");
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private String toJsonFormat(JSONObject object){
        return object.toString();
    }

    private boolean isValid(String value){
        if(value.indexOf('=') == -1){
            LOGGER.error("String not contains '='!");
            return false;
        }

        if(value.indexOf('=') != value.lastIndexOf('=')) {
            LOGGER.error("String contains more than 1 symbol '='!");
            return false;
        }

        if(value.contains("..")){
            LOGGER.error("String contains \"..\"!");
            return false;
        }

        if(value.indexOf('=') == 0 || value.indexOf('.') == 0){
            LOGGER.error("First symbol not in a-zA-Z!");
            return false;
        }

        if(value.contains(".=")){
            LOGGER.error("String contains \".=\"");
            return false;
        }

        for(int i = 0; i < value.length(); i++){
            char x = value.charAt(i);
            if(!(('a' <= x && x <= 'z') ||
                    ('A' <= x && x <= 'Z') ||
                    ('0' <= x && x <= '9') ||
                    (x == '.') ||
                    (x == '=')))
                return false;
        }

        return true;

    }

}
