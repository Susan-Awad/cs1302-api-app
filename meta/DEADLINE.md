# Deadline

Modify this file to satisfy a submission requirement related to the project
deadline. Please keep this file organized using Markdown. If you click on
this file in your GitHub repository website, then you will see that the
Markdown is transformed into nice-looking HTML.

## Part 1.1: App Description

> Please provide a friendly description of your app, including
> the primary functions available to users of the app. Be sure to
> describe exactly what APIs you are using and how they are connected
> in a meaningful way.

> **Also, include the GitHub `https` URL to your repository.**

    This project will take any dish that the user inputs (e.g. hummus or soup) and use
    it to search up recipes with Edamam's Recipe Search API. It will provide the title
    of the dish, a url to the full website, and a list of ingredients needed. If the
    ingredients list exceeds 7 items, the full list will be cut short for space
    purposes. Using the cuisine type given by the recipe API, the NewsAPI will search
    for the cuisine type and provide a news article within a month timefram. It will
    search for the term in any of the contents of the news article (title/description/
    contents). It will then provide the news article's title, url, and the contents,
    which is truncated to 200 characters. With each search, the app will provide 3
    recipes and 3 news articles about that specific recipes cuisine type. These can be
    navigated through with the next and back buttons.

    https://github.com/Susan-Awad/cs1302-api-app

## Part 1.2: APIs

> For each RESTful JSON API that your app uses (at least two are required),
> include an example URL for a typical request made by your app. If you
> need to include additional notes (e.g., regarding API keys or rate
> limits), then you can do that below the URL/URI. Placeholders for this
> information are provided below. If your app uses more than two RESTful
> JSON APIs, then include them with similar formatting.

### API 1

```
https://https://api.edamam.com/api/recipes/v2?type=public&q=hummus&app_id=aebf2db0
```

> This URL request requires an API key as well, which would be added on the end of the
    URL. The rate limit for this API is 10 hits per minute, so the app will have a
    slight delay of 1 minute if this is exceeded by the user.

### API 2

```
https://https://newsapi.org/v2/everything?q=asian+cuisine&language=en&sortBy=relevancy
```

> This URL request required an API key as well, with is added at the beginning of the
    query string. The rate limit for this API is 100 requests per day. There will
    be a warning when the user uses 50% of the requests and if 100 requests is reached
    the app will have a 1 minute delay.

## Part 2: New

> What is something new and/or exciting that you learned from working
> on this project?

    While working on this progect, I learned how to do many things. Since I had free
    reign over the design of the app, I had to learn how to do many things in order
    to get the app to look how I wanted. One of most challenging things to do was the
    main menu and getting the recipe and news article to stack on top of each other,
    changing with the back and next buttons. I kept getting duplication errors and
    didn't know how to fix it for a while. It was very fun when I was finally able to
    achieve what I wanted. I also was able to fully understand how the JSON classes
    worked, as before, I understood it on paper but couldn't in practice. This project
    forced me to make my own JSON classes, which helped me to understand what was
    happening.

## Part 3: Retrospect

> If you could start the project over from scratch, what do
> you think might do differently and why?

    If I had to start this project over again, I would change a few things about how
    I approached it. For one, I would find two API's that would work and plan out how
    they would connect. At first, I picked two and realized the first API wasn't used
    in the second API and that they were both pulling from the user input. I had to
    change my JSON classes and HTTP requests multiple times due to this. I would also
    make sure to pay attention to the rate limit and pick ones that would be easier
    to work with. Also, I would make sure to plan out exactly how the app would look
    and what would need to happen for that to work. The reason I struggled to write
    the main menu was because I hadn't planned it and just started to code, which was
    counterintuitive.