package cs1302.api;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Priority;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.Optional;
import java.util.List;

/**
 * Represents a JavaFX applicaion that takes the a user input to gather recipes about that dish.
 * The cuisine type from the recipe API will be used in a second news API to collect news articles
 * about the dish and presents the information on the screen.
 */
public class ApiApp extends Application {
    private NewsSourceAPI newsSourceApi = new NewsSourceAPI();
    private RecipeAPI recipeApi = new RecipeAPI();

    private Optional<RecipeAPI.RecipeResponse> recipeResponse;
    private Optional<NewsSourceAPI.NewsResponse> newsResponse;
    private List<RecipeAPI.RecipeResponse.Hit> hits;

    private int currentIndex;
    private String term;

    private Stage stage;
    private Scene scene;

    private VBox root;
    private VBox top;
    private HBox searchs;
    private HBox buttonBox;
    private VBox bottom;
    private StackPane stack;

    private TextField searchBar;
    private Label label;
    private Label apiLabel;
    private Button search;
    private Button back;
    private Button next;

   /**
     * Constructs an {@code ApiApp} object. This default (i.e., no argument)
     * constructor is executed in Step 2 of the JavaFX Application Life-Cycle.
     */
    public ApiApp() {
        this.currentIndex = 0;

        this.root = new VBox();
        this.top = new VBox(475);
        this.searchs = new HBox(5);
        this.buttonBox = new HBox(5);
        this.bottom = new VBox(3);
        this.stack = new StackPane();

        this.searchBar = new TextField("search a dish (e.g. hummus, dumpling, etc)");
        this.label = new Label("Search in a dish to get recipes and a related article.");
        this.apiLabel = new Label("Recipes provided by Edamam Recipe Search API "
            + " / News provided by NewsAPI");
        this.search = new Button("Search");
        this.back = new Button("Back");
        this.next = new Button("Next");
    } // ApiApp

    /**
     * Initializes the screen graph and sets up the nodes. It loads the background image,
     * adds the various buttons, and the textfield. It assigns action events to the buttons
     * and ensures the next and back button start off disabled.
     *
     * {@inheritDoc}
     */
    @Override
    public void init() {
        System.out.println("init");

        Image backgroundImage = new Image("file:resources/background.png");
        ImageView background = new ImageView(backgroundImage);
        background.setPreserveRatio(true);

        back.setDisable(true);
        next.setDisable(true);
        search.setOnAction(e -> handleSearch());
        back.setOnAction(e -> handleBack());
        next.setOnAction(e -> handleNext());

        stack.getChildren().addAll(background, root, top);
        root.getChildren().add(label);
        top.getChildren().addAll(searchs, bottom);
        searchs.getChildren().addAll(searchBar, search);
        bottom.getChildren().addAll(apiLabel, buttonBox);
        buttonBox.getChildren().addAll(back, next);

        back.setMaxWidth(Double.MAX_VALUE);
        next.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(back, Priority.ALWAYS);
        HBox.setHgrow(next, Priority.ALWAYS);
        HBox.setHgrow(searchBar, Priority.ALWAYS);

        initStyle();
    } // init

    /**
     * Sets the style of many UI components.
     */
    public void initStyle() {
        searchBar.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.7);" +
            "-fx-padding: 10;" +
            "-fx-font-size: 14;" +
            "-fx-background-radius: 50 50 50 50;"
        );
        search.setStyle(
            "-fx-pref-width: 80;" +
            "-fx-pref-height: 15;" +
            "-fx-font-size: 14;"
        );
        back.setStyle(
            "-fx-font-size: 14;"
        );
        next.setStyle(
            "-fx-font-size: 14;"
        );
        label.setStyle(
            "-fx-font-size: 16;" +
            "-fx-padding: 8;"
        );
        top.setPadding(new Insets(5));
        root.setStyle(
            "-fx-background-color: #FAF9F6;" +
            "-fx-background-color: rgba(255, 255, 255, 0.95);"
        );
        stack.setMargin(root, new Insets(50, 20, 60, 20));
    } // initStyle

    /**
     * Initializes the primary stage and sets up the main scene. It adds the stack
     * variable to the scene variable and ensures the stage is not resizable.
     *
     * {@inheritDoc}
     */
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        scene = new Scene(stack);

        // setup stage
        stage.setTitle("ApiApp!");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> Platform.exit());
        stage.sizeToScene();
        stage.setMaxWidth(1280);
        stage.setMaxHeight(720);
        stage.show();
        Platform.runLater(() -> this.stage.setResizable(false));
    } // start

    /**
     * Handles the Back button action, updating the UI to show the previous recipe and news.
     */
    public void handleBack() {
        currentIndex--;
        updateButtons();
        back.setDisable(currentIndex <= 0);
        next.setDisable(currentIndex >= 2 || currentIndex >= hits.size() - 1);
    } // handleBack

    /**
     * Handles the Next button action, updating the UI to show the next recipe and news.
     */
    public void handleNext() {
        currentIndex++;
        updateButtons();
        back.setDisable(currentIndex <= 0);
        next.setDisable(currentIndex >= 2 || currentIndex >= hits.size() - 1);
    } // handleNext

    /**
     * Handles the Search button action, beginning the search for recipes based on the user's
     * input as well as news results based on the information given by the recipe.
     */
    public void handleSearch() {
        term = searchBar.getText().trim();
        search.setDisable(true);
        next.setDisable(true);
        back.setDisable(true);
        label.setText("Loading...");
        search();
    } // handleSearch

    /**
     * Initiates the search for recipes given the user's input. Using the cuisine type of the
     * given recipe, it will search for related articles that have that name within its contents.
     * Will show an alert if no recipes or news articles are present.
     */
    public void search() {
        recipeResponse = recipeApi.searchRecipes(term);

        if (RecipeAPI.getRateLimitReached() || NewsSourceAPI.getRateLimitReached()) {
            run(() -> {
                try {
                    Thread.sleep(60000);
                    Platform.runLater(() -> {
                        search.setDisable(false);
                        back.setDisable(currentIndex <= 0);
                        next.setDisable(currentIndex >= 2 || currentIndex >= hits.size() - 1);
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } // try
            });
        } else {
            search.setDisable(false);
            next.setDisable(false);

            if (recipeResponse.isPresent()) {
                RecipeAPI.RecipeResponse response = recipeResponse.get();
                hits = response.getHits();

                if (hits != null && !hits.isEmpty()) {
                    currentIndex = 0;
                    updateRecipe();
                    back.setDisable(true);
                    next.setDisable(hits.size() <= 1);

                    List<String> cuisineList = response.getHits().get(0)
                        .getRecipe().getCuisineType();
                    newsResponse = newsSourceApi.searchNews(
                        cuisineList);

                    if (newsResponse.isPresent()) {
                        NewsSourceAPI.NewsResponse news = newsResponse.get();
                        if (news != null) {
                            List<NewsSourceAPI.NewsResponse.Article> articles = news.getArticles();
                            if (articles != null && !articles.isEmpty()) {
                                currentIndex = 0;
                                updateNews();
                            } // if
                        } else {
                            root.getChildren().clear();
                            root.getChildren().add(label);
                            next.setDisable(true);
                            label.setText("No articles were found! Try again with another dish.");
                        } // if
                    } // if
                } else {
                    root.getChildren().clear();
                    root.getChildren().add(label);
                    next.setDisable(true);
                    label.setText("No recipes were found! Try again with another dish.");
                } // if
            } // if
        } // if
    } // searchRecipe

    /**
     * Upates the UI to display information about a recipe based on the provided response.
     */
    public void updateRecipe() {
        root.getChildren().clear();

        if (recipeResponse.isPresent()) {
            RecipeAPI.RecipeResponse response = recipeResponse.get();
            hits = response.getHits();

            if (hits != null && !hits.isEmpty()) {
                RecipeAPI.RecipeResponse.Hit hit = hits.get(currentIndex);
                RecipeAPI.RecipeResponse.Hit.Recipe recipe = hit.getRecipe();

                if (recipe != null) {
                    String title = recipe.getLabel();
                    String url = recipe.getUrl();
                    double yield = recipe.getYield();
                    double calories = recipe.getCalories();
                    List<RecipeAPI.RecipeResponse.Hit.Recipe.Ingredient> ingredientsList =
                        recipe.getIngredients();

                    VBox newInfo = createRecipeInfoBox(
                        title,
                        url,
                        yield,
                        calories,
                        ingredientsList);
                    StackPane infoStack = new StackPane();
                    infoStack.getChildren().add(newInfo);
                    root.getChildren().add(infoStack);
                } // if
            } // if
        } // if
    } // updateRecipe

    /**
     * Updates the UI to display information about a news article based on the provided response.
     */
    public void updateNews() {
        if (newsResponse.isPresent()) {
            NewsSourceAPI.NewsResponse news = newsResponse.get();
            List<NewsSourceAPI.NewsResponse.Article> articles = news.getArticles();

            if (articles != null && !articles.isEmpty()) {
                NewsSourceAPI.NewsResponse.Article newsItem = articles.get(currentIndex);

                String title = newsItem.getTitle();
                String url = newsItem.getUrl();
                String content = newsItem.getContent();

                VBox newInfo = createNewsInfoBox(
                    title,
                    url,
                    content);
                StackPane infoStack = new StackPane();
                infoStack.getChildren().add(newInfo);
                root.getChildren().add(infoStack);
            } // if
        } // if
    } // updateNews

    /**
     * Updates the UI components, including recipe and news information, based on the current
     * recipe index.
     */
    public void updateButtons() {
        if (recipeResponse.isPresent()) {
            RecipeAPI.RecipeResponse response = recipeResponse.get();
            hits = response.getHits();
            updateRecipe();

            List<String> cuisines = hits.get(currentIndex)
                .getRecipe().getCuisineType();
            newsResponse = newsSourceApi.searchNews(cuisines);

            if (newsResponse.isPresent()) {
                updateNews();
            } // if
        } // if
    } // updateButtons

    /**
     * Creates a VBox containing information about a recipe and then styles the information.
     * Turns the ingredients list into a single string variable with no repeated ingredients and
     * ensures no more than 6 items are shown.
     *
     * @param title The title of the recipe.
     * @param url The url link of the recipe.
     * @param yield The yield of the given recipe.
     * @param calories The calories for the given recipe.
     * @param ingredientsList The list of ingredients of the recipe.
     * @return A VBox containing all the given information in string format.
     */
    public VBox createRecipeInfoBox(
        String title,
        String url,
        double yield,
        double calories,
        List<RecipeAPI.RecipeResponse.Hit.Recipe.Ingredient> ingredientsList) {

        String message = "";
        String ingredients = "";
        Set<String> unique = new HashSet<>();
        if (ingredientsList != null) {
            if (ingredientsList.size() > 6) {
                List<RecipeAPI.RecipeResponse.Hit.Recipe.Ingredient> maxIngredients =
                    ingredientsList.subList(0, 7);
                ingredientsList = maxIngredients;
                message = "* Recuded Ingredients List. See full website.";
            }
            for (RecipeAPI.RecipeResponse.Hit.Recipe.Ingredient ingredient : ingredientsList) {
                String text = ingredient.getText();
                if (unique.add(text)) {
                    ingredients += "* " + text + "\n";
                } // if
            } // for
            ingredients += message;
        } // if

        int newYield = (int) yield;
        int newCalories = (int) calories;

        Label header = new Label(title);
        Label information = new Label("For full instructions, visit the website:");
        Label link = new Label(url);
        Label amount = new Label("\nYield: " + String.valueOf(newYield) +
            "\tCalories: " + String.valueOf(newCalories));
        Label items = new Label(ingredients);

        header.setStyle("-fx-font-size: 20;" + "-fx-font-weight: bold;");
        items.setStyle("-fx-font-size: 14;");
        link.setStyle("-fx-font-style: italic;");
        amount.setStyle("-fx-underline: true;");

        header.setWrapText(true);
        items.setWrapText(true);
        link.setWrapText(true);

        VBox newInfo = new VBox();
        newInfo.setAlignment(Pos.TOP_CENTER);
        newInfo.getChildren().addAll(header, information, link, amount, items);
        newInfo.setPadding(new Insets(8));

        return newInfo;
    } // createRecipeInfoBox

    /**
     * Creates a VBox containing information about a news article and then styles the information.
     *
     * @param title The title of the news article.
     * @param url The url link of the article.
     * @param content The contents, truncated at 200 words, of the article.
     * @return A Vbox containing news article information.
     */
    public VBox createNewsInfoBox(String title,
        String url,
        String content) {

        Label header = new Label(title);
        Label text = new Label("For full article, visit the website:");
        Label link = new Label(url);
        Label info = new Label("\n" + content);

        header.setStyle("-fx-font-size: 20;" + "-fx-font-weight: bold;");
        link.setStyle("-fx-font-style: italic;");
        info.setStyle("-fx-font-size: 14;");

        info.setWrapText(true);
        link.setWrapText(true);
        header.setWrapText(true);

        VBox newInfo = new VBox();
        newInfo.getChildren().addAll(header, text, link, info);
        newInfo.setAlignment(Pos.TOP_CENTER);
        newInfo.setPadding(new Insets(8));

        return newInfo;
    } // createNewsInfoBox

    /**
     * Executes a thread with the given Runnable task.
     *
     * @param task A runnable task to be executed.
     */
    public void run(Runnable task) {
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    } // run
} // ApiApp
