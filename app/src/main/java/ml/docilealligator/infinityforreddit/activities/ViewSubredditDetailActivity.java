package ml.docilealligator.infinityforreddit.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import ml.docilealligator.infinityforreddit.ActivityToolbarInterface;
import ml.docilealligator.infinityforreddit.AppBarStateChangeListener;
import ml.docilealligator.infinityforreddit.FragmentCommunicator;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.MarkPostAsReadInterface;
import ml.docilealligator.infinityforreddit.PostFragmentContentScrollingInterface;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.SortType;
import ml.docilealligator.infinityforreddit.SortTypeSelectionCallback;
import ml.docilealligator.infinityforreddit.asynctasks.AddSubredditOrUserToMultiReddit;
import ml.docilealligator.infinityforreddit.asynctasks.CheckIsSubscribedToSubredditAsyncTask;
import ml.docilealligator.infinityforreddit.asynctasks.GetCurrentAccount;
import ml.docilealligator.infinityforreddit.asynctasks.InsertSubredditDataAsyncTask;
import ml.docilealligator.infinityforreddit.asynctasks.SwitchAccountAsyncTask;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.FABMoreOptionsBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.PostLayoutBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.PostTypeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.RandomBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.SortTimeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.SortTypeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.events.ChangeNSFWEvent;
import ml.docilealligator.infinityforreddit.events.GoBackToMainPageEvent;
import ml.docilealligator.infinityforreddit.events.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.fragments.PostFragment;
import ml.docilealligator.infinityforreddit.fragments.SidebarFragment;
import ml.docilealligator.infinityforreddit.message.ReadMessage;
import ml.docilealligator.infinityforreddit.multireddit.MultiReddit;
import ml.docilealligator.infinityforreddit.post.Post;
import ml.docilealligator.infinityforreddit.post.PostDataSource;
import ml.docilealligator.infinityforreddit.readpost.InsertReadPost;
import ml.docilealligator.infinityforreddit.subreddit.FetchSubredditData;
import ml.docilealligator.infinityforreddit.subreddit.SubredditData;
import ml.docilealligator.infinityforreddit.subreddit.SubredditSubscription;
import ml.docilealligator.infinityforreddit.subreddit.SubredditViewModel;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import pl.droidsonroids.gif.GifImageView;
import retrofit2.Retrofit;

public class ViewSubredditDetailActivity extends BaseActivity implements SortTypeSelectionCallback,
        PostTypeBottomSheetFragment.PostTypeSelectionCallback, PostLayoutBottomSheetFragment.PostLayoutSelectionCallback,
        ActivityToolbarInterface, FABMoreOptionsBottomSheetFragment.FABOptionSelectionCallback,
        RandomBottomSheetFragment.RandomOptionSelectionCallback, MarkPostAsReadInterface, PostFragmentContentScrollingInterface {

    public static final String EXTRA_SUBREDDIT_NAME_KEY = "ESN";
    public static final String EXTRA_MESSAGE_FULLNAME = "ENF";
    public static final String EXTRA_NEW_ACCOUNT_NAME = "ENAN";
    public static final String EXTRA_VIEW_SIDEBAR = "EVSB";

    private static final String FETCH_SUBREDDIT_INFO_STATE = "FSIS";
    private static final String CURRENT_ONLINE_SUBSCRIBERS_STATE = "COSS";
    private static final String IS_IN_LAZY_MODE_STATE = "IILMS";
    private static final String NULL_ACCESS_TOKEN_STATE = "NATS";
    private static final String ACCESS_TOKEN_STATE = "ATS";
    private static final String ACCOUNT_NAME_STATE = "ANS";
    private static final String MESSAGE_FULLNAME_STATE = "MFS";
    private static final String NEW_ACCOUNT_NAME_STATE = "NANS";
    private static final int ADD_TO_MULTIREDDIT_REQUEST_CODE = 1;

    @BindView(R.id.coordinator_layout_view_subreddit_detail_activity)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.view_pager_view_subreddit_detail_activity)
    ViewPager2 viewPager2;
    @BindView(R.id.appbar_layout_view_subreddit_detail_activity)
    AppBarLayout appBarLayout;
    @BindView(R.id.collapsing_toolbar_layout_view_subreddit_detail_activity)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.toolbar_linear_layout_view_subreddit_detail_activity)
    LinearLayout linearLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tab_layout_view_subreddit_detail_activity)
    TabLayout tabLayout;
    @BindView(R.id.banner_image_view_view_subreddit_detail_activity)
    GifImageView bannerImageView;
    @BindView(R.id.icon_gif_image_view_view_subreddit_detail_activity)
    GifImageView iconGifImageView;
    @BindView(R.id.subscribe_subreddit_chip_view_subreddit_detail_activity)
    Chip subscribeSubredditChip;
    @BindView(R.id.subreddit_name_text_view_view_subreddit_detail_activity)
    TextView subredditNameTextView;
    @BindView(R.id.subscriber_count_text_view_view_subreddit_detail_activity)
    TextView nSubscribersTextView;
    @BindView(R.id.online_subscriber_count_text_view_view_subreddit_detail_activity)
    TextView nOnlineSubscribersTextView;
    @BindView(R.id.since_text_view_view_subreddit_detail_activity)
    TextView sinceTextView;
    @BindView(R.id.creation_time_text_view_view_subreddit_detail_activity)
    TextView creationTimeTextView;
    @BindView(R.id.description_text_view_view_subreddit_detail_activity)
    TextView descriptionTextView;
    @BindView(R.id.bottom_app_bar_bottom_app_bar)
    BottomAppBar bottomNavigationView;
    @BindView(R.id.linear_layout_bottom_app_bar)
    LinearLayout linearLayoutBottomAppBar;
    @BindView(R.id.option_1_bottom_app_bar)
    ImageView option1BottomAppBar;
    @BindView(R.id.option_2_bottom_app_bar)
    ImageView option2BottomAppBar;
    @BindView(R.id.option_3_bottom_app_bar)
    ImageView option3BottomAppBar;
    @BindView(R.id.option_4_bottom_app_bar)
    ImageView option4BottomAppBar;
    @BindView(R.id.fab_view_subreddit_detail_activity)
    FloatingActionButton fab;
    @Inject
    @Named("no_oauth")
    Retrofit mRetrofit;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    @Named("sort_type")
    SharedPreferences mSortTypeSharedPreferences;
    @Inject
    @Named("nsfw_and_spoiler")
    SharedPreferences mNsfwAndSpoilerSharedPreferences;
    @Inject
    @Named("post_layout")
    SharedPreferences mPostLayoutSharedPreferences;
    @Inject
    @Named("bottom_app_bar")
    SharedPreferences bottomAppBarSharedPreference;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    @Inject
    Executor mExecutor;
    public SubredditViewModel mSubredditViewModel;
    private FragmentManager fragmentManager;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private boolean mNullAccessToken = false;
    private String mAccessToken;
    private String mAccountName;
    private String subredditName;
    private boolean mFetchSubredditInfoSuccess = false;
    private int mNCurrentOnlineSubscribers = 0;
    private boolean subscriptionReady = false;
    private boolean isInLazyMode = false;
    private boolean showToast = false;
    private boolean showBottomAppBar;
    private boolean lockBottomAppBar;
    private String mMessageFullname;
    private String mNewAccountName;
    private RequestManager glide;
    private Menu mMenu;
    private AppBarLayout.LayoutParams params;
    private int expandedTabTextColor;
    private int expandedTabBackgroundColor;
    private int expandedTabIndicatorColor;
    private int collapsedTabTextColor;
    private int collapsedTabBackgroundColor;
    private int collapsedTabIndicatorColor;
    private int unsubscribedColor;
    private int subscribedColor;
    private int fabOption;
    private SlidrInterface mSlidrInterface;
    private MaterialAlertDialogBuilder nsfwWarningBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);
        setTransparentStatusBarAfterToolbarCollapsed();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_subreddit_detail);

        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
            mSlidrInterface = Slidr.attach(this);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            if (isImmersiveInterface()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    coordinatorLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
                } else {
                    window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                }

                int navBarHeight = getNavBarHeight();
                if (navBarHeight > 0) {
                    CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
                    params.bottomMargin += navBarHeight;
                    fab.setLayoutParams(params);
                    bottomNavigationView.setPadding(0, 0, 0, navBarHeight);
                }

                showToast = true;
            }

            View decorView = window.getDecorView();
            if (isChangeStatusBarIconColor()) {
                appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
                    @Override
                    public void onStateChanged(AppBarLayout appBarLayout, AppBarStateChangeListener.State state) {
                        if (state == State.COLLAPSED) {
                            decorView.setSystemUiVisibility(getSystemVisibilityToolbarCollapsed());
                            tabLayout.setTabTextColors(collapsedTabTextColor, collapsedTabTextColor);
                            tabLayout.setSelectedTabIndicatorColor(collapsedTabIndicatorColor);
                            tabLayout.setBackgroundColor(collapsedTabBackgroundColor);
                        } else if (state == State.EXPANDED) {
                            decorView.setSystemUiVisibility(getSystemVisibilityToolbarExpanded());
                            tabLayout.setTabTextColors(expandedTabTextColor, expandedTabTextColor);
                            tabLayout.setSelectedTabIndicatorColor(expandedTabIndicatorColor);
                            tabLayout.setBackgroundColor(expandedTabBackgroundColor);
                        }
                    }
                });
            } else {
                appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
                    @Override
                    public void onStateChanged(AppBarLayout appBarLayout, State state) {
                        if (state == State.COLLAPSED) {
                            tabLayout.setTabTextColors(collapsedTabTextColor, collapsedTabTextColor);
                            tabLayout.setSelectedTabIndicatorColor(collapsedTabIndicatorColor);
                            tabLayout.setBackgroundColor(collapsedTabBackgroundColor);
                        } else if (state == State.EXPANDED) {
                            tabLayout.setTabTextColors(expandedTabTextColor, expandedTabTextColor);
                            tabLayout.setSelectedTabIndicatorColor(expandedTabIndicatorColor);
                            tabLayout.setBackgroundColor(expandedTabBackgroundColor);
                        }
                    }
                });
            }
        } else {
            appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
                @Override
                public void onStateChanged(AppBarLayout appBarLayout, State state) {
                    if (state == State.EXPANDED) {
                        tabLayout.setTabTextColors(expandedTabTextColor, expandedTabTextColor);
                        tabLayout.setSelectedTabIndicatorColor(expandedTabIndicatorColor);
                        tabLayout.setBackgroundColor(expandedTabBackgroundColor);
                    } else if (state == State.COLLAPSED) {
                        tabLayout.setTabTextColors(collapsedTabTextColor, collapsedTabTextColor);
                        tabLayout.setSelectedTabIndicatorColor(collapsedTabIndicatorColor);
                        tabLayout.setBackgroundColor(collapsedTabBackgroundColor);
                    }
                }
            });
        }

        showBottomAppBar = mSharedPreferences.getBoolean(SharedPreferencesUtils.BOTTOM_APP_BAR_KEY, false);
        lockBottomAppBar = mSharedPreferences.getBoolean(SharedPreferencesUtils.LOCK_BOTTOM_APP_BAR, false);
        boolean hideSubredditDescription = mSharedPreferences.getBoolean(SharedPreferencesUtils.HIDE_SUBREDDIT_DESCRIPTION, false);

        subredditName = getIntent().getStringExtra(EXTRA_SUBREDDIT_NAME_KEY);

        fragmentManager = getSupportFragmentManager();

        if (savedInstanceState == null) {
            mMessageFullname = getIntent().getStringExtra(EXTRA_MESSAGE_FULLNAME);
            mNewAccountName = getIntent().getStringExtra(EXTRA_NEW_ACCOUNT_NAME);
            getCurrentAccountAndBindView();
        } else {
            mFetchSubredditInfoSuccess = savedInstanceState.getBoolean(FETCH_SUBREDDIT_INFO_STATE);
            mNCurrentOnlineSubscribers = savedInstanceState.getInt(CURRENT_ONLINE_SUBSCRIBERS_STATE);
            mNullAccessToken = savedInstanceState.getBoolean(NULL_ACCESS_TOKEN_STATE);
            mAccessToken = savedInstanceState.getString(ACCESS_TOKEN_STATE);
            mAccountName = savedInstanceState.getString(ACCOUNT_NAME_STATE);
            isInLazyMode = savedInstanceState.getBoolean(IS_IN_LAZY_MODE_STATE);
            mMessageFullname = savedInstanceState.getString(MESSAGE_FULLNAME_STATE);
            mNewAccountName = savedInstanceState.getString(NEW_ACCOUNT_NAME_STATE);

            if (!mNullAccessToken && mAccessToken == null) {
                getCurrentAccountAndBindView();
            } else {
                bindView();
            }

            if (mFetchSubredditInfoSuccess) {
                nOnlineSubscribersTextView.setText(getString(R.string.online_subscribers_number_detail, mNCurrentOnlineSubscribers));
            }
        }

        fetchSubredditData();

        params = (AppBarLayout.LayoutParams) collapsingToolbarLayout.getLayoutParams();

        String title = "r/" + subredditName;
        subredditNameTextView.setText(title);

        toolbar.setTitle(title);
        adjustToolbar(toolbar);
        setSupportActionBar(toolbar);
        setToolbarGoToTop(toolbar);

        glide = Glide.with(this);
        Locale locale = getResources().getConfiguration().locale;

        mSubredditViewModel = new ViewModelProvider(this,
                new SubredditViewModel.Factory(getApplication(), mRedditDataRoomDatabase, subredditName))
                .get(SubredditViewModel.class);
        mSubredditViewModel.getSubredditLiveData().observe(this, subredditData -> {
            if (subredditData != null) {
                if (subredditData.getBannerUrl().equals("")) {
                    iconGifImageView.setOnClickListener(view -> {
                        //Do nothing as it has no image
                    });
                } else {
                    glide.load(subredditData.getBannerUrl()).into(bannerImageView);
                    bannerImageView.setOnClickListener(view -> {
                        Intent intent = new Intent(ViewSubredditDetailActivity.this, ViewImageOrGifActivity.class);
                        intent.putExtra(ViewImageOrGifActivity.EXTRA_IMAGE_URL_KEY, subredditData.getBannerUrl());
                        intent.putExtra(ViewImageOrGifActivity.EXTRA_FILE_NAME_KEY, subredditName + "-banner.jpg");
                        intent.putExtra(ViewImageOrGifActivity.EXTRA_SUBREDDIT_OR_USERNAME_KEY, subredditName);
                        startActivity(intent);
                    });
                }

                if (subredditData.getIconUrl().equals("")) {
                    glide.load(getDrawable(R.drawable.subreddit_default_icon))
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(216, 0)))
                            .into(iconGifImageView);
                    iconGifImageView.setOnClickListener(view -> {
                        //Do nothing as it is a default icon
                    });
                } else {
                    glide.load(subredditData.getIconUrl())
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(216, 0)))
                            .error(glide.load(R.drawable.subreddit_default_icon)
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(216, 0))))
                            .into(iconGifImageView);
                    iconGifImageView.setOnClickListener(view -> {
                        Intent intent = new Intent(ViewSubredditDetailActivity.this, ViewImageOrGifActivity.class);
                        intent.putExtra(ViewImageOrGifActivity.EXTRA_IMAGE_URL_KEY, subredditData.getIconUrl());
                        intent.putExtra(ViewImageOrGifActivity.EXTRA_FILE_NAME_KEY, subredditName + "-icon.jpg");
                        intent.putExtra(ViewImageOrGifActivity.EXTRA_SUBREDDIT_OR_USERNAME_KEY, subredditName);
                        startActivity(intent);
                    });
                }

                String subredditFullName = "r/" + subredditData.getName();
                if (!title.equals(subredditFullName)) {
                    getSupportActionBar().setTitle(subredditFullName);
                }
                subredditNameTextView.setText(subredditFullName);
                String nSubscribers = getString(R.string.subscribers_number_detail, subredditData.getNSubscribers());
                nSubscribersTextView.setText(nSubscribers);
                creationTimeTextView.setText(new SimpleDateFormat("MMM d, yyyy",
                        locale).format(subredditData.getCreatedUTC()));
                if (hideSubredditDescription || subredditData.getDescription().equals("")) {
                    descriptionTextView.setVisibility(View.GONE);
                } else {
                    descriptionTextView.setVisibility(View.VISIBLE);
                    descriptionTextView.setText(subredditData.getDescription());
                }

                if (subredditData.isNSFW()) {
                    if (nsfwWarningBuilder == null
                            && !mNsfwAndSpoilerSharedPreferences.getBoolean((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.NSFW_BASE, false)) {
                        nsfwWarningBuilder = new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                                .setTitle(R.string.warning)
                                .setMessage(R.string.this_is_a_nsfw_subreddit)
                                .setPositiveButton(R.string.leave, (dialogInterface, i)
                                        -> {
                                    finish();
                                })
                                .setNegativeButton(R.string.dismiss, null);
                        nsfwWarningBuilder.show();
                    }
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (sectionsPagerAdapter != null) {
            return sectionsPagerAdapter.handleKeyDown(keyCode) || super.onKeyDown(keyCode, event);
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public SharedPreferences getDefaultSharedPreferences() {
        return mSharedPreferences;
    }

    @Override
    protected CustomThemeWrapper getCustomThemeWrapper() {
        return mCustomThemeWrapper;
    }

    @Override
    protected void applyCustomTheme() {
        coordinatorLayout.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        collapsingToolbarLayout.setContentScrimColor(mCustomThemeWrapper.getColorPrimary());
        applyAppBarLayoutAndToolbarTheme(appBarLayout, toolbar);
        expandedTabTextColor = mCustomThemeWrapper.getTabLayoutWithExpandedCollapsingToolbarTextColor();
        expandedTabIndicatorColor = mCustomThemeWrapper.getTabLayoutWithExpandedCollapsingToolbarTabIndicator();
        expandedTabBackgroundColor = mCustomThemeWrapper.getTabLayoutWithExpandedCollapsingToolbarTabBackground();
        collapsedTabTextColor = mCustomThemeWrapper.getTabLayoutWithCollapsedCollapsingToolbarTextColor();
        collapsedTabIndicatorColor = mCustomThemeWrapper.getTabLayoutWithCollapsedCollapsingToolbarTabIndicator();
        collapsedTabBackgroundColor = mCustomThemeWrapper.getTabLayoutWithCollapsedCollapsingToolbarTabBackground();
        linearLayout.setBackgroundColor(expandedTabBackgroundColor);
        subredditNameTextView.setTextColor(mCustomThemeWrapper.getSubreddit());
        subscribeSubredditChip.setTextColor(mCustomThemeWrapper.getChipTextColor());
        int primaryTextColor = mCustomThemeWrapper.getPrimaryTextColor();
        nSubscribersTextView.setTextColor(primaryTextColor);
        nOnlineSubscribersTextView.setTextColor(primaryTextColor);
        sinceTextView.setTextColor(primaryTextColor);
        creationTimeTextView.setTextColor(primaryTextColor);
        descriptionTextView.setTextColor(primaryTextColor);
        bottomNavigationView.setBackgroundTint(ColorStateList.valueOf(mCustomThemeWrapper.getBottomAppBarBackgroundColor()));
        int bottomAppBarIconColor = mCustomThemeWrapper.getBottomAppBarIconColor();
        option2BottomAppBar.setColorFilter(bottomAppBarIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        option1BottomAppBar.setColorFilter(bottomAppBarIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        option3BottomAppBar.setColorFilter(bottomAppBarIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        option4BottomAppBar.setColorFilter(bottomAppBarIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        applyTabLayoutTheme(tabLayout);
        applyFABTheme(fab);
        unsubscribedColor = mCustomThemeWrapper.getUnsubscribed();
        subscribedColor = mCustomThemeWrapper.getSubscribed();
    }

    private void getCurrentAccountAndBindView() {
        GetCurrentAccount.getCurrentAccount(mExecutor, new Handler(), mRedditDataRoomDatabase, account -> {
            if (mNewAccountName != null) {
                if (account == null || !account.getUsername().equals(mNewAccountName)) {
                    new SwitchAccountAsyncTask(mRedditDataRoomDatabase, mNewAccountName, newAccount -> {
                        EventBus.getDefault().post(new SwitchAccountEvent(getClass().getName()));
                        Toast.makeText(this, R.string.account_switched, Toast.LENGTH_SHORT).show();

                        mNewAccountName = null;
                        if (newAccount == null) {
                            mNullAccessToken = true;
                        } else {
                            mAccessToken = newAccount.getAccessToken();
                            mAccountName = newAccount.getUsername();
                        }

                        bindView();
                    }).execute();
                } else {
                    mAccessToken = account.getAccessToken();
                    mAccountName = account.getUsername();
                    bindView();
                }
            } else {
                if (account == null) {
                    mNullAccessToken = true;
                } else {
                    mAccessToken = account.getAccessToken();
                    mAccountName = account.getUsername();
                }

                bindView();
            }
        });
    }

    private void fetchSubredditData() {
        if (!mFetchSubredditInfoSuccess) {
            FetchSubredditData.fetchSubredditData(mRetrofit, subredditName, new FetchSubredditData.FetchSubredditDataListener() {
                @Override
                public void onFetchSubredditDataSuccess(SubredditData subredditData, int nCurrentOnlineSubscribers) {
                    mNCurrentOnlineSubscribers = nCurrentOnlineSubscribers;
                    nOnlineSubscribersTextView.setText(getString(R.string.online_subscribers_number_detail, nCurrentOnlineSubscribers));
                    new InsertSubredditDataAsyncTask(mRedditDataRoomDatabase, subredditData, () -> mFetchSubredditInfoSuccess = true).execute();
                }

                @Override
                public void onFetchSubredditDataFail() {
                    makeSnackbar(R.string.cannot_fetch_subreddit_info, true);
                    mFetchSubredditInfoSuccess = false;
                }
            });
        }
    }

    private void bottomAppBarOptionAction(int option) {
        switch (option) {
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_HOME: {
                EventBus.getDefault().post(new GoBackToMainPageEvent());
                break;
            }
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_SUBSCRIPTIONS: {
                Intent intent = new Intent(this, SubscribedThingListingActivity.class);
                startActivity(intent);
                break;
            }
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_INBOX: {
                Intent intent = new Intent(this, InboxActivity.class);
                startActivity(intent);
                break;
            }
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_PROFILE: {
                Intent intent = new Intent(this, ViewUserDetailActivity.class);
                intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, mAccountName);
                startActivity(intent);
                break;
            }
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_MULTIREDDITS: {
                Intent intent = new Intent(this, SubscribedThingListingActivity.class);
                intent.putExtra(SubscribedThingListingActivity.EXTRA_SHOW_MULTIREDDITS, true);
                startActivity(intent);
                break;
            }
        }
    }

    private int getBottomAppBarOptionDrawableResource(int option) {
        switch (option) {
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_HOME:
                return R.drawable.ic_home_black_24dp;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_SUBSCRIPTIONS:
                return R.drawable.ic_subscritptions_bottom_app_bar_24dp;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_INBOX:
                return R.drawable.ic_inbox_24dp;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_MULTIREDDITS:
                return R.drawable.ic_multi_reddit_24dp;
            default:
                return R.drawable.ic_account_circle_24dp;
        }
    }

    private void bindView() {
        if (mAccessToken != null) {
            if (mMessageFullname != null) {
                ReadMessage.readMessage(mOauthRetrofit, mAccessToken, mMessageFullname, new ReadMessage.ReadMessageListener() {
                    @Override
                    public void readSuccess() {
                        mMessageFullname = null;
                    }

                    @Override
                    public void readFailed() {

                    }
                });
            }

            if (showBottomAppBar) {
                int optionCount = bottomAppBarSharedPreference.getInt(SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_COUNT, 4);
                int option1 = bottomAppBarSharedPreference.getInt(SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_1, SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_HOME);
                int option2 = bottomAppBarSharedPreference.getInt(SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_2, SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_SUBSCRIPTIONS);

                bottomNavigationView.setVisibility(View.VISIBLE);

                if (optionCount == 2) {
                    linearLayoutBottomAppBar.setWeightSum(3);
                    option1BottomAppBar.setVisibility(View.GONE);
                    option3BottomAppBar.setVisibility(View.GONE);

                    option2BottomAppBar.setImageResource(getBottomAppBarOptionDrawableResource(option1));
                    option4BottomAppBar.setImageResource(getBottomAppBarOptionDrawableResource(option2));

                    option2BottomAppBar.setOnClickListener(view -> {
                        bottomAppBarOptionAction(option1);
                    });

                    option4BottomAppBar.setOnClickListener(view -> {
                        bottomAppBarOptionAction(option2);
                    });
                } else {
                    int option3 = bottomAppBarSharedPreference.getInt(SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_3, SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_INBOX);
                    int option4 = bottomAppBarSharedPreference.getInt(SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_4, SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_PROFILE);

                    option1BottomAppBar.setImageResource(getBottomAppBarOptionDrawableResource(option1));
                    option2BottomAppBar.setImageResource(getBottomAppBarOptionDrawableResource(option2));
                    option3BottomAppBar.setImageResource(getBottomAppBarOptionDrawableResource(option3));
                    option4BottomAppBar.setImageResource(getBottomAppBarOptionDrawableResource(option4));

                    option1BottomAppBar.setOnClickListener(view -> {
                        bottomAppBarOptionAction(option1);
                    });

                    option2BottomAppBar.setOnClickListener(view -> {
                        bottomAppBarOptionAction(option2);
                    });

                    option3BottomAppBar.setOnClickListener(view -> {
                        bottomAppBarOptionAction(option3);
                    });

                    option4BottomAppBar.setOnClickListener(view -> {
                        bottomAppBarOptionAction(option4);
                    });
                }
            } else {
                CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
                lp.setAnchorId(View.NO_ID);
                lp.gravity = Gravity.END | Gravity.BOTTOM;
                fab.setLayoutParams(lp);
            }
        } else {
            CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
            lp.setAnchorId(View.NO_ID);
            lp.gravity = Gravity.END | Gravity.BOTTOM;
            fab.setLayoutParams(lp);
            bottomNavigationView.setVisibility(View.GONE);
        }
        fabOption = bottomAppBarSharedPreference.getInt(SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB, SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_SUBMIT_POSTS);
        switch (fabOption) {
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_REFRESH:
                fab.setImageResource(R.drawable.ic_refresh_24dp);
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_CHANGE_SORT_TYPE:
                fab.setImageResource(R.drawable.ic_sort_toolbar_24dp);
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_CHANGE_POST_LAYOUT:
                fab.setImageResource(R.drawable.ic_post_layout_24dp);
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_SEARCH:
                fab.setImageResource(R.drawable.ic_search_black_24dp);
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_GO_TO_SUBREDDIT:
                fab.setImageResource(R.drawable.ic_subreddit_24dp);
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_GO_TO_USER:
                fab.setImageResource(R.drawable.ic_user_24dp);
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_RANDOM:
                fab.setImageResource(R.drawable.ic_random_24dp);
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_HIDE_READ_POSTS:
                if (mAccessToken == null) {
                    fab.setImageResource(R.drawable.ic_filter_24dp);
                    fabOption = SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_FILTER_POSTS;
                } else {
                    fab.setImageResource(R.drawable.ic_hide_read_posts_24dp);
                }
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_FILTER_POSTS:
                fab.setImageResource(R.drawable.ic_filter_24dp);
                break;
            default:
                if (mAccessToken == null) {
                    fab.setImageResource(R.drawable.ic_filter_24dp);
                    fabOption = SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_FILTER_POSTS;
                } else {
                    fab.setImageResource(R.drawable.ic_add_day_night_24dp);
                }
                break;
        }
        fab.setOnClickListener(view -> {
            switch (fabOption) {
                case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_REFRESH: {
                    if (sectionsPagerAdapter != null) {
                        sectionsPagerAdapter.refresh();
                    }
                    break;
                }
                case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_CHANGE_SORT_TYPE: {
                    displaySortTypeBottomSheetFragment();
                    break;
                }
                case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_CHANGE_POST_LAYOUT: {
                    PostLayoutBottomSheetFragment postLayoutBottomSheetFragment = new PostLayoutBottomSheetFragment();
                    postLayoutBottomSheetFragment.show(getSupportFragmentManager(), postLayoutBottomSheetFragment.getTag());
                    break;
                }
                case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_SEARCH: {
                    Intent intent = new Intent(this, SearchActivity.class);
                    intent.putExtra(SearchActivity.EXTRA_SUBREDDIT_NAME, subredditName);
                    startActivity(intent);
                    break;
                }
                case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_GO_TO_SUBREDDIT:
                    goToSubreddit();
                    break;
                case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_GO_TO_USER:
                    goToUser();
                    break;
                case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_RANDOM:
                    random();
                    break;
                case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_HIDE_READ_POSTS:
                    if (sectionsPagerAdapter != null) {
                        sectionsPagerAdapter.hideReadPosts();
                    }
                    break;
                case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_FILTER_POSTS:
                    if (sectionsPagerAdapter != null) {
                        sectionsPagerAdapter.filterPosts();
                    }
                    break;
                default:
                    PostTypeBottomSheetFragment postTypeBottomSheetFragment = new PostTypeBottomSheetFragment();
                    postTypeBottomSheetFragment.show(getSupportFragmentManager(), postTypeBottomSheetFragment.getTag());
                    break;
            }
        });
        fab.setOnLongClickListener(view -> {
            FABMoreOptionsBottomSheetFragment fabMoreOptionsBottomSheetFragment = new FABMoreOptionsBottomSheetFragment();
            Bundle bundle = new Bundle();
            bundle.putBoolean(FABMoreOptionsBottomSheetFragment.EXTRA_ANONYMOUS_MODE, mAccessToken == null);
            fabMoreOptionsBottomSheetFragment.setArguments(bundle);
            fabMoreOptionsBottomSheetFragment.show(getSupportFragmentManager(), fabMoreOptionsBottomSheetFragment.getTag());
            return true;
        });
        fab.setVisibility(View.VISIBLE);

        subscribeSubredditChip.setOnClickListener(view -> {
            if (mAccessToken == null) {
                Toast.makeText(ViewSubredditDetailActivity.this, R.string.login_first, Toast.LENGTH_SHORT).show();
                return;
            }

            if (subscriptionReady) {
                subscriptionReady = false;
                if (subscribeSubredditChip.getText().equals(getResources().getString(R.string.subscribe))) {
                    SubredditSubscription.subscribeToSubreddit(mOauthRetrofit, mRetrofit, mAccessToken,
                            subredditName, mAccountName, mRedditDataRoomDatabase,
                            new SubredditSubscription.SubredditSubscriptionListener() {
                                @Override
                                public void onSubredditSubscriptionSuccess() {
                                    subscribeSubredditChip.setText(R.string.unsubscribe);
                                    subscribeSubredditChip.setChipBackgroundColor(ColorStateList.valueOf(subscribedColor));
                                    makeSnackbar(R.string.subscribed, false);
                                    subscriptionReady = true;
                                }

                                @Override
                                public void onSubredditSubscriptionFail() {
                                    makeSnackbar(R.string.subscribe_failed, false);
                                    subscriptionReady = true;
                                }
                            });
                } else {
                    SubredditSubscription.unsubscribeToSubreddit(mOauthRetrofit, mAccessToken,
                            subredditName, mAccountName, mRedditDataRoomDatabase,
                            new SubredditSubscription.SubredditSubscriptionListener() {
                                @Override
                                public void onSubredditSubscriptionSuccess() {
                                    subscribeSubredditChip.setText(R.string.subscribe);
                                    subscribeSubredditChip.setChipBackgroundColor(ColorStateList.valueOf(unsubscribedColor));
                                    makeSnackbar(R.string.unsubscribed, false);
                                    subscriptionReady = true;
                                }

                                @Override
                                public void onSubredditSubscriptionFail() {
                                    makeSnackbar(R.string.unsubscribe_failed, false);
                                    subscriptionReady = true;
                                }
                            });
                }
            }
        });

        new CheckIsSubscribedToSubredditAsyncTask(mRedditDataRoomDatabase, subredditName, mAccountName,
                new CheckIsSubscribedToSubredditAsyncTask.CheckIsSubscribedToSubredditListener() {
                    @Override
                    public void isSubscribed() {
                        subscribeSubredditChip.setText(R.string.unsubscribe);
                        subscribeSubredditChip.setChipBackgroundColor(ColorStateList.valueOf(subscribedColor));
                        subscriptionReady = true;
                    }

                    @Override
                    public void isNotSubscribed() {
                        subscribeSubredditChip.setText(R.string.subscribe);
                        subscribeSubredditChip.setChipBackgroundColor(ColorStateList.valueOf(unsubscribedColor));
                        subscriptionReady = true;
                    }
                }).execute();

        sectionsPagerAdapter = new SectionsPagerAdapter(this);
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    unlockSwipeRightToGoBack();
                } else {
                    lockSwipeRightToGoBack();
                }

                if (mAccessToken != null) {
                    if (showBottomAppBar) {
                        bottomNavigationView.performShow();
                    }
                }
                fab.show();

                sectionsPagerAdapter.displaySortTypeInToolbar();
            }
        });
        viewPager2.setAdapter(sectionsPagerAdapter);
        viewPager2.setOffscreenPageLimit(2);
        viewPager2.setUserInputEnabled(!mSharedPreferences.getBoolean(SharedPreferencesUtils.DISABLE_SWIPING_BETWEEN_TABS, false));
        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(R.string.posts);
                    break;
                case 1:
                    tab.setText(R.string.sidebar);
            }
        }).attach();
        fixViewPager2Sensitivity(viewPager2);

        boolean viewSidebar = getIntent().getBooleanExtra(EXTRA_VIEW_SIDEBAR, false);
        if (viewSidebar) {
            viewPager2.setCurrentItem(1, false);
        }
    }

    private void displaySortTypeBottomSheetFragment() {
        SortTypeBottomSheetFragment sortTypeBottomSheetFragment = new SortTypeBottomSheetFragment();
        Bundle bottomSheetBundle = new Bundle();
        bottomSheetBundle.putBoolean(SortTypeBottomSheetFragment.EXTRA_NO_BEST_TYPE, true);
        sortTypeBottomSheetFragment.setArguments(bottomSheetBundle);
        sortTypeBottomSheetFragment.show(getSupportFragmentManager(), sortTypeBottomSheetFragment.getTag());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_subreddit_detail_activity, menu);
        applyMenuItemTheme(menu);
        mMenu = menu;
        MenuItem lazyModeItem = mMenu.findItem(R.id.action_lazy_mode_view_subreddit_detail_activity);
        if (isInLazyMode) {
            lazyModeItem.setTitle(R.string.action_stop_lazy_mode);
            params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED);
            collapsingToolbarLayout.setLayoutParams(params);
        } else {
            lazyModeItem.setTitle(R.string.action_start_lazy_mode);
            params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS |
                    AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED);
            collapsingToolbarLayout.setLayoutParams(params);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.action_sort_view_subreddit_detail_activity) {
            displaySortTypeBottomSheetFragment();
            return true;
        } else if (itemId == R.id.action_search_view_subreddit_detail_activity) {
            Intent intent = new Intent(this, SearchActivity.class);
            intent.putExtra(SearchActivity.EXTRA_SUBREDDIT_NAME, subredditName);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_refresh_view_subreddit_detail_activity) {
            if (mMenu != null) {
                mMenu.findItem(R.id.action_lazy_mode_view_subreddit_detail_activity).setTitle(R.string.action_start_lazy_mode);
            }
            if (sectionsPagerAdapter != null) {
                sectionsPagerAdapter.refresh();
                mFetchSubredditInfoSuccess = false;
                fetchSubredditData();
            }
            return true;
        } else if (itemId == R.id.action_lazy_mode_view_subreddit_detail_activity) {
            if (sectionsPagerAdapter != null) {
                MenuItem lazyModeItem = mMenu.findItem(R.id.action_lazy_mode_view_subreddit_detail_activity);
                if (isInLazyMode) {
                    isInLazyMode = false;
                    sectionsPagerAdapter.stopLazyMode();
                    lazyModeItem.setTitle(R.string.action_start_lazy_mode);
                    params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS |
                            AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED);
                    collapsingToolbarLayout.setLayoutParams(params);
                } else {
                    isInLazyMode = true;
                    if (sectionsPagerAdapter.startLazyMode()) {
                        lazyModeItem.setTitle(R.string.action_stop_lazy_mode);
                        appBarLayout.setExpanded(false);
                        params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED);
                        collapsingToolbarLayout.setLayoutParams(params);
                    } else {
                        isInLazyMode = false;
                    }
                }
            }
            return true;
        } else if (itemId == R.id.action_change_post_layout_view_subreddit_detail_activity) {
            PostLayoutBottomSheetFragment postLayoutBottomSheetFragment = new PostLayoutBottomSheetFragment();
            postLayoutBottomSheetFragment.show(getSupportFragmentManager(), postLayoutBottomSheetFragment.getTag());
            return true;
        } else if (itemId == R.id.action_select_user_flair_view_subreddit_detail_activity) {
            if (mAccessToken == null) {
                Toast.makeText(this, R.string.login_first, Toast.LENGTH_SHORT).show();
                return true;
            }
            Intent selectUserFlairIntent = new Intent(this, SelectUserFlairActivity.class);
            selectUserFlairIntent.putExtra(SelectUserFlairActivity.EXTRA_SUBREDDIT_NAME, subredditName);
            startActivity(selectUserFlairIntent);
            return true;
        } else if (itemId == R.id.action_add_to_multireddit_view_subreddit_detail_activity) {
            if (mAccessToken == null) {
                Toast.makeText(this, R.string.login_first, Toast.LENGTH_SHORT).show();
                return true;
            }
            Intent intent = new Intent(this, MultiredditSelectionActivity.class);
            startActivityForResult(intent, ADD_TO_MULTIREDDIT_REQUEST_CODE);
        } else if (itemId == R.id.action_share_view_subreddit_detail_activity) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "https://www.reddit.com/r/" + subredditName);
            if (shareIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
            } else {
                Toast.makeText(this, R.string.no_app, Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_TO_MULTIREDDIT_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                MultiReddit multiReddit = data.getParcelableExtra(MultiredditSelectionActivity.EXTRA_RETURN_MULTIREDDIT);
                if (multiReddit != null) {
                    AddSubredditOrUserToMultiReddit.addSubredditOrUserToMultiReddit(mOauthRetrofit,
                            mAccessToken, multiReddit.getPath(), subredditName,
                            new AddSubredditOrUserToMultiReddit.AddSubredditOrUserToMultiRedditListener() {
                                @Override
                                public void success() {
                                    Toast.makeText(ViewSubredditDetailActivity.this,
                                            getString(R.string.add_subreddit_or_user_to_multireddit_success, subredditName, multiReddit.getDisplayName()), Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void failed(int code) {
                                    Toast.makeText(ViewSubredditDetailActivity.this,
                                            getString(R.string.add_subreddit_or_user_to_multireddit_failed, subredditName, multiReddit.getDisplayName()), Toast.LENGTH_LONG).show();
                                }
                            });
                }

            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(FETCH_SUBREDDIT_INFO_STATE, mFetchSubredditInfoSuccess);
        outState.putInt(CURRENT_ONLINE_SUBSCRIBERS_STATE, mNCurrentOnlineSubscribers);
        outState.putBoolean(IS_IN_LAZY_MODE_STATE, isInLazyMode);
        outState.putBoolean(NULL_ACCESS_TOKEN_STATE, mNullAccessToken);
        outState.putString(ACCESS_TOKEN_STATE, mAccessToken);
        outState.putString(ACCOUNT_NAME_STATE, mAccountName);
        outState.putString(MESSAGE_FULLNAME_STATE, mMessageFullname);
        outState.putString(NEW_ACCOUNT_NAME_STATE, mNewAccountName);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void makeSnackbar(int resId, boolean retry) {
        if (showToast) {
            Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
        } else {
            if (retry) {
                Snackbar.make(coordinatorLayout, resId, Snackbar.LENGTH_SHORT).setAction(R.string.retry,
                        view -> fetchSubredditData()).show();
            } else {
                Snackbar.make(coordinatorLayout, resId, Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void sortTypeSelected(SortType sortType) {
        if (sectionsPagerAdapter != null) {
            sectionsPagerAdapter.changeSortType(sortType);
        }
    }

    @Override
    public void sortTypeSelected(String sortType) {
        SortTimeBottomSheetFragment sortTimeBottomSheetFragment = new SortTimeBottomSheetFragment();
        Bundle bundle = new Bundle();
        bundle.putString(SortTimeBottomSheetFragment.EXTRA_SORT_TYPE, sortType);
        sortTimeBottomSheetFragment.setArguments(bundle);
        sortTimeBottomSheetFragment.show(getSupportFragmentManager(), sortTimeBottomSheetFragment.getTag());
    }

    @Override
    public void postTypeSelected(int postType) {
        Intent intent;
        switch (postType) {
            case PostTypeBottomSheetFragment.TYPE_TEXT:
                intent = new Intent(this, PostTextActivity.class);
                intent.putExtra(PostTextActivity.EXTRA_SUBREDDIT_NAME, subredditName);
                startActivity(intent);
                break;
            case PostTypeBottomSheetFragment.TYPE_LINK:
                intent = new Intent(this, PostLinkActivity.class);
                intent.putExtra(PostLinkActivity.EXTRA_SUBREDDIT_NAME, subredditName);
                startActivity(intent);
                break;
            case PostTypeBottomSheetFragment.TYPE_IMAGE:
                intent = new Intent(this, PostImageActivity.class);
                intent.putExtra(PostImageActivity.EXTRA_SUBREDDIT_NAME, subredditName);
                startActivity(intent);
                break;
            case PostTypeBottomSheetFragment.TYPE_VIDEO:
                intent = new Intent(this, PostVideoActivity.class);
                intent.putExtra(PostVideoActivity.EXTRA_SUBREDDIT_NAME, subredditName);
                startActivity(intent);
        }
    }

    @Override
    public void postLayoutSelected(int postLayout) {
        mPostLayoutSharedPreferences.edit().putInt(SharedPreferencesUtils.POST_LAYOUT_SUBREDDIT_POST_BASE + subredditName, postLayout).apply();
        sectionsPagerAdapter.changePostLayout(postLayout);
    }

    @Override
    public void contentScrollUp() {
        if (mAccessToken != null) {
            if (showBottomAppBar && !lockBottomAppBar) {
                bottomNavigationView.performShow();
            }
            if (!(showBottomAppBar && lockBottomAppBar)) {
                fab.show();
            }
        }
    }

    @Override
    public void contentScrollDown() {
        if (mAccessToken != null) {
            if (!(showBottomAppBar && lockBottomAppBar)) {
                fab.hide();
            }
            if (showBottomAppBar && !lockBottomAppBar) {
                bottomNavigationView.performHide();
            }
        }
    }

    @Subscribe
    public void onAccountSwitchEvent(SwitchAccountEvent event) {
        if (!getClass().getName().equals(event.excludeActivityClassName)) {
            finish();
        }
    }

    @Subscribe
    public void onChangeNSFWEvent(ChangeNSFWEvent changeNSFWEvent) {
        sectionsPagerAdapter.changeNSFW(changeNSFWEvent.nsfw);
    }

    @Subscribe
    public void goBackToMainPageEvent(GoBackToMainPageEvent event) {
        finish();
    }

    @Override
    public void onLongPress() {
        if (sectionsPagerAdapter != null) {
            sectionsPagerAdapter.goBackToTop();
        }
    }

    @Override
    public void displaySortType() {
        if (sectionsPagerAdapter != null) {
            sectionsPagerAdapter.displaySortTypeInToolbar();
        }
    }

    @Override
    public void fabOptionSelected(int option) {
        switch (option) {
            case FABMoreOptionsBottomSheetFragment.FAB_OPTION_SUBMIT_POST:
                PostTypeBottomSheetFragment postTypeBottomSheetFragment = new PostTypeBottomSheetFragment();
                postTypeBottomSheetFragment.show(getSupportFragmentManager(), postTypeBottomSheetFragment.getTag());
                break;
            case FABMoreOptionsBottomSheetFragment.FAB_OPTION_REFRESH:
                if (sectionsPagerAdapter != null) {
                    sectionsPagerAdapter.refresh();
                }
                break;
            case FABMoreOptionsBottomSheetFragment.FAB_OPTION_CHANGE_SORT_TYPE:
                displaySortTypeBottomSheetFragment();
                break;
            case FABMoreOptionsBottomSheetFragment.FAB_OPTION_CHANGE_POST_LAYOUT:
                PostLayoutBottomSheetFragment postLayoutBottomSheetFragment = new PostLayoutBottomSheetFragment();
                postLayoutBottomSheetFragment.show(getSupportFragmentManager(), postLayoutBottomSheetFragment.getTag());
                break;
            case FABMoreOptionsBottomSheetFragment.FAB_OPTION_SEARCH:
                Intent intent = new Intent(this, SearchActivity.class);
                intent.putExtra(SearchActivity.EXTRA_SUBREDDIT_NAME, subredditName);
                startActivity(intent);
                break;
            case FABMoreOptionsBottomSheetFragment.FAB_OPTION_GO_TO_SUBREDDIT: {
                goToSubreddit();
                break;
            }
            case FABMoreOptionsBottomSheetFragment.FAB_OPTION_GO_TO_USER: {
                goToUser();
                break;
            }
            case FABMoreOptionsBottomSheetFragment.FAB_RANDOM: {
                random();
                break;
            }
            case FABMoreOptionsBottomSheetFragment.FAB_HIDE_READ_POSTS: {
                if (sectionsPagerAdapter != null) {
                    sectionsPagerAdapter.hideReadPosts();
                }
                break;
            }
            case FABMoreOptionsBottomSheetFragment.FAB_FILTER_POSTS: {
                if (sectionsPagerAdapter != null) {
                    sectionsPagerAdapter.filterPosts();
                }
                break;
            }
        }
    }

    private void goToSubreddit() {
        EditText thingEditText = (EditText) getLayoutInflater().inflate(R.layout.dialog_go_to_thing_edit_text, null);
        thingEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
        new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                .setTitle(R.string.go_to_subreddit)
                .setView(thingEditText)
                .setPositiveButton(R.string.ok, (dialogInterface, i)
                        -> {
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(thingEditText.getWindowToken(), 0);
                    }
                    Intent subredditIntent = new Intent(this, ViewSubredditDetailActivity.class);
                    subredditIntent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, thingEditText.getText().toString());
                    startActivity(subredditIntent);
                })
                .setNegativeButton(R.string.cancel, null)
                .setOnDismissListener(dialogInterface -> {
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(thingEditText.getWindowToken(), 0);
                    }
                })
                .show();
    }

    private void goToUser() {
        EditText thingEditText = (EditText) getLayoutInflater().inflate(R.layout.dialog_go_to_thing_edit_text, null);
        thingEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
        new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                .setTitle(R.string.go_to_user)
                .setView(thingEditText)
                .setPositiveButton(R.string.ok, (dialogInterface, i)
                        -> {
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(thingEditText.getWindowToken(), 0);
                    }
                    Intent userIntent = new Intent(this, ViewUserDetailActivity.class);
                    userIntent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, thingEditText.getText().toString());
                    startActivity(userIntent);
                })
                .setNegativeButton(R.string.cancel, null)
                .setOnDismissListener(dialogInterface -> {
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(thingEditText.getWindowToken(), 0);
                    }
                })
                .show();
    }

    private void random() {
        RandomBottomSheetFragment randomBottomSheetFragment = new RandomBottomSheetFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(RandomBottomSheetFragment.EXTRA_IS_NSFW, mNsfwAndSpoilerSharedPreferences.getBoolean((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.NSFW_BASE, false));
        randomBottomSheetFragment.setArguments(bundle);
        randomBottomSheetFragment.show(getSupportFragmentManager(), randomBottomSheetFragment.getTag());
    }

    @Override
    public void randomOptionSelected(int option) {
        Intent intent = new Intent(this, FetchRandomSubredditOrPostActivity.class);
        intent.putExtra(FetchRandomSubredditOrPostActivity.EXTRA_RANDOM_OPTION, option);
        startActivity(intent);
    }

    @Override
    public void markPostAsRead(Post post) {
        InsertReadPost.insertReadPost(mRedditDataRoomDatabase, mExecutor, mAccountName, post.getId());
    }

    private class SectionsPagerAdapter extends FragmentStateAdapter {

        SectionsPagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                PostFragment fragment = new PostFragment();
                Bundle bundle = new Bundle();
                bundle.putString(PostFragment.EXTRA_NAME, subredditName);
                bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostDataSource.TYPE_SUBREDDIT);
                bundle.putString(PostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                bundle.putString(PostFragment.EXTRA_ACCOUNT_NAME, mAccountName);
                fragment.setArguments(bundle);
                return fragment;
            }
            SidebarFragment fragment = new SidebarFragment();
            Bundle bundle = new Bundle();
            bundle.putString(SidebarFragment.EXTRA_SUBREDDIT_NAME, subredditName);
            fragment.setArguments(bundle);
            return fragment;
        }

        @Nullable
        private Fragment getCurrentFragment() {
            if (fragmentManager == null) {
                return null;
            }
            return fragmentManager.findFragmentByTag("f" + viewPager2.getCurrentItem());
        }

        public boolean handleKeyDown(int keyCode) {
            if (viewPager2.getCurrentItem() == 0) {
                Fragment fragment = getCurrentFragment();
                if (fragment instanceof PostFragment) {
                    return ((PostFragment) fragment).handleKeyDown(keyCode);
                }
            }
            return false;
        }

        public void refresh() {
            Fragment fragment = getCurrentFragment();
            if (fragment instanceof PostFragment) {
                ((PostFragment) fragment).refresh();
            } else if (fragment instanceof SidebarFragment) {
                ((SidebarFragment) fragment).fetchSubredditData();
            }
        }

        boolean startLazyMode() {
            Fragment fragment = getCurrentFragment();
            if (fragment instanceof FragmentCommunicator) {
                return ((FragmentCommunicator) fragment).startLazyMode();
            }
            return false;
        }

        void stopLazyMode() {
            Fragment fragment = getCurrentFragment();
            if (fragment instanceof FragmentCommunicator) {
                ((FragmentCommunicator) fragment).stopLazyMode();
            }
        }

        public void changeSortType(SortType sortType) {
            Fragment fragment = getCurrentFragment();
            if (fragment instanceof PostFragment) {
                ((PostFragment) fragment).changeSortType(sortType);
                Utils.displaySortTypeInToolbar(sortType, toolbar);
            }
        }

        public void changeNSFW(boolean nsfw) {
            Fragment fragment = getCurrentFragment();
            if (fragment instanceof PostFragment) {
                ((PostFragment) fragment).changeNSFW(nsfw);
            }
        }

        void changePostLayout(int postLayout) {
            Fragment fragment = getCurrentFragment();
            if (fragment instanceof PostFragment) {
                ((PostFragment) fragment).changePostLayout(postLayout);
            }
        }

        void goBackToTop() {
            Fragment fragment = getCurrentFragment();
            if (fragment instanceof PostFragment) {
                ((PostFragment) fragment).goBackToTop();
            } else if (fragment instanceof SidebarFragment) {
                ((SidebarFragment) fragment).goBackToTop();
            }
        }

        void displaySortTypeInToolbar() {
            if (fragmentManager != null) {
                Fragment fragment = fragmentManager.findFragmentByTag("f" + viewPager2.getCurrentItem());
                if (fragment instanceof PostFragment) {
                    SortType sortType = ((PostFragment) fragment).getSortType();
                    Utils.displaySortTypeInToolbar(sortType, toolbar);
                }
            }
        }

        void hideReadPosts() {
            if (fragmentManager != null) {
                Fragment fragment = fragmentManager.findFragmentByTag("f0");
                if (fragment instanceof PostFragment) {
                    ((PostFragment) fragment).hideReadPosts();
                }
            }
        }

        void filterPosts() {
            if (fragmentManager != null) {
                Fragment fragment = fragmentManager.findFragmentByTag("f0");
                if (fragment instanceof PostFragment) {
                    ((PostFragment) fragment).filterPosts();
                }
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }

    private void lockSwipeRightToGoBack() {
        if (mSlidrInterface != null) {
            mSlidrInterface.lock();
        }
    }

    private void unlockSwipeRightToGoBack() {
        if (mSlidrInterface != null) {
            mSlidrInterface.unlock();
        }
    }
}