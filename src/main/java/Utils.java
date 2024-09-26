import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;

public class Utils {
    public static String getImage(String nasaUrl) { // Объявляем статический метод getImage, который принимает строковый параметр nasaUrl (URL для запроса к NASA) и возвращает строку (URL изображения).
        CloseableHttpClient httpclient = HttpClients.createDefault(); // Создаём экземпляр HTTP-клиента httpclient, который будет использоваться для выполнения HTTP-запросов. Метод createDefault() создает клиент с настройками по умолчанию
        ObjectMapper mapper = new ObjectMapper(); // Объект ObjectMapper из библиотеки Jackson. Он предназначен для преобразования данных из формата JSON в Java-объекты и обратно.
        HttpGet request = new HttpGet(nasaUrl); // Формирование HTTP GET-запроса с использованием переданного URL NASA.
        try {
            CloseableHttpResponse response = httpclient.execute(request); // В try блоке выполняется HTTP-запрос с использованием метода execute клиента httpclient. Ответ от сервера хранится в переменной response.
            NasaAnswer answer = mapper.readValue(response.getEntity().getContent(), NasaAnswer.class); //  Содержимое ответа (response.getEntity().getContent()) преобразуется из формата JSON в объект NasaAnswer с помощью метода readValue объекта mapper. Это позволяет получить данные в удобном для использования виде.
            return answer.url; //  Метод возвращает URL изображения, который был извлечён из объекта answer. Это основной результат выполнения метода.
        } catch (IOException e) { // Начинается блок обработки исключений. Если происходит ошибка (например, сетевые проблемы), управление переходит сюда.
            return ""; // Если произошла ошибка, метод возвращает пустую строку. Это указывает на неудачное выполнение метода.
        }
    }
}
