package ml.docilealligator.infinityforreddit;

import android.os.AsyncTask;
import android.text.Html;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import ml.docilealligator.infinityforreddit.Fragment.PostFragment;
import ml.docilealligator.infinityforreddit.Post.Post;
import ml.docilealligator.infinityforreddit.Utils.JSONUtils;
import ml.docilealligator.infinityforreddit.Utils.Utils;

/**
 * Created by alex on 3/21/18.
 */

public class ParsePost {
    public static void parsePosts(String response, Locale locale, int nPosts, int filter, boolean nsfw,
                                  ParsePostsListingListener parsePostsListingListener) {
        new ParsePostDataAsyncTask(response, locale, nPosts, filter, nsfw, parsePostsListingListener).execute();
    }

    public static void parsePost(String response, Locale locale, ParsePostListener parsePostListener) {
        new ParsePostDataAsyncTask(response, locale, true, parsePostListener).execute();
    }

    private static Post parseBasicData(JSONObject data, Locale locale) throws JSONException {
        String id = data.getString(JSONUtils.ID_KEY);
        String fullName = data.getString(JSONUtils.NAME_KEY);
        String subredditName = data.getString(JSONUtils.SUBREDDIT_KEY);
        String subredditNamePrefixed = data.getString(JSONUtils.SUBREDDIT_NAME_PREFIX_KEY);
        String author = data.getString(JSONUtils.AUTHOR_KEY);
        long postTime = data.getLong(JSONUtils.CREATED_UTC_KEY) * 1000;
        String title = data.getString(JSONUtils.TITLE_KEY);
        int score = data.getInt(JSONUtils.SCORE_KEY);
        int voteType;
        int gilded = data.getInt(JSONUtils.GILDED_KEY);
        int nComments = data.getInt(JSONUtils.NUM_COMMENTS_KEY);
        boolean hidden = data.getBoolean(JSONUtils.HIDDEN_KEY);
        boolean spoiler = data.getBoolean(JSONUtils.SPOILER_KEY);
        boolean nsfw = data.getBoolean(JSONUtils.NSFW_KEY);
        boolean stickied = data.getBoolean(JSONUtils.STICKIED_KEY);
        boolean archived = data.getBoolean(JSONUtils.ARCHIVED_KEY);
        boolean locked = data.getBoolean(JSONUtils.LOCKEC_KEY);
        boolean saved = data.getBoolean(JSONUtils.SAVED_KEY);
        String flair = null;
        if (!data.isNull(JSONUtils.LINK_FLAIR_TEXT_KEY)) {
            flair = data.getString(JSONUtils.LINK_FLAIR_TEXT_KEY);
        }

        if (data.isNull(JSONUtils.LIKES_KEY)) {
            voteType = 0;
        } else {
            voteType = data.getBoolean(JSONUtils.LIKES_KEY) ? 1 : -1;
            score -= voteType;
        }

        Calendar postTimeCalendar = Calendar.getInstance();
        postTimeCalendar.setTimeInMillis(postTime);
        String formattedPostTime = new SimpleDateFormat("MMM d, yyyy, HH:mm",
                locale).format(postTimeCalendar.getTime());
        String permalink = Html.fromHtml(data.getString(JSONUtils.PERMALINK_KEY)).toString();

        String previewUrl = "";
        String thumbnailPreviewUrl = "";
        int previewWidth = -1;
        int previewHeight = -1;
        if (data.has(JSONUtils.PREVIEW_KEY)) {
            JSONObject images = data.getJSONObject(JSONUtils.PREVIEW_KEY).getJSONArray(JSONUtils.IMAGES_KEY).getJSONObject(0);
            previewUrl = images.getJSONObject(JSONUtils.SOURCE_KEY).getString(JSONUtils.URL_KEY);
            JSONArray thumbnailPreviews = images.getJSONArray(JSONUtils.RESOLUTIONS_KEY);
            int thumbnailPreviewsLength = thumbnailPreviews.length();
            if (thumbnailPreviewsLength > 0) {
                if (thumbnailPreviewsLength >= 3) {
                    thumbnailPreviewUrl = images.getJSONArray(JSONUtils.RESOLUTIONS_KEY).getJSONObject(2).getString(JSONUtils.URL_KEY);
                } else {
                    thumbnailPreviewUrl = images.getJSONArray(JSONUtils.RESOLUTIONS_KEY).getJSONObject(0).getString(JSONUtils.URL_KEY);
                }
            }
            previewWidth = images.getJSONObject(JSONUtils.SOURCE_KEY).getInt(JSONUtils.WIDTH_KEY);
            previewHeight = images.getJSONObject(JSONUtils.SOURCE_KEY).getInt(JSONUtils.HEIGHT_KEY);
        }
        if (data.has(JSONUtils.CROSSPOST_PARENT_LIST)) {
            //Cross post
            data = data.getJSONArray(JSONUtils.CROSSPOST_PARENT_LIST).getJSONObject(0);
            Post crosspostParent = parseBasicData(data, locale);
            Post post = parseData(data, permalink, id, fullName, subredditName, subredditNamePrefixed,
                    author, formattedPostTime, postTime, title, previewUrl, thumbnailPreviewUrl,
                    previewWidth, previewHeight, score, voteType, gilded, nComments, flair, hidden, spoiler,
                    nsfw, stickied, archived, locked, saved, true);
            post.setCrosspostParentId(crosspostParent.getId());
            return post;
        } else {
            return parseData(data, permalink, id, fullName, subredditName, subredditNamePrefixed,
                    author, formattedPostTime, postTime, title, previewUrl, thumbnailPreviewUrl,
                    previewWidth, previewHeight, score, voteType, gilded, nComments, flair, hidden, spoiler,
                    nsfw, stickied, archived, locked, saved, false);
        }
    }

    private static Post parseData(JSONObject data, String permalink, String id, String fullName,
                                  String subredditName, String subredditNamePrefixed, String author,
                                  String formattedPostTime, long postTimeMillis, String title, String previewUrl,
                                  String thumbnailPreviewUrl, int previewWidth, int previewHeight,
                                  int score, int voteType, int gilded, int nComments, String flair,
                                  boolean hidden, boolean spoiler, boolean nsfw, boolean stickied,
                                  boolean archived, boolean locked, boolean saved, boolean isCrosspost) throws JSONException {
        Post post;

        boolean isVideo = data.getBoolean(JSONUtils.IS_VIDEO_KEY);
        String url = Html.fromHtml(data.getString(JSONUtils.URL_KEY)).toString();

        if (!data.has(JSONUtils.PREVIEW_KEY) && previewUrl.equals("")) {
            if (url.contains(permalink)) {
                //Text post
                int postType = Post.TEXT_TYPE;
                post = new Post(id, fullName, subredditName, subredditNamePrefixed, author, formattedPostTime, postTimeMillis,
                        title, permalink, score, postType, voteType, gilded, nComments, flair,
                        hidden, spoiler, nsfw, stickied, archived, locked, saved, isCrosspost);
                if (data.isNull(JSONUtils.SELFTEXT_KEY)) {
                    post.setSelfText("");
                } else {
                    post.setSelfText(Utils.modifyMarkdown(data.getString(JSONUtils.SELFTEXT_KEY).trim()));
                    if (data.isNull(JSONUtils.SELFTEXT_HTML_KEY)) {
                        post.setSelfTextPlainTrimmed("");
                    } else {
                        String selfTextPlain = Utils.trimTrailingWhitespace(
                                Html.fromHtml(data.getString(JSONUtils.SELFTEXT_HTML_KEY))).toString();
                        post.setSelfTextPlain(selfTextPlain);
                        if (selfTextPlain.length() > 250) {
                            selfTextPlain = selfTextPlain.substring(0, 250);
                        }
                        post.setSelfTextPlainTrimmed(selfTextPlain);
                    }
                }
            } else {
                if (url.endsWith("jpg") || url.endsWith("png")) {
                    //Image post
                    int postType = Post.IMAGE_TYPE;

                    post = new Post(id, fullName, subredditName, subredditNamePrefixed, author,
                            formattedPostTime, postTimeMillis, title, url, thumbnailPreviewUrl, url, permalink,
                            score, postType, voteType, gilded, nComments, flair, hidden, spoiler,
                            nsfw, stickied, archived, locked, saved, isCrosspost);

                    post.setPreviewWidth(previewWidth);
                    post.setPreviewHeight(previewHeight);
                } else {
                    //No preview link post
                    int postType = Post.NO_PREVIEW_LINK_TYPE;
                    post = new Post(id, fullName, subredditName, subredditNamePrefixed, author, formattedPostTime, postTimeMillis,
                            title, previewUrl, thumbnailPreviewUrl, url, permalink, score, postType, voteType,
                            gilded, nComments, flair, hidden, spoiler, nsfw, stickied, archived, locked, saved, isCrosspost);
                    if (data.isNull(JSONUtils.SELFTEXT_KEY)) {
                        post.setSelfText("");
                    } else {
                        post.setSelfText(Utils.modifyMarkdown(data.getString(JSONUtils.SELFTEXT_KEY).trim()));
                    }
                }
            }
        } else {
            if (previewUrl.equals("")) {
                previewUrl = Html.fromHtml(data.getJSONObject(JSONUtils.PREVIEW_KEY).getJSONArray(JSONUtils.IMAGES_KEY).getJSONObject(0)
                        .getJSONObject(JSONUtils.SOURCE_KEY).getString(JSONUtils.URL_KEY)).toString();
            }

            if (isVideo) {
                //Video post
                JSONObject redditVideoObject = data.getJSONObject(JSONUtils.MEDIA_KEY).getJSONObject(JSONUtils.REDDIT_VIDEO_KEY);
                int postType = Post.VIDEO_TYPE;
                String videoUrl = Html.fromHtml(redditVideoObject.getString(JSONUtils.HLS_URL_KEY)).toString();
                String videoDownloadUrl = redditVideoObject.getString(JSONUtils.FALLBACK_URL_KEY);

                post = new Post(id, fullName, subredditName, subredditNamePrefixed, author, formattedPostTime, postTimeMillis,
                        title, previewUrl, thumbnailPreviewUrl, permalink, score, postType, voteType,
                        gilded, nComments, flair, hidden, spoiler, nsfw, stickied, archived, locked, saved, isCrosspost);

                post.setPreviewWidth(previewWidth);
                post.setPreviewHeight(previewHeight);
                post.setVideoUrl(videoUrl);
                post.setVideoDownloadUrl(videoDownloadUrl);
            } else if (data.has(JSONUtils.PREVIEW_KEY)) {
                if (data.getJSONObject(JSONUtils.PREVIEW_KEY).has(JSONUtils.REDDIT_VIDEO_PREVIEW_KEY)) {
                    //Gif video post (HLS)
                    int postType = Post.VIDEO_TYPE;
                    String videoUrl = Html.fromHtml(data.getJSONObject(JSONUtils.PREVIEW_KEY)
                            .getJSONObject(JSONUtils.REDDIT_VIDEO_PREVIEW_KEY).getString(JSONUtils.HLS_URL_KEY)).toString();
                    String videoDownloadUrl = data.getJSONObject(JSONUtils.PREVIEW_KEY)
                            .getJSONObject(JSONUtils.REDDIT_VIDEO_PREVIEW_KEY).getString(JSONUtils.FALLBACK_URL_KEY);

                    post = new Post(id, fullName, subredditName, subredditNamePrefixed, author,
                            formattedPostTime, postTimeMillis, title, previewUrl, thumbnailPreviewUrl, permalink, score,
                            postType, voteType, gilded, nComments, flair, hidden, spoiler, nsfw,
                            stickied, archived, locked, saved, isCrosspost);
                    post.setPreviewWidth(previewWidth);
                    post.setPreviewHeight(previewHeight);
                    post.setVideoUrl(videoUrl);
                    post.setVideoDownloadUrl(videoDownloadUrl);
                } else {
                    if (url.endsWith("jpg") || url.endsWith("png")) {
                        //Image post
                        int postType = Post.IMAGE_TYPE;

                        post = new Post(id, fullName, subredditName, subredditNamePrefixed, author,
                                formattedPostTime, postTimeMillis, title, url, thumbnailPreviewUrl, url, permalink,
                                score, postType, voteType, gilded, nComments, flair, hidden, spoiler,
                                nsfw, stickied, archived, locked, saved, isCrosspost);

                        post.setPreviewWidth(previewWidth);
                        post.setPreviewHeight(previewHeight);
                    } else if (url.endsWith("gif")){
                        //Gif post
                        int postType = Post.GIF_TYPE;
                        post = new Post(id, fullName, subredditName, subredditNamePrefixed, author,
                                formattedPostTime, postTimeMillis, title, previewUrl, thumbnailPreviewUrl, url, permalink,
                                score, postType, voteType, gilded, nComments, flair, hidden, spoiler,
                                nsfw, stickied, archived, locked, saved, isCrosspost);

                        post.setPreviewWidth(previewWidth);
                        post.setPreviewHeight(previewHeight);
                        post.setVideoUrl(url);
                    } else {
                        if (url.contains(permalink)) {
                            //Text post but with a preview
                            int postType = Post.TEXT_TYPE;

                            post = new Post(id, fullName, subredditName, subredditNamePrefixed, author,
                                    formattedPostTime, postTimeMillis, title, permalink, score, postType,
                                    voteType, gilded, nComments, flair, hidden, spoiler, nsfw, stickied,
                                    archived, locked, saved, isCrosspost);

                            post.setPreviewWidth(previewWidth);
                            post.setPreviewHeight(previewHeight);

                            if (data.isNull(JSONUtils.SELFTEXT_KEY)) {
                                post.setSelfText("");
                            } else {
                                post.setSelfText(Utils.modifyMarkdown(data.getString(JSONUtils.SELFTEXT_KEY).trim()));
                                if (data.isNull(JSONUtils.SELFTEXT_HTML_KEY)) {
                                    post.setSelfTextPlainTrimmed("");
                                } else {
                                    String selfTextPlain = Utils.trimTrailingWhitespace(
                                            Html.fromHtml(data.getString(JSONUtils.SELFTEXT_HTML_KEY))).toString();
                                    post.setSelfTextPlain(selfTextPlain);
                                    if (selfTextPlain.length() > 250) {
                                        selfTextPlain = selfTextPlain.substring(0, 250);
                                    }
                                    post.setSelfTextPlainTrimmed(selfTextPlain);
                                }
                            }
                        } else {
                            //Link post
                            int postType = Post.LINK_TYPE;

                            post = new Post(id, fullName, subredditName, subredditNamePrefixed, author,
                                    formattedPostTime, postTimeMillis, title, previewUrl, thumbnailPreviewUrl, url, permalink,
                                    score, postType, voteType, gilded, nComments, flair, hidden, spoiler,
                                    nsfw, stickied, archived, locked, saved, isCrosspost);
                            if (data.isNull(JSONUtils.SELFTEXT_KEY)) {
                                post.setSelfText("");
                            } else {
                                post.setSelfText(Utils.modifyMarkdown(data.getString(JSONUtils.SELFTEXT_KEY).trim()));
                            }

                            post.setPreviewWidth(previewWidth);
                            post.setPreviewHeight(previewHeight);
                        }
                    }
                }
            } else {
                if (url.endsWith("jpg") || url.endsWith("png")) {
                    //Image post
                    int postType = Post.IMAGE_TYPE;

                    post = new Post(id, fullName, subredditName, subredditNamePrefixed, author,
                            formattedPostTime, postTimeMillis, title, previewUrl, thumbnailPreviewUrl, url, permalink,
                            score, postType, voteType, gilded, nComments, flair, hidden, spoiler,
                            nsfw, stickied, archived, locked, saved, isCrosspost);
                    post.setPreviewWidth(previewWidth);
                    post.setPreviewHeight(previewHeight);
                } else {
                    //CP No Preview Link post
                    int postType = Post.NO_PREVIEW_LINK_TYPE;

                    post = new Post(id, fullName, subredditName, subredditNamePrefixed, author,
                            formattedPostTime, postTimeMillis, title, url, thumbnailPreviewUrl, url, permalink, score,
                            postType, voteType, gilded, nComments, flair, hidden, spoiler, nsfw, stickied,
                            archived, locked, saved, isCrosspost);
                }
            }
        }

        return post;
    }

    public interface ParsePostsListingListener {
        void onParsePostsListingSuccess(ArrayList<Post> newPostData, String lastItem);

        void onParsePostsListingFail();
    }

    public interface ParsePostListener {
        void onParsePostSuccess(Post post);

        void onParsePostFail();
    }

    private static class ParsePostDataAsyncTask extends AsyncTask<Void, Void, Void> {
        private JSONArray allData;
        private Locale locale;
        private int nPosts;
        private int filter;
        private boolean nsfw;
        private ParsePostsListingListener parsePostsListingListener;
        private ParsePostListener parsePostListener;
        private ArrayList<Post> newPosts;
        private Post post;
        private String lastItem;
        private boolean parseFailed;

        ParsePostDataAsyncTask(String response, Locale locale, int nPosts, int filter, boolean nsfw,
                               ParsePostsListingListener parsePostsListingListener) {
            this.parsePostsListingListener = parsePostsListingListener;
            try {
                JSONObject jsonResponse = new JSONObject(response);
                allData = jsonResponse.getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.CHILDREN_KEY);
                lastItem = jsonResponse.getJSONObject(JSONUtils.DATA_KEY).getString(JSONUtils.AFTER_KEY);
                this.locale = locale;
                this.nPosts = nPosts;
                this.filter = filter;
                this.nsfw = nsfw;
                newPosts = new ArrayList<>();
                parseFailed = false;
            } catch (JSONException e) {
                e.printStackTrace();
                parseFailed = true;
            }
        }

        ParsePostDataAsyncTask(String response, Locale locale, boolean nsfw,
                               ParsePostListener parsePostListener) {
            this.parsePostListener = parsePostListener;
            try {
                allData = new JSONArray(response).getJSONObject(0).getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.CHILDREN_KEY);
                this.locale = locale;
                this.nsfw = nsfw;
                parseFailed = false;
            } catch (JSONException e) {
                e.printStackTrace();
                parseFailed = true;
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (parseFailed) {
                return null;
            }

            if (newPosts == null) {
                //Only one post
                if (allData.length() == 0) {
                    parseFailed = true;
                    return null;
                }

                try {
                    JSONObject data = allData.getJSONObject(0).getJSONObject(JSONUtils.DATA_KEY);
                    post = parseBasicData(data, locale);
                } catch (JSONException e) {
                    e.printStackTrace();
                    parseFailed = true;
                }
            } else {
                //Posts listing
                int size;
                if (nPosts < 0 || nPosts > allData.length()) {
                    size = allData.length();
                } else {
                    size = nPosts;
                }

                for (int i = 0; i < size; i++) {
                    try {
                        if (allData.getJSONObject(i).getString(JSONUtils.KIND_KEY).equals("t3")) {
                            JSONObject data = allData.getJSONObject(i).getJSONObject(JSONUtils.DATA_KEY);
                            Post post = parseBasicData(data, locale);
                            if (post != null && !(!nsfw && post.isNSFW())) {
                                if (filter == PostFragment.EXTRA_NO_FILTER) {
                                    newPosts.add(post);
                                } else if (filter == post.getPostType()) {
                                    newPosts.add(post);
                                } else if (filter == Post.LINK_TYPE && post.getPostType() == Post.NO_PREVIEW_LINK_TYPE) {
                                    newPosts.add(post);
                                } else if (filter == Post.NSFW_TYPE && post.isNSFW()) {
                                    newPosts.add(post);
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (!parseFailed) {
                if (newPosts != null) {
                    parsePostsListingListener.onParsePostsListingSuccess(newPosts, lastItem);
                } else {
                    parsePostListener.onParsePostSuccess(post);
                }
            } else {
                if (parsePostsListingListener != null) {
                    parsePostsListingListener.onParsePostsListingFail();
                } else {
                    parsePostListener.onParsePostFail();
                }
            }
        }
    }
}
