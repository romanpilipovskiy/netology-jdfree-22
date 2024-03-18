import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.FileOutputStream;
import java.io.IOException;

public class Main {
    // Получаем ключ для API из файла .env
    public static Dotenv dotenv = Dotenv.configure().directory("./").load();

    // Ссылка, на которую будем отправлять запрос
    public static final String URI = "https://api.nasa.gov/planetary/apod?api_key=" + dotenv.get("API_TOKEN");

    // Сущность, которая будет преобразовывать ответ в объект NASA
    public static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {

        // Настраиваем HTTP клиент, который будет отправлять запросы
        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(5000)
                        .setSocketTimeout(30000)
                        .setRedirectsEnabled(false)
                        .build())
                .build();

        // Отправляем запрос и получаем ответ
        CloseableHttpResponse response = httpClient.execute(new HttpGet(URI));

        // Преобразуем ответ в Java-объект NasaObject
        NasaObject nasaObject = mapper.readValue(response.getEntity().getContent(), NasaObject.class);
        System.out.println(nasaObject);

        // Отправляем запрос и получаем ответ с картинкой
        CloseableHttpResponse pictureResponse = httpClient.execute(new HttpGet(nasaObject.getUrl()));

        // Формируем автоматически название для файла
        String[] arr = nasaObject.getUrl().split("/");
        String fileName = arr[arr.length - 1];

        HttpEntity entity = pictureResponse.getEntity();

        // Сохраняем в файл
        FileOutputStream fos = new FileOutputStream(fileName);
        entity.writeTo(fos);
        fos.close();
    }
}