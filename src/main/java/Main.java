import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Main {
    public static void main(String[] args) throws IOException, TelegramApiException {
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream("config.properties")) {
            properties.load(input);
        }

        String botToken = properties.getProperty("bot.token");
        String nasaApiKey = properties.getProperty("nasa.api_key");
        MyTelegramBot bot = new MyTelegramBot("Picture_of_Nasa_bot", botToken, nasaApiKey);
    }
}