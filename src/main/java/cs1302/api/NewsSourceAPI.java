package cs1302.api;

import java.net.http.HttpResponse.BodyHandlers;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Alert;
import javafx.application.Platform;
import java.nio.charset.StandardCharsets;
import com.google.gson.GsonBuilder;
import com.google.gson.Gson;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;
import java.util.Optional;
import java.util.List;
import java.net.URLEncoder;
import java.net.URI;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * This class interatcs with the NewsAPI to search for news articles about the given
 * cuisines.
 */
public class NewsSourceAPI {

    /**
     * Represents a response structure from the NewsApi.
     */
    public class NewsResponse {

        private List<Article> articles;

        /**
         * Gets a list of the articles.
         * @return A list of articles.
         */
        public List<Article> getArticles() {
            return articles;
        } //getArticles

        /**
         * A class representing an article with its information.
         */
        public static class Article {
            private String title;
            private String url;
            private String content;

            /**
             * Gtes the title of the article.
             * @return The title of the article.
             */
            public String getTitle() {
                return title;
            } // getTitle

            /**
             * Gets the url link of the article.
             * @return The url of the article.
             */
            public String getUrl() {
                return url;
            } // getUrl

            /**
             * Gets the contents of the article, truncated to 200 words.
             * @return The contents of the article.
             */
            public String getContent() {
                return content;
            } // getContent
        } // Article
    } // NewsResponse

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();

    private static Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .create();

    private static final int MAX_REQUESTS_PER_DAY = 100;
    private static int newsApiRequests = 0;
    private static boolean rateLimitReached = false;

    private static String configPath = "resources/config.properties";

    private static String API_KEY;
    private static final String ENDPOINT = "https://newsapi.org/v2/everything";

    /**
     * Searches for news articles based on the specific cuisines. Sends an HttpRequest
     * and checks the status code. If the status code is not 200, it will show an
     * alert to the UI.
     *
     * @param cuisines The list of cuisines to search for.
     * @return An Optional NewsResponse containing the search results.
     */
    public static Optional<NewsSourceAPI.NewsResponse> searchNews(List<String> cuisines) {
        try {
            try (FileInputStream configFileStream = new FileInputStream(configPath)) {
                Properties config = new Properties();
                config.load(configFileStream);
                API_KEY = config.getProperty("newsapi.apikey");
            } catch (IOException e) {
                e.printStackTrace();
            } // try

            if (!checkNewsRateLimit()) {
                String cuisineString = cuisines.get(0) + " cuisine";
                String url = String.format("%s?apiKey=%s&q=%s&language=en&sortBy=relevancy",
                    ENDPOINT,
                    API_KEY,
                    URLEncoder.encode(cuisineString, StandardCharsets.UTF_8));

                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();

                HttpResponse<String> response = HTTP_CLIENT
                    .send(request, BodyHandlers.ofString());

                newsApiRequests++;

                final int statusCode = response.statusCode();
                if (statusCode == 200) {
                    String responseBody = response.body();
                    NewsResponse result = GSON.fromJson(responseBody, NewsResponse.class);
                    return Optional.ofNullable(result);
                } else {
                    throw new IOException("Response status code not 200:" + statusCode);
                } // if
            } else {
                newsApiRequests = 0;
            } // if
        } catch (IOException | InterruptedException e) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Error");
                alert.setTitle("Error");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            });
        } // try
        return Optional.empty();
    } // fetchNews

    /**
     * Checks whether or not the number of api requests exceeds the max amount of requests
     * per day. If it reaches 50% or 100%, it will send a warning to the screen and change the
     * rateLimitReached variable to true.
     *
     * @return Will return {@code true} if the max requests per day has been reached and
     * {@code false} if it hasn't.
     */
    private static boolean checkNewsRateLimit() {
        if (newsApiRequests == MAX_REQUESTS_PER_DAY / 2) {
            Platform.runLater(() -> sendWarning("50% of your news api requests have been used!"));
        } else if (newsApiRequests >= MAX_REQUESTS_PER_DAY) {
            Platform.runLater(() -> sendWarning("NewsApi rate limit has been reached. " +
                "There will be a pause for a minute."));
            return rateLimitReached = true;
        }
        return rateLimitReached = false;
    } // checkNewsRateLimit

    /**
     * Recieves the boolean value of the rateLimitReached variable.
     *
     * @return The boolean value of the {@code rateLimitReached} variable.
     */
    public static boolean getRateLimitReached() {
        return rateLimitReached;
    } // getRateLimitReached

    /**
     * Creates and initializes an alert variable which will be shown on the screen
     * with the provided contents.
     *
     * @param content The contents of the warning.
     */
    private static void sendWarning(String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText("Warning");
        alert.setTitle("Warning");
        alert.setContentText(content);
        alert.showAndWait();
    } // sendAlert
} // NewsSourceAPI
