package ml.docilealligator.infinityforreddit;

import javax.inject.Singleton;

import dagger.Component;
import ml.docilealligator.infinityforreddit.Activity.AccountPostsActivity;
import ml.docilealligator.infinityforreddit.Activity.AccountSavedThingActivity;
import ml.docilealligator.infinityforreddit.Activity.CommentActivity;
import ml.docilealligator.infinityforreddit.Activity.EditCommentActivity;
import ml.docilealligator.infinityforreddit.Activity.EditPostActivity;
import ml.docilealligator.infinityforreddit.Activity.FilteredThingActivity;
import ml.docilealligator.infinityforreddit.Activity.LinkResolverActivity;
import ml.docilealligator.infinityforreddit.Activity.LoginActivity;
import ml.docilealligator.infinityforreddit.Activity.MainActivity;
import ml.docilealligator.infinityforreddit.Activity.MultiRedditListingActivity;
import ml.docilealligator.infinityforreddit.Activity.PostImageActivity;
import ml.docilealligator.infinityforreddit.Activity.PostLinkActivity;
import ml.docilealligator.infinityforreddit.Activity.PostTextActivity;
import ml.docilealligator.infinityforreddit.Activity.PostVideoActivity;
import ml.docilealligator.infinityforreddit.Activity.RulesActivity;
import ml.docilealligator.infinityforreddit.Activity.SearchActivity;
import ml.docilealligator.infinityforreddit.Activity.SearchResultActivity;
import ml.docilealligator.infinityforreddit.Activity.SearchSubredditsResultActivity;
import ml.docilealligator.infinityforreddit.Activity.SettingsActivity;
import ml.docilealligator.infinityforreddit.Activity.SubredditSelectionActivity;
import ml.docilealligator.infinityforreddit.Activity.SubscribedThingListingActivity;
import ml.docilealligator.infinityforreddit.Activity.ViewGIFActivity;
import ml.docilealligator.infinityforreddit.Activity.ViewImageActivity;
import ml.docilealligator.infinityforreddit.Activity.ViewMessageActivity;
import ml.docilealligator.infinityforreddit.Activity.ViewMultiRedditDetailActivity;
import ml.docilealligator.infinityforreddit.Activity.ViewPostDetailActivity;
import ml.docilealligator.infinityforreddit.Activity.ViewSidebarActivity;
import ml.docilealligator.infinityforreddit.Activity.ViewSubredditDetailActivity;
import ml.docilealligator.infinityforreddit.Activity.ViewUserDetailActivity;
import ml.docilealligator.infinityforreddit.Activity.ViewVideoActivity;
import ml.docilealligator.infinityforreddit.Fragment.CommentsListingFragment;
import ml.docilealligator.infinityforreddit.Fragment.FlairBottomSheetFragment;
import ml.docilealligator.infinityforreddit.Fragment.FollowedUsersListingFragment;
import ml.docilealligator.infinityforreddit.Fragment.PostFragment;
import ml.docilealligator.infinityforreddit.Fragment.SubredditListingFragment;
import ml.docilealligator.infinityforreddit.Fragment.SubscribedSubredditsListingFragment;
import ml.docilealligator.infinityforreddit.Fragment.UserListingFragment;
import ml.docilealligator.infinityforreddit.Service.SubmitPostService;
import ml.docilealligator.infinityforreddit.Settings.GesturesAndButtonsPreferenceFragment;
import ml.docilealligator.infinityforreddit.Settings.MainPreferenceFragment;
import ml.docilealligator.infinityforreddit.Settings.NotificationPreferenceFragment;

@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {
    void inject(MainActivity mainActivity);

    void inject(LoginActivity loginActivity);

    void inject(PostFragment postFragment);

    void inject(SubredditListingFragment subredditListingFragment);

    void inject(UserListingFragment userListingFragment);

    void inject(ViewPostDetailActivity viewPostDetailActivity);

    void inject(ViewSubredditDetailActivity viewSubredditDetailActivity);

    void inject(ViewUserDetailActivity viewUserDetailActivity);

    void inject(CommentActivity commentActivity);

    void inject(SubscribedThingListingActivity subscribedThingListingActivity);

    void inject(PostTextActivity postTextActivity);

    void inject(SubscribedSubredditsListingFragment subscribedSubredditsListingFragment);

    void inject(PostLinkActivity postLinkActivity);

    void inject(PostImageActivity postImageActivity);

    void inject(PostVideoActivity postVideoActivity);

    void inject(FlairBottomSheetFragment flairBottomSheetFragment);

    void inject(RulesActivity rulesActivity);

    void inject(CommentsListingFragment commentsListingFragment);

    void inject(SubmitPostService submitPostService);

    void inject(FilteredThingActivity filteredPostsActivity);

    void inject(SearchResultActivity searchResultActivity);

    void inject(SearchSubredditsResultActivity searchSubredditsResultActivity);

    void inject(FollowedUsersListingFragment followedUsersListingFragment);

    void inject(SubredditSelectionActivity subredditSelectionActivity);

    void inject(EditPostActivity editPostActivity);

    void inject(EditCommentActivity editCommentActivity);

    void inject(AccountPostsActivity accountPostsActivity);

    void inject(PullNotificationWorker pullNotificationWorker);

    void inject(ViewMessageActivity viewMessageActivity);

    void inject(NotificationPreferenceFragment notificationPreferenceFragment);

    void inject(LinkResolverActivity linkResolverActivity);

    void inject(SearchActivity searchActivity);

    void inject(SettingsActivity settingsActivity);

    void inject(MainPreferenceFragment mainPreferenceFragment);

    void inject(AccountSavedThingActivity accountSavedThingActivity);

    void inject(ViewImageActivity viewImageActivity);

    void inject(ViewGIFActivity viewGIFActivity);

    void inject(MultiRedditListingActivity multiRedditListingActivity);

    void inject(ViewMultiRedditDetailActivity viewMultiRedditDetailActivity);

    void inject(ViewSidebarActivity viewSidebarActivity);

    void inject(ViewVideoActivity viewVideoActivity);

    void inject(GesturesAndButtonsPreferenceFragment gesturesAndButtonsPreferenceFragment);
}
