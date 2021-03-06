package ml.docilealligator.infinityforreddit.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.text.style.SuperscriptSpan;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.MarkwonConfiguration;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.linkify.LinkifyPlugin;
import io.noties.markwon.simple.ext.SimpleExtPlugin;
import ml.docilealligator.infinityforreddit.Activity.LinkResolverActivity;
import ml.docilealligator.infinityforreddit.Activity.ViewUserDetailActivity;
import ml.docilealligator.infinityforreddit.Message;
import ml.docilealligator.infinityforreddit.NetworkState;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.ReadMessage;
import retrofit2.Retrofit;

public class MessageRecyclerViewAdapter extends PagedListAdapter<Message, RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_DATA = 0;
    private static final int VIEW_TYPE_ERROR = 1;
    private static final int VIEW_TYPE_LOADING = 2;
    private static final DiffUtil.ItemCallback<Message> DIFF_CALLBACK = new DiffUtil.ItemCallback<Message>() {
        @Override
        public boolean areItemsTheSame(@NonNull Message message, @NonNull Message t1) {
            return message.getId().equals(t1.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Message message, @NonNull Message t1) {
            return message.getBody().equals(t1.getBody());
        }
    };
    private Context mContext;
    private Retrofit mOauthRetrofit;
    private Markwon mMarkwon;
    private String mAccessToken;
    private Resources mResources;
    private NetworkState networkState;
    private RetryLoadingMoreCallback mRetryLoadingMoreCallback;
    private int mMessageBackgroundColor;

    public MessageRecyclerViewAdapter(Context context, Retrofit oauthRetrofit, String accessToken,
                                      RetryLoadingMoreCallback retryLoadingMoreCallback) {
        super(DIFF_CALLBACK);
        mContext = context;
        mOauthRetrofit = oauthRetrofit;
        mRetryLoadingMoreCallback = retryLoadingMoreCallback;
        mMarkwon = Markwon.builder(mContext)
                .usePlugin(new AbstractMarkwonPlugin() {
                    @Override
                    public void configureConfiguration(@NonNull MarkwonConfiguration.Builder builder) {
                        builder.linkResolver((view, link) -> {
                            Intent intent = new Intent(mContext, LinkResolverActivity.class);
                            Uri uri = Uri.parse(link);
                            if (uri.getScheme() == null && uri.getHost() == null) {
                                intent.setData(LinkResolverActivity.getRedditUriByPath(link));
                            } else {
                                intent.setData(uri);
                            }
                            mContext.startActivity(intent);
                        });
                    }
                })
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(LinkifyPlugin.create(Linkify.WEB_URLS))
                .usePlugin(SimpleExtPlugin.create(plugin ->
                                plugin.addExtension(1, '^', (configuration, props) -> {
                                    return new SuperscriptSpan();
                                })
                        )
                )
                .build();
        mAccessToken = accessToken;
        mResources = context.getResources();

        TypedValue typedValue = new TypedValue();
        mContext.getTheme().resolveAttribute(R.attr.cardViewBackgroundColor, typedValue, true);
        mMessageBackgroundColor = typedValue.data;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_DATA) {
            return new DataViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false));
        } else if (viewType == VIEW_TYPE_ERROR) {
            return new ErrorViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_footer_error, parent, false));
        } else {
            return new LoadingViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_footer_loading, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DataViewHolder) {
            Message message = getItem(holder.getAdapterPosition());
            if (message != null) {
                if (message.isNew()) {
                    ((DataViewHolder) holder).itemView.setBackgroundColor(
                            mResources.getColor(R.color.unreadMessageBackgroundColor));
                }

                if (message.wasComment()) {
                    ((DataViewHolder) holder).authorTextView.setTextColor(mResources.getColor(R.color.colorPrimaryDarkDayNightTheme));
                    ((DataViewHolder) holder).titleTextView.setText(message.getTitle());
                } else {
                    ((DataViewHolder) holder).titleTextView.setVisibility(View.GONE);
                }

                ((DataViewHolder) holder).authorTextView.setText(message.getAuthor());
                String subject = message.getSubject().substring(0, 1).toUpperCase() + message.getSubject().substring(1);
                ((DataViewHolder) holder).subjectTextView.setText(subject);
                mMarkwon.setMarkdown(((DataViewHolder) holder).contentCustomMarkwonView, message.getBody());

                ((DataViewHolder) holder).itemView.setOnClickListener(view -> {
                    if (message.getContext() != null && !message.getContext().equals("")) {
                        Uri uri = LinkResolverActivity.getRedditUriByPath(message.getContext());
                        Intent intent = new Intent(mContext, LinkResolverActivity.class);
                        intent.setData(uri);
                        mContext.startActivity(intent);
                    }

                    if (message.isNew()) {
                        ((DataViewHolder) holder).itemView.setBackgroundColor(mMessageBackgroundColor);
                        message.setNew(false);

                        ReadMessage.readMessage(mOauthRetrofit, mAccessToken, message.getFullname(),
                                new ReadMessage.ReadMessageListener() {
                                    @Override
                                    public void readSuccess() {
                                    }

                                    @Override
                                    public void readFailed() {
                                        message.setNew(true);
                                        ((DataViewHolder) holder).itemView.setBackgroundColor(mResources.getColor(R.color.unreadMessageBackgroundColor));
                                    }
                                });
                    }
                });

                ((DataViewHolder) holder).authorTextView.setOnClickListener(view -> {
                    if (message.wasComment()) {
                        Intent intent = new Intent(mContext, ViewUserDetailActivity.class);
                        intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, message.getAuthor());
                        mContext.startActivity(intent);
                    }
                });

                ((DataViewHolder) holder).contentCustomMarkwonView.setOnClickListener(view -> ((DataViewHolder) holder).itemView.performClick());
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        // Reached at the end
        if (hasExtraRow() && position == getItemCount() - 1) {
            if (networkState.getStatus() == NetworkState.Status.LOADING) {
                return VIEW_TYPE_LOADING;
            } else {
                return VIEW_TYPE_ERROR;
            }
        } else {
            return VIEW_TYPE_DATA;
        }
    }

    @Override
    public int getItemCount() {
        if (hasExtraRow()) {
            return super.getItemCount() + 1;
        }
        return super.getItemCount();
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof DataViewHolder) {
            ((DataViewHolder) holder).itemView.setBackgroundColor(mMessageBackgroundColor);
            ((DataViewHolder) holder).titleTextView.setVisibility(View.VISIBLE);
            ((DataViewHolder) holder).authorTextView.setTextColor(mResources.getColor(R.color.primaryTextColor));
        }
    }

    private boolean hasExtraRow() {
        return networkState != null && networkState.getStatus() != NetworkState.Status.SUCCESS;
    }

    public void setNetworkState(NetworkState newNetworkState) {
        NetworkState previousState = this.networkState;
        boolean previousExtraRow = hasExtraRow();
        this.networkState = newNetworkState;
        boolean newExtraRow = hasExtraRow();
        if (previousExtraRow != newExtraRow) {
            if (previousExtraRow) {
                notifyItemRemoved(super.getItemCount());
            } else {
                notifyItemInserted(super.getItemCount());
            }
        } else if (newExtraRow && !previousState.equals(newNetworkState)) {
            notifyItemChanged(getItemCount() - 1);
        }
    }

    public interface RetryLoadingMoreCallback {
        void retryLoadingMore();
    }

    class DataViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        @BindView(R.id.author_text_view_item_message)
        TextView authorTextView;
        @BindView(R.id.subject_text_view_item_message)
        TextView subjectTextView;
        @BindView(R.id.title_text_view_item_message)
        TextView titleTextView;
        @BindView(R.id.content_custom_markwon_view_item_message)
        TextView contentCustomMarkwonView;

        DataViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.itemView = itemView;
        }
    }

    class ErrorViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.error_text_view_item_footer_error)
        TextView errorTextView;
        @BindView(R.id.retry_button_item_footer_error)
        Button retryButton;

        ErrorViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            errorTextView.setText(R.string.load_comments_failed);
            retryButton.setOnClickListener(view -> mRetryLoadingMoreCallback.retryLoadingMore());
        }
    }

    class LoadingViewHolder extends RecyclerView.ViewHolder {
        LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
