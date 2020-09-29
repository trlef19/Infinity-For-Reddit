package ml.docilealligator.infinityforreddit;

import android.content.Context;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import ml.docilealligator.infinityforreddit.Account.Account;
import ml.docilealligator.infinityforreddit.Account.AccountDao;
import ml.docilealligator.infinityforreddit.CustomTheme.CustomTheme;
import ml.docilealligator.infinityforreddit.CustomTheme.CustomThemeDao;
import ml.docilealligator.infinityforreddit.MultiReddit.MultiReddit;
import ml.docilealligator.infinityforreddit.MultiReddit.MultiRedditDao;
import ml.docilealligator.infinityforreddit.RecentSearchQuery.RecentSearchQuery;
import ml.docilealligator.infinityforreddit.RecentSearchQuery.RecentSearchQueryDao;
import ml.docilealligator.infinityforreddit.Subreddit.SubredditDao;
import ml.docilealligator.infinityforreddit.Subreddit.SubredditData;
import ml.docilealligator.infinityforreddit.SubscribedSubreddit.SubscribedSubredditDao;
import ml.docilealligator.infinityforreddit.SubscribedSubreddit.SubscribedSubredditData;
import ml.docilealligator.infinityforreddit.SubscribedUserDatabase.SubscribedUserDao;
import ml.docilealligator.infinityforreddit.SubscribedUserDatabase.SubscribedUserData;
import ml.docilealligator.infinityforreddit.User.UserDao;
import ml.docilealligator.infinityforreddit.User.UserData;

@Database(entities = {Account.class, SubredditData.class, SubscribedSubredditData.class, UserData.class,
        SubscribedUserData.class, MultiReddit.class, CustomTheme.class, RecentSearchQuery.class}, version = 10)
public abstract class RedditDataRoomDatabase extends RoomDatabase {
    private static RedditDataRoomDatabase INSTANCE;

    public static RedditDataRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (RedditDataRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            RedditDataRoomDatabase.class, "reddit_data")
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5,
                                    MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9,
                                    MIGRATION_9_10)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract AccountDao accountDao();

    public abstract SubredditDao subredditDao();

    public abstract SubscribedSubredditDao subscribedSubredditDao();

    public abstract UserDao userDao();

    public abstract SubscribedUserDao subscribedUserDao();

    public abstract MultiRedditDao multiRedditDao();

    public abstract CustomThemeDao customThemeDao();

    public abstract RecentSearchQueryDao recentSearchQueryDao();

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE subscribed_subreddits"
                    + " ADD COLUMN is_favorite INTEGER DEFAULT 0 NOT NULL");
            database.execSQL("ALTER TABLE subscribed_users"
                    + " ADD COLUMN is_favorite INTEGER DEFAULT 0 NOT NULL");
        }
    };

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE subscribed_subreddits_temp " +
                    "(id TEXT NOT NULL, name TEXT, icon TEXT, username TEXT NOT NULL, " +
                    "is_favorite INTEGER NOT NULL, PRIMARY KEY(id, username), " +
                    "FOREIGN KEY(username) REFERENCES accounts(username) ON DELETE CASCADE)");
            database.execSQL(
                    "INSERT INTO subscribed_subreddits_temp SELECT * FROM subscribed_subreddits");
            database.execSQL("DROP TABLE subscribed_subreddits");
            database.execSQL("ALTER TABLE subscribed_subreddits_temp RENAME TO subscribed_subreddits");

            database.execSQL("CREATE TABLE subscribed_users_temp " +
                    "(name TEXT NOT NULL, icon TEXT, username TEXT NOT NULL, " +
                    "is_favorite INTEGER NOT NULL, PRIMARY KEY(name, username), " +
                    "FOREIGN KEY(username) REFERENCES accounts(username) ON DELETE CASCADE)");
            database.execSQL(
                    "INSERT INTO subscribed_users_temp SELECT * FROM subscribed_users");
            database.execSQL("DROP TABLE subscribed_users");
            database.execSQL("ALTER TABLE subscribed_users_temp RENAME TO subscribed_users");
        }
    };

    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE multi_reddits" +
                    "(path TEXT NOT NULL, username TEXT NOT NULL, name TEXT NOT NULL, " +
                    "display_name TEXT NOT NULL, description TEXT, copied_from TEXT, " +
                    "n_subscribers INTEGER NOT NULL, icon_url TEXT, created_UTC INTEGER NOT NULL, " +
                    "visibility TEXT, over_18 INTEGER NOT NULL, is_subscriber INTEGER NOT NULL, " +
                    "is_favorite INTEGER NOT NULL, PRIMARY KEY(path, username), " +
                    "FOREIGN KEY(username) REFERENCES accounts(username) ON DELETE CASCADE)");
        }
    };

    private static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE subreddits"
                    + " ADD COLUMN sidebar_description TEXT");
        }
    };

    private static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE custom_themes" +
                    "(name TEXT NOT NULL PRIMARY KEY, is_light_theme INTEGER NOT NULL," +
                    "is_dark_theme INTEGER NOT NULL, is_amoled_theme INTEGER NOT NULL, color_primary INTEGER NOT NULL," +
                    "color_primary_dark INTEGER NOT NULL, color_accent INTEGER NOT NULL," +
                    "color_primary_light_theme INTEGER NOT NULL, primary_text_color INTEGER NOT NULL," +
                    "secondary_text_color INTEGER NOT NULL, post_title_color INTEGER NOT NULL," +
                    "post_content_color INTEGER NOT NULL, comment_color INTEGER NOT NULL," +
                    "button_text_color INTEGER NOT NULL, background_color INTEGER NOT NULL," +
                    "card_view_background_color INTEGER NOT NULL, comment_background_color INTEGER NOT NULL," +
                    "bottom_app_bar_background_color INTEGER NOT NULL, primary_icon_color INTEGER NOT NULL," +
                    "post_icon_and_info_color INTEGER NOT NULL," +
                    "comment_icon_and_info_color INTEGER NOT NULL, toolbar_primary_text_and_icon_color INTEGER NOT NULL," +
                    "toolbar_secondary_text_color INTEGER NOT NULL, circular_progress_bar_background INTEGER NOT NULL," +
                    "tab_layout_with_expanded_collapsing_toolbar_tab_background INTEGER NOT NULL," +
                    "tab_layout_with_expanded_collapsing_toolbar_text_color INTEGER NOT NULL," +
                    "tab_layout_with_expanded_collapsing_toolbar_tab_indicator INTEGER NOT NULL," +
                    "tab_layout_with_collapsed_collapsing_toolbar_tab_background INTEGER NOT NULL," +
                    "tab_layout_with_collapsed_collapsing_toolbar_text_color INTEGER NOT NULL," +
                    "tab_layout_with_collapsed_collapsing_toolbar_tab_indicator INTEGER NOT NULL," +
                    "nav_bar_color INTEGER NOT NULL, upvoted INTEGER NOT NULL, downvoted INTEGER NOT NULL," +
                    "post_type_background_color INTEGER NOT NULL, post_type_text_color INTEGER NOT NULL," +
                    "spoiler_background_color INTEGER NOT NULL, spoiler_text_color INTEGER NOT NULL," +
                    "nsfw_background_color INTEGER NOT NULL, nsfw_text_color INTEGER NOT NULL," +
                    "flair_background_color INTEGER NOT NULL, flair_text_color INTEGER NOT NULL," +
                    "archived_tint INTEGER NOT NULL, locked_icon_tint INTEGER NOT NULL," +
                    "crosspost_icon_tint INTEGER NOT NULL, stickied_post_icon_tint INTEGER NOT NULL, subscribed INTEGER NOT NULL," +
                    "unsubscribed INTEGER NOT NULL, username INTEGER NOT NULL, subreddit INTEGER NOT NULL," +
                    "author_flair_text_color INTEGER NOT NULL, submitter INTEGER NOT NULL," +
                    "moderator INTEGER NOT NULL, single_comment_thread_background_color INTEGER NOT NULL," +
                    "unread_message_background_color INTEGER NOT NULL, divider_color INTEGER NOT NULL," +
                    "no_preview_link_background_color INTEGER NOT NULL," +
                    "vote_and_reply_unavailable_button_color INTEGER NOT NULL," +
                    "comment_vertical_bar_color_1 INTEGER NOT NULL, comment_vertical_bar_color_2 INTEGER NOT NULL," +
                    "comment_vertical_bar_color_3 INTEGER NOT NULL, comment_vertical_bar_color_4 INTEGER NOT NULL," +
                    "comment_vertical_bar_color_5 INTEGER NOT NULL, comment_vertical_bar_color_6 INTEGER NOT NULL," +
                    "comment_vertical_bar_color_7 INTEGER NOT NULL, fab_icon_color INTEGER NOT NULL," +
                    "chip_text_color INTEGER NOT NULL, is_light_status_bar INTEGER NOT NULL," +
                    "is_light_nav_bar INTEGER NOT NULL," +
                    "is_change_status_bar_icon_color_after_toolbar_collapsed_in_immersive_interface INTEGER NOT NULL)");
        }
    };

    private static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE custom_themes ADD COLUMN awards_background_color INTEGER DEFAULT " + Color.parseColor("#EEAB02") + " NOT NULL");
            database.execSQL("ALTER TABLE custom_themes ADD COLUMN awards_text_color INTEGER DEFAULT " + Color.parseColor("#FFFFFF") + " NOT NULL");
        }
    };

    private static final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE users_temp " +
                    "(name TEXT NOT NULL PRIMARY KEY, icon TEXT, banner TEXT, " +
                    "link_karma INTEGER NOT NULL, comment_karma INTEGER DEFAULT 0 NOT NULL, created_utc INTEGER DEFAULT 0 NOT NULL," +
                    "is_gold INTEGER NOT NULL, is_friend INTEGER NOT NULL, can_be_followed INTEGER NOT NULL," +
                    "description TEXT)");
            database.execSQL(
                    "INSERT INTO users_temp(name, icon, banner, link_karma, is_gold, is_friend, can_be_followed) SELECT * FROM users");
            database.execSQL("DROP TABLE users");
            database.execSQL("ALTER TABLE users_temp RENAME TO users");

            database.execSQL("ALTER TABLE subreddits"
                    + " ADD COLUMN created_utc INTEGER DEFAULT 0 NOT NULL");

            database.execSQL("ALTER TABLE custom_themes"
                    + " ADD COLUMN bottom_app_bar_icon_color INTEGER DEFAULT " + Color.parseColor("#000000") + " NOT NULL");
        }
    };

    private static final Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE custom_themes"
                    + " ADD COLUMN link_color INTEGER DEFAULT " + Color.parseColor("#FF4081") + " NOT NULL");
            database.execSQL("ALTER TABLE custom_themes"
                    + " ADD COLUMN received_message_text_color INTEGER DEFAULT " + Color.parseColor("#FFFFFF") + " NOT NULL");
            database.execSQL("ALTER TABLE custom_themes"
                    + " ADD COLUMN sent_message_text_color INTEGER DEFAULT " + Color.parseColor("#FFFFFF") + " NOT NULL");
            database.execSQL("ALTER TABLE custom_themes"
                    + " ADD COLUMN received_message_background_color INTEGER DEFAULT " + Color.parseColor("#4185F4") + " NOT NULL");
            database.execSQL("ALTER TABLE custom_themes"
                    + " ADD COLUMN sent_message_background_color INTEGER DEFAULT " + Color.parseColor("#31BF7D") + " NOT NULL");
            database.execSQL("ALTER TABLE custom_themes"
                    + " ADD COLUMN send_message_icon_color INTEGER DEFAULT " + Color.parseColor("#4185F4") + " NOT NULL");
            database.execSQL("ALTER TABLE custom_themes"
                    + " ADD COLUMN fully_collapsed_comment_background_color INTEGER DEFAULT " + Color.parseColor("#8EDFBA") + " NOT NULL");
            database.execSQL("ALTER TABLE custom_themes"
                    + " ADD COLUMN awarded_comment_background_color INTEGER DEFAULT " + Color.parseColor("#FFF162") + " NOT NULL");

        }
    };

    private static final Migration MIGRATION_9_10 = new Migration(9, 10) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE recent_search_queries" +
                    "(username TEXT NOT NULL, search_query TEXT NOT NULL, PRIMARY KEY(username, search_query), " +
                    "FOREIGN KEY(username) REFERENCES accounts(username) ON DELETE CASCADE)");

            database.execSQL("ALTER TABLE subreddits"
                    + " ADD COLUMN suggested_comment_sort TEXT");

            database.execSQL("ALTER TABLE subreddits"
                    + " ADD COLUMN over18 INTEGER DEFAULT 0 NOT NULL");
        }
    };
}
