# Popular Movies

A mobile client for The Movie Database that shows movie posters and details for popular and top-rated movies.

## Running

To download and run this code from your own Android developer tools, you'll need to [obtain an API key](https://developers.themoviedb.org/3/getting-started) from The Movie Database.

Once you have an API key, clone this repository locally to a directory of your choosing.  

Go to the `.gradle` directory within your home directory:

```sh
cd ~/.gradle
```

In your `.gradle` directory, if you don't already have a file called `gradle.properties`, make one. Otherwise, you can skip this step.

```sh
touch gradle.properties
```

Include your API key in that `gradle.properties` as follows:

```sh
TmdbApiKey="YOUR_API_KEY_HERE"
```

You should now be able to build your project using Gradle.
