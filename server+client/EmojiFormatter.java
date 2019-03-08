import com.vdurmont.emoji.EmojiParser;

class EmojiFormatter {

    String toEmoji(String input){
        try {
            String formatted = EmojiParser.parseToUnicode(input);
            return formatted;
        }catch(NullPointerException n){
            return input;
        }
    }
    String toPlainText(String input) {
        try {
            String formatted = EmojiParser.parseToAliases(input);
            return formatted;
        } catch (NullPointerException n) {
            return input;
        }
    }
}
