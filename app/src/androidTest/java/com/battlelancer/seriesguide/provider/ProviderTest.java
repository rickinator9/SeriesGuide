package com.battlelancer.seriesguide.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.provider.ProviderTestRule;
import android.support.test.runner.AndroidJUnit4;
import com.battlelancer.seriesguide.Constants;
import com.battlelancer.seriesguide.SgApp;
import com.battlelancer.seriesguide.dataliberation.model.List;
import com.battlelancer.seriesguide.dataliberation.model.Season;
import com.battlelancer.seriesguide.dataliberation.model.Show;
import com.battlelancer.seriesguide.provider.SeriesGuideContract.Episodes;
import com.battlelancer.seriesguide.provider.SeriesGuideContract.Lists;
import com.battlelancer.seriesguide.provider.SeriesGuideContract.Movies;
import com.battlelancer.seriesguide.provider.SeriesGuideContract.Seasons;
import com.battlelancer.seriesguide.provider.SeriesGuideContract.Shows;
import com.battlelancer.seriesguide.thetvdbapi.TvdbEpisodeTools;
import com.battlelancer.seriesguide.ui.movies.MovieDetails;
import com.battlelancer.seriesguide.ui.movies.MovieTools;
import com.battlelancer.seriesguide.util.DBUtils;
import com.battlelancer.seriesguide.util.tasks.AddListTask;
import com.uwetrottmann.thetvdb.entities.Episode;
import com.uwetrottmann.tmdb2.entities.Movie;
import java.util.ArrayList;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ProviderTest {

    private static final Show SHOW;
    private static final Season SEASON;
    private static final Episode EPISODE;
    private static final com.battlelancer.seriesguide.dataliberation.model.Episode EPISODE_I;
    private static final MovieDetails MOVIE;
    private static final com.battlelancer.seriesguide.dataliberation.model.Movie MOVIE_I;

    private static final List LIST;

    static {
        SHOW = new Show();
        SHOW.tvdb_id = 12;

        SEASON = new Season();
        SEASON.tvdbId = 1234;
        SEASON.season = 42;

        EPISODE = new Episode();
        EPISODE.id = 123456;

        EPISODE_I = new com.battlelancer.seriesguide.dataliberation.model.Episode();
        EPISODE_I.tvdbId = EPISODE.id;

        LIST = new List();
        LIST.name = "Test List";
        LIST.listId = SeriesGuideContract.Lists.generateListId(LIST.name);

        MOVIE = new MovieDetails();
        Movie tmdbMovie = new Movie();
        tmdbMovie.id = 12;
        MOVIE.tmdbMovie(tmdbMovie);

        MOVIE_I = new com.battlelancer.seriesguide.dataliberation.model.Movie();
    }

    @Rule
    public ProviderTestRule providerRule = new ProviderTestRule.Builder(SeriesGuideProvider.class,
            SgApp.CONTENT_AUTHORITY).build();

    @Test
    public void showDefaultValues() throws RemoteException, OperationApplicationException {
        Context context = InstrumentationRegistry.getTargetContext();

        ContentValues values = SHOW.toContentValues(context, true);
        ContentProviderOperation op = ContentProviderOperation.newInsert(Shows.CONTENT_URI)
                .withValues(values).build();

        ArrayList<ContentProviderOperation> batch = new ArrayList<>();
        batch.add(op);
        providerRule.getResolver().applyBatch(SgApp.CONTENT_AUTHORITY, batch);

        Cursor query = providerRule.getResolver().query(Shows.CONTENT_URI, null,
                null, null, null);
        assertNotNull(query);
        assertEquals(1, query.getCount());
        assertTrue(query.moveToFirst());

        assertEquals(SHOW.tvdb_id, query.getInt(query.getColumnIndexOrThrow(Shows._ID)));
        assertNotNullValue(query, Shows.TITLE);
        assertNotNullValue(query, Shows.TITLE);
        assertNotNullValue(query, Shows.OVERVIEW);
        assertNotNullValue(query, Shows.GENRES);
        assertNotNullValue(query, Shows.NETWORK);
        assertNotNullValue(query, Shows.RUNTIME);
        assertNotNullValue(query, Shows.STATUS);
        assertNotNullValue(query, Shows.CONTENTRATING);
        assertNotNullValue(query, Shows.NEXTEPISODE);
        assertNotNullValue(query, Shows.POSTER);
        assertNotNullValue(query, Shows.NEXTTEXT);
        assertNotNullValue(query, Shows.IMDBID);
        // getInt returns 0 if NULL, so check explicitly
        assertDefaultValue(query, Shows.TRAKT_ID, 0);
        assertDefaultValue(query, Shows.FAVORITE, 0);
        assertDefaultValue(query, Shows.HEXAGON_MERGE_COMPLETE, 1);
        assertDefaultValue(query, Shows.HIDDEN, 0);
        assertDefaultValue(query, Shows.LASTUPDATED, 0);
        assertDefaultValue(query, Shows.LASTEDIT, 0);
        assertDefaultValue(query, Shows.LASTWATCHEDID, 0);
        assertDefaultValue(query, Shows.LASTWATCHED_MS, 0);
        assertNotNullValue(query, Shows.LANGUAGE);
        assertDefaultValue(query, Shows.UNWATCHED_COUNT, DBUtils.UNKNOWN_UNWATCHED_COUNT);
        assertDefaultValue(query, Shows.NOTIFY, 1);


        query.close();
    }

    @Test
    public void seasonDefaultValues() throws RemoteException, OperationApplicationException {
        ContentProviderOperation op = DBUtils
                .buildSeasonOp(SHOW.tvdb_id, SEASON.tvdbId, SEASON.season, true);
        insertAndAssertSeason(op);
    }

    @Test
    public void seasonDefaultValuesImport() throws RemoteException, OperationApplicationException {
        ContentValues values = SEASON.toContentValues(SHOW.tvdb_id);

        ContentProviderOperation op = ContentProviderOperation.newInsert(Seasons.CONTENT_URI)
                .withValues(values).build();

        insertAndAssertSeason(op);
    }

    private void insertAndAssertSeason(ContentProviderOperation op)
            throws RemoteException, OperationApplicationException {
        // note how this does not cause a foreign key constraint failure
        ArrayList<ContentProviderOperation> batch = new ArrayList<>();
        batch.add(op);
        providerRule.getResolver().applyBatch(SgApp.CONTENT_AUTHORITY, batch);

        Cursor query = providerRule.getResolver().query(Seasons.CONTENT_URI, null,
                null, null, null);
        assertNotNull(query);
        assertEquals(1, query.getCount());
        assertTrue(query.moveToFirst());

        assertEquals(SEASON.tvdbId, query.getInt(query.getColumnIndexOrThrow(Seasons._ID)));
        assertEquals(SHOW.tvdb_id, query.getInt(query.getColumnIndexOrThrow(Shows.REF_SHOW_ID)));
        assertEquals(SEASON.season, query.getInt(query.getColumnIndexOrThrow(Seasons.COMBINED)));
        // getInt returns 0 if NULL, so check explicitly
        assertDefaultValue(query, Seasons.WATCHCOUNT, 0);
        assertDefaultValue(query, Seasons.UNAIREDCOUNT, 0);
        assertDefaultValue(query, Seasons.NOAIRDATECOUNT, 0);
        assertDefaultValue(query, Seasons.TOTALCOUNT, 0);

        query.close();
    }

    @Test
    public void episodeDefaultValues() throws RemoteException, OperationApplicationException {
        ContentValues values = new ContentValues();
        TvdbEpisodeTools.toContentValues(EPISODE, values,
                EPISODE.id, 1234, SHOW.tvdb_id, 0,
                Constants.EPISODE_UNKNOWN_RELEASE);

        insertAndAssertEpisode(values);
    }

    @Test
    public void episodeDefaultValuesImport() throws RemoteException, OperationApplicationException {
        ContentValues values = EPISODE_I.toContentValues(SHOW.tvdb_id, 1234, 0);

        insertAndAssertEpisode(values);
    }

    private void insertAndAssertEpisode(ContentValues values) throws RemoteException, OperationApplicationException{
        ContentProviderOperation op = ContentProviderOperation.newInsert(Episodes.CONTENT_URI)
                .withValues(values).build();

        ArrayList<ContentProviderOperation> batch = new ArrayList<>();
        batch.add(op);
        providerRule.getResolver().applyBatch(SgApp.CONTENT_AUTHORITY, batch);

        Cursor query = providerRule.getResolver().query(Episodes.CONTENT_URI, null,
                null, null, null);
        assertNotNull(query);
        assertEquals(1, query.getCount());
        assertTrue(query.moveToFirst());

        assertNotNullValue(query, Episodes.TITLE);
        assertDefaultValue(query, Episodes.NUMBER, 0);
        assertDefaultValue(query, Episodes.WATCHED, 0);
        assertNotNullValue(query, Episodes.DIRECTORS);
        assertNotNullValue(query, Episodes.GUESTSTARS);
        assertNotNullValue(query, Episodes.WRITERS);
        assertNotNullValue(query, Episodes.IMAGE);
        assertDefaultValue(query, Episodes.COLLECTED, 0);
        assertNotNullValue(query, Episodes.IMDBID);
        assertDefaultValue(query, Episodes.LAST_EDITED, 0);
        assertDefaultValue(query, Episodes.LAST_UPDATED, 0);

        query.close();
    }

    @Test
    public void listDefaultValues() {
        AddListTask addListTask = new AddListTask(InstrumentationRegistry.getTargetContext(),
                LIST.name);
        addListTask.doDatabaseUpdate(providerRule.getResolver(), addListTask.getListId());

        Cursor query = providerRule.getResolver().query(Lists.CONTENT_URI, null,
                null, null, null);
        assertNotNull(query);
        assertEquals(1, query.getCount());
        assertTrue(query.moveToFirst());

        assertDefaultValue(query, Lists.ORDER, 0);

        query.close();
    }

    @Test
    public void listDefaultValuesImport() throws RemoteException, OperationApplicationException {
        ContentValues values = LIST.toContentValues();

        ContentProviderOperation op = ContentProviderOperation.newInsert(Lists.CONTENT_URI)
                .withValues(values).build();

        ArrayList<ContentProviderOperation> batch = new ArrayList<>();
        batch.add(op);
        providerRule.getResolver().applyBatch(SgApp.CONTENT_AUTHORITY, batch);

        Cursor query = providerRule.getResolver().query(Lists.CONTENT_URI, null,
                null, null, null);
        assertNotNull(query);
        assertEquals(1, query.getCount());
        assertTrue(query.moveToFirst());

        assertDefaultValue(query, Lists.ORDER, 0);

        query.close();
    }

    @Test
    public void movieDefaultValues() {
        ContentValues values = MOVIE.toContentValuesInsert();
        providerRule.getResolver().insert(Movies.CONTENT_URI, values);

        assertMovie(false);
    }

    @Test
    public void movieDefaultValuesWatchedShell() {
        MovieTools.addMovieWatchedShell(providerRule.getResolver(), MOVIE.tmdbMovie().id);

        assertMovie(true);
    }

    @Test
    public void movieDefaultValuesImport() {
        providerRule.getResolver().insert(Movies.CONTENT_URI, MOVIE_I.toContentValues());

        assertMovie(false);
    }

    private void assertMovie(boolean isWatched) {
        Cursor query = providerRule.getResolver().query(Movies.CONTENT_URI, null,
                null, null, null);
        assertNotNull(query);
        assertEquals(1, query.getCount());
        assertTrue(query.moveToFirst());

        assertDefaultValue(query, Movies.RUNTIME_MIN, 0);
        assertDefaultValue(query, Movies.IN_COLLECTION, 0);
        assertDefaultValue(query, Movies.IN_WATCHLIST, 0);
        assertDefaultValue(query, Movies.PLAYS, 0);
        assertDefaultValue(query, Movies.WATCHED, isWatched ? 1 : 0);
        assertDefaultValue(query, Movies.RATING_TMDB, 0);
        assertDefaultValue(query, Movies.RATING_VOTES_TMDB, 0);
        assertDefaultValue(query, Movies.RATING_TRAKT, 0);
        assertDefaultValue(query, Movies.RATING_VOTES_TRAKT, 0);

        query.close();
    }

    private void assertNotNullValue(Cursor query, String column) {
        assertFalse(query.isNull(query.getColumnIndexOrThrow(column)));
    }

    private void assertDefaultValue(Cursor query, String column, int defaultValue) {
        assertNotNullValue(query, column);
        assertEquals(defaultValue, query.getInt(query.getColumnIndexOrThrow(column)));
    }
}
