package com.battlelancer.seriesguide.ui;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import com.battlelancer.seriesguide.R;
import com.battlelancer.seriesguide.provider.SeriesGuideContract;
import com.battlelancer.seriesguide.provider.SeriesGuideDatabase;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * To avoid flakiness, turn off system animations on the virtual or physical devices used for
 * testing. On your device, under Settings > Developer options, disable the following 3 settings:
 *
 * - Window animation scale
 * - Transition animation scale
 * - Animator duration scale
 */
@RunWith(AndroidJUnit4.class)
public class ShowsActivityTest {

    @Rule
    public ActivityTestRule<ShowsActivity> mActivityTestRule = new ActivityTestRule<>(
            ShowsActivity.class);

    @Before
    public void setUp() {
        // delete the database and close the database helper inside the provider
        // to ensure a clean state for the add show test
        Context context = InstrumentationRegistry.getTargetContext();
        context.deleteDatabase(SeriesGuideDatabase.DATABASE_NAME);
        context.getContentResolver().query(SeriesGuideContract.Shows.CONTENT_URI_CLOSE,
                null, null, null, null);
    }

    @Test
    public void testAddShowAndSetWatchedThenReturn() throws InterruptedException {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(5000);

        ViewInteraction floatingActionButton = onView(
                allOf(withId(R.id.buttonShowsAdd), withContentDescription("Add show"),
                        childAtPosition(
                                allOf(withId(R.id.rootLayoutShows),
                                        childAtPosition(
                                                withId(R.id.drawer_layout),
                                                0)),
                                2),
                        isDisplayed()));
        floatingActionButton.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(700);

        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.recyclerViewShowsDiscover),
                        childAtPosition(
                                withId(R.id.constraintLayoutShowsDiscover),
                                1)));
        recyclerView.perform(actionOnItemAtPosition(2, click()));

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.buttonPositive), withText("Add show"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        2),
                                2),
                        isDisplayed()));
        appCompatButton.perform(click());

        ViewInteraction recyclerView2 = onView(
                allOf(withId(R.id.recyclerViewShowsDiscover),
                        childAtPosition(
                                withId(R.id.constraintLayoutShowsDiscover),
                                1)));
        recyclerView2.perform(actionOnItemAtPosition(2, click()));

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(1000);

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.buttonEpisodeWatched), withText("Set watched"),
                        withContentDescription("Set watched"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.buttonbar),
                                        0),
                                0),
                        isDisplayed()));
        appCompatButton2.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(200);

        pressBack();

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(700);

        pressBack();
    }

    private static Matcher<View> childAtPosition(final Matcher<View> parentMatcher,
            final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
