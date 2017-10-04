package ru.pflb.wd;

import cucumber.api.java.ru.И;
import cucumber.api.java.ru.Пусть;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.hamcrest.Matchers.*;

/**
 * @author <a href="mailto:8445322@gmail.com">Ivan Bonkin</a>.
 */
public class RestSteps {

    private static final String BASE_URI = "http://localhost:9966/petclinic";
    public static final String JSON_OBJECT = "JSONObject";
    public static final String JSON_ARRAY = "JSONArray";

    private Map<String, Object> aliasMap = new HashMap<>();

    @Пусть("^выполнен \"GET\" запрос к \"([^\"]*)\" и результат запомнен как JSONArray$")
    public void requestJSONObject(String uri) throws Throwable {
        String array = given()
                .accept("application/json")
                .baseUri(BASE_URI)
                .when()
                .get("/api/owners")
                // параметры ответа
                .then()
                // ожилаение 2xx кодов - успешных
                .statusCode(both(greaterThanOrEqualTo(200)).and(lessThan(300)))
                // и... .statusCode(equalTo(200)) - тоже будет работать
                // извлечение тела ответа сервера как JSONArray
                .extract().body().asString();
        aliasMap.put(JSON_ARRAY, new JSONArray(array));
    }


    @И("^из запомненного JSONArray извлечен JSONObject с полем \"([^\"]*)\" равным \"([^\"]*)\"$")
    public void extractJSONArray(String fieldName, String fieldValue) throws Throwable {

        // преобразование ответа сервера к типу JSONArray
        JSONArray array = (JSONArray) aliasMap.get(JSON_ARRAY);

        // see https://stackoverflow.com/a/7634559
        for(int n = 0; n < array.length(); n++) {
            JSONObject object = array.getJSONObject(n);
            // нашли внутри JSONArray объект по фамилии lastName
            if (fieldValue.equals(object.getString(fieldName))) {

                aliasMap.put(JSON_OBJECT, object);
                return;
            }
        }

        throw new IllegalArgumentException("\"" + fieldValue + "\" не был найден");
    }

    @И("^запомнить произвольную дату до (\\d+) дней в прошлом как \"([^\"]*)\"$")
    public void rememberDate(int daysAgoMax, String alias) throws Throwable {

        // дата от 1 до daysAgoMax дней назад
        int daysAgoRnd = daysAgoMax + 1;

        // генерация даты
        String date = LocalDate.now().minusDays(daysAgoRnd).format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        aliasMap.put(alias, date);

    }

    @И("^запомнить рандомное слово из (\\d+) букв как \"([^\"]*)\"$")
    public void rememberKeyword(int length, String alias) throws Throwable {
        aliasMap.put(alias, capitalize(randomAlphabetic(length).toLowerCase()));
    }

    @И("^запомнить как \"([^\"]*)\" JSONObject питомца \"птица\" с именем \"([^\"]*)\" и датой рождения \"([^\"]*)\"$")
    public void rememberPetJSONObject(String rememberAs, String petNameAlias, String birthDateAlias) throws Throwable {

        // составления тела запроса на добавление нового домашенго животного
        JSONObject petJsonObj = new JSONObject()
                // id = null
                .put("id", JSONObject.NULL)
                // name = сгенерированное имя
                .put("name", aliasMap.get(petNameAlias))
                .put("birthDate", aliasMap.get(birthDateAlias))
                // в поле тип вводим - bird
                .put("type", new JSONObject().put("id", 3).put("name", "lizard"))
                // в поле хозяина вводим JSON описания хозяина
                .put("owner", (JSONObject) aliasMap.get(JSON_OBJECT));

        aliasMap.put(rememberAs, petJsonObj);
    }


    @И("^выполнен \"POST\" запрос с телом \"([^\"]*)\" к \"([^\"]*)\" и id запомнен как \"([^\"]*)\"$")
    public void postJSONObject(String bodyAlias, String uri, String idAlias) throws Throwable {

        Integer id = given()
                // Content-type для запроса - формат body запроса
                .contentType("application/json")
                .accept("application/json")
                // тело запроса
                .body(((JSONObject)aliasMap.get(bodyAlias)).toString())
                // URI REST сервера
                .baseUri(BASE_URI)
                .when()
                // путь относительно REST сервера
                .post(uri)
                .then()
                // ожилаение 2xx кодов - успешных
                .statusCode(equalTo(201))
                // возвращение id созданного питомца
                .extract().path("id");

        aliasMap.put(idAlias, id.toString());
    }
}
