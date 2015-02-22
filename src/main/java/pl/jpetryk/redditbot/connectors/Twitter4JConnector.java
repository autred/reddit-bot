package pl.jpetryk.redditbot.connectors;


import org.joda.time.DateTime;
import pl.jpetryk.redditbot.exceptions.TwitterApiException;
import pl.jpetryk.redditbot.model.Tweet;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by Jan on 09/01/15.
 */
public class Twitter4JConnector implements TwitterConnectorInterface {

    private Twitter twitter;

    public Twitter4JConnector(String apiKey, String apiSecret, String accessToken, String accessTokenSecret) {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setOAuthConsumerKey(apiKey);
        configurationBuilder.setOAuthConsumerSecret(apiSecret);
        configurationBuilder.setOAuthAccessToken(accessToken);
        configurationBuilder.setOAuthAccessTokenSecret(accessTokenSecret);
        TwitterFactory twitterFactory = new TwitterFactory(configurationBuilder.build());
        twitter = twitterFactory.getInstance();
    }

    @Override
    public Tweet showStatus(Long id) throws TwitterApiException {
        try {
            Status status = twitter.showStatus(id);
            Tweet.Builder tweetBuilder = new Tweet.Builder()
                    .body(status.getText())
                    .datePosted(new DateTime(status.getCreatedAt()))
                    .id(id)
                    .poster(status.getUser().getScreenName());
            prepareEntities(status, tweetBuilder);
            return tweetBuilder.build();
        } catch (TwitterException e) {
            throw new TwitterApiException(e, e.exceededRateLimitation(),
                    (e.getRateLimitStatus() != null && e.getRateLimitStatus().getSecondsUntilReset() > 0) ?
                            e.getRateLimitStatus().getSecondsUntilReset() * 1000 : 0, e.getErrorCode());
        }
    }

    private void prepareEntities(Status status, Tweet.Builder tweetBuilder) {
        for (URLEntity urlEntity : status.getURLEntities()) {
            String resultUrl = getNoParticipationRedditLink(urlEntity.getExpandedURL());
            tweetBuilder.addUrlEntity(urlEntity.getURL(), resultUrl);
        }
        for (MediaEntity mediaEntity : status.getMediaEntities()) {
            tweetBuilder.addImageEntity(mediaEntity.getURL(), mediaEntity.getMediaURL());
        }
    }

    private String getNoParticipationRedditLink(String url) {
        String resultUrl;
        if(url.toLowerCase().contains("reddit.com")){
            resultUrl = url.replace("reddit.com", "np.reddit.com");
        }else{
            resultUrl = url;
        }
        return resultUrl;
    }

}
