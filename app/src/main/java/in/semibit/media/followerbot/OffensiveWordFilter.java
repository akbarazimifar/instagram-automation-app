package in.semibit.media.followerbot;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import in.semibit.media.common.GenricDataCallback;
import in.semibit.media.common.igclientext.post.model.User;

public class OffensiveWordFilter implements Predicate<User> {

    GenricDataCallback logger;
    public OffensiveWordFilter(GenricDataCallback logger) {
        this.logger = logger;
    }

    @Override
    public boolean test(User user) {
        String userName = user.getUsername() + " " + user.getFullName();
        return offensiveWordStrings.stream().noneMatch(userName::contains);
    }


    final String offensiveWords = "\uD83D\uDC23,@,\uD83D\uDC1D,\uD83D\uDCAC,\uD83C\uDF46,\uD83C\uDF51,\uD83C\uDF77,\uD83D\uDE18,\uD83C\uDFB7,\uD83D\uDC83,\uD83D\uDC40\uD83D\uDC59,\uD83D\uDC50,\uD83C\uDF51,\uD83C\uDF51,\uD83C\uDF46,\uD83C\uDF51,\uD83C\uDF89,\uD83D\uDCAB\n" +
            "        ,\uD83C\uDF0B,\uD83D\uDE0D,\uD83D\uDE2E,\uD83C\uDF3A,\uD83D\uDC4C,\uD83D\uDCA6,\uD83D\uDC49,\uD83C\uDF69,\uD83C\uDF4C,\uD83C\uDF51,♡,♥,\uD83D\uDC95,❤,\uD83E\uDDE1,\uD83D\uDC9B,\uD83D\uDC9A,\uD83D\uDC99,\uD83D\uDC9C,\uD83E\uDD0E,\uD83D\uDDA4,\uD83D\uDE18,❤️,\uD83C\uDF0A,\uD83D\uDCA6,\uD83E\uDD70,\uD83E\uDD75,\uD83D\uDC97,\uD83D\uDC93,\uD83D\uDC95,\uD83D\uDC98,\uD83D\uDC8B,❣️,\uD83D\uDC44,\uD83D\uDC43,\uD83D\uDC85,\uD83C\uDF51\n" +
            "        ,\uD83C\uDF4C,\uD83C\uDF46,\uD83D\uDC59,\uD83E\uDE73,\uD83E\uDE72,\uD83D\uDC84,\uD83D\uDC60,sex,dm,aajao,paid,service,satisf,paytm,akeli,lgbtq,bisexual,transgender,trans,gayboy,\n" +
            "        lgbtqia,lgbtpride,nonbinary,pridemonth,gayman,dragqueen,gaylove,gaymen,gaylife,follow,lgbtcommunity,lovewins,\n" +
            "        instagood,drag,darling,allah,muhammed,paidfun,love,sex,beautiful,babe,maja,dungi,call,dirty,naughty,seduce,seductive," +
            "callgirl,sexy,service,escort";
    final List<String> offensiveWordStrings = Arrays.asList(offensiveWords.split(",")).stream().map(str->str.trim()).collect(Collectors.toList());
}
