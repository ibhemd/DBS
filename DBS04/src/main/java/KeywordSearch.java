import java.sql.*;

public class KeywordSearch {

    static Connection conn;
    static String keyword;

    static void getData() throws SQLException {

        // print Movies
        System.out.println("MOVIES");
        // get Titles and Years
        ResultSet movies = DB_Query("" +
                "SELECT mid, title, year " +
                "FROM movie " +
                "WHERE title LIKE '%" + keyword + "%' " +
                "ORDER BY title ASC");
        while (movies.next()) {
            // hold title
            String movie = movies.getString("mid");

            // print Title and Year
            System.out.print(movies.getString("title"));
            System.out.print(", " + movies.getString("year"));

            // get Genres
            ResultSet genres = DB_Query("" +
                    "SELECT genre " +
                    "FROM genre " +
                    "WHERE movie_id = '" + movie + "'");
            while (genres.next()) {
                System.out.print(", " + genres.getString(1));
            }

            // get Actors
            System.out.print("\n");
            ResultSet movie_actors = DB_Query("" +
                    "WITH all_actors AS (SELECT * FROM actor UNION SELECT * FROM actress) " +
                    "SELECT name " +
                    "FROM all_actors " +
                    "WHERE movie_id = '" + movie + "' " +
                    "ORDER BY name ASC " +
                    "LIMIT 5;");
            while (movie_actors.next()) {
                System.out.println("\t" + movie_actors.getString("name"));
            }
        }

        // print Actors
        System.out.println("\nACTORS");
        // get Actors
        ResultSet actors = DB_Query(
                "WITH all_actors AS (SELECT * FROM actor UNION SELECT * FROM actress) " +
                "SELECT name " +
                "FROM all_actors " +
                "WHERE name LIKE '%" + keyword + "%' " +
                "GROUP BY name " +
                "ORDER BY name ASC;");
        while (actors.next()) {
            // hold name
            String name = actors.getString("name");

            // print Name
            System.out.println(name);

            // get Movies
            System.out.println("\tPLAYED IN");
            ResultSet actor_movies = DB_Query("" +
                    "WITH all_actors AS (SELECT * FROM actor UNION SELECT * FROM actress) " +
                    "SELECT title " +
                    "FROM all_actors, movie " +
                    "WHERE name = '" + name + "' " +
                    "AND movie_id = mid;");
            while (actor_movies.next()) {
                System.out.println("\t\t" + actor_movies.getString("title"));
            }

            // get Co-Stars
            System.out.println("\tCO-STARS");
            ResultSet actor_movie_actors = DB_Query("" +
                    "WITH all_actors AS (SELECT * FROM actor UNION SELECT * FROM actress), " +
                    "movies AS (SELECT movie_id FROM all_actors WHERE name = '" + name + "'), " +
                    "temp AS (SELECT name FROM all_actors, movies WHERE movies.movie_id = all_actors.movie_id) " +
                    "SELECT all_actors.name AS name, count(all_actors.name) AS count " +
                    "FROM all_actors, temp " +
                    "WHERE temp.name = all_actors.name " +
                    "AND all_actors.name <> '" + name + "' " +
                    "GROUP BY all_actors.name " +
                    "ORDER BY count ASC, name ASC " +
                    "LIMIT 5;");
            while (actor_movie_actors.next()) {
                System.out.println("\t\t" + actor_movie_actors.getString("name") + " (" + actor_movie_actors.getString("count") + ")");
            }
        }
    }

    static ResultSet DB_Query(String query) throws SQLException {
        return conn.createStatement().executeQuery(query);
    }

    public static void main(String[] args) {

        String DB_NAME = args[3];
        String DB_IP = args[5];
        String PORT = args[7];
        String USER = args[9];
        String PASSWORD = args[11];
        keyword = args[13];

        try {
            // open Connection
            String DB_URL = "jdbc:postgresql://" + DB_IP + ":" + PORT + "/" + DB_NAME;
            conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            System.out.println("Connected to database\n");

            // print Data
            getData();
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

}