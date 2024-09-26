import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class MyTelegramBot extends TelegramLongPollingBot {
    // Объявление класса MyTelegramBot, который наследует функциональность TelegramLongPollingBot

    private final String BOT_NAME;
    private final String BOT_TOKEN;
    private final String URL;

    public MyTelegramBot(String BOT_NAME, String BOT_TOKEN, String nasaApiKey) throws TelegramApiException {
        this.BOT_NAME = BOT_NAME;
        this.BOT_TOKEN = BOT_TOKEN;
        this.URL = "https://api.nasa.gov/planetary/apod?api_key=" + nasaApiKey;
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(this);
    }

    @Override
    public void onUpdateReceived(Update update) {
        // Метод, который вызывается при получении обновления от Telegram
        if (update.hasMessage() && update.getMessage().hasText()) { // Условие проверяет, существует ли обновление (update) и содержит ли оно сообщение (hasMessage()). Также проверяется, есть ли в сообщении текст (hasText()). Таким образом, код внутри этого условия будет выполняться только в случае, если в обновлении есть текстовое сообщение.
            long chatId = update.getMessage().getChatId(); // В этой строке извлекается идентификатор чата, из которого пришло сообщение. Этот идентификатор (chatId) используется для отправки ответов в тот же чат.
            String answer = update.getMessage().getText(); // Получение текста сообщения и сохранение в переменной answer
            String[] splittedAnswer = answer.split(" "); // Метод split(" ") разбивает текст сообщения на массив строк по пробелу. То есть, если в сообщении было несколько слов, каждое из них будет храниться в отдельном элементе массива splittedAnswer.
            String action = splittedAnswer[0]; // Получение первого слова (команды) из разделенного массива, действие (action) может использоваться для определения, какое действие должен выполнить бот. Например, если сообщение начинается со слова "старта", это может означать, что бот должен запустить какую-то определенную команду.

            switch (action) { // Определение действия на основе первой команды
                case "/start": // Если команда "/start"
                    sendMessage("Я бот Наса. Я присылаю картинку дня. Для получения картинки дня введите /image. Если необходимо получить картинку за конкретную дату введите введите /date гггг-мм-дд (пример: /date 2020-10-05).", chatId); // Отправка приветственного сообщения
                    break;
                case "/help": // Если команда "/help"
                    sendMessage("Введите /image для получения картинки дня. Если необходимо получить картинку за конкретную дату введите /date гггг-мм-дд (пример: /date 2023-03-15)", chatId); // Предложение помощи
                    break;
                case "/image": // Если команда "/image"
                    String image = Utils.getImage(URL); // Получение картинки дня с API NASA
                    sendMessage(image, chatId); // Отправка полученной картинки
                    break;
                case "/date": // Если команда "/date"
                    String date = splittedAnswer[1]; // Получение даты из второго слова
                    image = Utils.getImage(URL + "&date=" + date); // Получение картинки по указанной дате
                    sendMessage(image, chatId); // Отправка полученной картинки
                    break;
                default: // Если команда не распознана
                    sendMessage("Я не знаю такой команды. Введите /help", chatId); // Отправка сообщения об ошибке
            }
        }
    }

    public void sendMessage(String text, long chatId) {
        // Метод для отправки сообщения в чат
        SendMessage message = new SendMessage(); // Создание объекта SendMessage с обязательными полями
        message.setChatId(chatId); // Установка идентификатора чата, куда будет отправлено сообщение
        message.setText(text); // Установка текста сообщения

        try {
            execute(message); // Вызов метода для отправки сообщения
        } catch (TelegramApiException e) {
            e.printStackTrace(); // В случае ошибки печатаем стек вызовов
        }
    }

    @Override
    public String getBotUsername() {
        // Метод для получения имени бота
        return BOT_NAME; // Возврат имени бота
    }

    @Override
    public String getBotToken() {
        // Метод для получения токена бота
        return BOT_TOKEN; // Возврат токена бота
    }
}