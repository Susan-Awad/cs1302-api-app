package cs1302.api;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Optional;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.application.Platform;
import java.util.Properties;
import java.io.FileInputStream;

/**
 * Interacts with the Edamam Recipe API to search for recipes.
 */
public class RecipeAPI {

    /**
     * Represents a response structure from the Edamam Recipe API.
     */
    public static class RecipeResponse {
        private List<Hit> hits;

        /**
         * Gets the number of hits containing recipe information.
         * @return A list of hits of recipes.
         */
        public List<Hit> getHits() {
            return hits;
        } // getHits

        /**
         * A class representing a hit containing a recipe.
         */
        public class Hit {
            private Recipe recipe;

            /**
             * Gets the recipe associated with the hit.
             * @return A Recipe with its specific information.
             */
            public Recipe getRecipe() {
                return recipe;
            } // getRecipe

            /**
             * A class representing a recipe and its properties.
             */
            public class Recipe {
                private String url;
                private double yield;
                private double calories;
                private String label;
                private List<Ingredient> ingredients;
                private List<String> cuisineType;

                /**
                 * Gets the url of the recipe.
                 * @return A string of the url.
                 */
                public String getUrl() {
                    return url;
                } // getUri

                /**
                 * Gets the yield of the recipe.
                 * @return A double value of the yield.
                 */
                public double getYield() {
                    return yield;
                } // getYield

                /**
                 * Gets the calories of the recipe.
                 * @return A double value of the calories.
                 */
                public double getCalories() {
                    return calories;
                } // getCalories

                /**
                 * Gets the label of the recipe.
                 * @return A string of the label.
                 */
                public String getLabel() {
                    return label;
                } // getLabel

                /**
                 * Gets the ingredients of the recipe.
                 * @return A list of the ingredients.
                 */
                public List<Ingredient> getIngredients() {
                    return ingredients;
                } // getIngredients

                /**
                 * Gets the cuisine types of the recipe.
                 * @return A list of strings representing the types of cuisine.
                 */
                public List<String> getCuisineType() {
                    return cuisineType;
                } // getCuisine

                /**
                 * A class representing the ingredients in the recipe.
                 */
                public class Ingredient {
                    private String text;

                    /**
                     * Gets the text of the ingredients.
                     * @return A string of the ingredient.
                     */
                    public String getText() {
                        return text;
                    } // getText
                } // Ingredient
            } // Recipe
        } // Hit
    } // RecipeResponse

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();

    private static Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .create();

    private static final int MAX_REQUESTS_PER_MINUTE = 9;
    private static int recipeApiRequests = 0;
    private static boolean rateLimitReached = false;

    private static String configPath = "resources/config.properties";

    private static String API_KEY;
    private static final String SEARCH_ENDPOINT = "https://api.edamam.com/api/recipes/v2";
    private static int statusCode;

    /**
     * Searches for recipes based on the provided dish. It creates a url based on given
     * information and uses an HTTP_CLIENT to send a request to the Edamam Recipe API.
     *
     * @param dish A dish given by the user to search recipes on.
     * @return An Optional containing the RecipeResponse if successful, empty otherwise.
     */
    public static Optional<RecipeAPI.RecipeResponse> searchRecipes(String dish) {
        try {
            try (FileInputStream configFileStream = new FileInputStream(configPath)) {
                Properties config = new Properties();
                config.load(configFileStream);
                API_KEY = config.getProperty("recipeapi.apikey");
            } catch (IOException e) {
                e.printStackTrace();
            } // try

            if (!checkRecipeRateLimit()) {
                String url = String.format("%s?type=public&q=%s&app_id=aebf2db0&app_key=%s",
                    SEARCH_ENDPOINT,
                    URLEncoder.encode(dish, StandardCharsets.UTF_8),
                    API_KEY);

                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();

                HttpResponse<String> response = HTTP_CLIENT
                    .send(request, HttpResponse.BodyHandlers.ofString());

                recipeApiRequests++;

                statusCode = response.statusCode();
                if (statusCode == 200) {
                    String body = response.body();
                    RecipeResponse result = GSON.fromJson(body, RecipeResponse.class);
                    return Optional.ofNullable(result);
                } else {
                    throw new IOException("Response status code not 200:"
                        + statusCode);
                } // if
            } else {
                recipeApiRequests = 0;
            } // if
        } catch (IOException | InterruptedException e) {
            Platform.runLater(() -> showAlert(e.getMessage() + ": " + statusCode));
        } // try

        return Optional.empty();
    } // searchRecipes

    /**
     * Checks whether or not the number of api requests exceeds the max amount of requests
     * per minute. If the max amount of hits has been reached, the alert will pop up
     * to inform the user of an intentional delay. It will change the rateLimitReached
     * value to true.
     *
     * @return Will return {@code true} if the max requests per minute has been reached and
     * {@code false} if it hasn't.
     */
    private static boolean checkRecipeRateLimit() {
        if (recipeApiRequests >= MAX_REQUESTS_PER_MINUTE) {
            Platform.runLater(
                () -> showAlert("Intentional delay due to Edamam's rate limits. "
                    + "Please wait a minute."));
            return rateLimitReached = true;
        } // if
        return rateLimitReached = false;
    } // checkRecipeRateLimit

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
     * @param content The contents of the error message.
     */
    private static void showAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Error");
        alert.setTitle("Error");
        alert.setContentText(content);
        alert.showAndWait();
    } // showAlert
} // RecipeAPI
