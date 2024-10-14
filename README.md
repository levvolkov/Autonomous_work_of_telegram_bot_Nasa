# «Телеграм бот картинки Nasa за конкретную дату и его автономная работа»


### Предусловие.

* Установка и настройка [IDEA](https://github.com/netology-code/jdfree-homeworks/tree/jdfree-6/01#readme) на компьютер.
    * Все настройки проекта как в интрукции за исключением: Шаг 9. В появившемся окне в поле "Language" выберите "Java", в поле "System" выберите "Maven" вместо "IntelliJ".
* Несмотря на то, что API NASA публичный, доступ к нему предоставляется по ключу, который достаточно просто получить по адресу: https://api.nasa.gov/. Перейдя по ссылке, заполняем личными данными поля:
    * First Name
    * Last Name
    * Email

  На почтовый адрес будет выслан api_key. С этим ключом нужно делать запросы к API.

* Создаем бота
    * Откройте Telegram и найдите пользователя с именем [@BotFather](https://t.me/BotFather)
    * Напишите команду /newbot и следуйте инструкциям. Выберите имя и уникальное имя пользователя для вашего бота. После создания вам будет предоставлен токен (TOKEN), который понадобился для работы с ботом, а так же ссылка на этого бота.

------



**1. Настройка проекта.**
* Создайте новый Maven-проект (или Gradle, если предпочитаете).
* Добавьте зависимости в `pom.xml` воспользовавшись библиотеками [Apache HttpClient](https://mvnrepository.com/search?q=Apache+HttpClient), [Jackson Databind](https://mvnrepository.com/search?q=Jackson+Databind), [telegrambots](https://central.sonatype.com/artifact/org.telegram/telegrambots/6.8.0/overview), [GitHub telegrambots](https://github.com/rubenlagus/TelegramBots/blob/aad139de980ae25ee7a4b06bbe7644c6077421ce/TelegramBots.wiki/Getting-Started.md), 
```java
<dependencies>
        <dependency>
            <groupId>org.apache.httpcomponents.client5</groupId>
            <artifactId>httpclient5</artifactId>
            <version>5.3</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.17.0</version>
        </dependency>
        <dependency>
            <groupId>org.telegram</groupId>
            <artifactId>telegrambots</artifactId>
            <version>6.8.0</version>
        </dependency>
    </dependencies>
```
**2. Создадим класс `MyTelegramBot` предназначенный для создания и управления Telegram-ботом, который использует долгий опрос (long polling) для получения обновлений и сообщений. Он инициализирует бота с заданным именем и токеном, а также содержит метод для обработки входящих сообщений от пользователей. В этом классе также используется API NASA для получения изображения дня, которое бот может отправлять в ответ на запросы пользователей. Таким образом, бот может интерактивно взаимодействовать с пользователями, предоставляя им интересный контент.**
* Объявляем класс `MyTelegramBot`, который наследуется от `TelegramLongPollingBot`. Это нужно для того, чтобы использовать функциональность API Telegram для долгого опроса.
```java
    public class MyTelegramBot extends TelegramLongPollingBot {
```

* Объявление переменных, в которых определяются поля для хранения имени бота, токена и URL для доступа к API NASA. Это необходимо для аутентификации и получения данных для взаимодействия с пользователем.
```java
    private final String BOT_NAME; 
    private final String BOT_TOKEN; 
    private final String URL;
```

* Создадим конструктор, который принимает имя и токен бота. В этом месте происходит инициализация полей и регистрация бота в API Telegram. Это один из важных шагов, так как без регистрации бот не сможет получать обновления.
```java
   public MyTelegramBot(String BOT_NAME, String BOT_TOKEN, String nasaApiKey) throws TelegramApiException {
        this.BOT_NAME = BOT_NAME;
        this.BOT_TOKEN = BOT_TOKEN;
        this.URL = "https://api.nasa.gov/planetary/apod?api_key=" + nasaApiKey;
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(this);
    }
```

* Метод `onUpdateReceived` вызывается при поступлении новых сообщений. Зазмещаем его сразу после конструктора, поскольку он является основным методом для обработки входящих обновлений. В этом методе я обрабатываю текстовые сообщения и определяю, какое действие выполнять в зависимости от полученной команды.
```java
 @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) { 
            long chatId = update.getMessage().getChatId(); 
            String answer = update.getMessage().getText(); 
            String[] splittedAnswer = answer.split(" "); 
            String action = splittedAnswer[0]; 
```

* После того как получили команду, используем конструкцию `switch` для обработки различных действий. Это удобно и делает код более структурированным. Каждая команда имеет свое собственное условие и реализует определенное действие (например, отправку сообщения приветствия или получения изображения).
```java
switch (action) { 
                case "/start":
                    sendMessage("Я бот Наса. Я присылаю картинку дня", chatId); 
                    break;
                case "/help": 
                    sendMessage("Введите /image для получения картинки дня", chatId); 
                    break;
                case "/image": 
                    String image = Utils.getImage(URL); 
                    sendMessage(image, chatId); 
                    break;
                case "/date": // Если команда "/date"
                    String date = splittedAnswer[1]; 
                    image = Utils.getImage(URL + "&date=" + date); 
                    sendMessage(image, chatId); 
                    break;
                default: 
                    sendMessage("Я не знаю такой команды. Введите /help", chatId);
}
``` 

* Метод `sendMessage` помогает отправлять сообщения в чат. Он создан для избежания дублирования кода и упрощает отправку сообщений. Каждый раз, когда нужно отправить ответ пользователю, просто вызываем этот метод с нужным текстом и идентификатором чата. Так же добавим обработку исключений: `try` мы пытаемся исполнить опасный код, в `catch` мы отлавливаем возможные ошибки. Если во время отправки сообщения произойдет исключение (например, проблемы с сетью или неправильный chatId), будет вызван catch (TelegramApiException e), что позволит программе не завершиться аварийно, `e.printStackTrace();` печатает стек вызовов, что может помочь в диагностике проблемы, поскольку предоставляет информацию об исключении.
```java
public void sendMessage(String text, long chatId) {
        SendMessage message = new SendMessage(); 
        message.setChatId(chatId); 
        message.setText(text);
        try {
            execute(message); 
        } catch (TelegramApiException e) {
            e.printStackTrace(); 
        }
    }        
```

* Методы `getBotUsername` и `getBotToken` возвращают имя и токен бота, которые необходимы API Telegram для идентификации вашего бота.
```java
@Override
    public String getBotUsername() {
        return BOT_NAME;
    }

@Override
    public String getBotToken() {
        return BOT_TOKEN; 
    }
}
```
**3.Создадим класс приложения `Main`, откуда начинается выполнение программы.**
* Главный метод программы, который является точкой входа; он принимает массив строковых аргументов и может выбрасывать исключения IOException и TelegramApiException.
```java
public static void main(String[] args) throws IOException, TelegramApiException {
```
* Создаём объект Properties для загрузки конфигураций из файла **config.properties** с использованием безопасного потока ввода.
```java
   Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream("config.properties")) {
            properties.load(input);
        }
```
* Извлекаем значение токена бота из загруженных свойств, используя ключ **nasa.api_key** и токен **bot.token** и создаём новый экземпляр бота, передавая ему имя (**Picture_of_Nasa_bot**) токен и ключ, полученный из конфигурационного файла.
```java
String botToken = properties.getProperty("bot.token");
        String nasaApiKey = properties.getProperty("nasa.api_key");
        MyTelegramBot bot = new MyTelegramBot("Picture_of_Nasa_bot", botToken, nasaApiKey);
    }
}
```
* Создаем файл `config.properties` для хранения `nasa.api_key` и `bot.token` и записываем туда ключ Nasa который получили на почту и токен полученный от пользователя Telegram [@BotFather](https://t.me/BotFather). Не забыв добавить файл `config.properties` в `.gitignore`
```java
nasa.api_key=ключь_Nasa_полученный_на_почту
bot.token=токен_который_получили_из_Telegram
```

**4. Для того, чтобы связывать класс `NasaAnswer` c json форматом, генерируем конструктор со специальными особенностями.**
```java
 public NasaAnswer(@JsonProperty("date") String date,
                      @JsonProperty("explanation") String explanation,
                      @JsonProperty("hdurl") String hdurl,
                      @JsonProperty("media_type") String media_type,
                      @JsonProperty("service_version") String service_version,
                      @JsonProperty("title") String title,
                      @JsonProperty("url") String url,
                      @JsonProperty("copyright") String copyright) {
        this.copyright = copyright;
        this.date = date;
        this.explanation = explanation;
        this.hdurl = hdurl;
        this.media_type = media_type;
        this.service_version = service_version;
        this.title = title;
        this.url = url;
    }
```

**5. Создадим класс `Utils` с одним статическим методом `getImage`, который получает изображение с указанного URL NASA.**
* В этом коде реализуется функциональность для выполнения HTTP-запроса к API NASA с целью получения URL изображения. Метод `getImage` принимает строку `nasaUrl`, создаёт HTTP-клиент и использует `ObjectMapper` для парсинга JSON-ответа. Он формирует запрос с помощью `HttpGet` и выполняет его с помощью клиента. Полученный ответ обрабатывается, и URL изображения из поля `url` десериализуется в объект `NasaAnswer`. В результате, метод возвращает этот URL, или пустую строку в случае ошибки.
```java
public class Utils {
    public static String getImage(String nasaUrl) { 
        CloseableHttpClient httpclient = HttpClients.createDefault(); 
        ObjectMapper mapper = new ObjectMapper(); 
        HttpGet request = new HttpGet(nasaUrl); 
        try {
            CloseableHttpResponse response = httpclient.execute(request); 
            NasaAnswer answer = mapper.readValue(response.getEntity().getContent(), NasaAnswer.class); 
            return answer.url; 
        } catch (IOException e) { 
            return ""; 
        }
    }
}
```
**6. Для того чтоб телеграм бот работал постоянно и не зависил от запуска в IDEA его надо [сделать автономным](https://github.com/levvolkov/Autonomous_work_of_telegram_bot_Nasa/blob/main/Instructions/autonomous.md).**
 
