package in.semibit.media.followerbot;

import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.requests.friendships.FriendshipsActionRequest;
import com.github.instagram4j.instagram4j.responses.friendships.FriendshipStatusResponse;
import com.semibit.ezandroidutils.EzUtils;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import in.semibit.media.common.LogsViewModel;
import in.semibit.media.common.igclientext.IGrequestHelper;

public class AccountsAPIHelper {
    String followNavPath = "MainFeedFragment:feed_timeline:1:cold_start:10#230#301:2935540002369486928,UserDetailFragment:profile:2:media_owner::,ProfileMediaTabFragment:profile:3:button::,FollowListFragment:following:4:button::,FollowListFragment:followers:5:button::";

    IGClient igClient;
    IGrequestHelper iGrequestHelper;

    public AccountsAPIHelper(IGClient igClient) {
        this.igClient = igClient;
        iGrequestHelper = new IGrequestHelper(igClient);
    }


    private HashMap<String, String> getFromSplitString(String split) {
        HashMap<String, String> map = new HashMap<>();
        try {
            String lines[] = split.split("\n");
            for (String line : lines) {
                String header[] = line.split(":");
                map.put(header[0].trim(), header[1].trim());
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return map;
    }

    private HashMap<String, String> getBasicHeaders() {
        HashMap<String, String> map = new HashMap<>(getFromSplitString(
                "Ig-U-Ds-User-Id: " + getUserId() + "\n" +
                        "Ig-Intended-User-Id: " + getUserId() + "\n" +
                        "X-Fb-Http-Engine: Liger\n" +
                        "X-Fb-Client-Ip: True\n" +
                        "X-Bloks-Version-Id: ed06b936be88562bdc1a13aa16ef14521a460edaf0bd1c6d45748e2c542525a1"
        ));
        return map;
    }

    private String getUserId() {
        return "" + igClient.getSelfProfile().getPk();
    }


    public static final String ACTION_FOLLOW = "create",
            ACTION_UNFOLLOW = "destroy",
            ACTION_VIEW = "show";

    /**
     * @param targetUser
     * @param action     create | show | destroy
     * @return
     */
    public CompletableFuture<String> follow(FollowUserModel targetUser, String action) {
        try {

//        POST /api/v1/friendships/create/55071173026/ HTTP/2
//        POST /api/v1/friendships/show/55071173026/ HTTP/2
//        POST /api/v1/friendships/destroy/55071173026/ HTTP/2

//        String body = "{\n" +
//                "  \"user_id\": \"55071173026\",\n" +
//                "  \"radio_type\": \"wifi-none\",\n" +
//                "  \"_uid\": \"55063792127\",\n" +
//                "  \"device_id\": \"android-ba9156177f99d2ee\",\n" +
//                "  \"_uuid\": \"7397b647-0663-4d02-9746-8cd93c61e6f1\",\n" +
//                "  \"nav_chain\": \"MainFeedFragment:feed_timeline:1:cold_start:10#230#301:2935540002369486928,UserDetailFragment:profile:2:media_owner::,ProfileMediaTabFragment:profile:3:button::,FollowListFragment:following:4:button::,FollowListFragment:followers:5:button::\"\n" +
//                "}";
//
//            JSONObject postBodyObj = new JSONObject(body);
//            postBodyObj.put("_uuid", igClient.getGuid());
//            postBodyObj.put("_uid", "" + igClient.getSelfProfile().getPk());
//            postBodyObj.put("user_id", targetUser.getId());
//            postBodyObj.put("_uuid", IGUtils.randomUuid());
//
//            String targetUserId = targetUser.getId();
//
//            CompletableFuture<String> future = CompletableFuture.supplyAsync(new Supplier<String>() {
//                @Override
//                public String get() {
//                    String payload =
//                            "signed_body=SIGNATURE.%7B%22user_id%22%3A%2255071173026%22%2C%22radio_type%22%3A%22wifi-none%22%2C%22_uid%22%3A%2255063792127%22%2C%22device_id%22%3A%22android-ba9156177f99d2ee%22%2C%22_uuid%22%3A%227397b647-0663-4d02-9746-8cd93c61e6f1%22%2C%22nav_chain%22%3A%22MainFeedFragment%3Afeed_timeline%3A1%3Acold_start%3A10%23230%23301%3A2935540002369486928%2CUserDetailFragment%3Aprofile%3A2%3Amedia_owner%3A%3A%2CProfileMediaTabFragment%3Aprofile%3A3%3Abutton%3A%3A%2CFollowListFragment%3Afollowing%3A4%3Abutton%3A%3A%2CFollowListFragment%3Afollowers%3A5%3Abutton%3A%3A%22%7D";
//                    payload = payload.replaceAll("55063792127", "" + igClient.getSelfProfile().getPk());
//                    payload = payload.replaceAll("55071173026", targetUserId);
//                    payload = payload.replaceAll("android-ba9156177f99d2ee", igClient.getDeviceId());
//
//                    Map<String, String> basic = getBasicHeaders();
//                    basic.put("content-type", "application/x-www-form-urlencoded; charset=UTF-8");
//                    String followResponse = iGrequestHelper.doIGPost("/api/v1/friendships/" + action + "/" + targetUserId,
//                            payload, false, basic);
//                    LogsViewModel.addToLog("follow " + targetUser.userName + " : " + followResponse);
//                    return followResponse;
//                }
//            });
//            return future;

            CompletableFuture<String> future = new CompletableFuture<String>();
            CompletableFuture<FriendshipStatusResponse> req = new FriendshipsActionRequest(Long.parseLong(targetUser.getId()),
                    FriendshipsActionRequest.FriendshipsAction.CREATE).execute(igClient);
            req.exceptionally(e -> {
                e.printStackTrace();
                LogsViewModel.addToLog("error sending friend req " + e.getMessage());
                return null;
            }).thenAccept(resp -> {
                if (resp != null && resp.getStatusCode() < 300) {
                    future.complete(EzUtils.js.toJson(resp));
                } else {
                    future.complete("");
                }
            });
            return future;
        } catch (Exception e) {
            e.printStackTrace();
            LogsViewModel.addToLog("error sending friend req " + e.getMessage());
            return CompletableFuture.completedFuture("");
        }

//        Host: i.instagram.com
//        X-Ig-App-Locale: en_US
//        X-Ig-Device-Locale: en_US
//        X-Ig-Mapped-Locale: en_US
//        X-Pigeon-Session-Id: UFS-efbc32aa-89b7-4684-8735-5fbe3eff0b00-0
//        X-Pigeon-Rawclienttime: 1664201511.089
//        X-Ig-Bandwidth-Speed-Kbps: 717.000
//        X-Ig-Bandwidth-Totalbytes-B: 4508945
//        X-Ig-Bandwidth-Totaltime-Ms: 7070
//        X-Ig-App-Startup-Country: IN
//        X-Bloks-Version-Id: ed06b936be88562bdc1a13aa16ef14521a460edaf0bd1c6d45748e2c542525a1
//        X-Ig-Www-Claim: hmac.AR1XkNJoSiVg2SCrdp1xCZBYbs4kSLhWjxBlReNkyEzA8mrK
//        X-Bloks-Is-Layout-Rtl: false
//        X-Ig-Device-Id: 7397b647-0663-4d02-9746-8cd93c61e6f1
//        X-Ig-Family-Device-Id: 5f55170a-1070-4739-bcb5-3f9a7d8843e7
//        X-Ig-Android-Id: android-ba9156177f99d2ee
//        X-Ig-Timezone-Offset: 19800
//        X-Ig-Nav-Chain: MainFeedFragment:feed_timeline:1:cold_start:10#230#301:2935540002369486928,UserDetailFragment:profile:2:media_owner::,ProfileMediaTabFragment:profile:3:button::,FollowListFragment:following:4:button::,FollowListFragment:followers:5:button::
//                X-Fb-Connection-Type: WIFI
//        X-Ig-Connection-Type: WIFI
//        X-Ig-Capabilities: 3brTv10=
//                X-Ig-App-Id: 567067343352427
//        Priority: u=3
//        User-Agent: Instagram 252.0.0.17.111 Android (29/10; 400dpi; 1080x2040; Google/google; Android SDK built for x86; generic_x86; ranchu; en_US; 397702078)
//        Accept-Language: en-US
//        Authorization: Bearer IGT:2:eyJkc191c2VyX2lkIjoiNTUwNjM3OTIxMjciLCJzZXNzaW9uaWQiOiI1NTA2Mzc5MjEyNyUzQUZDNUFxVzJWUFlLeWdYJTNBMTMlM0FBWWUyUmE0R2d3U0xxTDRwUm5SeHJRNEdiMnplZmY0Z2EtRG91eWFJSXcifQ==
//                X-Mid: YyVeOwABAAFK3Xt0NoMq8MlI48Zo
//        Ig-U-Shbid: 10760,55063792127,1695736534:01f70a7940ea9f9d62513b6945df113033cf399dd11075652cacf878946a2c0be09c94fe
//        Ig-U-Shbts: 1664200534,55063792127,1695736534:01f7ab224e08d20a80c72d4c83fcfcf348fc0682ce09acc2482f61a2fb89ec0fdb7cbffc
//        Ig-U-Ds-User-Id: 55063792127
//        Ig-U-Rur: VLL,55063792127,1695737511:01f76f5679c8b99d9f86f8c6c56f69051e25501a8d6640ef3e2a419fce511ea2303a41b0
//        Ig-Intended-User-Id: 55063792127
//        Content-Type: application/x-www-form-urlencoded; charset=UTF-8
//        Content-Length: 570
//        Accept-Encoding: gzip, deflate
//        X-Fb-Http-Engine: Liger
//        X-Fb-Client-Ip: True
//        X-Fb-Server-Cluster: True
//
//                signed_body=SIGNATURE.%7B%22user_id%22%3A%2255071173026%22%2C%22radio_type%22%3A%22wifi-none%22%2C%22_uid%22%3A%2255063792127%22%2C%22device_id%22%3A%22android-ba9156177f99d2ee%22%2C%22_uuid%22%3A%227397b647-0663-4d02-9746-8cd93c61e6f1%22%2C%22nav_chain%22%3A%22MainFeedFragment%3Afeed_timeline%3A1%3Acold_start%3A10%23230%23301%3A2935540002369486928%2CUserDetailFragment%3Aprofile%3A2%3Amedia_owner%3A%3A%2CProfileMediaTabFragment%3Aprofile%3A3%3Abutton%3A%3A%2CFollowListFragment%3Afollowing%3A4%3Abutton%3A%3A%2CFollowListFragment%3Afollowers%3A5%3Abutton%3A%3A%22%7D

    }


    public CompletableFuture<Boolean> unFollow() {
        /*
        POST /api/v1/friendships/destroy/44372004016/ HTTP/2
Host: i.instagram.com
X-Ig-App-Locale: en_US
X-Ig-Device-Locale: en_US
X-Ig-Mapped-Locale: en_US
X-Pigeon-Session-Id: UFS-efbc32aa-89b7-4684-8735-5fbe3eff0b00-0
X-Pigeon-Rawclienttime: 1664202113.317
X-Ig-Bandwidth-Speed-Kbps: 717.000
X-Ig-Bandwidth-Totalbytes-B: 4508945
X-Ig-Bandwidth-Totaltime-Ms: 7070
X-Ig-App-Startup-Country: IN
X-Bloks-Version-Id: ed06b936be88562bdc1a13aa16ef14521a460edaf0bd1c6d45748e2c542525a1
X-Ig-Www-Claim: hmac.AR1XkNJoSiVg2SCrdp1xCZBYbs4kSLhWjxBlReNkyEzA8mrK
X-Bloks-Is-Layout-Rtl: false
X-Ig-Device-Id: 7397b647-0663-4d02-9746-8cd93c61e6f1
X-Ig-Family-Device-Id: 5f55170a-1070-4739-bcb5-3f9a7d8843e7
X-Ig-Android-Id: android-ba9156177f99d2ee
X-Ig-Timezone-Offset: 19800
X-Ig-Nav-Chain: MainFeedFragment:feed_timeline:1:cold_start:10#230#301:2935540002369486928,UserDetailFragment:profile:2:media_owner::,ProfileMediaTabFragment:profile:3:button::,FollowListFragment:following:4:button::,FollowListFragment:followers:5:button::
X-Fb-Connection-Type: WIFI
X-Ig-Connection-Type: WIFI
X-Ig-Capabilities: 3brTv10=
X-Ig-App-Id: 567067343352427
Priority: u=3
User-Agent: Instagram 252.0.0.17.111 Android (29/10; 400dpi; 1080x2040; Google/google; Android SDK built for x86; generic_x86; ranchu; en_US; 397702078)
Accept-Language: en-US
Authorization: Bearer IGT:2:eyJkc191c2VyX2lkIjoiNTUwNjM3OTIxMjciLCJzZXNzaW9uaWQiOiI1NTA2Mzc5MjEyNyUzQUZDNUFxVzJWUFlLeWdYJTNBMTMlM0FBWWUyUmE0R2d3U0xxTDRwUm5SeHJRNEdiMnplZmY0Z2EtRG91eWFJSXcifQ==
X-Mid: YyVeOwABAAFK3Xt0NoMq8MlI48Zo
Ig-U-Shbid: 10760,55063792127,1695736534:01f70a7940ea9f9d62513b6945df113033cf399dd11075652cacf878946a2c0be09c94fe
Ig-U-Shbts: 1664200534,55063792127,1695736534:01f7ab224e08d20a80c72d4c83fcfcf348fc0682ce09acc2482f61a2fb89ec0fdb7cbffc
Ig-U-Ds-User-Id: 55063792127
Ig-U-Rur: ODN,55063792127,1695738051:01f7d83b6b764c919d72c93c15d9f4a910186e19af7ef36aac2b1ce680e94a93336ef05c
Ig-Intended-User-Id: 55063792127
Content-Type: application/x-www-form-urlencoded; charset=UTF-8
Content-Length: 562
Accept-Encoding: gzip, deflate
X-Fb-Http-Engine: Liger
X-Fb-Client-Ip: True
X-Fb-Server-Cluster: True

signed_body=SIGNATURE.%7B%22user_id%22%3A%2244372004016%22%2C%22radio_type%22%3A%22wifi-none%22%2C%22_uid%22%3A%2255063792127%22%2C%22_uuid%22%3A%227397b647-0663-4d02-9746-8cd93c61e6f1%22%2C%22nav_chain%22%3A%22MainFeedFragment%3Afeed_timeline%3A1%3Acold_start%3A10%23230%23301%3A2935540002369486928%2CUserDetailFragment%3Aprofile%3A2%3Amedia_owner%3A%3A%2CProfileMediaTabFragment%3Aprofile%3A3%3Abutton%3A%3A%2CFollowListFragment%3Afollowing%3A4%3Abutton%3A%3A%2CFollowListFragment%3Afollowers%3A5%3Abutton%3A%3A%22%2C%22container_module%22%3A%22followers%22%7D
         */


        CompletableFuture<Boolean> onComplete = new CompletableFuture<>();

        return onComplete;
    }

    public CompletableFuture<Boolean> like() {
/*

POST /api/v1/media/2703831611082089389_50050906160/like/ HTTP/2
Host: i.instagram.com
X-Ig-App-Locale: en_US
X-Ig-Device-Locale: en_US
X-Ig-Mapped-Locale: en_US
X-Pigeon-Session-Id: UFS-efbc32aa-89b7-4684-8735-5fbe3eff0b00-0
X-Pigeon-Rawclienttime: 1664202530.075
X-Ig-Bandwidth-Speed-Kbps: 1519.000
X-Ig-Bandwidth-Totalbytes-B: 9503471
X-Ig-Bandwidth-Totaltime-Ms: 11369
X-Ig-App-Startup-Country: IN
X-Bloks-Version-Id: ed06b936be88562bdc1a13aa16ef14521a460edaf0bd1c6d45748e2c542525a1
X-Ig-Www-Claim: hmac.AR1XkNJoSiVg2SCrdp1xCZBYbs4kSLhWjxBlReNkyEzA8mrK
X-Bloks-Is-Layout-Rtl: false
X-Ig-Device-Id: 7397b647-0663-4d02-9746-8cd93c61e6f1
X-Ig-Family-Device-Id: 5f55170a-1070-4739-bcb5-3f9a7d8843e7
X-Ig-Android-Id: android-ba9156177f99d2ee
X-Ig-Timezone-Offset: 19800
X-Ig-Nav-Chain: MainFeedFragment:feed_timeline:1:cold_start:10#230#301:2935540002369486928,UserDetailFragment:profile:2:media_owner::,ProfileMediaTabFragment:profile:3:button::,FollowListFragment:following:4:button::,FollowListFragment:followers:5:button::,UserDetailFragment:profile:10:button::,ContextualFeedFragment:feed_contextual_profile:15:button::
X-Fb-Connection-Type: WIFI
X-Ig-Connection-Type: WIFI
X-Ig-Capabilities: 3brTv10=
X-Ig-App-Id: 567067343352427
Priority: u=3
User-Agent: Instagram 252.0.0.17.111 Android (29/10; 400dpi; 1080x2040; Google/google; Android SDK built for x86; generic_x86; ranchu; en_US; 397702078)
Accept-Language: en-US
Authorization: Bearer IGT:2:eyJkc191c2VyX2lkIjoiNTUwNjM3OTIxMjciLCJzZXNzaW9uaWQiOiI1NTA2Mzc5MjEyNyUzQUZDNUFxVzJWUFlLeWdYJTNBMTMlM0FBWWUyUmE0R2d3U0xxTDRwUm5SeHJRNEdiMnplZmY0Z2EtRG91eWFJSXcifQ==
X-Mid: YyVeOwABAAFK3Xt0NoMq8MlI48Zo
Ig-U-Shbid: 10760,55063792127,1695736534:01f70a7940ea9f9d62513b6945df113033cf399dd11075652cacf878946a2c0be09c94fe
Ig-U-Shbts: 1664200534,55063792127,1695736534:01f7ab224e08d20a80c72d4c83fcfcf348fc0682ce09acc2482f61a2fb89ec0fdb7cbffc
Ig-U-Ds-User-Id: 55063792127
Ig-U-Rur: VLL,55063792127,1695738526:01f7bf4298a65cd5a99663863b53bf448ba0eb819bd01a632e4002589fdb773d462cf91b
Ig-Intended-User-Id: 55063792127
Content-Type: application/x-www-form-urlencoded; charset=UTF-8
Content-Length: 874
Accept-Encoding: gzip, deflate
X-Fb-Http-Engine: Liger
X-Fb-Client-Ip: True
X-Fb-Server-Cluster: True

signed_body=SIGNATURE.%7B%22delivery_class%22%3A%22organic%22%2C%22tap_source%22%3A%22button%22%2C%22media_id%22%3A%222703831611082089389_50050906160%22%2C%22radio_type%22%3A%22wifi-none%22%2C%22_uid%22%3A%2255063792127%22%2C%22_uuid%22%3A%227397b647-0663-4d02-9746-8cd93c61e6f1%22%2C%22nav_chain%22%3A%22MainFeedFragment%3Afeed_timeline%3A1%3Acold_start%3A10%23230%23301%3A2935540002369486928%2CUserDetailFragment%3Aprofile%3A2%3Amedia_owner%3A%3A%2CProfileMediaTabFragment%3Aprofile%3A3%3Abutton%3A%3A%2CFollowListFragment%3Afollowing%3A4%3Abutton%3A%3A%2CFollowListFragment%3Afollowers%3A5%3Abutton%3A%3A%2CUserDetailFragment%3Aprofile%3A10%3Abutton%3A%3A%2CContextualFeedFragment%3Afeed_contextual_profile%3A15%3Abutton%3A%3A%22%2C%22is_carousel_bumped_post%22%3A%22false%22%2C%22container_module%22%3A%22feed_contextual_profile%22%2C%22feed_position%22%3A%228%22%7D&d=0
 */

        CompletableFuture<Boolean> onComplete = new CompletableFuture<>();

        return onComplete;

    }

    public CompletableFuture<Boolean> feed() {

        CompletableFuture<Boolean> onComplete = new CompletableFuture<>();

        /*
        GET /api/v1/feed/user/15952198849/?exclude_comment=true&only_fetch_first_carousel_media=false HTTP/2
Host: i.instagram.com
X-Ig-App-Locale: en_US
X-Ig-Device-Locale: en_US
X-Ig-Mapped-Locale: en_US
X-Pigeon-Session-Id: UFS-efbc32aa-89b7-4684-8735-5fbe3eff0b00-0
X-Pigeon-Rawclienttime: 1664202642.806
X-Ig-Bandwidth-Speed-Kbps: 1519.000
X-Ig-Bandwidth-Totalbytes-B: 9503471
X-Ig-Bandwidth-Totaltime-Ms: 11369
X-Ig-App-Startup-Country: IN
X-Bloks-Version-Id: ed06b936be88562bdc1a13aa16ef14521a460edaf0bd1c6d45748e2c542525a1
X-Ig-Www-Claim: hmac.AR1XkNJoSiVg2SCrdp1xCZBYbs4kSLhWjxBlReNkyEzA8mrK
X-Bloks-Is-Layout-Rtl: false
X-Ig-Device-Id: 7397b647-0663-4d02-9746-8cd93c61e6f1
X-Ig-Family-Device-Id: 5f55170a-1070-4739-bcb5-3f9a7d8843e7
X-Ig-Android-Id: android-ba9156177f99d2ee
X-Ig-Timezone-Offset: 19800
X-Ig-Nav-Chain: MainFeedFragment:feed_timeline:1:cold_start:10#230#301:2935540002369486928,UserDetailFragment:profile:2:media_owner::,ProfileMediaTabFragment:profile:3:button::,FollowListFragment:followers:16:back::
X-Ig-Salt-Ids: 383984041
X-Fb-Connection-Type: WIFI
X-Ig-Connection-Type: WIFI
X-Ig-Capabilities: 3brTv10=
X-Ig-App-Id: 567067343352427
Priority: u=3
User-Agent: Instagram 252.0.0.17.111 Android (29/10; 400dpi; 1080x2040; Google/google; Android SDK built for x86; generic_x86; ranchu; en_US; 397702078)
Accept-Language: en-US
Authorization: Bearer IGT:2:eyJkc191c2VyX2lkIjoiNTUwNjM3OTIxMjciLCJzZXNzaW9uaWQiOiI1NTA2Mzc5MjEyNyUzQUZDNUFxVzJWUFlLeWdYJTNBMTMlM0FBWWUyUmE0R2d3U0xxTDRwUm5SeHJRNEdiMnplZmY0Z2EtRG91eWFJSXcifQ==
X-Mid: YyVeOwABAAFK3Xt0NoMq8MlI48Zo
Ig-U-Shbid: 10760,55063792127,1695736534:01f70a7940ea9f9d62513b6945df113033cf399dd11075652cacf878946a2c0be09c94fe
Ig-U-Shbts: 1664200534,55063792127,1695736534:01f7ab224e08d20a80c72d4c83fcfcf348fc0682ce09acc2482f61a2fb89ec0fdb7cbffc
Ig-U-Ds-User-Id: 55063792127
Ig-U-Rur: VLL,55063792127,1695738638:01f7b7cf3953afa0da6f9970370d68210250cdfa4273d7475f1e7f6e35399c33df96e02f
Ig-Intended-User-Id: 55063792127
Accept-Encoding: gzip, deflate
X-Fb-Http-Engine: Liger
X-Fb-Client-Ip: True
X-Fb-Server-Cluster: True


         */
        return onComplete;

    }
}
